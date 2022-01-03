package com.fthiery.go4lunch.repository;

import static com.fthiery.go4lunch.TestUtils.TestUtils.clearFirebase;
import static com.fthiery.go4lunch.TestUtils.TestUtils.firebaseEmulatorInstance;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.fthiery.go4lunch.TestUtils.TestUtils;
import com.fthiery.go4lunch.model.Restaurant;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@RunWith(AndroidJUnit4.class)
public class RestaurantRepositoryTest {

    private static FirebaseFirestore firestore;
    private RestaurantRepository restaurantRepository;
    private static MockWebServer mockWebServer;

    private static final MockResponse mockNearbyResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(TestUtils.nearbySearchResponse);

    private static final MockResponse mockDetailResponse = new MockResponse()
            .setResponseCode(200)
            .setBody(TestUtils.placeDetailResponse);

    @BeforeClass
    public static void startEmulator() {
        // We use the firebase emulator to run the tests
        firestore = firebaseEmulatorInstance();
    }

    @Before
    public void init() {

        mockWebServer = new MockWebServer();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        Retrofit mockRetrofit = new Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        restaurantRepository = new RestaurantRepository(firestore, mockRetrofit.create(GooglePlaceService.class));
    }

    @After
    public void clear() throws IOException {
        clearFirebase();
        mockWebServer.shutdown();
    }

    @Test
    public void testSearchRestaurants() throws IOException {
        mockWebServer.enqueue(mockNearbyResponse);

        List<Restaurant> restaurants = restaurantRepository
                .searchRestaurants("", new LatLng(-33.8670522, 151.1957362))
                .blockingGet();

        assert (!restaurants.isEmpty());
        assertEquals(restaurants.get(0).getName(),"The Little Snail Restaurant");
        assertEquals(restaurants.get(1).getName(),"Steersons Steakhouse");
        assertEquals(restaurants.get(2).getName(),"Georges Mediterranean Bar & Grill");
        assertEquals(restaurants.get(3).getName(),"Flying Fish");
        assertEquals(restaurants.get(4).getName(),"The Malaya");
    }

    @Test
    public void testGetRestaurant() {
        mockWebServer.enqueue(mockDetailResponse);

        Restaurant restaurant = restaurantRepository
                .getRestaurant("ChIJtwapWjeuEmsRcxV5JARHpSk")
                .timeout(3, TimeUnit.SECONDS)
                .blockingGet();

        assertEquals(restaurant.getName(), "The Little Snail Restaurant");
        assertEquals(restaurant.getAddress(), "3/50 Murray St, Pyrmont NSW 2009, Australie");
        assertEquals(restaurant.getPhoneNumber(), "(02) 9212 7512");
        assertEquals(restaurant.getWebsiteUrl(), "http://www.thelittlesnail.com.au/");
    }

    @Test
    public void testWatchRestaurantById() {
        mockWebServer.enqueue(mockDetailResponse);

        Restaurant restaurant = restaurantRepository
                .watchRestaurant("ChIJtwapWjeuEmsRcxV5JARHpSk")
                .timeout(3, TimeUnit.SECONDS)
                .blockingFirst();

        assertEquals(restaurant.getName(), "The Little Snail Restaurant");
        assertEquals(restaurant.getAddress(), "3/50 Murray St, Pyrmont NSW 2009, Australie");
        assertEquals(restaurant.getPhoneNumber(), "(02) 9212 7512");
        assertEquals(restaurant.getWebsiteUrl(), "http://www.thelittlesnail.com.au/");
    }

    @Test
    public void testWatchRestaurant() {
        mockWebServer.enqueue(mockDetailResponse);

        Restaurant restaurant = restaurantRepository
                .watchRestaurant(new Restaurant("ChIJtwapWjeuEmsRcxV5JARHpSk"))
                .timeout(3, TimeUnit.SECONDS)
                .blockingFirst();

        assertEquals(restaurant.getName(), "The Little Snail Restaurant");
        assertEquals(restaurant.getAddress(), "3/50 Murray St, Pyrmont NSW 2009, Australie");
        assertEquals(restaurant.getPhoneNumber(), "(02) 9212 7512");
        assertEquals(restaurant.getWebsiteUrl(), "http://www.thelittlesnail.com.au/");
    }
}