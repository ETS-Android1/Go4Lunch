package com.fthiery.go4lunch.repository;

import static com.fthiery.go4lunch.utils.GooglePlaceService.retrofit;

import android.util.Log;

import androidx.annotation.NonNull;

import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceDetailResponse;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceNearbyResponse;
import com.fthiery.go4lunch.utils.GooglePlaceService;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class RestaurantRepositoryRX {

    private static volatile RestaurantRepositoryRX instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final GooglePlaceService service = retrofit.create(GooglePlaceService.class);

    public static RestaurantRepositoryRX getInstance() {
        RestaurantRepositoryRX result = instance;

        if (result != null) {
            return result;
        }
        synchronized (RestaurantRepository.class) {
            if (instance == null) {
                instance = new RestaurantRepositoryRX();
            }
            return instance;
        }
    }

    public Observable<List<Restaurant>> getNearbyPlaces(String location) {
        return service.getNearbyPlaces(location)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS)
                .map(GooglePlaceNearbyResponse::getResults);
    }

    public Observable<List<Restaurant>> searchPlaces(String location, String keyword) {
        return service.searchPlaces(location, keyword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS)
                .map(GooglePlaceNearbyResponse::getResults);
    }

    public Observable<Restaurant> getRestaurantDetailsFromGooglePlaceApi(String placeId) {
        return service.getPlaceInfo(placeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS)
                .map(GooglePlaceDetailResponse::getResult);
    }

    public Observable<Restaurant> getRestaurantDetailsFromFirebase(String placeId) {
        return Observable.create(emitter -> {
            ListenerRegistration registration = db.collection("restaurants")
                    .document(placeId)
                    .addSnapshotListener((document, error) -> {
                        if (error != null) {
                            Log.w("test", "Listen failed.", error);
                            emitter.onError(error);
                            return;
                        }
                        if (document != null && document.exists()) {
                            Restaurant restaurant = document.toObject(Restaurant.class);
                            emitter.onNext(restaurant);
                        } else {
                            DisposableObserver<Restaurant> observer = getRestaurantDetailsFromGooglePlaceApi(placeId).subscribeWith(new DisposableObserver<Restaurant>() {
                                @Override
                                public void onNext(@NonNull Restaurant restaurant) {
                                    db.collection("restaurants")
                                            .document(placeId)
                                            .set(restaurant);
                                    emitter.onNext(restaurant);
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    emitter.onError(e);
                                }

                                @Override
                                public void onComplete() {
                                }
                            });
                        }
                    });
            emitter.setCancellable(registration::remove);
        });
    }

    public Single<List<Restaurant>> SearchRestaurants(String keyword, LatLng location) {
        String latLng = location.latitude + "," + location.longitude;
        Observable<List<Restaurant>> observable;

        if (keyword.equals("")) {
            observable = getNearbyPlaces(latLng);
        } else {
            observable = searchPlaces(latLng, keyword);
        }

        return observable.flatMapIterable(list -> list)
                .flatMap(new Function<Restaurant, ObservableSource<Restaurant>>() {
                    @Override
                    public ObservableSource<Restaurant> apply(@NonNull Restaurant restaurant) throws Exception {
                        return getRestaurantDetailsFromFirebase(restaurant.getId());
                    }
                })
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }
}
