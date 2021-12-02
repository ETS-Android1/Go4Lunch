package com.fthiery.go4lunch.ui.mainactivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fthiery.go4lunch.databinding.FragmentListBinding;
import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.ui.adapters.RestaurantListAdapter;
import com.fthiery.go4lunch.viewmodel.MainViewModel;

import java.util.Collections;

public class RestaurantListFragment extends Fragment {

    private FragmentListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        binding = FragmentListBinding.inflate(inflater, container, false);

        // Initiate the RecyclerView
        RestaurantListAdapter adapter = new RestaurantListAdapter();
        binding.restaurantsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        binding.restaurantsRecyclerView.setAdapter(adapter);

        // Add a divider between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),LinearLayoutManager.VERTICAL);
        binding.restaurantsRecyclerView.addItemDecoration(dividerItemDecoration);

        // Start observing the restaurants list
        viewModel.getRestaurantsLiveData().observe(getViewLifecycleOwner(), list -> {
            Collections.sort(list, new Restaurant.RestaurantDistanceComparator());
            adapter.submitList(list);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}