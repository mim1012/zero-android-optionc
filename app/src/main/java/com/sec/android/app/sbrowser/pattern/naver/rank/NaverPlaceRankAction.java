package com.sec.android.app.sbrowser.pattern.naver.rank;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.js.JsApi;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;

public class NaverPlaceRankAction extends NaverRankAction {

    private static final String TAG = NaverPlaceRankAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = TAG;

    protected int _sourceType = -1;
    protected String _sourceUrl = null;

    public NaverPlaceRankAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsQuery = new PlaceRankJsQuery(JS_INTERFACE_NAME);
        _jsInterface = new PlaceHtmlJsInterface(_jsApi);
        _jsApi.register(_jsInterface);
    }

    public int getSourceType() {
        return _sourceType;
    }

    public String getSourceUrl() {
        return _sourceUrl;
    }

    public boolean checkRank(String mid1) {
        Log.d(TAG, "- 순위 검사");
        _jsInterface.reset();
        _jsApi.postQuery(((PlaceRankJsQuery) _jsQuery).getRankQuery(mid1));
        threadWait();

        Integer rank = ((RankHtmlJsInterface) _jsInterface).getRank();

        if (rank == null) {
            return false;
        }

        _rank = rank;

        return true;
    }

    public InsideData getContentCodeInsideData(String code) {
        if (!getWebViewWindowSize()) {
            return null;
        }

        String selector = ":where(" + ((PlaceRankJsQuery) _jsQuery).getSelectors() + ")[href*=\"/" + code + "\"]";
        return getInsideData(selector);
    }

    public boolean checkSource(String code) {
        Log.d(TAG, "- 소스 검사");
        _sourceType = -1;
        _sourceUrl = null;
        _jsInterface.reset();
        _jsApi.postQuery(((PlaceRankJsQuery) _jsQuery).getSourceQuery(code));
        threadWait();

        if (_sourceType == -1 || TextUtils.isEmpty(_sourceUrl)) {
            return false;
        }

        return true;
    }

    public boolean hasOpenMoreButton(String url) {
        return getNodeCount(getOpenMoreButtonSelector(url)) > 0;
    }

    public boolean checkMoreButton(String url) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        return getCheckInside(getMoreButtonSelector(url));
    }

    public void clickOpenMoreButton(String url) {
        if (!getWebViewWindowSize()) {
            return;
        }

        String selector = getOpenMoreButtonSelector(url);
        _jsApi.postQuery(((PlaceRankJsQuery) _jsQuery).clickUrl(selector));
        threadWait();
    }

    public void clickMoreButton(String url) {
        if (!getWebViewWindowSize()) {
            return;
        }

        String selector = getMoreButtonSelector(url);
        _jsApi.postQuery(((PlaceRankJsQuery) _jsQuery).clickUrl(selector));
        threadWait();
    }

    private String getNextButtonSelector() {
        return ".btn_next";
    }

    private String getOpenMoreButtonSelector(String url) {
        String query = ".YORrF";

        if (url.contains("restaurant")) {             // 식당관련
            query = ".FtXwJ";
        } else if (url.contains("accommodation")) {   // 예약관련
//            query = "a[href*=\"accommodation\"].cf8PL";
        } else if (url.contains("hospital")) {        // 병원관련
//            query = "._35OzJ";
        } else if (url.contains("hairshop")) {        // 미용실관련
//            query = "._35OzJ";
        }

        return query;
    }

    private String getMoreButtonSelector(String url) {
        String query = ".M7vfr .cf8PL:is([href*=\"/list?\"]), ._1zF_n ._35OzJ";

        if (url.contains("restaurant")) {             // 식당관련
//            query = "._35OzJ";
        } else if (url.contains("accommodation")) {   // 예약관련
            query = "a[href*=\"accommodation\"].cf8PL";
        } else if (url.contains("hospital")) {        // 병원관련
//            query = "._35OzJ";
        } else if (url.contains("hairshop")) {        // 미용실관련
//            query = "._35OzJ";
        }

        return query;
    }



    private class PlaceRankJsQuery extends JsQuery {

        public PlaceRankJsQuery(String jsInterfaceName) {
            super(jsInterfaceName);
        }

        public String getRankQuery(String code) {
            String query = "var list = nodeList;"
                    + "var rank = 0;"
                    + "var i = 0;"
                    + "for (var obj of list) {"
                    + "if (obj.href.includes(" + code + ")) {"
                    + "rank = i + 1;"
                    + "break;"
                    + "}"
                    + "++i"
                    + "}"
                    + getJsInterfaceQuery("getRank", "rank, list.length");

            return wrapJsFunction(getValidateNodeQuery(getSelectors(), query));
        }

        public String getSourceQuery(String code) {
            String[] selectorArray = getImgSelectors().split(",");
            StringBuilder selectorsWrap = new StringBuilder("[");

            for (String sel : selectorArray) {
                if (selectorsWrap.length() > 1) {
                    selectorsWrap.append(",");
                }

                selectorsWrap.append("\"");
                selectorsWrap.append(sel.trim());
                selectorsWrap.append("\"");
            }

            selectorsWrap.append("]");

            String query = "var selectorsArray = " + selectorsWrap + ";"
                    + "var nodeList = null;"
                    + "var findNode = false;"
                    + "var sType = 0;"
                    + "var sUrl = '';"
                    + "for (var sel of selectorsArray) {"
                    + "  nodeList = document.querySelectorAll(sel);"
                    + "  if (nodeList.length > 0) {"
                    + "    findNode = true;"
                    + "    break;"
                    + "  }"
                    + "  ++sType;"
                    + "}"
                    + "if (findNode) {"
                    + "  for (var obj of nodeList) {"
                    + "    var parent = obj.parentNode;"
                    + "    for (var i = 0; i < 3; ++i) {"
                    + "      if (parent.hasAttribute('href')) {"
                    + "        break;"
                    + "      }"
                    + "      parent = parent.parentNode;"
                    + "    }"
                    + "    if (parent.hasAttribute('href') && parent.href.includes(" + code + ")) {"
                    + "      sUrl = obj.src;"
                    + "      break;"
                    + "    }"
                    + "  }"
                    + "} else {"
                    + "  sType = -1;"
                    + "}"
                    + getJsInterfaceQuery("getSource", "sType, sUrl");

            Log.d(TAG, "getSourceQuery: " + query);

            return wrapJsFunction(query);
        }

        public String clickUrl(String selector) {
            String query = "var list = nodeList;"
                    + "if (list.length > 0) {"
                    + "list[0].click();"
                    + "}"
                    + getJsInterfaceQuery("clickUrl");

            return wrapJsFunction(getValidateNodeQuery(selector, query));
        }

        private String getBaseSelector(int type) {
            switch (type) {
                case 1: return ".UEzoS:not(.cZnHG)";        // restaurant a link(소래포구맛집)
                case 2: return ".Fh8nG:not(.ocbnV)";        // accommodation a link (강릉 펜션)
                case 3: return ".p0FrU:not(._0Ynn)";        // hairshop, nailshop a link(강남 미용실)
                case 4: return ".DWs4Q:not(.bjvIv)";        // hospital a link(정자동 치과)
                case 5: return ".Ki6eC:not(.xE3qV)";        // attraction a link(외도유람선)
                case 6: return "._9v52G:not(.EykuO)";       // place a link(거제유람선)
                case 7: return "";                          // place 단독

                default: return ".VLTHu:not(.hTu5x)";       // place a link(강남 문구)
            }
        }

        private String getSelectors() {
            String selectors = getBaseSelector(0) + " .ouxiq > a:first-child";     // place a link(강남 문구)
            selectors += ", " + getBaseSelector(1) + " .CHC5F > a:first-child";    // restaurant a link(소래포구맛집)
            selectors += ", " + getBaseSelector(2) + " .zzp3_ > a:first-child";    // accommodation a link (강릉 펜션)
            selectors += ", " + getBaseSelector(3) + " .QTjRp > a:first-child";    // hairshop, nailshop a link(강남 미용실)
            selectors += ", " + getBaseSelector(4) + " a.gqFka:first-child";       // hospital a link(정자동 치과)
            selectors += ", " + getBaseSelector(5) + " a.u92d5:first-child";       // attraction a link(외도유람선)
            selectors += ", " + getBaseSelector(6) + " a.OpCwG:first-child";       // place a link(거제유람선)
            selectors += ", " + getBaseSelector(7) + " .zD5Nm .LylZZ > a";         // place 단독

            return selectors;
        }

        private String getImgSelectors() {
            // 클래스에서는 first-child, first-of-type 차이가 없다. 일반 태그는 영향있음.
            String selectors = getBaseSelector(0) + " img";                         // place a link(강남 문구)
            selectors += ", " + getBaseSelector(1) + " .yLaWz:first-child img";     // restaurant a link(소래포구맛집)
            selectors += ", " + getBaseSelector(2) + " .byA2s:first-child img";     // accommodation a link (강릉 펜션)
            selectors += ", " + getBaseSelector(3) + " .iEMuT:first-child img";     // hairshop, nailshop a link(강남 미용실)
            selectors += ", " + getBaseSelector(4) + " img:first-child";            // hospital a link(정자동 치과)
            selectors += ", " + getBaseSelector(5) + " img:first-child";            // attraction a link(외도유람선)
            selectors += ", " + getBaseSelector(6) + " .uNqWn:first-child img";     // place a link(거제유람선)
            selectors += ", " + getBaseSelector(7) + " .uDR4i .CEX4u:first-child img";  // place 단독

            return selectors;
        }
    }

    protected class PlaceHtmlJsInterface extends RankHtmlJsInterface {

        public PlaceHtmlJsInterface(JsApi jsApi) {
            super(jsApi);
        }

        @JavascriptInterface
        public void clickUrl() {
            _jsApi.callbackOnSuccess(null);
        }

        @JavascriptInterface
        public void getSource(int type, String url) {
            Log.d(TAG, "type: " + type + ", url: " + url);
            _sourceType = type;
            _sourceUrl = url;
            _jsApi.callbackOnSuccess(_rank);
        }
    }
}
