package com.loveplusplus.update;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class UpdateChecker {


    public static void checkForDialog(Context context) {
        if (context != null) {
            new CheckUpdateTask(context, Constants.TYPE_DIALOG, true, null).execute();
        } else {
            Log.e(Constants.TAG, "The arg context is null");
        }
    }


    public static void checkForNotification(Context context) {
        if (context != null) {
            new CheckUpdateTask(context, Constants.TYPE_NOTIFICATION, false, null).execute();
        } else {
            Log.e(Constants.TAG, "The arg context is null");
        }
    }

    public static void checkForBackground(Context context, Handler handler) {
        if (context != null) {
            new CheckUpdateTask(context, Constants.TYPE_BACKGROUND, true, handler).execute();
        } else {
            Log.e(Constants.TAG, "The arg context is null");
        }
    }

}
