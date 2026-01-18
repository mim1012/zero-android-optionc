package com.sec.android.app.sbrowser.pattern.coupang.rank;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.js.JsApi;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;

public class CoupangPcRankAction extends CoupangRankAction {

    private static final String TAG = CoupangPcRankAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = TAG;

    protected int _totalPrevNodeCount = 0;
    protected int _page = 1;

    public CoupangPcRankAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsQuery = new CoupangPcRankJsQuery(JS_INTERFACE_NAME);
        _jsInterface = new CoupangPcHtmlJsInterface(_jsApi);
        _jsApi.register(_jsInterface);
    }

    public boolean hasPagination() {
        return getNodeCount(".search-pagination") > 0;
    }

    public String getCurrentPage() {
        return getInnerText(".btn-page .selected");
    }

    public boolean checkRank(String url, int page) {
        Log.d(TAG, "- 순위 검사");
        if (_page != page) {
            _page = page;
            _totalPrevNodeCount += _nodeCount;
        }
        Log.d(TAG, "- 순위 검사 total: " + _totalPrevNodeCount);

        _jsInterface.reset();
        _jsApi.postQuery(((CoupangPcRankJsQuery) _jsQuery).getRankQuery(url));
        threadWait();

        Integer rank = ((CoupangPcHtmlJsInterface) _jsInterface).getRank();

        if (rank == null) {
            return false;
        }

        if (rank > 0) {
            _rank = _totalPrevNodeCount + rank;
        }

        return true;
    }

    public boolean checkNextButton() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        return getCheckInside(getNextButtonSelector());
    }

    public boolean clickNextButton() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getNextButtonSelector();
        _jsApi.postQuery(((CoupangPcRankJsQuery) _jsQuery).clickUrl(selector));
        return true;
    }

    private String getNextButtonSelector() {
        return ".btn-next:not(.disabled)";
    }

    private class CoupangPcRankJsQuery extends JsQuery {

        public CoupangPcRankJsQuery(String jsInterfaceName) {
            super(jsInterfaceName);
        }

        public String getRankQuery(String code) {
            String selectors = ".search-product:not(.search-product__ad-badge) > a";

            String query = "var list = nodeList;"
                    + "var rank = 0;"
                    + "var i = 0;"
                    + "for (var obj of list) {"
                    + "if (obj.href.includes('" + code + "')) {"
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

    protected class CoupangPcHtmlJsInterface extends RankHtmlJsInterface {

        public CoupangPcHtmlJsInterface(JsApi jsApi) {
            super(jsApi);
        }

        @JavascriptInterface
        public void clickUrl() {
            _jsApi.callbackOnSuccess(null);
        }
    }
}
