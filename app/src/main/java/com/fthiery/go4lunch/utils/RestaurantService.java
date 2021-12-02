package com.fthiery.go4lunch.utils;

import com.fthiery.go4lunch.BuildConfig;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceDetailResponse;
import com.fthiery.go4lunch.model.placedetails.GooglePlaceNearbyResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RestaurantService {
    @GET("nearbysearch/json?type=restaurant&radius=800&key=" + BuildConfig.MAPS_API_KEY)
    Call<GooglePlaceNearbyResponse> getNearbyPlaces(@Query("location") String location);

    @GET("nearbysearch/json?type=restaurant&radius=5000&key=" + BuildConfig.MAPS_API_KEY)
    Call<GooglePlaceNearbyResponse> searchPlaces(@Query("location") String location, @Query("keyword") String keyword);

    @GET("details/json?key=" + BuildConfig.MAPS_API_KEY)
    Call<GooglePlaceDetailResponse> getPlacesInfo(@Query("placeid") String placeId);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
