package com.fthiery.go4lunch.repository;

import android.content.Context;

import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.fthiery.go4lunch.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public interface Listener {
        void updateChosenRestaurants(List<String> chosenRestaurants);
    }

    private static volatile UserRepository instance;
    private Listener listener;

    public static UserRepository getInstance() {
        UserRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized(UserRepository.class) {
            if (instance == null) {
                instance = new UserRepository();
            }
            return instance;
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
        // Observes users and updates the list of chosen restaurants
        getUsersCollection().whereNotEqualTo("chosenRestaurantId",null).addSnapshotListener((value, error) -> {
            List<String> chosenRestaurants = new ArrayList<>();
            for (QueryDocumentSnapshot doc : value) {
                chosenRestaurants.add(doc.getString("chosenRestaurantId"));
            }
            this.listener.updateChosenRestaurants(chosenRestaurants);
        });
    }

    @Nullable
    public FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    @Nullable
    public String getCurrentUserUID() {
        FirebaseUser user = getCurrentUser();
        return (user != null)? user.getUid() : null;
    }

    public Task<Void> signOut(Context context){
        return AuthUI.getInstance().signOut(context);
    }

    public Task<Void> deleteUser(Context context){
        return AuthUI.getInstance().delete(context);
    }

    // Get the Collection Reference
    private CollectionReference getUsersCollection() {
        return FirebaseFirestore.getInstance().collection("users");
    }

    // Create User in Firestore
    public void createUser() {
        FirebaseUser user = getCurrentUser();
        if(user != null){
            String urlPicture = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;
            String username = user.getDisplayName();
            String uid = user.getUid();

            User userToCreate = new User(uid, username, urlPicture);

            Task<DocumentSnapshot> userData = getUserData();
            // If the user already exist in Firestore, we get his data (isMentor)
            userData.addOnSuccessListener(documentSnapshot -> {
                this.getUsersCollection().document(uid).set(userToCreate);
            });
        }
    }

    // Get User Data from Firestore
    public Task<DocumentSnapshot> getUserData() {
        String uid = this.getCurrentUserUID();
        if(uid != null){
            return this.getUsersCollection().document(uid).get();
        }else{
            return null;
        }
    }
}
