package com.sec.android.app.sbrowser.engine;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.sec.android.app.sbrowser.models.NnbData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by K2Y on 2017. 6. 15..
 */

public class UserManager {
    private static final String PREFERENCES_USER_INFO = "userInfo";

    private static class LazyHolder {
        public static final UserManager INSTANCE = new UserManager();
    }

    public static UserManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public String imei = null;
    // 쓰레드 문제가 생기므로, 여기에 두면 안된다.
    public int uaId = 0;
    public NnbData nnbData = null;
    public String ua = null;
    public String chromeVersion = null;
    public String browserVersion = null;
    public String nnb = null;
    public String webProxy = null;

    public boolean isPcUa() {
        if (!TextUtils.isEmpty(ua)) {
            if (!ua.toLowerCase().contains("mobile")) {
                return true;
            }
        }

        return false;
    }


    public void setLoginId(Context context, String loginId) {
        SharedPreferences.Editor editor = getPref(context).edit();
        editor.putString("loginId", loginId);
        editor.apply();
    }

    public String getLoginId(Context context) {
        SharedPreferences pref = getPref(context);
        return pref.getString("loginId", null);
    }

    public void setTargetSsid(Context context, String targetSsid) {
        SharedPreferences.Editor editor = getPref(context).edit();
        editor.putString("targetSsid", targetSsid);
        editor.apply();
    }

    public String getTargetSsid(Context context) {
        SharedPreferences pref = getPref(context);
        return pref.getString("targetSsid", null);
    }

    public void setTargetPassword(Context context, String targetPassword) {
        SharedPreferences.Editor editor = getPref(context).edit();
        editor.putString("targetPassword", targetPassword);
        editor.apply();
    }

    public String getTargetPassword(Context context) {
        SharedPreferences pref = getPref(context);
        return pref.getString("targetPassword", null);
    }

    public void setProxy(Context context, String proxy) {
        SharedPreferences.Editor editor = getPref(context).edit();
        editor.putString("proxy", proxy);
        editor.apply();
    }

    public String getProxy(Context context) {
        SharedPreferences pref = getPref(context);
        return pref.getString("proxy", "");
    }

    public void setBootTime(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = dateFormat.format(Calendar.getInstance().getTime());

        SharedPreferences.Editor editor = getPref(context).edit();
        editor.putString("bootTime", dateString);
        editor.apply();
    }

    public String getBootTime(Context context) {
        SharedPreferences pref = getPref(context);
        return pref.getString("bootTime", null);
    }

    public Date getBootTimeDate(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = getBootTime(context);

        if (dateString != null) {
            try {
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void setProcessCount(Context context, int count) {
        SharedPreferences.Editor editor = getPref(context).edit();
        editor.putInt("processCount", count);
        editor.apply();
    }

    public int getProcessCount(Context context) {
        SharedPreferences pref = getPref(context);
        return pref.getInt("processCount", 0);
    }

    public void setChangeIp(Context context, boolean change) {
        SharedPreferences.Editor editor = getPref(context).edit();
        editor.putBoolean("changeIp", change);
        editor.apply();
    }

    public boolean getChangeIp(Context context) {
        SharedPreferences pref = getPref(context);
        return pref.getBoolean("changeIp", false);
    }

    public void setUpdateCheckTime(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = dateFormat.format(Calendar.getInstance().getTime());

        SharedPreferences.Editor editor = getPref(context).edit();
        editor.putString("updateCheckTime", dateString);
        editor.apply();
    }

    public void clearUpdateCheckTime(Context context) {
        SharedPreferences.Editor editor = getPref(context).edit();
        editor.remove("updateCheckTime");
        editor.apply();
    }

    public String getUpdateCheckTime(Context context) {
        SharedPreferences pref = getPref(context);
        return pref.getString("updateCheckTime", null);
    }

    public Date getUpdateCheckTimeDate(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = getUpdateCheckTime(context);

        if (dateString != null) {
            try {
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void setKeywordEmptyCount(Context context, int count) {
        SharedPreferences.Editor editor = getPref(context).edit();
        editor.putInt("keywordEmptyCount", count);
        editor.apply();
    }

    public int getKeywordEmptyCount(Context context) {
        SharedPreferences pref = getPref(context);
        return pref.getInt("keywordEmptyCount", 0);
    }

    public void setIpChangeTime(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = dateFormat.format(Calendar.getInstance().getTime());

        SharedPreferences.Editor editor = getPref(context).edit();
        editor.putString("ipChangeTime", dateString);
        editor.apply();
    }

    public String getIpChangeTime(Context context) {
        SharedPreferences pref = getPref(context);
        return pref.getString("ipChangeTime", null);
    }

    public Date getIpChangeTimeDate(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = getIpChangeTime(context);

        if (dateString != null) {
            try {
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void setCaptureMode(Context context, int mode) {
        SharedPreferences.Editor editor = getPref(context).edit();
        editor.putInt("captureMode", mode);
        editor.apply();
    }

    public int getCaptureMode(Context context) {
        SharedPreferences pref = getPref(context);
        return pref.getInt("captureMode", 0);
    }



    private UserManager() {
    }

    private SharedPreferences getPref(Context context) {
        return context.getSharedPreferences(PREFERENCES_USER_INFO, Context.MODE_PRIVATE);
    }

}
