package com.fthiery.go4lunch.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.fthiery.go4lunch.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;

    public UserRepository() {
        this(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance());
    }

    public UserRepository(FirebaseFirestore firestore, FirebaseAuth firebaseAuth) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
    }

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
     * @param restaurantId the Id of the restaurant
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

    public void setChosenRestaurant(String userId, String restaurantId) {
        if (userId != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("chosenRestaurantId", restaurantId);
            getUsersCollection().document(userId)
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
                            for (QueryDocumentSnapshot doc : collection) {
                                String chosenRestaurantId = doc.get("chosenRestaurantId", String.class);
                                if (!chosenRestaurants.contains(chosenRestaurantId))
                                    chosenRestaurants.add(chosenRestaurantId);
                            }
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
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    public Task<Void> signOut(Context context) {
        return AuthUI.getInstance().signOut(context);
    }

    public Task<Void> deleteUser(Context context) {
        String id = getCurrentUserId();
        if (id != null) {
            getUsersCollection()
                    .document(id)
                    .delete();
        }
        signOut(context);
        return AuthUI.getInstance().delete(context);
    }

    // Get the Collection Reference
    private CollectionReference getUsersCollection() {
        return firestore.collection("users");
    }

    // Create User in Firestore
    public void addCurrentUserToFirestore() {
        FirebaseUser fbUser = firebaseAuth.getCurrentUser();

        if (fbUser != null) {
            String urlPicture = (fbUser.getPhotoUrl() != null) ? fbUser.getPhotoUrl().toString() : null;
            String username = fbUser.getDisplayName();
            String uid = fbUser.getUid();
            String emailAddress = fbUser.getEmail();

            createUser(new User(uid, username, emailAddress, urlPicture));
        }
    }

    private void createUser(User user) {
        // If the user doesn't exist in the database, add it
        getUsersCollection()
                .document(user.getId())
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) getUsersCollection().document(user.getId()).set(user);
                });
    }

    public Observable<User> watchCurrentUser() {
        return watchUser(getCurrentUserId());
    }

    public Observable<User> watchUser(String userId) {
        return Observable.create(emitter -> {
            ListenerRegistration listener = getUsersCollection()
                    .document(userId)
                    .addSnapshotListener((document, error) -> {
                        if (error != null) emitter.onError(error);
                        User user = document != null ? document.toObject(User.class) : null;
                        if (user != null)
                            emitter.onNext(user);
                    });
            emitter.setCancellable(listener::remove);
        });
    }
}
