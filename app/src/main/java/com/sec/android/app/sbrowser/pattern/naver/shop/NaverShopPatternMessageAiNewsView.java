package com.sec.android.app.sbrowser.pattern.naver.shop;

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
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class NaverShopPatternMessageAiNewsView extends NaverPatternMessage {

    private static final String TAG = NaverShopPatternMessageAiNewsView.class.getSimpleName();

    protected static final int GO_SHOP_HOME = 40;
    protected static final int GO_SHOP_HOME_DIRECT = 41;
    private static final int RANDOM_SCROLL = 50;
    private static final int TOUCH_BACK_BUTTON = RANDOM_SCROLL + 1;
    private static final int TOUCH_CONTENT = RANDOM_SCROLL + 2;
    private static final int TOUCH_TOP_DOTS_BUTTON = RANDOM_SCROLL + 3;
    private static final int TOUCH_TOP_SHOPPING_BUTTON = RANDOM_SCROLL + 4;
    private static final int TOUCH_MORE_BUTTON = RANDOM_SCROLL + 5;
    private static final int TOUCH_MORE2_BUTTON = RANDOM_SCROLL + 6;
    private static final int TOUCH_HOME_NEXT_BUTTON = RANDOM_SCROLL + 7;
    private static final int TOUCH_HOME_PREV_BUTTON = RANDOM_SCROLL + 8;
    private static final int TOUCH_NEXT_BUTTON = TOUCH_HOME_PREV_BUTTON + 1;
    private static final int TOUCH_NEXT_BUTTON_WAITING = TOUCH_NEXT_BUTTON + 2;
    private static final int TOUCH_OPTION_BUTTON = TOUCH_NEXT_BUTTON + 3;
    private static final int TOUCH_COMPANY_CONTENT = TOUCH_NEXT_BUTTON + 4;
    private static final int TOUCH_ALL_COMPANY_BUTTON = TOUCH_NEXT_BUTTON + 5;
    private static final int SCROLL_BOTTOM = TOUCH_NEXT_BUTTON + 6;
    private static final int TOUCH_SEARCH_BAR_FOR_CLEAR = SCROLL_BOTTOM + 1;

    private final NaverHomeAction _homeAction;
    private final NaverShopPageAction _shopPageAction;
    private final RandomSwipePatternAction _randomSwipePatternAction;
    private final SwipeThreadAction _swipeAction;
    protected final NaverShopSearchBarAction _shopSearchBarAction;
    protected final NaverShopSearchBarClearPatternAction _shopSearchBarClearPatternAction;

    private int _nextMessage = 0;
    private int _currentPage = 0;
    private int _step = 0;
    private int _searchStep = 0;
    private int _findBarCount = 0;
    private int _waitCount = 0;
    private String _keyword;
    private String _mid;
    private String _mid2;
    private int _scrollType = 0;
    private int _scrollCount1 = 0;
    private int _scrollCount2 = 0;

    private int _startHomeMode = 0;
    private int _randomClickCount = 0;
    private int _randomClickWorkCount = 0;
    private boolean _foundRandomItem = true;
    private ArrayList<String> _randomMids = new ArrayList<>();
    private boolean _workMore = true;

    public NaverShopPatternMessageAiNewsView(WebViewManager manager, KeywordItemMoon item) {
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
        _randomSwipePatternAction = new RandomSwipePatternAction(manager.getWebView().getContext());
        _shopSearchBarAction = new NaverShopSearchBarAction(manager.getWebView());
        _shopSearchBarClearPatternAction = new NaverShopSearchBarClearPatternAction(manager.getWebView());

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
                Log.d(TAG, "# 네이버 쇼핑 작업 시작");
                _searchStep = 0;
                _randomClickWorkCount = 0;

                if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_BACK) {
                    Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_SHOP_ABC_BACK");
                } else if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_RANDOM) {
                    Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_SHOP_ABC_RANDOM");
                } else {
                    Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_NORMAL");
                }

                if (_item.item.account != null) {
                    _accountItem = _item.item.account;
                    _handler.sendEmptyMessage(GO_LOGIN);
                } else {
                    _handler.sendEmptyMessage(RUN_NEXT);
                }
                break;
            }

            case RUN_NEXT: {
                // 패치후 삭제 예정.
                if (_item.item.workType == 2) {
                    _handler.sendEmptyMessage(GO_SHOP_HOME_DIRECT);
                } else {
                    if (_startHomeMode == 1) {
                        _handler.sendEmptyMessage(GO_SHOP_HOME);
                    } else if (_startHomeMode == 2) {
                        _handler.sendEmptyMessage(GO_SEARCH_HOME_EMPTY);
                    } else if (_startHomeMode == 3) {
                        _handler.sendEmptyMessage(GO_SHOP_HOME_DIRECT);
                    } else {
                        _handler.sendEmptyMessage(GO_HOME);
                        // _handler.sendEmptyMessage(FIND_KEYWORD);
                    }
                }
                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 네이버 홈으로 이동");
//                _webViewManager.loadUrl("http://google.com");
                webViewLoad(msg, Config.HOME_URL);
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
                webViewLoad(msg, "https://m.shopping.naver.com/");
                break;
            }

            case GO_SHOP_HOME_DIRECT: {
                Log.d(TAG, "# 네이버 쇼핑홈으로 검색어 이동");
                _currentPage = 2;
                try {
                    _keyword = URLEncoder.encode(_keyword, "UTF-8");
                } catch (UnsupportedEncodingException e) {

                }

                // https://msearch.shopping.naver.com/search/all?query=간장게장&frm=NVSHSRC&cat_id=&pb=true&mall=
                // https://msearch.shopping.naver.com/search/all?query=%EA%B0%84%EC%9E%A5%EA%B2%8C%EC%9E%A5&frm=NVSHSRC&vertical=home
                webViewLoad(msg, "https://msearch.shopping.naver.com/search/all?query=" + _keyword + "&frm=NVSHSRC&vertical=home");
                // 신규 버전 테스트 필요.
//                webViewLoad(msg, "https://msearch.shopping.naver.com/search/all?query=" + _keyword + "&fs=true");
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
                        _workCode = 110002;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                } else {
                    if (_homeAction.touchSearchBar(_startHomeMode != 2)) {
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
                                Log.d(TAG, "# 검색창이 떠있지 않아서 3초 후 다시 시도..." + _waitCount);
                                ++_waitCount;
                                _handler.sendEmptyMessageDelayed(msg.what, 3000);
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
                        inputKeyword(_keyword);
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
                    _searchBarAction.submitSearchButton();
//                    _action.touchSearchButton();
                }
                webViewLoading(msg);
                break;
            }

            case RANDOM_SCROLL: {
                Log.d(TAG, "# 랜덤 스크롤");
                int baseRange = 6;

                if (_nextMessage == WEB_BACK) {
                    if (_item.item.randomScrollCount > 0) {
                        baseRange = _item.item.randomScrollCount;
                    }
                }

                int count = (int) MathHelper.randomRange(baseRange, baseRange + 4);

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
                Log.d(TAG, "# 네이버 쇼핑 컨텐츠 검사");
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

                InsideData insideData = _shopPageAction.getContentMidInsideData(_mid, (_currentPage <= 1));
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 컨텐츠 터치");
                        if (_shopPageAction.touchContentMid(_mid, (_currentPage <= 1))) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 컨텐츠 터치 실패로 패턴종료.");
                            _workCode = 110021;
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
                    Log.d(TAG, "# 네이버 쇼핑 컨텐츠 못찾아서 다음으로...");
                    if (_currentPage <= 1) {
                        if (_item.item.workType == 1) {
                            sendMessageDelayed(TOUCH_TOP_DOTS_BUTTON, 100);
                        } else {
                            sendMessageDelayed(TOUCH_MORE2_BUTTON, 100);
                        }
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

            case TOUCH_TOP_DOTS_BUTTON: {
                Log.d(TAG, "# 상단 점세개 버튼 검사");
                InsideData insideData = _homeAction.getTopDotsButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 상단 점세개 버튼 터치");
                        if (_homeAction.touchButton(NaverHomeAction.BUTTON_TOP_DOTS)) {
                            _handler.sendEmptyMessageDelayed(TOUCH_TOP_SHOPPING_BUTTON, MathHelper.randomRange(1000, 2000));
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
                            _workCode = 110036;
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
                InsideData insideData = _shopPageAction.getNextButtonInsideData();
                if (insideData != null) {
                    String page =  _shopPageAction.getCurrentPage();
                    if (page == null) {
                        Log.d(TAG, "# 현재 페이지를 못찾아서 패턴종료.");
                        _workCode = 110041;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    } else if (Integer.parseInt(page) > 7) {
                        Log.d(TAG, "# 7페이지 초과로 패턴종료.");
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
                        if (_shopPageAction.touchContentMid2(_mid2)) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 네이버 쇼핑 가격비교 터치 실패로 패턴종료.");
                            _workCode = 110061;
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
            case GO_HOME: {
                Log.d(TAG, "# 홈이동 후 동작");
                _findBarCount = 0;
//                _handler.sendEmptyMessageDelayed(TOUCH_NEW_POPUP_OK, MathHelper.randomRange(5000, 6000));
                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
//                SystemClock.sleep(5000);
//                _shopPageAction.printHtml();
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
                Log.d(TAG, "# 쇼핑홈이동 후 동작");
                _findBarCount = 0;
//                SystemClock.sleep(5000);
//                _shopPageAction.printLocalStorage();
                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
                break;
            }

            case GO_SHOP_HOME_DIRECT: {
                Log.d(TAG, "# 쇼핑홈 검색어 이동 후 동작");
                _findBarCount = 0;
                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(5000, 6000));
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
                }
                break;
            }

            case INPUT_KEYWORD: {
                Log.d(TAG, "# 검색창 검사 새로고침 후 동작");
                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
                break;
            }

            case TOUCH_BACK_BUTTON: {
                Log.d(TAG, "# 백 버튼 터치 후 동작");
                _workCode = 110901;
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_CONTENT: {
                Log.d(TAG, "# 네이버 쇼핑 컨텐츠 터치 후 동작");
//                _result = 1;
                if (_randomClickWorkCount >= _randomClickCount) {
                    if (!_mid2.equals(".")) {
                        if (TextUtils.isEmpty(_item.item.code3)) {
                            _nextMessage = TOUCH_COMPANY_CONTENT;
                        } else {
                            _nextMessage = TOUCH_OPTION_BUTTON;
                        }
                    } else {
                        Log.d(TAG, "# 작업 성공");
                        _result = ResultAction.SUCCESS;
                        _nextMessage = WEB_BACK;
                    }
                } else {
                    _nextMessage = WEB_BACK;
//                    _nextMessage = TOUCH_BACK_BUTTON;
                }
                _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
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
//                _result = 1;
                _result = ResultAction.SUCCESS;
                _nextMessage = WEB_BACK;
//                _nextMessage = TOUCH_BACK_BUTTON;
                _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(3000, 5000));
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
                    if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_BACK) {
                        if (_searchStep < 4) {
                            ++_searchStep;
                            _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(4000, 5000));
                        } else {
                            _workCode = 110905;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
                        }
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

            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1300, 3000));
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

            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1300, 3000));
        } else if (_item.item.workType == 5) {
            // 추가 가능한 기능있으면 추가 예정..
        } else {
            Log.d(TAG, "# 검색어 입력: " + keyword);
            _action.inputKeywordForTyping(keyword);
            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1000, 3000));
        }
    }
}
