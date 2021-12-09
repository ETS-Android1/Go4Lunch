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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class UserRepository {

    private static volatile UserRepository instance;

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

    public Observable<List<User>> watchAllUsers() {
        // Observes users and updates the list of chosen restaurants
        return Observable.create(emitter -> {
            ListenerRegistration listener = getUsersCollection()
                    .addSnapshotListener((collection, error) -> {
                        if (error != null) emitter.onError(error);

                        List<User> users = new ArrayList<>();
                        if (collection != null)
                            for (QueryDocumentSnapshot doc : collection)
                                users.add(doc.toObject(User.class));
                        emitter.onNext(users);
                    });
            emitter.setCancellable(listener::remove);
        });
    }

    public Observable<Integer> watchNumberOfUsers() {
        return Observable.create(emitter -> {
            ListenerRegistration listener = getUsersCollection()
                    .addSnapshotListener((collection, error) -> {
                        if (collection != null) emitter.onNext(collection.size());
                    });
            emitter.setCancellable(listener::remove);
        });
    }

    /**
     * Requests the users who have chosen to eat at restaurantId
     *
     * @param restaurantId
     * @return an Observable
     */
    public Observable<List<User>> watchUsersEatingAt(String restaurantId) {
        return Observable.create(emitter -> {
            ListenerRegistration listener = getUsersCollection()
                    .whereEqualTo("chosenRestaurantId", restaurantId)
                    .addSnapshotListener((collection, error) -> {
                        if (error != null) emitter.onError(error);

                        List<User> users = new ArrayList<>();
                        if (collection != null)
                            for (QueryDocumentSnapshot doc : collection)
                                users.add(doc.toObject(User.class));
                        emitter.onNext(users);
                    });
            emitter.setCancellable(listener::remove);
        });
    }

    public void setChosenRestaurant(String restaurantId) {
        String id = getCurrentUserUID();
        if (id != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("chosenRestaurantId", restaurantId);
            getUsersCollection().document(id)
                    .set(data, SetOptions.merge());
        }
    }

    public Observable<List<String>> watchChosenRestaurants() {
        return Observable.create(emitter -> {
            ListenerRegistration listener = getUsersCollection()
                    .whereNotEqualTo("chosenRestaurantId", null)
                    .addSnapshotListener((collection, error) -> {
                        List<String> chosenRestaurants = new ArrayList<>();
                        if (collection != null)
                            for (QueryDocumentSnapshot doc : collection)
                                chosenRestaurants.add(doc.get("chosenRestaurantId", String.class));
                        emitter.onNext(chosenRestaurants);
                    });
            emitter.setCancellable(listener::remove);
        });
    }

    public Observable<String> watchChosenRestaurant(String userId) {
        return Observable.create(emitter -> {
            ListenerRegistration listener = getUsersCollection()
                    .document(userId)
                    .addSnapshotListener((document, error) -> {
                        if (error != null) emitter.onError(error);
                        String chosenRestaurantId = document.get("chosenRestaurantId", String.class);
                        emitter.onNext(chosenRestaurantId != null ? chosenRestaurantId : "");
                    });
            emitter.setCancellable(listener::remove);
        });
    }

    public Single<String> getChosenRestaurant(String userId) {
        return Single.create(emitter -> {
            getUsersCollection()
                    .document(userId)
                    .get()
                    .addOnSuccessListener(document -> {
                        String chosenRestaurantId = document.get("chosenRestaurantId", String.class);
                        emitter.onSuccess(chosenRestaurantId != null ? chosenRestaurantId : "");
                    });
        });
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
