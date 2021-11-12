package com.fthiery.go4lunch.ui.adapters;

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

public class WorkmatesListAdapter extends ListAdapter<User, WorkmatesListAdapter.WorkmateViewHolder> {

    WorkmateViewBinding binding;

    public WorkmatesListAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = WorkmateViewBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
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

    static class WorkmateViewHolder extends RecyclerView.ViewHolder {
        WorkmateViewBinding itemBinding;

        public WorkmateViewHolder(WorkmateViewBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        void bind(User user) {
            if (user.getChosenRestaurantId() != null) {
                // If the workmate has chosen a restaurant, display his choice
                itemBinding.workmateName.setTextAppearance(itemView.getContext(),R.style.WorkmateDecided);
                itemBinding.workmateName.setText(String.format("%s %s", user.getName(), itemView.getResources().getString(R.string.is_eating_at)));
                // TODO: missing restaurant name
            } else {
                itemBinding.workmateName.setTextAppearance(itemView.getContext(),R.style.WorkmateUndecided);
                itemBinding.workmateName.setText(String.format("%s %s", user.getName(), itemView.getResources().getString(R.string.hasnt_decided)));
            }
            Glide.with(itemBinding.getRoot())
                    .load(user.getPhoto())
                    .apply(RequestOptions.circleCropTransform())
                    .into(itemBinding.workmatePhoto);
        }
    }
}
