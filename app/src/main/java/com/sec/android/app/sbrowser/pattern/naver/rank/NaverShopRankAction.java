package com.sec.android.app.sbrowser.pattern.naver.rank;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.js.JsApi;

public class NaverShopRankAction extends NaverRankAction {

    private static final String TAG = NaverShopRankAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = TAG;

    protected int _totalPrevNodeCount = 0;
    protected int _productRank = -1;
    protected int _page = 1;
    protected String _seq = null;
    protected String _href = null;

    public NaverShopRankAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsQuery = new ShopRankJsQuery(JS_INTERFACE_NAME);
        _jsInterface = new ShopRankHtmlJsInterface(_jsApi);
        _jsApi.register(_jsInterface);
    }

    public int getProductRank() {
        return _productRank;
    }

    public void setProductRank(int rank) {
        _productRank = rank;
    }

    public String getSeq() {
        return _seq;
    }

    public String getHref() {
        return _href;
    }

    public boolean checkErrorPage() {
        return getNodeCount(".style_content_error__PqzHg") > 0;
    }

    public boolean isNoResult() {
        return getNodeCount(".noResultWithBestResult_no_keyword__HyAXD") > 0;
    }

    public boolean checkRank(String mid1, int page) {
        Log.d(TAG, String.format("- 단일상품 순위 검사 %d페이지: %s", page, mid1));
        if (_page != page) {
            _page = page;
            _totalPrevNodeCount += _nodeCount;
        }
        Log.d(TAG, "- 단일상품 순위 검사 total: " + _totalPrevNodeCount);

        _jsInterface.reset();
        _jsApi.postQuery(((ShopRankJsQuery) _jsQuery).getRankQuery(mid1));
        threadWait();

        Integer rank = ((RankHtmlJsInterface) _jsInterface).getRank();

        if (rank == null) {
            return false;
        }

        if (rank > 0) {
            _rank = _totalPrevNodeCount + rank;
        }

        return true;
    }

    public int getContentCount() {
        return getNodeCount(getContentSelector());
    }

    public void clickContent(String mid1) {
        if (!getWebViewWindowSize()) {
            return;
        }

        String selector = "a[data-shp-contents-id=\"" + mid1 + "\"]";
        _jsApi.postQuery(((ShopRankJsQuery) _jsQuery).clickUrl(selector));
        threadWait();
    }

    public void clickContent2(String mid2) {
        if (!getWebViewWindowSize()) {
            return;
        }

        String selector = "a[data-nclick*=\"i:" + mid2 + "\"]";
        _jsApi.postQuery(((ShopRankJsQuery) _jsQuery).clickUrl(selector));
        threadWait();
    }

    public boolean checkNextButton() {
        return getNodeCount(".paginator_btn_next__BE1_y:not(.paginator_disabled__XpDer)") > 0;
    }

    public void clickNextButton() {
        String selector = ".paginator_btn_next__BE1_y:not(.paginator_disabled__XpDer)";
        _jsApi.postQuery(((ShopRankJsQuery) _jsQuery).clickUrl(selector));
        threadWait();
    }

    public boolean hasPageBottom() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        Log.d(TAG, "페이지 하단 검사");
        return getNodeCount("._footer_notice_area_LoaRN" +
                ", ._footer_center_area_3x15C, .footer_center_area__GAsXJ, .footer_center_area__1b-Ha, .footer_center_area__AeoI7") > 0;
    }

    public boolean checkPageBottom() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        Log.d(TAG, "페이지 하단 검사");
        if (!getCheckInside("._footer_notice_area_LoaRN" +
                ", ._footer_center_area_3x15C, .footer_center_area__GAsXJ, .footer_center_area__1b-Ha, .footer_center_area__AeoI7")) {
            return false;
        }

        return _jsInterface.getInsideData().isInside();
    }

    // 250403 변경 - .topInfo_top_info__VGumu
    // 221201 추가 - .topInfo_top_info__7VTun
    public boolean checkCatalogPage() {
        Log.d(TAG, "카탈로그 페이지 검사");
        return getNodeCount(".topInfo_top_info__VGumu, .header_header_catalog__6kXvk") > 0;
    }

    public boolean getPurchaseConditionSequence() {
        Log.d(TAG, "URL 의 PurchaseConditionSequence 검사");
        _jsApi.postQuery(((ShopRankJsQuery) _jsQuery).getPurchaseConditionSequence());
        threadWait();

        String seq = ((ShopRankHtmlJsInterface) _jsInterface).getSeq();

        if (seq == null) {
            return false;
        }

        _seq = seq;
        return true;
    }

    public boolean checkShopRank(String mid2) {
        Log.d(TAG, "- 상품페이지 업체 순위 검사");
        _jsInterface.reset();
        _jsApi.postQuery(((ShopRankJsQuery) _jsQuery).getShopRankQuery(mid2));
        threadWait();

        Integer rank = ((RankHtmlJsInterface) _jsInterface).getRank();

        if (rank == null) {
            return false;
        }

        _productRank = rank;
        return true;
    }

    public String getHrefString(String mid1) {
        _jsApi.postQuery(((ShopRankJsQuery) _jsQuery).getHrefQuery(mid1));
        threadWait();

        return _href;
    }

    public String getMallId(String mid1) {
        return getValue(getContentSelector() + "[data-shp-contents-id=\"" + mid1 + "\"]", "data-ms");
    }

    public String getProductName(String mid1) {
//        String text = getCurrentTextOnly(getContentSelector() + "[data-shp-contents-id=\"" + mid1 + "\"] .product_info_tit__UOCqq");
        String text = getCurrentTextOnlyFromParent(getContentSelector() + "[data-shp-contents-id=\"" + mid1 + "\"]", ".product_info_tit__UOCqq");
        if (text != null) {
            String[] splatted = text.split("\\n");
            text = splatted[0];
        }
        return text;
    }

    public String getSellerName(String mid1) {
//        return getInnerText(getContentSelector() + "[data-shp-contents-id=\"" + mid1 + "\"] .product_mall__v9966");
        return getInnerTextFromParent(getContentSelector() + "[data-shp-contents-id=\"" + mid1 + "\"]", ".product_mall__gUvbk");
    }

    public String getShopSellerName(String mid2) {
        return getCurrentTextOnly("a." + getShopContentSelector() + "[data-shp-contents-id*=\"" + mid2 + "\"] .productContent_seller__02zS5");
    }

    public String getShopProductName(String mid2) {
        return getCurrentTextOnly("a." + getShopContentSelector() + "[data-shp-contents-id*=\"" + mid2 + "\"] .productContent_info_title__G8QdV");
    }

    public String getSmartStoreProductName() {
        String text = getCurrentTextOnly("._3Q2GZqdH9Z");
        if (text != null) {
            String[] splatted = text.split("\\n");
            text = splatted[0];
        }
        return text;
    }

    public String getSmartStoreName() {
        String text = getCurrentTextOnly("._2Ksdt_w0Bq");
        if (text != null) {
            String[] splatted = text.split("\\n");
            text = splatted[0];
        }
        return text;
    }

    private String getContentSelector() {
        return ".product_btn_link__AhZaM.linkAnchor";
    }

    private String getShopContentSelector() {
        return ".productContent_link_seller__gBFQj.linkAnchor";
    }

    protected class ShopRankHtmlJsInterface extends RankHtmlJsInterface {

        private String _seq = null;

        public ShopRankHtmlJsInterface(JsApi jsApi) {
            super(jsApi);
        }

        @Override
        public void reset() {
            super.reset();

            _seq = null;
        }

        public String getSeq() {
            return _seq;
        }

        @JavascriptInterface
        public void getHref(String href) {
            Log.d(TAG, "href: " + href);
            _href = href;
            _jsApi.callbackOnSuccess(_href);
        }

        @JavascriptInterface
        public void getPurchaseConditionSequence(String seq) {
            Log.d(TAG, "seq: " + seq);

            if (!seq.equals("undefined")) {
                _seq = seq;
            }

            _jsApi.callbackOnSuccess(seq);
        }
    }

    private class ShopRankJsQuery extends RankJsQuery {

        public ShopRankJsQuery(String jsInterfaceName) {
            super(jsInterfaceName);
        }

        public String getRankQuery(String code) {
            String selectors = getContentSelector();
            String query = "var list = nodeList;"
                    + "var rank = 0;"
                    + "var i = 0;"
                    + "for (var obj of list) {"
                    + "if (obj.getAttribute('data-shp-contents-id') == \"" + code + "\") {"
//                    + "if (obj.getAttribute('data-nclick').includes(\"i:" + code + "\")) {"
                    + "rank = i + 1;"
                    + "break;"
                    + "}"
                    + "++i"
                    + "}"
                    + getJsInterfaceQuery("getRank", "rank, list.length");

            return wrapJsFunction(getValidateNodeQuery(selectors, query));
        }

        public String getHrefQuery(String code) {
            String selectors = getContentSelector();
            String query = "var list = nodeList;"
                    + "var href = '';"
                    + "for (var obj of list) {"
                    + "if (obj.getAttribute('data-shp-contents-id') == \"" + code + "\") {"
//                    + "if (obj.getAttribute('data-nclick').includes(\"i:" + code + "\")) {"
                    + "href = obj.getAttribute('href');"
                    + "break;"
                    + "}"
                    + "}"
                    + getJsInterfaceQuery("getHref", "href");

            return wrapJsFunction(getValidateNodeQuery(selectors, query));
        }

        public String getShopRankQuery(String code) {
            String selectors = getShopContentSelector();
            String query = "var list = nodeList;"
                    + "var rank = 0;"
                    + "var i = 0;"
                    + "for (var obj of list) {"
                    + "if (obj.getAttribute('data-shp-contents-id') == \"" + code + "\") {"
//                    + "if (obj.getAttribute('data-nclick').includes(\"i:" + code + "\")) {"
                    + "rank = i + 1;"
                    + "break;"
                    + "}"
                    + "++i"
                    + "}"
                    + getJsInterfaceQuery("getRank", "rank, list.length");

            return wrapJsFunction(getValidateNodeQuery(selectors, query));
        }

        public String getPurchaseConditionSequence() {
            String query = "var params = {}; window.location.search.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(str, key, value) { params[key] = value; });"
                    + getJsInterfaceQuery("getPurchaseConditionSequence", "params.purchaseConditionSequence");

            return wrapJsFunction(query);
        }
    }
}
