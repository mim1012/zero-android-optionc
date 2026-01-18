package com.sec.android.app.sbrowser.pattern.action;

import android.content.Context;

import com.sec.android.app.sbrowser.engine.AppHelper;
import com.sec.android.app.sbrowser.library.updater.UpdaterApi;
import com.sec.android.app.sbrowser.library.updater.client.UpdaterClient;
import com.sec.android.app.sbrowser.library.updater.retrofit.models.VersionData;

public class UpdateAction {

    private static final String TAG = UpdateAction.class.getSimpleName();

    private final Object _mutex = new Object();
    private int _appType = 1;
    private String _packageName = "";
    private Boolean _success = false;
    private String _url = null;

    public UpdateAction() {
    }

    public void setAppType(int appType) {
        _appType = appType;
    }

    public String getPackageName() {
        return _packageName;
    }

    public void setPackageName(String packageName) {
        _packageName = packageName;
    }

    public Boolean isSuccess() {
        return _success;
    }

    public String getUpdateUrl() {
        return _url;
    }

    public void getVersion(Context context) {
        getVersionToServer(context);
        threadWait();
    }

    private void getVersionToServer(Context context) {
        final int versionCode = AppHelper.getVersionCode(context, _packageName);

        UpdaterApi.getInstance().getVersion(new UpdaterClient.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                VersionData versionData = (VersionData) data;
                _success = true;

//                // 서버에서 전달해준 버전코드가 높으면 업데이트 해준다.
//                if (versionData.versionCode > versionCode) {
//                    if (versionData.url != null) {
//                        _url = versionData.url;
//                    }
//                }

                if (versionData.url != null) {
                    _url = versionData.url;
                }

                synchronized (_mutex) {
                    _mutex.notify();
                }
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                synchronized (_mutex) {
                    _mutex.notify();
                }
            }
        }, _appType, versionCode);
    }

    private void threadWait() {
        synchronized (_mutex) {
            try {
                _mutex.wait(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
