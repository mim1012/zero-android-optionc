package com.sec.android.app.sbrowser.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sec.android.app.sbrowser.engine.UserManager;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            UserManager.getInstance().setBootTime(context);
        }
    }
}
