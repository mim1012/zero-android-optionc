package com.sec.android.app.sbrowser.pattern.naver;

import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class NaverHomeAction extends BasePatternAction {

    private static final String TAG = NaverHomeAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zh";

    public static final int BUTTON_POPUP_OK = 0;
    public static final int BUTTON_POPUP2_OK = 1;
    public static final int BUTTON_MAIL = 2;
    public static final int BUTTON_NEWS_PAN = 3;
    public static final int BUTTON_NEWS_PAN_REFRESH = 4;
    public static final int BUTTON_ECONOMY_PAN = 5;
    public static final int BUTTON_SPORTS_PAN = 6;
    public static final int BUTTON_TOP_DOTS = 7;
    public static final int BUTTON_TOP_SHOPPING = 8;
    public static final int BUTTON_TOP_VIEW = 9;

    private int _panMode = BUTTON_NEWS_PAN;
    private String _dataId = null;
    private String _viewDataId = null;
    private String _siteDataId = null;

    public int getPanMode() {
        return _panMode;
    }

    public NaverHomeAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsApi.register(_jsInterface);
    }

    public boolean touchSearchBar(boolean home) {
        if (!getWebViewWindowSize(true)) {
            return false;
        }
        String selector = home ? getHomeSearchBarSelector() : getSearchPageSearchBarSelector();
        if (!getCheckInside(selector)) {
            return false;
        }
        return touchTarget(90, 40);
    }

    public boolean touchSearchBarLong(boolean home) {
        String selector = home ? getInSearchBarSelector() : getSearchPageSearchBarSelector();
        InsideData insideData = getInsideData(selector);

        if (insideData == null) {
            return false;
        }

        touchWebLong(30, 75);
        return true;
    }

    public void touchEmptyArea() {
        Rect rc = new Rect(5, 700, 35, 750);
        touchScreen(rc);
    }

    // 예전 OS 전용.
    public void touchPasteButton() {
        Rect rc = new Rect(270, 240, 460, 270);

        if (Build.MODEL.contains("G90")) {
            rc = new Rect(90, 330, 290, 390);
        }

        touchScreen(rc);
    }

    // 원래는 최근검색어 창이 뜨면 사용하려고 했으나 사용하지 않음.
    public void touchPasteButtonNoHistory() {
        Rect rc = new Rect(270, 240, 460, 270);

        if (Build.MODEL.contains("G90")) {
            rc = new Rect(100, 250, 300, 310);
        }

        touchScreen(rc);
    }

    public void inputSearchBar(boolean home, String keyword) {
        setInputValue(home ? getInSearchBarSelector() : getSearchPageSearchBarSelector(), keyword);
    }

    public boolean touchPcSearchBar(boolean home) {
        if (!getWebViewWindowSize()) {
            return false;
        }
        String selector = home ? "#query" : ".box_window";
        if (!getCheckInside(selector)) {
            return false;
        }
        return touchTarget(30);
    }

    public boolean touchRandomPan() {
        _panMode = (int) MathHelper.randomRange(BUTTON_NEWS_PAN, BUTTON_SPORTS_PAN);
        _panMode = BUTTON_NEWS_PAN;   // test.
        String logText = "BUTTON_NEWS_PAN";

        switch (_panMode) {
            case BUTTON_ECONOMY_PAN:
                logText = "BUTTON_ECONOMY_PAN";
                break;

            case BUTTON_SPORTS_PAN:
                logText = "BUTTON_SPORTS_PAN";
                break;

            case BUTTON_NEWS_PAN:
            default:
                break;
        }

        Log.d(TAG, "# 랜덤 판 버튼 터치: " + logText);

        return touchButton(_panMode);
    }

    public boolean pickRandomItem() {
        boolean result = false;
        ArrayList<String> exceptList = new ArrayList<>();

        if (_dataId != null) {
            exceptList.add(_dataId);
        }

        switch (_panMode) {
            case BUTTON_ECONOMY_PAN:
                result = getEconomyPanRandomItem(exceptList);
                break;

            case BUTTON_SPORTS_PAN:
                break;

            case BUTTON_NEWS_PAN:
            default:
                result = getNewsPanRandomItem(exceptList);
                break;
        }

        return result;
    }

    public boolean pickRandomViewItem() {
        ArrayList<String> exceptList = new ArrayList<>();
        if (_viewDataId != null) {
            exceptList.add(_viewDataId);
        }
        return getViewRandomItem(exceptList);
    }

    public boolean pickRandomSiteItem() {
        ArrayList<String> exceptList = new ArrayList<>();
        if (_siteDataId != null) {
            exceptList.add(_siteDataId);
        }
        return getSiteRandomItem(exceptList);
    }


    protected boolean getNewsPanRandomItem(ArrayList<String> exceptList) {
        StringBuilder selector = new StringBuilder(getNewsPanContentsSelector());

        for (String except : exceptList) {
            String text = ":not(.cjs_news_a[data-aid=\"" + except +"\"])";
            selector.append(text);
        }

        String value = getRandomValue(selector.toString(), "data-aid");

        if (TextUtils.isEmpty(value)) {
            return false;
        } else {
            _dataId = value;
        }

        return true;
    }

    protected boolean getEconomyPanRandomItem(ArrayList<String> exceptList) {
        StringBuilder selector = new StringBuilder(getEconomyPanContentsSelector());

        for (String except : exceptList) {
            String text = ":not(.ce_news_a[href=\"" + except +"\"])";
            selector.append(text);
        }

        String value = getRandomValue(selector.toString(), "href");

        if (TextUtils.isEmpty(value)) {
            return false;
        } else {
            _dataId = value;
        }

        return true;
    }

    public boolean hasView() {
        return getNodeCount(getViewContentsSelector()) > 0;
    }

    protected boolean getViewRandomItem(ArrayList<String> exceptList) {
        StringBuilder selector = new StringBuilder(getViewContentsSelector());

        for (String except : exceptList) {
            String text = ":not(.total_dsc[data-cr-gdid=\"" + except +"\"])";
            selector.append(text);
        }

        String value = getRandomValue(selector.toString(), "data-cr-gdid");

        if (TextUtils.isEmpty(value)) {
            return false;
        } else {
            _viewDataId = value;
        }

        return true;
    }

    protected boolean getSiteRandomItem(ArrayList<String> exceptList) {
        StringBuilder selector = new StringBuilder(getSiteContentsSelector());

        for (String except : exceptList) {
            String text = ":not(.total_dsc_wrap a:last-child[href=\"" + except +"\"])";
            selector.append(text);
        }

        String value = getRandomValue(selector.toString(), "href");

        if (TextUtils.isEmpty(value)) {
            return false;
        } else {
            _siteDataId = value;
        }

        return true;
    }

    public InsideData getViewContentInsideData() {
        return getInsideDataTopOffset(getViewContentSelector(_viewDataId), 120);
    }

    public InsideData getSiteContentInsideData() {
        return getInsideDataTopOffset(getSiteContentSelector(_siteDataId), 120);
    }

    public InsideData getPanContentInsideData() {
        String selector;

        switch (_panMode) {
            case BUTTON_ECONOMY_PAN:
                Log.d(TAG, "경제판 컨텐츠 위치 얻기");
                selector = getEconomyPanContentSelector(_dataId);
                break;

            case BUTTON_SPORTS_PAN:
                Log.d(TAG, "스포츠판 컨텐츠 위치 얻기");
                selector = getSportsPanButtonSelector();
                break;

            case BUTTON_NEWS_PAN:
            default:
                Log.d(TAG, "뉴스판 컨텐츠 위치 얻기");
                selector = getNewsPanContentSelector(_dataId);
                break;
        }

        return getInsideDataTopOffset(selector, 120);
    }

    public boolean touchPanContent() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;

        switch (_panMode) {
            case BUTTON_ECONOMY_PAN:
                Log.d(TAG, "경제판 컨텐츠 위치 얻기");
                selector = getEconomyPanContentSelector(_dataId);
                break;

            case BUTTON_SPORTS_PAN:
                Log.d(TAG, "스포츠판 컨텐츠 위치 얻기");
                selector = getNewsPanContentSelector(_dataId);
                break;

            case BUTTON_NEWS_PAN:
            default:
                Log.d(TAG, "뉴스판 컨텐츠 위치 얻기");
                selector = getNewsPanContentSelector(_dataId);
                break;
        }

        InsideData insideData = getInsideData(selector);
        if (insideData == null) {
            return false;
        }

        return touchTarget(300, 60);
    }

    public boolean touchViewContent() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getViewContentSelector(_viewDataId);

        InsideData insideData = getInsideData(selector);
        if (insideData == null) {
            return false;
        }

        return touchTarget(300, 60);
    }

    public boolean touchSiteContent() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = getSiteContentSelector(_siteDataId);

        InsideData insideData = getInsideData(selector);
        if (insideData == null) {
            return false;
        }

        return touchTarget(300, 60);
    }

    public InsideData getButtonInsideData(int type) {
        return getButtonInsideData(type, 120);
    }

    public InsideData getTopButtonInsideData(int type) {
        return getButtonInsideData(type, 10);
    }

    public InsideData getButtonInsideData(int type, int topOffset) {
        String selector;

        switch (type) {
            case BUTTON_MAIL:
                Log.d(TAG, "메일 버튼 위치 얻기");
                selector = getMailButtonSelector();
                break;

            case BUTTON_POPUP2_OK:
                Log.d(TAG, "신규 팝업 확인 버튼 위치 얻기");
                selector = getNewPopup2OkButtonSelector();
                break;

            case BUTTON_NEWS_PAN:
                Log.d(TAG, "뉴스판 버튼 위치 얻기");
                selector = getNewsPanButtonSelector();
                break;

            case BUTTON_NEWS_PAN_REFRESH:
                Log.d(TAG, "뉴스판 새로보기 버튼 위치 얻기");
                selector = getNewsPanRefreshButtonSelector();
                break;

            case BUTTON_ECONOMY_PAN:
                Log.d(TAG, "경제판 버튼 위치 얻기");
                selector = getEconomyPanButtonSelector();
                break;

            case BUTTON_SPORTS_PAN:
                Log.d(TAG, "스포츠판 버튼 위치 얻기");
                selector = getSportsPanButtonSelector();
                break;

            case BUTTON_TOP_DOTS:
                Log.d(TAG, "상단 점세게 버튼 위치 얻기");
                selector = getTopDotsButtonSelector();
                break;

            case BUTTON_TOP_SHOPPING:
                Log.d(TAG, "상단 쇼핑탭 버튼 위치 얻기");
                selector = getTopShoppingButtonSelector();
                break;

            case BUTTON_TOP_VIEW:
                Log.d(TAG, "상단 뷰탭 버튼 위치 얻기");
                selector = getTopViewButtonSelector();
                break;

            case BUTTON_POPUP_OK:
            default:
                Log.d(TAG, "신규 팝업 확인 버튼 위치 얻기");
                selector = getNewPopupOkButtonSelector();
                break;
        }

        return getInsideDataTopOffset(selector, topOffset);
    }

    public boolean touchButton(int type) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;

        switch (type) {
            case BUTTON_MAIL:
                Log.d(TAG, "메일 버튼 위치 얻기");
                selector = getMailButtonSelector();
                break;

            case BUTTON_POPUP2_OK:
                Log.d(TAG, "신규 팝업 확인 버튼 위치 얻기");
                selector = getNewPopup2OkButtonSelector();
                break;

            case BUTTON_NEWS_PAN:
                Log.d(TAG, "뉴스판 버튼 위치 얻기");
                selector = getNewsPanButtonSelector();
                break;

            case BUTTON_NEWS_PAN_REFRESH:
                Log.d(TAG, "뉴스판 새로보기 버튼 위치 얻기");
                selector = getNewsPanRefreshButtonSelector();
                break;

            case BUTTON_ECONOMY_PAN:
                Log.d(TAG, "경제판 버튼 위치 얻기");
                selector = getEconomyPanButtonSelector();
                break;

            case BUTTON_SPORTS_PAN:
                Log.d(TAG, "스포츠판 버튼 위치 얻기");
                selector = getSportsPanButtonSelector();
                break;

            case BUTTON_TOP_DOTS:
                Log.d(TAG, "상단 점세게 버튼 위치 얻기");
                selector = getTopDotsButtonSelector();
                break;

            case BUTTON_TOP_SHOPPING:
                Log.d(TAG, "상단 쇼핑탭 버튼 위치 얻기");
                selector = getTopShoppingButtonSelector();
                break;

            case BUTTON_TOP_VIEW:
                Log.d(TAG, "상단 뷰탭 버튼 위치 얻기");
                selector = getTopViewButtonSelector();
                break;

            case BUTTON_POPUP_OK:
            default:
                Log.d(TAG, "신규 팝업 확인 버튼 위치 얻기");
                selector = getNewPopupOkButtonSelector();
                break;
        }

        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget(40);
    }

    public InsideData getTopDotsButtonInsideData() {
        return getInsideData(getTopDotsButtonSelector());
    }

    public InsideData getTopShoppingButtonInsideData() {
        return getInsideData(getTopShoppingButtonSelector());
    }

    public InsideData getTopViewButtonInsideData() {
        return getInsideData(getTopViewButtonSelector());
    }


    private String getHomeSearchBarSelector() {
        return "#MM_SEARCH_FAKE.sch_input";
    }

    private String getSearchPageSearchBarSelector() {
        return "#query.sch_input, #nx_query.search_input";
    }

    private String getInSearchBarSelector() {
        return "#query.sch_input, #nx_query.search_input";
    }

    private String getNewPopupOkButtonSelector() {
        return ".lst_btn_close.MM_CLOSE";
    }

    private String getNewPopup2OkButtonSelector() {
        return ".lsc_btn_close";
    }

    // 홈 판버튼들
    private String getNewsPanButtonSelector() {
        return ".shs_link.ITEM[data-code=\"menu.NEWS\"]";
    }

    private String getEconomyPanButtonSelector() {
        return ".shs_link.ITEM[data-code=\"menu.DATA\"]";
    }

    private String getSportsPanButtonSelector() {
        return ".shs_link.ITEM[data-code=\"menu.SPORTS\"]";
    }

    private String getMailButtonSelector() {
        return ".shs_link.ITEM[data-code*=\"svc.mail\"]";
    }

    private String getCafeButtonSelector() {
        return ".shs_link.ITEM[data-code*=\"svc.cafe\"]";
    }

    private String getBlogButtonSelector() {
        return ".shs_link.ITEM[data-code*=\"svc.blog\"]";
    }

    // 뉴스판
    private String getNewsPanContentsSelector() {
        return ".comp_journal_subscribe:not([style*=\"display:none\"]):not([style*=\"display: none\"]) .cjs_news_a";
    }

    private String getNewsPanContentSelector(String dataId) {
        return ".comp_journal_subscribe:not([style*=\"display:none\"]):not([style*=\"display: none\"]) .cjs_news_a[data-aid=\"" + dataId + "\"]";
    }

    private String getNewsPanRefreshButtonSelector() {
        return "._MM_COMMON_SELECT .comp_journal_subscribe:not([style*=\"display:none\"]):not([style*=\"display: none\"]) .cjs_btn_refresh";
    }

    // 경제판
    private String getEconomyPanContentsSelector() {
        return ".ce_news_a";
    }

    private String getEconomyPanContentSelector(String url) {
        String encodedUrl = url;

        try {
            encodedUrl = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ".ce_news_a[href=\"" + encodedUrl + "\"]";
    }


    private String getTopDotsButtonSelector() {
        return ".sch_tab._sch_tab:not(.open) .btn_tab_more";
    }

    private String getTopShoppingButtonSelector() {
        return ".sch_tab .lst_sch .bx a[href*=\"shopping.naver.com/search\"]";
    }

    private String getTopViewButtonSelector() {
        return ".sch_tab .lst_sch .bx a[href*=\"m_view\"]";
    }


    private String getViewContentsSelector() {
        return ".total_dsc";
    }

    private String getViewContentSelector(String dataId) {
        return ".total_dsc[data-cr-gdid=\"" + dataId + "\"]";
    }

    private String getSiteContentsSelector() {
        return ".total_dsc_wrap a:last-child";
    }

    private String getSiteContentSelector(String url) {
        String encodedUrl = url;

        try {
            encodedUrl = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ".total_dsc_wrap a:last-child[href*=\"" + encodedUrl + "\"]";
    }

}
