package com.sec.android.app.sbrowser.system;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiController {

    private static final String TAG = WifiController.class.getSimpleName();

    public static WifiManager getWifiManager(Context context) {
        return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public static void wifiEnabled(Context context, boolean enabled) {
        WifiManager wifiManager = getWifiManager(context);
        wifiManager.setWifiEnabled(enabled);
//        wifiManager.reconnect();
    }

    public static boolean isWifiEnabled(Context context) {
        WifiManager wifiManager = getWifiManager(context);
        return wifiManager.isWifiEnabled();
    }

    public static boolean reconnect(Context context) {
        WifiManager wifiManager = getWifiManager(context);
        return wifiManager.reconnect();
    }

    public static String getParsedSSID(String SSID) {
        return String.format("\"%s\"", SSID != null ? SSID : "");
    }

    public static boolean checkConnectedWifi(Context context, String SSID) {
        WifiManager wifiManager = getWifiManager(context);
        String currentSSID = wifiManager.getConnectionInfo().getSSID();
        Log.d(TAG, "Wifi 현재: " + currentSSID + " / 대상: " + SSID);

        return currentSSID.equalsIgnoreCase(getParsedSSID(SSID));
    }

    // SSID 는 따옴표로 감싸지지 않은 원문자열이다.
    public static int getNetworkId(Context context, String SSID) {
        WifiManager wifiManager = getWifiManager(context);

        for (WifiConfiguration con : wifiManager.getConfiguredNetworks()) {
            if (con.SSID.equalsIgnoreCase(getParsedSSID(SSID))) {
                return con.networkId;
            }
        }

        return -1;
    }

    /**
     * Connect to the specified wifi network.
     *
     * @param SSID      - The wifi network SSID
     * @param password  - the wifi password
     */
    public static boolean connectToWifi(Context context, String SSID, String password, boolean hidden) {
        WifiManager wifiManager = getWifiManager(context);
        String currentSSID = wifiManager.getConnectionInfo().getSSID();
        String parsedSSID = getParsedSSID(SSID);
        int networkId = -1;

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        if (!currentSSID.equalsIgnoreCase(parsedSSID)) {
            for (WifiConfiguration conf : wifiManager.getConfiguredNetworks()) {
                if (conf.SSID.equalsIgnoreCase(parsedSSID) ) {
                    networkId = conf.networkId;
                    break;
                }
            }

            if (networkId == -1) {
                WifiConfiguration conf = new WifiConfiguration();
                conf.BSSID = "any";
                conf.SSID = parsedSSID;
                conf.preSharedKey = String.format("\"%s\"", password);
                conf.hiddenSSID = hidden;
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

                networkId = wifiManager.addNetwork(conf);
                Log.d(TAG, "Wifi " + SSID + " 추가(" + networkId + ").");
            } else {
                Log.d(TAG, "저장된 Wifi(" + networkId + ")로 바로 연결.");
            }

            if (networkId > -1) {
                wifiManager.setWifiEnabled(false);
                return false;

//                wifiManager.disconnect();
//                wifiManager.enableNetwork(networkId, true);
//                wifiManager.reconnect();
            } else {
                return false;
            }
        }

        return true;
    }
}
