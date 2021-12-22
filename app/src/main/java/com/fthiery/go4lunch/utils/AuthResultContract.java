package com.fthiery.go4lunch.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.fthiery.go4lunch.R;

import java.util.Arrays;
import java.util.List;

public class AuthResultContract extends ActivityResultContract<Integer, IdpResponse> {

    private static final String INPUT_INT = "input_int";
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.FacebookBuilder().build(),
            new AuthUI.IdpConfig.TwitterBuilder().build(),
            new AuthUI.IdpConfig.EmailBuilder().build());

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Integer input) {
        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false, true)
                .setLogo(R.drawable.logo)
                .build()
                .putExtra(INPUT_INT, input);
    }

    @Override
    public IdpResponse parseResult(int resultCode, @Nullable Intent intent) {
        if (resultCode == Activity.RESULT_OK) return IdpResponse.fromResultIntent(intent);
        else return null;
    }
}
