package com.sec.android.app.sbrowser.pattern.naver;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.HtmlJsInterface;
import com.sec.android.app.sbrowser.pattern.js.JsApi;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;

public class NaverSearchBarAction extends BasePatternAction {

    private static final String TAG = NaverSearchBarAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zsb";

    public NaverSearchBarAction(WebView webView, String jsInterfaceName) {
        super(jsInterfaceName == null ? JS_INTERFACE_NAME : jsInterfaceName, webView);

        if (jsInterfaceName == null) {
            jsInterfaceName = JS_INTERFACE_NAME;
        }

        _jsQuery = new SearchBarJsQuery(jsInterfaceName);
        _jsInterface = new SearchBarHtmlJsInterface(_jsApi);
        _jsApi.register(_jsInterface);
    }

    public boolean touchHomeButton() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getHomeButtonSelector();
        if (!getCheckInside(selector)) {
            return false;
        }
        return touchTarget(30);
    }

    public boolean checkSearchButton() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        return getCheckInside(getSearchButtonSelector());
    }

    public boolean clickSearchButton() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getSearchButtonSelector();
        Log.d(TAG, "- 검색폼: " + selector);
        _jsApi.postQuery(((SearchBarJsQuery) _jsQuery).clickUrl(selector));
        threadWait();

        return getCheckInside(selector);
    }

    public boolean submitSearchButton() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getSearchButtonSelector();
        Log.d(TAG, "- 검색폼: " + selector);
        _jsApi.postQuery(((SearchBarJsQuery) _jsQuery).submit(selector));
        threadWait();

        return getCheckInside(selector);
    }

    public boolean touchSearchButton() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = "._searchInput_button_search_pA3ap";
        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget(30);
    }

    protected String getHomeButtonSelector() {
        return ".sch_logo_naver .sch_ico_mask";
    }

    protected String getSearchButtonSelector() {
        return "form[name=search]";
    }

    protected class SearchBarJsQuery extends JsQuery {

        public SearchBarJsQuery(String jsInterfaceName) {
            super(jsInterfaceName);
        }

        public String getRankQuery(String url) {
            String selectors = ".btn_save";

            String query = "var list = nodeList;"
                    + "var rank = 0;"
                    + "var i = 0;"
                    + "for (var obj of list) {"
                    + "if (obj.dataset.url.includes('" + url + "')) {"
                    + "rank = i + 1;"
                    + "break;"
                    + "}"
                    + "++i"
                    + "}"
                    + getJsInterfaceQuery("getRank", "rank, list.length");

            return wrapJsFunction(getValidateNodeQuery(selectors, query));
        }

        public String clickUrl(String selector) {
            String query = "var list = nodeList;"
                    + "if (list.length > 0) {"
                    + "list[0].click();"
                    + "}"
                    + getJsInterfaceQuery("clickUrl");

            return wrapJsFunction(getValidateNodeQuery(selector, query));
        }

        public String submit(String selector) {
            String query = "var list = nodeList;"
                    + "if (list.length > 0) {"
                    + "list[0].submit();"
                    + "}"
                    + getJsInterfaceQuery("submit");

            return wrapJsFunction(getValidateNodeQuery(selector, query));
        }
    }

    protected class SearchBarHtmlJsInterface extends HtmlJsInterface {

        public SearchBarHtmlJsInterface(JsApi jsApi) {
            super(jsApi);
        }

        @JavascriptInterface
        public void clickUrl() {
            _jsApi.callbackOnSuccess(null);
        }

        @JavascriptInterface
        public void submit() {
            _jsApi.callbackOnSuccess(null);
        }
    }
}
