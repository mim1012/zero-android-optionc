package com.sec.android.app.sbrowser.engine;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by K2Y on 2017. 8. 28..
 */

public class SystemHelper {
    private static final String TAG = "SystemHelper";

    public static String getVersion(Context context) {
        String version = "1";

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }

    public static int getVersionCode(Context context) {
        int code = 1;

        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return code;
    }

    public static void logDeviceInfo() {
        Log.i(TAG, "BOARD = " + Build.BOARD);
        Log.i(TAG, "BRAND = " + Build.BRAND);
        Log.i(TAG, "CPU_ABI = " + Build.CPU_ABI);
        Log.i(TAG, "DEVICE = " + Build.DEVICE);
        Log.i(TAG, "DISPLAY = " + Build.DISPLAY);
        Log.i(TAG, "FINGERPRINT = " + Build.FINGERPRINT);
        Log.i(TAG, "HOST = " + Build.HOST);
        Log.i(TAG, "ID = " + Build.ID);
        Log.i(TAG, "MANUFACTURER = " + Build.MANUFACTURER);
        Log.i(TAG, "MODEL = " + Build.MODEL);
        Log.i(TAG, "PRODUCT = " + Build.PRODUCT);
        Log.i(TAG, "TAGS = " + Build.TAGS);
        Log.i(TAG, "TYPE = " + Build.TYPE);
        Log.i(TAG, "USER = " + Build.USER);
        Log.i(TAG, "VERSION.RELEASE = " + Build.VERSION.RELEASE);
    }

    public static boolean hasSim(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT;
    }

    public static String getTelecomName(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (!hasSim(context)) {
            return null;
        }

        String simOperator = tm.getSimOperator();
        Log.d(TAG, "simOperator: " + simOperator);
        if (simOperator != null) {
            if (simOperator.contains("45005") || simOperator.equals("45012")) {
                return "sk";
            } else if (simOperator.equals("45008") || simOperator.equals("45007") ||
                    simOperator.equals("45002") || simOperator.equals("45004")) {
                return "kt";
            } else if (simOperator.equals("45006")) {
                return "lg";
            }
        }

//        String networkOperator = tm.getNetworkOperatorName();
//        Log.d(TAG, "networkOperator: " + networkOperator);
//        if (networkOperator != null) {
//            if (networkOperator.contains("SK")) {
//                return "sk";
//            } else if (networkOperator.equals("KT") || networkOperator.equals("olleh")) {
//                return "kt";
//            } else if (networkOperator.matches(".*LG.*")) {
//                return "lg";
//            }
//        }

        return null;
    }

    /**
     * 배터리 잔량 퍼센티지로 반환.
     * @param context {@link Context}
     * @return 배터리 잔량(0 ~ 100)
     */
    public static int getBatteryRemain(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level * 100 / (float)scale;
            return (int) batteryPct;
        }
    }

    public static int getBatteryHealth(Context context) {
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, 1);
        return health;

//        switch (health) {
//            case BatteryManager.BATTERY_HEALTH_GOOD:
//                return "좋음";
//            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
//                return "차가움";
//            case BatteryManager.BATTERY_HEALTH_DEAD:
//                return "차가움";
//            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
//                return "차가움";
//            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
//                return "차가움";
//            case BatteryManager.BATTERY_HEALTH_COLD:
//                return "차가움";
//
//            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
//            default:
//                return "알수없음";
//        }
    }
}
