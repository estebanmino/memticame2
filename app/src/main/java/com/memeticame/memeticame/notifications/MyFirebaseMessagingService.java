package com.memeticame.memeticame.notifications;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
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
            final String message = remoteMessage.getNotification().getBody();
            Intent intent = new Intent("com.memeticame.memeticame_FCM_MESSAGE");
            intent.putExtra("message", message);
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
            localBroadcastManager.sendBroadcast(intent);

        }

    }
}
