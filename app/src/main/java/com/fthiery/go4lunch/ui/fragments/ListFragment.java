package com.fthiery.go4lunch.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fthiery.go4lunch.databinding.FragmentListBinding;
import com.fthiery.go4lunch.viewmodel.MyViewModel;

public class ListFragment extends Fragment {

    private MyViewModel myViewModel;
    private FragmentListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myViewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);

        binding = FragmentListBinding.inflate(inflater, container, false);

        RestaurantListAdapter adapter = new RestaurantListAdapter();
        binding.restaurantsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        binding.restaurantsRecyclerView.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),LinearLayoutManager.VERTICAL);
        binding.restaurantsRecyclerView.addItemDecoration(dividerItemDecoration);

        myViewModel.getRestaurantsLiveData().observe(requireActivity(),adapter::submitList);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}