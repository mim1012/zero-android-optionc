package com.sec.android.app.sbrowser.pattern.naver.place;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.Config;
import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.pattern.RandomSwipePatternAction;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverHomeAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;

public class NaverPlacePatternMessage2 extends NaverPatternMessage {

    private static final String TAG = NaverPlacePatternMessage.class.getSimpleName();
    private static final int FIND_KEYWORD = 30;
    private static final int FIND_CONTENT = 40;
    private static final int RUN_AFTER = 41;

    protected static final int TOUCH_PHONE = 1042;


    private final NaverPatternAction _action;
    private final NaverHomeAction _homeAction;
    private final NaverPlacePhoneAction _phoneAction;
    private final NaverPlaceTouchUrlPatternAction _touchUrlPatternAction;
    protected final NaverPlaceResultAction _resultPatternAction;

    private final NaverPlaceAfterPatternAction _afterPatternAction;
    private final RandomSwipePatternAction _randomSwipePatternAction;
    private final SwipeThreadAction _swipeAction;

    private int _step = 0;
    private int _findBarCount = 0;
    private int rank = -1;
    String 키워드;
    String Code;
    public NaverPlacePatternMessage2(WebViewManager manager, KeywordItemMoon item) {
        super(manager);
        키워드 = item.keyword;
        Code = item.code;

        _action = new NaverPatternAction(manager.getWebView());
        _homeAction = new NaverHomeAction(manager.getWebView());
        _phoneAction = new NaverPlacePhoneAction(manager.getWebView());
        _touchUrlPatternAction = new NaverPlaceTouchUrlPatternAction(manager.getWebView(),Code);
        _touchUrlPatternAction.item = item;

        _resultPatternAction = new NaverPlaceResultAction();
        _resultPatternAction.item = item;

        _afterPatternAction = new NaverPlaceAfterPatternAction(manager.getWebView(),Code);
        _randomSwipePatternAction = new RandomSwipePatternAction(manager.getWebView().getContext());

        TouchInjector touchInjector = new TouchInjector(manager.getWebView().getContext());
        touchInjector.setSoftKeyboard(new SamsungKeyboard());
        _swipeAction = new SwipeThreadAction(touchInjector);
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# Place 작업 시작");
                //  _handler.sendEmptyMessage(FIND_KEYWORD);
                _handler.sendEmptyMessage(GO_HOME);

                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 네이버 홈으로 이동");
                webViewLoad(msg, Config.HOME_URL);
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
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                }
                break;
            }

            case INPUT_KEYWORD: {
                Log.d(TAG, "# 검색창 검사");
                _searchBarCheckPatternAction.checkSearchBarShown();

                if (!_searchBarCheckPatternAction.isFocus()) {
                    if (_findBarCount > 3) {
                        Log.d(TAG, "# 로딩에러로 처리 중단.");
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                    } else {
                        Log.d(TAG, "# 검색창이 떠있지 않아서 다시 터치");
                        ++_findBarCount;
                        _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, 5000);
                    }
                } else {
                    Log.d(TAG, "# 검색어 입력: " + 키워드);
                    _action.extractStrings(키워드);
                    _action.inputKeyword();
                    _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1000, 3000));
                }
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치");
                _searchBarAction.submitSearchButton();
//                _action.touchSearchButton();
                webViewLoading(msg);
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
                if ((int)MathHelper.randomRange(0, 1) == 1) {
                    _handler.sendEmptyMessageDelayed(TOUCH_PHONE, MathHelper.randomRange(3000, 6000));
                } else {
                    _handler.sendEmptyMessageDelayed(WEB_BACK, MathHelper.randomRange(3000, 6000));
                }
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
                Log.d(TAG, "# Place 키워드 패턴 종료");
                registerFinish();
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
                _handler.sendEmptyMessageDelayed(TOUCH_NEW_POPUP_OK, MathHelper.randomRange(5000, 6000));
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(FIND_CONTENT, 7000);
                break;
            }

            case FIND_KEYWORD: {
                Log.d(TAG, "# 키워드 검색 로딩 후 동작");
                _handler.sendEmptyMessageDelayed(REGISTER_FINISH, 1000);
                break;
            }

            case WEB_BACK: {
                Log.d(TAG, "# 웹뷰 뒤로 후 동작");
                _handler.sendEmptyMessageDelayed(RUN_AFTER, MathHelper.randomRange(4000, 5000));
                break;
            }

            case TOUCH_LOGO: {
                Log.d(TAG, "# 로고 버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                break;
            }
        }

        _lastMessage = -1;
    }

    protected void registerFinish() {
        _resultPatternAction.registerFinish(rank);
    }
}
