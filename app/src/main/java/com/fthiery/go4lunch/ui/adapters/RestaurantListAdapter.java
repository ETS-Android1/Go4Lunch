package com.fthiery.go4lunch.ui.adapters;

import static com.fthiery.go4lunch.R.string;
import static com.fthiery.go4lunch.R.style;
import static java.text.DateFormat.SHORT;
import static java.text.DateFormat.getTimeInstance;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.fthiery.go4lunch.R;
import com.fthiery.go4lunch.databinding.RestaurantViewBinding;
import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.ui.detailactivity.RestaurantDetailActivity;
import com.fthiery.go4lunch.utils.WordUtils;
import com.fthiery.go4lunch.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RestaurantListAdapter extends ListAdapter<Restaurant, RestaurantListAdapter.RestaurantViewHolder> {

    public RestaurantListAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RestaurantViewBinding binding = RestaurantViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RestaurantViewHolder(binding);
    }

    @Override
    public void submitList(@Nullable List<Restaurant> list) {
        super.submitList(list);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static final DiffUtil.ItemCallback<Restaurant> DIFF_CALLBACK = new DiffUtil.ItemCallback<Restaurant>() {

        @Override
        public boolean areItemsTheSame(@NonNull Restaurant oldItem, @NonNull Restaurant newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Restaurant oldItem, @NonNull Restaurant newItem) {
            return oldItem.equals(newItem);
        }
    };

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private final RestaurantViewBinding itemBinding;
        Resources res = itemView.getResources();
        Context context = itemView.getContext();

        public RestaurantViewHolder(RestaurantViewBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        void bind(Restaurant restaurant) {

            itemBinding.restaurantName.setText(WordUtils.capitalize(restaurant.getName()));
            itemBinding.restaurantAddress.setText(restaurant.getAddress());

            if (restaurant.getWorkmates() != 0) {
                itemBinding.workmates.setVisibility(View.VISIBLE);
                itemBinding.workmates.setText(String.format("(%s)", restaurant.getWorkmates()));
            } else {
                itemBinding.workmates.setVisibility(View.INVISIBLE);
            }

            setRating(restaurant.getRating());

            setOpeningHours(restaurant);

            itemBinding.distance.setText(String.format("%s m", restaurant.getDistance()));

            Glide.with(itemBinding.getRoot())
                    .load(restaurant.getPhoto(300))
                    .placeholder(R.drawable.restaurant_photo_placeholder)
                    .transform(new CenterCrop(), new RoundedCorners(8))
                    .into(itemBinding.restaurantPhoto);

            itemView.setOnClickListener(view -> {
                Intent detailActivity = new Intent(view.getContext(), RestaurantDetailActivity.class);
                detailActivity.putExtra("Id", restaurant.getId());
                view.getContext().startActivity(detailActivity);
            });
        }

        private void setRating(int rating) {
            if (rating >= 3)
                itemBinding.restaurantRating.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like_3_stars));
            else if (rating == 2)
                itemBinding.restaurantRating.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like_2_stars));
            else if (rating == 1)
                itemBinding.restaurantRating.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.like_1_star));
            else
                itemBinding.restaurantRating.setImageDrawable(null);
        }

        private void setOpeningHours(Restaurant restaurant) {
            itemBinding.openTill.setTextAppearance(context, R.style.Open);
            if (restaurant.getOpeningHours() != null) {
                if (restaurant.getOpeningHours().isOpenAt(Calendar.getInstance())) {
                    // Restaurant is open
                    Calendar closingTime = restaurant.getOpeningHours().nextTime(Calendar.getInstance());
                    Calendar inOneHour = Calendar.getInstance();
                    inOneHour.add(Calendar.HOUR, 1);

                    if (inOneHour.after(closingTime)) {
                        // Closing soon
                        itemBinding.openTill.setText(res.getString(string.closingSoon));
                        itemBinding.openTill.setTextAppearance(context, style.ClosingSoon);
                    } else {
                        String hour = getTimeInstance(SHORT).format(closingTime.getTime());
                        itemBinding.openTill.setText(String.format(res.getString(string.openUntil), hour));
                    }
                } else {
                    // Restaurant is closed
                    Calendar openingTime = restaurant.getOpeningHours().nextTime(Calendar.getInstance());
                    String day = new SimpleDateFormat("EEEE", Locale.getDefault()).format(openingTime.getTime());
                    String hour = getTimeInstance(SHORT).format(openingTime.getTime());

                    Calendar now = Calendar.getInstance();

                    Calendar tomorrow = Calendar.getInstance();
                    tomorrow.add(Calendar.DAY_OF_YEAR, 1);

                    itemBinding.openTill.setTextAppearance(context, style.Closed);

                    if (openingTime.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                        // Opens later today
                        itemBinding.openTill.setText(String.format(res.getString(string.closedUntilHour), hour));
                    } else if (openingTime.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)) {
                        // Opens tomorrow
                        itemBinding.openTill.setText(String.format(res.getString(string.closedUntilTomorrow), hour));
                    } else {
                        // Opens another day
                        itemBinding.openTill.setText(String.format(res.getString(string.closedUntilDayHour), day, hour));
                    }
                }
            } else {
                itemBinding.openTill.setText(string.unknownOpeningHours);
            }
        }
    }
}
