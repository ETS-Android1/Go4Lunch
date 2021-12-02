
package com.fthiery.go4lunch.model.placedetails;

import com.fthiery.go4lunch.model.Restaurant;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GooglePlaceDetailResponse {

    @SerializedName("result")
    private Restaurant result;

    public Restaurant getResult() {
        return result;
    }

    public void setResult(Restaurant result) {
        this.result = result;
    }

}
