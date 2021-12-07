package com.fthiery.go4lunch.ui.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fthiery.go4lunch.R;
import com.fthiery.go4lunch.model.User;
import com.fthiery.go4lunch.repository.RestaurantRepository;
import com.fthiery.go4lunch.repository.UserRepository;
import com.fthiery.go4lunch.utils.Callback;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.getBoolean("notifications", false)) {
            // If the user has activated notifications in the settings, create the notification

            RestaurantRepository restaurantRepository = RestaurantRepository.getInstance();
            UserRepository userRepository = UserRepository.getInstance();
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            String userId = userRepository.getCurrentUserUID();
            userRepository.getChosenRestaurant(userId, restaurantId -> {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence name = context.getString(R.string.channel_name);
                    String description = context.getString(R.string.channel_description);
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;

                    NotificationChannel channel = new NotificationChannel("Default", name, importance);
                    channel.setDescription(description);
                    // Register the channel with the system; you can't change the importance
                    // or other notification behaviors after this
                    manager.createNotificationChannel(channel);
                }

                // Build notification based on Intent
                NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "Default")
                        .setSmallIcon(R.drawable.logo);

                // Load the restaurant data from repository
                restaurantRepository.getRestaurant(
                        restaurantId,
                        restaurant -> {
                            // When the data is loaded, build the notification
                            if (restaurant != null) {
                                notification.setContentTitle(String.format(context.getString(R.string.you_are_eating_at), restaurant.getName()));
                                notification.setContentText(restaurant.getAddress());

                                // Load the restaurant photo
                                Glide.with(context)
                                        .asBitmap()
                                        .load(restaurant.getPhoto(300))
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                notification.setLargeIcon(resource);
                                                manager.notify(42, notification.build());
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                                notification.setLargeIcon(null);
                                            }
                                        });

                                // Load the list of users eating at the restaurant
                                userRepository.listenUsersEatingAt(
                                        restaurant.getId(),
                                        users -> {
                                            // When loaded, add the list to the notification
                                            if (users.size() > 1) {
                                                StringBuilder bigText = new StringBuilder(restaurant.getAddress());
                                                bigText.append(context.getString(R.string.you_ll_be_eating_with));
                                                for (User user : users) {
                                                    if (!user.getId().equals(userId))
                                                        bigText.append(String.format("\n%s", user.getName()));
                                                }
                                                notification.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
                                            }

                                            // Show the notification
                                            manager.notify(42, notification.build());
                                        });
                            }
                        });
            });
        }
    }
}
