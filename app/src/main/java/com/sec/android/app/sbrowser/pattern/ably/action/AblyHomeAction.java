package com.sec.android.app.sbrowser.pattern.ably.action;

import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;

public class AblyHomeAction extends BasePatternAction {

    private static final String TAG = AblyHomeAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);

    public static final int BUTTON_MOBILE_WEB = 0;
    public static final int BUTTON_SEARCH = 1;

    public AblyHomeAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsApi.register(_jsInterface);
    }

    public boolean touchButton(int type) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;

        switch (type) {
            case BUTTON_SEARCH:
                Log.d(TAG, "검색 버튼 위치 얻기");
                selector = getHomeSearchButtonSelector();
                break;

            case BUTTON_MOBILE_WEB:
            default:
                Log.d(TAG, "모바일웹으로 보기 버튼 위치 얻기");
                selector = getMobileWebButtonSelector();
                break;
        }

        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget();
    }

    public boolean checkFullBanner() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        if (!getCheckInside(".css-d9sqsi")) {
            return false;
        }

        return true;
    }

    public boolean checkSearchBar() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        if (!getCheckInside(getHomeSearchBarOnSelector())) {
            return false;
        }

        return true;
    }

    public boolean touchSearchBar() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        if (!getCheckInside(getInSearchBarSelector())) {
            return false;
        }
        return touchTarget(30);
    }

    public boolean touchSearchBarLong() {
        InsideData insideData = getInsideData(getInSearchBarSelector());

        if (insideData == null) {
            return false;
        }

//        Rect rc = new Rect();
//        insideData.rect.round(rc);
//        rc.top += 30;
//        rc.bottom += 30;
//        Rect rc = new Rect(125, 220, 350, 270);
        touchWebLong(30, 75);
        return true;
    }

    public void touchPasteButton() {
        Rect rc = new Rect(270, 280, 470, 320);

        if (Build.MODEL.contains("G90")) {
            rc = new Rect(90, 310, 290, 370);
        }

        touchScreen(rc);
    }

    public void inputSearchBar(String keyword) {
        setInputValue(getInSearchBarSelector(), keyword);
    }

    private String getHomeSearchButtonSelector() {
        return ".css-1wv1qut";
    }

    private String getHomeSearchBarOnSelector() {
        return getInSearchBarSelector();
    }

    private String getInSearchBarSelector() {
        return ".css-1ag9g3n .css-1pneado";
    }

    private String getMobileWebButtonSelector() {
        return ".css-1ehncww";
    }
}
