package com.sec.android.app.sbrowser.system;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MobileNetworkController {

    public static void setMobileNetworkfromLollipop(Context context) throws Exception {
        String command = null;
        int state = 0;
        try {
            // Get the current state of the mobile network.
            state = isMobileDataEnabledFromLollipop(context) ? 0 : 1;
            // Get the value of the "TRANSACTION_setDataEnabled" field.
            String transactionCode = getTransactionCode(context);
            // Android 5.1+ (API 22) and later.
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                // Loop through the subscription list i.e. SIM list.
                for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoCountMax(); i++) {
                    if (transactionCode != null && transactionCode.length() > 0) {
                        // Get the active subscription ID for a given SIM card.
                        int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
                        // Execute the command via `su` to turn off
                        // mobile network for a subscription service.
                        command = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + state;
                        executeCommandViaSu(context, "-c", command);
                    }
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                // Android 5.0 (API 21) only.
                if (transactionCode != null && transactionCode.length() > 0) {
                    // Execute the command via `su` to turn off mobile network.
                    command = "service call phone " + transactionCode + " i32 " + state;
                    executeCommandViaSu(context, "-c", command);
                }
            }
        } catch(Exception e) {
            // Oops! Something went wrong, so we throw the exception here.
            throw e;
        }
    }

    public static boolean isMobileDataEnabledFromLollipop(Context context) {
        boolean state = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            state = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
        }
        return state;
    }

    private static String getTransactionCode(Context context) throws Exception {
        try {
            final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
            final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
            mTelephonyMethod.setAccessible(true);
            final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
            final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
            final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
            final Field field = mClass.getDeclaredField("TRANSACTION_setDataEnabled");
            field.setAccessible(true);
            return String.valueOf(field.getInt(null));
        } catch (Exception e) {
            // The "TRANSACTION_setDataEnabled" field is not available,
            // or named differently in the current API level, so we throw
            // an exception and inform users that the method is not available.
            throw e;
        }
    }

    private static void executeCommandViaSu(Context context, String option, String command) {
        boolean success = false;
        String su = "su";
        for (int i=0; i < 3; i++) {
            // Default "su" command executed successfully, then quit.
            if (success) {
                break;
            }
            // Else, execute other "su" commands.
            if (i == 1) {
                su = "/system/xbin/su";
            } else if (i == 2) {
                su = "/system/bin/su";
            }
            try {
                // Execute command as "su".
                Runtime.getRuntime().exec(new String[]{su, option, command});
            } catch (IOException e) {
                success = false;
                // Oops! Cannot execute `su` for some reason.
                // Log error here.
            } finally {
                success = true;
            }
        }
    }
}
