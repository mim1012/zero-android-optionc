package com.sec.android.app.sbrowser.pattern.naver.site;

import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;

public class NaverSitePageAction extends BasePatternAction {

    private static final String TAG = NaverSitePageAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "SitePageAction";

    public static final int BUTTON_MORE = 0;
    public static final int BUTTON_NEXT = 1;

    public NaverSitePageAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsQuery = new SiteJsQuery(JS_INTERFACE_NAME);
        _jsApi.register(_jsInterface);
    }

    protected InsideData getInsideData2(String url) {
        Log.d(TAG, "- 화면 안에 보이는지 검사: " + url);
        _jsInterface.resetInsideData();
        _jsApi.postQuery(((SiteJsQuery)_jsQuery).getCheckInsideQuery2(url));
//        _jsApi.postQuery(((NaverSiteRankAction.SiteRankJsQuery) _jsQuery).getRankQuery(url));
        threadWait();

        return _jsInterface.getInsideData();
    }

    public InsideData getContentUrlInsideData(String url) {
        return getInsideData2(url);
    }

    public boolean touchContentUrl(String url) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getContentUrlSelector(url);
        InsideData insideData = getInsideData2(url);
        if (insideData == null) {
            return false;
        }

        return touchTarget();
    }

    public InsideData getMoreButtonInsideData() {
        return getInsideData(getMoreButtonSelector());
    }

    public InsideData getNextButtonInsideData() {
        return getInsideData(getNextButtonSelector());
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
                Log.d(TAG, "더보기 버튼 위치 얻기");
                selector = getMoreButtonSelector();
                break;

            case BUTTON_NEXT:
            default:
                Log.d(TAG, "다음 버튼 위치 얻기");
                selector = getNextButtonSelector();
                break;
        }

        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget();
    }

    private String getContentUrlSelector(String url) {
        return "a[href*=\"" + url + "\"]";
    }

    private String getMoreButtonSelector() {
        return ".sp_nweb a.api_more";
    }

    private String getNextButtonSelector() {
        return ".btn_next";
    }

    private class SiteJsQuery extends JsQuery {

        public SiteJsQuery(String jsInterfaceName) {
            super(jsInterfaceName);
        }

        public String getCheckInsideQuery2(String url) {
            String selectors = ".total_tit a";
            String selectorsWrap = "'" + selectors + "'";

            String query = "var list = nodeList;"
                    + "var i = 0;"
                    + "for (var obj of list) {"
                    + "var href = decodeURIComponent(obj.href);"
                    + "if (href.includes('" + url + "')) {"
                    + "break;"
                    + "}"
                    + "++i"
                    + "}"
                    + "if (nodeList.length == i) {"
                    + getJsInterfaceQuery("undefinedNode", selectorsWrap)
                    + "return;"
                    + "}"
                    + "var rect = nodeList[i].getBoundingClientRect();"
                    + "var inside = 0;"
                    + "if (rect.top < " + 0 + ") { inside = -1; }"
                    + "else if (rect.bottom > window.innerHeight) { inside = 1; }"
                    + getJsInterfaceQuery("checkInside", "inside, rect.left, rect.top, rect.right, rect.bottom");

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
}
