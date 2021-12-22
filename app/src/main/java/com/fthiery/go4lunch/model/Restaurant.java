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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class Restaurant implements ClusterItem {

    @SerializedName("place_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("photos")
    private List<Photo> photos = null;

    @SerializedName("formatted_address")
    private String address;

    @SerializedName("formatted_phone_number")
    private String phoneNumber;

    @SerializedName("website")
    @Nullable
    private String websiteUrl;

    @SerializedName("opening_hours")
    private OpeningHours openingHours;

    @SerializedName("geometry")
    private Geometry geometry;

    /**
     * The list of users liking this restaurant
     **/
    private List<String> likes = new ArrayList<>();

    /**
     * Number of workmates eating at this restaurant
     **/
    private transient int workmates = 0;

    /**
     * Distance from the user's device
     **/
    private transient int distance;

    private transient int rating = 0;

    public Restaurant() {
    }

    public Restaurant(String id) {
        this.id = id;
    }

    public Restaurant(String id, String name, String address, LatLng latLng) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.geometry = new Geometry(latLng);
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
        this.likes = that.likes;
        this.rating = that.rating;
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

    @Exclude
    public int howLongStillOpen() {
        if (openingHours != null) {
            Calendar now = Calendar.getInstance();
            if (openingHours.isOpenAt(now))
                return (int) (openingHours.nextTime(now).getTimeInMillis() - now.getTimeInMillis());
            else
                return (int) (now.getTimeInMillis() - openingHours.nextTime(now).getTimeInMillis());
        } else return -1_000_000_000;
    }

    public void updateDistanceTo(LatLng position) {
        this.distance = (int) SphericalUtil.computeDistanceBetween(getPosition(), position);
    }

    @Exclude
    public int getDistance() {
        return distance;
    }

    @Exclude
    public int getRating() {
        return rating;
    }

    @Exclude
    public void updateRating(int numberOfUsers) {
        double nLikes = likes.size() * 6;
        double rating = 0;
        if (numberOfUsers != 0) {
            rating = Math.round(nLikes / (double) numberOfUsers);
        }
        this.rating = (int) rating;
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
        if (geometry == null) geometry = new Geometry();
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
        if (rating != that.rating) return false;
        if (!Objects.equals(photos, that.photos)) return false;
        if (!Objects.equals(name, that.name)) return false;
        if (!Objects.equals(address, that.address)) return false;
        if (!Objects.equals(phoneNumber, that.phoneNumber)) return false;
        if (!Objects.equals(websiteUrl, that.websiteUrl)) return false;
        if (!Objects.equals(openingHours, that.openingHours)) return false;
        if (!Objects.equals(likes, that.likes)) return false;
        return Objects.equals(geometry, that.geometry);
    }

    public void setLikes(List<String> userIds) {
        if (likes == null) this.likes = new ArrayList<>();
        else this.likes = userIds;
    }

    public List<String> getLikes() {
        return likes;
    }

    @Exclude
    public boolean toggleLike(String userId) {
        if (likes.contains(userId)) {
            likes.remove(userId);
            return false;
        } else {
            likes.add(userId);
            return true;
        }
    }
}
