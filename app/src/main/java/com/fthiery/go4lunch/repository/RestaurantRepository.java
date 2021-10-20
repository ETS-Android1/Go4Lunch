package com.fthiery.go4lunch.repository;

import com.fthiery.go4lunch.model.Restaurant;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RestaurantRepository {

    private static volatile RestaurantRepository instance;

    public static RestaurantRepository getInstance() {
        RestaurantRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized(UserRepository.class) {
            if (instance == null) {
                instance = new RestaurantRepository();
            }
            return instance;
        }
    }

    private CollectionReference getRestaurantsCollection() {
        return FirebaseFirestore.getInstance().collection("restaurants");
    }

    public void createRestaurant(Restaurant restaurant) {
        Task<DocumentSnapshot> restaurantData = getRestaurant(restaurant.getId());
        restaurantData.addOnSuccessListener(documentSnapshot -> {
            getRestaurantsCollection().document(restaurant.getId()).set(restaurant);
        });
    }

    public Task<DocumentSnapshot> getRestaurant(String id) {
        if (id != null) {
            return getRestaurantsCollection().document(id).get();
        } else {
            return null;
        }
    }
}
