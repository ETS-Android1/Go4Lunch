package com.fthiery.go4lunch.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fthiery.go4lunch.databinding.RestaurantViewBinding;
import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.ui.detailactivity.RestaurantDetailActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.List;
import java.util.Locale;

public class RestaurantListAdapter extends ListAdapter<Restaurant,RestaurantListAdapter.RestaurantViewHolder> {

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

        public RestaurantViewHolder(RestaurantViewBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        void bind(Restaurant restaurant) {

            itemBinding.restaurantName.setText(restaurant.getName());
            itemBinding.restaurantAddress.setText(restaurant.getAddress());
            if (restaurant.getWorkmates() != 0) {
                itemBinding.workmates.setVisibility(View.VISIBLE);
                itemBinding.workmates.setText(String.format("(%s)", restaurant.getWorkmates()));
            } else {
                itemBinding.workmates.setVisibility(View.INVISIBLE);
            }

            itemBinding.distance.setText(String.format("%s m",restaurant.getDistance()));

            Glide.with(itemBinding.getRoot())
                    .load(restaurant.getPhoto())
                    .apply(RequestOptions.centerCropTransform())
                    .into(itemBinding.restaurantPhoto);

            itemView.setOnClickListener(view -> {
                Intent detailActivity = new Intent(view.getContext(), RestaurantDetailActivity.class);
                detailActivity.putExtra("Id", restaurant.getId());
                view.getContext().startActivity(detailActivity);
            });
        }
    }
}
