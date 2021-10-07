package com.fthiery.go4lunch.viewmodel;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.fthiery.go4lunch.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public class MyViewModel extends ViewModel {
    private UserRepository userRepository;

    public MyViewModel() {
        super();
        userRepository = UserRepository.getInstance();
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public Boolean isCurrentUserLogged(){
        return (this.getCurrentUser() != null);
    }

    public Task<Void> signOut(Context context){
        return userRepository.signOut(context);
    }

    public Task<Void> deleteUser(Context context) {
        return userRepository.deleteUser(context);
    }
}
