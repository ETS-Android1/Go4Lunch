package com.fthiery.go4lunch.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.fthiery.go4lunch.ui.fragments.ListFragment;
import com.fthiery.go4lunch.ui.fragments.MapFragment;
import com.fthiery.go4lunch.ui.fragments.WorkMatesFragment;

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
                return new ListFragment();
            default:
                return new WorkMatesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
