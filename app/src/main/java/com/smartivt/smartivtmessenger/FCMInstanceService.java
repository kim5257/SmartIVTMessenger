package com.smartivt.smartivtmessenger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Build;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FCMInstanceService extends FirebaseInstanceIdService {
    private final String TAG = "FCM Instance Service";

    private final String CHANNEL_ID = "DEFAULT_CHANNEL";
    private final String CHANNEL_NAME = "Default Channel";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.d(TAG, "Refreshed Token: " + refreshedToken);

        // FCM 채널 생성
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            NotificationManager notificatoinManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription(CHANNEL_NAME);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificatoinManager.createNotificationChannel(notificationChannel);
        }


    }
}
