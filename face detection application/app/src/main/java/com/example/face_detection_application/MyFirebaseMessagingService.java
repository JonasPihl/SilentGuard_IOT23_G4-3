package com.example.face_detection_application;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

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
        }
    }


