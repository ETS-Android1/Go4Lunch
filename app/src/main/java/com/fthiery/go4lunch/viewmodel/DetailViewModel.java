package com.fthiery.go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.model.User;
import com.fthiery.go4lunch.repository.RestaurantRepository;
import com.fthiery.go4lunch.repository.UserRepository;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class DetailViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final List<ListenerRegistration> registrations = new ArrayList<>();

    public DetailViewModel() {
        super();

        userRepository = UserRepository.getInstance();
        restaurantRepository = RestaurantRepository.getInstance();
    }

    public LiveData<Restaurant> requestRestaurantDetails(String id) {
        MutableLiveData<Restaurant> restaurantLiveData = new MutableLiveData<>();
        registrations.add(restaurantRepository.addRestaurantListener(id, restaurantLiveData::postValue));
        return restaurantLiveData;
    }

    public LiveData<List<User>> requestWorkmatesEatingAtRestaurant(String restaurantId) {
        MutableLiveData<List<User>> workmates = new MutableLiveData<>();
        registrations.add(userRepository.requestUsersEatingAt(restaurantId, workmates::postValue));
        return workmates;
    }

    public void stopListening() {
        for (ListenerRegistration registration : registrations) {
            registration.remove();
        }
        registrations.clear();
    }

    public void toggleChosenRestaurant(String restaurantId) {
        userRepository.setChosenRestaurant(restaurantId, unused -> {});
    }
}
