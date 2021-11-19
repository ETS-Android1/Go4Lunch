package com.fthiery.go4lunch.repository;

import static com.fthiery.go4lunch.utils.RestaurantService.retrofit;

import android.net.Uri;
import android.util.Log;

import com.fthiery.go4lunch.BuildConfig;
import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceDetails;
import com.fthiery.go4lunch.model.placedetails.OpeningHours;
import com.fthiery.go4lunch.utils.Callback;
import com.fthiery.go4lunch.utils.RestaurantService;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.GeoApiContext;
import com.google.maps.ImageResult;
import com.google.maps.NearbySearchRequest;
import com.google.maps.PendingResult;
import com.google.maps.PhotoRequest;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.model.Photo;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Call;
import retrofit2.Response;

public class RestaurantRepository {

    private static volatile RestaurantRepository instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final GeoApiContext geoApiContext = new GeoApiContext.Builder().apiKey(BuildConfig.MAPS_API_KEY).build();
    private final Map<String, Restaurant> restaurantMap = new ConcurrentHashMap<>();

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

        if (restaurantId == null || restaurantId == "") {
            callback.onSuccess(null);
            return;
        }

        getRestaurantsCollection().document(restaurantId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // The restaurant exists in firebase, fetch it, then add it to the map and return it
                        Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                        restaurantMap.put(restaurantId, restaurant);
                        callback.onSuccess(restaurant);
                    } else {
                        PlaceDetailsRequest request = PlacesApi.placeDetails(geoApiContext, restaurantId);
                        request.setCallback(new PendingResult.Callback<PlaceDetails>() {
                            @Override
                            public void onResult(PlaceDetails result) {
                                Restaurant restaurant = new Restaurant(restaurantId);

                                restaurant.setName(result.name);
                                restaurant.setAddress(result.formattedAddress);
                                restaurant.setPosition(result.geometry.location.lat, result.geometry.location.lng);
                                restaurant.setPhoneNumber(result.formattedPhoneNumber);
                                if (result.website != null) restaurant.setWebsiteUrl(result.website.toString());

                                if (result.photos != null) getPhotoFromPlacesApi(result.photos[0], restaurant, callback);

                                getOpeningHours(restaurant, callback);

                                restaurantMap.put(restaurantId, restaurant);
                                addRestaurantToFirebase(restaurant);
                                callback.onSuccess(restaurant);
                            }

                            @Override
                            public void onFailure(Throwable e) {
                                Log.e("RestaurantRepository", "Unable to get details: ", e);
                            }
                        });
                    }
                });

    }

    private void getOpeningHours(Restaurant restaurant, Callback<Restaurant> callback) {
        RestaurantService service = retrofit.create(RestaurantService.class);
        Call<GooglePlaceDetails> call = service.getPlacesInfo(restaurant.getId(), BuildConfig.MAPS_API_KEY);
        call.enqueue(new retrofit2.Callback<GooglePlaceDetails>() {
            @Override
            public void onResponse(Call<GooglePlaceDetails> call, Response<GooglePlaceDetails> response) {
                if (response.body() != null && response.body().getResult() != null) {
                    OpeningHours openingHours = response.body().getResult().getOpeningHours();
                    restaurant.setOpeningHours(openingHours);

                    restaurantMap.put(restaurant.getId(), restaurant);
                    addRestaurantToFirebase(restaurant);
                    callback.onSuccess(restaurant);
                }
            }

            @Override
            public void onFailure(Call<GooglePlaceDetails> call, Throwable t) {
                Log.e("RestaurantRepository", "Unable to get Opening Hours: ", t);
            }
        });
    }

    private void getPhotoFromPlacesApi(Photo photo, Restaurant restaurant, Callback<Restaurant> callback) {
        PhotoRequest photoRequest = PlacesApi.photo(geoApiContext, photo.photoReference).maxHeight(800).maxWidth(800);
        photoRequest.setCallback(new PendingResult.Callback<ImageResult>() {
            @Override
            public void onResult(ImageResult result) {
                StorageReference storageRef = storage.getReference().child("restaurant_pictures/" + restaurant.getId() + ".jpg");
                UploadTask uploadTask = storageRef.putBytes(result.imageData);

                uploadTask.continueWithTask(task -> storageRef.getDownloadUrl())
                        .addOnCompleteListener(task -> {
                            // Set restaurant photo uri
                            Uri uri = task.getResult();
                            restaurant.setPhoto(uri.toString());
                            addRestaurantToFirebase(restaurant);
                            callback.onSuccess(restaurant);
                        });
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e("RestaurantRepository", "PHOTO: " + e.getMessage());
            }
        });
    }

    public void updateRestaurantsAround(LatLng location, int radius, Callback<List<String>> callback) {
        com.google.maps.model.LatLng latlng = new com.google.maps.model.LatLng(location.latitude, location.longitude);
        NearbySearchRequest request = PlacesApi.nearbySearchQuery(geoApiContext, latlng).radius(radius).type(PlaceType.RESTAURANT);

        request.setCallback(new PendingResult.Callback<PlacesSearchResponse>() {
            @Override
            public void onResult(PlacesSearchResponse response) {
                List<String> restaurants = new ArrayList<>();

                for (PlacesSearchResult result : response.results) {
                    if (result.businessStatus.equals("OPERATIONAL")) {
                        restaurants.add(result.placeId);
                    }
                }
                callback.onSuccess(restaurants);
            }

            @Override
            public void onFailure(Throwable e) {

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
