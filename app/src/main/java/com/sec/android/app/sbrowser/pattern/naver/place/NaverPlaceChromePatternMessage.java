package com.sec.android.app.sbrowser.pattern.naver.place;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.sec.android.app.sbrowser.R;
import com.sec.android.app.sbrowser.engine.ImageFinder;
import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;
import com.sec.android.app.sbrowser.models.BookmarkItem;
import com.sec.android.app.sbrowser.models.KeywordItem;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.pattern.RandomSwipePatternAction;
import com.sec.android.app.sbrowser.pattern.action.ProcessAction;
import com.sec.android.app.sbrowser.pattern.action.ResultAction;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.naver.NaverHomeAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverLoginChromePatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternAction;

import java.util.List;

public class NaverPlaceChromePatternMessage extends NaverLoginChromePatternMessage {

    private static final String TAG = NaverPlaceChromePatternMessage.class.getSimpleName();

    public static final int CHECK_FIRST_LAUNCH = 5000;
    public static final int CHECK_URL_LOADED = 5003;
    public static final int TOUCH_URL_BAR = 5011;
    public static final int CHECK_HOME_MODE = 5012;
    public static final int CHECK_ALLOW_SHORTCUT = 5050;
    public static final int CHECK_SEARCH_BAR_BUTTON = 5013;
    public static final int CHECK_KEYBOARD = 5014;
    public static final int CHECK_CONTENT = 5016;
    public static final int RANDOM_SCROLL = 5030;

    public static final int CHECK_BOOKMARK = 5040;
    public static final int CHECK_BOOKMARK_BUTTON = CHECK_BOOKMARK + 1;
    public static final int TOUCH_ADD_BUTTON = CHECK_BOOKMARK + 2;
    public static final int TOUCH_SAVE_BUTTON = CHECK_BOOKMARK + 3;
    public static final int CHECK_BOOKMARK_RESULT = CHECK_BOOKMARK + 4;

    public static final int FIND_NAVER_VIEW_AREA = 5020;

    private static final int CLEAR_SEARCH_BAR = 500;





    private static final int FIND_KEYWORD = 45;
    private static final int FIND_CONTENT = 46;
    private static final int RUN_AFTER = 47;

//    private static final int RANDOM_SCROLL = 50;
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


    private final ProcessAction _processAction;
    private final NaverPatternAction _action;
    private final NaverHomeAction _homeAction;
    private final NaverPlacePageAction _placePageAction;
    private final NaverPlacePhoneAction _phoneAction;
    private final NaverPlaceTouchUrlPatternAction _touchUrlPatternAction;
    protected final NaverPlaceResultAction _resultPatternAction;
    protected final NaverPlaceBookmarkAction _bookmarkAction;

    private final NaverPlaceAfterPatternAction _afterPatternAction;
    private final RandomSwipePatternAction _randomSwipePatternAction;
    private final SwipeThreadAction _swipeAction;

    private final Context _context;

    private KeywordItemMoon _item = null;

    private int _startHomeMode = 0;

    private boolean _take = true;
    private int _scrollCount = 0;
    private int _startPageCheckCount = 0;

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

    private int _bookmarkId = 0;
    private int _tempStep = 0;

    private InsideData _prevInsideData = null;

    public NaverPlaceChromePatternMessage(WebViewManager manager, KeywordItemMoon item) {
        super(manager);
        _context = manager.getWebView().getContext();

        _item = item;
        _keyword = item.keyword;
        _code = item.code;
        _startHomeMode = item.shopHome;

        _processAction = new ProcessAction(manager.getWebView().getContext());

        _action = new NaverPatternAction(manager.getWebView());
        _homeAction = new NaverHomeAction(manager.getWebView());
        _placePageAction = new NaverPlacePageAction(manager.getWebView());
        _phoneAction = new NaverPlacePhoneAction(manager.getWebView());
        _touchUrlPatternAction = new NaverPlaceTouchUrlPatternAction(manager.getWebView(), _code);
        _touchUrlPatternAction.item = item;

        _resultPatternAction = new NaverPlaceResultAction();
        _resultPatternAction.item = item;

        _bookmarkAction = new NaverPlaceBookmarkAction(manager.getWebView().getContext());

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
        if (msg.what == START_PATTERN) {
            if (_startHomeMode == 1) {
                if (_tempStep == 0) {
                    ++_tempStep;
                    _processAction.checkForegroundChromeUrl("https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m&query=");
                } else {
                    _processAction.checkForegroundChromeUrl("https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m&query=");
                }
            } else {
                _processAction.checkForegroundChromeUrl("https://m.naver.com/");
            }
        } else {
            _processAction.checkForegroundChrome();
        }

        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 네이버 플레이스(크롬) 작업 시작");
                if (_startHomeMode == 1) {
                    _currentPageResourceId = R.drawable.t_search_empty_input;
                    sendMessageDelayed(CHECK_FIRST_LAUNCH, MathHelper.randomRange(4000, 6000));
                } else {
                    // 우선은 다른로직 만들기 전까지 놔누자.
                    _currentPageResourceId = R.drawable.t_search_empty_input;
                    sendMessageDelayed(CHECK_FIRST_LAUNCH, 5000);
                }
                break;
            }

            case CHECK_FIRST_LAUNCH: {
                Log.d(TAG, "# 첫 페이지 로딩 검사");
                Rect rc = findResourceFromScreen(_currentPageResourceId);
                if (rc != null) {
                    Log.d(TAG, "# 첫 페이지 로딩 확인 : " + rc);
                    if (_item.item.useNid == 1) {
                        _loginNextMessage = TOUCH_SEARCH_BAR;
                        sendMessageDelayed(CHECK_LOGIN, 100);
                    } else {
                        sendMessageDelayed(TOUCH_SEARCH_BAR, 100);
                    }
                } else {
                    if (_startPageCheckCount < 2) {
                        Log.d(TAG, "첫 페이지 로딩 못찾아서 다시 시도: " + _startPageCheckCount);
                        ++_startPageCheckCount;
                        _action.touchBackButton();
                        _handler.sendEmptyMessageDelayed(START_PATTERN, 2000);
                    } else {
                        Log.d(TAG, "# 첫 페이지 로딩 못찾아서 패턴 종료..." + _startPageCheckCount);
                        _workCode = 131001;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                }
                break;
            }

            case TOUCH_SEARCH_BAR: {
                Log.d(TAG, "# 네이버 검색창 검사");

                if (_startHomeMode == 1) {
                    Rect rc = findResourceFromScreen(R.drawable.t_search_empty_input);
                    if (rc != null) {
                        Log.d(TAG, "# 검색창 터치: " + rc);
                        // ADB 에서 붙여넣기 하기위해서 사용함.
//                        _action.touchScreenAdb(rc, 20);
                        _action.touchScreen(rc, 20);
                        // 시간이 길어지면 붙여넣기가 안되서 약간 빠르게 처리.
                        SystemClock.sleep(MathHelper.randomRange(1000, 1200));
                        inputKeyword();
//                        _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
//                        sendMessageDelayed(CHECK_KEYBOARD, MathHelper.randomRange(3000, 4000));
                    } else {
                        Log.d(TAG, "# 검색창을 못찾아서 다시 시도: " + _retryCount);
                        if (!resendMessageDelayed(msg.what, 500, 3)) {
                            Log.d(TAG, "# 검색창을 못찾아서 스크롤 후 주소창 터치");
                            _swipeAction.swipeUpAi();
                            sendMessageDelayed(TOUCH_URL_BAR, 1000);
                        }
                    }
                } else {
                    Rect rc = findResourceFromScreen(R.drawable.t_home_naver_icon);
                    if (rc != null) {
                        Log.d(TAG, "# 검색창 터치: " + rc);
                        // 홈 검색창 위부분을 찾는것이므로 크기만큼 더한다.
                        rc.offset(0, rc.height());
                        rc.right -= 30;
                        _action.touchScreen(rc, 20);
                        sendMessageDelayed(CHECK_SEARCH_BAR_BUTTON, MathHelper.randomRange(3000, 4000));
                    } else {
                        Log.d(TAG, "# 검색창을 못찾아서 다시 시도: " + _retryCount);
                        if (!resendMessageDelayed(msg.what, 500, 3)) {
                            Log.d(TAG, "# 검색창을 못찾아서 스크롤 후 주소창 터치");
                            _swipeAction.swipeUpAi();
                            sendMessageDelayed(TOUCH_URL_BAR, 1000);
                        }
                    }
                }
                break;
            }

            case INPUT_KEYWORD: {
                if (_startHomeMode == 1) {
                    Log.d(TAG, "# 검색어 입력 - 기존처리");
                    inputKeyword();
                } else {
                    Log.d(TAG, "# 검색어 입력");
//                    _action.extractStrings(_item.search + " " + _item.target);
                    _action.inputKeyword();
                    sendMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1500, 2000));
                }
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치");

                // 키보드의 검색버튼 클릭
                 _action.touchSearchButton();
                sendMessageDelayed(CHECK_URL_LOADED, MathHelper.randomRange(3000, 4000));
                break;
            }

            case CHECK_URL_LOADED: {
                Log.d(TAG, "# 페이지 로딩 검사");
                Rect rc = findResourceFromScreen(R.drawable.t_bar_mic);
                if (rc != null) {
                    Log.d(TAG, "# 페이지 로딩 확인: " + rc);
//                    saveSbrowserData();
//                    ++_step;
//                    setRandomSwipe(3, 5);
//                    sendMessageDelayed(RANDOM_SCROLL, 1000);

                    sendMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(2000, 2500));
                } else {
                    Log.d(TAG, "# 페이지 로딩이 완료되지 않아서 다시검사: " + _retryCount);
                    if (!resendMessageDelayed(msg.what, 2000, 10)) {
                        Log.d(TAG, "# 페이지 로딩이 완료되지 않아서 패턴 종료!!");
                        _workCode = 131010;
                        sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                    }
                }
                break;
            }

            case RANDOM_SCROLL: {
                Log.d(TAG, "# 랜덤 스크롤");
                if (_nextMessage == WEB_BACK || _nextMessage == END_PATTERN) {
                    if (_item.item.randomScrollCount > 0) {
                        _randomRange = _item.item.randomScrollCount;
                    } else if (_item.item.randomScrollCount < 0) {
                        _randomRange = 0;
                    }
                }

                if (_randomRange > 0) {
                    int count = (int) MathHelper.randomRange(_randomRange, _randomRange + 4);
                    Log.d(TAG, "# 랜덤 스크롤: " + count);
                    boolean isUp = false;
                    int sameCount = 0;

                    for (int i = 0; i < count; ++i) {
                        if (i < 5) {
                            Log.d(TAG, "아래로 스크롤");
                            ++sameCount;
                            _swipeAction.swipeDown(false);
                        } else {
                            if (sameCount < 2) {
                                ++sameCount;
                            } else {
                                isUp = !isUp;
                                sameCount = 1;
//                                int isUp = (int) MathHelper.randomRange(0, 1);
                            }

                            if (!isUp) {
                                Log.d(TAG, "아래로 스크롤");
                                _swipeAction.swipeDown(false);
                            } else {
                                Log.d(TAG, "위로 스크롤");
                                _swipeAction.swipeUp(false);
                            }
                        }

                        SystemClock.sleep(MathHelper.randomRange(1300, 2500));
                    }

                    _handler.sendEmptyMessageDelayed(_nextMessage, MathHelper.randomRange(2000, 3000));
                } else {
                    _handler.sendEmptyMessageDelayed(_nextMessage, 100);
                }
                break;
            }

            case TOUCH_CONTENT: {
                Log.d(TAG, "# 네이버 플레이스(크롬) 컨텐츠 검사");
                int retryMessage = msg.what;

                findUrlImageFromScreen(new ImageFinder.FinderCallback() {
                    @Override
                    public void findSuccess(List<Rect> rectList) {
                        if (rectList != null && rectList.size() > 0) {
                            Rect rc = rectList.get(0);

                            // 랜덤 패턴 만들어야함.
//                            int pattern = (int) MathHelper.randomRange(0, 2);

                            Log.d(TAG, "# 네이버 플레이스(크롬) 컨텐츠 터치: " + rc);
                            _action.touchScreen(rc, 10);

                            _result = ResultAction.SUCCESS;

                            if (_loginCookieAction.isLoginChrome()) {
                                sendMessageDelayed(CHECK_BOOKMARK, 100);
                            } else {
                                _workCode = 131911;
                                _nextMessage = END_PATTERN;
                                _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(4000, 7000));
                            }
                        } else {
//                            if (_scrollCount < MAX_SCROLL_COUNT) {
//                                Log.d(TAG, "# 에이블리 컨텐츠 못찾아서 아래로 스크롤..." + _scrollCount);
//                                ++_scrollCount;
//                                _swipeAction.swipeDownFast(110, 200);
//                                _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
//                            } else {
//                                Log.d(TAG, "# 에이블리 컨텐츠 못찾아서 패턴 종료..." + _scrollCount);
//                                _workCode = 230023;
//                                _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
//                            }

                            if (_retryCount < 2) {
                                Log.d(TAG, "# 네이버 플레이스(크롬) 컨텐츠를 못찾아서 2번까지 재시도: " + _retryCount);
                            } else {
                                Log.d(TAG, "# 네이버 플레이스(크롬) 컨텐츠를 못찾아서 아래로 스크롤: " + _retryCount);
                                _swipeAction.swipeDown(true);
                            }

                            if (!resendMessageDelayed(retryMessage, 2000, 20)) {
                                Log.d(TAG, "# 네이버 플레이스(크롬) 컨텐츠를 못찾아서 패턴 종료!!");
                                _workCode = 131021;
                                sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                            }
                        }
                    }

                    @Override
                    public void findFailed(int failCode) {
                        _take = true;
                        Log.d(TAG, "# 네이버 플레이스(크롬) 컨텐츠를 찾기 실패(" + failCode + ")로 다시검사: " + _retryCount);
                        if (!resendMessageDelayed(retryMessage, 2000, 10)) {
                            Log.d(TAG, "# 네이버 플레이스(크롬) 컨텐츠를 못찾아서 패턴 종료!!");
                            _workCode = 131022;
                            sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                        }
                    }
                }, _take, _item.item.sourceUrl);

//                List<Rect> rcList = findResourceFromScreen(R.drawable.t_view_content);
//                if (rcList != null && rcList.size() > 0) {
//                    int index = (int) MathHelper.randomRange(0, rcList.size() - 1);
//                    Rect rc = rcList.get(index);
//                    Log.d(TAG, "# 네이버 뷰 컨텐츠 확인(" + index + "): " + rc);
//                    rc.left = 60;
//                    rc.right = 950;
//                    _action.touchScreen(rc, 15);
//                    sendMessageDelayed(CHECK_URL_LOADED, 2000);
//                } else {
//                    Log.d(TAG, "네이버 뷰 컨텐츠를 못찾아서 다시검사: " + _retryCount);
//                    if (!resendMessageDelayed(msg.what, 2000, 10)) {
//                        Log.d(TAG, "네이버 뷰 컨텐츠를 못찾아서 패턴 종료!!");
//                        sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
//                    }
//                }
                break;
            }

            case CHECK_BOOKMARK: {
                Log.d(TAG, "# 북마크 여부 검사");
//                int accountAuthId = 1;

//                if (_bookmarkAction.checkBookmark(accountAuthId) == 1) {
                if (_bookmarkAction.checkBookmarkAut(_loginCookieAction.getNidAutChrome()) == 1) {
                    Log.d(TAG, "# 북마크 작업 진행");
                    BookmarkItem bookmarkItem = _bookmarkAction.getBookmarkItem();

                    if (bookmarkItem != null) {
                        _bookmarkId = bookmarkItem.bookmarkId;
                        sendMessageDelayed(CHECK_BOOKMARK_BUTTON, MathHelper.randomRange(4000, 7000));
                    } else {
                        // 여기는 아에 들어올 일이 없다.
                        Log.d(TAG, "# 북마크 아이디가 없어서 다음으로...");
                        _workCode = 131902;
                        _nextMessage = END_PATTERN;
                        _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(4000, 7000));
                        break;
                    }
                } else {
                    _workCode = 131901;
                    _nextMessage = END_PATTERN;
                    _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(4000, 7000));
                    break;
                }
                break;
            }

            case CHECK_BOOKMARK_BUTTON: {
                Log.d(TAG, "# 북마크 버튼 검사");
                Rect rc = findResourceFromScreen(R.drawable.t_place_bookmark_off);
                if (rc != null) {
                    Log.d(TAG, "# 북마크 버튼 터치: " + rc);
                    _action.touchScreen(rc, 20);
                    sendMessageDelayed(TOUCH_ADD_BUTTON, MathHelper.randomRange(2000, 2500));
                } else {
                    Log.d(TAG, "# 이미 북마크 중인지 검사");
                    rc = findResourceFromScreen(R.drawable.t_place_bookmark_on);
                    if (rc != null) {
                        Log.d(TAG, "# 이미 북마크 중인지 확인: " + rc);
                        _bookmarkAction.registerBookmarkFinish(_bookmarkId, 0, 0);
                        _workCode = 131903;
                        _nextMessage = END_PATTERN;
                        _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(100, 3000));
                    } else {
                        Log.d(TAG, "# 북마크 버튼 못찾아서 위로 스크롤: " + _retryCount);
                        _swipeAction.swipeUp(true);

                        if (!resendMessageDelayed(msg.what, 2000, 3)) {
                            Log.d(TAG, "# 북마크 버튼 못찾아서 다음으로...");
                            _workCode = 131904;
                            _nextMessage = END_PATTERN;
                            _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(4000, 7000));
                        }
                    }
                }
                break;
            }

            case TOUCH_ADD_BUTTON: {
                Log.d(TAG, "# 내장소 버튼 검사");
                Rect rc = findResourceFromScreen(R.drawable.t_place_bookmark_add);
                if (rc != null) {
                    Log.d(TAG, "# 내장소 버튼 터치: " + rc);
                    _action.touchScreen(rc, 20);
                    sendMessageDelayed(TOUCH_SAVE_BUTTON, MathHelper.randomRange(1500, 2500));
                } else {
                    Log.d(TAG, "# 내장소 버튼 못찾아서 다시 시도: " + _retryCount);
                    if (!resendMessageDelayed(msg.what, 500, 3)) {
                        Log.d(TAG, "# 내장소 버튼 못찾아서 패턴 종료!!");
                        _workCode = 131905;
                        sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                    }
                }
                break;
            }

            case TOUCH_SAVE_BUTTON: {
                Log.d(TAG, "# 저장 버튼 검사");
                Rect rc = findResourceFromScreen(R.drawable.t_place_save);
                if (rc != null) {
                    Log.d(TAG, "# 저장 버튼 터치: " + rc);
                    _action.touchScreen(rc, 20);
                    sendMessageDelayed(CHECK_BOOKMARK_RESULT, MathHelper.randomRange(2500, 3500));
                } else {
                    Log.d(TAG, "# 저장 버튼 못찾아서 다시 시도: " + _retryCount);
                    if (!resendMessageDelayed(msg.what, 500, 3)) {
                        Log.d(TAG, "# 저장 버튼 못찾아서 패턴 종료!!");
                        _workCode = 131906;
                        sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                    }
                }
                break;
            }

            case CHECK_BOOKMARK_RESULT: {
                Log.d(TAG, "# 북마크 결과 검사");
                Rect rc = findResourceFromScreen(R.drawable.t_place_bookmark_on);
                if (rc != null) {
                    Log.d(TAG, "# 북마크 결과 확인: " + rc);
                    _bookmarkAction.registerBookmarkFinish(_bookmarkId, 0, 0);
                    _workCode = 131907;
                    _nextMessage = END_PATTERN;
                    _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(100, 3000));
                } else {
                    Log.d(TAG, "# 북마크 결과 못찾아서 위로 스크롤: " + _retryCount);
                    _swipeAction.swipeUp(true);

                    if (!resendMessageDelayed(msg.what, MathHelper.randomRange(1700, 2300), 3)) {
                        Log.d(TAG, "# 북마크 결과 못찾아서 다음으로...");
                        _bookmarkAction.registerBookmarkFinish(_bookmarkId, 1, 0);
                        _workCode = 131908;
                        _nextMessage = END_PATTERN;
                        _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(100, 3000));
                    }
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
                Log.d(TAG, "# 네이버 플레이스(크롬) 키워드 패턴 종료");
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
                Log.d(TAG, "# 네이버 플레이스(크롬) 컨텐츠 터치 후 동작");
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

//    protected List<Rect> findResourceFromScreen(int id) {
//        return ImageFinder.getInstance().findResourceFromScreen(_context, id);
//    }

    protected Rect findResourceFromScreen(int id) {
        List<Rect> rcList = ImageFinder.getInstance().findResourceFromScreen(_context, id);

        if (rcList != null && rcList.size() > 0) {
            return rcList.get(0);
        }

        return null;
    }

    protected void findUrlImageFromScreen(ImageFinder.FinderCallback callback, boolean take, String url) {
        int width;
        int height;

//        case 1: return ".UEzoS:not(.cZnHG)";        // restaurant a link(소래포구맛집)
//        case 2: return ".Fh8nG:not(.ocbnV)";        // accommodation a link (강릉 펜션)
//        case 3: return ".p0FrU:not(._0Ynn)";        // hairshop, nailshop a link(강남 미용실)
//        case 4: return ".DWs4Q:not(.bjvIv)";        // hospital a link(정자동 치과)
//        case 5: return ".Ki6eC:not(.xE3qV)";        // attraction a link(외도유람선)
//        case 6: return "._9v52G:not(.EykuO)";       // place a link(거제유람선)
//        case 7: return "";                          // place 단독
//
//        default: return ".VLTHu:not(.hTu5x)";       // place a link(강남 문구)

        switch (_item.item.sourceType) {
            case 1: // 세로긴 형태 (소래포구 맛집, 강릉 펜션, 거제유람선)
            case 2:
            case 6:
                width = 104;
                height = 139;
                break;

            case 3: // 세로긴 형태2 (강남 미용실)
                width = 108;
                height = 59;    //119
                break;

            case 7: // 단독 형태 (홍은동 카페 무지개)
                width = 119;
                height = 125;
                break;

            case 0:
            case 4:
            case 5:
            default: // 일반 형태 (강남 문구, 정자동 치과, 외도유람선)
                width = 88;
                height = 48;
                break;

        }

        ImageFinder.getInstance().findUrlImageFromScreen(callback, take, width, height, url);
    }

//    protected Rect getRectFromScreen(Message msg, int id) {
//        List<Rect> rcList = findResourceFromScreen(id);
//        if (rcList != null && rcList.size() > 0) {
//            return rcList.get(0);
//        } else {
//            Log.d(TAG, "# 검색 버튼을 못찾아서 다시 시도: " + _retryCount);
//            if (!resendMessageDelayed(msg.what, 500, 3)) {
//                Log.d(TAG, "# 검색 버튼을 못찾아서 패턴 종료..");
//                sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
//            }
//        }
//
//        return null;
//    }

    public void inputKeyword() {
//        if (_item.item.workType == KeywordItem.WORK_TYPE_INPUT) {
//            Log.d(TAG, "# 검색어 삽입: " + _keyword);
//            // 인풋태그에 값 넣기
//            _homeAction.inputSearchBar(false, _keyword);
//
//            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1300, 3000));
//        } else
        if (_item.item.workType == KeywordItem.WORK_TYPE_CLIPBOARD) {
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
                    _workCode = 131012;
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
