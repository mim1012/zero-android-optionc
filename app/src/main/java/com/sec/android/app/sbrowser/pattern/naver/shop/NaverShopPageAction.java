package com.sec.android.app.sbrowser.pattern.naver.shop;

import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NaverShopPageAction extends BasePatternAction {

    private static final String TAG = NaverShopPageAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zsp";

    public static final int BUTTON_MORE = 1;
    public static final int BUTTON_MORE2 = 2;
    public static final int BUTTON_HOME_NEXT = 3;
    public static final int BUTTON_HOME_PREV = 4;
    public static final int BUTTON_NEXT = 5;
    public static final int BUTTON_ALL_COMPANY = 6;
    public static final int BUTTON_SHOW_CONTENT_MORE = 7;
    public static final int BUTTON_TAB_DETAIL = 8;
    public static final int BUTTON_TAB_REVIEW = 9;
    public static final int BUTTON_TAB_QNA = 10;
    public static final int BUTTON_GO_TO_BUY = 11;
    public static final int BUTTON_GO_TO_IMAGE_BUY = 12;

    public NaverShopPageAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsQuery = new NaverShopJsQuery(JS_INTERFACE_NAME);
        _jsApi.register(_jsInterface);
    }

    public boolean touchHomePopupCloseButton() {
        if (!getWebViewWindowSize(true)) {
            return false;
        }

        //20240504
        String selector = "._targetBanner_close_2F4H0";
        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget(40);
    }

    public boolean touchHomeSearchButton() {
        if (!getWebViewWindowSize(true)) {
            return false;
        }

        String selector = "._combineHeader_expansion_search_inner_1VxB3";
        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget(30);
    }

    public boolean touchSearchBar() {
        if (!getWebViewWindowSize(true)) {
            return false;
        }

        String selector = "#input_text";
        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget(30);
    }

    public boolean touchSearchBarLong() {
        InsideData insideData = getInsideData("#input_text");

        if (insideData == null) {
            return false;
        }

//        Rect rc = new Rect();
//        insideData.rect.round(rc);
//        rc.top += 30;
//        rc.bottom += 30;
//        Rect rc = new Rect(125, 220, 350, 270);
        touchWebLong(30, 75);
        return true;
    }

    // 예전 OS 전용.
    public void touchPasteButton() {
        Rect rc = new Rect(230, 230, 420, 260);

        if (Build.MODEL.contains("G90")) {
            rc = new Rect(90, 310, 290, 370);
        }

        touchScreen(rc);
    }

    public void inputSearchBar(String keyword) {
        setInputValue("#input_text", keyword);
    }

    // .recentHistory_lately_wrap__umC5n 1차 검색후 검색창 다시 눌렀을때
    // ._recentHistory_lately_wrap_2k4KM.active 는 쇼핑홈 검색창 눌렀을때
    // .g_lately_wrap.active 는 5월18일 패치전
    public boolean searchBarShown() {
        return getNodeCount("._recentKeyword_mobile_recent_keyword_nBtnL, ._recentKeyword_recent_keyword_1utHF, ._recentTab_recent_tab_3aulQ._recentTab_active_2Utla, ._recentHistory_recent_history_voqIZ._recentHistory_active_H4j1d, ._recentHistory_recent_history_21w2i._recentHistory_active_Sagp8, .recentHistory_lately_wrap__ArBM9.active, .g_lately_wrap.active") == 1;
    }

    public boolean hasMain(String mid) {
        return getNodeCount(getCheckMainMidSelector(mid)) > 0;
    }

    // 모통: .sp_nshop .deep_noti, 쇼검: partialInfo_paricial_info__dy0Lt
    public boolean hasEmptyResult() {
        return getNodeCount(".sp_nshop .deep_noti, .partialInfo_paricial_info__dy0Lt") == 1;
    }

    public boolean hasShopEmptyResult() {
        return getNodeCount(".noresult_wrap") == 1;
    }

    public boolean hasNoProductResult() {
        return getNodeCount(".xMftE2Xjw_") == 1;
    }

    public InsideData getContentMidInsideData(String mid, boolean main) {
        return getContentMidInsideData(mid, main, false);
    }

    public InsideData getContentMidInsideData(String mid, boolean main, boolean onlyFirstRank) {
        if (!getWebViewWindowSize()) {
            return null;
        }

        return getInsideDataTopOffset(main ? getMainMidSelector(mid, onlyFirstRank) : getMidSelector(mid), main ? 115 : 52);
    }

    public boolean touchContentMid(String mid, boolean main) {
        return touchContentMid(mid, main, false);
    }

    public boolean touchContentMid(String mid, boolean main, boolean onlyFirstRank) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = main ? getMainMidSelector(mid, onlyFirstRank) : getMidSelector(mid);
        InsideData insideData = getInsideData(selector);
        if (insideData == null) {
            return false;
        }

        return touchTarget(360, 100);
    }

    public InsideData getContentMid2InsideData(String mid) {
        return getInsideDataTopOffset(getMid2Selector(mid), 60);
    }

    public boolean touchContentMid2(String mid) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getMid2Selector(mid);
        InsideData insideData = getInsideData(selector);
        if (insideData == null) {
            return false;
        }

        return touchTarget(60, 60, 60, 490);
    }

    public InsideData getCenterHomeLinkInsideData() {
        return getInsideDataTopOffset("._1WTXU7spDS", 20);
    }

    public boolean touchCenterHomeLink() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = "._1WTXU7spDS";
        InsideData insideData = getInsideData(selector);
        if (insideData == null) {
            return false;
        }

        return touchTarget(20);
    }

    public InsideData getMoreButtonInsideData() {
        return getInsideDataTopOffset(getMoreButtonSelector(), 30);
    }

    public InsideData getMore2ButtonInsideData() {
        return getInsideData(getMore2ButtonSelector());
    }

    public InsideData getHomeNextButtonInsideData() {
        return getInsideDataTopOffset(getHomeNextButtonSelector(), 30);
    }

    public InsideData getHomePrevButtonInsideData() {
        return getInsideDataTopOffset(getHomePrevButtonSelector(), 30);
    }

    public InsideData getNextButtonInsideData() {
        return getInsideDataTopOffset(getNextButtonSelector(), 50);
    }

    public InsideData getAllCompanyButtonInsideData() {
        return getInsideDataTopOffset(getAllCompanyButtonSelector(), 30);
    }

    public InsideData getShowContentMoreButtonInsideData() {
        return getInsideDataOffset(getShowContentMoreButtonSelector(), 30, 70);
    }

    public InsideData getTabDetailButtonInsideData() {
        return getInsideDataOffset(getTabDetailButtonSelector(), 5, 70);
    }

    public InsideData getTabReviewButtonInsideData() {
        return getInsideDataOffset(getTabReviewButtonSelector(), 5, 70);
    }

    public InsideData getTabQnaButtonInsideData() {
        return getInsideDataOffset(getTabQnaButtonSelector(), 5, 70);
    }

    public InsideData getGoToBuyButtonInsideData() {
        return getInsideDataTopOffset(getGoToBuyButtonSelector(), 30);
    }

    public InsideData getImageViewerInsideData() {
        return getInsideDataTopOffset(getImageViewerSelector(), 30);
    }

    public InsideData getGoToImageBuyButtonInsideData() {
        return getInsideDataTopOffset(getGoToImageBuyButtonSelector(), 30);
    }

    public String getCurrentPage() {
        return getInnerText(".paginator_active__3C_54");
    }

    public String getHomeShopCurrentPage() {
        return getInnerText(".cmm_npgs_now._current, .cmm_npgs_now._currentCount");
    }

    public String getHomeShopMidPage(String mid) {
        Log.d(TAG, "- getHomeShopMidPage: " + mid);
        _jsInterface.resetInnerText();
        _jsApi.postQuery(((NaverShopJsQuery) _jsQuery).getFlickPageIndexQuery(mid));
        threadWait();

        return _jsInterface.getInnerText();
    }

    public boolean touchButton(int type) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;
        int offset = 15;
        int topOffset = 0;

        switch (type) {
            case BUTTON_MORE2:
                Log.d(TAG, "아래로 더보기 버튼 위치 얻기");
                selector = getMore2ButtonSelector();
                break;

            case BUTTON_MORE:
                Log.d(TAG, "쇼핑 더보기 버튼 위치 얻기");
                selector = getMoreButtonSelector();
                break;

            case BUTTON_ALL_COMPANY:
                Log.d(TAG, "전체 판매처 버튼 위치 얻기");
                selector = getAllCompanyButtonSelector();
                topOffset = 30;
                break;

            case BUTTON_HOME_NEXT:
                Log.d(TAG, "홈 쇼핑 좌버튼 버튼 위치 얻기");
                selector = getHomeNextButtonSelector();
                topOffset = 30;
                offset = 30;
                break;

            case BUTTON_HOME_PREV:
                Log.d(TAG, "홈 쇼핑 우버튼 버튼 위치 얻기");
                selector = getHomePrevButtonSelector();
                topOffset = 30;
                offset = 30;
                break;

            case BUTTON_SHOW_CONTENT_MORE:
                Log.d(TAG, "쇼핑 펼쳐보기 버튼 위치 얻기");
                selector = getShowContentMoreButtonSelector();
                topOffset = 30;
                offset = 30;
                break;

            case BUTTON_TAB_DETAIL:
                Log.d(TAG, "쇼핑 상세정보탭 버튼 위치 얻기");
                selector = getTabDetailButtonSelector();
                topOffset = 5;
                offset = 30;
                break;

            case BUTTON_TAB_REVIEW:
                Log.d(TAG, "쇼핑 리뷰탭 버튼 위치 얻기");
                selector = getTabReviewButtonSelector();
                topOffset = 5;
                offset = 30;
                break;

            case BUTTON_TAB_QNA:
                Log.d(TAG, "쇼핑 QnA탭 버튼 위치 얻기");
                selector = getTabQnaButtonSelector();
                topOffset = 5;
                offset = 30;
                break;

            case BUTTON_GO_TO_BUY:
                Log.d(TAG, "사러가기 버튼 위치 얻기");
                selector = getGoToBuyButtonSelector();
                topOffset = 30;
                offset = 30;
                break;

            case BUTTON_GO_TO_IMAGE_BUY:
                Log.d(TAG, "이미지 사러가기 버튼 위치 얻기");
                selector = getGoToImageBuyButtonSelector();
                topOffset = 30;
                offset = 30;
                break;

            case BUTTON_NEXT:
            default:
                Log.d(TAG, "다음 버튼 위치 얻기");
                selector = getNextButtonSelector();
                topOffset = 60;
                offset = 40;
                break;
        }

        if (!getCheckInsideTopOffset(selector, topOffset)) {
            return false;
        }

        return touchTarget(offset);
    }

    // 일단 이렇게 처리.. 만약 문제가 되면 좌 스크롤을 넣어야한다.
    public boolean clickOptionButton(String optionId) {
        String selector = getOptionButtonSelector(optionId);
        _jsApi.postQuery(_jsQuery.clickUrl(selector));
        return true;
    }

    public boolean checkPageBottom() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        Log.d(TAG, "페이지 하단 검사");
        if (!getCheckInside("._footer_notice_area_LoaRN" +
                ", ._footer_center_area_3x15C" +
                ", .footer_center_area__GAsXJ")) {
            return false;
        }

        return _jsInterface.getInsideData().isInside();
    }

    public String getRandomItem(ArrayList<String> exceptList) {
        StringBuilder selector = new StringBuilder("a.product_btn_link__AhZaM");

        for (String except : exceptList) {
            String text = ":not(.product_btn_link__AhZaM[data-shp-contents-id=\"" + except +"\"])";
            selector.append(text);
        }

        return getRandomValue(selector.toString(), "data-shp-contents-id");
    }

    public String getMainRandomItem(ArrayList<String> exceptList) {
        StringBuilder selector1 = new StringBuilder("._product");
        StringBuilder selector2 = new StringBuilder(".square_bx");

        for (String except : exceptList) {
            String text1 = ":not([data-nvmid=\"" + except +"\"])";
            String text2 = ":not([data-nvmid=\"" + except +"\"])";
            selector1.append(text1);
            selector2.append(text2);
        }

        selector1.append(", ").append(selector2);
        return getRandomValue(selector1.toString(), "data-nvmid");
    }

    public String getItemUrl(String mid) {
        return getValue(getCheckMainMidSelector(mid), "href");
    }

    public String getMid2Url(String mid) {
        return getValue(getMid2Selector(mid), "href");
    }

    public String getNClickUrl(String mid, String urlString) {
        String nClick = getValue(getMidSelector(mid), "data-nclick");

        if (TextUtils.isEmpty(nClick)) {
            return null;
        }

        Map<String, String> kvMap = new HashMap<>();

//        'N=a:lst*N.item,i:12223579755,r:11,g:undefined'
        nClick = nClick.replace("N=", "");
        String[] clickData = nClick.split(",");

        for (String click : clickData) {
            String[] kv = click.trim().split(":");
            String value = "";

            if (kv.length > 1) {
                value = kv[1];
            }

            kvMap.put(kv[0], value);
        }

        long time = new Date().getTime();

        try {
            urlString = URLEncoder.encode(urlString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //https://volts.shopping.naver.com/click?a=lst*N.item&r=11&i=12223579755&ssc=m.shopping.all&ts=1708790754287&location=https%3A%2F%2Fmsearch.shopping.naver.com%2Fsearch%2Fall%3Fquery%3D%25EC%259E%2590%25EC%25A0%2584%25EA%25B1%25B0%2B%25EA%25B1%25B0%25EC%25B9%2598%25EB%258C%2580%26bt%3D-1%26frm%3DMOSCPRO
        return "https://volts.shopping.naver.com/click?a=" + kvMap.get("a") + "&r=" + kvMap.get("r") + "&i=" + mid + "&ssc=m.shopping.all&ts=" + time + "&location=" + urlString;
    }

    public void changeTargetContentMid2(String mid) {
        String selector = getMid2Selector(mid);
        String value = getValue(selector, "target");

        if (!TextUtils.isEmpty(value)) {
            if (value.equals("_blank")) {
                Log.d(TAG, "mid2 target=\"_blank\" 제거");
                setValue(selector, "target", "");
            }
        }
    }

    public void changeTargetAllCompanyButton() {
        String selector = getAllCompanyButtonSelector();
        String value = getValue(selector, "target");

        if (!TextUtils.isEmpty(value)) {
            if (value.equals("_blank")) {
                Log.d(TAG, "AllCompanyButton target=\"_blank\" 제거");
                setValue(selector, "target", "");
            }
        }
    }


    private String getCheckMainMidSelector(String mid) {
        return "._item a[href*=\"mid=" + mid + "\"]" +
                ", .shop_tile_list_wrap .tile_list:not(._ad) a[href*=\"mid=" + mid + "\"]" +
                ", .prod_info_basic .prod_explain[href*=\"mid=" + mid + "\"]";    // 일치하는 상품명이 있는 검색(AF17B7538WZRS)
    }

    private String getMainMidSelector(String mid, boolean onlyFirstRank) {
        int page = 1;
        String getPageString = getHomeShopCurrentPage();
        if (getPageString != null) {
            page = Integer.valueOf(getPageString);
        }

        String condition = " li:not(:has(.adBadge-mobile-module__ad_badge___pbjVN))";
//        if (onlyFirstRank) {
//            condition = " li:first-child ";
//        }

//        .flickingShoppingProductList-mobile-module__flicking_shopping_product_list___vGi1Y > .flicking-viewport > .flicking-camera > .flickingShoppingProductList-mobile-module__list_item___LVnnm:nth-child(1) a[href*=`"mid=" . work.midexternal . "`"], .catalogSummary-mobile-module__catalog_summary___gQSyq a[href*=`"mid=" . work.midexternal . "`"]
//        .flickingShoppingProductList-mobile-module__flicking_shopping_product_list___vGi1Y .flickingShoppingProductList-mobile-module__list_item___LVnnm:nth-child(1) a[href*=`"mid=" . work.midexternal . "`"], .catalogSummary-mobile-module__catalog_summary___gQSyq a[href*=`"mid=" . work.midexternal . "`"]
        // 첫줄 일반목록 방식의 페이징(보조배터리)
        // 줄째줄 카드모양 방식(남자 크로스백)
        // 일치하는 상품명이 있는 검색(AF17B7538WZRS)
        return ".flickingShoppingProductList-mobile-module__flicking_shopping_product_list___vGi1Y .flickingShoppingProductList-mobile-module__list_item___LVnnm:nth-child(" + page + ")" + condition + " a[href*=\"mid=" + mid + "\"]" +
                ", .trendProductCard-mobile-module__trend_product_card___mE0Ea:not(:has(.adBadge-mobile-module__ad_badge___pbjVN)) a[href*=\"mid=" + mid + "\"]" +
                ", .catalogSummary-mobile-module__catalog_summary___gQSyq[href*=\"mid=" + mid + "\"]";
//        return "._item:first-child a[href*=\"mid=" + mid + "\"]";
    }

    private String getMidSelector(String mid) {
        return "a.product_btn_link__AhZaM[data-shp-contents-id=\"" + mid + "\"]";
//        return "a[data-shp-contents-id^=\"" + mid + "\"]";
    }

    private String getMid2Selector(String mid) {
        return "a.productContent_link_seller__gBFQj[data-shp-contents-id=\"" + mid + "\"]" +       // 전체 판매처용
                ", a.productPerMall_link_item___Etii[data-shp-contents-id=\"" + mid + "\"]" +        // 판매처
                ", a.buyButton_link_buy__QV2Np[data-shp-contents-id=\"" + mid + "\"]" +               // 상단 바로가기(파란색,최저가)
                "";
//        return "a[data-shp-contents-id^=\"" + mid + "\"]";
    }

    private String getContentUrlSelector(String url) {
        return "a[href*=\"" + url + "\"]";
    }

    private String getMoreButtonSelector() {
        return ":where(.guide-mobile-module__page___LndOY, .trend-mobile-module__page___AqE5Z, .togetherRecommend-mobile-module__together_recommend___vmeZy) .storeMoreLink-mobile-module__link___x3mTJ";
    }

    private String getMore2ButtonSelector() {
        return ".sp_nshop .api_more_wrap:not([style*=\"display:none\"]):not([style*=\"display: none\"]) a.api_more_multi";
    }

    private String getHomeNextButtonSelector() {
        return ".arrowPagination-mobile-module__button___r1ffb:not(.arrowPagination-mobile-module__disabled___Aygvh, .arrowPagination-mobile-module__previous___p9yAY)";
    }

    private String getHomePrevButtonSelector() {
        return ".arrowPagination-mobile-module__button___r1ffb.arrowPagination-mobile-module__previous___p9yAY:not(.arrowPagination-mobile-module__disabled___Aygvh)";
    }

    private String getShowContentMoreButtonSelector() {
        return "._1xuYbO3k64";
    }

    private String getTabDetailButtonSelector() {
        return "._1ntZIn0qSQ[data-shp-contents-dtl*=\"상세정보\"]";
    }

    private String getTabReviewButtonSelector() {
        return "._1ntZIn0qSQ[data-shp-contents-dtl*=\"리뷰\"]";
    }

    private String getTabQnaButtonSelector() {
        return "._1ntZIn0qSQ[data-shp-contents-dtl*=\"Q&A\"]";
    }

    private String getGoToBuyButtonSelector() {
        return ".product_btn_link__XRWYu:last-child";
    }

    private String getImageViewerSelector() {
        return ".inline_viewer";
    }

    private String getGoToImageBuyButtonSelector() {
        return ".btn_buy";
    }

    private String getNextButtonSelector() {
        return ".paginator_btn_next__BE1_y:not(.paginator_disabled__XpDer)";
    }

    private String getOptionButtonSelector(String optionId) {
        return ".productFilter_btn_option__I5uqe[data-shp-contents-id*=\"" + optionId + "\"]";
    }

    private String getAllCompanyButtonSelector() {
        return ".main_link_more__mD_PP";
    }

    private class NaverShopJsQuery extends JsQuery {

        public NaverShopJsQuery(String jsInterfaceName) {
            super(jsInterfaceName);
        }

        public String getFlickPageIndexQuery(String mid) {
            String selectors = "._item";

            String query = "var list = nodeList;"
                    + "var rank = 0;"
                    + "var page = 1;"
                    + "for (var obj of list) {"
                    + "if (obj.querySelectorAll('._product:not(._ad)[data-nvmid=\"" + mid + "\"]').length > 0) {"
                    + "break;"
                    + "}"
                    + "++page"
                    + "}"
                    + getJsInterfaceQuery("readInnerText", "page");

            return wrapJsFunction(getValidateNodeQuery(selectors, query));
        }
    }
}
