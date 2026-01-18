package com.sec.android.app.sbrowser.pattern.naver.rank;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.js.JsApi;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;

public class NaverInfluencerRankAction extends NaverRankAction {

    private static final String TAG = NaverInfluencerRankAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = TAG;

    public NaverInfluencerRankAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsQuery = new SiteRankJsQuery(JS_INTERFACE_NAME);
        _jsInterface = new SiteHtmlJsInterface(_jsApi);
        _jsApi.register(_jsInterface);
    }

    public boolean checkRank(String url) {
        Log.d(TAG, "- 순위 검사");
        _jsInterface.reset();
        _jsApi.postQuery(((SiteRankJsQuery) _jsQuery).getRankQuery(url));
        threadWait();

        Integer nodeCount = ((SiteHtmlJsInterface) _jsInterface).getNodeCount();
        Integer rank = ((SiteHtmlJsInterface) _jsInterface).getRank();

        if (nodeCount != null) {
            _nodeCount = nodeCount;
        }

        if (rank == null) {
            return false;
        }

        _rank = rank;

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
        _jsApi.postQuery(((SiteRankJsQuery) _jsQuery).clickUrl(selector));
        threadWait();

        return getCheckInside(selector);
    }


    private String getNextButtonSelector() {
        return ".btn_next";
    }

    private class SiteRankJsQuery extends JsQuery {

        public SiteRankJsQuery(String jsInterfaceName) {
            super(jsInterfaceName);
        }

        public String getRankQuery(String url) {
            String selectors = ".dsc_area > a.name_link";

            String query = "var list = nodeList;"
                    + "var rank = 0;"
                    + "var i = 0;"
                    + "for (var obj of list) {"
                    + "if (obj.href.includes(" + url + ")) {"
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

    protected class SiteHtmlJsInterface extends RankHtmlJsInterface {

        public SiteHtmlJsInterface(JsApi jsApi) {
            super(jsApi);
        }

        @JavascriptInterface
        public void clickUrl() {
            _jsApi.callbackOnSuccess(null);
        }
    }
}
