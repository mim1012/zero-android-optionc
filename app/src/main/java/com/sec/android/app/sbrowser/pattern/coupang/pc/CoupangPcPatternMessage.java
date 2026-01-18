package com.sec.android.app.sbrowser.pattern.coupang.pc;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;
import com.sec.android.app.sbrowser.models.KeywordItem;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.pattern.RandomSwipePatternAction;
import com.sec.android.app.sbrowser.pattern.action.ResultAction;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.coupang.CoupangPatternMessage;
import com.sec.android.app.sbrowser.pattern.coupang.action.CoupangHomeAction;
import com.sec.android.app.sbrowser.pattern.coupang.action.CoupangPatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.naver.view.NaverViewAfterPatternAction;

public class CoupangPcPatternMessage extends CoupangPatternMessage {

    private static final String TAG = CoupangPcPatternMessage.class.getSimpleName();

    private static final int FIND_CONTENT = 40;
    private static final int RUN_AFTER = 41;
    private static final int RANDOM_SCROLL = 50;
    private static final int TOUCH_BACK_BUTTON = RANDOM_SCROLL + 1;
    private static final int TOUCH_CONTENT = RANDOM_SCROLL + 2;
    private static final int TOUCH_NEXT_BUTTON = RANDOM_SCROLL + 3;
    private static final int TOUCH_MORE_CONTENT = RANDOM_SCROLL + 4;

    private final CoupangPatternAction _action;
    private final CoupangHomeAction _homeAction;
    private final CoupangPcPageAction _viewPageAction;
    private final NaverViewAfterPatternAction _afterPatternAction;
    private final RandomSwipePatternAction _randomSwipePatternAction;
    private final SwipeThreadAction _swipeAction;

    private int _nextMessage = 0;
    private int _nextPopupMessage = 0;
    private int _step = 0;
    private int _findBarCount = 0;
    private String _keyword;
    private String _url;
    private String _code;

    public CoupangPcPatternMessage(WebViewManager manager, KeywordItemMoon item) {
        super(manager);
        _item = item;
        _keyword = item.keyword;
        _url = item.url;
        _code = item.code;

        _action = new CoupangPatternAction(manager.getWebView());
        _homeAction = new CoupangHomeAction(manager.getWebView());
        _viewPageAction = new CoupangPcPageAction(manager.getWebView());
        _afterPatternAction = new NaverViewAfterPatternAction(manager.getWebView(), _url);
        _randomSwipePatternAction = new RandomSwipePatternAction(manager.getWebView().getContext());

        TouchInjector injector = new TouchInjector(manager.getWebView().getContext());
        injector.setSoftKeyboard(new SamsungKeyboard());

        _swipeAction = new SwipeThreadAction(injector);
        getResultAction().item = item;

//        _webViewManager.getWebView().post(new Runnable() {
//            @Override
//            public void run() {
//                _webViewManager.getWebView().getSettings().setLoadsImagesAutomatically(false);
//            }
//        });
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 쿠팡PC 작업 시작");
              //  _handler.sendEmptyMessage(FIND_KEYWORD);
                _handler.sendEmptyMessage(GO_HOME);
                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 쿠팡PC 홈으로 이동");
//                webViewLoad(msg, HOME_URL);
                webViewLoad(msg, "https://m.coupang.com/nm/redirect/pcHome");
                break;
            }

//            case TOUCH_MOBILE_WEB_POPUP: {
//                Log.d(TAG, "# 모바일웹 보기 팝업 검사");
//
//                if (_homeAction.checkFullBanner()) {
//                    if (_homeAction.touchButton(CoupangHomeAction.BUTTON_MOBILE_WEB)) {
//                        _handler.sendEmptyMessageDelayed(_nextPopupMessage, MathHelper.randomRange(2500, 3500));
//                    } else {
//                        _handler.sendEmptyMessageDelayed(_nextPopupMessage, 1000);
//                    }
//                } else {
//                    _handler.sendEmptyMessageDelayed(_nextPopupMessage, 1000);
//                }
//                break;
//            }

            case TOUCH_SEARCH_BAR: {
                Log.d(TAG, "# 검색창 터치");
                if (_homeAction.touchPcSearchBar()) {
                    _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
                } else {
                    Log.d(TAG, "# 검색창 터치에 실패해서 패턴종료.");
                    _workCode = 220001;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case INPUT_KEYWORD: {
                inputKeyword(_keyword);

//                Log.d(TAG, "# 검색창 검사");
//                if (!_homeAction.checkPcSearchBar()) {
//                    if (_findBarCount > 3) {
//                        Log.d(TAG, "# 로딩에러로 처리 중단.");
//                        _workCode = 220011;
//                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
//                    } else {
//                        Log.d(TAG, "# 검색창이 떠있지 않아서 새로 고침");
//                        ++_findBarCount;
//                        _webViewManager.reload();
//                        webViewLoading(msg);
////                        _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, 5000);
//                    }
//                } else {
//                }
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치");
                _action.touchSearchButton();
                webViewLoading(msg);
                break;
            }

//            case FIND_KEYWORD:{
//                Log.d(TAG, _keyword +" 검색시작");
//                try {
//                    _keyword =  URLEncoder.encode(_keyword, "UTF-8");
//                } catch (UnsupportedEncodingException e) {
//
//                }
//                webViewLoad(msg, "https://m.search.naver.com/search.naver?query="+ _keyword +"&where=m&sm=mtp_hty.top");
//                break;
//            }

            case REGISTER_FINISH: {
                Log.d(TAG, "# 검색어 입력완료 처리");
              //  _finishPatternAction.finishKeyword(loginId, imei);
                // 로딩이 늦어질수 있으므로 7초대기.
                _handler.sendEmptyMessageDelayed(FIND_CONTENT, 7000);
                break;
            }

            case RANDOM_SCROLL: {
                Log.d(TAG, "# 랜덤 스크롤");
                int count = (int) MathHelper.randomRange(7, 10);

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
                Log.d(TAG, "# 쿠팡PC 컨텐츠 검사");
                InsideData insideData = _viewPageAction.getContentCodeInsideData(_code);
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 쿠팡PC 컨텐츠 터치");
                        if (_viewPageAction.touchContentCode(_code)) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 쿠팡PC 컨텐츠 터치 실패로 패턴종료.");
                            _workCode = 220021;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        _swipeAction.swipeDown();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
//                    Log.d(TAG, "# 쿠팡PC 컨텐츠 못찾아서 패턴종료.");
//                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    Log.d(TAG, "# 쿠팡PC 컨텐츠 못찾아서 다음으로...");
                    sendMessageDelayed(TOUCH_NEXT_BUTTON, 100);
                }
                break;
            }

            case TOUCH_NEXT_BUTTON: {
                Log.d(TAG, "# 다음 버튼 검사");
                InsideData insideData = _viewPageAction.getNextButtonInsideData();
                if (insideData != null) {
                    String page =  _viewPageAction.getCurrentPage();
                    if (page == null) {
                        Log.d(TAG, "# 현재 페이지를 못찾아서 패턴종료.");
                        _workCode = 220031;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    } else if (Integer.parseInt(page) > 11) {
                        Log.d(TAG, "# 11페이지 초과로 패턴종료.");
                        _workCode = 220032;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    } else {
                        if (insideData.isInside()) {
                            Log.d(TAG, "# 다음 버튼 터치");
                            if (_viewPageAction.touchButton(CoupangPcPageAction.BUTTON_NEXT)) {
                                webViewLoading(msg);
                            } else {
                                Log.d(TAG, "# 다음 버튼 터치 실패로 패턴종료.");
                                _workCode = 220033;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                            }
                        } else if (insideData.inside > 0) {
                            Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
//                            _viewPageAction.scrollToBottom();
                            _swipeAction.swipeDown();
                            _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                        } else {
                            Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                            _swipeAction.swipeUp();
                            _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                        }
                    }
                } else {
                    Log.d(TAG, "# 다음 버튼 못찾아서 패턴종료.");
                    _workCode = 220034;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_MORE_CONTENT: {
                Log.d(TAG, "# 쿠팡PC 상품정보 더보기 버튼 검사");
                InsideData insideData = _viewPageAction.getContentMoreButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 쿠팡PC 상품정보 더보기 버튼 터치");
                        if (_viewPageAction.touchButton(CoupangPcPageAction.BUTTON_CONTENT_MORE)) {
                            _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(1000, 3000));
                        } else {
                            Log.d(TAG, "# 쿠팡PC 상품정보 더보기 버튼 터치 실패로 패턴종료.");
                            _workCode = 220041;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        _swipeAction.swipeDown();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 쿠팡PC 상품정보 더보기 버튼 못찾아서 다음으로...");
                    sendMessageDelayed(RANDOM_SCROLL, 100);
                }
                break;
            }


//            case FIND_CONTENT: {
//                Log.d(TAG, "# 컨텐츠 찾기");
//
//                // 클래스 내부 작업을 쪼개야한다..ㅠㅠ
//                _touchUrlPatternAction.workInThread();
//
//                if (_touchUrlPatternAction.isFind()) {
////                    _handler.sendEmptyMessageDelayed(REGISTER_RANK, MathHelper.randomRange(6000, 9000));
//                    _handler.sendEmptyMessageDelayed(STAY_RANDOM, MathHelper.randomRange(6000, 7000));
//                } else {
//                    // 컨텐츠를 찾지 못했을때.
//                    if (_step == 0) {
//                        _touchUrlPatternAction.setMoreView(true);
//                        // 위에서 더보기를 누른 상태이므로 로딩까지 다시 기다린다.
//                        _handler.sendEmptyMessageDelayed(FIND_CONTENT, MathHelper.randomRange(10000, 11000));
//                        _step++;
//                    } else {
//                        _handler.sendEmptyMessageDelayed(TOUCH_LOGO, MathHelper.randomRange(2000, 3000));
//                    }
//                }
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
                Log.d(TAG, "# 쿠팡PC 패턴 종료");
                registerResultFinish();
                _action.endPattern();
                _homeAction.endPattern();
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
                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(10000, 11000));
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치 후 동작");

                SystemClock.sleep(MathHelper.randomRange(2000, 3000));

                Log.d(TAG, "# 랜덤 스크롤");
                int count = (int) MathHelper.randomRange(3, 4);

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

                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(2000, 3000));
//                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, 1000);
//                _handler.sendEmptyMessageDelayed(REGISTER_FINISH, 1000);
                break;
            }

            case INPUT_KEYWORD: {
                Log.d(TAG, "# 검색창 검사 새로고침 후 동작");
                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
                break;
            }

//            case FIND_KEYWORD: {
//                Log.d(TAG, "# 키워드 검색 로딩 후 동작");
//                _handler.sendEmptyMessageDelayed(REGISTER_FINISH, 1000);
//                break;
//            }

            case TOUCH_BACK_BUTTON: {
                Log.d(TAG, "# 백 버튼 터치 후 동작");
                _workCode = 220901;
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_CONTENT: {
                Log.d(TAG, "# 쿠팡PC 컨텐츠 터치 후 동작");
//                _result = 1;
                _result = ResultAction.SUCCESS;
                _nextMessage = WEB_BACK;
                _handler.sendEmptyMessageDelayed(TOUCH_MORE_CONTENT, MathHelper.randomRange(9000, 10000));
                break;
            }

            case TOUCH_NEXT_BUTTON: {
                Log.d(TAG, "# 다음 버튼 터치 후 동작");
                _nextPopupMessage = TOUCH_CONTENT;
                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(6000, 8000));
                break;
            }

            case WEB_BACK: {
                Log.d(TAG, "# 웹뷰 뒤로 후 동작");
                _workCode = 220902;
//                _handler.sendEmptyMessageDelayed(RUN_AFTER, MathHelper.randomRange(4000, 5000));
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
                break;
            }

            case TOUCH_LOGO: {
                Log.d(TAG, "# 로고 버튼 터치 후 동작");
                _workCode = 220903;
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                break;
            }
        }

        _lastMessage = -1;
    }

    public void inputKeyword(String keyword) {
        if (_item.item.workType == KeywordItem.WORK_TYPE_INPUT) {
            Log.d(TAG, "# 검색어 삽입: " + keyword);
            // 인풋태그에 값 넣기
            _homeAction.inputPcSearchBar(keyword);
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
//                if (!_homeAction.touchSearchBarLong(_startHomeMode == 0)) {
//                    Log.d(TAG, "# 검색창 롱터치에 실패해서 패턴종료.");
//                    _workCode = 110014;
//                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
//                    return;
//                }
//
//                SystemClock.sleep(MathHelper.randomRange(1000, 1500));
//                Log.d(TAG, "# 검색어 붙여넣기");
//                _homeAction.touchPasteButton();
            }

            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1500, 3000));
        } else if (_item.item.workType == 5) {
            // 추가 가능한 기능있으면 추가 예정..
        } else {
            Log.d(TAG, "# 검색어 입력: " + _keyword);
            _action.extractStrings(_keyword);
            _action.inputKeyword();
            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1000, 3000));
        }
    }
}
