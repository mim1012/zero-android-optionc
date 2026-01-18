package com.sec.android.app.sbrowser.pattern.coupang.pc;

import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;

public class CoupangPcPageAction extends BasePatternAction {

    private static final String TAG = CoupangPcPageAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = "PcPageAction";

    public static final int BUTTON_MOBILE_WEB = 0;
    public static final int BUTTON_SEARCH = 1;
    public static final int BUTTON_NEXT = 4;
    public static final int BUTTON_CONTENT_MORE = 5;

    public CoupangPcPageAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsApi.register(_jsInterface);
    }

    public InsideData getContentCodeInsideData(String code) {
        return getInsideData(getContentCodeSelector(code));
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
        return getInsideData(getNextButtonSelector());
    }

    public InsideData getContentMoreButtonInsideData() {
        return getInsideData(getContentMoreButtonSelector());
    }

    public String getCurrentPage() {
        return getInnerText(".btn-page .selected");
    }

    public boolean touchButton(int type) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;

        switch (type) {
            case BUTTON_CONTENT_MORE:
                Log.d(TAG, "정보 더보기 버튼 위치 얻기");
                selector = getContentMoreButtonSelector();
                break;

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
        return ".search-product:not(.search-product__ad-badge)[id*=\"" + code + "\"] > a";
    }

    private String getNextButtonSelector() {
        return ".btn-next:not(.disabled)";
    }

    private String getContentMoreButtonSelector() {
        return ".product-detail-seemore-btn";
    }
}
