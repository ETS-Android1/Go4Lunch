package com.fthiery.go4lunch.model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fthiery.go4lunch.BuildConfig;
import com.fthiery.go4lunch.model.placedetails.Geometry;
import com.fthiery.go4lunch.model.placedetails.OpeningHours;
import com.fthiery.go4lunch.model.placedetails.Photo;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.Exclude;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class Restaurant implements ClusterItem {

    @SerializedName("place_id")
    @Expose
    private String id;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("photos")
    @Expose
    private List<Photo> photos = null;

    @SerializedName("formatted_address")
    @Expose
    private String address;

    @SerializedName("formatted_phone_number")
    @Expose
    private String phoneNumber;

    @SerializedName("website")
    @Expose
    @Nullable
    private String websiteUrl;

    @SerializedName("opening_hours")
    @Expose
    private OpeningHours openingHours;

    @SerializedName("geometry")
    @Expose
    private Geometry geometry;

    /**
     * Number of workmates eating at this restaurant
     */
    @Exclude
    private int workmates = 0;
    @Exclude
    private int distance;

    public Restaurant() {
    }

    public Restaurant(String id) {
        this.id = id;
    }

    public Restaurant(Restaurant that) {
        this.id = that.id;
        this.name = that.name;
        this.address = that.address;
        this.phoneNumber = that.phoneNumber;
        this.websiteUrl = that.websiteUrl;
        this.workmates = that.workmates;
        this.distance = that.distance;
        this.openingHours = that.openingHours;
        this.photos = that.photos;
        this.geometry = that.geometry;
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
        this.geometry.setLocation(latLng);
    }

    public void setPosition(double latitude, double longitude) {
        setPosition(new LatLng(latitude, longitude));
    }

    @Exclude
    public String getPhoto(int maxSize) {
        if (photos != null && photos.size() > 0) {
            return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=" + maxSize
                    + "&maxheight=" + maxSize
                    + "&photoreference=" + photos.get(0).getPhotoReference()
                    + "&key=" + BuildConfig.MAPS_API_KEY;
        } else return "";
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

    @Exclude
    public int getWorkmates() {
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
        this.distance = (int) SphericalUtil.computeDistanceBetween(getPosition(), position);
    }

    @Exclude
    public int getDistance() {
        return distance;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @NonNull
    @Override
    @Exclude
    public LatLng getPosition() {
        return geometry.getLatLng();
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

        if (workmates != that.workmates) return false;
        if (distance != that.distance) return false;
        if (!id.equals(that.id)) return false;
        if (!Objects.equals(photos, that.photos)) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(address, that.address)) return false;
        if (!Objects.equals(phoneNumber, that.phoneNumber)) return false;
        if (!Objects.equals(websiteUrl, that.websiteUrl)) return false;
        if (!Objects.equals(openingHours, that.openingHours)) return false;
        return Objects.equals(geometry, that.geometry);
    }

    public static class RestaurantDistanceComparator implements Comparator<Restaurant> {
        @Override
        public int compare(Restaurant left, Restaurant right) {
            return left.distance - right.distance;
        }
    }
}
