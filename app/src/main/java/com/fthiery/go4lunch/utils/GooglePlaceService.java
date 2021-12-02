package com.fthiery.go4lunch.utils;

import com.fthiery.go4lunch.BuildConfig;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceDetailResponse;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceNearbyResponse;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GooglePlaceService {
    @GET("nearbysearch/json?type=restaurant&radius=800&key=" + BuildConfig.MAPS_API_KEY)
    Observable<GooglePlaceNearbyResponse> getNearbyPlaces(@Query("location") String location);

    @GET("nearbysearch/json?type=restaurant&radius=5000&key=" + BuildConfig.MAPS_API_KEY)
    Observable<GooglePlaceNearbyResponse> searchPlaces(@Query("location") String location, @Query("keyword") String keyword);

    @GET("details/json?key=" + BuildConfig.MAPS_API_KEY)
    Observable<GooglePlaceDetailResponse> getPlaceInfo(@Query("placeid") String placeId);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();
}
