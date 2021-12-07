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

public class MainViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();
    private final Map<String, Restaurant> restaurants = new ConcurrentHashMap<>();
    private final List<ListenerRegistration> listeners = new ArrayList<>();
    private final List<Callback<LatLng>> locationListeners = new ArrayList<>();
    private int numberOfUsers = 1;
    private ListenerRegistration usersListener;
    private final ListenerRegistration numberListener;
    private LatLng location;

    public MainViewModel() {
        super();
        userRepository = UserRepository.getInstance();
        restaurantRepository = RestaurantRepository.getInstance();
        numberListener = userRepository.listenNumberOfUsers(this::updateRatings);
    }

    private void updateRatings(Integer n) {
        numberOfUsers = n;
        for (Restaurant restaurant : restaurants.values()) {
            restaurant.updateRating(numberOfUsers);
        }
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
        for (Callback<LatLng> locationListener : locationListeners)
            locationListener.onSuccess(latlng);
        searchRestaurants("", onLoaded);
    }

    public void setLocation(Location location, Callback<List<Restaurant>> onLoaded) {
        setLocation(new LatLng(location.getLatitude(), location.getLongitude()), onLoaded);
    }

    public void registerLocationUpdates(Callback<LatLng> callback) {
        locationListeners.add(callback);
        callback.onSuccess(location);
    }

    public void searchRestaurants(String query, Callback<List<Restaurant>> onLoaded) {

        restaurantRepository.searchRestaurants(query, location, restaurantIds -> {
            // When we get the restaurant list, request the list of users eating there
            restaurants.clear();

            // Clear registrations
            ClearRegistrations();

            if (restaurantIds.size() == 0) {
                restaurantsLiveData.postValue(new ArrayList<>(restaurants.values()));
            }

            for (String restaurantId : restaurantIds) {
                // Start listening to updates to this restaurant
                listeners.add(restaurantRepository.listenRestaurant(restaurantId, restaurant -> {

                    // Add the restaurant to the HashMap and update the LiveData
                    updateRestaurantMap(restaurant);

                    if (restaurants.size() == restaurantIds.size()) {
                        onLoaded.onSuccess(new ArrayList<>(restaurants.values()));
                    }

                    // Update the distance
                    registerLocationUpdates(latLng -> {
                        restaurant.updateDistanceTo(location);
                        updateRestaurantMap(restaurant);
                    });

                    // Update the rating
                    restaurant.updateRating(numberOfUsers);

                    // Listen to updates to the list of users eating at this restaurant
                    listeners.add(
                            userRepository.listenUsersEatingAt(restaurantId, users -> {
                                // When the users eating at the restaurantId are updated,
                                // create a new instance of the restaurantId and of the list
                                // to force the livedata listeners to update
                                restaurant.setWorkmates(users.size());
                                updateRestaurantMap(restaurant);
                            })
                    );
                }));
            }
        });
    }

    private void updateRestaurantMap(Restaurant restaurant) {
        restaurants.put(restaurant.getId(), new Restaurant(restaurant));
        restaurantsLiveData.postValue(new ArrayList<>(restaurants.values()));
    }

    public LiveData<List<Restaurant>> getRestaurantsLiveData() {
        return restaurantsLiveData;
    }

    public LiveData<List<User>> getWorkmatesLiveData() {
        MutableLiveData<List<User>> workmatesLiveData = new MutableLiveData<>();
        if (usersListener != null) usersListener.remove();

        usersListener = userRepository.listenAllUsers(users -> {
            for (User user : users) {
                restaurantRepository.getRestaurant(user.getChosenRestaurantId(), chosenRestaurant -> {
                    user.setChosenRestaurant(chosenRestaurant);
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

    private void ClearRegistrations() {
        for (ListenerRegistration registration : listeners) {
            if (registration != null) registration.remove();
        }
        listeners.clear();
    }

    public void stopListening() {
        ClearRegistrations();
        if (usersListener != null) usersListener.remove();
        if (numberListener != null) numberListener.remove();
    }
}
