package com.sec.android.app.sbrowser.pattern.naver.influencer;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.HtmlJsInterface;
import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.js.JsApi;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;

public class NaverInfluencerPageAction extends BasePatternAction {

    private static final String TAG = NaverInfluencerPageAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zifp";

    public static final int BUTTON_MORE = 0;
    public static final int BUTTON_NEXT = 1;

    protected int _nodeCount = 0;
    protected int _rank = -1;

    public NaverInfluencerPageAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsQuery = new RankJsQuery(JS_INTERFACE_NAME);
        _jsInterface = new RankHtmlJsInterface(_jsApi);
        _jsApi.register(_jsInterface);
    }

    public boolean checkRank(String url) {
        Log.d(TAG, "- 순위 검사");
        _jsInterface.reset();
        _jsApi.postQuery(((RankJsQuery) _jsQuery).getRankQuery(url));
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

    protected InsideData getParentInsideData(String selectors) {
        Log.d(TAG, "- 화면 안에 보이는지 검사: " + selectors);
        _jsInterface.resetInsideData();
        _jsApi.postQuery(((RankJsQuery) _jsQuery).getCheckParentInsideQuery(selectors));
        threadWait();

        return _jsInterface.getInsideData();
    }

    // 네이버가 속성을 hidden 처리하면서 화면에 안보이면 실제 좌표 값이 안나오므로 화면에 보이기 전에는 이걸 쓴다.
    public InsideData getParentContentUrlInsideData(String url) {
        return getParentInsideData(getContentUrlSelector(url));
    }

    public InsideData getContentUrlInsideData(String url) {
        return getParentInsideData(getContentUrlSelector(url));
    }

    public boolean touchContentUrl(String url) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getContentUrlSelector(url);
        InsideData insideData = getInsideData(selector);
        if (insideData == null) {
            return false;
        }

        return touchTarget();
    }

    public int getNodeCount() {
        return _nodeCount;
    }

    public boolean checkPageBottom() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        Log.d(TAG, "페이지 하단 검사");
        if (!getCheckInside("#aside")) {
            return false;
        }

        return _jsInterface.getInsideData().isInside();
    }

    public InsideData getMoreButtonInsideData() {
        return getInsideData(getMoreButtonSelector());
    }

    public String getCurrentPage() {
        return getInnerText(".pgn.now");
    }

    public boolean touchButton(int type) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;

        switch (type) {
            case BUTTON_MORE:
            default:
                Log.d(TAG, "더보기 버튼 위치 얻기");
                selector = getMoreButtonSelector();
                break;
        }

        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget();
    }

    private String getContentUrlSelector(String url) {
        return ".dsc_area a[href*=\"" + url + "\"]";
    }

    private String getMoreButtonSelector() {
        return "#influencer_wrap a.api_more";
    }


    private class RankJsQuery extends JsQuery {

        public RankJsQuery(String jsInterfaceName) {
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

        public String getCheckParentInsideQuery(String selectors) {
            String query = "var rect = nodeList[0].parentElement.parentElement.parentElement.parentElement.getBoundingClientRect();"
                    + "var inside = 0;"
                    + "if (rect.top < 0) { inside = -1; }"
                    + "else if (rect.bottom > window.innerHeight) { inside = 1; }"
                    + getJsInterfaceQuery("checkInside", "inside, rect.left, rect.top, rect.right, rect.bottom");

            return wrapJsFunction(getValidateNodeQuery(selectors, query));
        }
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
