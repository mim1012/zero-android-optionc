package com.sec.android.app.sbrowser.pattern.naver.place;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.Config;
import com.sec.android.app.sbrowser.engine.MathHelper;
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
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NaverPlacePatternMessage extends NaverPatternMessage {

    private static final String TAG = NaverPlacePatternMessage.class.getSimpleName();
    private static final int FIND_KEYWORD = 45;
    private static final int FIND_CONTENT = 46;
    private static final int RUN_AFTER = 47;

    private static final int RANDOM_SCROLL = 50;
    private static final int TOUCH_BACK_BUTTON = RANDOM_SCROLL + 1;
    private static final int TOUCH_CONTENT = RANDOM_SCROLL + 3;
    private static final int TOUCH_OPEN_MORE_BUTTON = RANDOM_SCROLL + 4;
    private static final int TOUCH_MORE_BUTTON = RANDOM_SCROLL + 5;
    private static final int CHECK_MORE_LOADED = RANDOM_SCROLL + 6;
    private static final int TOUCH_SHOW_LIST_BUTTON = RANDOM_SCROLL + 7;
    private static final int TOUCH_COMPANY_CONTENT = RANDOM_SCROLL + 9;
    private static final int TOUCH_ALL_COMPANY_BUTTON = RANDOM_SCROLL + 10;
    private static final int SCROLL_BOTTOM = RANDOM_SCROLL + 11;

    protected static final int TOUCH_PHONE = 1042;


    private final NaverPatternAction _action;
    private final NaverHomeAction _homeAction;
    private final NaverPlacePageAction _placePageAction;
    private final NaverPlacePhoneAction _phoneAction;
    private final NaverPlaceTouchUrlPatternAction _touchUrlPatternAction;
    protected final NaverPlaceResultAction _resultPatternAction;

    private final NaverPlaceAfterPatternAction _afterPatternAction;
    private final RandomSwipePatternAction _randomSwipePatternAction;
    private final SwipeThreadAction _swipeAction;

    private KeywordItemMoon _item = null;

    private int _nextMessage = 0;
    private int _currentPage = 0;
    private int _step = 0;
    private int _searchStep = 0;
    private int _findBarCount = 0;
    String _keyword;
    String _code;
    private int _contentCount = 0;
    private int _sameContentCount = 0;
    private int _loadWaitCount = 0;

    private InsideData _prevInsideData = null;

    public NaverPlacePatternMessage(WebViewManager manager, KeywordItemMoon item) {
        super(manager);
        _item = item;
        _keyword = item.keyword;
        _code = item.code;

        _action = new NaverPatternAction(manager.getWebView());
        _homeAction = new NaverHomeAction(manager.getWebView());
        _placePageAction = new NaverPlacePageAction(manager.getWebView());
        _phoneAction = new NaverPlacePhoneAction(manager.getWebView());
        _touchUrlPatternAction = new NaverPlaceTouchUrlPatternAction(manager.getWebView(), _code);
        _touchUrlPatternAction.item = item;

        _resultPatternAction = new NaverPlaceResultAction();
        _resultPatternAction.item = item;

        _afterPatternAction = new NaverPlaceAfterPatternAction(manager.getWebView(), _code);
        _randomSwipePatternAction = new RandomSwipePatternAction(manager.getWebView().getContext());

        TouchInjector injector = new TouchInjector(manager.getWebView().getContext());
        injector.setSoftKeyboard(new SamsungKeyboard());

        _swipeAction = new SwipeThreadAction(injector);
        getResultAction().item = item;

//        if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_BACK) {
////            Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_SHOP_ABC_BACK");
//        } else
        if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_RANDOM) {
            Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_SHOP_ABC_RANDOM");

            if (!TextUtils.isEmpty(item.item.search2)) {
                _keyword = item.item.search2;
            }
        } else {
            Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_NORMAL");
        }
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 네이버 플레이스 작업 시작");
                _searchStep = 0;
//                _handler.sendEmptyMessage(FIND_KEYWORD);
//                _handler.sendEmptyMessage(GO_HOME);

                int _startHomeMode = _item.shopHome;

                if (_startHomeMode == 3 || _startHomeMode == 4) {
                    _handler.sendEmptyMessage(GO_SEARCH_HOME_DIRECT);
                } else {
                    _handler.sendEmptyMessage(GO_HOME);
                }
                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 네이버 홈으로 이동");
                webViewLoad(msg, Config.NAVER_HOME_MOBILE_URL);
                break;
            }

            case GO_SEARCH_HOME_DIRECT: {
                Log.d(TAG, "# 네이버 홈 검색결과로 이동");
                try {
                    _keyword = URLEncoder.encode(_keyword, "UTF-8");
                } catch (UnsupportedEncodingException e) {

                }

//                _webViewManager.loadUrl("http://google.com");
                //랜덤
                webViewLoad(msg, "https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m&query=" + _keyword);
//                webViewLoad(msg, "https://m.search.naver.com/search.naver?sm=mtp_sly.hst&where=m&query=" + _keyword + "&acr=1");
//                https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m&query=%ED%99%8D%EC%9D%80%EB%8F%99+%EC%B9%B4%ED%8E%98
//                https://m.search.naver.com/search.naver?sm=mtp_sly.hst&where=m&query=%ED%99%8D%EC%9D%80%EB%8F%99+%EC%B9%B4%ED%8E%98&acr=1
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

                if (_homeAction.touchSearchBar(true)) {
                    _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
                } else {
                    Log.d(TAG, "# 검색창 터치에 실패해서 패턴종료.");
                    _workCode = 130001;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case INPUT_KEYWORD: {
                Log.d(TAG, "# 검색창 검사");
//                if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_BACK) {
//                    if (_searchStep == 0 && !TextUtils.isEmpty(_item.item.search1)) {
//                        _keyword = _item.item.search1;
//                    } else if (_searchStep == 1) {
//                        _keyword = _item.keyword;
//                    } else if (_searchStep == 2 && !TextUtils.isEmpty(_item.item.searchMain)) {
//                        _keyword = _item.item.searchMain;
//                    } else {
//                        _keyword = _item.keyword;
//                    }
//                } else
                if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_RANDOM) {
                    if (_searchStep == 0 && !TextUtils.isEmpty(_item.item.search2)) {
                        _keyword = _item.item.search2;
                    } else {
                        _keyword = _item.keyword;
                    }
                }

                _searchBarCheckPatternAction.checkSearchBarShown();

                if (!_searchBarCheckPatternAction.isFocus()) {
                    if (_findBarCount > 15) {
                        Log.d(TAG, "# 로딩에러로 처리 중단.");
                        _workCode = 130011;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    } else {
                        Log.d(TAG, "# 검색창이 떠있지 않아서 빈영역 터치 후 다시 터치");
                        _homeAction.touchEmptyArea();
                        ++_findBarCount;
                        _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(3500, 5000));
                    }
                } else {
                    inputKeyword();
                }
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치");
                _currentPage = 1;
                _searchBarAction.submitSearchButton();
//                _action.touchSearchButton();
                webViewLoading(msg);
                break;
            }

            case RANDOM_SCROLL: {
                Log.d(TAG, "# 랜덤 스크롤");
                int count = (int) MathHelper.randomRange(6, 10);

                for (int i = 0; i < count; ++i) {
                    if (i < 3) {
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
                Log.d(TAG, "# 네이버 플레이스 컨텐츠 검사");
                InsideData insideData = _placePageAction.getContentCodeInsideData(_code);
                if (insideData != null) {
                    // 이동되지 않았다는 것이다.
                    if (_prevInsideData != null && insideData.rect.top == _prevInsideData.rect.top) {
                        Log.d(TAG, "# 목록이 보이지 않아서 목록버튼 다시 터치");
                        _handler.sendEmptyMessageDelayed(TOUCH_SHOW_LIST_BUTTON, MathHelper.randomRange(1000, 2000));
                    } else {
                        _prevInsideData = insideData;

                        if (insideData.isInside()) {
                            Log.d(TAG, "# 네이버 플레이스 컨텐츠 터치");
                            if (_placePageAction.touchContentCode(_code)) {
                                webViewLoading(msg);
                            } else {
                                Log.d(TAG, "# 네이버 플레이스 컨텐츠 터치 실패로 패턴종료.");
                                _workCode = 130021;
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
                    }
                } else {
                    Log.d(TAG, "# 네이버 플레이스 컨텐츠 못찾아서 다음으로...");
                    if (_currentPage <= 1) {
                        if (_placePageAction.hasOpenMoreButton(_item.url)) {
                            sendMessageDelayed(TOUCH_OPEN_MORE_BUTTON, 100);
                        } else {
                            ++_currentPage;
                            sendMessageDelayed(TOUCH_MORE_BUTTON, 100);
                        }
                    } else {
                        int currentCount = _placePageAction.getContentCount();
                        Log.d(TAG, "# 현재 노드: " + currentCount);
                        if (currentCount < 100) {
                            if (_contentCount != currentCount) {
                                Log.d(TAG, "# 아래로 스크롤");
                                _contentCount = currentCount;
                                _sameContentCount = 0;
                                _swipeAction.swipeDownFast(110, 200);
                                _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                            } else {
                                Log.d(TAG, "# 이전에 검사한 개수가 같아서 아래로 스크롤..." + _sameContentCount);
                                if (_sameContentCount < 20) {
                                    ++_sameContentCount;
                                    _swipeAction.swipeDownFast(110, 200);
                                    _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                                } else {
                                    Log.d(TAG, "# 페이지 하단으로 판단되어 패턴 종료..." + _sameContentCount);
                                    _workCode = 130023;
                                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                                }
                            }
                        } else {
                            Log.d(TAG, "# 100위 초과로 패턴종료.");
                            _workCode = 130022;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    }
                }
                break;
            }

            case TOUCH_OPEN_MORE_BUTTON: {
                Log.d(TAG, "# 펼쳐서 더보기 버튼 검사");
                InsideData insideData = _placePageAction.getOpenMoreButtonInsideData(_item.url);
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 펼쳐서 더보기 버튼 터치");
                        if (_placePageAction.touchOpenMoreButton(_item.url)) {
                            _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, 3000);
                        } else {
                            Log.d(TAG, "# 펼쳐서 더보기 버튼 터치 실패로 패턴종료.");
                            _workCode = 130033;
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
                    Log.d(TAG, "# 펼쳐서 더보기 버튼 못찾아서 다음으로.");
                    _handler.sendEmptyMessageDelayed(TOUCH_MORE_BUTTON, MathHelper.randomRange(3000, 5000));
                }
                break;
            }

            case TOUCH_MORE_BUTTON: {
                Log.d(TAG, "# 더보기 버튼 검사");
                InsideData insideData = _placePageAction.getMoreButtonInsideData(_item.url);
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 더보기 버튼 터치");
                        if (_placePageAction.touchMoreButton(_item.url)) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 더보기 버튼 터치 실패로 패턴종료.");
                            _workCode = 130031;
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
                    _workCode = 130032;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case CHECK_MORE_LOADED: {
                Log.d(TAG, "# 네이버 플레이스 더보기 내용 로딩 검사");
                if (_placePageAction.checkMorePageLoaded()) {
//                    if (_placePageAction.checkShowListButtonClick()) {
//                        _handler.sendEmptyMessageDelayed(TOUCH_SHOW_LIST_BUTTON, MathHelper.randomRange(3000, 5000));
//                    } else {
                        _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(3000, 5000));
//                    }
                } else {
                    Log.d(TAG, "# 네이버 플레이스 더보기 내용 로딩되지 않아 대기..." + _loadWaitCount);
                    if (_loadWaitCount < 6) {
                        ++_loadWaitCount;
                        // 페이지 하단이 아니라면 아래로 스크롤한다.
                        _handler.sendEmptyMessageDelayed(msg.what, 5000);
                    } else {
                        Log.d(TAG, "# 5번 초과로 패턴종료.");
                        _workCode = 130041;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                }
                break;
            }

            case TOUCH_SHOW_LIST_BUTTON: {
                Log.d(TAG, "# 목록보기 버튼 터치");
                if (_placePageAction.touchButton(NaverPlacePageAction.BUTTON_SHOW_LIST)) {
                    _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(3000, 4000));
                } else {
                    Log.d(TAG, "# 목록보기 버튼 터치 실패로 패턴종료.");
                    _workCode = 130051;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }



            case FIND_CONTENT: {
                Log.d(TAG, "# 컨텐츠 찾기");
                // 클래스 내부 작업을 쪼개야한다..ㅠㅠ
                _touchUrlPatternAction.workInThread();
                if (_touchUrlPatternAction.isFind()) {
//                    _handler.sendEmptyMessageDelayed(REGISTER_RANK, MathHelper.randomRange(6000, 9000));
                    _handler.sendEmptyMessageDelayed(STAY_RANDOM, MathHelper.randomRange(6000, 7000));
                } else {
                    // 컨텐츠를 찾지 못했을때.
                    if (_step == 0) {
                        _touchUrlPatternAction.setMoreView(true);
                        // 위에서 더보기를 누른 상태이므로 로딩까지 다시 기다린다.
                        _handler.sendEmptyMessageDelayed(FIND_CONTENT, MathHelper.randomRange(10000, 11000));
                        _step++;
                    } else {
                        _handler.sendEmptyMessageDelayed(TOUCH_LOGO, MathHelper.randomRange(2000, 3000));
                    }
                }
                break;
            }

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

//                _handler.sendEmptyMessageDelayed(TOUCH_PHONE, MathHelper.randomRange(3000, 6000));
//                if ((int)MathHelper.randomRange(0, 1) == 1) {
//                    _handler.sendEmptyMessageDelayed(TOUCH_PHONE, MathHelper.randomRange(3000, 6000));
//                } else {
                    _handler.sendEmptyMessageDelayed(WEB_BACK, MathHelper.randomRange(3000, 6000));
//                }
                break;
            }

            case TOUCH_PHONE: {
                Log.d(TAG, "# 전화 버튼 터치 진행");
                if (_phoneAction.touchPhoneButton()) {
                    SystemClock.sleep(MathHelper.randomRange(10000, 12000));
                    _action.touchBackButton();
//                    AppHelper.checkAppRunning(_webViewManager.getWebView().getContext(), BuildConfig.APPLICATION_ID);
                    _handler.sendEmptyMessageDelayed(WEB_BACK, MathHelper.randomRange(3000, 6000));
                } else {
                    Log.d(TAG, "# 전화 버튼 터치에 실패해서 위로 스크롤 후 다시 시도..." + _retryCount);
                    _swipeAction.swipeUpAi();
                    if (!resendMessageDelayed(msg.what, 2000, 5)) {
                        Log.d(TAG, "# 전화 버튼 터치에 실패해서 다음으로..");
                        _handler.sendEmptyMessageDelayed(WEB_BACK, 100);
                    }
                }
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
                _searchBarClearPatternAction.clearSearchBar();
                _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(3000, 5000));
                break;
            }

            case RUN_AFTER: {
                Log.d(TAG, "# 후행 작업 진행");
                _afterPatternAction.workInThread();
                _handler.sendEmptyMessageDelayed(TOUCH_LOGO, MathHelper.randomRange(2000, 3000));
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
                Log.d(TAG, "# 네이버 플레이스 키워드 패턴 종료");
                uploadOtherCookieInWebView();

                if (_isLoginCookieExpired) {
                    _workCode += 5000;
                }

                registerResultFinish();
                _action.endPattern();
                _homeAction.endPattern();
                _touchUrlPatternAction.endPattern();
                _afterPatternAction.endPattern();

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
            case GO_HOME: {
                Log.d(TAG, "# 홈이동 후 동작");
                _findBarCount = 0;
//                _handler.sendEmptyMessageDelayed(TOUCH_NEW_POPUP_OK, MathHelper.randomRange(5000, 6000));
                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
                break;
            }

            case GO_SEARCH_HOME_DIRECT: {
                Log.d(TAG, "# 네이버 홈 검색결과 이동 후 동작");
                if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_RANDOM) {
                    if (_searchStep == 0 && !TextUtils.isEmpty(_item.item.search2)) {
                        _nextMessage = CLEAR_SEARCH_BAR;
                        _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
//                        _handler.sendEmptyMessageDelayed(CLEAR_SEARCH_BAR, MathHelper.randomRange(2000, 3000));
                    } else {
                        _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(2000, 5000));
                    }
                } else {
                    _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(4000, 6000));
                }
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치 후 동작");
                if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_RANDOM) {
                    if (_searchStep == 0 && !TextUtils.isEmpty(_item.item.search2)) {
                        if (sendPacketLcs(_placePageAction)) {
                            _nextMessage = CLEAR_SEARCH_BAR;
                            _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
//                        _handler.sendEmptyMessageDelayed(CLEAR_SEARCH_BAR, MathHelper.randomRange(2000, 3000));
                        } else {
                            Log.d(TAG, "# 통신 오류로 패턴종료.");
                            _workCode = 130911;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                        }
                    } else {
                        if (sendPacketLcs(_placePageAction)) {
                            _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(2000, 5000));
                        } else {
                            Log.d(TAG, "# 통신 오류로 패턴종료.");
                            _workCode = 130912;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                        }
                    }
                } else {
//                    if (sendPacketLcs(_placePageAction)) {
                        _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(2000, 5000));
//                    } else {
//                        Log.d(TAG, "# 통신 오류로 패턴종료.");
//                        _workCode = 130913;
//                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
//                    }
                }
                break;
            }

            case FIND_KEYWORD: {
                Log.d(TAG, "# 키워드 검색 로딩 후 동작");
                _handler.sendEmptyMessageDelayed(REGISTER_FINISH, 1000);
                break;
            }

            case TOUCH_BACK_BUTTON: {
                Log.d(TAG, "# 백 버튼 터치 후 동작");
                _workCode = 130901;
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_CONTENT: {
                Log.d(TAG, "# 네이버 플레이스 컨텐츠 터치 후 동작");
//                _result = 1;
//                if (!_mid2.equals(".")) {
//                    _nextMessage = TOUCH_COMPANY_CONTENT;
//                } else {
                    _result = ResultAction.SUCCESS;
                    _nextMessage = WEB_BACK;
//                    _nextMessage = TOUCH_BACK_BUTTON;
//                }
                _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_OPEN_MORE_BUTTON: {
                Log.d(TAG, "# 펼쳐서 더보기 버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_MORE_LOADED, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_MORE_BUTTON: {
                Log.d(TAG, "# 더보기 버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_MORE_LOADED, MathHelper.randomRange(3000, 5000));
                break;
            }

            case WEB_BACK: {
                Log.d(TAG, "# 웹뷰 뒤로 후 동작");
                _workCode = 130902;
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
                break;
            }

            case TOUCH_LOGO: {
                Log.d(TAG, "# 로고 버튼 터치 후 동작");
                _workCode = 130903;
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                break;
            }
        }

        _lastMessage = -1;
    }

    public void inputKeyword() {
        if (_item.item.workType == KeywordItem.WORK_TYPE_INPUT) {
            Log.d(TAG, "# 검색어 삽입: " + _keyword);
            // 인풋태그에 값 넣기
            _homeAction.inputSearchBar(false, _keyword);

            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1300, 3000));
        } else if (_item.item.workType == KeywordItem.WORK_TYPE_CLIPBOARD) {
            Log.d(TAG, "# 검색어 클립보드 복사: " + _keyword);
            _action.copyToClipboard(_webViewManager.getWebView().getContext(), _keyword);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SystemClock.sleep(MathHelper.randomRange(1000, 1500));
                Log.d(TAG, "# 검색어 붙여넣기");
                _action.pasteClipboard();
            } else {
                Log.d(TAG, "# 검색창 롱터치");
                if (!_homeAction.touchSearchBarLong(true)) {
                    Log.d(TAG, "# 검색창 롱터치에 실패해서 패턴종료.");
                    _workCode = 130012;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    return;
                }

                SystemClock.sleep(MathHelper.randomRange(1000, 1500));
                Log.d(TAG, "# 검색어 붙여넣기");
                _homeAction.touchPasteButton();
            }

            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1300, 3000));
        } else if (_item.item.workType == 5) {
            // 추가 가능한 기능있으면 추가 예정..
        } else {
            Log.d(TAG, "# 검색어 입력: " + _keyword);
            _action.inputKeywordForTyping(_keyword);
            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1000, 3000));
        }
    }
}
