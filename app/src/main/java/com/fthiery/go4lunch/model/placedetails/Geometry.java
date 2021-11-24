
package com.fthiery.go4lunch.model.placedetails;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.Exclude;
import com.google.geo.type.Viewport;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Geometry {

    @SerializedName("location")
    @Expose
    private Location location;

    public Location getLocation() {
        return location;
    }

    @Exclude public LatLng getLatLng() {
        return new LatLng(location.getLat(),location.getLng());
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Exclude public void setLocation(LatLng latlng) {
        this.location.setLat(latlng.latitude);
        this.location.setLng(latlng.longitude);
    }
}
