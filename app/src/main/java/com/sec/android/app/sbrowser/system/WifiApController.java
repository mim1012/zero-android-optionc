package com.sec.android.app.sbrowser.system;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.android.dx.stock.ProxyBuilder;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class WifiApController {

    private static final String TAG = WifiApController.class.getSimpleName();

    public static boolean setEnabled(Context context, boolean enabled) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wificonfiguration = null;
        try {
            if (enabled) {
                // if WiFi is on, turn it off
                if (isEnabled(context)) {
                    wifiManager.setWifiEnabled(false);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enableTetheringNew(context, new MyOnStartTetheringCallback() {
                    @Override
                    public void onTetheringStarted() {
                        Log.d(TAG, "Wifi Hotspot is on now");
                    }

                    @Override
                    public void onTetheringFailed() {
                        Log.d(TAG, "Wifi Hotspot is on failed.");
                    }
                });

                // 콜백 방식이므로 잠시 대기.
                SystemClock.sleep(1000);

                // 아래 방식을 사용하면 위치를 켜야한다.
//                wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
//                    @Override
//                    public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
//                        super.onStarted(reservation);
//                        Log.d("TAG!!", "Wifi Hotspot is on now");
////                        mReservation = reservation;
//                    }
//
//                    @Override
//                    public void onStopped() {
//                        super.onStopped();
//                        Log.d("TAG!!", "Wifi Hotspot onStopped");
//                    }
//
//                    @Override
//                    public void onFailed(int reason) {
//                        super.onFailed(reason);
//                        Log.d("TAG!!", "Wifi Hotspot onFailed" + reason);
//                    }
//                }, null);
            } else {
                Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method.invoke(wifiManager, wificonfiguration, enabled);
            }

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isEnabled(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);

            return (Boolean) method.invoke(wifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean toggleEnabled(Context context) {
        return setEnabled(context, !isEnabled(context));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean enableTetheringNew(Context context, final MyOnStartTetheringCallback callback) {
        File outputDir = context.getCodeCacheDir();
        Object proxy;
        try {
            proxy = ProxyBuilder.forClass(OnStartTetheringCallbackClass())
                    .dexCache(outputDir).handler(new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            switch (method.getName()) {
                                case "onTetheringStarted":
                                    callback.onTetheringStarted();
                                    break;
                                case "onTetheringFailed":
                                    callback.onTetheringFailed();
                                    break;
                                default:
                                    ProxyBuilder.callSuper(proxy, method, args);
                            }
                            return null;
                        }

                    }).build();
        } catch (Exception e) {
            Log.e(TAG, "Error in enableTethering ProxyBuilder");
            e.printStackTrace();
            return false;
        }

        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(ConnectivityManager.class);
        Method method = null;
        try {
            method = manager.getClass().getDeclaredMethod("startTethering", int.class, boolean.class, OnStartTetheringCallbackClass(), Handler.class);
            if (method == null) {
                Log.e(TAG, "startTetheringMethod is null");
            } else {
                method.invoke(manager, ConnectivityManager.TYPE_MOBILE, false, proxy, null);
                Log.d(TAG, "startTethering invoked");
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error in enableTethering");
            e.printStackTrace();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopTethering(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getApplicationContext().getSystemService(ConnectivityManager.class);

        try {
            Method method = manager.getClass().getDeclaredMethod("stopTethering", int.class);
            if (method == null) {
                Log.e(TAG, "stopTetheringMethod is null");
            } else {
                method.invoke(manager, ConnectivityManager.TYPE_MOBILE);
                Log.d(TAG, "stopTethering invoked");
            }
        } catch (Exception e) {
            Log.e(TAG, "stopTethering error: " + e.toString());
            e.printStackTrace();
        }
    }

    private static Class OnStartTetheringCallbackClass() {
        try {
            return Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "OnStartTetheringCallbackClass error: " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public abstract static class MyOnStartTetheringCallback {
        /**
         * Called when tethering has been successfully started.
         */
        public abstract void onTetheringStarted();

        /**
         * Called when starting tethering failed.
         */
        public abstract void onTetheringFailed();

    }
}
