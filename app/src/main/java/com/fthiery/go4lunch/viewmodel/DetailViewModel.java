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
            listeners.add(userRepository.listenNumberOfUsers(numberOfUsers -> {
                restaurant.updateRating(numberOfUsers);
                restaurantLiveData.postValue(new Restaurant(restaurant));
            }));
        }));
        return restaurantLiveData;
    }

    public LiveData<List<User>> requestWorkmatesEatingAtRestaurant(String restaurantId) {
        MutableLiveData<List<User>> workmatesLiveData = new MutableLiveData<>();
        listeners.add(userRepository.listenUsersEatingAt(restaurantId, users -> {
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

    public void toggleChosenRestaurant(String restaurantId, Callback<Boolean> callback) {
        getChosenRestaurant(chosenRestaurantId -> {
            if (chosenRestaurantId != null && chosenRestaurantId.equals(restaurantId)) {
                userRepository.setChosenRestaurant("",null);
                callback.onSuccess(false);
            } else {
                userRepository.setChosenRestaurant(restaurantId, null);
                callback.onSuccess(true);
            }
        });
    }

    public void getChosenRestaurant(Callback<String> restaurantId) {
        userRepository.getChosenRestaurant(getUserId(), restaurantId);
    }

    public String getUserId() {
        return userRepository.getCurrentUserUID();
    }

    public void toggleLike(Restaurant restaurant, Callback<Restaurant> callback) {
        restaurant.toggleLike(userRepository.getCurrentUserUID());
        restaurantRepository.addRestaurantToFirebase(restaurant, callback);
    }
}
