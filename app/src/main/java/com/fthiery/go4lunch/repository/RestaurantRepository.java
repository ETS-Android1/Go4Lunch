package com.fthiery.go4lunch.repository;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.fthiery.go4lunch.BuildConfig;
import com.fthiery.go4lunch.model.Restaurant;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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
import com.google.maps.model.RankBy;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RestaurantRepository {

    private static volatile RestaurantRepository instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final GeoApiContext geoApiContext = new GeoApiContext.Builder().apiKey(BuildConfig.MAPS_API_KEY).build();
    private PlacesClient placesClient;
    private final List<String> ids = new ArrayList<>();
    private final Map<String,Restaurant> restaurantMap = new ConcurrentHashMap<>();
    private final MutableLiveData<List<Restaurant>> restaurants = new MutableLiveData<>();

    List<Place.Field> placeFields = Arrays.asList(
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.PHONE_NUMBER,
            Place.Field.OPENING_HOURS,
            Place.Field.WEBSITE_URI,
            Place.Field.PHOTO_METADATAS);

    public static RestaurantRepository getInstance() {
        RestaurantRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized(UserRepository.class) {
            if (instance == null) {
                instance = new RestaurantRepository();
            }
            return instance;
        }
    }

    public MutableLiveData<List<Restaurant>> getRestaurants() {
        return restaurants;
    }

    public void updateMap() {
        for (String id : ids) {
            getRestaurantsCollection().document(id).get().addOnSuccessListener(documentSnapshot -> {
                // The restaurant exists in the database
                Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                restaurantMap.put(id,restaurant);
            }).addOnFailureListener(e -> {
                // If the restaurant isn't in the database, fetch it from Place Api
                FetchPlaceRequest request = FetchPlaceRequest.newInstance(id, placeFields);

                placesClient.fetchPlace(request).addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    Restaurant restaurant = new Restaurant(id);

                    restaurant.setName(place.getName());
                    restaurant.setAddress(place.getAddress());
                    restaurant.setLocation(place.getLatLng());
                    restaurant.setPhoneNumber(place.getPhoneNumber());
                    restaurant.setOpeningHours(place.getOpeningHours());
                    restaurant.setWebsiteUrl(place.getWebsiteUri());

                    getPhoto(restaurant,place);

                    addRestaurantToFirebase(restaurant);
                    restaurantMap.put(id,restaurant);
                }).addOnFailureListener(this::onApiException);
            });
        }
    }

    private void getPhoto(Restaurant restaurant, Place place) {
        // Get the photo metadata.
        final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
        if (metadata == null || metadata.isEmpty()) {
            Log.w("RestaurantRepository", "No photo metadata for "+place.getName());
            return;
        }
        final PhotoMetadata photoMetadata = metadata.get(0);

        // Create a FetchPhotoRequest.
        final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .setMaxWidth(1000)
                .setMaxHeight(800)
                .build();

        placesClient.fetchPhoto(photoRequest)
                .addOnSuccessListener((response) -> {
                    // Fetch the bitmap from Place Api
                    Bitmap bitmap = response.getBitmap();
                    // Create a storage reference for the bitmap
                    StorageReference storageRef = storage.getReference().child("restaurant_pictures/" + place.getId() + ".jpg");

                    // Compress the bitmap
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] data = baos.toByteArray();

                    // Upload the bitmap to Firebase Storage
                    UploadTask uploadTask = storageRef.putBytes(data);
                    Task<Uri> urlTask = uploadTask
                            .continueWithTask(task -> storageRef.getDownloadUrl())
                            .addOnCompleteListener(task -> {
                                // Set restaurant photo uri
                                Uri uri = task.getResult();
                                restaurant.setPhoto(uri);
                                addRestaurantToFirebase(restaurant);
                            });

                }).addOnFailureListener(this::onApiException);
    }

    public void updateRestaurantsAround(LatLng location) {

        NearbySearchRequest request = PlacesApi.nearbySearchQuery(geoApiContext, location).rankby(RankBy.DISTANCE).type(PlaceType.RESTAURANT);

        request.setCallback(new PendingResult.Callback<PlacesSearchResponse>() {
            @Override
            public void onResult(PlacesSearchResponse response) {

                ids.clear();
                for (PlacesSearchResult result : response.results) {
                    ids.add(result.placeId);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                // TODO: Manage failure to get the data
            }
        });
    }

    private CollectionReference getRestaurantsCollection() {
        return db.collection("restaurants");
    }

    private void listenToRestaurantsUpdates() {
        getRestaurantsCollection().addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                List<Restaurant> restaurants = new ArrayList<>();
                for (QueryDocumentSnapshot d : value) {

                }
            }
        });
    }

    public void addRestaurantToFirebase(Restaurant restaurant) {
        if (restaurant.getId() != null) {
            Task<DocumentSnapshot> restaurantData = getRestaurantsCollection().document(restaurant.getId()).get();
            restaurantData.addOnSuccessListener(documentSnapshot -> {
                getRestaurantsCollection().document(restaurant.getId()).set(restaurant);
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
    public void setPlacesClient(PlacesClient placesClient) {
        this.placesClient = placesClient;
    }
//
//    public Restaurant getRestaurant(String id) {
//        getRestaurantsCollection().document(id).get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
//                        return restaurant;
//                    }
//                });
//    }
}
