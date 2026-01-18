package com.sec.android.app.sbrowser.pattern.naver.place;

import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;

public class NaverPlacePageAction extends BasePatternAction {

    private static final String TAG = NaverPlacePageAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zplp";

    public static final int BUTTON_OPEN_MORE = 0;
    public static final int BUTTON_MORE = 1;

    public static final int BUTTON_SHOW_LIST = 1;

    public NaverPlacePageAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsApi.register(_jsInterface);
    }

    public boolean hasAccommodationList() {
        String innerText = getInnerText(".place_section_header");
        if (innerText != null) {
            return innerText.startsWith("펜션 실시간 예약");
        }

        return false;
//        return getInnerText(".place_section_header").startsWith("펜션 실시간 예약");
//        return getNodeCount(".") > 0;
    }

    public InsideData getContentCodeInsideData(String code) {
        return getInsideData(hasAccommodationList() ? getAccommodationContentCodeSelector(code) : getContentCodeSelector(code));
    }

    public boolean hasOpenMoreButton(String url) {
        return getNodeCount(getOpenMoreButtonSelector(url)) > 0;
    }

    public boolean checkMorePageLoaded() {
        return getNodeCount("#_list_scroll_container") > 0;
    }

    public boolean checkShowListButtonClick() {
        // only
        // place a link
        return getNodeCount(".VLTHu:not(.hTu5x) .icT4K") > 0;
    }

    public boolean touchContentCode(String code) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = hasAccommodationList() ? getAccommodationContentCodeSelector(code) : getContentCodeSelector(code);
        InsideData insideData = getInsideData(selector);
        if (insideData == null) {
            return false;
        }

        return touchTarget(300, 60);
    }

    public int getContentCount() {
        return getNodeCount(getContentSelector());
    }

    public InsideData getOpenMoreButtonInsideData(String url) {
        return getInsideData(getOpenMoreButtonSelector(url));
    }

    public InsideData getMoreButtonInsideData(String url) {
        return getInsideData(getMoreButtonSelector(url));
    }

    public boolean touchButton(int type) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;

        switch (type) {
            case BUTTON_SHOW_LIST:
            default:
                Log.d(TAG, "목록보기 버튼 위치 얻기");
                selector = getShowListButtonSelector();
                break;
        }

        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget();
    }

    public boolean touchOpenMoreButton(String url) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;
        Log.d(TAG, "플레이스 펼쳐서 더보기 버튼 위치 얻기");
        selector = getOpenMoreButtonSelector(url);

        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget();
    }

    public boolean touchMoreButton(String url) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;
        Log.d(TAG, "플레이스 더보기 버튼 위치 얻기");
        selector = getMoreButtonSelector(url);

        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget();
    }

    private String getContentCodeSelector(String code) {
        return "a[href*=\"" + code + "?entry=\"]" +
                ", .LylZZ a[href*=\"" + code + "\"]";   //홍은동 카페 무지개
    }

    private String getAccommodationContentCodeSelector(String code) {
        return "#place-main-section-root a[href*=\"" + code + "?entry=\"]";
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
        String query = ".M7vfr .cf8PL, ._1zF_n ._35OzJ";

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

    private String getContentSelector() {
        String selectors = ".VLTHu:not(.hTu5x) .ouxiq";     // place a link(강남 문구)
        selectors += ", .UEzoS:not(.cZnHG) .CHC5F";         // restaurant a link(소래포구맛집)
        selectors += ", .Fh8nG:not(.ocbnV) .zzp3_";         // accommodation a link(강릉 펜션)
        selectors += ", .p0FrU:not(._0Ynn) .QTjRp";         // hairshop, nailshop a link(강남 미용실)
        selectors += ", .DWs4Q:not(.bjvIv) .gqFka";         // hospital a link(정자동 치과)
        selectors += ", .Ki6eC:not(.xE3qV) .u92d5";         // attraction a link(외도유람선)
        selectors += ", ._9v52G:not(.EykuO) .OpCwG";        // place a link(거제유람선)

        return selectors;
    }

    private String getShowListButtonSelector() {
        return ".AtjOO, .nI0KX";
    }
}
