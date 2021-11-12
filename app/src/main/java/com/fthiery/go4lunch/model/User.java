package com.fthiery.go4lunch.model;

import java.util.Objects;

public class User {
    private String id;
    private String name;
    private String photo;
    private String chosenRestaurantId;

    public User() {
    }

    public User(String id, String name, String photo) {
        this.id = id;
        this.name = name;
        this.photo = photo;
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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getChosenRestaurantId() {
        return chosenRestaurantId;
    }

    public void setChosenRestaurantId(String chosenRestaurantId) {
        this.chosenRestaurantId = chosenRestaurantId;
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
