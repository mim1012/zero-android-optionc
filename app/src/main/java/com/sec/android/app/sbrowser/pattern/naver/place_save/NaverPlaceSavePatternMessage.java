package com.sec.android.app.sbrowser.pattern.naver.place_save;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.pattern.RandomSwipePatternAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverHomeAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;

public class NaverPlaceSavePatternMessage extends NaverPatternMessage {

    private static final String TAG = NaverPlaceSavePatternMessage.class.getSimpleName();


    private static final int FIND_PLACE = 30;
    private static final int FIND_CONTENT = 40;
    private static final int FIND_CONTENT2 = 43;
    private final NaverPatternAction _action;
    private final NaverHomeAction _homeAction;
    private NaverPlaceSaveTouchUrlPatternAction _touchUrlPatternAction;
    private final RandomSwipePatternAction _randomSwipePatternAction;
    private int page = 1;
    String Code;
    WebViewManager webViewManager;
    public NaverPlaceSavePatternMessage(WebViewManager manager,String code) {
        super(manager);
        webViewManager = manager;
        Code=code;
        _action = new NaverPatternAction(manager.getWebView());
        _homeAction = new NaverHomeAction(manager.getWebView());
        _touchUrlPatternAction = new NaverPlaceSaveTouchUrlPatternAction(manager.getWebView(),Code);
        _randomSwipePatternAction = new RandomSwipePatternAction(manager.getWebView().getContext());
    }
    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);
        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# Place_Save 작업 시작");
                _handler.sendEmptyMessage(FIND_PLACE);
                //_handler.sendEmptyMessage(FIND_KEYWORD);
                break;
            }



            case FIND_PLACE:{
                Log.d(TAG, "작업시작");
                webViewLoad(msg, "https://m.place.naver.com/place/"+ Code);
                break;
            }

            case FIND_CONTENT: {
                Log.d(TAG, "# 상품 찾기");
                _touchUrlPatternAction.workInThread();
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 6000));
                break;
            }

                case WEB_BACK: {
                Log.d(TAG, "# 웹뷰 뒤로");
                webViewGoBack(msg);
                break;
            }
            case END_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# Place_Save 패턴 종료");
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


            case FIND_PLACE: {
                Log.d(TAG, "# 키워드 검색 후 동작");
                _handler.sendEmptyMessageDelayed(FIND_CONTENT, MathHelper.randomRange(5000, 6000));
                break;
            }


            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치 후 동작");
                _randomSwipePatternAction.randomSwipe();
                _handler.sendEmptyMessageDelayed(FIND_CONTENT, 1000);
                break;
            }

/*
            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(FIND_CONTENT, 1000);
                break;
            }
            */
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
