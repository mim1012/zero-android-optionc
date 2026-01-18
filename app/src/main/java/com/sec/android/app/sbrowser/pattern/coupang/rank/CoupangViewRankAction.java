package com.sec.android.app.sbrowser.pattern.coupang.rank;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.js.JsApi;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;

public class CoupangViewRankAction extends CoupangRankAction {

    private static final String TAG = CoupangViewRankAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = TAG;

    protected int _totalPrevNodeCount = 0;
    protected int _page = 1;

    public CoupangViewRankAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsQuery = new CoupangViewRankJsQuery(JS_INTERFACE_NAME);
        _jsInterface = new CoupangViewHtmlJsInterface(_jsApi);
        _jsApi.register(_jsInterface);
    }

    public String getCurrentPage() {
        return getInnerText(".page.selected");
    }

    public boolean checkRank(String url, int page) {
        Log.d(TAG, "- 순위 검사: " + url);
        if (_page != page) {
            _page = page;
            _totalPrevNodeCount += _nodeCount;
        }
        Log.d(TAG, "- 순위 검사 total: " + _totalPrevNodeCount);

        _jsInterface.reset();
        _jsApi.postQuery(((CoupangViewRankJsQuery) _jsQuery).getRankQuery(url));
        threadWait();

        Integer rank = ((CoupangViewHtmlJsInterface) _jsInterface).getRank();

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
        _jsApi.postQuery(((CoupangViewRankJsQuery) _jsQuery).clickUrl(selector));

        return getCheckInside(selector);
    }

    private String getNextButtonSelector() {
        return ".page.next:not(.dim) a";
    }

    private class CoupangViewRankJsQuery extends JsQuery {

        public CoupangViewRankJsQuery(String jsInterfaceName) {
            super(jsInterfaceName);
        }

        public String getRankQuery(String code) {
            String selectors = ".plp-default__item:not(.search-product__cmg-badge) > a";

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

    protected class CoupangViewHtmlJsInterface extends RankHtmlJsInterface {

        public CoupangViewHtmlJsInterface(JsApi jsApi) {
            super(jsApi);
        }

        @JavascriptInterface
        public void clickUrl() {
            _jsApi.callbackOnSuccess(null);
        }
    }
}
