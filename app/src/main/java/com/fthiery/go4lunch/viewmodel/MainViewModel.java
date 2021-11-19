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
import com.fthiery.go4lunch.utils.Callback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();
    private final Map<String, ListenerRegistration> registrations = new ConcurrentHashMap<>();
    private ListenerRegistration listener;
    private final List<Callback<LatLng>> locationListeners = new ArrayList<>();
    private LatLng location;

    public MainViewModel() {
        super();
        userRepository = UserRepository.getInstance();
        restaurantRepository = RestaurantRepository.getInstance();
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

    public void setLocation(LatLng latlng, Callback<List<Restaurant>> onLoaded) {
        location = latlng;
        for (Callback<LatLng> locationListener : locationListeners) locationListener.onSuccess(latlng);
        updateRestaurantsAround(latlng, onLoaded);
    }

    public void setLocation(Location location, Callback<List<Restaurant>> onLoaded) {
        setLocation(new LatLng(location.getLatitude(), location.getLongitude()), onLoaded);
    }

    public void registerLocationUpdates(Callback<LatLng> callback) {
        locationListeners.add(callback);
        callback.onSuccess(location);
    }

    private void updateRestaurantsAround(LatLng location, Callback<List<Restaurant>> onLoaded) {
        restaurantRepository.updateRestaurantsAround(location, 800, restaurantIds -> {
            // When we get the restaurant list, request the list of users eating there

            Map<String,Restaurant> restaurants = new ConcurrentHashMap<>();

            // Clear registrations
            ClearRegistrations();

            for (String restaurantId : restaurantIds) {
                restaurantRepository.getRestaurant(restaurantId, restaurant -> {

                    updateRestaurantMap(restaurants, restaurant);
                    if (restaurants.size() == restaurantIds.size()) {
                        onLoaded.onSuccess(new ArrayList<>(restaurants.values()));
                    }

                    registerLocationUpdates(latLng -> {
                        restaurant.updateDistanceTo(location);
                        updateRestaurantMap(restaurants, restaurant);
                    });

                    registrations.put(
                            restaurantId,
                            userRepository.listenToUsersEatingAt(restaurantId, users -> {
                                // When the users eating at the restaurantId are updated,
                                // create a new instance of the restaurantId and of the list
                                // to force the livedata listeners to update
                                restaurant.setWorkmates(users.size());
                                updateRestaurantMap(restaurants, restaurant);
                            })
                    );
                });
            }
        });
    }

    private void updateRestaurantMap(Map<String, Restaurant> restaurants, Restaurant restaurant) {
        restaurants.put(restaurant.getId(), new Restaurant(restaurant));
        restaurantsLiveData.postValue(new ArrayList<>(restaurants.values()));
    }

    private void ClearRegistrations() {
        for (ListenerRegistration registration : registrations.values()) {
            if (registration != null) registration.remove();
        }
        registrations.clear();
    }

    public LiveData<List<Restaurant>> getRestaurantsLiveData() {
        return restaurantsLiveData;
    }

    public LiveData<List<User>> getWorkmatesLiveData() {
        MutableLiveData<List<User>> workmatesLiveData = new MutableLiveData<>();
        if (listener != null) listener.remove();

        listener = userRepository.requestAllUsers(users -> {
            for (User user : users) {
                restaurantRepository.getRestaurant(user.getChosenRestaurantId(), chosenRestaurant -> {
                    User newUser = new User(user);
                    newUser.setChosenRestaurant(chosenRestaurant);
                    users.set(users.indexOf(user), newUser);
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
        ClearRegistrations();
        if (listener != null) listener.remove();
    }
}
