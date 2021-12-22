package com.fthiery.go4lunch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.repository.RestaurantRepository;
import com.fthiery.go4lunch.utils.GooglePlaceService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.util.concurrent.Executor;

import io.reactivex.rxjava3.observers.TestObserver;

public class RestaurantRepositoryUnitTest {

    RestaurantRepository restaurantRepository;

    @Before
    public void init() {
        restaurantRepository = RestaurantRepository.getInstance();
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
}
