package com.fthiery.go4lunch.ui.MainActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fthiery.go4lunch.databinding.FragmentWorkmatesBinding;
import com.fthiery.go4lunch.ui.adapters.WorkmatesListAdapter;
import com.fthiery.go4lunch.viewmodel.MainViewModel;

public class WorkmatesFragment extends Fragment {

    private MainViewModel viewModel;
    private FragmentWorkmatesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        binding = FragmentWorkmatesBinding.inflate(inflater, container, false);

        // Initiate the RecyclerView
        WorkmatesListAdapter adapter = new WorkmatesListAdapter(false);
        binding.workmatesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false));
        binding.workmatesRecyclerView.setAdapter(adapter);

        // Add a divider between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),LinearLayoutManager.VERTICAL);
        binding.workmatesRecyclerView.addItemDecoration(dividerItemDecoration);

        // Start observing the user list
        viewModel.getWorkmatesLiveData().observe(getViewLifecycleOwner(),adapter::submitList);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}