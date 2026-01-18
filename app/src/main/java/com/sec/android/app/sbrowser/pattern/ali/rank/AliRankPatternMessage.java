package com.sec.android.app.sbrowser.pattern.ali.rank;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;
import com.sec.android.app.sbrowser.models.KeywordItem;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.pattern.action.RankResultAction;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.ali.AliPatternMessage;
import com.sec.android.app.sbrowser.pattern.ali.action.AliHomeAction;
import com.sec.android.app.sbrowser.pattern.ali.action.AliPatternAction;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class AliRankPatternMessage extends AliPatternMessage {

    private static final String TAG = AliRankPatternMessage.class.getSimpleName();

    private static final int MAX_SCROLL_COUNT = 35;
    private static final int MAX_PAGE_COUNT = 10;
    private static final int MAX_PAGE_PC_COUNT = 5;

    private static final int GO_HOME_PC = 50;
    private static final int CHECK_RANK = GO_HOME_PC + 1;
    private static final int CHECK_RANK_PC = GO_HOME_PC + 2;

    private final AliPatternAction _action;
    private final AliHomeAction _homeAction;
    private final AliShopRankAction _shopRankAction;
    private final AliPcRankAction _pcRankAction;
    private final RankResultAction _resultPatternAction;
    private final SwipeThreadAction _swipeAction;

    private KeywordItemMoon _item;
    private String _keyword = "";
    private String _parsedKeyword = "";
    private int _page = 0;
    private int _nextPopupMessage = 0;
    private int _findBarCount = 0;
    private boolean _success = false;
    private int _scrollCount = 0;

    public AliRankPatternMessage(WebViewManager manager, KeywordItemMoon item) {
        super(manager);

        _item = item;

        _action = new AliPatternAction(manager.getWebView());
        _homeAction = new AliHomeAction(manager.getWebView());
        _shopRankAction = new AliShopRankAction(manager.getWebView());
        _pcRankAction = new AliPcRankAction(manager.getWebView());
        _resultPatternAction = new RankResultAction();
        _resultPatternAction.loginId = UserManager.getInstance().getLoginId(manager.getWebView().getContext());
        _resultPatternAction.item = item;

        TouchInjector injector = new TouchInjector(manager.getWebView().getContext());
        injector.setSoftKeyboard(new SamsungKeyboard());

        _swipeAction = new SwipeThreadAction(injector);
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 알리 순위 검사 작업 시작");
                _keyword = _item.keyword;
                _parsedKeyword = _item.keyword;

                try {
                    _parsedKeyword = _parsedKeyword.replace(" ", "-");
                    _parsedKeyword = URLEncoder.encode(_parsedKeyword, "UTF-8");
                } catch (UnsupportedEncodingException e) {

                }

                if (_item.category.equals("ali")) {
                    _handler.sendEmptyMessage(GO_HOME);
                } else if (_item.category.equals("ali_pc")) {
                    _handler.sendEmptyMessage(GO_HOME_PC);
                } else {
                    Log.d(TAG, "# 알수 없는 타입 패턴종료.");
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                }

                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 알리 홈으로 이동");
                webViewLoad(msg, HOME_URL);
//                Log.d(TAG, "# 알리 결과로 이동");
//                _page = 1;
////                https://ko.aliexpress.com/w/wholesale-%ED%95%B8%EB%93%9C%ED%8F%B0-%EC%BC%80%EC%9D%B4%EB%B8%94.html?osf=direct&spm=a2g0n.home.header.0
////                https://ko.aliexpress.com/w/wholesale-%EA%B9%80%EC%B9%98%ED%86%B5.html?osf=direct&spm=a2g0n.home.header.0
//                webViewLoad(msg, "https://ko.aliexpress.com/w/wholesale-" + _parsedKeyword + ".html?osf=direct&spm=a2g0n.home.header.0");
                break;
            }

            case TOUCH_MOBILE_WEB_TOP_BANNER_CLOSE: {
                Log.d(TAG, "# 모바일웹 상단 배너 닫기 버튼 검사");

                if (_homeAction.checkTopBannerClose()) {
                    if (_homeAction.touchButton(AliHomeAction.BUTTON_MOBILE_WEB_TOP_BANNER_CLOSE)) {
                        _handler.sendEmptyMessageDelayed(_nextPopupMessage, MathHelper.randomRange(2500, 3500));
                    } else {
                        _handler.sendEmptyMessageDelayed(_nextPopupMessage, 1000);
                    }
                } else {
                    _handler.sendEmptyMessageDelayed(_nextPopupMessage, 100);
                }
                break;
            }

            case TOUCH_MOBILE_WEB_POPUP_LOGIN: {
                Log.d(TAG, "# 모바일웹 로그인 팝업 검사");

                if (_homeAction.checkFullBanner()) {
                    _webViewManager.setLoadsImagesAutomatically(false);

                    if (_homeAction.touchButton(AliHomeAction.BUTTON_MOBILE_WEB_POPUP_LOGIN_CLOSE)) {
                        SystemClock.sleep(MathHelper.randomRange(1000, 2000));

                        if (_homeAction.touchButton(AliHomeAction.BUTTON_MOBILE_WEB_POPUP_LOGIN_CANCEL)) {
                            _handler.sendEmptyMessageDelayed(_nextPopupMessage, MathHelper.randomRange(2500, 3500));
                        } else {
                            _handler.sendEmptyMessageDelayed(_nextPopupMessage, 1000);
                        }
                    } else {
                        _handler.sendEmptyMessageDelayed(_nextPopupMessage, 1000);
                    }
                } else {
                    _handler.sendEmptyMessageDelayed(_nextPopupMessage, 100);
                }
                break;
            }

            case TOUCH_SEARCH_BAR: {
                Log.d(TAG, "# 검색창 터치");
                if (_homeAction.touchButton(AliHomeAction.BUTTON_SEARCH)) {
                    _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
                } else {
                    Log.d(TAG, "# 검색창 터치에 실패해서 패턴종료.");
                    _workCode = 430001;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case INPUT_KEYWORD: {
                Log.d(TAG, "# 검색창 검사");
                if (!_homeAction.checkSearchBar()) {
                    if (_findBarCount > 3) {
                        Log.d(TAG, "# 로딩에러로 처리 중단.");
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    } else {
                        Log.d(TAG, "# 검색창이 떠있지 않아서 다시 터치");
                        ++_findBarCount;
                        _webViewManager.reload();
                        webViewLoading(msg);
//                        _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, 5000);
                    }
                } else {
                    inputKeyword(_keyword);
                }
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치");
                _action.touchSearchButton();
                webViewLoading(msg);
                break;
            }

            case CHECK_RANK: {
                Log.d(TAG, "# 알리 순위 검사: " + _item.item.code);
                if (_shopRankAction.checkRank(_item.item.code)) {
                    if (_shopRankAction.getRank() > 0) {
                        Log.d(TAG, "# 알리 순위 검사 성공");
                        // 순위 업로드.
                        _success = true;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    } else if (_scrollCount < MAX_SCROLL_COUNT) {
                        Log.d(TAG, "# 순위를 못찾아서 아래로 스크롤..." + _scrollCount);
                        ++_scrollCount;
                        _swipeAction.swipeDownFast(110, 130);
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1500, 2500));
                    } else {
                        Log.d(TAG, "# 순위를 못찾아서 패턴 종료..." + _scrollCount);
                        // 순위 업로드.
                        _success = true;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                } else {
                    Log.d(TAG, "# 알리 순위 검사 실패로 5초후 다시 시도..." + _retryCount);
                    if (!resendMessageDelayed(msg.what, 5000, 3)) {
                        Log.d(TAG, "# 알리 순위 검사 실패로 패턴종료.");
                        sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                    }
                }
                break;
            }

            case GO_HOME_PC: {
                Log.d(TAG, "# 알리PC 결과로 이동");
                _page = 1;
//                https://ko.aliexpress.com/w/wholesale-%EC%9E%90%EC%A0%84%EA%B1%B0-%ED%95%B8%EB%93%A4.html?spm=a2g0o.home.search.0
//                webViewLoad(msg, "https://www.coupang.com/np/search?q=" + _parsedKeyword + "&listSize=72");
                break;
            }

            case CHECK_RANK_PC: {
                Log.d(TAG, "# 알리PC 순위 검사");
                String currentPage =  _pcRankAction.getCurrentPage();

                if (!_pcRankAction.hasPagination()) {
                    currentPage = "1";
                }

                if (currentPage == null) {
                    Log.d(TAG, "# 현재 페이지를 못찾아서 패턴종료.");
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                } else if (Integer.parseInt(currentPage) > MAX_PAGE_PC_COUNT) {
                    Log.d(TAG, "# " + MAX_PAGE_PC_COUNT + "페이지 초과로 패턴종료.");
                    _success = true;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    int page = Integer.parseInt(currentPage);
                    if (_pcRankAction.checkRank(_item.code, page)) {
                        if (_pcRankAction.getRank() > 0) {
                            Log.d(TAG, "# 알리PC 순위 검사 성공");
                            // 순위 업로드.
                            _success = true;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                        } else {
                            if (_pcRankAction.checkNextButton()) {
                                Log.d(TAG, "# 순위를 못찾아서 다음 버튼 터치.. " + _page);
                                _pcRankAction.clickNextButton();
                                webViewLoading(msg);
                            } else {
                                Log.d(TAG, "# 다음 버튼 못찾아서 패턴종료.");
                                sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                            }
                        }
                    } else {
                        Log.d(TAG, "# 알리PC 순위 검사 실패로 5초후 다시 시도..." + _retryCount);
                        if (!resendMessageDelayed(msg.what, 5000, 3)) {
                            Log.d(TAG, "# 알리PC 순위 검사 실패로 패턴종료.");
                            sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                        }
                    }
                }
                break;
            }

            case END_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# 알리 순위 검사 패턴 종료");
                _webViewManager.goBlankPage();
                registerFinish();
                _shopRankAction.endPattern();
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
                Log.d(TAG, "# 알리 홈 이동 후 동작");
                _findBarCount = 0;
                _nextPopupMessage = TOUCH_SEARCH_BAR;
                _handler.sendEmptyMessageDelayed(TOUCH_MOBILE_WEB_TOP_BANNER_CLOSE, MathHelper.randomRange(5000, 6000));
//                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
////                _nextPopupMessage = CHECK_RANK;
////                _handler.sendEmptyMessageDelayed(TOUCH_MOBILE_WEB_POPUP, MathHelper.randomRange(5000, 6000));
//                _handler.sendEmptyMessageDelayed(CHECK_RANK, MathHelper.randomRange(5000, 6000));
                break;
            }

            case GO_HOME_PC: {
                Log.d(TAG, "# 알리PC 결과 이동 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_RANK_PC, MathHelper.randomRange(5000, 6000));
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치 후 동작");
                _nextPopupMessage = CHECK_RANK;
                _handler.sendEmptyMessageDelayed(TOUCH_MOBILE_WEB_POPUP_LOGIN, MathHelper.randomRange(3000, 5000));
//                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, 1000);
//                _handler.sendEmptyMessageDelayed(REGISTER_FINISH, 1000);
                break;
            }

            case INPUT_KEYWORD: {
                Log.d(TAG, "# 검색창 검사 새로고침 후 동작");
                _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, MathHelper.randomRange(5000, 6000));
                break;
            }

            case CHECK_RANK: {
                Log.d(TAG, "# 다음 버튼 터치 후 동작");
                _nextPopupMessage = CHECK_RANK;
                _handler.sendEmptyMessageDelayed(TOUCH_MOBILE_WEB_POPUP_LOGIN, MathHelper.randomRange(5000, 6000));
//                _handler.sendEmptyMessageDelayed(CHECK_RANK, MathHelper.randomRange(5000, 6000));
                break;
            }

            case CHECK_RANK_PC: {
                Log.d(TAG, "# 알리PC 다음 버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_RANK_PC, MathHelper.randomRange(5000, 6000));
//                _handler.sendEmptyMessageDelayed(CHECK_RANK, MathHelper.randomRange(5000, 6000));
                break;
            }
        }

        _lastMessage = -1;
    }

    public void inputKeyword(String keyword) {
        if (_item.item.workType == KeywordItem.WORK_TYPE_INPUT) {
            Log.d(TAG, "# 검색어 삽입: " + keyword);
            // 인풋태그에 값 넣기
            _homeAction.inputSearchBar(keyword);
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
                if (!_homeAction.touchSearchBarLong()) {
                    Log.d(TAG, "# 검색창 롱터치에 실패해서 패턴종료.");
                    _workCode = 430014;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    return;
                }

                SystemClock.sleep(MathHelper.randomRange(1000, 1500));
                Log.d(TAG, "# 검색어 붙여넣기");
                _homeAction.touchPasteButton();
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

    protected void registerFinish() {
        if (!_success) {
            Log.d(TAG, "# 순위 검사 실패로 서버에 등록하지 않고 패스.");
            return;
        }

        if (_item.category.equals("ali")) {
            _resultPatternAction.registerFinish(_shopRankAction.getRank(), 0);
        } else if (_item.category.equals("ali_pc")) {
            _resultPatternAction.registerFinish(_pcRankAction.getRank(), 0);
        }
    }
}
