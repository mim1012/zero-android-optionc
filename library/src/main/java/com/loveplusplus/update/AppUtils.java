package com.loveplusplus.update;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;

/**
 * @author feicien (ithcheng@gmail.com)
 * @since 2016-07-05 17:41
 */

public class AppUtils {

    public static int getVersionCode(Context mContext, String packageName) {
        if (mContext != null) {
            try {
                int test = mContext.getPackageManager().getPackageInfo(packageName, 0).versionCode;
                Log.d("ver:", "together: versionCode" + test);
                return mContext.getPackageManager().getPackageInfo(packageName, 0).versionCode;
//                return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return 0;
    }

    public static String getVersionName(Context mContext, String packageName) {
        if (mContext != null) {
            try {
                return mContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
//                return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        return "";
    }

    public static boolean isAppRunning(final Context context, final String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null)
        {
            for (ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }

        return false;
    }
}
