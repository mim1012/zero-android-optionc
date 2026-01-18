package com.sec.android.app.sbrowser.pattern.ali.shop;

import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;

public class AliShopPageAction extends BasePatternAction {

    private static final String TAG = AliShopPageAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);

    public static final int BUTTON_MOBILE_WEB = 0;
    public static final int BUTTON_SEARCH = 1;
    public static final int BUTTON_MORE = 4;

    public AliShopPageAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsApi.register(_jsInterface);
    }

    public InsideData getContentCodeInsideData(String code) {
        return getInsideDataOffset(getContentCodeSelector(code), 170, 10);
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

        return touchTarget(30);
    }

    public InsideData getMoreButtonInsideData() {
        return getInsideDataOffset(getMoreButtonSelector(), 89, 100);
    }

    public String getCurrentPage() {
        return getInnerText(".page.selected");
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
                selector = getMoreButtonSelector();
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
        return ".fm_h:not(:has(.fm_fp))[href*=\"" + code + "\"] .fm_aj";
    }

    private String getMoreButtonSelector() {
        return ".css-t10yzt";
    }
}
