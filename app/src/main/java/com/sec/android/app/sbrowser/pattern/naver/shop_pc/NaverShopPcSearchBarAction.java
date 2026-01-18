package com.sec.android.app.sbrowser.pattern.naver.shop_pc;

import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.naver.NaverSearchBarAction;

public class NaverShopPcSearchBarAction extends NaverSearchBarAction {

    private static final String TAG = NaverShopPcSearchBarAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = TAG;

    public NaverShopPcSearchBarAction(WebView webView) {
        super(webView, JS_INTERFACE_NAME);
    }

    @Override
    protected String getSearchButtonSelector() {
        return "#_verticalGnbModule ._searchInput_btn_search_JMLYD";
    }
}
