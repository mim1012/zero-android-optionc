package com.sec.android.app.sbrowser.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiReceiver extends BroadcastReceiver {

    private static final String TAG = WifiReceiver.class.getSimpleName();

    private WifiManager _wifiManager = null;
    private OnWifiStateChangedListener _listener = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        _wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        String action =  intent.getAction();

        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
//            switch (_wifiManager.getWifiState()) {
//                case WifiManager.WIFI_STATE_DISABLED:
//                    Log.d(TAG, "WIFI_STATE_DISABLED");
//                    break;
//
//                case WifiManager.WIFI_STATE_ENABLED:
//                    Log.d(TAG, "WIFI_STATE_ENABLED");
//                    break;
//
//                case WifiManager.WIFI_STATE_UNKNOWN:
//                    Log.d(TAG, "WIFI_STATE_UNKNOWN");
//                    break;
//            }

            if (_listener != null) {
                _listener.OnChanged(_wifiManager.getWifiState());
            }
        } else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
//            List<ScanResult> results = _wifiManager.getScanResults();
            Log.d(TAG, "SCAN_RESULTS_AVAILABLE_ACTION");

        }

    }

    public void setOnWifiStateChangedListener(OnWifiStateChangedListener listener) {
        _listener = listener;
    }

    public interface OnWifiStateChangedListener {
        void OnChanged(int state);
    }

}
