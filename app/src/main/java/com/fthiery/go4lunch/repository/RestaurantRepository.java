package com.fthiery.go4lunch.repository;

import static com.fthiery.go4lunch.utils.GooglePlaceService.retrofit;

import android.util.Log;

import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceDetailResponse;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceNearbyResponse;
import com.fthiery.go4lunch.utils.GooglePlaceService;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class RestaurantRepository {

    private static volatile RestaurantRepository instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final GooglePlaceService service = retrofit.create(GooglePlaceService.class);

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

    public Single<List<String>> searchRestaurants(String keyword, LatLng location) {
        String latLng = location.latitude + "," + location.longitude;
        int radius = (keyword.length() >= 1) ? 5000 : 800;

        return service.searchPlaces(latLng, keyword, radius)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS)
                .map(GooglePlaceNearbyResponse::getRestaurantIds);
    }

    public Observable<Restaurant> watchRestaurant(String placeId) {
        return Observable.create(emitter -> {
            if (placeId != null) {
                ListenerRegistration listener = db.collection("restaurants")
                        .document(placeId)
                        .addSnapshotListener((document, error) -> {
                            if (error != null) emitter.onError(error);

                            if (document != null && document.exists()) {
                                Restaurant restaurant = document.toObject(Restaurant.class);
                                emitter.onNext(restaurant);
                            } else {
                                getRestaurantDetailsFromGooglePlaceApi(placeId)
                                        .subscribe(this::addRestaurantToFirebase);
                            }
                        });
                emitter.setCancellable(listener::remove);
            } else {
                emitter.onComplete();
            }
        });
    }

    public Single<Restaurant> getRestaurant(String placeId) {
        return Single.create(emitter -> {
           if (placeId != null && !placeId.equals("")) {
               db.collection("restaurants")
                       .document(placeId)
                       .get()
                       .addOnSuccessListener(document -> {
                           if (document != null && document.exists())
                               emitter.onSuccess(document.toObject(Restaurant.class));
                           else getRestaurantDetailsFromGooglePlaceApi(placeId).subscribe(restaurant -> {
                               addRestaurantToFirebase(restaurant);
                               emitter.onSuccess(restaurant);
                           });
                       });
           }
        });
    }

    private Single<Restaurant> getRestaurantDetailsFromGooglePlaceApi(String placeId) {
        return service.getPlaceInfo(placeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS)
                .map(GooglePlaceDetailResponse::getResult);
    }

    public void addRestaurantToFirebase(Restaurant restaurant) {
        if (restaurant.getId() != null) {
            db.collection("restaurants")
                    .document(restaurant.getId())
                    .set(restaurant);
        } else {
            Log.w("RestaurantRepository", "createRestaurant: Missing Id");
        }
    }
}
