package com.fthiery.go4lunch.repository;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.fthiery.go4lunch.TestUtils.TestUtils;
import com.fthiery.go4lunch.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class UserRepositoryTest {

    private static UserRepository userRepository;
    private static final User user1 = new User("1", "Michael Jackson", "michaeljackson@music.com", "http://michaeljackson.com/picture.jpg");
    private static final User user2 = new User("2", "Jean-Paul II", "jeanpaul2@vatican.com", "http://vatican.com/jpii.jpg");
    private static final User user3 = new User("3", "Jacques Chirac", "jacqueschirac@elysee.fr", "http://politique.fr/chichi.jpg");

    @BeforeClass
    public static void initFirebase() {
        // We use the firebase emulator to run the tests
        FirebaseFirestore firestore = TestUtils.firebaseEmulatorInstance();

        user1.setChosenRestaurantId("r1");
        user2.setChosenRestaurantId("r2");
        user3.setChosenRestaurantId("r1");

        List<User> userList = Arrays.asList(user1, user2, user3);

        for (User user : userList) {
            firestore.collection("users").document(user.getId()).set(user);
        }
        userRepository = new UserRepository(firestore, TestUtils.firebaseAuthEmulatorInstance());
    }

    @AfterClass
    public static void clear() throws IOException {
        TestUtils.clearFirebase();
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