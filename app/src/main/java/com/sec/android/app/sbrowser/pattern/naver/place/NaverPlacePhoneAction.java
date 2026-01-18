package com.sec.android.app.sbrowser.pattern.naver.place;

import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;

public class NaverPlacePhoneAction extends BasePatternAction {

    private static final String TAG = NaverPlacePhoneAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = TAG;

    public NaverPlacePhoneAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsApi.register(_jsInterface);
    }

    public boolean touchPhoneButton() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        Log.d(TAG, "전화 버튼 위치 얻기");
        if (!getCheckInside("a[href*=\"tel:\"]")) {
            return false;
        }

        return touchTarget(3);
    }
}
