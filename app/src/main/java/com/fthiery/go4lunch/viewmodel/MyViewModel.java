package com.fthiery.go4lunch.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fthiery.go4lunch.BuildConfig;
import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.repository.RestaurantRepository;
import com.fthiery.go4lunch.repository.UserRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.RankBy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();
    private Location lastKnownLocation;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GeoApiContext context;

    public MyViewModel() {
        super();
        userRepository = UserRepository.getInstance();
        restaurantRepository = RestaurantRepository.getInstance();
        context = new GeoApiContext.Builder().apiKey(BuildConfig.MAPS_API_KEY).build();
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public Boolean isCurrentUserLogged(){
        return (this.getCurrentUser() != null);
    }

    public Task<Void> signOut(Context context){
        return userRepository.signOut(context);
    }

    public Task<Void> deleteUser(Context context) {
        return userRepository.deleteUser(context);
    }

    public Location getDeviceLocation() {
        return lastKnownLocation;
    }

    @SuppressLint("MissingPermission")
    private void updateDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnSuccessListener(location -> lastKnownLocation = location);
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    public List<Restaurant> getRestaurantsAround(double latitude, double longitude) {
        LatLng location = new LatLng(latitude, longitude);
        PlacesSearchResponse request = new PlacesSearchResponse();
        List<Restaurant> restaurants = new ArrayList<>();

        try {
            request = PlacesApi.nearbySearchQuery(context, location)
                    .radius(1500)
                    .type(PlaceType.RESTAURANT)
                    .await();
        } catch (ApiException | IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (request.results != null) {
                for (PlacesSearchResult result : request.results) {
                    Restaurant restaurant = new Restaurant();

                    restaurant.setId(result.placeId);
                    restaurant.setName(result.name);

                    restaurant.setLocation(result.geometry.location.lat,result.geometry.location.lng);

                    restaurant.setAddress(result.formattedAddress);
//                    restaurant.setPhoto(result.photos[0].photoReference);

//                    restaurantRepository.createRestaurant(restaurant);
                    restaurants.add(restaurant);
                }
            }
        }

        restaurantsLiveData.setValue(restaurants);

        return restaurants;
    }

    public MutableLiveData<List<Restaurant>> getRestaurantsLiveData() {
        return restaurantsLiveData;
    }
}
