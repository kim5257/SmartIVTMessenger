package com.smartivt.smartivtmessenger;

import android.util.Log;
import android.webkit.JavascriptInterface;
import com.google.firebase.iid.FirebaseInstanceId;

public class JSPInterface {
    private final String TAG = "JSPInterface";

    @JavascriptInterface
    public String testMethod (final String val) {
        Log.d(TAG, "Received: " +  val);

        return "Return value";
    }

    @JavascriptInterface
    public String getToken () {
        return FirebaseInstanceId.getInstance().getToken();
    }
}
