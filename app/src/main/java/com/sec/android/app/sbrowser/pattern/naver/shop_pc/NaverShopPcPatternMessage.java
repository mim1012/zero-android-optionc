package com.sec.android.app.sbrowser.pattern.naver.shop_pc;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

import com.sec.android.app.sbrowser.BuildConfig;
import com.sec.android.app.sbrowser.engine.Config;
import com.sec.android.app.sbrowser.engine.CookieFileManager;
import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;
import com.sec.android.app.sbrowser.models.KeywordItem;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.models.NnbData;
import com.sec.android.app.sbrowser.pattern.RandomSwipePatternAction;
import com.sec.android.app.sbrowser.pattern.action.NaverCookieOtherAction;
import com.sec.android.app.sbrowser.pattern.action.NaverCookieStatusAction;
import com.sec.android.app.sbrowser.pattern.action.ResultAction;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.naver.NaverHomeAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.NaverPcSearchBarAction;

public class NaverShopPcPatternMessage extends NaverPatternMessage {

    private static final String TAG = NaverShopPcPatternMessage.class.getSimpleName();

    protected static final int GO_PRE_SEND_WORKING = 39;
    protected static final int GO_SHOP_HOME = 40;
    protected static final int GO_SHOP_BUY_PAGE = 42;
    protected static final int GO_IMAGE_BUY_PAGE = 43;
    protected static final int GO_SHOP_PLAN_SUB_PAGE = 46;
    private static final int RANDOM_SCROLL = 50;
    private static final int TOUCH_BACK_BUTTON = RANDOM_SCROLL + 1;
    private static final int TOUCH_CONTENT = RANDOM_SCROLL + 2;
    private static final int TOUCH_GO_TO_BUY_BUTTON = RANDOM_SCROLL + 3;
    private static final int CHECK_IMAGE_POPUP = RANDOM_SCROLL + 4;
    private static final int TOUCH_GO_TO_IMAGE_BUY_BUTTON = RANDOM_SCROLL + 5;
    private static final int AFTER_ACTION_PLAN_SUB_PAGE = RANDOM_SCROLL + 6;
    private static final int TOUCH_MORE_BUTTON = AFTER_ACTION_PLAN_SUB_PAGE + 1;
    private static final int TOUCH_MORE2_BUTTON = AFTER_ACTION_PLAN_SUB_PAGE + 2;
    private static final int TOUCH_NEXT_BUTTON = AFTER_ACTION_PLAN_SUB_PAGE + 3;
    private static final int TOUCH_NEXT_BUTTON_WAITING = AFTER_ACTION_PLAN_SUB_PAGE + 4;
    private static final int TOUCH_COMPANY_CONTENT = AFTER_ACTION_PLAN_SUB_PAGE + 5;
    private static final int TOUCH_COMPANY_NEXT_BUTTON = AFTER_ACTION_PLAN_SUB_PAGE + 6;
    private static final int SCROLL_BOTTOM = AFTER_ACTION_PLAN_SUB_PAGE + 7;

    private final NaverPatternAction _action;
    private final NaverHomeAction _homeAction;
    private final NaverShopPcPageAction _shopPageAction;
    private final RandomSwipePatternAction _randomSwipePatternAction;
    private final SwipeThreadAction _swipeAction;
    private final NaverPcSearchBarAction _searchBarAction;
    protected final NaverShopPcSearchBarAction _shopSearchBarAction;
    protected final NaverCookieStatusAction _cookieStatusAction;
    protected final NaverCookieOtherAction _cookieOtherAction;

    private int _nextMessage = 0;
    private int _currentPage = 0;
    private int _nextPage = 0;
    private int _nextCompanyPage = 0;
    private int _step = 0;
    private int _findBarCount = 0;
    private int _waitCount = 0;
    private int _imageWaitCount = 0;
    private String _keyword;
    private String _mid;
    private String _mid2;
    private NnbData _nnbData = null;
    private boolean _isLoginCookieExpired = false;
    private int _scrollType = 0;
    private int _scrollCount1 = 0;
    private int _scrollCount2 = 0;

    private int _startHomeMode = 0;
    private boolean _workMore = true;

    public NaverShopPcPatternMessage(WebViewManager manager, KeywordItemMoon item) {
        super(manager);
        _item = item;
        _keyword = item.keyword;
        _mid = item.mid1;
        _mid2 = item.mid2;
        _startHomeMode = item.shopHome;
        _workMore = (item.item.workMore == 1);

        _action = new NaverPatternAction(manager.getWebView());
        _homeAction = new NaverHomeAction(manager.getWebView());
        _shopPageAction = new NaverShopPcPageAction(manager.getWebView());
        _randomSwipePatternAction = new RandomSwipePatternAction(manager.getWebView().getContext());
        _searchBarAction = new NaverPcSearchBarAction(manager.getWebView());
        _shopSearchBarAction = new NaverShopPcSearchBarAction(manager.getWebView());

        _cookieStatusAction = new NaverCookieStatusAction(manager.getWebView().getContext());
        _cookieOtherAction = new NaverCookieOtherAction(manager.getWebView().getContext());

        TouchInjector injector = new TouchInjector(manager.getWebView().getContext());
        injector.setSoftKeyboard(new SamsungKeyboard());

        _swipeAction = new SwipeThreadAction(injector);
        getResultAction().item = item;
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 네이버 쇼핑 PC 작업 시작");
                _nnbData = UserManager.getInstance().nnbData;

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
//                } else if (_startHomeMode == 2) {
//                    _handler.sendEmptyMessage(GO_HOME_DIRECT);
//                } else if (_startHomeMode == 3 || _startHomeMode == 4) {
//                    _handler.sendEmptyMessage(GO_SHOP_HOME_DIRECT);
                } else {
                    if (_item.item.workType >= 300) {
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
                webViewLoad(msg, Config.HOME_URL);
                break;
            }

            case GO_SHOP_HOME: {
                Log.d(TAG, "# 네이버 쇼핑 PC홈으로 이동");
                webViewLoad(msg, "https://shopping.naver.com/");
                break;
            }

            case GO_SHOP_BUY_PAGE: {
                Log.d(TAG, "# 네이버 쇼핑 PC 사러가기로 이동");
                // 가격 비교도 단일처럼 처리한다.
                if (!_mid2.equals(".")) {
                    _mid = _mid2;
                    _mid2 = ".";
                }

                webViewLoad(msg, "https://search.shopping.naver.com/product/" + _mid);
                break;
            }

            case GO_IMAGE_BUY_PAGE: {
                Log.d(TAG, "# 네이버 PC 이미지 사러가기로 이동");
                // 가격 비교도 단일처럼 처리한다.
                if (!_mid2.equals(".")) {
                    _mid = _mid2;
                    _mid2 = ".";
                }


//                https://search.naver.com/search.naver?sm=tab_hty.top&where=image&ssc=tab.image.all&query=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80&oquery=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80&tqi=iFPOCdqVN8Vsshe5dHossssst0Z-096302&nso=so%3Ar%2Ca%3Aall%2Cp%3Aall#lens_kr_shp_product%%3A88016271952
//                https://search.naver.com/search.naver?where=image&query=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80&oquery=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80&tqi=iFPOCdqVN8Vsshe5dHossssst0Z-096302&nso=so%3Ar%2Ca%3Aall%2Cp%3Aall#lens_kr_shp_product%%3A88016271952
                String parsed = WebViewManager.keywordEncodeForNaver(_keyword);
                String url = String.format("https://search.naver.com/search.naver?where=image&mode=column&section=nshopping&query=%s&nso_open=1&pq=#imgId=lens_kr_shp_product%%3A%s", parsed, _mid);
                webViewLoad(msg, url);
                break;
            }

            case GO_SHOP_PLAN_SUB_PAGE: {
                Log.d(TAG, "# 네이버 쇼핑 PC 기획전 상세 상품으로 이동");
                String url = String.format("https://smartstore.naver.com/inflow/outlink/product?p=%s&tr=sld&trx=%s", _item.item.productId, _item.item.planId);
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

            case TOUCH_SEARCH_BAR: {
                Log.d(TAG, "# 검색창 터치");
                if (isShopHome()) {
                    if (_shopPageAction.touchSearchBar()) {
                        _waitCount = 0;
                        _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
                    } else {
                        Log.d(TAG, "# 검색창 터치에 실패해서 패턴종료.");
                        _workCode = 111002;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                    }
                } else {
                    if (_homeAction.touchPcSearchBar(true)) {
                        _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
                    } else {
                        Log.d(TAG, "# 검색창 터치에 실패해서 패턴종료.");
                        _workCode = 111001;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                    }
                }
                break;
            }

            case INPUT_KEYWORD: {
                Log.d(TAG, "# 검색창 검사");
                if (isShopHome()) {
                    inputKeyword(_keyword);
//                    Log.d(TAG, "# 검색어 입력: " + _keyword);
//                    _action.extractStrings(_keyword);
//                    _action.inputKeyword();
//                    _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1000, 3000));
                } else {
                    _searchBarCheckPatternAction.checkPcSearchBarShown();
                    if (!_searchBarCheckPatternAction.isFocus()) {
                        if (_findBarCount > 3) {
                            Log.d(TAG, "# 로딩에러로 처리 중단.");
                            _workCode = 111011;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                        } else {
                            Log.d(TAG, "# 검색창이 떠있지 않아서 다시 터치");
                            ++_findBarCount;
                            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, 5000);
                        }
                    } else {
                            inputKeyword(_keyword);
//                        Log.d(TAG, "# 검색어 입력: " + _keyword);
//                        _action.extractStrings(_keyword);
//                        _action.inputKeyword();
//                        _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1000, 3000));
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
                    _action.touchSearchButton();
//                    _shopSearchBarAction.clickSearchButton();
                    SystemClock.sleep(800);
                } else {
                    _currentPage = 1;
                    _searchBarAction.submitSearchButton();
//                    _action.touchSearchButton();
                }
                webViewLoading(msg);
                break;
            }

            case RANDOM_SCROLL: {
                Log.d(TAG, "# 랜덤 스크롤");
                int count = (int) MathHelper.randomRange(8, 10);

                for (int i = 0; i < count; ++i) {
                    if (i < 4) {
                        Log.d(TAG, "아래로 스크롤");
                        _swipeAction.swipeDown(false);
                    } else {
                        int isUp = (int) MathHelper.randomRange(0, 1);

                        if (isUp == 0) {
                            Log.d(TAG, "아래로 스크롤");
                            _swipeAction.swipeDown(false);
                        } else {
                            Log.d(TAG, "위로 스크롤");
                            _swipeAction.swipeUp(false);
                        }
                    }

                    SystemClock.sleep(MathHelper.randomRange(1300, 2500));
                }

                _handler.sendEmptyMessageDelayed(_nextMessage, MathHelper.randomRange(1000, 3000));
                break;
            }

            case TOUCH_BACK_BUTTON: {
                Log.d(TAG, "# 백 버튼 터치");
                pressBackButton();
                webViewLoading(msg);
                break;
            }

            case TOUCH_CONTENT: {
                Log.d(TAG, "# 네이버 쇼핑 PC 컨텐츠 검사");
                InsideData insideData = _shopPageAction.getContentMidInsideData(_mid, (_currentPage <= 1));
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 PC 컨텐츠 터치");
                        if (_shopPageAction.touchContentMid(_mid, (_currentPage <= 1))) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 PC 컨텐츠 터치 실패로 패턴종료.");
                            _workCode = 111021;
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
                    Log.d(TAG, "# 네이버 쇼핑 PC 컨텐츠 못찾아서 다음으로...");
                    if (_currentPage <= 1) {
                        ++_currentPage;
                        sendMessageDelayed(TOUCH_MORE2_BUTTON, 100);
                    } else {
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
                    }
                }
                break;
            }

            case TOUCH_GO_TO_BUY_BUTTON: {
                Log.d(TAG, "# 네이버 쇼핑 PC 사러가기 버튼 검사");
                if (_nnbData != null && !TextUtils.isEmpty(_nnbData.nidSes)) {
                    CookieManager cookieManager = CookieManager.getInstance();
                    String cookies = cookieManager.getCookie(".naver.com");

                    if (!cookies.contains("NID_AUT") && !cookies.contains("NID_SES")) {
                        Log.d(TAG, "# 로그인 쿠키가 유요하지 않아 상태 업로드");
                        _cookieStatusAction.registerNaverCookieStatus(_nnbData.loginCookieId, 2);
                        _isLoginCookieExpired = true;
                    }
                }

                InsideData insideData = _shopPageAction.getGoToBuyButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 PC 사러가기 버튼 터치");
                        if (_shopPageAction.touchButton(NaverShopPcPageAction.BUTTON_GO_TO_BUY)) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 PC 사러가기 버튼 터치 실패로 패턴종료.");
                            _workCode = 111019;
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
                    Log.d(TAG, "# 네이버 쇼핑 PC 사러가기 버튼 없어서 패턴종료.");
                    _workCode = 111022;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 1000);
                }
                break;
            }

            case CHECK_IMAGE_POPUP: {
                Log.d(TAG, "# 네이버 PC 이미지 뷰어 떠있는지 검사");
                InsideData insideData = _shopPageAction.getImageViewerInsideData();
                if (insideData != null) {
                    Log.d(TAG, "# 네이버 PC 이미지 뷰어 확인");
                    _handler.sendEmptyMessageDelayed(TOUCH_GO_TO_IMAGE_BUY_BUTTON, MathHelper.randomRange(1000, 3000));
                } else {
                    if (_imageWaitCount < 20) {
                        Log.d(TAG, "# 네이버 PC 이미지 뷰어 확인 실패로 3초 후 다시 시도..." + _imageWaitCount);
                        ++_imageWaitCount;
                        _handler.sendEmptyMessageDelayed(msg.what, 3000);
                    } else {
                        Log.d(TAG, "# 네이버 PC 이미지 뷰어 확인 실패로 패턴종료.");
                        _workCode = 111016;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 1000);
                    }
                }
                break;
            }

            case TOUCH_GO_TO_IMAGE_BUY_BUTTON: {
                Log.d(TAG, "# 네이버 PC 이미지 사러가기 버튼 검사");
                uploadLoginCookieStatusInWebView();

                InsideData insideData = _shopPageAction.getGoToImageBuyButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 PC 이미지 사러가기 버튼 터치");
                        _webViewManager.setLoadsImagesAutomatically(false);
                        if (_shopPageAction.touchButton(NaverShopPcPageAction.BUTTON_GO_TO_IMAGE_BUY)) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 네이버 PC 이미지 사러가기 버튼 터치 실패로 패턴종료.");
                            _workCode = 111017;
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
                    Log.d(TAG, "# 네이버 PC 이미지 사러가기 버튼 없어서 패턴종료.");
                    _workCode = 111018;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 1000);
                }
                break;
            }

            case AFTER_ACTION_PLAN_SUB_PAGE: {
                Log.d(TAG, "# 네이버 쇼핑 PC 기획전 후처리");
                if (_nnbData != null && !TextUtils.isEmpty(_nnbData.nidSes)) {
                    CookieManager cookieManager = CookieManager.getInstance();
                    String cookies = cookieManager.getCookie(".naver.com");

                    if (!cookies.contains("NID_AUT") && !cookies.contains("NID_SES")) {
                        Log.d(TAG, "# 로그인 쿠키가 유요하지 않아 상태 업로드");
                        _cookieStatusAction.registerNaverCookieStatus(_nnbData.loginCookieId, 2);
                        _isLoginCookieExpired = true;
                    }
                }

                int nextMessage = RANDOM_SCROLL;
                long delay = MathHelper.randomRange(3000, 5000);

                _result = ResultAction.SUCCESS;
                _workCode = 111911;

                if (_item.item.randomScrollCount < 0) {
                    nextMessage = END_PATTERN;
                    _workCode = 111912;
                    delay = MathHelper.randomRange(4000, 12000);
                    Log.d(TAG, "# 대기: " + delay + "ms");
                }

                _handler.sendEmptyMessageDelayed(nextMessage, delay);
                break;
            }

            case TOUCH_MORE2_BUTTON: {
                Log.d(TAG, "# 아래로 더보기 버튼 검사");
                InsideData insideData = _shopPageAction.getMore2ButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 아래로 더보기 버튼 터치");
                        if (_shopPageAction.touchButton(NaverShopPcPageAction.BUTTON_MORE2)) {
                            sendMessageDelayed(TOUCH_MORE_BUTTON, 3000);
                        } else {
                            Log.d(TAG, "# 아래로 더보기 버튼 터치 실패로 패턴종료.");
                            _workCode = 111033;
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
                    sendMessageDelayed(TOUCH_MORE_BUTTON, 100);
                }
                break;
            }

            case TOUCH_MORE_BUTTON: {
                Log.d(TAG, "# 더보기 버튼 검사");
                InsideData insideData = _shopPageAction.getMoreButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 더보기 버튼 터치");
                        if (_shopPageAction.touchButton(NaverShopPcPageAction.BUTTON_MORE)) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 더보기 버튼 터치 실패로 패턴종료.");
                            _workCode = 111031;
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
                    _workCode = 111032;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_NEXT_BUTTON: {
                Log.d(TAG, "# 다음 버튼 검사");
                InsideData insideData = _shopPageAction.getNextButtonInsideData();
                if (insideData != null) {
                    String page =  _shopPageAction.getCurrentPage();
                    if (page == null) {
                        Log.d(TAG, "# 현재 페이지를 못찾아서 패턴종료.");
                        _workCode = 111041;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    } else if (Integer.parseInt(page.replaceAll("\\D", "")) > 7) {
                        Log.d(TAG, "# 7페이지 초과로 패턴종료.");
                        _workCode = 111042;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    } else {
                        if (insideData.isInside()) {
                            Log.d(TAG, "# 다음 버튼 터치");
                            _scrollCount1 = 0;
                            int pageNumber = Integer.parseInt(page.replaceAll("\\D", ""));

                            if (_nextPage == pageNumber) {
                                Log.d(TAG, "# 현재 페이지에만 머물러 있어서 패턴종료.");
                                _workCode = 111044;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                            } else {
                                _nextPage = pageNumber;
                                webViewLoading(msg);
                                _handler.sendEmptyMessageDelayed(TOUCH_NEXT_BUTTON_WAITING, 100);
//                            if (_shopPageAction.touchButton(NaverShopPageAction.BUTTON_NEXT)) {
////                                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(5000, 6000));
//                            } else {
//                                Log.d(TAG, "# 다음 버튼 터치 실패로 패턴종료.");
//                                _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
//                            }
                            }
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
                    _workCode = 111043;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_NEXT_BUTTON_WAITING: {
                Log.d(TAG, "# 다음 버튼 터치2");
                if (_shopPageAction.touchButton(NaverShopPcPageAction.BUTTON_NEXT)) {
//                    _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(5000, 6000));
                } else {
                    Log.d(TAG, "# 다음 버튼 터치 실패로 패턴종료.");
                    _workCode = 111051;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_COMPANY_CONTENT: {
                Log.d(TAG, "# 네이버 쇼핑 PC 가격비교 검사");
                InsideData insideData = _shopPageAction.getContentMid2InsideData(_mid2);
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 PC 가격비교 터치");
                        if (_shopPageAction.touchContentMid2(_mid2)) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 PC 가격비교 터치 실패로 패턴종료.");
                            _workCode = 111061;
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
                    Log.d(TAG, "# 네이버 쇼핑 PC 가격비교 못찾아서 다음으로...");
                    sendMessageDelayed(TOUCH_COMPANY_NEXT_BUTTON, 100);
                }
                break;
            }

            case TOUCH_COMPANY_NEXT_BUTTON: {
                Log.d(TAG, "# 가격비교 다음 페이지 버튼 검사");
                String pageString =  _shopPageAction.getCurrentCompanyPage();
                if (pageString == null) {
                    Log.d(TAG, "# 현재 페이지를 못찾아서 패턴종료.");
                    _workCode = 111071;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                } else {
                    int page = Integer.parseInt(pageString.replaceAll("\\D", ""));
                    if (page > 10) {
                        Log.d(TAG, "# 10페이지 초과로 패턴종료.");
                        _workCode = 111072;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    } else {
                        int nextPage = page + 1;
                        InsideData insideData = _shopPageAction.getCompanyPageButtonInsideData(nextPage);
                        if (insideData != null) {
                            if (insideData.isInside()) {
                                Log.d(TAG, "# " + nextPage + "페이지 버튼 터치");
                                if (_shopPageAction.touchCompanyPageButton(nextPage)) {
                                    if (_nextCompanyPage == nextPage) {
                                        Log.d(TAG, "# 현재 페이지에만 머물러 있어서 패턴종료.");
                                        _workCode = 111074;
                                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                                    } else {
                                        _nextCompanyPage = nextPage;
                                        _handler.sendEmptyMessageDelayed(TOUCH_COMPANY_CONTENT, MathHelper.randomRange(3000, 5000));
                                    }
                                } else {
                                    Log.d(TAG, "# " + nextPage + "페이지 버튼 터치 실패로 패턴종료.");
                                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                                }
                            } else if (insideData.inside > 0) {
                                Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                                if (insideData.rect.top > 1500) {
                                    _swipeAction.swipeDownFast(110, 200);
                                } else {
                                    _swipeAction.swipeDown();
                                }
                                // 부분 로딩이 있기 때문에 스크롤 하고 체크한다.
//                                _handler.sendEmptyMessageDelayed(TOUCH_COMPANY_CONTENT, MathHelper.randomRange(1000, 2000));
                                _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                            } else {
                                Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                                if (insideData.rect.top < -2000) {
                                    _swipeAction.swipeUpFast(110, 200);
                                } else {
                                    _swipeAction.swipeUp();
                                }
                                _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                            }
                        } else {
                            Log.d(TAG, "# " + nextPage + "페이지 버튼 못찾아서 패턴종료.");
                            _workCode = 111073;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    }
                }
                break;
            }

            case SCROLL_BOTTOM: {
                Log.d(TAG, "# 하단으로 이동");
                if (_scrollType == 0) {
                    _swipeAction.swipeDownFast(90, 110);
                } else {
                    _swipeAction.swipeDownFast(45, 55);
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

            case TOUCH_LOGO: {
                Log.d(TAG, "# 로고 버튼 터치");
                _action.touchLogoButton();
                webViewLoading(msg);
                break;
            }

            case END_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# 네이버 쇼핑 PC 패턴 종료");
                if (_webViewManager.resetProxy()) {
                    UserManager.getInstance().webProxy = null;
                }

                // 쿠키 정보 있으면 업로드.
                if (_nnbData != null && _nnbData.naverCookieId > 0) {
                    CookieFileManager manager = new CookieFileManager();
                    String others = manager.getAllCookieString(_webViewManager.getWebView().getContext(), ".naver.com");
                    if (!TextUtils.isEmpty(others)) {
                        _cookieOtherAction.registerNaverCookieOthers(_nnbData.naverCookieId, others);
                    }
                }

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
//                _webViewManager.loadUrl("https://api.myip.la/en?json");

                if (_item.item.account != null) {
                    _accountItem = _item.item.account;
                    _handler.sendEmptyMessageDelayed(GO_LOGIN, 1000);
                } else {
                    _handler.sendEmptyMessageDelayed(RUN_NEXT, 1000);
                }
                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 홈이동 후 동작");
                _findBarCount = 0;
//                _handler.sendEmptyMessageDelayed(TOUCH_NEW_POPUP_OK, MathHelper.randomRange(5000, 6000));
                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
                break;
            }

            case GO_SHOP_HOME: {
                Log.d(TAG, "# 쇼핑홈이동 후 동작");
                _findBarCount = 0;
//                _handler.sendEmptyMessageDelayed(TOUCH_NEW_POPUP_OK, MathHelper.randomRange(5000, 6000));
                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
                break;
            }

            case GO_SHOP_BUY_PAGE: {
                Log.d(TAG, "# 네이버 쇼핑 PC 사러가기 이동 후 동작");
                _findBarCount = 0;
                _handler.sendEmptyMessageDelayed(TOUCH_GO_TO_BUY_BUTTON, MathHelper.randomRange(3000, 6500));
                break;
            }

            case GO_IMAGE_BUY_PAGE: {
                Log.d(TAG, "# 네이버 PC 이미지 사러가기 이동 후 동작");
                _imageWaitCount = 0;
                _handler.sendEmptyMessageDelayed(CHECK_IMAGE_POPUP, MathHelper.randomRange(4000, 6000));
                break;
            }

            case GO_SHOP_PLAN_SUB_PAGE: {
                Log.d(TAG, "# 네이버 쇼핑 PC 기획전 링크 이동 후 동작");
                _handler.sendEmptyMessageDelayed(AFTER_ACTION_PLAN_SUB_PAGE, MathHelper.randomRange(4000, 6000));
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
//                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(3000, 6000));
                _webViewManager.getWebView().post(new Runnable() {
                    @Override
                    public void run() {
                        _webViewManager.getWebView().setInitialScale(-1);
                    }
                });
                _currentPage = 2;

                _handler.sendEmptyMessageDelayed(TOUCH_MORE_BUTTON, MathHelper.randomRange(3000, 4000));
//                _handler.sendEmptyMessageDelayed(REGISTER_FINISH, 1000);
                break;
            }

            case INPUT_KEYWORD: {
                Log.d(TAG, "# 검색창 검사 새로고침 후 동작");
                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
                break;
            }

            case TOUCH_BACK_BUTTON: {
                Log.d(TAG, "# 백 버튼 터치 후 동작");
                _workCode = 111901;
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_GO_TO_BUY_BUTTON: {
                Log.d(TAG, "# 네이버 쇼핑 PC 사러가기 컨텐츠 터치 후 동작");
                int nextMessage = RANDOM_SCROLL;
                long delay = MathHelper.randomRange(3000, 5000);

                _result = ResultAction.SUCCESS;
                _workCode = 111908;

                if (_item.item.randomScrollCount < 0) {
                    nextMessage = END_PATTERN;
                    _workCode = 111909;
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
                _workCode = 111908;
                _workCodeAddition = -200;

//                if (_runAfterShowPattern) {
//                    _randomRange = 6;
//
//                    int pickNumber = (int) MathHelper.randomRange(1, 3);
//
//                    if (pickNumber == 1) {
//                        _nextMessage = TOUCH_TAB_REVIEW_BUTTON;
//                    } else if (pickNumber == 2) {
//                        _nextMessage = TOUCH_TAB_QNA_BUTTON;
//                    } else {
//                        _result = ResultAction.SUCCESS;
//                        _workCode = 110931;
//                        _nextMessage = END_PATTERN;
////                        _nextMessage = WEB_BACK;
//                    }
//                } else
                if (_item.item.randomScrollCount < 0) {
                    nextMessage = END_PATTERN;
                    _workCode = 111909;
                    delay = MathHelper.randomRange(4000, 15000);
                    Log.d(TAG, "# 대기: " + delay + "ms");
                }

                _handler.sendEmptyMessageDelayed(nextMessage, delay);
                break;
            }

            case TOUCH_CONTENT: {
                Log.d(TAG, "# 네이버 쇼핑 PC 컨텐츠 터치 후 동작");
//                _result = 1;
                if (!_mid2.equals(".")) {
                    _nextMessage = TOUCH_COMPANY_CONTENT;
                } else {
                    _result = ResultAction.SUCCESS;
                    _nextMessage = WEB_BACK;
//                    _nextMessage = TOUCH_BACK_BUTTON;
                }
                _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
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
                Log.d(TAG, "# 네이버 쇼핑 PC 가격비교 터치 후 동작");
//                _result = 1;
                _result = ResultAction.SUCCESS;
                _nextMessage = WEB_BACK;
//                _nextMessage = TOUCH_BACK_BUTTON;
                _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(4000, 6000));
                break;
            }

            case WEB_BACK: {
                Log.d(TAG, "# 웹뷰 뒤로 후 동작");
                _workCode = 111902;
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
                break;
            }

            case TOUCH_LOGO: {
                Log.d(TAG, "# 로고 버튼 터치 후 동작");
                _workCode = 111903;
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
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
