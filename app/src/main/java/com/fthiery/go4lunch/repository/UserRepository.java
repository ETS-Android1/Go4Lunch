package com.fthiery.go4lunch.repository;

import android.content.Context;

import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.fthiery.go4lunch.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {

    public interface Listener {
        void updateUsers(List<User> users);
    }

    private static volatile UserRepository instance;
    private Listener listener;

    public static UserRepository getInstance() {
        UserRepository result = instance;
        if (result != null) {
            return result;
        }
        synchronized (UserRepository.class) {
            if (instance == null) {
                instance = new UserRepository();
            }
            return instance;
        }
    }

    public ListenerRegistration setListener(Listener listener) {
        this.listener = listener;
        // Observes users and updates the list of chosen restaurants
        return getUsersCollection()
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        List<User> users = new ArrayList<>();
                        if (value != null) {
                            for (QueryDocumentSnapshot doc : value) {
                                users.add(doc.toObject(User.class));
                            }
                        }
                        listener.updateUsers(users);
                    }
                });
    }

    /**
     * Requests the users who have chosen to eat at restaurantId
     *
     * @param restaurantId
     * @param listener     will get the data when ready
     * @return a registration allowing to remove it later
     */
    public ListenerRegistration requestUsersEatingAt(String restaurantId, Listener listener) {
        return getUsersCollection()
                .whereEqualTo("chosenRestaurantId", restaurantId)
                .addSnapshotListener((value, error) -> {
                    List<User> users = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            users.add(doc.toObject(User.class));
                        }
                    }
                    listener.updateUsers(users);
                });
    }

    public void setChosenRestaurant(String restaurantId, OnSuccessListener<Void> listener) {
        String id = getCurrentUserUID();
        if (id != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("chosenRestaurantId", restaurantId);
            getUsersCollection().document(id)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(listener);
        }
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    @Nullable
    public String getCurrentUserUID() {
        FirebaseUser user = getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    public Task<Void> signOut(Context context) {
        return AuthUI.getInstance().signOut(context);
    }

    public Task<Void> deleteUser(Context context) {
        return AuthUI.getInstance().delete(context);
    }

    // Get the Collection Reference
    private CollectionReference getUsersCollection() {
        return FirebaseFirestore.getInstance().collection("users");
    }

    // Create User in Firestore
    public void createUser() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            String urlPicture = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : null;
            String username = user.getDisplayName();
            String uid = user.getUid();

            User userToCreate = new User(uid, username, urlPicture);

            getUserData().addOnSuccessListener(documentSnapshot -> getUsersCollection().document(uid).set(userToCreate));
        }
    }

    // Get User Data from Firestore
    public Task<DocumentSnapshot> getUserData() {
        String uid = getCurrentUserUID();
        if (uid != null) {
            return getUsersCollection().document(uid).get();
        } else {
            return null;
        }
    }
}
