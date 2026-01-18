package com.sec.android.app.sbrowser.pattern.naver;

import android.webkit.WebView;

public class NaverPcSearchBarAction extends NaverSearchBarAction {

    private static final String TAG = NaverPcSearchBarAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zpsb";

    public NaverPcSearchBarAction(WebView webView) {
        super(webView, JS_INTERFACE_NAME);
    }

    @Override
    protected String getSearchButtonSelector() {
        return "form[name=search]";
//        return "form[id=sform]";
    }
}
