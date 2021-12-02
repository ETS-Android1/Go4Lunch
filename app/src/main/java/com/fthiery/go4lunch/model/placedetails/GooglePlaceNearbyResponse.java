package com.fthiery.go4lunch.model.placedetails;

import com.fthiery.go4lunch.model.Restaurant;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GooglePlaceNearbyResponse {

    private List<Restaurant> results;

    public List<Restaurant> getResults() {
        return results;
    }

    public void setResult(List<Restaurant> results) {
        this.results = results;
    }

    public List<String> getRestaurantIds() {
        List<String> list = new ArrayList<>();
        for (Restaurant r : results) {
            list.add(r.getId());
        }
        return list;
    }
}