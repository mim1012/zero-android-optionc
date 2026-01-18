package com.sec.android.app.sbrowser.pattern.google.action;

import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;

public class GoogleHomeAction extends BasePatternAction {

    private static final String TAG = GoogleHomeAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = "HomeAction";

    public static final int BUTTON_MOBILE_WEB = 0;
    public static final int BUTTON_SEARCH = 1;

    public GoogleHomeAction(WebView webView) {
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

        if (!getCheckInside(getSearchBarSelector())) {
            return false;
        }
        return touchTarget(30);
    }

    private String getSearchBarSelector() {
        return ".gLFyf";
    }

    private String getHomeSearchButtonSelector() {
        return ".gnb-search";
    }

    private String getHomeSearchBarOnSelector() {
        return ".gnb-search.on";
    }

    private String getInSearchBarSelector() {
        return "#query.sch_inp";
    }

    private String getMobileWebButtonSelector() {
        return ".close-banner";
    }
}
