package com.fthiery.go4lunch.model;


import android.location.Location;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.firebase.firestore.Exclude;
import com.google.maps.android.clustering.ClusterItem;


public class Restaurant implements ClusterItem {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    @Nullable private String photo;
    private String address;
    private String phoneNumber;
    @Nullable private String websiteUrl;
    @Exclude private boolean chosen;

    public Restaurant() {
        chosen = false;
    }

    public Restaurant(String id) {
        this.id = id;
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

    public LatLng getLatLng() {
        return new LatLng(latitude,longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLocation(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLocation(LatLng latLng) {
        latitude = latLng.latitude;
        longitude = latLng.longitude;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
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

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public boolean isChosen() {
        return chosen;
    }

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) { return true; }

        if (!(obj instanceof Restaurant)) { return false; }

        Restaurant other = (Restaurant) obj;
        return this.id.equals(other.id)
                && this.name.equals(other.name)
                && this.address.equals(other.address)
                && (this.websiteUrl != null && this.websiteUrl.equals(other.websiteUrl))
                && (this.photo != null && this.photo.equals(other.photo));
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude,longitude);
    }

    @Nullable
    @Override
    public String getTitle() {
        return name;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }
}
