package com.sec.android.app.sbrowser.pattern.naver.shop;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.sec.android.app.sbrowser.BuildConfig;
import com.sec.android.app.sbrowser.engine.Config;
import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpEngine;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;
import com.sec.android.app.sbrowser.models.KeywordItem;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.pattern.RandomSwipePatternAction;
import com.sec.android.app.sbrowser.pattern.action.ResultAction;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.naver.NaverHomeAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class NaverShopPatternMessage extends NaverPatternMessage {

    private static final String TAG = NaverShopPatternMessage.class.getSimpleName();

    private static final int MAX_PAGE_COUNT = 7;

    protected static final int GO_PRE_SEND_WORKING = 39;
    protected static final int GO_SHOP_HOME = 40;
    protected static final int GO_SHOP_HOME_DIRECT = 41;
    protected static final int GO_SHOP_BUY_PAGE = 42;
    protected static final int GO_IMAGE_BUY_PAGE = 43;
    protected static final int GO_SHOP_PLAN_SUB_PAGE = 44;
    protected static final int GO_SHOP_CONTENT_URL = 45;
    protected static final int GO_GOOGLE_HOME = 46;
    protected static final int GO_SHOPPING_LIVE_URL = 47;
    private static final int TOUCH_POPUP_CLOSE_BUTTON = 48;
    private static final int TOUCH_SHOP_HOME_SEARCH_BUTTON = 49;
    private static final int RANDOM_SCROLL = 50;
    private static final int TOUCH_BACK_BUTTON = RANDOM_SCROLL + 1;
    private static final int TOUCH_CONTENT = RANDOM_SCROLL + 2;
    private static final int TOUCH_CONTENT_CHECK = RANDOM_SCROLL + 3;
    private static final int TOUCH_GO_TO_BUY_BUTTON = RANDOM_SCROLL + 4;
    private static final int CHECK_IMAGE_POPUP = RANDOM_SCROLL + 5;
    private static final int TOUCH_GO_TO_IMAGE_BUY_BUTTON = RANDOM_SCROLL + 6;
    private static final int AFTER_ACTION_PLAN_SUB_PAGE = RANDOM_SCROLL + 7;
    private static final int TOUCH_TOP_DOTS_BUTTON = RANDOM_SCROLL + 8;
    private static final int TOUCH_TOP_SHOPPING_BUTTON = TOUCH_TOP_DOTS_BUTTON + 1;
    private static final int TOUCH_MORE_BUTTON = TOUCH_TOP_DOTS_BUTTON + 2;
    private static final int TOUCH_MORE2_BUTTON = TOUCH_TOP_DOTS_BUTTON + 3;
    private static final int TOUCH_HOME_NEXT_BUTTON = TOUCH_TOP_DOTS_BUTTON + 4;
    private static final int TOUCH_HOME_PREV_BUTTON = TOUCH_TOP_DOTS_BUTTON + 5;
    private static final int TOUCH_NEXT_BUTTON = TOUCH_HOME_PREV_BUTTON + 1;
    private static final int TOUCH_NEXT_BUTTON_WAITING = TOUCH_NEXT_BUTTON + 2;
    private static final int TOUCH_OPTION_BUTTON = TOUCH_NEXT_BUTTON + 3;
    private static final int TOUCH_COMPANY_CONTENT = TOUCH_NEXT_BUTTON + 4;
    private static final int TOUCH_ALL_COMPANY_BUTTON = TOUCH_NEXT_BUTTON + 5;
    private static final int SCROLL_BOTTOM = TOUCH_NEXT_BUTTON + 6;
    private static final int TOUCH_SEARCH_BAR_FOR_CLEAR = SCROLL_BOTTOM + 1;

    private static final int TOUCH_SHOW_CONTENT_MORE = SCROLL_BOTTOM + 2;
    private static final int TOUCH_TAB_DETAIL_BUTTON = SCROLL_BOTTOM + 3;
    private static final int TOUCH_TAB_REVIEW_BUTTON = SCROLL_BOTTOM + 4;
    private static final int TOUCH_TAB_QNA_BUTTON = SCROLL_BOTTOM + 5;
    private static final int TOUCH_AFTER_HOME = SCROLL_BOTTOM + 6;
    private static final int TOUCH_AFTER_POPULAR = SCROLL_BOTTOM + 7;

    private static final int TOUCH_HOME_PAN_BUTTON = TOUCH_AFTER_POPULAR + 1;
    private static final int PICK_HOME_PAN_CONTENT = TOUCH_HOME_PAN_BUTTON + 1;
    private static final int TOUCH_HOME_PAN_CONTENT = TOUCH_HOME_PAN_BUTTON + 2;
    private static final int HOME_PAN_WEB_BACK = TOUCH_HOME_PAN_BUTTON + 3;
    private static final int TOUCH_HOME_NEWS_REFRESH_BUTTON = TOUCH_HOME_PAN_BUTTON + 4;
    private static final int TOUCH_HOME_ECONOMY_CHART_BUTTON = TOUCH_HOME_PAN_BUTTON + 5;
    private static final int TOUCH_HOME_SPORTS_REFRESH_BUTTON = TOUCH_HOME_PAN_BUTTON + 6;
    private static final int TOUCH_SEARCH_BAR_HOME_BUTTON = TOUCH_HOME_PAN_BUTTON + 7;
    private static final int TOUCH_TOP_VIEW_BUTTON = TOUCH_HOME_PAN_BUTTON + 8;
    private static final int PICK_VIEW_CONTENT = TOUCH_HOME_PAN_BUTTON + 9;
    private static final int TOUCH_VIEW_CONTENT = TOUCH_HOME_PAN_BUTTON + 10;
    private static final int VIEW_WEB_BACK = TOUCH_HOME_PAN_BUTTON + 11;
    private static final int PICK_SITE_CONTENT = TOUCH_HOME_PAN_BUTTON + 12;
    private static final int TOUCH_SITE_CONTENT = TOUCH_HOME_PAN_BUTTON + 13;
    private static final int WEB_BACK_NEXT = TOUCH_HOME_PAN_BUTTON + 14;

    private final NaverHomeAction _homeAction;
    private final NaverShopPageAction _shopPageAction;
    private final RandomSwipePatternAction _randomSwipePatternAction;
    private final SwipeThreadAction _swipeAction;
    protected final NaverShopSearchBarAction _shopSearchBarAction;
    protected final NaverShopSearchBarClearPatternAction _shopSearchBarClearPatternAction;
//    protected final NaverShopPatternMessage _subNaverShopPatternMessage;

    private int _nextMessage = 0;
    private int _currentPage = 0;
    private int _step = 0;
    private int _searchStep = 0;
    private int _findBarCount = 0;
    private int _homeWaitCount = 0;
    private int _waitCount = 0;
    private int _imageWaitCount = 0;
    private int _homePickCount = 0;
    private int _homePickRetryCount = 0;
    private int _homeRandomClickCount = 0;
    private int _homeRandomClickWorkCount = 0;
    private int _homeNewsRefreshCount = 0;
    private String _keyword;
    private String _mid;
    private String _mid2;
    private int _scrollType = 0;
    private int _scrollCount1 = 0;
    private int _scrollCount2 = 0;
    private boolean _isLastUp = false;
    private boolean _isViewFail = false;
    private boolean _isSiteSearch = false;

    private int _startHomeMode = 0;
    private int _randomClickCount = 0;
    private int _randomClickWorkCount = 0;
    private boolean _foundRandomItem = true;
    private ArrayList<String> _randomMids = new ArrayList<>();
    private boolean _workMore = true;

    private boolean _runAfterShowPattern = false;
    private boolean _runHomePattern = false;
    private boolean _runViewPattern = false;
    private int _noProductRetryCount = 0;

    public NaverShopPatternMessage(WebViewManager manager, KeywordItemMoon item) {
        super(manager);
        _item = item;
        _keyword = item.keyword;
        _mid = item.mid1;
        _mid2 = item.mid2;
        _startHomeMode = item.shopHome;
        _workMore = (item.item.workMore == 1);
        _randomClickCount = item.item.randomClickCount;

        // 랜덤클릭이 필요하다면 랜덤아이템을 찾는다.
        if (_randomClickCount > 0) {
            _foundRandomItem = false;
        }

        // 클릭 개수,
//        if (!TextUtils.isEmpty(item.item.randomClickCount)) {
//            String ratios[] = item.item.randomClickCount.split(":");
//            List<Integer> valueList = new ArrayList<>();
//            _clickCount = ratios.length;
//            int totalNumber = 0;
//
//            for (String ratio : ratios) {
//                totalNumber += Integer.parseInt(ratio);
//                valueList.add(totalNumber);
//            }
//
//            int pickNumber = (int) MathHelper.randomRange(0, totalNumber - 1);
//            int index = 0;
//
//            for (Integer value : valueList) {
//                if (pickNumber < value) {
//                    break;
//                }
//            }
//        }

        // 이것 작동 보장되지 않음.
        if (item.item.searchWork.length() > 0) {
            _keyword = item.item.searchWork;

            if (!_mid2.equals(".")) {
                _mid = _mid2;
                _mid2 = ".";
            }
        } else if (item.item.productName.length() > 0) {
            String str = item.item.productName;
            String storeName = item.item.storeName;

            if (storeName.length() > 0) {
                str = str.replace(item.item.storeName, "");
                storeName += " ";
            }

//            String[] strArray = str.trim().split("\\s+");
//            int length = strArray.length;
//            StringBuilder sb = new StringBuilder();
//            String productName = "";
//
//            if (length > 3) {
//                length = 3;
//            }
//
//            sb.append(strArray[0]);
//
//            for (int i = 1; i < length; i++) {
//                sb.append(" " + strArray[i]);
//            }
//
//            productName = sb.toString();
//            _keyword = storeName + productName;
            _keyword = storeName + str;

            if (!_mid2.equals(".")) {
                _mid = _mid2;
                _mid2 = ".";
            }
        }

        _homeAction = new NaverHomeAction(manager.getWebView());
        _shopPageAction = new NaverShopPageAction(manager.getWebView());
        manager.setPatternAction(_shopPageAction);

        _randomSwipePatternAction = new RandomSwipePatternAction(manager.getWebView().getContext());
        _shopSearchBarAction = new NaverShopSearchBarAction(manager.getWebView());
        _shopSearchBarClearPatternAction = new NaverShopSearchBarClearPatternAction(manager.getWebView());

        if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_BACK) {
//            _subNaverShopPatternMessage = new NaverShopPatternMessageAll(manager, item);
            Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_SHOP_ABC_BACK");
        } else if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_RANDOM) {
            Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_SHOP_ABC_RANDOM");
//            _subNaverShopPatternMessage = new NaverShopPatternMessageAll(manager, item);
        } else if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_AI_NEWS_VIEW) {
            Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_SHOP_AI_NEWS_VIEW");
//            _subNaverShopPatternMessage = new NaverShopPatternMessageAiNewsView(manager, item);

            _runHomePattern = true;
            _runViewPattern = true;
            _homeRandomClickCount = _item.item.homeRandomClickCount;
            // 이건 무조건 홈에서 시작한다.
            _startHomeMode = 0;
        } else if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_URL_CHANGE) {
            Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_SHOP_URL_CHANGE");
            // 이건 무조건 홈에서 시작한다.
            _startHomeMode = 0;
        } else {
            Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_NORMAL");
//            _subNaverShopPatternMessage = new NaverShopPatternMessageAll(manager, item);
        }

        if (_item.item.afterType == KeywordItem.AFTER_TYPE_POPULAR) {
            Log.d(TAG, "# 후처리 모드: AFTER_TYPE_POPULAR");
            _runAfterShowPattern = true;
        } else {
            Log.d(TAG, "# 후처리 모드: AFTER_TYPE_NONE");
        }

        TouchInjector injector = new TouchInjector(manager.getWebView().getContext());
        injector.setSoftKeyboard(new SamsungKeyboard());

        _swipeAction = new SwipeThreadAction(injector);
        getResultAction().item = item;
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

//        _subNaverShopPatternMessage.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 네이버 쇼핑 작업 시작");
                _searchStep = 0;
                _randomClickWorkCount = 0;
                _noProductRetryCount = 0;

                String proxy = UserManager.getInstance().webProxy;

                if (TextUtils.isEmpty(proxy) || (_item.item.useWorkingApi == 0)) {
                    if (_item.item.account != null) {
                        _accountItem = _item.item.account;
                        _handler.sendEmptyMessage(GO_LOGIN);
                    } else {
                        _handler.sendEmptyMessage(RUN_NEXT);
                    }
                } else {
                    _handler.sendEmptyMessageDelayed(GO_PRE_SEND_WORKING, 1000);
                }
                break;
            }

            case GO_PRE_SEND_WORKING: {
                Log.d(TAG, "# 작업 정보 등록");
                String url = BuildConfig.SERVER_URL + "v1/mobile/keyword/" + _item.item.keywordId +
                        "/working?login_id=" + UserManager.getInstance().getLoginId(_webViewManager.getWebView().getContext()) +
                        "&imei=" + UserManager.getInstance().imei +
                        "&work_id=" + _item.uid;

                webViewLoad(msg, url);
                break;
            }

            case RUN_NEXT: {
                if (_startHomeMode == 1) {
                    _handler.sendEmptyMessage(GO_SHOP_HOME);
                } else if (_startHomeMode == 2) {
                    _handler.sendEmptyMessage(GO_SEARCH_HOME_EMPTY);
                } else if (_startHomeMode == 3 || _startHomeMode == 4) {
                    _handler.sendEmptyMessage(GO_SHOP_HOME_DIRECT);
                } else {
                    if (_item.item.workType >= 500) {
                        // 신규 url 방식.
                        _handler.sendEmptyMessage(GO_SHOPPING_LIVE_URL);
                    } else if (_item.item.workType >= 400) {
                        // 구글홈 방식
                        _handler.sendEmptyMessage(GO_GOOGLE_HOME);
                    } else if (_item.item.workType >= 300) {
                        _webViewManager.setLoadsImagesAutomatically(true);
                        _handler.sendEmptyMessage(GO_IMAGE_BUY_PAGE);
                    } else if (_item.item.workType >= 210) {
                        _handler.sendEmptyMessage(GO_SHOP_PLAN_SUB_PAGE);
                    } else if (_item.item.workType >= 200) {
                        _handler.sendEmptyMessage(GO_SHOP_BUY_PAGE);
                    } else {
                        _handler.sendEmptyMessage(GO_HOME);
                    }

                    // _handler.sendEmptyMessage(FIND_KEYWORD);
                }
                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 네이버 홈으로 이동");
//                _webViewManager.loadUrl("http://google.com");
                if (_item.item.interceptType == 1) {
                    webViewLoad(msg, Config.NAVER_HOME_MOBILE_URL);
//                    webViewLoad(msg, "http://naver.com");
                } else {
                    if ((int) MathHelper.randomRange(0, 1) == 0) {
                        webViewLoad(msg, Config.NAVER_HOME_URL);
                    } else {
                        webViewLoad(msg, Config.NAVER_HOME_MOBILE_URL);
                    }
                }
//                webViewLoad(msg, "https://212b-222-237-14-89.ngrok.io/h2");
                break;
            }

            case GO_SEARCH_HOME_EMPTY: {
                Log.d(TAG, "# 네이버 빈검색결과로 이동");
//                _webViewManager.loadUrl("http://google.com");
                webViewLoad(msg, "https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m&query=");
                break;
            }

            case GO_SHOP_HOME: {
                Log.d(TAG, "# 네이버 쇼핑홈으로 이동");
//                webViewLoad(msg, "https://search.shopping.naver.com/home");
                webViewLoad(msg, "https://shopping.naver.com/");
                break;
            }

            case GO_SHOP_HOME_DIRECT: {
                Log.d(TAG, "# 네이버 쇼핑홈으로 검색어 이동");
                _currentPage = 2;
                try {
                    _keyword = URLEncoder.encode(_keyword, "UTF-8");
                } catch (UnsupportedEncodingException e) {

                }

                if (_startHomeMode == 4) {
                    // 신규 버전 테스트 필요.
                    // https://msearch.shopping.naver.com/search/all?query=%EA%B0%84%EC%9E%A5%EA%B2%8C%EC%9E%A5&frm=NVSHSRC&vertical=home&fs=true
                    webViewLoad(msg, "https://msearch.shopping.naver.com/search/all?query=" + _keyword + "&frm=MOSCVUI&age=999&gender=all");
//                    webViewLoad(msg, "https://msearch.shopping.naver.com/search/all?query=" + _keyword + "&frm=NVSHSRC&vertical=home&fs=true");
                } else {
                    // https://msearch.shopping.naver.com/search/all?query=%ED%99%8D%EB%AF%B8%EC%A7%91%20%EB%A7%8C%EB%8A%A5%EC%86%8C%EC%8A%A4&frm=NVSHSRC&vertical=search
                    // https://msearch.shopping.naver.com/search/all?query=간장게장&frm=NVSHSRC&cat_id=&pb=true&mall=
                    // https://msearch.shopping.naver.com/search/all?query=%EA%B0%84%EC%9E%A5%EA%B2%8C%EC%9E%A5&frm=NVSHSRC&vertical=home
                    // https://msearch.shopping.naver.com/search/all?query=%EB%B0%98%ED%8C%94%ED%8B%B0&frm=NVSHSRC&vertical=search
                    // https://msearch.shopping.naver.com/search/all?query=%EB%B0%98%ED%8C%94%ED%8B%B0&bt=-1&frm=MOSCVUI&age=999&gender=all
                    // https://msearch.shopping.naver.com/search/all?query=%EC%9E%90%EC%A0%84%EA%B1%B0&bt=-1&frm=MOSCPRO
//                    webViewLoad(msg, "https://msearch.shopping.naver.com/search/all?query=" + _keyword + "&bt=-1&frm=MOSCPRO");

                    // 위에 파싱을 하지만 쇼검에서는 +가 없다.
                    _keyword = WebViewManager.keywordEncodeForNaverInclPlus(_item.keyword);
                    webViewLoad(msg, "https://msearch.shopping.naver.com/search/all?query=" + _keyword + "&vertical=search");
//                    webViewLoad(msg, "https://msearch.shopping.naver.com/search/all?query=" + _keyword + "&frm=NVSHSRC&vertical=search");
                }
                break;
            }

            case GO_SHOP_BUY_PAGE: {
                Log.d(TAG, "# 네이버 쇼핑 사러가기로 이동");
                // 가격 비교도 단일처럼 처리한다.
                if (!_mid2.equals(".")) {
                    _mid = _mid2;
                    _mid2 = ".";
                }

                webViewLoad(msg, "https://msearch.shopping.naver.com/product/" + _mid);
                break;
            }

            case GO_IMAGE_BUY_PAGE: {
                Log.d(TAG, "# 네이버 이미지 사러가기로 이동");
                // 가격 비교도 단일처럼 처리한다.
                if (!_mid2.equals(".")) {
                    _mid = _mid2;
                    _mid2 = ".";
                }

                String parsed = WebViewManager.keywordEncodeForNaver(_keyword);
                String url = String.format("https://m.search.naver.com/search.naver?where=m_image&mode=column&section=nshopping&query=%s&nso_open=1&pq=#imgId=lens_kr_shp_product%%3A%s", parsed, _mid);
                webViewLoad(msg, url);
                break;
            }

            case GO_SHOP_PLAN_SUB_PAGE: {
                Log.d(TAG, "# 네이버 쇼핑 기획전 상세 상품으로 이동");
                String url = String.format("https://m.smartstore.naver.com/inflow/outlink/product?p=%s&tr=sld&trx=%s", _item.item.productId, _item.item.planId);
                webViewLoad(msg, url);
                break;
            }

            case GO_SHOP_CONTENT_URL: {
                Log.d(TAG, "# 쇼핑 컨텐츠 URL로 이동");
                String url = _shopPageAction.getItemUrl(_mid);
                if (!TextUtils.isEmpty(url)) {
                    String mainKeyword = _item.item.searchMain;

                    if (TextUtils.isEmpty(mainKeyword)) {
                        mainKeyword = _keyword;
                    }

//                String url = "https://cr3.shopping.naver.com/bridge/searchGate?cat_id=50007243&nv_mid=27441808344&query=%EC%96%B8%EA%B0%90%EC%83%9D%EC%8B%A0+%EC%B4%88%EB%B0%A5%EB%A8%B9%EC%9D%B4&bt=2&frm=MOSCPRO&h=7b2578292f12ff024a0208aa76ac0df1904bc395&t=L97F7KKK";
//                String keyword = "아동 밸류";
//                String keyword2 = "아동  밸류 무지개";
//                String ss = _webViewManager.keywordEncodeForNaver(keyword);
//                String ss2 = _webViewManager.keywordEncodeForNaver(keyword2);
                    String parsed = WebViewManager.keywordEncodeForNaver(mainKeyword);
                    String newUrl = _webViewManager.changeQueryValue(url, "query", parsed);
                    Log.d(TAG, "키워드: " + _keyword + " -> " + mainKeyword);
                    webViewLoad(msg, newUrl);
                } else {
                    Log.d(TAG, "# 상품을 찾을 수 없어서 패턴종료.");
                    _workCode = 110005;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case GO_GOOGLE_HOME: {
                Log.d(TAG, "# 구글홈으로 이동");
                webViewLoad(msg, "https://www.google.co.kr");
                break;
            }

            case GO_SHOPPING_LIVE_URL: {
                Log.d(TAG, "# 쇼핑 라이브 URL로 이동");
                // 500, 502
                // 유입경로: 네이버 쇼핑 - 서비스
                String tr = "nsh";

                if (_item.item.workType == 501 || _item.item.workType == 503) {
                    // 유입경로: 네이버 디스플레이광고
                    tr = "ndptop";
                } else if (_item.item.workType == 504) {
                    // 유입경로: 웹사이트 (네이버 모바일홈 값)
                    tr = "sblim";
                } else if (_item.item.workType == 505) {
                    // 유입경로: 웹사이트 (원래 경로값)
                    tr = "pdlim";
                }

                //https://product.shoppinglive.naver.com/bridge/v4/recommend-product/shopping?tr=ndptop&channelProductNo=10250006130&sourceUrl=https%3A%2F%2Fsmartstore.naver.com%2Fmain%2Fproducts%2F10250006130
                String url = String.format("https://product.shoppinglive.naver.com/bridge/v4/recommend-product/shopping?tr=%s&channelProductNo=%s&sourceUrl=https%%3A%%2F%%2Fsmartstore.naver.com%%2Fmain%%2Fproducts%%2F%s", tr, _item.item.url, _item.item.url);
                webViewLoad(msg, url);
                break;
            }

            case TOUCH_NEW_POPUP_OK: {  // 4월 3일 바뀐 홈.
                Log.d(TAG, "# 안내팝업창 검사");
                if (_homeAction.touchButton(NaverHomeAction.BUTTON_POPUP_OK)) {
                    _handler.sendEmptyMessageDelayed(TOUCH_NEW_POPUP2_OK, MathHelper.randomRange(2500, 3500));
                } else {
                    _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, 1000);
                }
                break;
            }

            case TOUCH_NEW_POPUP2_OK: {  // 4월 3일 바뀐 홈.
                Log.d(TAG, "# 안내팝업창2 검사");
                if (_homeAction.touchButton(NaverHomeAction.BUTTON_POPUP2_OK)) {
                    _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(2500, 3500));
                } else {
                    _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, 1000);
                }
                break;
            }

            case TOUCH_POPUP_CLOSE_BUTTON: {  // 20240504
                Log.d(TAG, "# 쇼핑홈 팝업창 닫기 버튼 검사");
                if (_shopPageAction.touchHomePopupCloseButton()) {
                    _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(1500, 2500));
//                    _handler.sendEmptyMessageDelayed(TOUCH_SHOP_HOME_SEARCH_BUTTON, MathHelper.randomRange(1500, 2500));
                } else {
                    _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, 1000);
//                    _handler.sendEmptyMessageDelayed(TOUCH_SHOP_HOME_SEARCH_BUTTON, 1000);
                }
                break;
            }

            case TOUCH_SEARCH_BAR: {
                Log.d(TAG, "# 검색창 터치");
                if (isShopHome()) {
                    if (_shopPageAction.touchSearchBar()) {
                        _waitCount = 0;
                        _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
                    } else {
                        Log.d(TAG, "# 검색창 터치에 실패해서 패턴종료.");
                        _workCode = 110002;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                } else {
                    boolean isHome = _startHomeMode != 2;

                    if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_AI_NEWS_VIEW) {
                        isHome = false;
                    }

                    if (_homeAction.touchSearchBar(isHome)) {
                        _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
                    } else {
                        Log.d(TAG, "# 검색창 터치에 실패해서 패턴종료.");
                        _workCode = 110001;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                }
                break;
            }

            case TOUCH_SEARCH_BAR_FOR_CLEAR: {
                Log.d(TAG, "# 삭제하기위해 검색창 터치");
                if (isShopHome()) {
                    if (_shopPageAction.touchSearchBar()) {
                        _waitCount = 0;
                        _handler.sendEmptyMessageDelayed(CLEAR_SEARCH_BAR, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 검색창 터치에 실패해서 패턴종료.");
                        _workCode = 110004;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                } else {
                    if (_homeAction.touchSearchBar(_startHomeMode != 2)) {
                        _handler.sendEmptyMessageDelayed(CLEAR_SEARCH_BAR, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 검색창 터치에 실패해서 패턴종료.");
                        _workCode = 110003;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                }
                break;
            }

            case INPUT_KEYWORD: {
                Log.d(TAG, "# 검색창 검사");
                if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_BACK) {
                    if (_searchStep == 0 && !TextUtils.isEmpty(_item.item.search1)) {
                        _keyword = _item.item.search1;
                    } else if (_searchStep == 1) {
                        _keyword = _item.keyword;
                    } else if (_searchStep == 2 && !TextUtils.isEmpty(_item.item.searchMain)) {
                        _keyword = _item.item.searchMain;
                    } else {
                        _keyword = _item.keyword;
                    }
                } else if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_RANDOM) {
                    if (_searchStep == 0 && !TextUtils.isEmpty(_item.item.search1)) {
                        _keyword = _item.item.search1;
                    } else if (_searchStep == 1 && !TextUtils.isEmpty(_item.item.search2)) {
                        _keyword = _item.item.search2;
                    } else {
                        _keyword = _item.keyword;
                    }
                }

                if (isShopHome()) {
                    if (!_shopPageAction.searchBarShown()) {
                        if (_findBarCount > 15) {
                            Log.d(TAG, "# 로딩에러로 처리 중단.");
                            _workCode = 110012;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        } else {
                            if (_waitCount < 5) {
//                                Log.d(TAG, "# 검색창이 떠있지 않아서 3초 후 다시 시도..." + _waitCount);
                                Log.d(TAG, "# 검색창이 떠있지 않아서 3초 후 다시 터치..." + _waitCount);
                                ++_waitCount;
//                                _handler.sendEmptyMessageDelayed(msg.what, 3000);
                                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, 3000);
//                                _handler.sendEmptyMessageDelayed(TOUCH_SHOP_HOME_SEARCH_BUTTON, 3000);
                            } else {
                                Log.d(TAG, "# 검색창이 떠있지 않아서 새로고침");
                                SystemClock.sleep(MathHelper.randomRange(3500, 5000));
                                ++_findBarCount;
                                _webViewManager.reload();
                                webViewLoading(msg);
//                            Log.d(TAG, "# 검색창이 떠있지 않아서 다시 터치");
//                            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, 5000);
                            }
                        }
                    } else {
                        // 네이버 쇼검 검색창이 변경되어서 재터치 하도록 변경.
                        if (_shopPageAction.touchSearchBar()) {
                            SystemClock.sleep(MathHelper.randomRange(2000, 3000));
                            inputKeyword(_keyword);
                        } else {
                            Log.d(TAG, "# 검색창 재 터치에 실패해서 패턴종료.");
                            _workCode = 110007;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    }
                } else {
                    _searchBarCheckPatternAction.checkSearchBarShown();

                    if (!_searchBarCheckPatternAction.isFocus()) {
                        if (_findBarCount > 15) {
                            Log.d(TAG, "# 로딩에러로 처리 중단.");
                            _workCode = 110011;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        } else {
                            if (_startHomeMode == 2) {
                                Log.d(TAG, "# 검색창이 떠있지 않아서 다시 홈으로");
                                ++_findBarCount;
                                _handler.sendEmptyMessageDelayed(GO_SEARCH_HOME_EMPTY, MathHelper.randomRange(3500, 5000));
                            } else {
                                Log.d(TAG, "# 검색창이 떠있지 않아서 빈영역 터치 후 다시 터치");
                                _homeAction.touchEmptyArea();
                                ++_findBarCount;
                                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(3500, 5000));
                            }
                        }
                    } else {
                        inputKeyword(_keyword);
                    }
                }
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치");
                _step = 0;
                _scrollCount1 = 0;

                if (isShopHome()) {
                    _currentPage = 2;   // 실제 페이지가 아니라 단순 구분용이므로. 메인이 아닌것으로 설정.
//                    _shopSearchBarAction.submitSearchButton();
                    _action.touchSearchButton();
                } else {
                    _currentPage = 1;
//                    _searchBarAction.submitSearchButton();
                    _action.touchSearchButton();
                }
                webViewLoading(msg);
                break;
            }

            case TOUCH_SHOP_HOME_SEARCH_BUTTON: {
                Log.d(TAG, "# 쇼핑홈 검색버튼 터치");
                if (_shopPageAction.touchHomeSearchButton()) {
                    _currentPage = 2;   // 실제 페이지가 아니라 단순 구분용이므로. 메인이 아닌것으로 설정.
                    _waitCount = 0;
                    _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
                } else {
                    Log.d(TAG, "# 쇼핑홈 검색버튼 터치에 실패해서 패턴종료.");
                    _workCode = 110006;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case RANDOM_SCROLL: {
                if (_nextMessage == WEB_BACK || _nextMessage == HOME_PAN_WEB_BACK) {
                    if (_item.item.randomScrollCount > 0) {
                        _randomRange = _item.item.randomScrollCount;
                    } else if (_item.item.randomScrollCount < 0) {
                        _randomRange = 0;
                    }
                }

                if (_randomRange > 0) {
                    int count = (int) MathHelper.randomRange(_randomRange, _randomRange + 4);
                    Log.d(TAG, "# 랜덤 스크롤: " + count);
                    boolean isUp = false;
                    int sameCount = 0;

                    for (int i = 0; i < count; ++i) {
                        if (i < 5) {
                            Log.d(TAG, "아래로 스크롤");
                            ++sameCount;
                            _swipeAction.swipeDown(false);
                        } else {
                            if (sameCount < 2) {
                                ++sameCount;
                            } else {
                                isUp = !isUp;
                                sameCount = 1;
    //                            int isUp = (int) MathHelper.randomRange(0, 1);
                            }

                            if (!isUp) {
                                Log.d(TAG, "아래로 스크롤");
                                _swipeAction.swipeDown(false);
                            } else {
                                Log.d(TAG, "위로 스크롤");
                                _swipeAction.swipeUp(false);
                            }
                        }

                        SystemClock.sleep(MathHelper.randomRange(1300, 2500));
                    }

                    _handler.sendEmptyMessageDelayed(_nextMessage, MathHelper.randomRange(2000, 3000));
                } else {
                    _handler.sendEmptyMessageDelayed(_nextMessage, 100);
                }
                break;
            }

            case TOUCH_BACK_BUTTON: {
                Log.d(TAG, "# 백 버튼 터치");
                pressBackButton();
                webViewLoading(msg);
                break;
            }

            case TOUCH_CONTENT: {
                Log.d(TAG, "# 네이버 쇼핑 컨텐츠 검사");
                uploadLoginCookieStatusInWebView();

                if (!_foundRandomItem) {
                    ArrayList<String> exceptList = new ArrayList<>();
                    exceptList.add(_mid);
                    exceptList.addAll(_randomMids);
                    _foundRandomItem = true;
                    String randomMid;

                    if (_currentPage <= 1) {
                        randomMid = _shopPageAction.getMainRandomItem(exceptList);
                    } else {
                        randomMid = _shopPageAction.getRandomItem(exceptList);
                    }

                    if (!TextUtils.isEmpty(randomMid)) {
                        Log.d(TAG, "# 랜덤터치: " + randomMid);
                        _mid = randomMid;
                        _randomMids.add(randomMid);
                    } else {
                        Log.d(TAG, "# 상품이 한개여서 랜덤클릭 무시");
                        // 상품이 한개일때, 랜덤할것이 없다면 원래 값으로 처리하고, 랜덤클릭 처리를 하지 않는다.
                        _mid = _item.mid1;
                        _randomClickCount = 0;
                    }
                }

                if (_shopPageAction.searchBarShown()) {
//                    Log.d(TAG, "# 쇼핑 검색창이 떠있어서 검색버튼 터치.");
//                    if (_shopSearchBarAction.touchSearchButton()) {
//                        msg.what = TOUCH_SEARCH_BUTTON;
//                        webViewLoading(msg);
//                    } else {
//                        Log.d(TAG, "# 쇼핑 검색 버튼 터치 실패로 패턴종료.");
                        // 여기 들어오는 상황이 검색버 화면에서 인터넷이 끊어져서 submit 이 안된 상태에서 새로고침이 발생하고 그상태에서 로직처리가 넘어가면 여기로 오는데,
                        // 우선적으로는 다시 판단해봐야하므로 실패 처리로 넘긴다.
                        Log.d(TAG, "# 쇼핑 검색 화면에서 이동 실패로 패턴종료.");
                        _workCode = 110048;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
//                    }
                    break;
                } else {
                    _searchBarCheckPatternAction.checkSearchBarShown();
                    // 모통버전도 한번 체크필요하다.
                    if (_searchBarCheckPatternAction.isFocus()) {
                        Log.d(TAG, "# 검색창이 떠있어서 검색버튼 터치.");
                        _searchBarAction.submitSearchButton();
                        msg.what = TOUCH_SEARCH_BUTTON;
                        webViewLoading(msg);
                        break;
                    }
                }

                if ((_item.item.inclEmpty == 0) && _shopPageAction.hasEmptyResult()) {
                    Log.d(TAG, "# 검색결과 없는 연관 상품 노출로 패턴종료.");
                    _workCode = 110022;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 1000);
                    break;
                }

                if (_shopPageAction.hasShopEmptyResult()) {
                    Log.d(TAG, "# 쇼핑 검색결과 없어서 패턴종료.");
                    _workCode = 110023;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 1000);
                    break;
                }

                InsideData insideData = _shopPageAction.getContentMidInsideData(_mid, (_currentPage <= 1), (_item.item.workMore == -1));
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 컨텐츠 터치");
                        if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_URL_CHANGE) {
                            _handler.sendEmptyMessageDelayed(GO_SHOP_CONTENT_URL, MathHelper.randomRange(2000, 4000));
                        } else {
                            String currentUrl = _webViewManager.getCurrentUrl();

                            if (_shopPageAction.touchContentMid(_mid, (_currentPage <= 1))) {
                                // 쇼검만해당.
                                if (_currentPage > 1) {
                                    String url = _shopPageAction.getNClickUrl(_mid, currentUrl);

                                    if (!TextUtils.isEmpty(url)) {
                                        // 네이버 신규 버전 처리용.
                                        HttpEngine _httpEngine;
                                        _httpEngine = new HttpEngine(_webViewManager.getWebView().getContext());
                                        _httpEngine.setUa(UserManager.getInstance().ua);
                                        _httpEngine.setChromeVersion(UserManager.getInstance().chromeVersion);
                                        _httpEngine.setBrowserVersion(UserManager.getInstance().browserVersion);
                                        _httpEngine.setNnb(UserManager.getInstance().nnb);
                                        _httpEngine.setReferer(currentUrl);
                                        _httpEngine.requestUrlWithOkHttpClientImage(url);
                                    }
                                }

                                webViewLoading(msg);
                            } else {
                                Log.d(TAG, "# 네이버 쇼핑 컨텐츠 터치 실패로 패턴종료.");
                                _workCode = 110021;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                            }
                        }
                    } else if (insideData.getHeight() > 450) {
                        Log.d(TAG, "# 컨텐츠가 제대로 로드되지 않아서 새로고침.");
                        _webViewManager.reload();
                        msg.what = TOUCH_SEARCH_BUTTON;
                        webViewLoading(msg);
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 네이버 쇼핑 컨텐츠 못찾아서 다음으로...");
                    if (_currentPage <= 1) {    // 모통홈 로직
                        if (_item.item.workType == 1) {
                            _nextMessage = TOUCH_TOP_SHOPPING_BUTTON;
                            sendMessageDelayed(TOUCH_TOP_DOTS_BUTTON, 100);
                        } else {
                            sendMessageDelayed(TOUCH_MORE2_BUTTON, 100);
                        }
                    } else {
                        if (_workMore) {
                            Log.d(TAG, "# 페이지 하단 검사");
                            if ((_scrollCount1 < 10) && !_shopPageAction.checkPageBottom()) {
                                ++_scrollCount1;
                                // 페이지 하단이 아니라면 아래로 스크롤한다.
                                _nextMessage = TOUCH_CONTENT;
                                _scrollType = 0;
                                _handler.sendEmptyMessageDelayed(SCROLL_BOTTOM, 100);
                            } else {
                                sendMessageDelayed(TOUCH_NEXT_BUTTON, 100);
                            }
                        } else {
                            Log.d(TAG, "# 더보기 처리 중단으로 패턴종료.");
                            _workCode = 110045;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    }
                }
                break;
            }

            case TOUCH_CONTENT_CHECK: {
                if (_shopPageAction.hasNoProductResult()) {
                    if (_noProductRetryCount < 5) {
                        ++_noProductRetryCount;
                        Log.d(TAG, "# 쇼핑 상품 없습니다 페이지여서 뒤로가기.");
                        _nextMessage = TOUCH_CONTENT;
                        _handler.sendEmptyMessageDelayed(WEB_BACK_NEXT, MathHelper.randomRange(3000, 5000));
                    } else {
                        Log.d(TAG, "# 쇼핑 상품 없습니다 페이지여서 패턴종료.");
                        _workCode = 110049;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 1000);
                    }
                    break;
                }

                if (_lastMessage == TOUCH_CONTENT) {
                    // 페이지 넘어갔는지 확인.
                    InsideData insideData = _shopPageAction.getContentMidInsideData(_mid, (_currentPage <= 1), (_item.item.workMore == -1));
                    if (insideData != null) {
                        Log.d(TAG, "# 상세 페이지로 이동되지 않아서 다시 검사");
                        _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, 100);
                        break;
                    }
                }

//                _result = 1;
                int nextMessage = RANDOM_SCROLL;
                int baseMessage = WEB_BACK;
                long delay = MathHelper.randomRange(3000, 5000);

                if (_item.item.afterType == KeywordItem.AFTER_TYPE_POPULAR) {
                    baseMessage = TOUCH_AFTER_HOME;
                }

                if (_randomClickWorkCount >= _randomClickCount) {
                    if (!_mid2.equals(".")) {
                        if (TextUtils.isEmpty(_item.item.code3)) {
                            _nextMessage = TOUCH_COMPANY_CONTENT;
                        } else {
                            _nextMessage = TOUCH_OPTION_BUTTON;
                        }

                        _randomRange = 6;

                        // 랜덤스크롤 사용 안하도록 설정한다.
                        if (_item.item.randomScrollCount < 0) {
                            _randomRange = 0;
                        }
                    } else {
                        Log.d(TAG, "# 작업 성공");
                        if (_runAfterShowPattern) {
                            _randomRange = 6;
                            nextMessage = TOUCH_SHOW_CONTENT_MORE;
                        }

                        if (baseMessage == WEB_BACK) {
                            _result = ResultAction.SUCCESS;

                            // 임시로 적용. 원래는 뒤로가기 였다.
                            nextMessage = END_PATTERN;
                            _workCode = 110913;
                            delay = MathHelper.randomRange(4000, 10000);
                            Log.d(TAG, "# 대기: " + delay + "ms");
                        }

                        if (_startHomeMode == 5) {
                            nextMessage = END_PATTERN;
                            _workCode = 110910;
                            delay = MathHelper.randomRange(4000, 15000);
                            Log.d(TAG, "# 대기: " + delay + "ms");
                        }

                        _nextMessage = baseMessage;
                    }
                } else {
                    _nextMessage = baseMessage;
//                    _nextMessage = TOUCH_BACK_BUTTON;
                }

                _handler.sendEmptyMessageDelayed(nextMessage, delay);
                break;
            }

            case TOUCH_GO_TO_BUY_BUTTON: {
                Log.d(TAG, "# 네이버 쇼핑 사러가기 버튼 검사");
                uploadLoginCookieStatusInWebView();

                InsideData insideData = _shopPageAction.getGoToBuyButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 사러가기 버튼 터치");
                        if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_GO_TO_BUY)) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 사러가기 버튼 터치 실패로 패턴종료.");
                            _workCode = 110019;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 네이버 쇼핑 사러가기 버튼 없어서 패턴종료.");
                    _workCode = 110020;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 1000);
                }
                break;
            }

            case CHECK_IMAGE_POPUP: {
                Log.d(TAG, "# 네이버 이미지 뷰어 떠있는지 검사");
                InsideData insideData = _shopPageAction.getImageViewerInsideData();
                if (insideData != null) {
                    Log.d(TAG, "# 네이버 이미지 뷰어 확인");
                    _handler.sendEmptyMessageDelayed(TOUCH_GO_TO_IMAGE_BUY_BUTTON, MathHelper.randomRange(1000, 3000));
                } else {
                    if (_imageWaitCount < 20) {
                        Log.d(TAG, "# 네이버 이미지 뷰어 확인 실패로 3초 후 다시 시도..." + _imageWaitCount);
                        ++_imageWaitCount;
                        _handler.sendEmptyMessageDelayed(msg.what, 3000);
                    } else {
                        Log.d(TAG, "# 네이버 이미지 뷰어 확인 실패로 패턴종료.");
                        _workCode = 110016;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 1000);
                    }
                }
                break;
            }

            case TOUCH_GO_TO_IMAGE_BUY_BUTTON: {
                Log.d(TAG, "# 네이버 이미지 사러가기 버튼 검사");
                uploadLoginCookieStatusInWebView();

                InsideData insideData = _shopPageAction.getGoToImageBuyButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 이미지 사러가기 버튼 터치");
                        _webViewManager.setLoadsImagesAutomatically(false);
                        if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_GO_TO_IMAGE_BUY)) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 네이버 이미지 사러가기 버튼 터치 실패로 패턴종료.");
                            _workCode = 110017;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 네이버 이미지 사러가기 버튼 없어서 패턴종료.");
                    _workCode = 110018;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 1000);
                }
                break;
            }

            case AFTER_ACTION_PLAN_SUB_PAGE: {
                Log.d(TAG, "# 네이버 쇼핑 PC 기획전 후처리");
                uploadLoginCookieStatusInWebView();

                int nextMessage = RANDOM_SCROLL;
                long delay = MathHelper.randomRange(3000, 5000);

                _result = ResultAction.SUCCESS;
                _workCode = 110911;

                if (_item.item.randomScrollCount < 0) {
                    nextMessage = END_PATTERN;
                    _workCode = 110912;
                    delay = MathHelper.randomRange(4000, 12000);
                    Log.d(TAG, "# 대기: " + delay + "ms");
                }

                _handler.sendEmptyMessageDelayed(nextMessage, delay);
                break;
            }

            case TOUCH_SHOW_CONTENT_MORE: {
                Log.d(TAG, "# 네이버 쇼핑 펼쳐보기 버튼 검사");
                InsideData insideData = _shopPageAction.getShowContentMoreButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 펼쳐보기 버튼 터치");
                        if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_SHOW_CONTENT_MORE)) {
                            _result = ResultAction.SUCCESS;
                            _workCode = 110920;
                            _nextMessage = END_PATTERN;
//                            _nextMessage = WEB_BACK;
                            _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 펼쳐보기 버튼 터치 실패로 패턴종료.");
                            _workCode = 110026;
                            _workCodeAddition = 0;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 네이버 쇼핑 펼쳐보기 버튼 못찾아서 다음으로.");
//                    int pickNumber = (int) MathHelper.randomRange(1, 3);
                    int pickNumber = (int) MathHelper.randomRange(1, 2);

                    if (pickNumber == 1) {
                        _nextMessage = TOUCH_TAB_REVIEW_BUTTON;
                        _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                    } else if (pickNumber == 2) {
                        _nextMessage = TOUCH_TAB_QNA_BUTTON;
                        _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                    } else {
                        _result = ResultAction.SUCCESS;
                        _workCode = 110921;
                        _nextMessage = END_PATTERN;
//                        _nextMessage = WEB_BACK;
                        _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                    }
                }
                break;
            }

            case TOUCH_TAB_DETAIL_BUTTON: {
                Log.d(TAG, "# 네이버 쇼핑 상세정보탭 버튼 검사");
                InsideData insideData = _shopPageAction.getTabDetailButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 상세정보탭 버튼 터치");
                        if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_TAB_DETAIL)) {
                            _result = ResultAction.SUCCESS;
                            _workCode = 110922;
                            _nextMessage = END_PATTERN;
                            _randomRange = 1;
//                            _nextMessage = WEB_BACK;
                            _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 상세정보탭 버튼 터치 실패로 패턴종료.");
                            _workCode = 110027;
                            _workCodeAddition = 0;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 네이버 쇼핑 상세정보탭 버튼 못찾아서 다음으로.");
                    _result = ResultAction.SUCCESS;
                    _workCode = 110923;
                    _nextMessage = END_PATTERN;
//                    _nextMessage = WEB_BACK;
                    _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                }
                break;
            }

            case TOUCH_TAB_REVIEW_BUTTON: {
                Log.d(TAG, "# 네이버 쇼핑 리뷰탭 버튼 검사");
                InsideData insideData = _shopPageAction.getTabReviewButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 리뷰탭 버튼 터치");
                        if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_TAB_REVIEW)) {
                            _result = ResultAction.SUCCESS;
                            _workCode = 110924;
                            _nextMessage = END_PATTERN;
                            _randomRange = 1;
//                            _nextMessage = WEB_BACK;
                            _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 리뷰탭 버튼 터치 실패로 패턴종료.");
                            _workCode = 110028;
                            _workCodeAddition = 0;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 네이버 쇼핑 리뷰탭 버튼 못찾아서 다음으로.");
                    _result = ResultAction.SUCCESS;
                    _workCode = 110925;
                    _nextMessage = END_PATTERN;
//                    _nextMessage = WEB_BACK;
                    _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                }
                break;
            }

            case TOUCH_TAB_QNA_BUTTON: {
                Log.d(TAG, "# 네이버 쇼핑 Q&A탭 버튼 검사");
                InsideData insideData = _shopPageAction.getTabQnaButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 Q&A탭 버튼 터치");
                        if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_TAB_QNA)) {
                            _result = ResultAction.SUCCESS;
                            _workCode = 110926;
                            _nextMessage = END_PATTERN;
                            _randomRange = 1;
//                            _nextMessage = WEB_BACK;
                            _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 Q&A탭 버튼 터치 실패로 패턴종료.");
                            _workCode = 110029;
                            _workCodeAddition = 0;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 네이버 쇼핑 Q&A탭 버튼 못찾아서 다음으로.");
                    _result = ResultAction.SUCCESS;
                    _workCode = 110927;
                    _nextMessage = END_PATTERN;
//                    _nextMessage = WEB_BACK;
                    _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                }
                break;
            }

            case TOUCH_AFTER_HOME: {
                Log.d(TAG, "# 네이버 쇼핑 후처리 상점홈 검사");
                InsideData insideData = _shopPageAction.getCenterHomeLinkInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 후처리 상점홈 터치");
                        if (_shopPageAction.touchCenterHomeLink()) {
                            _result = ResultAction.SUCCESS;
                            _nextMessage = WEB_BACK;
                            _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 후처리 상점홈 터치 실패로 패턴종료.");
                            _workCode = 110024;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 네이버 쇼핑 후처리 상점홈 못찾아서 패턴종료.");
                    _workCode = 110046;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_AFTER_POPULAR: {
                Log.d(TAG, "# 네이버 쇼핑 후처리 인기상품 검사");
                InsideData insideData = _shopPageAction.getCenterHomeLinkInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 후처리 인기상품 터치");
                        if (_shopPageAction.touchCenterHomeLink()) {
                            _result = ResultAction.SUCCESS;
                            _nextMessage = WEB_BACK;
                            _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 후처리 인기상품 터치 실패로 패턴종료.");
                            _workCode = 110025;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 네이버 쇼핑 후처리 인기상품 못찾아서 패턴종료.");
                    _workCode = 110047;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_TOP_DOTS_BUTTON: {
                Log.d(TAG, "# 상단 점세개 버튼 검사");
                InsideData insideData = _homeAction.getTopDotsButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 상단 점세개 버튼 터치");
                        if (_homeAction.touchButton(NaverHomeAction.BUTTON_TOP_DOTS)) {
                            _handler.sendEmptyMessageDelayed(_nextMessage, MathHelper.randomRange(1000, 2000));
                        } else {
                            Log.d(TAG, "# 상단 점세개 버튼 터치 실패로 패턴종료.");
                            _workCode = 110035;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 상단 점세개 버튼 못찾아서 패턴종료.");
                    _workCode = 110036;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_TOP_SHOPPING_BUTTON: {
                Log.d(TAG, "# 상단 쇼핑탭 버튼 검사");
                InsideData insideData = _homeAction.getTopShoppingButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 상단 쇼핑탭 버튼 터치");
                        if (_homeAction.touchButton(NaverHomeAction.BUTTON_TOP_SHOPPING)) {
                            ++_currentPage;
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 상단 쇼핑탭 버튼 터치 실패로 패턴종료.");
                            _workCode = 110037;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 상단 쇼핑탭 버튼 못찾아서 패턴종료.");
                    _workCode = 110038;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_MORE2_BUTTON: {
                Log.d(TAG, "# 아래로 더보기 버튼 검사");
                InsideData insideData = _shopPageAction.getMore2ButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 아래로 더보기 버튼 터치");
                        if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_MORE2)) {
                            sendMessageDelayed(TOUCH_CONTENT, 3000);
//                            sendMessageDelayed(TOUCH_MORE_BUTTON, 3000);
                        } else {
                            Log.d(TAG, "# 아래로 더보기 버튼 터치 실패로 패턴종료.");
                            _workCode = 110033;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 아래로 더보기 버튼 못찾아서 다음으로...");
                    if (_shopPageAction.hasMain(_mid)) {
                        // 플릭페이지가 있을때만 여기로 들어온다.
                        String homeShopCurrentPage = _shopPageAction.getHomeShopCurrentPage();
                        if (homeShopCurrentPage == null) {
                            Log.d(TAG, "# 홈의 현재 페이지를 못찾아서 패턴종료.");
                            _workCode = 110044;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        } else {
                            int page = Integer.parseInt(homeShopCurrentPage);
                            int targetPage = 1;
                            String targetPageString = _shopPageAction.getHomeShopMidPage(_mid);
                            if (targetPageString != null) {
                                targetPage = Integer.parseInt(targetPageString);
                            }

                            Log.d(TAG, "# 타겟: " + targetPage + "/ 현재: " + page);
                            if (page < targetPage) {
                                sendMessageDelayed(TOUCH_HOME_NEXT_BUTTON, 100);
                            } else {
                                sendMessageDelayed(TOUCH_HOME_PREV_BUTTON, 100);
                            }
                        }
                    } else {
                        if (_workMore) {
                            sendMessageDelayed(TOUCH_MORE_BUTTON, 100);
                        } else {
                            Log.d(TAG, "# 더보기 처리 중단으로 패턴종료.");
                            _workCode = 110039;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    }
                }
                break;
            }

            case TOUCH_HOME_NEXT_BUTTON: {
                Log.d(TAG, "# 홈 쇼핑 좌버튼 검사");
                InsideData insideData = _shopPageAction.getHomeNextButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 홈 쇼핑 좌버튼 터치");
                        if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_HOME_NEXT)) {
                            sendMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(1000, 2000));
                        } else {
                            Log.d(TAG, "# 홈 쇼핑 좌버튼 터치 실패로 패턴종료.");
                            _workCode = 110034;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 홈 쇼핑 좌버튼 못찾아서 다음으로...");
                    if (_workMore) {
                        sendMessageDelayed(TOUCH_MORE_BUTTON, 100);
                    } else {
                        Log.d(TAG, "# 더보기 처리 중단으로 패턴종료.");
                        _workCode = 110040;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                }
                break;
            }

            case TOUCH_HOME_PREV_BUTTON: {
                Log.d(TAG, "# 홈 쇼핑 우버튼 검사");
                InsideData insideData = _shopPageAction.getHomePrevButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 홈 쇼핑 우버튼 터치");
                        if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_HOME_PREV)) {
                            sendMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(1000, 2000));
                        } else {
                            Log.d(TAG, "# 홈 쇼핑 우버튼 터치 실패로 패턴종료.");
                            _workCode = 110030;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 홈 쇼핑 우버튼 못찾아서 다시 터치 시도..");
                    sendMessageDelayed(TOUCH_CONTENT, 100);
                }
                break;
            }

            case TOUCH_MORE_BUTTON: {
                Log.d(TAG, "# 더보기 버튼 검사");
                InsideData insideData = _shopPageAction.getMoreButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 더보기 버튼 터치");
                        if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_MORE)) {
                            ++_currentPage;
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 더보기 버튼 터치 실패로 패턴종료.");
                            _workCode = 110031;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 더보기 버튼 못찾아서 패턴종료.");
                    _workCode = 110032;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_NEXT_BUTTON: {
                Log.d(TAG, "# 다음 버튼 검사");
                int maxPage = MAX_PAGE_COUNT;

                if (!_mid2.equals(".")) {
                    maxPage = 0;
                }

                InsideData insideData = _shopPageAction.getNextButtonInsideData();
                if (insideData != null) {
                    String page =  _shopPageAction.getCurrentPage();
                    if (page == null) {
                        Log.d(TAG, "# 현재 페이지를 못찾아서 패턴종료.");
                        _workCode = 110041;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    } else if (Integer.parseInt(page) > maxPage) {
                        Log.d(TAG, "# " + maxPage + " 페이지 초과로 패턴종료.");
                        _workCode = 110042;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    } else {
                        if (insideData.isInside()) {
                            Log.d(TAG, "# 다음 버튼 터치");
                            _scrollCount1 = 0;
                            webViewLoading(msg);
                            _handler.sendEmptyMessageDelayed(TOUCH_NEXT_BUTTON_WAITING, 100);
//                            if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_NEXT)) {
////                                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(5000, 6000));
//                            } else {
//                                Log.d(TAG, "# 다음 버튼 터치 실패로 패턴종료.");
//                                _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
//                            }
                        } else if (insideData.inside > 0) {
                            Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                            if (insideData.rect.top > 1500) {
                                _swipeAction.swipeDownFast(110, 200);
                            } else {
                                _swipeAction.swipeDown();
                            }
                            // 부분 로딩이 있기 때문에 스크롤 하고 체크한다.
                            _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(1000, 2000));
                        } else {
                            Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                            _swipeAction.swipeUp();
                            _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                        }
                    }
                } else {
                    Log.d(TAG, "# 다음 버튼 못찾아서 패턴종료.");
                    _workCode = 110043;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_NEXT_BUTTON_WAITING: {
                Log.d(TAG, "# 다음 버튼 터치2");
                if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_NEXT)) {
//                    _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(5000, 6000));
                } else {
                    Log.d(TAG, "# 다음 버튼 터치 실패로 패턴종료.");
                    _workCode = 110051;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_OPTION_BUTTON: {
                Log.d(TAG, "# 옵션 버튼 클릭");
                if (_shopPageAction.clickOptionButton(_item.item.code3)) {
                    ++_currentPage;
                    _handler.sendEmptyMessageDelayed(TOUCH_COMPANY_CONTENT, MathHelper.randomRange(2000, 3000));
                } else {
                    Log.d(TAG, "# 옵션 버튼 터치 실패로 패턴종료.");
                    _workCode = 110052;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_COMPANY_CONTENT: {
                Log.d(TAG, "# 네이버 쇼핑 가격비교 검사");
                InsideData insideData = _shopPageAction.getContentMid2InsideData(_mid2);
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 가격비교 터치");
                        if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_URL_CHANGE) {
                            String url = _shopPageAction.getMid2Url(_mid2);
                            if (!TextUtils.isEmpty(url)) {
                                webViewLoad(msg, url);
                            } else {
                                Log.d(TAG, "# 네이버 쇼핑 가격비교 터치 실패로 패턴종료.");
                                _workCode = 110063;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                            }
                        } else {
//                            _shopPageAction.changeTargetContentMid2(_mid2);
//                            SystemClock.sleep(1000);

                            if (_shopPageAction.touchContentMid2(_mid2)) {
                                webViewLoading(msg);
                            } else {
                                Log.d(TAG, "# 네이버 쇼핑 가격비교 터치 실패로 패턴종료.");
                                _workCode = 110061;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                            }
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    if (_step == 0) {
                         ++_step;
                        Log.d(TAG, "# 네이버 쇼핑 가격비교 못찾아서 다음으로...");
                        sendMessageDelayed(TOUCH_ALL_COMPANY_BUTTON, 100);
                    } else {
                        if ((_scrollCount2 < 22) && !_shopPageAction.checkPageBottom()) {
                            Log.d(TAG, "# 아래로 스크롤..." + _scrollCount2);
                            ++_scrollCount2;
                            // 페이지 하단이 아니라면 아래로 스크롤한다.
                            _nextMessage = TOUCH_COMPANY_CONTENT;
                            _scrollType = 1;
                            _handler.sendEmptyMessageDelayed(SCROLL_BOTTOM, MathHelper.randomRange(1000, 2000));
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 가격비교 못찾아서 패턴종료.");
                            _workCode = 110062;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    }
                }
                break;
            }

            case TOUCH_ALL_COMPANY_BUTTON: {
                Log.d(TAG, "# 전체 판매처 버튼 검사");
                InsideData insideData = _shopPageAction.getAllCompanyButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 전체 판매처 버튼 터치");
//                        _shopPageAction.changeTargetAllCompanyButton();
//                        SystemClock.sleep(1000);

                        if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_ALL_COMPANY)) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 전체 판매처 버튼 터치 실패로 패턴종료.");
                            _workCode = 110071;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 전체 판매처 버튼 못찾아서 패턴종료.");
                    _workCode = 110072;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case SCROLL_BOTTOM: {
                Log.d(TAG, "# 하단으로 이동");
                if (_scrollType == 0) {
                    _swipeAction.swipeDownFast(65, 80);
                } else {
                    // 네이버 패치로 조금 천천히로 수정 //2022.01.29
//                    _swipeAction.swipeDownFast(45, 55);
                    _swipeAction.swipeDownFast(115, 125);
                }
                _handler.sendEmptyMessageDelayed(_nextMessage, MathHelper.randomRange(3000, 4000));
                break;
            }


//            case FIND_CONTENT: {
//                Log.d(TAG, "# 상품 찾기");
//                _touchUrlPatternAction.workInThread();
//                if (_touchUrlPatternAction.isFind()) {
//                    if(_mid2.equals(".")){
//                        _handler.sendEmptyMessageDelayed(STAY_RANDOM, MathHelper.randomRange(6000, 7000));
//                    }else{
//                        _handler.sendEmptyMessageDelayed(FIND_CONTENT2, MathHelper.randomRange(10000, 11000));
//                    }
//                }else{
//                    if(!_touchUrlPatternAction.not_page()) {
//                        if (page < 7) {
//                             page++;
//                             webViewManager.loadUrl("https://msearch.shopping.naver.com/search/all?query="+ _keyword+"&pagingIndex="+page);
//                            _handler.sendEmptyMessageDelayed(FIND_CONTENT, MathHelper.randomRange(10000, 11000));
//                        } else {
//                            _handler.sendEmptyMessageDelayed(WEB_BACK, MathHelper.randomRange(3000, 6000));
//                        }
//                    }else{
//                        _handler.sendEmptyMessageDelayed(WEB_BACK, MathHelper.randomRange(3000, 6000));
//                }
//
//                }
//                break;
//            }
//
//            case FIND_CONTENT2: {
//                Log.d(TAG, "# 가격비교 찾기");
//                if(count == 0) {
//                    count++;
//                    _touchUrlPatternAction.workInThread2(false);
//                    if(!_touchUrlPatternAction.is_pro2clk()) {
//                        _handler.sendEmptyMessageDelayed(FIND_CONTENT2, MathHelper.randomRange(10000, 11000));
//                    }else{
//                        _handler.sendEmptyMessageDelayed(STAY_RANDOM, MathHelper.randomRange(6000, 7000));
//                    }
//                }else{
//                    if(count < 5) {
//                        _touchUrlPatternAction.workInThread2(true);
//                        if(!_touchUrlPatternAction.is_pro2clk()) {
//                            _handler.sendEmptyMessageDelayed(FIND_CONTENT2, MathHelper.randomRange(10000, 11000));
//                        }else{
//                            _handler.sendEmptyMessageDelayed(STAY_RANDOM, MathHelper.randomRange(6000, 7000));
//                        }
//                        count++;
//                    }else{
//                        _handler.sendEmptyMessageDelayed(WEB_BACK, MathHelper.randomRange(3000, 6000));
//                    }
//                }
//
//                break;
//            }

            // touchUrlPatternAction 을 밖으로 빼기전에는 콜백방식으로 처리한다.
//            case REGISTER_RANK: {
//                Log.d(TAG, "# 순위 등록");
//                _rankPatternAction.registerRank(loginId, imei);
//                _handler.sendEmptyMessageDelayed(STAY_RANDOM, MathHelper.randomRange(2000, 5000));
//                break;
//            }

            case STAY_RANDOM: {
                Log.d(TAG, "# 랜덤 스테이 진행");
                _randomSwipePatternAction.randomSwipe();
                _handler.sendEmptyMessageDelayed(WEB_BACK, MathHelper.randomRange(3000, 6000));
                break;
            }

            case WEB_BACK: {
                Log.d(TAG, "# 웹뷰 뒤로");
                webViewGoBack(msg);
                break;
            }

            case CLEAR_SEARCH_BAR: {
                Log.d(TAG, "# 검색창 지우기");
                ++_searchStep;
//                _searchBarClearPatternAction.keyword = _step == 0 ? _item.search : _item.target;

                if (isShopHome()) {
                    _shopSearchBarClearPatternAction.clearSearchBar();
                    _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(3000, 4000));
                } else {
                    _searchBarClearPatternAction.clearSearchBar();
                    _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(3000, 5000));
                }
                break;
            }

            case TOUCH_LOGO: {
                Log.d(TAG, "# 로고 버튼 터치");
                _action.touchLogoButton();
                webViewLoading(msg);
                break;
            }

            case END_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# 네이버 쇼핑 패턴 종료");
                if (_webViewManager.resetProxy()) {
                    UserManager.getInstance().webProxy = null;
                }

                uploadOtherCookieInWebView();

                if (_isLoginCookieExpired) {
                    _workCode += 5000;
                }

                registerResultFinish();
                _shopSearchBarAction.endPattern();
                _action.endPattern();
                _homeAction.endPattern();
                sendEndPatternMessage();
                break;
            }

            case PAUSE_PATTERN: {
                Log.d(TAG, "# 패턴 중단");
                break;
            }


            //---- Home pan.
            case TOUCH_HOME_PAN_BUTTON: {
                Log.d(TAG, "# 홈 판 버튼 터치");
                _homePickCount = 0;
                if (_homeAction.touchRandomPan()) {
                    webViewLoading(msg);
//                    _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
                } else {
                    if (_homeWaitCount < 10) {
                        ++_homeWaitCount;

                        if (_homeWaitCount % 4 == 0) {
                            Log.d(TAG, "# 홈 판 버튼 터치 실패로 새로고침... " + _homeWaitCount);
                            _handler.sendEmptyMessageDelayed(RUN_NEXT, MathHelper.randomRange(3500, 5000));
                        } else {
                            Log.d(TAG, "# 홈 판 버튼 터치 실패로 3초 후 다시 시도..." + _homeWaitCount);
                            _handler.sendEmptyMessageDelayed(msg.what, 3000);
                        }
                    } else {
                        // 여기에 재시도 만들것. 실패하면 다시 홈로드.
                        Log.d(TAG, "# 홈 판 버튼 터치에 실패해서 패턴종료.");
                        _workCode = 110101;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                }
                // 뉴스 두개 클릭, 클릭후 스크롤
                break;
            }

            case PICK_HOME_PAN_CONTENT: {
                Log.d(TAG, "# 홈 판 컨텐츠 가져오기");
                if (_homeAction.pickRandomItem()) {
                    if (_homeRandomClickWorkCount == 0) {
                        _nextMessage = TOUCH_HOME_PAN_CONTENT;
                        _randomRange = 2;
                        _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(4000, 5000));
                    } else {
                        _handler.sendEmptyMessageDelayed(TOUCH_HOME_PAN_CONTENT, MathHelper.randomRange(2000, 3000));
                    }
                } else {
                    if (_homePickRetryCount < 10) {
                        if (_homePickCount < 5) {
                            Log.d(TAG, "# 홈 판 컨텐츠 가져오기 실패로 3초 후 다시 시도..." + _homePickCount);
                            ++_homePickCount;
                            _handler.sendEmptyMessageDelayed(msg.what, 3000);
                        } else {
                            Log.d(TAG, "# 홈 판 컨텐츠 가져오기 실패로 새로고침");
                            SystemClock.sleep(MathHelper.randomRange(3500, 5000));
                            ++_homePickRetryCount;
                            _webViewManager.reload();
                            webViewLoading(msg);
                        }
                    } else {
                        Log.d(TAG, "# 홈 판 컨텐츠 로딩에러로 처리 중단.");
                        _workCode = 110102;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                }
                // 뉴스 두개 클릭, 클릭후 스크롤
                break;
            }

            case TOUCH_HOME_PAN_CONTENT: {
                Log.d(TAG, "# 홈 판 컨텐츠 검사");
                InsideData insideData = _homeAction.getPanContentInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 홈 판 컨텐츠 터치");
                        if (_homeAction.touchPanContent()) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 홈 판 컨텐츠 터치 실패로 패턴종료.");
                            _workCode = 110110;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 800) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        if (insideData.rect.top < -800) {
                            _swipeAction.swipeUpFast(110, 200);
                        } else {
                            _swipeAction.swipeUp();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 홈 판 컨텐츠 못찾아서 패턴종료.");
                    _workCode = 110111;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_HOME_NEWS_REFRESH_BUTTON: {
                Log.d(TAG, "# 홈 판 뉴스 새로고침 버튼 검사");
                InsideData insideData = _homeAction.getButtonInsideData(NaverHomeAction.BUTTON_NEWS_PAN_REFRESH);
                if (insideData != null) {
                    if (insideData.isInside()) {
                        if (_homeNewsRefreshCount > 0) {
                            Log.d(TAG, "# 홈 판 뉴스 새로고침 버튼 터치: " + _homeNewsRefreshCount);
                            if (_homeAction.touchButton(NaverHomeAction.BUTTON_NEWS_PAN_REFRESH)) {
                                --_homeNewsRefreshCount;
                                _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(2000, 4000));
                            } else {
                                Log.d(TAG, "# 홈 판 뉴스 새로고침 버튼 터치에 실패해서 패턴종료.");
                                _workCode = 110112;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                            }
                        } else {
                            _handler.sendEmptyMessageDelayed(PICK_HOME_PAN_CONTENT, MathHelper.randomRange(4000, 5000));
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 800) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        if (insideData.rect.top < -800) {
                            _swipeAction.swipeUpFast(110, 200);
                        } else {
                            _swipeAction.swipeUp();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 홈 판 뉴스 새로고침 버튼 못찾아서 패턴종료.");
                    _workCode = 110113;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_HOME_ECONOMY_CHART_BUTTON: {
                Log.d(TAG, "# 홈 판 경제 주요지표 버튼 검사");
                InsideData insideData = _homeAction.getButtonInsideData(NaverHomeAction.BUTTON_NEWS_PAN_REFRESH);
                if (insideData != null) {
                    if (insideData.isInside()) {
                        if (_homeNewsRefreshCount > 0) {
                            Log.d(TAG, "# 홈 판 뉴스 새로고침 버튼 터치: " + _homeNewsRefreshCount);
                            if (_homeAction.touchButton(NaverHomeAction.BUTTON_NEWS_PAN_REFRESH)) {
                                --_homeNewsRefreshCount;
                                _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(2000, 4000));
                            } else {
                                Log.d(TAG, "# 홈 판 뉴스 새로고침 버튼 터치에 실패해서 패턴종료.");
                                _workCode = 110115;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                            }
                        } else {
                            _handler.sendEmptyMessageDelayed(PICK_HOME_PAN_CONTENT, MathHelper.randomRange(4000, 5000));
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 800) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        if (insideData.rect.top < -800) {
                            _swipeAction.swipeUpFast(110, 200);
                        } else {
                            _swipeAction.swipeUp();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 홈 판 뉴스 새로고침 버튼 못찾아서 패턴종료.");
                    _workCode = 110116;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_SEARCH_BAR_HOME_BUTTON: {
                Log.d(TAG, "# 검색창 네이버 홈 버튼 터치");
                if (_searchBarAction.touchHomeButton()) {
                    webViewLoading(msg);
                } else {
                    Log.d(TAG, "# 검색창 네이버 홈 버튼 터치에 실패해서 패턴종료.");
                    _workCode = 110114;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_TOP_VIEW_BUTTON: {
                Log.d(TAG, "# 상단 뷰탭 버튼 검사");
                InsideData insideData = _homeAction.getTopViewButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 상단 뷰탭 버튼 터치");
                        if (_homeAction.touchButton(NaverHomeAction.BUTTON_TOP_VIEW)) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 상단 뷰탭 버튼 터치 실패로 패턴종료.");
                            _workCode = 110120;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 상단 뷰탭 버튼 못찾아서 패턴종료.");
                    _workCode = 110121;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case PICK_VIEW_CONTENT: {
                Log.d(TAG, "# 뷰 컨텐츠 가져오기");
                if (_homeAction.hasView()) {
                    if (_homeAction.pickRandomViewItem()) {
                        _handler.sendEmptyMessageDelayed(TOUCH_VIEW_CONTENT, MathHelper.randomRange(2000, 3000));
                    } else {
                        if (_homePickRetryCount < 5) {
                            if (_homePickCount < 5) {
                                Log.d(TAG, "# 뷰 컨텐츠 가져오기 실패로 3초 후 다시 시도..." + _homePickCount);
                                ++_homePickCount;
                                _handler.sendEmptyMessageDelayed(msg.what, 3000);
                            } else {
                                Log.d(TAG, "# 뷰 컨텐츠 가져오기 실패로 새로고침");
                                SystemClock.sleep(MathHelper.randomRange(3500, 5000));
                                ++_homePickRetryCount;
                                _webViewManager.reload();
                                webViewLoading(msg);
                            }
                        } else {
                            Log.d(TAG, "# 뷰 컨텐츠 로딩에러로 처리 중단.");
                            _workCode = 110122;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    }
                } else {
                    _isViewFail = true;
                    _isSiteSearch = true;
                    Log.d(TAG, "# 뷰 컨텐츠 가져오기 실패로 다음으로...");
                    _handler.sendEmptyMessageDelayed(VIEW_WEB_BACK, 100);
                }
                break;
            }

            case TOUCH_VIEW_CONTENT: {
                Log.d(TAG, "# 뷰 컨텐츠 검사");
                InsideData insideData = _homeAction.getViewContentInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 뷰 컨텐츠 터치");
                        if (_homeAction.touchViewContent()) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 뷰 컨텐츠 터치 실패로 패턴종료.");
                            _workCode = 110123;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 800) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _isLastUp = false;
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        if (insideData.rect.top < -800) {
                            _swipeAction.swipeUpFast(110, 200);
                        } else {
                            _swipeAction.swipeUp();
                        }
                        _isLastUp = true;
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 뷰 컨텐츠 못찾아서 패턴종료.");
                    _workCode = 110124;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case PICK_SITE_CONTENT: {
                Log.d(TAG, "# 사이트 컨텐츠 가져오기");
                if (_homeAction.pickRandomSiteItem()) {
                    _handler.sendEmptyMessageDelayed(TOUCH_SITE_CONTENT, MathHelper.randomRange(2000, 3000));
                } else {
                    if (_homePickRetryCount < 10) {
                        if (_homePickCount < 5) {
                            Log.d(TAG, "# 사이트 컨텐츠 가져오기 실패로 3초 후 다시 시도..." + _homePickCount);
                            ++_homePickCount;
                            _handler.sendEmptyMessageDelayed(msg.what, 3000);
                        } else {
                            Log.d(TAG, "# 사이트 컨텐츠 가져오기 실패로 새로고침");
                            SystemClock.sleep(MathHelper.randomRange(3500, 5000));
                            ++_homePickRetryCount;
                            _webViewManager.reload();
                            webViewLoading(msg);
                        }
                    } else {
                        Log.d(TAG, "# 사이트 컨텐츠 로딩에러로 처리 중단.");
                        _workCode = 110130;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                }
                // 뉴스 두개 클릭, 클릭후 스크롤
                break;
            }

            case TOUCH_SITE_CONTENT: {
                Log.d(TAG, "# 사이트 컨텐츠 검사");
                InsideData insideData = _homeAction.getSiteContentInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 사이트 컨텐츠 터치");
                        if (_homeAction.touchSiteContent()) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 사이트 컨텐츠 터치 실패로 패턴종료.");
                            _workCode = 110131;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 800) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _isLastUp = false;
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        if (insideData.rect.top < -800) {
                            _swipeAction.swipeUpFast(110, 200);
                        } else {
                            _swipeAction.swipeUp();
                        }
                        _isLastUp = true;
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 사이트 컨텐츠 못찾아서 패턴종료.");
                    _workCode = 110132;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case HOME_PAN_WEB_BACK: {
                Log.d(TAG, "# 홈 판 웹뷰 뒤로");
                webViewGoBack(msg);
                break;
            }

            case VIEW_WEB_BACK: {
                Log.d(TAG, "# 뷰 웹뷰 뒤로");
                webViewGoBack(msg);
                break;
            }

            case WEB_BACK_NEXT: {
                Log.d(TAG, "# 웹뷰 뒤로 다음처리");
                webViewGoBack(msg);
                break;
            }
        }
    }

    @Override
    public void onPageLoaded(String url) {
        super.onPageLoaded(url);

        switch (_lastMessage) {
            case GO_PRE_SEND_WORKING: {
                Log.d(TAG, "# 작업 정보 등록 후 동작");
                SystemClock.sleep(2000);
                _webViewManager.goBlankPage();

                if (_item.item.account != null) {
                    _accountItem = _item.item.account;
                    _handler.sendEmptyMessageDelayed(GO_LOGIN, 1000);
                } else {
                    _handler.sendEmptyMessageDelayed(RUN_NEXT, 1000);
                }
                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 홈 이동 후 동작");

                if (_runHomePattern) {
                    _handler.sendEmptyMessageDelayed(TOUCH_HOME_PAN_BUTTON, MathHelper.randomRange(5500, 7500));
                } else {
                    _findBarCount = 0;
//                _handler.sendEmptyMessageDelayed(TOUCH_NEW_POPUP_OK, MathHelper.randomRange(5000, 6000));
                    _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
//                SystemClock.sleep(5000);
//                _shopPageAction.printHtml();
                }
                break;
            }

            case GO_SEARCH_HOME_EMPTY: {
                Log.d(TAG, "# 홈 빈 검색어 이동 후 동작");
                _findBarCount = 0;
//                _handler.sendEmptyMessageDelayed(TOUCH_NEW_POPUP_OK, MathHelper.randomRange(5000, 6000));
                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
                break;
            }

            case GO_SHOP_HOME: {
                Log.d(TAG, "# 쇼핑홈 이동 후 동작");
                _findBarCount = 0;
//                SystemClock.sleep(5000);
//                _shopPageAction.printLocalStorage();
                _handler.sendEmptyMessageDelayed(TOUCH_POPUP_CLOSE_BUTTON, MathHelper.randomRange(5000, 6000));
//                _handler.sendEmptyMessageDelayed(TOUCH_SHOP_HOME_SEARCH_BUTTON, MathHelper.randomRange(5000, 6000));
                break;
            }

            case GO_SHOP_HOME_DIRECT: {
                Log.d(TAG, "# 쇼핑홈 검색어 이동 후 동작");
                _findBarCount = 0;
                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(5000, 6000));
                break;
            }

            case GO_SHOP_BUY_PAGE: {
                Log.d(TAG, "# 네이버 쇼핑 사러가기 이동 후 동작");
                _findBarCount = 0;
                _handler.sendEmptyMessageDelayed(TOUCH_GO_TO_BUY_BUTTON, MathHelper.randomRange(4000, 6000));
                break;
            }

            case GO_IMAGE_BUY_PAGE: {
                Log.d(TAG, "# 네이버 이미지 사러가기 이동 후 동작");
                _imageWaitCount = 0;
                _handler.sendEmptyMessageDelayed(CHECK_IMAGE_POPUP, MathHelper.randomRange(4000, 6000));
                break;
            }

            case GO_SHOP_PLAN_SUB_PAGE: {
                Log.d(TAG, "# 네이버 쇼핑 기획전 링크 이동 후 동작");
                _handler.sendEmptyMessageDelayed(AFTER_ACTION_PLAN_SUB_PAGE, MathHelper.randomRange(4000, 6000));
                break;
            }

            case GO_GOOGLE_HOME: {
                Log.d(TAG, "# 구글홈 이동 후 동작");
                _handler.sendEmptyMessageDelayed(AFTER_ACTION_PLAN_SUB_PAGE, MathHelper.randomRange(4000, 6000));
                break;
            }

            case GO_SHOPPING_LIVE_URL: {
                Log.d(TAG, "# 쇼핑 라이브 URL로 이동 후 동작");
                int nextMessage = RANDOM_SCROLL;
                long delay = 10000 + MathHelper.randomRange(3000, 5000);

                _result = ResultAction.SUCCESS;
                _workCode = 110908;
                _workCodeAddition = -300;

                if (_runAfterShowPattern) {
                    _randomRange = 6;

                    int pickNumber = (int) MathHelper.randomRange(1, 3);

                    if (pickNumber == 1) {
                        _nextMessage = TOUCH_TAB_REVIEW_BUTTON;
                    } else if (pickNumber == 2) {
                        _nextMessage = TOUCH_TAB_QNA_BUTTON;
                    } else {
                        _result = ResultAction.SUCCESS;
                        _workCode = 110931;
                        _nextMessage = END_PATTERN;
//                        _nextMessage = WEB_BACK;
                    }
                } else if (_item.item.randomScrollCount < 0) {
                    nextMessage = END_PATTERN;
                    _workCode = 110909;
                    delay = 10000 + MathHelper.randomRange(4000, 10000);
                    Log.d(TAG, "# 대기: " + delay + "ms");
                }

                _handler.sendEmptyMessageDelayed(nextMessage, delay);
                break;
            }

//            case FIND_KEYWORD: {
//                Log.d(TAG, "# 키워드 검색 후 동작");
//                _handler.sendEmptyMessageDelayed(FIND_CONTENT, MathHelper.randomRange(5000, 6000));
//                break;
//            }


//            case TOUCH_SEARCH_BUTTON: {
//                Log.d(TAG, "# 검색버튼 터치 후 동작");
//                _randomSwipePatternAction.randomSwipe();
//                _handler.sendEmptyMessageDelayed(FIND_CONTENT, 1000);
//                break;
//            }

//            case TOUCH_SEARCH_BUTTON: {
//                Log.d(TAG, "# 검색버튼 터치 후 동작");
//                _handler.sendEmptyMessageDelayed(FIND_CONTENT, 7000);
//                break;
//            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치 후 동작");
                // 웹뷰 사이즈 재계산을 위해서 넣어줌.
                _shopPageAction.getWebViewWindowSize(true);
                int next = CLEAR_SEARCH_BAR;

                if (isShopHome()) {
                    next = TOUCH_SEARCH_BAR_FOR_CLEAR;
                }

                if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_BACK) {
                    if (_searchStep == 0 && !TextUtils.isEmpty(_item.item.search1)) {
                        _handler.sendEmptyMessageDelayed(next, MathHelper.randomRange(5000, 7000));
                    } else if (_searchStep == 1) {
                        _handler.sendEmptyMessageDelayed(next, MathHelper.randomRange(5000, 7000));
                    } else if (_searchStep == 2 && !TextUtils.isEmpty(_item.item.searchMain)) {
                        ++_searchStep;
                        _nextMessage = WEB_BACK;
                        _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                    } else {
                        Log.d(TAG, "# 알 수 없는 상태로 패턴종료.");
                        _workCode = 110081;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                } else if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_RANDOM) {
                    if (_searchStep == 0 && !TextUtils.isEmpty(_item.item.search1)) {
                        _handler.sendEmptyMessageDelayed(next, MathHelper.randomRange(2000, 3000));
                    } else if (_searchStep == 1 && !TextUtils.isEmpty(_item.item.search2)) {
                        _handler.sendEmptyMessageDelayed(next, MathHelper.randomRange(2000, 3000));
                    } else {
                        _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, 1000);
                    }
                } else if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_AI_NEWS_VIEW) {
                    boolean inside = false;
                    InsideData insideData = _homeAction.getTopButtonInsideData(NaverHomeAction.BUTTON_TOP_VIEW);
                    if (insideData != null && insideData.isInside()) {
                        if (insideData.rect.right < 300) {
                            inside = true;
                        }
                    }

                    if (inside) {
                        sendMessageDelayed(TOUCH_TOP_VIEW_BUTTON, MathHelper.randomRange(1000, 3000));
                    } else {
                        _nextMessage = TOUCH_TOP_VIEW_BUTTON;
                        sendMessageDelayed(TOUCH_TOP_DOTS_BUTTON, MathHelper.randomRange(1000, 3000));
                    }
                } else if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_URL_CHANGE) {
                    _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(1000, 2000));
                } else {
                    next = TOUCH_CONTENT;

                    if (_startHomeMode == 5) {
                        next = TOUCH_MORE_BUTTON;
                    }

                    _handler.sendEmptyMessageDelayed(next, MathHelper.randomRange(1000, 2000));
                }
                break;
            }

            case INPUT_KEYWORD: {
                Log.d(TAG, "# 검색창 검사 새로고침 후 동작");
                int next = TOUCH_SEARCH_BAR;

//                if (isShopHome()) {
//                    next = TOUCH_SHOP_HOME_SEARCH_BUTTON;
//                }

                _handler.sendEmptyMessageDelayed(next, MathHelper.randomRange(5000, 6000));
                break;
            }

            case TOUCH_BACK_BUTTON: {
                Log.d(TAG, "# 백 버튼 터치 후 동작");
                _workCode = 110901;
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_GO_TO_BUY_BUTTON: {
                Log.d(TAG, "# 네이버 쇼핑 사러가기 컨텐츠 터치 후 동작");
                int nextMessage = RANDOM_SCROLL;
                long delay = MathHelper.randomRange(3000, 5000);

                _result = ResultAction.SUCCESS;
                _workCode = 110908;
                _workCodeAddition = -100;

                if (_runAfterShowPattern) {
                    _randomRange = 6;

                    int pickNumber = (int) MathHelper.randomRange(1, 3);

                    if (pickNumber == 1) {
                        _nextMessage = TOUCH_TAB_REVIEW_BUTTON;
                    } else if (pickNumber == 2) {
                        _nextMessage = TOUCH_TAB_QNA_BUTTON;
                    } else {
                        _result = ResultAction.SUCCESS;
                        _workCode = 110931;
                        _nextMessage = END_PATTERN;
//                        _nextMessage = WEB_BACK;
                    }
                } else if (_item.item.randomScrollCount < 0) {
                    nextMessage = END_PATTERN;
                    _workCode = 110909;
                    delay = MathHelper.randomRange(4000, 15000);
                    Log.d(TAG, "# 대기: " + delay + "ms");
                }

                _handler.sendEmptyMessageDelayed(nextMessage, delay);
                break;
            }

            case TOUCH_GO_TO_IMAGE_BUY_BUTTON: {
                Log.d(TAG, "# 네이버 이미지 사러가기 컨텐츠 터치 후 동작");
                int nextMessage = RANDOM_SCROLL;
                long delay = MathHelper.randomRange(3000, 5000);

                _result = ResultAction.SUCCESS;
                _workCode = 110908;
                _workCodeAddition = -200;

                if (_runAfterShowPattern) {
                    _randomRange = 6;

                    int pickNumber = (int) MathHelper.randomRange(1, 3);

                    if (pickNumber == 1) {
                        _nextMessage = TOUCH_TAB_REVIEW_BUTTON;
                    } else if (pickNumber == 2) {
                        _nextMessage = TOUCH_TAB_QNA_BUTTON;
                    } else {
                        _result = ResultAction.SUCCESS;
                        _workCode = 110931;
                        _nextMessage = END_PATTERN;
//                        _nextMessage = WEB_BACK;
                    }
                } else if (_item.item.randomScrollCount < 0) {
                    nextMessage = END_PATTERN;
                    _workCode = 110909;
                    delay = MathHelper.randomRange(4000, 15000);
                    Log.d(TAG, "# 대기: " + delay + "ms");
                }

                _handler.sendEmptyMessageDelayed(nextMessage, delay);
                break;
            }

            case GO_SHOP_CONTENT_URL:
            case TOUCH_CONTENT: {
                switch (_lastMessage) {
                    case TOUCH_GO_TO_BUY_BUTTON:
                        Log.d(TAG, "# 네이버 쇼핑 사러가기 컨텐츠 터치 후 동작");
                        break;

                    case GO_SHOP_CONTENT_URL:
                        Log.d(TAG, "# 쇼핑 컨텐츠 URL로 이동 후 동작");
                        break;

                    default:
                        Log.d(TAG, "# 네이버 쇼핑 컨텐츠 터치 후 동작");
                }

                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT_CHECK, 5000);
                break;
            }

            case TOUCH_TOP_SHOPPING_BUTTON: {
                Log.d(TAG, "# 상단 쇼핑탭 버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_MORE_BUTTON: {
                Log.d(TAG, "# 더보기 버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_NEXT_BUTTON: {
                Log.d(TAG, "# 다음 버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_COMPANY_CONTENT: {
                Log.d(TAG, "# 네이버 쇼핑 가격비교 터치 후 동작");
                int nextMessage = RANDOM_SCROLL;
                long delay = MathHelper.randomRange(3000, 5000);

//                _result = 1;
                _result = ResultAction.SUCCESS;

                if (_runAfterShowPattern) {
                    _randomRange = 6;
                    nextMessage = TOUCH_SHOW_CONTENT_MORE;
                } else {
                    // 임시로 적용. 원래는 뒤로가기 였다.
                    _nextMessage = END_PATTERN;
                    _workCode = 110913;
                    delay = MathHelper.randomRange(4000, 10000);
                    Log.d(TAG, "# 대기: " + delay + "ms");

//                    _nextMessage = WEB_BACK;
//                    _nextMessage = TOUCH_BACK_BUTTON;
                }

                _handler.sendEmptyMessageDelayed(nextMessage, delay);
                break;
            }

            case TOUCH_ALL_COMPANY_BUTTON: {
                Log.d(TAG, "# 전체 판매처 버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(TOUCH_COMPANY_CONTENT, MathHelper.randomRange(3000, 5000));
                break;
            }

            case WEB_BACK: {
                Log.d(TAG, "# 웹뷰 뒤로 후 동작");
                if (_randomClickWorkCount < _randomClickCount) {
                    // 마지막 작업은 원래 mid로 한다.
                    if (_randomMids.size() >= _randomClickCount) {
                        _mid = _item.mid1;
                    } else {
                        _foundRandomItem = false;
                    }

                    ++_randomClickWorkCount;
                    Log.d(TAG, "# 랜덤 클릭 작업수: " + _randomClickWorkCount + " / " + _randomClickCount);
                    _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(4000, 5000));
                } else {
                    SystemClock.sleep(MathHelper.randomRange(1000, 2500));
                    _webViewManager.goBack();

                    if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_BACK) {
                        if (_searchStep < 4) {
                            ++_searchStep;
                            _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(4000, 5000));
                        } else {
                            _workCode = 110905;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
                        }
                    } else if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_AI_NEWS_VIEW) {
                        _workCode = 110906;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
                    } else if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_URL_CHANGE) {
                        _workCode = 110907;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
                    } else {
                        if (_randomMids.size() > 0) {
                            _workCode = 110904;
                        } else {
                            _workCode = 110902;
                        }
                        _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
                    }
                }
                break;
            }

            case TOUCH_LOGO: {
                Log.d(TAG, "# 로고 버튼 터치 후 동작");
                _workCode = 110903;
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                break;
            }


            //---- Home pan.
            case TOUCH_HOME_PAN_BUTTON: {
                Log.d(TAG, "# 홈 판 버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(PICK_HOME_PAN_CONTENT, MathHelper.randomRange(3000, 5000));
//                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
//                _homeNewsRefreshCount = 2;
//                _handler.sendEmptyMessageDelayed(TOUCH_HOME_NEWS_REFRESH_BUTTON, MathHelper.randomRange(3000, 5000));
                break;
            }

            case PICK_HOME_PAN_CONTENT: {
                Log.d(TAG, "# 홈 판 컨텐츠 가져오기 새로고침 후 동작");
                _handler.sendEmptyMessageDelayed(PICK_HOME_PAN_CONTENT, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_HOME_PAN_CONTENT: {
                Log.d(TAG, "# 홈 판 컨텐츠 터치 후 동작");
                ++_homeRandomClickWorkCount;
//                _result = 1;
//                if (_homeRandomClickWorkCount >= _homeRandomClickCount) {
//                } else {
                    _nextMessage = HOME_PAN_WEB_BACK;
//                    _nextMessage = TOUCH_BACK_BUTTON;
//                }
                _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(4000, 6000));
                break;
            }

            case TOUCH_SEARCH_BAR_HOME_BUTTON: {
                Log.d(TAG, "# 검색창 네이버 홈 버튼 터치 후 동작");
//                _handler.sendEmptyMessageDelayed(PICK_HOME_PAN_CONTENT, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_TOP_VIEW_BUTTON: {
                Log.d(TAG, "# 상단 뷰탭 버튼 터치 후 동작");
                _homePickCount = 0;
                _handler.sendEmptyMessageDelayed(PICK_VIEW_CONTENT, MathHelper.randomRange(3000, 5000));
                break;
            }

            case PICK_VIEW_CONTENT: {
                Log.d(TAG, "# 뷰 컨텐츠 가져오기 새로고침 후 동작");
                _handler.sendEmptyMessageDelayed(PICK_VIEW_CONTENT, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_VIEW_CONTENT: {
                Log.d(TAG, "# 뷰 컨텐츠 터치 후 동작");
                _nextMessage = VIEW_WEB_BACK;
                _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(4000, 6000));
                break;
            }

            case HOME_PAN_WEB_BACK: {
                Log.d(TAG, "# 홈 판 웹뷰 뒤로 후 동작");
                if (_homeRandomClickWorkCount < _homeRandomClickCount) {
                    Log.d(TAG, "# 홈 판 랜덤 클릭 작업수: " + _homeRandomClickWorkCount + " / " + _homeRandomClickCount);

                    switch (_homeAction.getPanMode()) {
                        case NaverHomeAction.BUTTON_ECONOMY_PAN:
                            _handler.sendEmptyMessageDelayed(TOUCH_HOME_ECONOMY_CHART_BUTTON, MathHelper.randomRange(4000, 6000));
                            break;

                        case NaverHomeAction.BUTTON_SPORTS_PAN:
                            break;

                        case NaverHomeAction.BUTTON_NEWS_PAN:
                        default:
                            int min = 0;
                            if (_homeRandomClickWorkCount > 1) {
                                min = 1;
                            }
                            _homeNewsRefreshCount = (int) MathHelper.randomRange(min, 5);
                            _handler.sendEmptyMessageDelayed(TOUCH_HOME_NEWS_REFRESH_BUTTON, MathHelper.randomRange(4000, 6000));
                            break;
                    }
                } else {
                    _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
                }
                break;
            }

            case PICK_SITE_CONTENT: {
                Log.d(TAG, "# 사이트 컨텐츠 가져오기 새로고침 후 동작");
                _handler.sendEmptyMessageDelayed(PICK_SITE_CONTENT, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_SITE_CONTENT: {
                Log.d(TAG, "# 사이트 컨텐츠 터치 후 동작");
                _nextMessage = VIEW_WEB_BACK;
                _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(4000, 6000));
                break;
            }

            case VIEW_WEB_BACK: {
                Log.d(TAG, "# 뷰 웹뷰 뒤로 후 동작");
                if (_isViewFail) {
                    _isViewFail = false;
                    _homePickCount = 0;
                    _handler.sendEmptyMessageDelayed(PICK_SITE_CONTENT, MathHelper.randomRange(3000, 5000));
                } else {
                    SystemClock.sleep(MathHelper.randomRange(1500, 2500));
                    int count = (int) MathHelper.randomRange(_isLastUp ? 0 : 1, 3);

                    for (int i = 0; i < count; ++i) {
                        Log.d(TAG, "위로 스크롤");
                        _swipeAction.swipeUp(false);
                        SystemClock.sleep(MathHelper.randomRange(1300, 2500));
                    }

                    _startHomeMode = _item.item.shopHome;

                    if (isShopHome()) {
                        boolean inside = false;
                        InsideData insideData = _homeAction.getTopButtonInsideData(NaverHomeAction.BUTTON_TOP_SHOPPING);
                        if (insideData != null && insideData.isInside()) {
                            if (insideData.rect.right < 300) {
                                inside = true;
                            }
                        }

                        if (inside) {
                            sendMessageDelayed(TOUCH_TOP_SHOPPING_BUTTON, MathHelper.randomRange(3000, 5000));
                        } else {
                            _nextMessage = TOUCH_TOP_SHOPPING_BUTTON;
                            sendMessageDelayed(TOUCH_TOP_DOTS_BUTTON, MathHelper.randomRange(3000, 5000));
                        }
                    } else {
                        if (_isSiteSearch) {
                            _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(4000, 5000));
                        } else {
                            // 뷰탭에서 통합탭으로 한번더 뒤로 간다.
                            _nextMessage = TOUCH_CONTENT;
                            _handler.sendEmptyMessageDelayed(WEB_BACK_NEXT, MathHelper.randomRange(3000, 5000));
                        }
                    }
                }
                break;
            }

            case WEB_BACK_NEXT: {
                Log.d(TAG, "# 웹뷰 뒤로 다음처리");
                _handler.sendEmptyMessageDelayed(_nextMessage, MathHelper.randomRange(4000, 6000));
                break;
            }
        }

        _lastMessage = -1;
    }

    public boolean isShopHome() {
        return _startHomeMode == 1 || _startHomeMode == 3;
    }

    public void inputKeyword(String keyword) {
        if (_item.item.workType == KeywordItem.WORK_TYPE_INPUT) {
            Log.d(TAG, "# 검색어 삽입: " + keyword);
            // 인풋태그에 값 넣기
            if (isShopHome()) {
                _shopPageAction.inputSearchBar(keyword);
            } else {
                _homeAction.inputSearchBar(_startHomeMode == 0, keyword);
            }

            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1500, 3000));
        } else if (_item.item.workType == KeywordItem.WORK_TYPE_CLIPBOARD) {
            Log.d(TAG, "# 검색어 클립보드 복사: " + keyword);
            _action.copyToClipboard(_webViewManager.getWebView().getContext(), keyword);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SystemClock.sleep(MathHelper.randomRange(1000, 1500));
                Log.d(TAG, "# 검색어 붙여넣기");
                _action.pasteClipboard();
            } else {
                Log.d(TAG, "# 검색창 롱터치");
                if (isShopHome()) {
                    if (!_shopPageAction.touchSearchBarLong()) {
                        Log.d(TAG, "# 검색창 롱터치에 실패해서 패턴종료.");
                        _workCode = 110013;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        return;
                    }

                    SystemClock.sleep(MathHelper.randomRange(1000, 1500));
                    Log.d(TAG, "# 검색어 붙여넣기");
                    _shopPageAction.touchPasteButton();
                } else {
                    if (!_homeAction.touchSearchBarLong(_startHomeMode == 0)) {
                        Log.d(TAG, "# 검색창 롱터치에 실패해서 패턴종료.");
                        _workCode = 110014;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        return;
                    }

                    SystemClock.sleep(MathHelper.randomRange(1000, 1500));
                    Log.d(TAG, "# 검색어 붙여넣기");
                    _homeAction.touchPasteButton();
                }
            }

            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1500, 3000));
        } else if (_item.item.workType == 5) {
            // 추가 가능한 기능있으면 추가 예정..
        } else {
            Log.d(TAG, "# 검색어 입력: " + keyword);
            _action.inputKeywordForTyping(keyword);
            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1000, 3000));
        }
    }
}
