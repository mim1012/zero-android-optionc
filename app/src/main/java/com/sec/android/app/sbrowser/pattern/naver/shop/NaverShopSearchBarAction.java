package com.sec.android.app.sbrowser.pattern.naver.shop;

import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.naver.NaverSearchBarAction;

public class NaverShopSearchBarAction extends NaverSearchBarAction {

    private static final String TAG = NaverShopSearchBarAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zssb";

    public NaverShopSearchBarAction(WebView webView) {
        super(webView, JS_INTERFACE_NAME);
    }

    @Override
    protected String getSearchButtonSelector() {
        return "#searchForm";
    }
}
