package com.fthiery.go4lunch.repository;

import static com.fthiery.go4lunch.TestUtils.TestUtils.clearFirebase;
import static com.fthiery.go4lunch.TestUtils.TestUtils.firebaseAuthEmulatorInstance;
import static com.fthiery.go4lunch.TestUtils.TestUtils.firebaseEmulatorInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.fthiery.go4lunch.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.annotations.NonNull;

@RunWith(AndroidJUnit4.class)
public class UserRepositoryTest extends TestCase {

    private static UserRepository userRepository;
    private static final User user1 = new User("1", "Michael Jackson", "michaeljackson@music.com", "http://michaeljackson.com/picture.jpg");
    private static final User user2 = new User("2", "Jean-Paul II", "jeanpaul2@vatican.com", "http://vatican.com/jpii.jpg");
    private static final User user3 = new User("3", "Jacques Chirac", "jacqueschirac@elysee.fr", "http://politique.fr/chichi.jpg");

    @BeforeClass
    public static void initFirebase() {
        // We use the firebase emulator to run the tests
        FirebaseFirestore firestore = firebaseEmulatorInstance();

        user1.setChosenRestaurantId("r1");
        user2.setChosenRestaurantId("r2");
        user3.setChosenRestaurantId("r1");

        List<User> userList = Arrays.asList(user1, user2, user3);

        for (User user : userList) {
            firestore.collection("users").document(user.getId()).set(user);
        }
        userRepository = new UserRepository(firestore, firebaseAuthEmulatorInstance());
    }

    @AfterClass
    public static void clear() throws IOException {
        clearFirebase();
    }

    @Test
    public void watchAllUsers() {
        List<User> users = userRepository
                .watchAllUsers()
                .blockingFirst();

        assert (users.get(0).equals(user1));
        assert (users.get(1).equals(user2));
        assert (users.get(2).equals(user3));
    }

    @Test
    public void watchNumberOfUsers() {
        int nUsers = userRepository
                .watchNumberOfUsers()
                .blockingFirst();
        assertEquals(nUsers, 3);
    }

    @Test
    public void watchUsersEatingAt() {
        List<User> users = userRepository
                .watchUsersEatingAt("r1")
                .blockingFirst();

        assert (users.get(0).equals(user1));
        assert (users.get(1).equals(user3));
    }

    @Test
    public void watchChosenRestaurants() {
        List<String> restaurants = userRepository
                .watchChosenRestaurants()
                .blockingFirst();

        assertEquals(restaurants.get(0), "r1");
        assertEquals(restaurants.get(1), "r2");
    }

    @Test
    public void watchChosenRestaurant() {
        String restaurant = userRepository
                .watchChosenRestaurant("1")
                .blockingFirst();

        assertEquals(restaurant, "r1");
    }

    @Test
    public void getChosenRestaurant() {
        String restaurant = userRepository
                .getChosenRestaurant("2")
                .blockingGet();

        assertEquals(restaurant, "r2");
    }

    @Test
    public void watchUser() {
        User user = userRepository
                .watchUser("1")
                .blockingFirst();

        assert (user.equals(user1));
    }

}