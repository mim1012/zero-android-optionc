package com.sec.android.app.sbrowser.pattern.naver.rank;

import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.js.JsQuery;

public class NaverViewRankAction extends NaverRankAction {

    private static final String TAG = NaverViewRankAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = TAG;

    public NaverViewRankAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsQuery = new NaverViewRankAction.ViewRankJsQuery(JS_INTERFACE_NAME);
        _jsInterface = new RankHtmlJsInterface(_jsApi);
        _jsApi.register(_jsInterface);
    }

    public boolean checkRank(String url) {
        Log.d(TAG, "- 순위 검사");
        _jsInterface.reset();
        _jsApi.postQuery(((NaverViewRankAction.ViewRankJsQuery) _jsQuery).getRankQuery(url));
        threadWait();

        Integer nodeCount = ((RankHtmlJsInterface) _jsInterface).getNodeCount();
        Integer rank = ((RankHtmlJsInterface) _jsInterface).getRank();

        if (nodeCount != null) {
            _nodeCount = nodeCount;
        }

        if (rank == null) {
            return false;
        }

        _rank = rank;

        return true;
    }

    private class ViewRankJsQuery extends JsQuery {

        public ViewRankJsQuery(String jsInterfaceName) {
            super(jsInterfaceName);
        }

        public String getRankQuery(String url) {
//            String selectors = "._svp_item:not(._pwr_content) .total_tit";
            String selectors = "._slog_visible .t0ZSeRhLDI88qOA3Nvk6";
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
    }
}
