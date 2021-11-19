package com.fthiery.go4lunch.utils;

import com.fthiery.go4lunch.model.placedetails.GooglePlaceDetails;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RestaurantService {
    @GET("details/json")
    Call<GooglePlaceDetails> getPlacesInfo(@Query("placeid") String placeId, @Query("key") String key);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
