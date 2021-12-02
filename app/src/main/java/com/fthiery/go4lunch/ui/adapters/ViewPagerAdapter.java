package com.fthiery.go4lunch.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fthiery.go4lunch.ui.mainactivity.MapFragment;
import com.fthiery.go4lunch.ui.mainactivity.RestaurantListFragment;
import com.fthiery.go4lunch.ui.mainactivity.WorkmatesFragment;


public class ViewPagerAdapter extends FragmentStateAdapter {

    public MapFragment mapFragment;
    public RestaurantListFragment restaurantListFragment;
    public WorkmatesFragment workmatesFragment;

    public ViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                mapFragment = new MapFragment();
                return mapFragment;
            case 1:
                restaurantListFragment = new RestaurantListFragment();
                return restaurantListFragment;
            default:
                workmatesFragment = new WorkmatesFragment();
                return workmatesFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
