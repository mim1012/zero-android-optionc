package com.sec.android.app.sbrowser.engine;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.sec.android.app.sbrowser.BuildConfig;

import java.util.List;

public class AppHelper {

    private static final String TAG = AppHelper.class.getSimpleName();

    public static void checkAppRunning(Context context, String packageName) {
        if (!isForegroundApp(context, packageName)) {
            Log.d(TAG, "앱이 실행중이 아니어서 강제 실행: " + packageName);

            // 앱 자동실행. 실행중이지 않으면 실행해준다.
            launchIntentForPackage(context, packageName);
        } else {
            Log.d(TAG, "앱이 실행중..");
        }
    }

    public static void launchIntentForPackage(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);

        if (intent != null) {
            context.startActivity(intent);
        }
    }

    public static void launchIntentForPackageUrl(Context context, String packageName, String urlString) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);

        if (!TextUtils.isEmpty(urlString)) {
            intent.setData(Uri.parse(urlString));
        }

        // 기본 인텐트 작업.
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));

        if (intent != null) {
            context.startActivity(intent);
        }
    }

    public static boolean isForegroundApp(Context context, String packageName) {
        String foregroundApp = getForegroundApp(context);
        if (foregroundApp == null) {
            Log.d(TAG, "foregroundApp 이 null 이라서 패스.");
            return true;
        } else {
            return packageName.equals(foregroundApp);
        }
    }

    public static String getForegroundApp(Context context) {
        if (!hasUsageStatsPermission(context)) {
            Log.d(TAG, "not permission");
            return null;
        }

        String foregroundApp = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            UsageStatsManager usm = (UsageStatsManager) context.
                    getSystemService("usagestats");//Context.USAGE_STATS_SERVICE;
            long time = System.currentTimeMillis();
            // We get usage stats for the last 1800 seconds
            long lastSeconds = 1800;

            if (BuildConfig.FLAVOR_build.equals("single")) {
                lastSeconds = 18000;//3600 * 5;
            }

            long beginTime = time - 1000 * lastSeconds;

            // Method 1. 시간순 정렬로 찾기.
            List<UsageStats> statsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * lastSeconds, time);

            if (statsList != null && statsList.size() > 0) {
//                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
//                for (UsageStats usageStats : statsList) {
//                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
//                }
//                if (!mySortedMap.isEmpty()) {
//                    foregroundApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
//                }

                long lastUsedAppTime = 0;
                for (UsageStats usageStats : statsList) {
                    if (usageStats.getLastTimeUsed() > lastUsedAppTime) {
                        foregroundApp = usageStats.getPackageName();
                        lastUsedAppTime = usageStats.getLastTimeUsed();
                    }
                }
            }

            // Method 2. 이벤트 순으로 찾기.
//            UsageEvents usageEvents = usm.queryEvents(beginTime, time);
//            UsageEvents.Event event = new UsageEvents.Event();
//            while (usageEvents.hasNextEvent()) {
//                usageEvents.getNextEvent(event);
//                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
//                    foregroundApp = event.getPackageName();
//                }
//            }

        } else {
            ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            foregroundApp = tasks.get(0).processName;
        }

        Log.d(TAG, "foreground app: " + foregroundApp);

//        Log.e("adapter", "Current App in foreground is: " + foregroundApp);
        return foregroundApp;
    }

    //checkUsageStatsPermission
    public static boolean hasUsageStatsPermission(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    public static boolean checkUsageStatsPermission(Context context) {
//        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
//        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
//                android.os.Process.myUid(), context.getPackageName());
//        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
//        return granted;
//    }

    public static int getVersionCode(Context context, String packageName) {
        try {
            int versionCode = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
            Log.d(TAG, "[" + packageName + "] versionCode: " + versionCode);
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getVersionName(Context context, String packageName) {
        try {
            String versionName = context.getPackageManager().getPackageInfo(packageName, 0).versionName;
            Log.d(TAG, "[" + packageName + "] version: " + versionName);
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "미설치";
    }
}
