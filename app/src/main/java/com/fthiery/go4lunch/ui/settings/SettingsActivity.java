package com.fthiery.go4lunch.ui.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.fthiery.go4lunch.R;
import com.fthiery.go4lunch.viewmodel.MainViewModel;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment(new ViewModelProvider(this).get(MainViewModel.class)))
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private final MainViewModel viewModel;

        public SettingsFragment(MainViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference deleteAccount = findPreference("delete_account");
            if (deleteAccount != null) {
                deleteAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                        builder.setTitle(R.string.delete_account_dialog_title)
                                .setMessage(R.string.delete_account_warning)
                                .setPositiveButton(R.string.delete_acount_dialog_confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        viewModel.deleteUser(requireContext());
                                        requireActivity().finish();
                                    }
                                })
                                .setNegativeButton(R.string.delete_acount_dialog_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                        .create()
                        .show();

                        return false;
                    }
                });
            }
        }

    }
}