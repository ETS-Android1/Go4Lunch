package com.fthiery.go4lunch.model;

import com.google.firebase.firestore.Exclude;

import java.util.Objects;

public class User {
    private String id;
    private String name;
    private String emailAddress;
    private String photo;
    private String chosenRestaurantId;
    private transient Restaurant chosenRestaurant;

    public User () {}

    public User(String id, String name, String emailAddress, String urlPicture) {
        this.id = id;
        this.name = name;
        this.emailAddress = emailAddress;
        this.photo = urlPicture;
    }

    public User(User that) {
        this.id = that.id;
        this.name = that.name;
        this.emailAddress = that.emailAddress;
        this.photo = that.photo;
        this.chosenRestaurantId = that.chosenRestaurantId;
        this.chosenRestaurant = that.chosenRestaurant;
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

    public String getEmail() {
        return emailAddress;
    }

    public void setEmail(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getChosenRestaurantId() {
        return (chosenRestaurantId == null) ? "" : chosenRestaurantId;
    }

    public void setChosenRestaurantId(String chosenRestaurantId) {
        this.chosenRestaurantId = chosenRestaurantId;
    }

    @Exclude public Restaurant getChosenRestaurant() {
        return chosenRestaurant;
    }

    public void setChosenRestaurant(Restaurant chosenRestaurant) {
        this.chosenRestaurant = chosenRestaurant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (!id.equals(user.id)) return false;
        if (!Objects.equals(name, user.name)) return false;
        if (!Objects.equals(photo, user.photo)) return false;
        return Objects.equals(chosenRestaurantId, user.chosenRestaurantId);
    }
}
