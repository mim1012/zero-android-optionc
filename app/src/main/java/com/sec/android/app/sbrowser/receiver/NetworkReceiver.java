package com.sec.android.app.sbrowser.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.sec.android.app.sbrowser.BuildConfig;

public class NetworkReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkReceiver.class.getSimpleName();

    public static final String WIFI_CONNECTED = BuildConfig.APPLICATION_ID + ".WIFI_CONNECTED";
    public static final String ANY_CONNECTED = BuildConfig.APPLICATION_ID + ".ANY_CONNECTED";
    public static final String CONNECTION_LOST = BuildConfig.APPLICATION_ID + ".CONNECTION_LOST";

    private static String _prevAction = null;
    private static String _wifiSsid = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conn =  (ConnectivityManager)
                context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        String action = null;
        boolean wifiChanged = false;

        Log.d(TAG, "Connection change received.");

        if (networkInfo != null) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                action = WIFI_CONNECTED;

                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                String ssid = null;

                if (wifiManager != null) {
                    ssid = wifiManager.getConnectionInfo().getSSID();
                }

                if ((_wifiSsid == null && ssid != null) ||
                        (_wifiSsid != null && !_wifiSsid.equals(ssid))) {
                    Log.d(TAG, "new ssid: " + _wifiSsid + " -> " + ssid);
                    wifiChanged = true;
                    _wifiSsid = ssid;
                }

//                Toast.makeText(context, "Wifi Connected", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "Wifi Connected");
            } else {
                action = ANY_CONNECTED;
                _wifiSsid = null;
//                Toast.makeText(context, "Any Connected", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "Any Connected");
            }
        } else {
            action = CONNECTION_LOST;
            _wifiSsid = null;
//            Toast.makeText(context, "Lost connection", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "Lost connection");
        }

        if (!action.equals(_prevAction) || wifiChanged) {
            _prevAction = action;
            broadcastMessage(context, action);
        } else {
//            Log.d(TAG, "Same action: " + action);
        }
    }

    private void broadcastMessage(Context context, String action) {
        // 메시지 브로드캐스팅.
        Intent intent = new Intent();
        intent.setAction(action);
        context.sendBroadcast(intent);
    }
}
