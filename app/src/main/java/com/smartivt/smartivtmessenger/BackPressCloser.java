package com.smartivt.smartivtmessenger;

import android.app.Activity;
import android.widget.Toast;

public class BackPressCloser {
    private Activity activity;

    private Toast toast;
    private long timeout = 0;

    public BackPressCloser (Activity context ) {
        this.activity = context;
    }

    public void onBackPress () {
        long curTime = System.currentTimeMillis();

        if ( curTime > timeout + 2000 ) {
            timeout = curTime;
            showGuide();
        }
        else {
            toast.cancel();
            activity.finish();
        }
    }

    public void showGuide () {
        toast = Toast.makeText(activity, activity.getString(R.string.close_ment), Toast.LENGTH_SHORT);
        toast.show();
    }
}
