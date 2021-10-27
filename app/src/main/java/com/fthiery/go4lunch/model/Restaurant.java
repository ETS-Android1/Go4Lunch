package com.fthiery.go4lunch.model;


import android.location.Location;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.OpeningHours;


public class Restaurant {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String photo;
    private String address;
    private String phoneNumber;
    private String websiteUrl;
    private OpeningHours openingHours;

    public Restaurant() {
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
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(Uri photo) {
        if (photo != null) this.photo = photo.toString();
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

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(Uri websiteUrl) {
        if (websiteUrl != null) this.websiteUrl = websiteUrl.toString();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) { return true; }

        if (!(obj instanceof Restaurant)) { return false; }

        Restaurant other = (Restaurant) obj;
        return this.id.equals(other.id)
                && this.name.equals(other.name)
                && this.address.equals(other.address)
                && this.websiteUrl.equals(other.websiteUrl)
                && this.photo.equals(other.photo);
    }
}
