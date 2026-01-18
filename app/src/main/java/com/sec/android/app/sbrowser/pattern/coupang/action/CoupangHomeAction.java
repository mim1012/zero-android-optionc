package com.sec.android.app.sbrowser.pattern.coupang.action;

import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;

public class CoupangHomeAction extends BasePatternAction {

    private static final String TAG = CoupangHomeAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = "HomeAction";

    public static final int BUTTON_MOBILE_WEB = 0;
    public static final int BUTTON_SEARCH = 1;
    public static final int BUTTON_PC_SEARCH = 2;
    public static final int BUTTON_HOME_POPUP_CLOSE = 3;
    public static final int BUTTON_BOTTOM_APP_BANNER_CLOSE = 4;
    public static final int BUTTON_BOTTOM_APP_DL_WIDGET_CLOSE = 5;

    public CoupangHomeAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsApi.register(_jsInterface);
    }

    public boolean touchButton(int type) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;
        int offset = 15;

        switch (type) {
            case BUTTON_SEARCH:
                Log.d(TAG, "검색 버튼 위치 얻기");
                selector = getHomeSearchButtonSelector();
                offset = 100;
                break;

            case BUTTON_PC_SEARCH:
                Log.d(TAG, "PC 검색 버튼 위치 얻기");
                selector = getPcHomeSearchButtonSelector();
                break;

            case BUTTON_HOME_POPUP_CLOSE:
                Log.d(TAG, "팝업 닫기 버튼 위치 얻기");
                selector = ".bottom-sheet-nudge-container__header-close";
                break;

            case BUTTON_BOTTOM_APP_BANNER_CLOSE:
                Log.d(TAG, "하단 앱배너 닫기 버튼 위치 얻기");
                selector = ".close-banner";
                offset = 30;
                break;

            case BUTTON_BOTTOM_APP_DL_WIDGET_CLOSE:
                Log.d(TAG, "하단 앱다운로드 위젯 닫기 버튼 위치 얻기");
                selector = "#app-dl-nudge-close-15m-btn";
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

        return touchTarget(offset);
    }

    private boolean checkInside(String selectors) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        if (!getCheckInside(selectors)) {
            return false;
        }

        return true;
    }

    public boolean checkFullBanner() {
        return checkInside("#fullBanner");
    }

    public boolean checkHomePopup() {
        return checkInside(".bottom-sheet-nudge-container");
    }

    public boolean checkBottomAppBanner() {
        return checkInside("#BottomAppBanner");
    }

    public boolean checkBottomAppDlWidget() {
        return checkInside("#app-dl-nudge-widget");
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

    public void inputSearchBar(String keyword) {
        setInputValue(getInSearchBarSelector(), keyword);
    }

    public boolean checkPcSearchBar() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        if (!getCheckInside(getPcHomeSearchBarOnSelector())) {
            return false;
        }

        return true;
    }

    public boolean touchPcSearchBar() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        if (!getCheckInside(getPcHomeSearchBarSelector())) {
            return false;
        }
        return touchTarget(30);
    }

    public void inputPcSearchBar(String keyword) {
        setInputValue(getPcHomeSearchBarSelector(), keyword);
    }

    private String getHomeSearchButtonSelector() {
        return ".fw-block .coupang-search";
    }

    private String getHomeSearchBarOnSelector() {
        return ".fw-block .coupang-search:not(.ad-keyword)";
    }

    private String getPcHomeSearchButtonSelector() {
        return "#headerSearchBtn";
    }

    private String getPcHomeSearchBarSelector() {
        return "#headerSearchKeyword";
    }

    private String getPcHomeSearchBarOnSelector() {
        return ".coupang-search.is-speech:not(.ad-keyword)";
//        return "#headerPopupWords:not([style*=\"none\"])";
    }

    private String getInSearchBarSelector() {
        return ".fw-block .headerSearchKeyword";
    }

    private String getMobileWebButtonSelector() {
        return ".close-banner-icon-button";
    }
}
