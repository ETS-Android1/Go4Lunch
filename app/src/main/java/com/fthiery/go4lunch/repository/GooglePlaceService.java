package com.fthiery.go4lunch.repository;

import com.fthiery.go4lunch.BuildConfig;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceDetailResponse;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceNearbyResponse;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GooglePlaceService {

    @GET("nearbysearch/json?type=restaurant&key=" + BuildConfig.MAPS_API_KEY)
    Single<GooglePlaceNearbyResponse> searchPlaces(@Query("location") String location, @Query("keyword") String keyword, @Query("radius") int radius);

    @GET("details/json?key=" + BuildConfig.MAPS_API_KEY)
    Single<GooglePlaceDetailResponse> getPlaceInfo(@Query("placeid") String placeId);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build();
}
