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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

    private CollectionReference getRestaurantsCollection() {
        return db.collection("restaurants");
    }

    public void getRestaurant(String restaurantId, Callback<Restaurant> callback) {

        if (restaurantId == null || restaurantId.equals("")) {
            callback.onSuccess(null);
            return;
        }

        getRestaurantsCollection().document(restaurantId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // The restaurant exists in firebase, fetch it, then add it to the map and return it
                        Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                        callback.onSuccess(restaurant);
                    } else {
                        Call<GooglePlaceDetailResponse> call = service.getPlacesInfo(restaurantId);
                        call.enqueue(new retrofit2.Callback<GooglePlaceDetailResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<GooglePlaceDetailResponse> call, @NonNull Response<GooglePlaceDetailResponse> response) {
                                if (response.body() != null) {
                                    Restaurant restaurant = response.body().getResult();
                                    addRestaurantToFirebase(restaurant);
                                    callback.onSuccess(restaurant);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<GooglePlaceDetailResponse> call, Throwable t) {
                                Log.e("RestaurantRepository", "Unable to get details: ", t);
                            }
                        });
                    }
                });
    }

    public void updateRestaurantsAround(LatLng location, int radius, Callback<List<String>> callback) {
        String latLng = location.latitude + "," + location.longitude;

        Call<GooglePlaceNearbyResponse> call = service.getNearbyPlaces(latLng, radius);
        call.enqueue(new retrofit2.Callback<GooglePlaceNearbyResponse>() {
            @Override
            public void onResponse(@NonNull Call<GooglePlaceNearbyResponse> call, @NonNull Response<GooglePlaceNearbyResponse> response) {
                if (response.body() != null) {
                    callback.onSuccess(response.body().getList());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GooglePlaceNearbyResponse> call, Throwable t) {

            }
        });
    }

    public void addRestaurantToFirebase(Restaurant restaurant) {
        if (restaurant.getId() != null) {
            Task<DocumentSnapshot> restaurantData = getRestaurantsCollection().document(restaurant.getId()).get();
            restaurantData.addOnSuccessListener(documentSnapshot -> getRestaurantsCollection().document(restaurant.getId()).set(restaurant));
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
