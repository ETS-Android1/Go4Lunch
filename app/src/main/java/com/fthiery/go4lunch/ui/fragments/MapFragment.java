package com.fthiery.go4lunch.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.fthiery.go4lunch.R;
import com.fthiery.go4lunch.databinding.FragmentMapBinding;
import com.fthiery.go4lunch.model.Restaurant;
import com.fthiery.go4lunch.viewmodel.MyViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, Observer<List<Restaurant>> {

    private MyViewModel myViewModel;
    private FragmentMapBinding binding;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private List<Marker> markers = new ArrayList<>();
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableMyLocation();
                } else {
                    Toast.makeText(requireContext(), R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
                    requireActivity().finish();
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialise the ViewModel
        myViewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);

        // Inflate the fragment layout
        binding = FragmentMapBinding.inflate(inflater, container, false);

        // Initialize GoogleMaps fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize the location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        myViewModel.getRestaurantsLiveData().observe(getViewLifecycleOwner(), this);

        return binding.getRoot();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style_json));
        enableMyLocation();
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                updateLocation();
                return false;
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (isLocationPermissionGranted()) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
                updateLocation();
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    public void updateLocation() {
        if (isLocationPermissionGranted()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    myViewModel.setLocation(location);

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(),location.getLongitude()), 15));
                }
            });
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onChanged(List<Restaurant> restaurants) {
        // Clear markers
        for (Marker marker : markers) {
            marker.remove();
        }
        // Add new markers
        for (Restaurant restaurant : restaurants) {
            markers.add(googleMap.addMarker(new MarkerOptions().position(restaurant.getLatLng()).title(restaurant.getName())));
        }
    }
}