package com.fthiery.go4lunch.ui.MainActivity;

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
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.fthiery.go4lunch.R;
import com.fthiery.go4lunch.databinding.ActivityMainBinding;
import com.fthiery.go4lunch.databinding.NavHeaderMainBinding;
import com.fthiery.go4lunch.ui.DetailActivity.RestaurantDetailActivity;
import com.fthiery.go4lunch.ui.adapters.ViewPagerAdapter;
import com.fthiery.go4lunch.ui.notifications.NotificationReceiver;
import com.fthiery.go4lunch.ui.settings.SettingsActivity;
import com.fthiery.go4lunch.utils.AuthResultContract;
import com.fthiery.go4lunch.utils.Sort;
import com.fthiery.go4lunch.viewmodel.MainViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private MainViewModel viewModel;
    private ActivityMainBinding binding;
    private final ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this);
    private final ActivityResultLauncher<Integer> authResultLauncher = registerForActivityResult(new AuthResultContract(), this::handleAuthResponse);
    private String chosenRestaurant;
    private boolean searchIconVisible = true;
    private boolean sortIconVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Inflate the layout
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            // Check if the user is logged and start the sign-in activity if needed
            if (!viewModel.isCurrentUserLogged()) {
                authResultLauncher.launch(RC_SIGN_IN);
            } else {
                // Initialize the toolbar
                setSupportActionBar(binding.layoutMain.toolbar);

                // Initialize the rest of the activity
                initViewPager();
                initNavigationDrawer();
                initNotificationAlarm();
            }
        });
    }

    private void initViewPager() {
        // Initialize the viewpager and bottom navigation
        binding.layoutMain.viewpager.setAdapter(pagerAdapter);
        binding.layoutMain.viewpager.setUserInputEnabled(false);

        binding.layoutMain.viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        binding.layoutMain.bottomNavView.getMenu().findItem(R.id.navigation_map_view).setChecked(true);
                        searchIconVisible = true;
                        sortIconVisible = false;
                        break;
                    case 1:
                        binding.layoutMain.bottomNavView.getMenu().findItem(R.id.navigation_list_view).setChecked(true);
                        searchIconVisible = true;
                        sortIconVisible = true;
                        break;
                    case 2:
                        binding.layoutMain.bottomNavView.getMenu().findItem(R.id.navigation_workmates_view).setChecked(true);
                        searchIconVisible = false;
                        sortIconVisible = false;
                        break;
                }
                invalidateOptionsMenu();
            }
        });
        binding.layoutMain.bottomNavView.setOnItemSelectedListener(this::navigationItemSelected);
    }

    private void initNavigationDrawer() {
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

        // Bind navigation drawer fields
        View headerView = binding.navView.getHeaderView(0);
        NavHeaderMainBinding headerBinding = NavHeaderMainBinding.bind(headerView);
        viewModel.watchCurrentUser().observe(this, user -> {
            // Display the user name and email address
            headerBinding.navHeaderName.setText(user.getName());
            headerBinding.navHeaderMail.setText(user.getEmail());

            chosenRestaurant = user.getChosenRestaurantId();
            // Enable or disable "Your Lunch" menu item
            binding.navView.getMenu().findItem(R.id.your_lunch).setEnabled(chosenRestaurant != null && !chosenRestaurant.equals(""));

            // Load the user photo
            Glide.with(headerBinding.getRoot())
                    .load(user.getPhoto())
                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                    .apply(RequestOptions.circleCropTransform())
                    .into(headerBinding.navHeaderAvatar);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_search).setVisible(searchIconVisible);
        menu.findItem(R.id.action_sort).setVisible(sortIconVisible);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.getRestaurants(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.getRestaurants(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_sort_by_distance)
            viewModel.setSort(Sort.BY_DISTANCE);
        else if (itemId == R.id.action_sort_by_rating)
            viewModel.setSort(Sort.BY_RATING);
        else if (itemId == R.id.action_sort_by_workmates)
            viewModel.setSort(Sort.BY_WORKMATES);
        else if (itemId == R.id.action_sort_by_opening_hour)
            viewModel.setSort(Sort.BY_OPENING_HOUR);

        return super.onOptionsItemSelected(item);
    }

    private void handleAuthResponse(IdpResponse result) {
        if (result != null && result.getError() == null) {
            showSnackBar(getString(R.string.connection_succeed));
            viewModel.createUser();
            return;
        }
        if (result == null) showSnackBar(getString(R.string.error_authentication_canceled));
        else if (result.getError() != null) {
            if (result.getError().getErrorCode() == ErrorCodes.NO_NETWORK)
                showSnackBar(getString(R.string.error_no_internet));
            else showSnackBar(getString(R.string.error_unknown_error));
        }
        finish();
    }

    // Show Snack Bar with a message
    private void showSnackBar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    public boolean navigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.logout) {
            viewModel.signOut(this);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (itemId == R.id.action_settings) {
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
        } else if (itemId == R.id.your_lunch) {
            Intent detailActivity = new Intent(this, RestaurantDetailActivity.class);
            detailActivity.putExtra("Id", chosenRestaurant);
            startActivity(detailActivity);
        } else if (itemId == R.id.navigation_map_view) {
            binding.layoutMain.viewpager.setCurrentItem(0);
        } else if (itemId == R.id.navigation_list_view) {
            binding.layoutMain.viewpager.setCurrentItem(1);
        } else if (itemId == R.id.navigation_workmates_view) {
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

        manager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), 86_400_000, pending);
    }
}
