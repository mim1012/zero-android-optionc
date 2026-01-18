package com.sec.android.app.sbrowser.pattern.naver.active;

import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;

import java.util.ArrayList;

public class NaverMailPageAction extends BasePatternAction {

    private static final String TAG = NaverMailPageAction.class.getSimpleName();
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

    private String _mailDataId = null;

    public NaverMailPageAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsQuery = new NaverShopJsQuery(JS_INTERFACE_NAME);
        _jsApi.register(_jsInterface);
    }

    public boolean pickRandomMail() {
        ArrayList<String> exceptList = new ArrayList<>();

        if (_mailDataId != null) {
            exceptList.add(_mailDataId);
        }

        return getRandomMail(exceptList);
    }

    protected boolean getRandomMail(ArrayList<String> exceptList) {
        StringBuilder selector = new StringBuilder(getMailsSelector());

        for (String except : exceptList) {
            String text = ":not(.mail-" + except +")";
            selector.append(text);
        }

        selector.append(" .mail_title_link");
        String value = getRandomValue(selector.toString(), "href");

        if (TextUtils.isEmpty(value)) {
            return false;
        } else {
            String[] urlParts = value.split("/");
            _mailDataId = urlParts[urlParts.length - 1];
        }

        return true;
    }

    public InsideData getMailInsideData() {
        return getInsideDataTopOffset(getMailSelector(_mailDataId), 120);
    }

    public boolean touchMail() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getMailSelector(_mailDataId);

        InsideData insideData = getInsideData(selector);
        if (insideData == null) {
            return false;
        }

        return touchTarget(300, 60);
    }

    private String getMailsSelector() {
        return ".mail_item";
    }
    private String getMailSelector(String mailId) {
        return ".mail_item.mail-" + mailId + "";
    }


    public boolean touchSearchBar() {
        if (!getWebViewWindowSize(true)) {
            return false;
        }

        String selector = "#sear";
        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget(30);
    }

    public boolean touchSearchBarLong() {
        InsideData insideData = getInsideData("#sear");

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
        Rect rc = new Rect(140, 230, 330, 260);

        if (Build.MODEL.contains("G90")) {
            rc = new Rect(100, 310, 300, 370);
        }

        touchScreen(rc);
    }

    public void inputSearchBar(String keyword) {
        setInputValue("#sear", keyword);
    }

    // .recentHistory_lately_wrap__umC5n 1차 검색후 검색창 다시 눌렀을때
    // ._recentHistory_lately_wrap_2k4KM.active 는 쇼핑홈 검색창 눌렀을때
    // .g_lately_wrap.active 는 5월18일 패치전
    public boolean searchBarShown() {
        return getNodeCount("._recentHistory_recent_history_voqIZ._recentHistory_active_H4j1d, ._recentHistory_recent_history_21w2i._recentHistory_active_Sagp8, .recentHistory_lately_wrap__ArBM9.active, .g_lately_wrap.active") == 1;
    }

    public boolean hasMain(String mid) {
        return getNodeCount(getCheckMainMidSelector(mid)) > 0;
    }

    // 모통: .sp_nshop .deep_noti, 쇼검: partialInfo_paricial_info__dy0Lt
    public boolean hasEmptyResult() {
        return getNodeCount(".sp_nshop .deep_noti, .partialInfo_paricial_info__dy0Lt") == 1;
    }

    public boolean hasShopEmptyResult() {
        return getNodeCount(".noResultWithBestResult_no_keyword__HyAXD") == 1;
    }

    public InsideData getContentMidInsideData(String mid, boolean main) {
        if (!getWebViewWindowSize()) {
            return null;
        }

        return getInsideDataTopOffset(main ? getMainMidSelector(mid) : getMidSelector(mid), 120);
    }

    public boolean touchContentMid(String mid, boolean main) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = main ? getMainMidSelector(mid) : getMidSelector(mid);
        InsideData insideData = getInsideData(selector);
        if (insideData == null) {
            return false;
        }

        return touchTarget(300, 60);
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
        return getInsideData(getNextButtonSelector());
    }

    public InsideData getAllCompanyButtonInsideData() {
        return getInsideDataTopOffset(getAllCompanyButtonSelector(), 30);
    }

    public InsideData getShowContentMoreButtonInsideData() {
        return getInsideDataTopOffset(getShowContentMoreButtonSelector(), 30);
    }

    public InsideData getTabDetailButtonInsideData() {
        return getInsideDataTopOffset(getTabDetailButtonSelector(), 30);
    }

    public InsideData getTabReviewButtonInsideData() {
        return getInsideDataTopOffset(getTabReviewButtonSelector(), 30);
    }

    public InsideData getTabQnaButtonInsideData() {
        return getInsideDataTopOffset(getTabQnaButtonSelector(), 30);
    }

    public InsideData getGoToBuyButtonInsideData() {
        return getInsideDataTopOffset(getGoToBuyButtonSelector(), 30);
    }

    // .paginator_active__3FOUl 20220816이전
    public String getCurrentPage() {
        return getInnerText(".paginator_active__oDL1H" +
                ", .paginator_active__3FOUl");
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
                topOffset = 30;
                offset = 30;
                break;

            case BUTTON_TAB_REVIEW:
                Log.d(TAG, "쇼핑 리뷰탭 버튼 위치 얻기");
                selector = getTabReviewButtonSelector();
                topOffset = 30;
                offset = 30;
                break;

            case BUTTON_TAB_QNA:
                Log.d(TAG, "쇼핑 QnA탭 버튼 위치 얻기");
                selector = getTabQnaButtonSelector();
                topOffset = 30;
                offset = 30;
                break;

            case BUTTON_GO_TO_BUY:
                Log.d(TAG, "사러가기 버튼 위치 얻기");
                selector = getGoToBuyButtonSelector();
                topOffset = 30;
                offset = 30;
                break;

            case BUTTON_NEXT:
            default:
                Log.d(TAG, "다음 버튼 위치 얻기");
                selector = getNextButtonSelector();
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

    // .footer_center_area__1b-Ha, .footer_center_area__AeoI7 20220816이전
    public boolean checkPageBottom() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        Log.d(TAG, "페이지 하단 검사");
        if (!getCheckInside("._footer_notice_area_LoaRN" +
                ", ._footer_center_area_3x15C," +
                " .footer_center_area__GAsXJ, .footer_center_area__1b-Ha, .footer_center_area__AeoI7")) {
            return false;
        }

        return _jsInterface.getInsideData().isInside();
    }

    public String getRandomItem(ArrayList<String> exceptList) {
        StringBuilder selector = new StringBuilder("a.product_info_main__piyRs");

        for (String except : exceptList) {
            String text = ":not(.product_info_main__piyRs[data-i=\"" + except +"\"])";
            selector.append(text);
        }

        return getRandomValue(selector.toString(), "data-i");
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
        return "._item a[href*=\"mid=" + mid + "\"], .shop_square_list .square_bx:not(._ad) a[href*=\"mid=" + mid + "\"]," +
                ".prod_info_basic .prod_explain[href*=\"mid=" + mid + "\"]";    // 일치하는 상품명이 있는 검색(BOB MS26)
    }

    private String getMainMidSelector(String mid) {
        int page = 1;
        String getPageString = getHomeShopCurrentPage();
        if (getPageString != null) {
            page = Integer.valueOf(getPageString);
        }

        return "._item:nth-child(" + page + ") a[href*=\"mid=" + mid + "\"], .shop_square_list .square_bx:not(._ad):not([style*=\"display:none\"]):not([style*=\"display: none\"]) a[href*=\"mid=" + mid + "\"] .thumb," +
                ".prod_info_basic .prod_explain[href*=\"mid=" + mid + "\"]";    // 일치하는 상품명이 있는 검색(BOB MS26)
//        return "._item:first-child a[href*=\"mid=" + mid + "\"]";
    }

    // .product_info_main__1RU2S 20220816이전
    private String getMidSelector(String mid) {
        return "a.product_info_main__piyRs[data-i=\"" + mid + "\"], a.product_info_main__1RU2S[data-i=\"" + mid + "\"]";
//        return "a[data-i^=\"" + mid + "\"]";
    }

    private String getMid2Selector(String mid) {
        return "a.productContent_link_seller__p3N_C[data-nclick*=\"i:" + mid + "\"]" +      // 전체 판매처용
                ", a.productPerMall_link_seller__K8_B_[data-nclick*=\"i:" + mid + "\"]" +   // 판매처
                ", a.buyButton_link_buy__a_Zkc[data-nclick*=\"i:" + mid + "\"]" +           // 상단 바로가기(파란색)
                ", a.officialSeller_link_seller__3_n22[data-nclick*=\"i:" + mid + "\"]" +   // 공식몰
                // 20220816이전
                ", a.productContent_link_seller__uA-1b[data-i=\"" + mid + "\"]" +   // 전체 판매처용
                ", a.productPerMall_link_seller__3GSdU[data-i=\"" + mid + "\"]" +   // 판매처
                ", a.buyButton_link_buy__3l-5b[data-i=\"" + mid + "\"]" +           // 상단 바로가기(파란색)
                ", a.officialSeller_link_seller__3TeAj[data-i=\"" + mid + "\"]";    // 공식몰
//        return "a[data-i^=\"" + mid + "\"]";
    }

    private String getContentUrlSelector(String url) {
        return "a[href*=\"" + url + "\"]";
    }

    private String getMoreButtonSelector() {
        return ".sp_nshop .api_more_wrap a.api_more";
    }

    private String getMore2ButtonSelector() {
        return ".sp_nshop .api_more_wrap:not([style*=\"display:none\"]):not([style*=\"display: none\"]) a.api_more_multi";
    }

    private String getHomeNextButtonSelector() {
        return ".cmm_pg_next._next.on, .cmm_pg_next._btnNext.on";
    }

    private String getHomePrevButtonSelector() {
        return ".cmm_pg_prev._prev.on, .cmm_pg_prev._btnPrev.on";
    }

    private String getShowContentMoreButtonSelector() {
        return ".PI5NSn_N8Y";
    }

    private String getTabDetailButtonSelector() {
        return "._3bCe-Qsh3J";
    }

    private String getTabReviewButtonSelector() {
        return "#REVIEW";
    }

    private String getTabQnaButtonSelector() {
        return "#QNA";
    }

    private String getGoToBuyButtonSelector() {
        return ".product_btn_link__XRWYu:last-child";
    }

    // .paginator_btn_next__36Dhk 20220816이전
    private String getNextButtonSelector() {
        return ".paginator_btn_next__3fcZx:not(.paginator_disabled__X1zB2)" +
                ", .paginator_btn_next__36Dhk";
    }

    // .productFilter_btn_product__1sAlV 20220816이전
    private String getOptionButtonSelector(String optionId) {
        return ".productFilter_btn_product__Smi9N[data-nclick*=\"" + optionId + "\"]" +
                ", .productFilter_btn_product__1sAlV[data-nclick*=\"" + optionId + "\"]";
    }

    // .main_link_more__1qw78 20220816이전
    private String getAllCompanyButtonSelector() {
        return ".main_link_more__qG9ns" +
                ", .main_link_more__1qw78";
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
                    + "if (obj.querySelectorAll('._product[data-nvmid=\"" + mid + "\"]').length > 0) {"
                    + "break;"
                    + "}"
                    + "++page"
                    + "}"
                    + getJsInterfaceQuery("readInnerText", "page");

            return wrapJsFunction(getValidateNodeQuery(selectors, query));
        }
    }
}
