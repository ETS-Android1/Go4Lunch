package com.fthiery.go4lunch;

import static com.fthiery.go4lunch.utils.GooglePlaceService.retrofit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.repository.RestaurantRepository;
import com.fthiery.go4lunch.utils.GooglePlaceService;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.reactivex.rxjava3.observers.TestObserver;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RestaurantRepositoryTest {

    private static FirebaseFirestore firestore;
    private RestaurantRepository restaurantRepository;

    @BeforeClass
    public static void startEmulator() {
        // We use the firebase emulator to run the tests
        // TODO: use OkHttp MockWebServer to mock retrofit responses
        firestore = FirebaseFirestore.getInstance();
        firestore.useEmulator("10.0.2.2", 8080);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        firestore.setFirestoreSettings(settings);
    }

    @Before
    public void init() {
        restaurantRepository = new RestaurantRepository(firestore, retrofit.create(GooglePlaceService.class));
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.fthiery.go4lunch", appContext.getPackageName());
    }

    @Test
    public void testSearchRestaurants() {
        List<Restaurant> restaurants = restaurantRepository
                .searchRestaurants("", new LatLng(-33.8670522, 151.1957362))
                .blockingGet();
        assert (!restaurants.isEmpty());
    }

    @Test
    public void testGetRestaurant() {
        TestObserver<Restaurant> observer = restaurantRepository
                .getRestaurant("ChIJtwapWjeuEmsRcxV5JARHpSk")
                .test();

        Restaurant result = observer.values().get(0);

        assertEquals(result.getName(), "The Little Snail Restaurant");
        assertNotEquals(result.getName(), "The Little Snail Restorant");
        assertEquals(result.getAddress(), "3/50 Murray St, Pyrmont NSW 2009, Australia");
        assertEquals(result.getPhoneNumber(), "(02) 9212 7512");
        assertEquals(result.getWebsiteUrl(), "http://www.thelittlesnail.com.au/");
        observer.dispose();
    }

    @Test
    public void testWatchRestaurantById() {
        TestObserver<Restaurant> observer = new TestObserver<>();

        restaurantRepository
                .watchRestaurant("ChIJtwapWjeuEmsRcxV5JARHpSk")
                .subscribeWith(observer)
                .assertNoErrors();
    }

    @Test
    public void testWatchRestaurant() {
        TestObserver<Restaurant> observer = new TestObserver<>();
        restaurantRepository
                .watchRestaurant(new Restaurant("ChIJtwapWjeuEmsRcxV5JARHpSk"))
                .subscribeWith(observer)
                .assertNoErrors();
    }
}