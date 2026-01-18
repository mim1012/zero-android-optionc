package com.sec.android.app.sbrowser.pattern.action;

import android.content.Context;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.AppHelper;
import com.sec.android.app.sbrowser.engine.TogetherWebDataManager;

public class ProcessAction {
    private static final String TAG = ProcessAction.class.getSimpleName();

    public static final String PACKAGE_NAME_SBROWSER = "com.sec.android.app.sbrowser";
    public static final String PACKAGE_NAME_CHROME = "com.android.chrome";
    public static final String PACKAGE_NAME_NAVER = "com.nhn.android.search";

    private final Context _context;

    public ProcessAction(Context context) {
        _context = context;
    }

    public void checkForeground(String packageName) {
        checkForeground(packageName, null);
    }

    public void checkForeground(String packageName, String url) {
        if (!AppHelper.isForegroundApp(_context, packageName)) {
            Log.d(TAG, "앱을 foreground 로 실행: " + packageName);

            // 앱 자동실행. 실행중이지 않으면 실행해준다.
            AppHelper.launchIntentForPackageUrl(_context, packageName, url);
            // 실행이 끝나면 현 상태를 점검해야한다.
        } else {
            Log.d(TAG, "앱이 실행중..");
        }
    }

    public void checkForegroundChrome() {
        checkForeground(PACKAGE_NAME_CHROME);
    }

    public void checkForegroundChromeUrl(String url) {
        checkForeground(PACKAGE_NAME_CHROME, url);
    }

    public void checkForegroundSbrowser() {
        checkForeground(PACKAGE_NAME_SBROWSER);
    }

    protected void saveSbrowserData() {
        TogetherWebDataManager manager = TogetherWebDataManager.getInstance();

        if (manager.getMaxDataCount() > 0) {
            manager.saveCurrentWebData(_context.getApplicationContext());
//            Log.d(TAG, "saveSbrowserData save...");
//            Toast.makeText(_context, "쿠키를 저장했습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
