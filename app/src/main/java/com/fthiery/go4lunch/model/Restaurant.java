package com.fthiery.go4lunch.model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fthiery.go4lunch.model.placedetails.OpeningHours;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.Exclude;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Comparator;
import java.util.Objects;


public class Restaurant implements ClusterItem {

    @SerializedName("place_id")
    private String id;

    private String name;

    @SerializedName("geometry/location/lat")
    private double latitude;
    @SerializedName("geometry/location/lng")
    private double longitude;

    @Nullable private String photo;
    private String address;
    private String phoneNumber;

    @SerializedName("url")
    @Nullable private String websiteUrl;

    private OpeningHours openingHours;

    /**
     * Number of workmates eating at this restaurant
     */
    @Exclude private int workmates = 0;
    @Exclude private int distance;

    public Restaurant() {}

    public Restaurant(String id) {
        this.id = id;
    }

    public Restaurant(Restaurant that) {
        this.id = that.id;
        this.name = that.name;
        this.latitude = that.latitude;
        this.longitude = that.longitude;
        this.photo = that.photo;
        this.address = that.address;
        this.phoneNumber = that.phoneNumber;
        this.websiteUrl = that.websiteUrl;
        this.workmates = that.workmates;
        this.distance = that.distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public void setPosition(double latitude, double longitude) {
        setPosition(new LatLng(latitude,longitude));
    }

    @Nullable
    public String getPhoto() {
        return photo;
    }

    public void setPhoto(@Nullable String photo) {
        this.photo = photo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Nullable
    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(@Nullable String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    @Exclude public int getWorkmates() {
        return workmates;
    }

    public void setWorkmates(int workmates) {
        this.workmates = workmates;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public void updateDistanceTo(LatLng position) {
        this.distance = (int) SphericalUtil.computeDistanceBetween(getPosition(),position);
    }

    @Exclude public int getDistance() {
        return distance;
    }

    @NonNull
    @Override
    @Exclude
    public LatLng getPosition() {
        return new LatLng(latitude,longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Nullable
    @Override
    @Exclude
    public String getTitle() {
        return name;
    }

    @Nullable
    @Override
    @Exclude
    public String getSnippet() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant)) return false;

        Restaurant that = (Restaurant) o;

        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (workmates != that.workmates) return false;
        if (distance != that.distance) return false;
        if (!id.equals(that.id)) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(photo, that.photo)) return false;
        if (!Objects.equals(address, that.address)) return false;
        if (!Objects.equals(phoneNumber, that.phoneNumber)) return false;
        return Objects.equals(websiteUrl, that.websiteUrl);
    }

    public static class RestaurantDistanceComparator implements Comparator<Restaurant> {
        @Override
        public int compare(Restaurant left, Restaurant right) {
            return left.distance - right.distance;
        }
    }
}
