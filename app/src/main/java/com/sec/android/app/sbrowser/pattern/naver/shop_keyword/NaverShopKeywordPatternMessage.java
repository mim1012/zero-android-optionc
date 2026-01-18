package com.sec.android.app.sbrowser.pattern.naver.shop_keyword;

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

public class NaverShopKeywordPatternMessage extends NaverPatternMessage {

    private static final String TAG = NaverShopKeywordPatternMessage.class.getSimpleName();


    private static final int FIND_KEYWORD = 30;
    private static final int FIND_CONTENT = 40;
    private static final int FIND_SHOP_KEYWORD = 41;
    private static final int RUN_AFTER = 42;
    private final NaverPatternAction _action;
    private final NaverHomeAction _homeAction;
    private NaverShopKeywordTouchUrlPatternAction _touchUrlPatternAction;
    private final RandomSwipePatternAction _randomSwipePatternAction;

    String 키워드;
    String urls;

    WebViewManager webViewManager;
    public NaverShopKeywordPatternMessage(WebViewManager manager, String keyword,String url) {
        super(manager);
        webViewManager = manager;
        키워드=keyword;
        urls=url;
        _action = new NaverPatternAction(manager.getWebView());
        _homeAction = new NaverHomeAction(manager.getWebView());
        _touchUrlPatternAction = new NaverShopKeywordTouchUrlPatternAction(manager.getWebView());
        _randomSwipePatternAction = new RandomSwipePatternAction(manager.getWebView().getContext());
    }
    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);
        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 쇼핑검색어 작업 시작");
                _handler.sendEmptyMessage(FIND_KEYWORD);
                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 네이버 홈으로 이동");
             //   webViewLoad(msg, Config.HOME_URL);
                webViewLoad(msg, "https://m.naver.com");
                break;
            }

            case FIND_KEYWORD:{
                Log.d(TAG, 키워드+" 검색 시작");
                try {
                    키워드 =  URLEncoder.encode(키워드, "UTF-8");
                } catch (UnsupportedEncodingException e) {

                }
                webViewLoad(msg, "https://msearch.shopping.naver.com/search/all?query="+ 키워드);
                break;
            }

            case FIND_SHOP_KEYWORD:{
                Log.d(TAG, 키워드+" 카테고리 검색 시작");
                webViewLoad(msg, urls);
                break;
            }

            case FIND_CONTENT: {
                Log.d(TAG, "# 상품 찾기");
                _touchUrlPatternAction.workInThread();
                if (_touchUrlPatternAction.isFind()) {
                        _handler.sendEmptyMessageDelayed(STAY_RANDOM, MathHelper.randomRange(6000, 7000));
                }else{
                    _handler.sendEmptyMessageDelayed(STAY_RANDOM, MathHelper.randomRange(6000, 7000));
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
                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 6000));
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
                Log.d(TAG, "# 쇼핑검색어 패턴 종료");
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

                _handler.sendEmptyMessageDelayed(FIND_KEYWORD, MathHelper.randomRange(5000, 6000));
                break;
            }

            case FIND_KEYWORD: {
                Log.d(TAG, "# 카테고리 검색");
                _handler.sendEmptyMessageDelayed(FIND_SHOP_KEYWORD, MathHelper.randomRange(5000, 6000));
                break;
            }

            case FIND_SHOP_KEYWORD: {
                Log.d(TAG, "# 동작");
                _handler.sendEmptyMessageDelayed(FIND_CONTENT, MathHelper.randomRange(5000, 6000));
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(FIND_CONTENT, 1000);
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
}
