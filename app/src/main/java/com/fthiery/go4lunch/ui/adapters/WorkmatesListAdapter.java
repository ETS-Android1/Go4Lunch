package com.fthiery.go4lunch.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fthiery.go4lunch.R;
import com.fthiery.go4lunch.databinding.WorkmateViewBinding;
import com.fthiery.go4lunch.model.User;
import com.fthiery.go4lunch.ui.detailactivity.RestaurantDetailActivity;

public class WorkmatesListAdapter extends ListAdapter<User, WorkmatesListAdapter.WorkmateViewHolder> {

    private final boolean detailActivity;

    public WorkmatesListAdapter(boolean detailActivity) {
        super(DIFF_CALLBACK);
        this.detailActivity = detailActivity;
    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        WorkmateViewBinding binding = WorkmateViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WorkmateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmateViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    };

    class WorkmateViewHolder extends RecyclerView.ViewHolder {
        WorkmateViewBinding itemBinding;

        public WorkmateViewHolder(WorkmateViewBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        void bind(User user) {
            Resources resources = itemView.getResources();
            Context context = itemView.getContext();
            if (detailActivity) {
                // Detail activity
                itemBinding.workmateName.setTextAppearance(context, R.style.WorkmateDecided);
                itemBinding.workmateName.setText(String.format(resources.getString(R.string.is_joining), user.getName()));
                itemView.setOnClickListener(null);
            } else {
                // Main activity
                if (user.getChosenRestaurant() != null) {
                    // If the workmate has chosen a restaurant, display his choice and set the onClickListener
                    itemBinding.workmateName.setTextAppearance(context, R.style.WorkmateDecided);
                    itemBinding.workmateName.setText(String.format(resources.getString(R.string.is_eating_at),
                            user.getName(),
                            user.getChosenRestaurant().getName()));

                    itemView.setOnClickListener(view -> {
                        Intent detailActivity = new Intent(view.getContext(), RestaurantDetailActivity.class);
                        detailActivity.putExtra("Id", user.getChosenRestaurantId());
                        view.getContext().startActivity(detailActivity);
                    });
                } else {
                    // If the workmate hasn't chosen a restaurant, gray out his name
                    itemBinding.workmateName.setTextAppearance(context, R.style.WorkmateUndecided);
                    itemBinding.workmateName.setText(String.format(resources.getString(R.string.hasnt_decided),
                            user.getName()));
                    itemView.setOnClickListener(null);
                }
            }

            Glide.with(itemBinding.getRoot())
                    .load(user.getPhoto())
                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                    .apply(RequestOptions.circleCropTransform())
                    .into(itemBinding.workmatePhoto);

        }
    }
}
