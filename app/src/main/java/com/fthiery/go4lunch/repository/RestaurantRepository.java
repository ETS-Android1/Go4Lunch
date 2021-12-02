package com.fthiery.go4lunch.repository;

import static com.fthiery.go4lunch.utils.RestaurantService.retrofit;

import android.util.Log;

import androidx.annotation.NonNull;

import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceDetailResponse;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceNearbyResponse;
import com.fthiery.go4lunch.utils.Callback;
import com.fthiery.go4lunch.utils.RestaurantService;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class RestaurantRepository {

    private static volatile RestaurantRepository instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final RestaurantService service = retrofit.create(RestaurantService.class);

    public static RestaurantRepository getInstance() {
        RestaurantRepository result = instance;

        if (result != null) {
            return result;
        }
        synchronized (RestaurantRepository.class) {
            if (instance == null) {
                instance = new RestaurantRepository();
            }
            return instance;
        }
    }

    /**
     * Start listening to Firebase updates to the restaurant
     **/
    public ListenerRegistration listenRestaurant(String restaurantId, Callback<Restaurant> callback) {

        if (restaurantId == null || restaurantId.equals("")) {
            callback.onSuccess(null);
            return null;
        }

        return db.collection("restaurants")
                .document(restaurantId)
                .addSnapshotListener((document, error) -> {
                    if (document != null && document.exists()) {
                        Restaurant restaurant = document.toObject(Restaurant.class);
                        callback.onSuccess(restaurant);
                    } else {
                        getRestaurantDetails(restaurantId, callback);
                    }
                });
    }

    /**
     * Try to get the restaurant from Firebase. On failure, fetch the details from Google Place API and add it to Firebase
     **/
    public void getRestaurant(String restaurantId, Callback<Restaurant> callback) {

        if (restaurantId == null || restaurantId.equals("")) {
            callback.onSuccess(null);
            return;
        }

        db.collection("restaurants")
                .document(restaurantId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // The restaurant exists in firebase, fetch it, then add it to the map and return it
                        Restaurant restaurant = document.toObject(Restaurant.class);
                        callback.onSuccess(restaurant);
                    } else {
                        getRestaurantDetails(restaurantId, callback);
                    }
                });
    }

    /**
     * Fetch place details from Google Place API using Retrofit
     **/
    private void getRestaurantDetails(String restaurantId, Callback<Restaurant> callback) {
        Call<GooglePlaceDetailResponse> call = service.getPlacesInfo(restaurantId);

        call.enqueue(new retrofit2.Callback<GooglePlaceDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<GooglePlaceDetailResponse> call, @NonNull Response<GooglePlaceDetailResponse> response) {
                if (response.body() != null) {
                    Restaurant restaurant = response.body().getResult();
                    addRestaurantToFirebase(restaurant, callback);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GooglePlaceDetailResponse> call, Throwable t) {
                Log.e("RestaurantRepository", "Unable to get details: ", t);
            }
        });
    }

    /**
     * Get a list of restaurant ids from Google Place API using Retrofit
     **/
    public void updateRestaurantsAround(LatLng location, int radius, Callback<List<String>> callback) {
        String latLng = location.latitude + "," + location.longitude;

        Call<GooglePlaceNearbyResponse> call = service.getNearbyPlaces(latLng);
        call.enqueue(new retrofit2.Callback<GooglePlaceNearbyResponse>() {
            @Override
            public void onResponse(@NonNull Call<GooglePlaceNearbyResponse> call, @NonNull Response<GooglePlaceNearbyResponse> response) {
                if (response.body() != null) {
                    callback.onSuccess(response.body().getRestaurantIds());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GooglePlaceNearbyResponse> call, @NonNull Throwable t) {
            }
        });
    }

    /**
     * Get a list of restaurant ids corresponding to the keyword from Google Place API
     **/
    public void searchRestaurants(String keyword, LatLng location, Callback<List<String>> callback) {

        String latLng = location.latitude + "," + location.longitude;
        Call<GooglePlaceNearbyResponse> call;

        if (keyword.equals("")) {
            call = service.getNearbyPlaces(latLng);
        } else {
            call = service.searchPlaces(latLng, keyword);
        }

        call.enqueue(new retrofit2.Callback<GooglePlaceNearbyResponse>() {
            @Override
            public void onResponse(@NonNull Call<GooglePlaceNearbyResponse> call, @NonNull Response<GooglePlaceNearbyResponse> response) {
                if (response.body() != null) {
                    callback.onSuccess(response.body().getRestaurantIds());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GooglePlaceNearbyResponse> call, @NonNull Throwable t) {
            }
        });
    }

    /**
     * Add or update the restaurant in Firebase
     **/
    public void addRestaurantToFirebase(Restaurant restaurant, Callback<Restaurant> callback) {
        if (restaurant.getId() != null) {
            db.collection("restaurants")
                    .document(restaurant.getId())
                    .set(restaurant)
                    .addOnSuccessListener(unused -> {
                        if (callback != null) callback.onSuccess(restaurant);
                    });
        } else {
            Log.w("RestaurantRepository", "createRestaurant: Missing Id");
        }
    }

    private void onApiException(Exception exception) {
        if (exception instanceof ApiException) {
            final ApiException apiException = (ApiException) exception;
            Log.e("RestaurantRepository", "Place not found: " + exception.getMessage());
            final int statusCode = apiException.getStatusCode();
        }
    }
}
