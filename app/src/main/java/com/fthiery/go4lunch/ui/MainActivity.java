package com.fthiery.go4lunch.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.fthiery.go4lunch.BuildConfig;
import com.fthiery.go4lunch.R;
import com.fthiery.go4lunch.databinding.ActivityMainBinding;
import com.fthiery.go4lunch.viewmodel.MyViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener{

    private static final int RC_SIGN_IN = 123;
    private MyViewModel viewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(MyViewModel.class);

        // Initialize the Places SDK
        Places.initialize(this, BuildConfig.MAPS_API_KEY);

        // Create a new PlacesClient instance
        viewModel.setPlacesClient(Places.createClient(this));

        // Inflate the layout
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the toolbar
        setSupportActionBar(binding.layoutMain.toolbar);

        // Initialize the viewpager and bottom navigation
        initViewPager();
        binding.layoutMain.bottomNavView.setOnItemSelectedListener(this);

        // Initialize the navigation drawer
        binding.navView.setNavigationItemSelectedListener(this);
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
    }

    private void initViewPager() {
        binding.layoutMain.viewpager.setAdapter(new ViewPagerAdapter(this));
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
                } else if (response.getError()!= null) {
                    if(response.getError().getErrorCode() == ErrorCodes.NO_NETWORK){
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
    private void showSnackBar( String message){
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            viewModel.signOut(this);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            startSignInActivity();
        } else if (item.getItemId() == R.id.navigation_map_view) {
            binding.layoutMain.viewpager.setCurrentItem(0);
        } else if (item.getItemId() == R.id.navigation_list_view) {
            binding.layoutMain.viewpager.setCurrentItem(1);
        } else if (item.getItemId() == R.id.navigation_workmates_view) {
            binding.layoutMain.viewpager.setCurrentItem(2);
        }
        return false;
    }
}
