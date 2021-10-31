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
    private final MutableLiveData<List<Restaurant>> restaurantsLiveData = new MutableLiveData<>();
    private final GeoApiContext geoApiContext;
    private Location lastKnownLocation;
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
        restaurantRepository = RestaurantRepository.getInstance();
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

        NearbySearchRequest request = PlacesApi.nearbySearchQuery(geoApiContext, location).radius(1500).type(PlaceType.RESTAURANT);
        request.setCallback(new PendingResult.Callback<PlacesSearchResponse>() {
            @Override
            public void onResult(PlacesSearchResponse response) {
                List<Restaurant> restaurants = new ArrayList<>();
                for (PlacesSearchResult result : response.results) {
                    Restaurant restaurant = new Restaurant();

                    restaurant.setId(result.placeId);
                    restaurant.setName(result.name);

                    restaurant.setLocation(result.geometry.location.lat, result.geometry.location.lng);

                    getDetails(restaurant, result.placeId);

                    restaurants.add(restaurant);
                }
                restaurantsLiveData.postValue(restaurants);
            }

            @Override
            public void onFailure(Throwable e) {

            }
        });
    }

    private void getDetails(Restaurant restaurant, String placeId) {
        // Uses Place API to get the details of a restaurant
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();

                    restaurant.setAddress(place.getAddress());
                    restaurant.setPhoneNumber(place.getPhoneNumber());
                    restaurant.setOpeningHours(place.getOpeningHours());
                    restaurant.setWebsiteUrl(place.getWebsiteUri());

                    // Add the restaurant to Firebase database
                    restaurantRepository.addRestaurantToFirebase(restaurant);

                    getPhoto(restaurant, place);

                }).addOnFailureListener(this::onApiException);
    }

    private void getPhoto(Restaurant restaurant, Place place) {
        // Get the photo metadata.
        final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
        if (metadata == null || metadata.isEmpty()) {
            Log.w("MyViewModel", "No photo metadata.");
            return;
        }
        final PhotoMetadata photoMetadata = metadata.get(0);

        // Create a FetchPhotoRequest.
        final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(1000) // Optional.
                .setMaxHeight(800) // Optional.
                .build();

        placesClient.fetchPhoto(photoRequest)
                .addOnSuccessListener((fetchPhotoResponse) -> {
                    // Fetch the bitmap from Place Api
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    // Create a storage reference for the bitmap
                    StorageReference storageRef = storage.getReference().child("restaurant_pictures/" + place.getId() + ".jpg");

                    // Compress the bitmap
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                    byte[] data = baos.toByteArray();

                    // Upload the bitmap to Firebase Storage
                    UploadTask uploadTask = storageRef.putBytes(data);
                    Task<Uri> urlTask = uploadTask
                            .continueWithTask(task -> storageRef.getDownloadUrl())
                            .addOnCompleteListener(task -> {
                                // Set restaurant photo uri
                                restaurant.setPhoto(task.getResult());
                                restaurantRepository.addRestaurantToFirebase(restaurant);
                            });

                }).addOnFailureListener(this::onApiException);
    }


    public MutableLiveData<List<Restaurant>> getRestaurantsLiveData() {
        return restaurantsLiveData;
    }

    public void setPlacesClient(PlacesClient placesClient) {
        restaurantRepository.setPlacesClient(placesClient);
    }
}
