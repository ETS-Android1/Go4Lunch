package com.fthiery.go4lunch.viewmodel;

import static com.fthiery.go4lunch.TestUtils.TestUtils.getOrAwaitValue;
import static junit.framework.TestCase.assertEquals;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.model.User;
import com.fthiery.go4lunch.repository.RestaurantRepository;
import com.fthiery.go4lunch.repository.UserRepository;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

@RunWith(MockitoJUnitRunner.class)
public class DetailViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    DetailViewModel viewModel;

    @Before
    public void setUp() {
        Restaurant restaurant1 = new Restaurant("1", "Le petit restaurant", "1 rue des restaurants", new LatLng(0.1, 0));
        Restaurant restaurant2 = new Restaurant("2", "Le restaurant moyen", "2 rue des restaurants", new LatLng(0.2, 0));
        Restaurant restaurant3 = new Restaurant("3", "Le grand restaurant", "3 rue des restaurants", new LatLng(0.3, 0));

        User user1 = new User("u1", "Jean-Jacques Goldman", "jeanjacquesgoldman@gmail.com", "");
        User user2 = new User("u2", "Sting", "sting@hotmail.com", "");
        User user3 = new User("u3", "Bono", "bono@u2.com", "");

        RestaurantRepository mockRestaurantRepository = Mockito.mock(RestaurantRepository.class);
        Mockito.when(mockRestaurantRepository.getRestaurant("1")).thenReturn(Single.just(restaurant1));
        Mockito.when(mockRestaurantRepository.watchRestaurant("1")).thenReturn(Observable.just(restaurant1));
        Mockito.when(mockRestaurantRepository.watchRestaurant(new Restaurant("1"))).thenReturn(Observable.just(restaurant1));
        Mockito.when(mockRestaurantRepository.searchRestaurants("", new LatLng(0, 0))).thenReturn(Single.just(Arrays.asList(restaurant1, restaurant2, restaurant3)));

        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.watchNumberOfUsers()).thenReturn(Observable.just(3));
        Mockito.when(mockUserRepository.watchUsersEatingAt("1")).thenReturn(Observable.just(Arrays.asList(user1,user2,user3)));
        Mockito.when(mockUserRepository.getChosenRestaurant("u1")).thenReturn(Single.just("1"));
        Mockito.when(mockUserRepository.watchChosenRestaurant("u1")).thenReturn(Observable.just("1"));
        Mockito.when(mockUserRepository.getCurrentUserId()).thenReturn("u1");

        viewModel = new DetailViewModel(mockUserRepository, mockRestaurantRepository);
    }

    @Test
    public void testWatchRestaurantDetails() throws InterruptedException {
        Restaurant restaurant = getOrAwaitValue(viewModel.watchRestaurantDetails("1"));

        assertEquals(restaurant.getName(),"Le petit restaurant");
    }

    @Test
    public void testWatchChosenRestaurant() throws InterruptedException {
        String chosenRestaurant = getOrAwaitValue(viewModel.watchChosenRestaurant("u1"));

        assertEquals(chosenRestaurant,"1");
    }

    @Test
    public void testWorkmatesEatingAt() throws InterruptedException {
        List<User> users = getOrAwaitValue(viewModel.watchWorkmatesEatingAtRestaurant("1"));

        assertEquals(users.get(0).getName(), "Jean-Jacques Goldman");
        assertEquals(users.get(1).getName(), "Sting");
        assertEquals(users.get(2).getName(), "Bono");
    }

    @Test
    public void testGetUserId() {
        String id = viewModel.getUserId();
        assertEquals(id, "u1");
    }
}