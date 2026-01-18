package com.sec.android.app.sbrowser.pattern.naver.shop;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.pattern.RandomSwipePatternAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverHomeAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NaverShopPatternMessage2 extends NaverPatternMessage {

    private static final String TAG = NaverShopPatternMessage2.class.getSimpleName();


    private static final int FIND_KEYWORD = 30;
    private static final int FIND_CONTENT = 40;
    private static final int FIND_CONTENT2 = 43;
    private static final int RUN_AFTER = 42;
    private final NaverPatternAction _action;
    private final NaverHomeAction _homeAction;
    private NaverShopTouchUrlPatternAction _touchUrlPatternAction;
    private final RandomSwipePatternAction _randomSwipePatternAction;
    private int page = 1;
    private int _findBarCount = 0;
    String 키워드;
    String MID;
    String MID2;
    int count = 0;
    WebViewManager webViewManager;
    public NaverShopPatternMessage2(WebViewManager manager, String keyword, String mid , String mid2) {
        super(manager);
        webViewManager = manager;
        키워드=keyword;
        MID=mid;
        MID2=mid2;
        _action = new NaverPatternAction(manager.getWebView());
        _homeAction = new NaverHomeAction(manager.getWebView());
        _touchUrlPatternAction = new NaverShopTouchUrlPatternAction(manager.getWebView(),MID,MID2);
        _randomSwipePatternAction = new RandomSwipePatternAction(manager.getWebView().getContext());
    }
    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);
        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# Shop 작업 시작");
                _handler.sendEmptyMessage(GO_HOME);
                // _handler.sendEmptyMessage(FIND_KEYWORD);
                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 네이버 홈으로 이동");
             //   webViewLoad(msg, Config.HOME_URL);
                webViewLoad(msg, "https://m.naver.com");
                break;
            }

            case FIND_KEYWORD:{
                Log.d(TAG, 키워드+"/"+MID+"/"+MID2+" 검색시작");
                try {
                    키워드 =  URLEncoder.encode(키워드, "UTF-8");
                } catch (UnsupportedEncodingException e) {

                }
                webViewLoad(msg, "https://msearch.shopping.naver.com/search/all?query="+ 키워드);
                break;
            }


            case TOUCH_NEW_POPUP_OK: {  // 4월 3일 바뀐 홈.
                Log.d(TAG, "# 안내팝업창 검사");
                if (_homeAction.touchButton(NaverHomeAction.BUTTON_POPUP_OK)) {
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
                _action.touchSearchButton();
                webViewLoading(msg);
                break;
            }



            case FIND_CONTENT: {
                Log.d(TAG, "# 상품 찾기");
                _touchUrlPatternAction.workInThread();
                if (_touchUrlPatternAction.isFind()) {
                    if(MID2.equals(".")){
                        _handler.sendEmptyMessageDelayed(STAY_RANDOM, MathHelper.randomRange(6000, 7000));
                    }else{
                        _handler.sendEmptyMessageDelayed(FIND_CONTENT2, MathHelper.randomRange(10000, 11000));
                    }
                }else{
                    if(!_touchUrlPatternAction.not_page()) {
                        if (page < 7) {
                             page++;
                             webViewManager.loadUrl("https://msearch.shopping.naver.com/search/all?query="+ 키워드+"&pagingIndex="+page);
                            _handler.sendEmptyMessageDelayed(FIND_CONTENT, MathHelper.randomRange(10000, 11000));
                        } else {
                            _handler.sendEmptyMessageDelayed(WEB_BACK, MathHelper.randomRange(3000, 6000));
                        }
                    }else{
                        _handler.sendEmptyMessageDelayed(WEB_BACK, MathHelper.randomRange(3000, 6000));
                }

                }
                break;
            }

            case FIND_CONTENT2: {
                Log.d(TAG, "# 가격비교 찾기");
                if(count == 0) {
                    count++;
                    _touchUrlPatternAction.workInThread2(false);
                    if(!_touchUrlPatternAction.is_pro2clk()) {
                        _handler.sendEmptyMessageDelayed(FIND_CONTENT2, MathHelper.randomRange(10000, 11000));
                    }else{
                        _handler.sendEmptyMessageDelayed(STAY_RANDOM, MathHelper.randomRange(6000, 7000));
                    }
                }else{
                    if(count < 5) {
                        _touchUrlPatternAction.workInThread2(true);
                        if(!_touchUrlPatternAction.is_pro2clk()) {
                            _handler.sendEmptyMessageDelayed(FIND_CONTENT2, MathHelper.randomRange(10000, 11000));
                        }else{
                            _handler.sendEmptyMessageDelayed(STAY_RANDOM, MathHelper.randomRange(6000, 7000));
                        }
                        count++;
                    }else{
                        _handler.sendEmptyMessageDelayed(WEB_BACK, MathHelper.randomRange(3000, 6000));
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
                Log.d(TAG, "# Shop 패턴 종료");

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
                _handler.sendEmptyMessageDelayed(TOUCH_NEW_POPUP_OK, MathHelper.randomRange(5000, 6000));
                break;
            }

            case FIND_KEYWORD: {
                Log.d(TAG, "# 키워드 검색 후 동작");
                _handler.sendEmptyMessageDelayed(FIND_CONTENT, MathHelper.randomRange(5000, 6000));
                break;
            }


//            case TOUCH_SEARCH_BUTTON: {
//                Log.d(TAG, "# 검색버튼 터치 후 동작");
//                _randomSwipePatternAction.randomSwipe();
//                _handler.sendEmptyMessageDelayed(FIND_CONTENT, 1000);
//                break;
//            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(FIND_CONTENT, 7000);
                break;
            }

            case WEB_BACK: {
                Log.d(TAG, "# 웹뷰 뒤로 후 동작");
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
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
}
