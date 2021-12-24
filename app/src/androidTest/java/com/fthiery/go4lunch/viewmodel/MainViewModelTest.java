package com.fthiery.go4lunch.viewmodel;

import static com.fthiery.go4lunch.TestUtils.TestUtils.getOrAwaitValue;
import static junit.framework.TestCase.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

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
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

@RunWith(MockitoJUnitRunner.class)
public class MainViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();


    private static final Restaurant restaurant1 = new Restaurant("1", "Le petit restaurant", "1 rue des restaurants", new LatLng(0.1, 0));
    private static final Restaurant restaurant2 = new Restaurant("2", "Le restaurant moyen", "2 rue des restaurants", new LatLng(0.2, 0));
    private static final Restaurant restaurant3 = new Restaurant("3", "Le grand restaurant", "3 rue des restaurants", new LatLng(0.3, 0));

    private static final User user1 = new User("u1", "Jean-Jacques Goldman", "jeanjacquesgoldman@gmail.com", "");
    private static final User user2 = new User("u2", "Sting", "sting@hotmail.com", "");
    private static final User user3 = new User("u3", "Bono", "bono@u2.com", "");

    private MainViewModel viewModel;

    @Before
    public void setUp() {
        RestaurantRepository mockRestaurantRepository = Mockito.mock(RestaurantRepository.class);
        Mockito.when(mockRestaurantRepository.getRestaurant("1")).thenReturn(Single.just(restaurant1));
        Mockito.when(mockRestaurantRepository.getRestaurant("2")).thenReturn(Single.just(restaurant2));
        Mockito.when(mockRestaurantRepository.getRestaurant("3")).thenReturn(Single.just(restaurant3));
        Mockito.when(mockRestaurantRepository.watchRestaurant("1")).thenReturn(Observable.just(restaurant1));
        Mockito.when(mockRestaurantRepository.watchRestaurant("2")).thenReturn(Observable.just(restaurant2));
        Mockito.when(mockRestaurantRepository.watchRestaurant("3")).thenReturn(Observable.just(restaurant3));
        Mockito.when(mockRestaurantRepository.watchRestaurant(any(Restaurant.class)))
                .then(invocation -> Observable.just(invocation.getArgument(0,Restaurant.class)));
        Mockito.when(mockRestaurantRepository.searchRestaurants(any(), any())).thenReturn(Single.just(Arrays.asList(restaurant1, restaurant2, restaurant3)));

        UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
        Mockito.when(mockUserRepository.watchUser()).thenReturn(Observable.just(user1));
        Mockito.when(mockUserRepository.watchNumberOfUsers()).thenReturn(Observable.just(3));
        Mockito.when(mockUserRepository.watchUsersEatingAt("1")).thenReturn(Observable.just(Arrays.asList(user1,user2)));
        Mockito.when(mockUserRepository.watchUsersEatingAt("2")).thenReturn(Observable.just(Collections.singletonList(user3)));
        Mockito.when(mockUserRepository.watchUsersEatingAt("3")).thenReturn(Observable.just(Collections.emptyList()));
        Mockito.when(mockUserRepository.watchAllUsers()).thenReturn(Observable.just(Arrays.asList(user1,user2,user3)));
        Mockito.when(mockUserRepository.getChosenRestaurant("u1")).thenReturn(Single.just("1"));
        Mockito.when(mockUserRepository.watchChosenRestaurant("u1")).thenReturn(Observable.just("1"));
        Mockito.when(mockUserRepository.getCurrentUserUID()).thenReturn("u1");

        viewModel = new MainViewModel(mockUserRepository, mockRestaurantRepository);
    }

    @Test
    public void testGetRestaurants() throws InterruptedException {

        viewModel.setLocation(new LatLng(0,0));
        List<Restaurant> restaurants = getOrAwaitValue(viewModel.getRestaurantsLiveData());

        assertEquals(restaurants.get(0).getName(),"Le petit restaurant");
        assertEquals(restaurants.get(1).getName(),"Le restaurant moyen");
        assertEquals(restaurants.get(2).getName(),"Le grand restaurant");
    }

    @Test
    public void testGetWorkmatesLiveData() throws InterruptedException {
        List<User> workmates = getOrAwaitValue(viewModel.getWorkmatesLiveData());

        assert(workmates.get(0).equals(user1));
        assert(workmates.get(1).equals(user2));
        assert(workmates.get(2).equals(user3));
    }

    @Test
    public void testGetUser() throws InterruptedException {
        User user = getOrAwaitValue(viewModel.getUser());

        assert(user.equals(user1));
    }
}