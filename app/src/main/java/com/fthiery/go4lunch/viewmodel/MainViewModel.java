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
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final ListenerRegistration userRegistration;
    private final RestaurantRepository restaurantRepository;
    private Location lastKnownLocation;
    private final MutableLiveData<List<Restaurant>> restaurants = new MutableLiveData<>();
    private final MutableLiveData<List<User>> workmates = new MutableLiveData<>();
    private final List<String> chosenRestaurants = new ArrayList<>();

    public MainViewModel() {
        super();

        userRepository = UserRepository.getInstance();
        // When Users are modified, update the chosen restaurants list
        userRegistration = userRepository.setListener((List<User> users) -> {
            // When Users are modified, update the chosen restaurants list
            chosenRestaurants.clear();
            for (User user : users) {
                if (user.getChosenRestaurantId() != null) {
                    chosenRestaurants.add(user.getChosenRestaurantId());
                }
            }
            workmates.postValue(users);
        });

        restaurantRepository = RestaurantRepository.getInstance();
        restaurantRepository.setListener(restaurantList -> {
            // When Restaurants are modified, update chosen status
            // TODO : Not a very good way to do it, maybe should use a Map<User,Restaurant>
            for (Restaurant restaurant : restaurantList) {
                restaurant.setChosen(chosenRestaurants.contains(restaurant.getId()));
            }
            restaurants.postValue(restaurantList);
        });

    }

    @Nullable
    public FirebaseUser getCurrentUser() {
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

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    public void setLocation(Location location) {
        lastKnownLocation = location;
        updateRestaurants();
    }

    private void updateRestaurants() {
        LatLng location = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        restaurantRepository.updateRestaurantsAround(location);
    }

    public MutableLiveData<List<Restaurant>> getRestaurantsLiveData() {
        return restaurants;
    }

    public LiveData<List<User>> getWorkmatesLiveData() {
        return workmates;
    }

    public void setPlacesClient(PlacesClient placesClient) {
        restaurantRepository.setPlacesClient(placesClient);
    }

    public void createUser() {
        userRepository.createUser();
    }

}
