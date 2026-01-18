package com.sec.android.app.sbrowser.pattern.naver.shop_pc;

import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;

public class NaverShopPcPageAction extends BasePatternAction {

    private static final String TAG = NaverShopPcPageAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zspcp";

    public static final int BUTTON_MORE = 0;
    public static final int BUTTON_MORE2 = 3;
    public static final int BUTTON_NEXT = 1;
    public static final int BUTTON_ALL_COMPANY = 2;
    public static final int BUTTON_GO_TO_BUY = 11;
    public static final int BUTTON_GO_TO_IMAGE_BUY = 12;

    public NaverShopPcPageAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsApi.register(_jsInterface);
    }

    public boolean touchSearchBar() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = "._searchInput_search_text_fSuJ6, ._searchInput_search_input_QXUFf";
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

    public void touchPasteButton() {
        Rect rc = new Rect(140, 230, 330, 260);

        if (Build.MODEL.contains("G90")) {
            rc = new Rect(100, 310, 300, 370);
        }

        touchScreen(rc);
    }

    public void inputSearchBar(String keyword) {
        setInputValue("#input_text", keyword);
    }

    public boolean searchBarShown() {
        return getNodeCount("._autoComplete_auto_complete_LaYsq, ._autoComplete_layer_auto_3rN4S") == 1;
    }

    public InsideData getContentMidInsideData(String mid, boolean main) {
        if (!getWebViewWindowSize(true)) {
            return null;
        }

        return getInsideData(main ? getMainMidSelector(mid) : getMidSelector(mid));
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

        return touchTarget(30, 30);
    }

    public InsideData getContentMid2InsideData(String mid) {
        return getInsideData(getMid2Selector(mid));
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

    public InsideData getMoreButtonInsideData() {
        return getInsideData(getMoreButtonSelector());
    }

    public InsideData getMore2ButtonInsideData() {
        return getInsideData(getMore2ButtonSelector());
    }

    public InsideData getNextButtonInsideData() {
        return getInsideData(getNextButtonSelector());
    }

    public InsideData getAllCompanyButtonInsideData() {
        return getInsideData(getAllCompanyButtonSelector());
    }

    public InsideData getCompanyPageButtonInsideData(int page) {
        return getInsideDataShop(getCompanyPageButtonSelector(page));
    }

    public boolean touchCompanyPageButton(int page) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        Log.d(TAG, page + "페이지 버튼 위치 얻기");
        String selector = getCompanyPageButtonSelector(page);

        if (!getCheckInsideShop(selector)) {
            return false;
        }

        return touchTarget(5);
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
        return getInnerText(".pagination_btn_page___ry_S.active");
    }

    public String getCurrentCompanyPage() {
        return getInnerText(".productList_seller_wrap__FZtUS .pagination_now__Ey_sR");
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
                break;

            case BUTTON_GO_TO_BUY:
                Log.d(TAG, "사러가기 버튼 위치 얻기");
                selector = getGoToBuyButtonSelector();
//                topOffset = 30;
//                offset = 30;
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
                selector = getNextButtonSelector() + ":not(.paginator_disabled__X1zB2)";
                break;
        }

        if (!getCheckInsideTopOffset(selector, topOffset)) {
            return false;
        }

        return touchTarget(offset);
    }

    public boolean checkPageBottom() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        Log.d(TAG, "페이지 하단 검사");
        if (!getCheckInside(".pagination_pagination__fsf34")) {
            return false;
        }

        return _jsInterface.getInsideData().isInside();
    }


    private String getMainMidSelector(String mid) {
        return ".shop_product:first-child a[href*=\"mid=" + mid + "\"]";
    }

    private String getMidSelector(String mid) {
        return "a.product_link__TrAac[data-nclick*=\"i:" + mid + "\"]";
//        return "a[data-i^=\"" + mid + "\"]";
    }

    private String getMid2Selector(String mid) {
        return "a.productList_title__R1qZP[data-i*=\"" + mid + "\"]";
//        return "a[data-i^=\"" + mid + "\"]";
    }

    private String getContentUrlSelector(String url) {
        return "a[href*=\"" + url + "\"]";
    }

    private String getMoreButtonSelector() {
        return ".sp_nshop .group_more";
    }

    private String getMore2ButtonSelector() {
        return ".sp_nshop .api_more_wrap a.api_more_multi";
    }

    private String getGoToBuyButtonSelector() {
        return ".product_btn_link__XRWYu:last-child";
    }

    private String getImageViewerSelector() {
        return ".viewer_open .sp_viewer";
    }

    private String getGoToImageBuyButtonSelector() {
        return ".btn_buy";
    }

    private String getNextButtonSelector() {
        return ".pagination_next__pZuC6";
    }

    private String getCompanyPageButtonSelector(int page) {
        return ".productList_seller_wrap__FZtUS a[data-nclick*=\"page,r:" + page + "\"]";
    }

    private String getAllCompanyButtonSelector() {
        return ".main_link_more__1qw78";
    }
}
