package com.smartivt.smartivtmessenger;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import java.util.List;

public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMessaging";

    private final String CHANNEL_ID = "DEFAULT_CHANNEL";
    private final String CHANNEL_NAME = "Default Channel";
    private final String GROUP_NAME = "MESSAGE_GROUP";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        NotificationManager notiMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if ( remoteMessage.getData().get("title") != null ) {
            Log.d(TAG, "onMessageReceived: " + remoteMessage.getData().get("title"));
            Log.d(TAG, "onMessageReceived: " + remoteMessage.getData().get("body"));

            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
                Notification.Builder notify = new Notification.Builder(getApplicationContext(), CHANNEL_ID);

                notify.setSmallIcon(R.drawable.ic_stat_notification);
                notify.setContentTitle(remoteMessage.getData().get("title"));
                notify.setContentText(remoteMessage.getData().get("body"));
                notify.setAutoCancel(true);

                notiMgr.notify(NotificationID.getID(), notify.build());

                /*
                Intent badgeIntent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");

                badgeIntent.putExtra("badge_count", 5);
                badgeIntent.putExtra("badge_count_package_name", getPackageName());
                badgeIntent.putExtra("badge_count_class_name", getLauncherClassName());
                sendBroadcast(badgeIntent);
                */
                Log.d(TAG, "update badge: " + getPackageName() + ", " + getLauncherClassName());
            }
            else {
                Notification.Builder notify = new Notification.Builder(getApplicationContext());

                notify.setSmallIcon(R.drawable.ic_stat_notification);
                notify.setContentTitle(remoteMessage.getData().get("title"));
                notify.setContentText(remoteMessage.getData().get("body"));
                notify.setAutoCancel(true);

                int id = NotificationID.getID();

                Log.d(TAG, "id: " + id);
                notiMgr.notify(id, notify.build());
            }
        }
        else if ( remoteMessage.getNotification() != null ) {
            Log.d(TAG, "onMessageReceived2: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "onMessageReceived2: " + remoteMessage.getNotification().getBody());

            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
                Notification.Builder notify = new Notification.Builder(getApplicationContext(), CHANNEL_ID);

                notify.setSmallIcon(R.drawable.ic_stat_notification);
                notify.setContentTitle(remoteMessage.getNotification().getTitle());
                notify.setContentText(remoteMessage.getNotification().getBody());
                notify.setAutoCancel(true);

                notiMgr.notify(NotificationID.getID(), notify.build());
            }
            else {
                Notification.Builder notify = new Notification.Builder(getApplicationContext());

                notify.setSmallIcon(R.drawable.ic_stat_notification);
                notify.setContentTitle(remoteMessage.getNotification().getTitle());
                notify.setContentText(remoteMessage.getNotification().getBody());
                notify.setAutoCancel(true);

                notiMgr.notify(NotificationID.getID(), notify.build());
            }
        }
    }

    @Nullable
    private String getLauncherClassName () {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = getApplicationContext().getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if ( pkgName.equalsIgnoreCase(getPackageName())) {
                return resolveInfo.activityInfo.name;
            }
        }

        return null;
    }
}
