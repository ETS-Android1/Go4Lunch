package com.fthiery.go4lunch.viewmodel;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.model.User;
import com.fthiery.go4lunch.repository.RestaurantRepository;
import com.fthiery.go4lunch.repository.UserRepository;
import com.fthiery.go4lunch.utils.Sort;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class MainViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    private final MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();
    private final Map<String, Restaurant> restaurantsMap = new ConcurrentHashMap<>();

    private final CompositeDisposable disposables = new CompositeDisposable();
    private Disposable restaurantsDisposable;

    private LatLng location;
    private int numberOfUsers = 1;
    AtomicInteger nRestaurants = new AtomicInteger();
    private int sort = 0;

    public MainViewModel() {
        this(UserRepository.getInstance(), RestaurantRepository.getInstance());
        disposables.add(userRepository.watchNumberOfUsers().subscribe(this::updateRatings));
    }

    public MainViewModel(UserRepository userRepository, RestaurantRepository restaurantRepository) {
        super();
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    private void updateRatings(Integer n) {
        numberOfUsers = n;
        for (Restaurant restaurant : restaurantsMap.values()) {
            restaurant.updateRating(numberOfUsers);
        }
    }

    @Nullable
    private FirebaseUser getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public Boolean isCurrentUserLogged() {
        return (this.getCurrentUser() != null);
    }

    public Task<Void> signOut(Context context) {
        disposables.clear();
        return userRepository.signOut(context);
    }

    public Task<Void> deleteUser(Context context) {
        return userRepository.deleteUser(context);
    }

    public void setLocation(LatLng latlng) {
        location = latlng;
        getRestaurants("");
    }

    public void setLocation(Location location) {
        setLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    public void getRestaurants(String query) {
        restaurantsMap.clear();
        if (restaurantsDisposable != null) restaurantsDisposable.dispose();

        restaurantsDisposable = restaurantRepository.searchRestaurants(query, location)
                .doOnSuccess(restaurants -> nRestaurants.set(restaurants.size()))
                .flatMapObservable(Observable::fromIterable)
                .flatMap(restaurantRepository::watchRestaurant)
                .flatMap(this::updateWorkmates)
                .map(restaurant -> {
                    restaurant.updateDistanceTo(location);
                    restaurant.updateRating(numberOfUsers);
                    return restaurant;
                })
                .subscribe(this::updateRestaurantMap, throwable -> Log.e("getRestaurants", "error: ", throwable));

        disposables.add(restaurantsDisposable);
    }

    private Observable<Restaurant> updateWorkmates(Restaurant restaurant) {
        return userRepository.watchUsersEatingAt(restaurant.getId())
                .map(users -> {
                    restaurant.setWorkmates(users.size());
                    return restaurant;
                });
    }

    private void updateRestaurantMap(Restaurant restaurant) {
        restaurantsMap.put(restaurant.getId(), new Restaurant(restaurant));
        updateRestaurantMap();
    }

    private void updateRestaurantMap() {
        // Builds a list from restaurantsMap then sorts it and updates the LiveData
        if (restaurantsMap.size() == nRestaurants.get()) {
            ArrayList<Restaurant> restaurants = new ArrayList<>(restaurantsMap.values());

            Collections.sort(restaurants, (left, right) -> {
                int comp = 0;

                if (sort == Sort.BY_RATING)
                    comp = (right.getRating() - left.getRating()) * 100000;
                else if (sort == Sort.BY_WORKMATES)
                    comp = (right.getWorkmates() - left.getWorkmates()) * 100000;
                else if (sort == Sort.BY_OPENING_HOUR)
                    comp = right.howLongStillOpen() - left.howLongStillOpen();

                if (comp == 0) comp += left.getDistance() - right.getDistance();

                return comp;
            });

            restaurantsLiveData.postValue(restaurants);
        }
    }

    public LiveData<List<Restaurant>> getRestaurantsLiveData() {
        return restaurantsLiveData;
    }

    public LiveData<List<User>> getWorkmatesLiveData() {
        MutableLiveData<List<User>> workmatesLiveData = new MutableLiveData<>();
        Map<String, User> users = new ConcurrentHashMap<>();

        disposables.add(
                userRepository
                        .watchAllUsers()
                        .flatMap(Observable::fromIterable)
                        .flatMap(this::updateChosenRestaurant)
                        .subscribe(user -> {
                            users.put(user.getId(), new User(user));
                            workmatesLiveData.postValue(new ArrayList<>(users.values()));
                        })
        );

        return workmatesLiveData;
    }

    private Observable<User> updateChosenRestaurant(User user) {
        if (user.getChosenRestaurantId().equals(""))
            return Observable.just(user);
        else return restaurantRepository
                .getRestaurant(user.getChosenRestaurantId())
                .map(restaurant -> {
                    user.setChosenRestaurant(restaurant);
                    return user;
                })
                .toObservable();
    }

    public LiveData<User> getUser() {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        disposables.add(
                userRepository.watchUser().subscribe(
                        userLiveData::postValue,
                        throwable -> Log.e("userRepository", "onError: ", throwable)
                )
        );
        return userLiveData;
    }

    public void createUser() {
        userRepository.createUser();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }

    public void setSort(int sortType) {
        sort = sortType;
        updateRestaurantMap();
    }
}
