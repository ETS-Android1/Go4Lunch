package com.fthiery.go4lunch.ui.detailactivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fthiery.go4lunch.R;
import com.fthiery.go4lunch.databinding.ActivityDetailBinding;
import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.ui.adapters.WorkmatesListAdapter;
import com.fthiery.go4lunch.viewmodel.DetailViewModel;

public class RestaurantDetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private DetailViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        // Inflate the layout
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Activate the Up button
        setSupportActionBar(binding.detailToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Request and observe the data
        viewModel.requestRestaurantDetails(getIntent().getStringExtra("Id"))
                .observe(this, this::bindRestaurant);

        // Initiate the RecyclerView
        WorkmatesListAdapter adapter = new WorkmatesListAdapter();
        binding.detailRestaurantUsers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.detailRestaurantUsers.setAdapter(adapter);

        // Request and observe the workmates who want to eat there
        viewModel.requestWorkmatesEatingAtRestaurant(getIntent().getStringExtra("Id"))
                .observe(this, adapter::submitList);
    }

    // Get back to previous activity when clicking on Up
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.stopListening();
    }

    // Bind the restaurant data to the views
    private void bindRestaurant(Restaurant restaurant) {

        binding.detailRestaurantPrimary.setText(restaurant.getName());
        binding.detailRestaurantSecondary.setText(restaurant.getAddress());

        // Load the photo
        if (restaurant.getPhoto() != null) {
            Glide.with(binding.getRoot())
                    .load(restaurant.getPhoto())
                    .apply(RequestOptions.centerCropTransform())
                    .placeholder(R.drawable.login_background_img)
                    .into(binding.detailRestaurantPhoto);
        }

        // Enable or disable the "Call" button
        Intent intentDial = new Intent(Intent.ACTION_DIAL);
        intentDial.setData(Uri.parse("tel:" + restaurant.getPhoneNumber()));
        if (restaurant.getPhoneNumber() != null && intentDial.resolveActivity(getPackageManager()) != null) {
            binding.actionCall.setEnabled(true);
            binding.actionCall.setOnClickListener(view -> {
                startActivity(intentDial);
            });
        } else {
            binding.actionCall.setEnabled(false);
        }

        // Enable or disable the "Website" button
        if (restaurant.getWebsiteUrl() == null) {
            binding.actionWebsite.setEnabled(false);
        } else {
            Intent intentWeb = new Intent(Intent.ACTION_VIEW, Uri.parse(restaurant.getWebsiteUrl()));
            if (intentWeb.resolveActivity(getPackageManager()) != null) {
                binding.actionWebsite.setEnabled(true);
                binding.actionWebsite.setOnClickListener(view -> {
                    startActivity(intentWeb);
                });
            } else {
                binding.actionWebsite.setEnabled(false);
            }
        }

        // Floating Action Button to chose to eat at this restaurant
        binding.restaurantDetailFab.setOnClickListener(view -> {
            viewModel.toggleChosenRestaurant(restaurant.getId());
        });

    }
}