package com.smartivt.smartivtmessenger;

import android.nfc.Tag;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FCMInstanceService extends FirebaseInstanceIdService {
    private final String TAG = "FCM Instance Service";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.d(TAG, "Refreshed Token: " + refreshedToken);
    }
}
