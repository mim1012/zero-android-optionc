package com.sec.android.app.sbrowser.pattern.common;

import android.content.Context;
import android.os.Build;

import com.sec.android.app.sbrowser.ActivityMCloud;
import com.sec.android.app.sbrowser.pattern.action.UpdateAction;

public class CoupangUpdatePatternMessage extends UpdatePatternMessage {

    private static final String TAG = CoupangUpdatePatternMessage.class.getSimpleName();

    public CoupangUpdatePatternMessage(Context context) {
        super(context);

        if (Build.MODEL.contains("G930")) {
            UpdateAction updateAction = new UpdateAction();
            updateAction.setAppType(14);
            setLogHeader("쿠팡 G930");
            updateAction.setPackageName(ActivityMCloud.PACKAGE_NAME_COUPANG);
            setUpdateAction(updateAction);
        }
    }
}
