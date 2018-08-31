package com.smartivt.smartivtmessenger;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;

public class JSPInterface {
    private final String TAG = "JSPInterface";
    private String token = null;

    public JSPInterface () {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken();
            }
        });
    }

    @JavascriptInterface
    public String testMethod (final String val) {
        Log.d(TAG, "Received: " +  val);

        return "Return value";
    }

    @JavascriptInterface
    public String getToken () {
        Log.d(TAG, "getToken: " + token);
        return token;
    }
}
