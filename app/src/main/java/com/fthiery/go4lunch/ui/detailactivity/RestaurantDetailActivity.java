package com.fthiery.go4lunch.ui.detailactivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fthiery.go4lunch.R;
import com.fthiery.go4lunch.databinding.ActivityDetailBinding;
import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.ui.adapters.WorkmatesListAdapter;
import com.fthiery.go4lunch.ui.notifications.NotificationReceiver;
import com.fthiery.go4lunch.utils.WordUtils;
import com.fthiery.go4lunch.viewmodel.DetailViewModel;

import java.util.Calendar;

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

        binding.detailRestaurantPrimary.setText(WordUtils.capitalize(restaurant.getName()));
        binding.detailRestaurantSecondary.setText(restaurant.getAddress());

        // Load the photo
        Glide.with(binding.getRoot())
                .load(restaurant.getPhoto(800))
                .apply(RequestOptions.centerCropTransform())
                .placeholder(R.drawable.login_background_img)
                .into(binding.detailRestaurantPhoto);

        // Enable or disable the "Call" button
        Intent intentDial = new Intent(Intent.ACTION_DIAL);
        intentDial.setData(Uri.parse("tel:" + restaurant.getPhoneNumber()));
        if (restaurant.getPhoneNumber() != null && intentDial.resolveActivity(getPackageManager()) != null) {
            binding.actionCall.setEnabled(true);
            binding.actionCall.setOnClickListener(view -> startActivity(intentDial));
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
                binding.actionWebsite.setOnClickListener(view -> startActivity(intentWeb));
            } else {
                binding.actionWebsite.setEnabled(false);
            }
        }

        setRating(restaurant.getRating());

        setLikeIcon(restaurant);

        binding.actionLike.setOnClickListener(view -> viewModel.toggleLike(restaurant, this::setLikeIcon));

        // Floating Action Button to chose to eat at this restaurant
        viewModel.getChosenRestaurant(chosenRestaurant -> {
            if (chosenRestaurant != null) binding.restaurantDetailFab.setActivated(chosenRestaurant.equals(restaurant.getId()));
        });
        binding.restaurantDetailFab.setOnClickListener(view -> {
            viewModel.toggleChosenRestaurant(restaurant.getId(), chosen -> {
                binding.restaurantDetailFab.setActivated(chosen);
                initNotificationAlarm();
            });
        });

    }

    private void initNotificationAlarm() {
        // TODO: À placer ailleurs
        Context context = getApplicationContext();
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 42, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar time = Calendar.getInstance();
        time.add(Calendar.SECOND, 5);
//            time.set(Calendar.HOUR_OF_DAY, 16);
//            time.set(Calendar.MINUTE, 19);
//            time.set(Calendar.SECOND, 0);
        if (Calendar.getInstance().after(time.getTime())) time.add(Calendar.DAY_OF_YEAR, 1);

        manager.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pending);
    }

    private void setRating(int rating) {
        int drawable;
        if (rating >= 3)
            drawable = R.drawable.like_3_stars;
        else if (rating == 2)
            drawable = R.drawable.like_2_stars;
        else if (rating == 1)
            drawable = R.drawable.like_1_star;
        else
            drawable = 0;
        binding.detailRestaurantPrimary.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);
    }

    private void setLikeIcon(Restaurant restaurant) {
        if (restaurant.getLikes().contains(viewModel.getUserId())) {
            binding.actionLike.setIconResource(R.drawable.ic_baseline_star_full_24);
        } else {
            binding.actionLike.setIconResource(R.drawable.ic_baseline_star_outline_24);
        }
    }
}