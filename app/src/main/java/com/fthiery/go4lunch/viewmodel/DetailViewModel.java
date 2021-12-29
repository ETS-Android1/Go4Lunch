package com.fthiery.go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.model.User;
import com.fthiery.go4lunch.repository.RestaurantRepository;
import com.fthiery.go4lunch.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class DetailViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public DetailViewModel() {
        this(UserRepository.getInstance(),RestaurantRepository.getInstance());
    }

    public DetailViewModel(UserRepository userRepository, RestaurantRepository restaurantRepository) {
        super();
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public LiveData<Restaurant> watchRestaurantDetails(String id) {
        MutableLiveData<Restaurant> restaurantLiveData = new MutableLiveData<>();

        disposables.add(
                restaurantRepository
                        .watchRestaurant(id)
                        .flatMap(this::updateRating)
                        .subscribe(restaurantLiveData::postValue));

        return restaurantLiveData;
    }

    private Observable<Restaurant> updateRating(Restaurant restaurant) {
        return userRepository.watchNumberOfUsers()
                .map(nTotalUsers -> {
                    restaurant.updateRating(nTotalUsers);
                    return restaurant;
                });
    }

    public LiveData<List<User>> watchWorkmatesEatingAtRestaurant(String restaurantId) {
        MutableLiveData<List<User>> workmatesLiveData = new MutableLiveData<>();
        Map<String, User> users = new ConcurrentHashMap<>();

        disposables.add(
                userRepository
                        .watchUsersEatingAt(restaurantId)
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

    public void stopListening() {
        disposables.clear();
    }

    public void toggleChosenRestaurant(String restaurantId) {
        disposables.add(
                userRepository
                        .getChosenRestaurant(getUserId())
                        .subscribe(chosenRestaurantId -> {
                            if (chosenRestaurantId != null && chosenRestaurantId.equals(restaurantId)) {
                                userRepository.setChosenRestaurant("");
                            } else {
                                userRepository.setChosenRestaurant(restaurantId);
                            }
                        })
        );
    }

    public LiveData<String> watchChosenRestaurant() {
        MutableLiveData<String> chosenRestaurant = new MutableLiveData<>();
        disposables.add(
                userRepository
                        .watchChosenRestaurant(getUserId())
                        .subscribe(chosenRestaurant::postValue));
        return chosenRestaurant;
    }

    public String getUserId() {
        return userRepository.getCurrentUserId();
    }

    public void toggleLike(Restaurant restaurant) {
        restaurant.toggleLike(userRepository.getCurrentUserId());
        restaurantRepository.addRestaurantToFirebase(restaurant);
    }
}
