package com.fthiery.go4lunch.ui.mainactivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.fthiery.go4lunch.R;
import com.fthiery.go4lunch.databinding.ActivityMainBinding;
import com.fthiery.go4lunch.ui.adapters.ViewPagerAdapter;
import com.fthiery.go4lunch.ui.notifications.NotificationReceiver;
import com.fthiery.go4lunch.ui.settings.SettingsActivity;
import com.fthiery.go4lunch.viewmodel.MainViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private MainViewModel viewModel;
    private ActivityMainBinding binding;
    private final ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Inflate the layout
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the toolbar
        setSupportActionBar(binding.layoutMain.toolbar);

        // Initialize the viewpager and bottom navigation
        initViewPager();
        binding.layoutMain.bottomNavView.setOnItemSelectedListener(this::navigationItemSelected);

        // Initialize the navigation drawer
        binding.navView.setNavigationItemSelectedListener(this::navigationItemSelected);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                binding.drawerLayout,
                binding.layoutMain.toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        // Check if the user is logged and start the sign-in activity if needed
        if (!viewModel.isCurrentUserLogged()) {
            startSignInActivity();
        }

        initNotificationAlarm();
    }

    private void initViewPager() {
        binding.layoutMain.viewpager.setAdapter(pagerAdapter);
        binding.layoutMain.viewpager.setUserInputEnabled(false);
        binding.layoutMain.viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        binding.layoutMain.bottomNavView.getMenu().findItem(R.id.navigation_map_view).setChecked(true);
                        break;
                    case 1:
                        binding.layoutMain.bottomNavView.getMenu().findItem(R.id.navigation_list_view).setChecked(true);
                        break;
                    case 2:
                        binding.layoutMain.bottomNavView.getMenu().findItem(R.id.navigation_workmates_view).setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.searchRestaurants(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.searchRestaurants(newText);
                return false;
            }
        });

        return true;
    }

    private void startSignInActivity() {

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());

        // Launch the activity
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.logo)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            // SUCCESS
            if (resultCode == RESULT_OK) {
                showSnackBar(getString(R.string.connection_succeed));
                viewModel.createUser();
                //recreate();
            } else {
                // ERRORS
                if (response == null) {
                    showSnackBar(getString(R.string.error_authentication_canceled));
                } else if (response.getError() != null) {
                    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                        showSnackBar(getString(R.string.error_no_internet));
                    } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                        showSnackBar(getString(R.string.error_unknown_error));
                    }
                }
                finish();
            }
        }
    }

    // Show Snack Bar with a message
    private void showSnackBar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    public boolean navigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            viewModel.signOut(this);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            startSignInActivity();
        } else if (item.getItemId() == R.id.action_settings) {
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
        } else if (item.getItemId() == R.id.navigation_map_view) {
            binding.layoutMain.viewpager.setCurrentItem(0);
        } else if (item.getItemId() == R.id.navigation_list_view) {
            binding.layoutMain.viewpager.setCurrentItem(1);
        } else if (item.getItemId() == R.id.navigation_workmates_view) {
            binding.layoutMain.viewpager.setCurrentItem(2);
        }
        return false;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void initNotificationAlarm() {
        // Starts an alarm which launches a notification every day at 12 o'clock
        Context context = getApplicationContext();
        Intent intent = new Intent(context, NotificationReceiver.class);

        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flag = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        else flag = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pending = PendingIntent.getBroadcast(context, 42, intent, flag);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, 12);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        time.getTime();
        if (time.before(Calendar.getInstance())) time.add(Calendar.DAY_OF_YEAR, 1);

        manager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), 86400000, pending);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.stopListening();
    }
}
