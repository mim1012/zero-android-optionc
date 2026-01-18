package com.sec.android.app.sbrowser.pattern.coupang.view;

import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;

public class CoupangViewPageAction extends BasePatternAction {

    private static final String TAG = CoupangViewPageAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = "ViewPageAction";

    public static final int BUTTON_MOBILE_WEB = 0;
    public static final int BUTTON_SEARCH = 1;
    public static final int BUTTON_NEXT = 4;

    public CoupangViewPageAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsApi.register(_jsInterface);
    }

    public InsideData getContentCodeInsideData(String code) {
        return getInsideDataOffset(getContentCodeSelector(code), 180, 55);
    }

    public boolean touchContentCode(String code) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getContentCodeSelector(code);
        InsideData insideData = getInsideData(selector);
        if (insideData == null) {
            return false;
        }

        return touchTarget();
    }

    public InsideData getNextButtonInsideData() {
        return getInsideDataOffset(getNextButtonSelector(), 180, 5);
    }

    public String getCurrentPage() {
        return getInnerText(".Pagination_selected__r1eiC");
    }

    public boolean touchButton(int type) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;

        switch (type) {
            case BUTTON_NEXT:
            default:
                Log.d(TAG, "다음 버튼 위치 얻기");
                selector = getNextButtonSelector();
                break;
        }

        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget();
    }

    public void scrollToBottom() {
        Log.d(TAG, "- 맨 아래로 이동");
        _jsApi.postQuery(_jsQuery.scrollToBottom());
        threadWait();
    }

    private String getContentCodeSelector(String code) {
        return ".ProductUnit_productUnit__Qd6sv:not(:has(.AdMark_adMark__KPMsC)) > a[href*=\"" + code + "\"] .ProductUnit_productName__gre7e";
//        return ".plp-default__item:not(.search-product__cmg-badge) > a[href*=\"" + code + "\"]";
    }

    private String getNextButtonSelector() {
        return ".Pagination_nextBtn__TUY5t:not(.Pagination_disabled__EbhY6)";
//        return ".page.next:not(.dim)";
    }
}
