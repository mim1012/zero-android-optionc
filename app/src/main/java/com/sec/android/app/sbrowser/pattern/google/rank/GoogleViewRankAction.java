package com.sec.android.app.sbrowser.pattern.google.rank;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.js.JsApi;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;

public class GoogleViewRankAction extends GoogleRankAction {

    private static final String TAG = GoogleViewRankAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = TAG;

    public GoogleViewRankAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsQuery = new GoogleViewRankJsQuery(JS_INTERFACE_NAME);
        _jsInterface = new GoogleViewHtmlJsInterface(_jsApi);
        _jsApi.register(_jsInterface);
    }

    public boolean checkRank(String url) {
        Log.d(TAG, "- 순위 검사");
        _jsInterface.reset();
        _jsApi.postQuery(((GoogleViewRankJsQuery) _jsQuery).getRankQuery(url));
        threadWait();

        Integer rank = ((GoogleViewHtmlJsInterface) _jsInterface).getRank();

        if (rank == null) {
            return false;
        }

        _rank = rank;

        return true;
    }

    public boolean checkMoreButton() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        return getCheckInside(getMoreButtonSelector());
    }

    public boolean clickMoreButton() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getMoreButtonSelector();
        _jsApi.postQuery(((GoogleViewRankJsQuery) _jsQuery).clickUrl(selector));

        return getCheckInside(selector);
    }

    private String getMoreButtonSelector() {
        return ".T7sFge.VknLRd";
    }

    private class GoogleViewRankJsQuery extends JsQuery {

        public GoogleViewRankJsQuery(String jsInterfaceName) {
            super(jsInterfaceName);
        }

        public String getRankQuery(String url) {
            String selectors = ".C8nzq.BmP5tf:not(.d5oMvf)";

            String query = "var list = nodeList;"
                    + "var rank = 0;"
                    + "var i = 0;"
                    + "for (var obj of list) {"
                    + "if (obj.href.includes('" + url + "')) {"
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
    }

    protected class GoogleViewHtmlJsInterface extends RankHtmlJsInterface {

        public GoogleViewHtmlJsInterface(JsApi jsApi) {
            super(jsApi);
        }

        @JavascriptInterface
        public void clickUrl() {
            _jsApi.callbackOnSuccess(null);
        }
    }
}
