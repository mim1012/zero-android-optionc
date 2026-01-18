package com.sec.android.app.sbrowser.pattern.naver.view;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.Config;
import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.models.KeywordItem;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.pattern.RandomSwipePatternAction;
import com.sec.android.app.sbrowser.pattern.action.ResultAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverHomeAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NaverViewPatternMessage extends NaverPatternMessage {

    private static final String TAG = NaverViewPatternMessage.class.getSimpleName();
    private static final int FIND_KEYWORD = 30;
    private static final int FIND_CONTENT = 40;
    private static final int RUN_AFTER = 41;


    private final NaverPatternAction _action;
    private final NaverHomeAction _homeAction;
    private final NaverViewTouchUrlPatternAction _touchUrlPatternAction;
    private final NaverViewRankPatternAction _rankPatternAction;
    private final NaverViewAfterPatternAction _afterPatternAction;
    private final RandomSwipePatternAction _randomSwipePatternAction;

    private int _step = 0;
    private int _findBarCount = 0;
    private String _keyword;
    String url_;
    public NaverViewPatternMessage(WebViewManager manager, KeywordItemMoon item, final String uid ,String keyword,String url) {
        super(manager);
        _item = item;
        _keyword = item.keyword;
        url_ = url;

        _action = new NaverPatternAction(manager.getWebView());
        _homeAction = new NaverHomeAction(manager.getWebView());
        _touchUrlPatternAction = new NaverViewTouchUrlPatternAction(manager.getWebView(),url_);
        _rankPatternAction = new NaverViewRankPatternAction(manager.getWebView(),url_);
        _afterPatternAction = new NaverViewAfterPatternAction(manager.getWebView(),url_);
        _randomSwipePatternAction = new RandomSwipePatternAction(manager.getWebView().getContext());

        getResultAction().item = item;

//        _touchUrlPatternAction.setFindCallback(new NaverViewTouchUrlPatternAction.FindCallback() {
//            @Override
//            public void contentFind(boolean find) {
//
//                    Log.d(TAG, "# 순위 등록");
//                    _rankPatternAction.registerRank(uid,url_);
//                    SystemClock.sleep(1000);
//
//            }
//        });

    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# View 작업 시작");
              //  _handler.sendEmptyMessage(FIND_KEYWORD);
                   _handler.sendEmptyMessage(GO_HOME);

                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 네이버 홈으로 이동");
                webViewLoad(msg, Config.NAVER_HOME_MOBILE_URL);
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
                    _workCode = 120001;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case INPUT_KEYWORD: {
                Log.d(TAG, "# 검색창 검사");
                _searchBarCheckPatternAction.checkSearchBarShown();

                if (!_searchBarCheckPatternAction.isFocus()) {
                    if (_findBarCount > 15) {
                        Log.d(TAG, "# 로딩에러로 처리 중단.");
                        _workCode = 120011;
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
                _searchBarAction.submitSearchButton();
//                _action.touchSearchButton();
                webViewLoading(msg);
                break;
            }

            case FIND_KEYWORD:{
                Log.d(TAG, _keyword +" 검색시작");
                try {
                    _keyword =  URLEncoder.encode(_keyword, "UTF-8");
                } catch (UnsupportedEncodingException e) {

                }
                webViewLoad(msg, "https://m.search.naver.com/search.naver?query="+ _keyword +"&where=m&sm=mtp_hty.top");
                break;
            }

            case REGISTER_FINISH: {


                Log.d(TAG, "# 검색어 입력완료 처리");
              //  _finishPatternAction.finishKeyword(loginId, imei);
                // 로딩이 늦어질수 있으므로 7초대기.
                _handler.sendEmptyMessageDelayed(FIND_CONTENT, 7000);
                break;
            }

            case FIND_CONTENT: {
                Log.d(TAG, "# 컨텐츠 찾기");

                // 클래스 내부 작업을 쪼개야한다..ㅠㅠ
                _touchUrlPatternAction.workInThread();

                if (_touchUrlPatternAction.isFind()) {
                    _result = ResultAction.SUCCESS;
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
                Log.d(TAG, "# View 키워드 패턴 종료");
                uploadOtherCookieInWebView();

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

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(REGISTER_FINISH, 1000);
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
                _workCode = 120901;
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
                    _workCode = 120012;
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
