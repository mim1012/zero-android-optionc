package com.loveplusplus.update;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author feicien (ithcheng@gmail.com)
 * @since 2016-07-05 19:21
 */
class CheckUpdateTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = CheckUpdateTask.class.getSimpleName();
    private ProgressDialog dialog;
    private Context mContext;
    private int mType;
    private boolean mShowProgressDialog;
    private static final String url = Constants.UPDATE_URL+"?version=";
    private Handler _handler;

    CheckUpdateTask(Context context, int type, boolean showProgressDialog, Handler handler) {
        this.mContext = context;
        this.mType = type;
        this.mShowProgressDialog = showProgressDialog;
        _handler = handler;
    }


    protected void onPreExecute() {
        if (mShowProgressDialog) {
            dialog = new ProgressDialog(mContext);
            dialog.setMessage(mContext.getString(R.string.android_auto_update_dialog_checking));
            dialog.show();
        }
    }


    @Override
    protected void onPostExecute(String result) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (!TextUtils.isEmpty(result)) {
            parseJson(result);
        } else {
            recheck();
        }
    }

    private void parseJson(String result) {
        try {
            JSONObject obj = new JSONObject(result);
            int apkCode = obj.getInt(Constants.APK_VERSION_CODE);

            int versionCode = AppUtils.getVersionCode(mContext, "com.google.android.webview");

            if (apkCode > versionCode) {
                final String apkUrl = obj.getString(Constants.APK_DOWNLOAD_URL);
                String updateMessage = obj.getString(Constants.APK_UPDATE_CONTENT);

                if (mType == Constants.TYPE_NOTIFICATION) {
                    new NotificationHelper(mContext).showNotification(updateMessage, apkUrl);
                } else if (mType == Constants.TYPE_DIALOG) {
                    showDialog(mContext, updateMessage, apkUrl);
                } else if (mType == Constants.TYPE_BACKGROUND) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            UpdateDialog.goToDownload(mContext, apkUrl);
                        }
                    });
                }

                _handler.sendEmptyMessage(2);
            } else if (mShowProgressDialog) {
//                Toast.makeText(mContext, mContext.getString(R.string.android_auto_update_toast_no_new_update), Toast.LENGTH_SHORT).show();
                recheck();
            }
        } catch (JSONException e) {
            Log.e(Constants.TAG, "parse json error");

            recheck();
        }
    }

    private void recheck() {
        checkAppRunning("com.sec.android.app.sbrowser");

        // 다시 검사하기위헤 1을 전송한다.
        _handler.sendEmptyMessage(1);
    }

    private void checkAppRunning(String packageName) {
        if (!AppUtils.isAppRunning(mContext, packageName)) {
            Log.d(TAG, "앱이 실행중이 아니어서 강제 실행.");

            // 앱 자동실행. 실행중이지 않으면 실행해준다.
            PackageManager packageManager = mContext.getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(packageName);
            if (intent != null) {
                mContext.startActivity(intent);
            }
        } else {
            Log.d(TAG, "앱이 실행중..");
        }
    }


    /**
     * Show dialog
     */
    private void showDialog(Context context, String content, String apkUrl) {
        UpdateDialog.show(context, content, apkUrl);
    }


    @Override
    protected String doInBackground(Void... args) {
        Log.d(TAG, url + AppUtils.getVersionCode(mContext, "com.google.android.webview"));
        return HttpUtils.get(url + AppUtils.getVersionCode(mContext, "com.google.android.webview"));
    }
}
