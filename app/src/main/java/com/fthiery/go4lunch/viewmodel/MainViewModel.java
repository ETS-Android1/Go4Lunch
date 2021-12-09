package com.fthiery.go4lunch.viewmodel;

import android.content.Context;
import android.location.Location;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.model.User;
import com.fthiery.go4lunch.repository.RestaurantRepository;
import com.fthiery.go4lunch.repository.UserRepository;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class MainViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    private final MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();
    private final Map<String, Restaurant> restaurants = new ConcurrentHashMap<>();

    private final CompositeDisposable disposables = new CompositeDisposable();
    private Disposable usersDisposable = null;
    private final Disposable numberDisposable;

    private LatLng location;
    private int numberOfUsers = 1;


    public MainViewModel() {
        super();
        userRepository = UserRepository.getInstance();
        restaurantRepository = RestaurantRepository.getInstance();
        numberDisposable = userRepository.watchNumberOfUsers().subscribe(this::updateRatings);
    }

    private void updateRatings(Integer n) {
        numberOfUsers = n;
        for (Restaurant restaurant : restaurants.values()) {
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
        return userRepository.signOut(context);
    }

    public Task<Void> deleteUser(Context context) {
        return userRepository.deleteUser(context);
    }

    public void setLocation(LatLng latlng) {
        location = latlng;
        searchRestaurants("");
    }

    public void setLocation(Location location) {
        setLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    public void searchRestaurants(String query) {

        restaurantRepository.searchRestaurants(query, location).subscribe(result -> {
            restaurants.clear();
            disposables.clear();
            for (String restaurantId : result) {
                disposables.add(restaurantRepository.watchRestaurant(restaurantId).subscribe(restaurant -> {
                    restaurant.updateRating(numberOfUsers);
                    restaurant.updateDistanceTo(location);
                    disposables.add(userRepository.watchUsersEatingAt(restaurant.getId()).subscribe(users -> {
                        restaurant.setWorkmates(users.size());
                        updateRestaurantMap(restaurant, result.size());
                    }));
                    updateRestaurantMap(restaurant, result.size());
                }));
            }
        });
    }

    private void updateRestaurantMap(Restaurant restaurant, int nItems) {
        restaurants.put(restaurant.getId(), new Restaurant(restaurant));
        if (restaurants.size() == nItems)
            restaurantsLiveData.postValue(new ArrayList<>(restaurants.values()));
    }

    public LiveData<List<Restaurant>> getRestaurantsLiveData() {
        return restaurantsLiveData;
    }

    public LiveData<List<User>> getWorkmatesLiveData() {
        MutableLiveData<List<User>> workmatesLiveData = new MutableLiveData<>();
        if (usersDisposable != null) usersDisposable.dispose();

        usersDisposable = userRepository.watchAllUsers().subscribe(users -> {
            for (User user : users) {
                restaurantRepository.getRestaurant(user.getChosenRestaurantId()).subscribe(restaurant -> {
                    user.setChosenRestaurant(restaurant);
                    users.set(users.indexOf(user), new User(user));
                    workmatesLiveData.postValue(users);
                });
            }
        });

        return workmatesLiveData;
    }

    public void createUser() {
        userRepository.createUser();
    }

    public void stopListening() {
        disposables.clear();
        numberDisposable.dispose();
        if (usersDisposable != null) usersDisposable.dispose();
    }
}
