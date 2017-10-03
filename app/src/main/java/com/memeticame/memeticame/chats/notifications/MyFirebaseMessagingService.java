package com.memeticame.memeticame.chats.notifications;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by ESTEBANFML on 03-10-2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String from = remoteMessage.getFrom();
        Log.d("REMOTEMESS", from);

        if (remoteMessage.getNotification() != null) {
            Log.d("NOTIFICATION", remoteMessage.getNotification().getBody());
        }
    }
}
