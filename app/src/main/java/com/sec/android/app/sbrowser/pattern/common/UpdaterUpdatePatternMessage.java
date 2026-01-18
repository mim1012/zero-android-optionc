package com.sec.android.app.sbrowser.pattern.common;

import android.content.Context;

import com.sec.android.app.sbrowser.ActivityMCloud;
import com.sec.android.app.sbrowser.BuildConfig;
import com.sec.android.app.sbrowser.pattern.action.UpdateAction;

public class UpdaterUpdatePatternMessage extends UpdatePatternMessage {

    private static final String TAG = UpdaterUpdatePatternMessage.class.getSimpleName();

    public UpdaterUpdatePatternMessage(Context context) {
        super(context);

        UpdateAction updateAction = new UpdateAction();
        String packageName = ActivityMCloud.PACKAGE_NAME_SYSTEM_UPDATER;

        if (BuildConfig.FLAVOR_mode.equals("child")) {
            updateAction.setAppType(11);
            setLogHeader("업데이터 제로 - 쫄병");
        } else if (BuildConfig.FLAVOR_mode.equals("rank")) {
            updateAction.setAppType(12);
            packageName = ActivityMCloud.PACKAGE_NAME_SYSTEM_UPDATER_RANK;
            setLogHeader("업데이터 제로 - 순위");
        } else {
            updateAction.setAppType(10);
            setLogHeader("업데이터 제로 - 대장");
        }

        updateAction.setPackageName(packageName);
        setUpdateAction(updateAction);
    }
}
