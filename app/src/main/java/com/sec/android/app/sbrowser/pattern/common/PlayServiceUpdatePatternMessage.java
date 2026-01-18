package com.sec.android.app.sbrowser.pattern.common;

import android.content.Context;
import android.os.Build;

import com.sec.android.app.sbrowser.ActivityMCloud;
import com.sec.android.app.sbrowser.pattern.action.UpdateAction;

public class PlayServiceUpdatePatternMessage extends UpdatePatternMessage {

    private static final String TAG = PlayServiceUpdatePatternMessage.class.getSimpleName();

    public PlayServiceUpdatePatternMessage(Context context) {
        super(context);

        UpdateAction updateAction = new UpdateAction();

        if (Build.MODEL.contains("G906")) {
            updateAction.setAppType(6);
            setLogHeader("플레이서비스 G906");
        } else if (Build.MODEL.contains("G900")) {
            updateAction.setAppType(7);
            setLogHeader("플레이서비스 G900");
        } else if (Build.MODEL.contains("G930")) {
            updateAction.setAppType(8);
            setLogHeader("플레이서비스 G930");
        } else {
            updateAction.setAppType(4);
            setLogHeader("플레이서비스 S4");
        }

        updateAction.setPackageName(ActivityMCloud.PACKAGE_NAME_PLAY_SERVICE);
        setUpdateAction(updateAction);
    }
}
