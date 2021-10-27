package com.fthiery.go4lunch.repository;

import android.util.Log;

import androidx.annotation.Nullable;

import com.fthiery.go4lunch.model.Restaurant;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class RestaurantRepository {

    private static volatile RestaurantRepository instance;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        return db.collection("restaurants");
    }

    private void listenToRestaurantsUpdates() {
        getRestaurantsCollection().addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                List<Restaurant> restaurants = new ArrayList<>();
                for (QueryDocumentSnapshot d : value) {

                }
            }
        });
    }

    public void createRestaurant(Restaurant restaurant) {
        if (restaurant.getId() != null) {
            Task<DocumentSnapshot> restaurantData = getRestaurantsCollection().document(restaurant.getId()).get();
            restaurantData.addOnSuccessListener(documentSnapshot -> {
                getRestaurantsCollection().document(restaurant.getId()).set(restaurant);
            });
        } else {
            Log.w("RestaurantRepository", "createRestaurant: Missing Id");
        }
    }
//
//    public Restaurant getRestaurant(String id) {
//        getRestaurantsCollection().document(id).get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
//                        return restaurant;
//                    }
//                });
//    }
}
