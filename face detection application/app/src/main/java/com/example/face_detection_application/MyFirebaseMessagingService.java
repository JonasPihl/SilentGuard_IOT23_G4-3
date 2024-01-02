package com.example.face_detection_application;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.example.face_detection_application.ui.log.retrofitInterface;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String serverAddress = "http://192.168.1.174";

    @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            if (remoteMessage.getData().size() > 0) {
                // Handle data payload
                Map<String, String> data = remoteMessage.getData();
                // Access data values using data.get("key")
                System.out.println("Visitor Detected");
                // Customize this method to handle data payload
            }

            if (remoteMessage.getNotification() != null) {
                // Handle notification payload

                sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
            }
        }

        private void sendNotification(String title, String body) {
            // Customize this method to create and show a notification based on the title and body
            // You can use NotificationCompat.Builder to create a notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String NOTIFICATION_CHANNEL_ID = "12345";

            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "My Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );


            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);


            int smallIconResourceId = R.drawable.ic_notifications_black_24dp;
            notificationBuilder.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setTicker("Hearty365")
                    .setSmallIcon(smallIconResourceId)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentInfo("Info");

            notificationManager.notify(12345, notificationBuilder.build());


        }

        @Override
        public void onNewToken(String token) {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(serverAddress).addConverterFactory(GsonConverterFactory.create()).build();
            retrofitInterface apiService = retrofit.create(retrofitInterface.class);
            Call<String> FCMToken = apiService.updateFCMToken(token);

            FCMToken.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }
    }


