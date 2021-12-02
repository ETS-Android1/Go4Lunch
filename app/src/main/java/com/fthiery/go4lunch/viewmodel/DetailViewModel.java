package com.fthiery.go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.model.User;
import com.fthiery.go4lunch.repository.RestaurantRepository;
import com.fthiery.go4lunch.repository.UserRepository;
import com.fthiery.go4lunch.utils.Callback;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class DetailViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final List<ListenerRegistration> listeners = new ArrayList<>();

    public DetailViewModel() {
        super();

        userRepository = UserRepository.getInstance();
        restaurantRepository = RestaurantRepository.getInstance();
    }

    public LiveData<Restaurant> requestRestaurantDetails(String id) {
        MutableLiveData<Restaurant> restaurantLiveData = new MutableLiveData<>();
        listeners.add(restaurantRepository.listenRestaurant(id, restaurant -> {
            restaurantLiveData.postValue(restaurant);
            listeners.add(userRepository.listenToNumberOfUsers(numberOfUsers -> {
                restaurant.updateRating(numberOfUsers);
                restaurantLiveData.postValue(new Restaurant(restaurant));
            }));
        }));
        return restaurantLiveData;
    }

    public LiveData<List<User>> requestWorkmatesEatingAtRestaurant(String restaurantId) {
        MutableLiveData<List<User>> workmatesLiveData = new MutableLiveData<>();
        listeners.add(userRepository.listenToUsersEatingAt(restaurantId, users -> {
            for (User user : users) {
                restaurantRepository.getRestaurant(user.getChosenRestaurantId(), chosenRestaurant -> {
                    user.setChosenRestaurant(chosenRestaurant);
                    workmatesLiveData.postValue(users);
                });
            }
        }));
        return workmatesLiveData;
    }

    public void stopListening() {
        for (ListenerRegistration registration : listeners) {
            registration.remove();
        }
        listeners.clear();
    }

    public void toggleChosenRestaurant(String restaurantId) {
        userRepository.setChosenRestaurant(restaurantId, null);
    }

    public String getUserId() {
        return userRepository.getCurrentUserUID();
    }

    public void toggleLike(Restaurant restaurant, Callback<Restaurant> callback) {
        restaurant.toggleLike(userRepository.getCurrentUserUID());
        restaurantRepository.addRestaurantToFirebase(restaurant, callback);
    }
}
