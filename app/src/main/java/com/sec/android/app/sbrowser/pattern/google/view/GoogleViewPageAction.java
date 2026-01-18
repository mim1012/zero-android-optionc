package com.sec.android.app.sbrowser.pattern.google.view;

import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;

public class GoogleViewPageAction extends BasePatternAction {

    private static final String TAG = GoogleViewPageAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = "ViewPageAction";

    public static final int BUTTON_MOBILE_WEB = 0;
    public static final int BUTTON_SEARCH = 1;
    public static final int BUTTON_MORE = 4;

    public GoogleViewPageAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsApi.register(_jsInterface);
    }

    public InsideData getContentUrlInsideData(String url) {
        return getInsideData(getContentUrlSelector(url));
    }

    public boolean touchContentUrl(String code) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getContentUrlSelector(code);
        InsideData insideData = getInsideData(selector);
        if (insideData == null) {
            return false;
        }

        return touchTarget();
    }

    public InsideData getNextButtonInsideData() {
        return getInsideData(getNextButtonSelector());
    }

    public boolean touchButton(int type) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;

        switch (type) {
            case BUTTON_MORE:
            default:
                Log.d(TAG, "더보기 버튼 위치 얻기");
                selector = getNextButtonSelector();
                break;
        }

        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget();
    }

    private String getContentUrlSelector(String code) {
        return "a[href*=\"" + code + "\"]";
    }

    private String getNextButtonSelector() {
        return ".T7sFge.VknLRd";
    }
}
