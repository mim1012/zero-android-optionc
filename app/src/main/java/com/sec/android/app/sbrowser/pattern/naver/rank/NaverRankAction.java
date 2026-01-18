package com.sec.android.app.sbrowser.pattern.naver.rank;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.HtmlJsInterface;
import com.sec.android.app.sbrowser.pattern.js.JsApi;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;

public class NaverRankAction extends BasePatternAction {

    private static final String TAG = NaverRankAction.class.getSimpleName();

    protected int _nodeCount = 0;
    protected int _rank = -1;

    public NaverRankAction(String jsInterfaceName, WebView webView) {
        super(jsInterfaceName, webView);
    }

    public int getNodeCount() {
        return _nodeCount;
    }

    public int getRank() {
        return _rank;
    }

    public void setRank(int rank) {
        _rank = rank;
    }

    public void scrollToBottom() {
        Log.d(TAG, "- 맨 아래로 이동");
        _jsApi.postQuery(_jsQuery.scrollToBottom());
        threadWait();
    }

    public void scrollToBottom(int offset) {
        Log.d(TAG, "- 맨 아래로 이동 + " + offset);
        _jsApi.postQuery(_jsQuery.scrollToBottom(offset));
        threadWait();
    }

    protected class RankJsQuery extends JsQuery {

        public RankJsQuery(String jsInterfaceName) {
            super(jsInterfaceName);
        }

//        public String getCheckErrorQuery() {
//            String selectors = ".error_next_box";
//            String query = "var errs = nodeList;"
//                    + "var find = false;"
//                    + "for (var err of errs) {"
//                    + "if (err.id == 'pswd1Msg') continue;"
//                    + "if (!err.classList.contains('green') && err.style.display != 'none') {"
//                    + "find = true;"
//                    + "break;"
//                    + "}"
//                    + "}"
//                    + getJsInterfaceQuery("hasError", "find");
//
//            return wrapJsFunction(getValidateNodeQuery(selectors, query));
//        }
//
//        public String getRankQuery(String selectors, int page, int countPerPage) {
//            String query = "var list = nodeList;"
//                    + "var rank = -1;"
//                    + "for (var obj of list) {"
//                    + "if (obj.id.contains( == 'pswd1Msg') continue;"
//                    + "if (!err.classList.contains('green') && err.style.display != 'none') {"
//                    + "find = true;"
//                    + "break;"
//                    + "}"
//                    + "}"
//                    + getJsInterfaceQuery("hasError", "find");
//
////            String query = getJsInterfaceQuery("getInnerText",
////                    "nodeList[0].innerText");
//
//            return wrapJsFunction(getValidateNodeQuery(selectors, query));
//        }
    }

    protected class RankHtmlJsInterface extends HtmlJsInterface {

        private Integer _rank = null;

        public RankHtmlJsInterface(JsApi jsApi) {
            super(jsApi);
        }

        @Override
        public void reset() {
            super.reset();

            _rank = null;
        }

        public Integer getRank() {
            return _rank;
        }

        @JavascriptInterface
        public void getRank(int rank, int nodeCount) {
            Log.d(TAG, "rank: " + rank + ", nodes: " + nodeCount);
            _nodeCount = nodeCount;
            _rank = rank;
            _jsApi.callbackOnSuccess(_rank);
        }
    }
}
