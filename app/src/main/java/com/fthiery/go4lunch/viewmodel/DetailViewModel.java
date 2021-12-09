package com.fthiery.go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.model.User;
import com.fthiery.go4lunch.repository.RestaurantRepository;
import com.fthiery.go4lunch.repository.UserRepository;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class DetailViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public DetailViewModel() {
        super();

        userRepository = UserRepository.getInstance();
        restaurantRepository = RestaurantRepository.getInstance();
    }

    public LiveData<Restaurant> watchRestaurantDetails(String id) {
        MutableLiveData<Restaurant> restaurantLiveData = new MutableLiveData<>();

        disposables.add(restaurantRepository.watchRestaurant(id).subscribe(restaurant -> {
            restaurantLiveData.postValue(restaurant);
            disposables.add(userRepository.watchNumberOfUsers().subscribe(n -> {
                restaurant.updateRating(n);
                restaurantLiveData.postValue(new Restaurant(restaurant));
            }));
        }));

        return restaurantLiveData;
    }

    public LiveData<List<User>> watchWorkmatesEatingAtRestaurant(String restaurantId) {
        MutableLiveData<List<User>> workmatesLiveData = new MutableLiveData<>();

        disposables.add(userRepository.watchUsersEatingAt(restaurantId).subscribe(users -> {
            for (User user : users) {
                restaurantRepository.getRestaurant(user.getChosenRestaurantId()).subscribe(restaurant -> {
                    user.setChosenRestaurant(restaurant);
                    workmatesLiveData.postValue(users);
                });
            }
        }));

        return workmatesLiveData;
    }

    public void stopListening() {
        disposables.clear();
    }

    public void toggleChosenRestaurant(String restaurantId) {
        userRepository.getChosenRestaurant(getUserId()).subscribe(chosenRestaurantId -> {
            if (chosenRestaurantId != null && chosenRestaurantId.equals(restaurantId)) {
                userRepository.setChosenRestaurant("");
            } else {
                userRepository.setChosenRestaurant(restaurantId);
            }
        });
    }

    public LiveData<String> watchChosenRestaurant() {
        MutableLiveData<String> chosenRestaurant = new MutableLiveData<>();
        disposables.add(userRepository.watchChosenRestaurant(getUserId()).subscribe(chosenRestaurant::postValue));
        return chosenRestaurant;
    }

    public String getUserId() {
        return userRepository.getCurrentUserUID();
    }

    public void toggleLike(Restaurant restaurant) {
        restaurant.toggleLike(userRepository.getCurrentUserUID());
        restaurantRepository.addRestaurantToFirebase(restaurant);
    }
}
