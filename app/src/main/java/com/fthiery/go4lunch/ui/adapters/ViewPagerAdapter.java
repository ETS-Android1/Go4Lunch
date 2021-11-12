package com.fthiery.go4lunch.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fthiery.go4lunch.ui.mainactivity.MapFragment;
import com.fthiery.go4lunch.ui.mainactivity.RestaurantListFragment;
import com.fthiery.go4lunch.ui.mainactivity.WorkmatesFragment;


public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new MapFragment();
            case 1:
                return new RestaurantListFragment();
            default:
                return new WorkmatesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
