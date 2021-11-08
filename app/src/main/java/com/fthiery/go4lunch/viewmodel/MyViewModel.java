package com.fthiery.go4lunch.viewmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fthiery.go4lunch.BuildConfig;
import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.repository.RestaurantRepository;
import com.fthiery.go4lunch.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.GeoApiContext;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PendingResult;
import com.google.maps.PlacesApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final GeoApiContext geoApiContext;
    private Location lastKnownLocation;
    private MutableLiveData<List<Restaurant>> restaurants = new MutableLiveData<>();
    private List<String> chosenRestaurants = new ArrayList<>();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    List<Place.Field> placeFields = Arrays.asList(
            Place.Field.ADDRESS,
            Place.Field.PHONE_NUMBER,
            Place.Field.OPENING_HOURS,
            Place.Field.WEBSITE_URI,
            Place.Field.PHOTO_METADATAS);

    public MyViewModel() {
        super();

        userRepository = UserRepository.getInstance();
        userRepository.setListener(restaurants -> chosenRestaurants = restaurants);

        restaurantRepository = RestaurantRepository.getInstance();
        restaurantRepository.setListener(restaurantList -> {
            for (Restaurant restaurant : restaurantList) {
                restaurant.setChosen(chosenRestaurants.contains(restaurant.getId()));
            }
            restaurants.postValue(restaurantList);
        });

        geoApiContext = new GeoApiContext.Builder().apiKey(BuildConfig.MAPS_API_KEY).build();
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

    public void setPlacesClient(PlacesClient placesClient) {
        restaurantRepository.setPlacesClient(placesClient);
    }

    public void createUser() {
        userRepository.createUser();
    }
}
