package com.sec.android.app.sbrowser.pattern.naver.active;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.models.NnbData;
import com.sec.android.app.sbrowser.pattern.RandomSwipePatternAction;
import com.sec.android.app.sbrowser.pattern.action.NaverCookieOtherAction;
import com.sec.android.app.sbrowser.pattern.action.NaverCookieStatusAction;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.naver.NaverHomeAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;

import java.util.ArrayList;

public class NaverMailActivePatternMessage extends NaverPatternMessage {

    private static final String TAG = NaverMailActivePatternMessage.class.getSimpleName();

    public static final int START_MAIL_PATTERN = 1000;
    public static final int END_MAIL_PATTERN = START_MAIL_PATTERN + 1;
    private static final int TOUCH_BACK_BUTTON = END_MAIL_PATTERN + 1;
    private static final int RANDOM_SCROLL = TOUCH_BACK_BUTTON + 1;
    private static final int PICK_MAIL = RANDOM_SCROLL + 1;
    private static final int TOUCH_MAIL = PICK_MAIL + 1;
    private static final int MAIL_BACK = PICK_MAIL + 2;

//    private static final int MAIL_BACK = TOUCH_MAIL + 1;


    // 액션: 메일 클릭, 스크롤, 편지함 탭 이동, 메뉴버튼 액션



//    private static final int PICK_VIEW_CONTENT = TOUCH_HOME_PAN_BUTTON + 9;
//    private static final int TOUCH_VIEW_CONTENT = TOUCH_HOME_PAN_BUTTON + 10;
//    private static final int VIEW_WEB_BACK = TOUCH_HOME_PAN_BUTTON + 11;
//    private static final int PICK_SITE_CONTENT = TOUCH_HOME_PAN_BUTTON + 12;
//    private static final int TOUCH_SITE_CONTENT = TOUCH_HOME_PAN_BUTTON + 13;
//    private static final int WEB_BACK_NEXT = TOUCH_HOME_PAN_BUTTON + 14;

    private final NaverMailPageAction _mailPageAction;
    private final NaverHomeAction _homeAction;
    private final RandomSwipePatternAction _randomSwipePatternAction;
    private final SwipeThreadAction _swipeAction;
    protected final NaverCookieStatusAction _cookieStatusAction;
    protected final NaverCookieOtherAction _cookieOtherAction;

    private int _nextMessage = 0;
    private int _step = 0;
    private int _waitCount = 0;
    private int _mailPickCount = 0;
    private int _mailPickRetryCount = 0;
    private NnbData _nnbData = null;
    private boolean _isLoginCookieExpired = false;
    private int _scrollType = 0;
    private int _scrollCount1 = 0;
    private int _scrollCount2 = 0;
    private int _randomRange = 6;
    private boolean _isLastUp = false;
    private boolean _isViewFail = false;
    private boolean _isSiteSearch = false;

    private int _startHomeMode = 0;
    private int _randomClickCount = 0;
    private int _randomClickWorkCount = 0;
    private boolean _foundRandomItem = true;
    private ArrayList<String> _randomMids = new ArrayList<>();
    private boolean _workMore = true;

    public NaverMailActivePatternMessage(WebViewManager manager, KeywordItemMoon item) {
        super(manager);
        _item = item;
        _randomClickCount = item.item.randomClickCount;

        // 랜덤클릭이 필요하다면 랜덤아이템을 찾는다.
        if (_randomClickCount > 0) {
            _foundRandomItem = false;
        }

        _mailPageAction = new NaverMailPageAction(manager.getWebView());
        _homeAction = new NaverHomeAction(manager.getWebView());
        manager.setPatternAction(_mailPageAction);

        _randomSwipePatternAction = new RandomSwipePatternAction(manager.getWebView().getContext());

        _cookieStatusAction = new NaverCookieStatusAction(manager.getWebView().getContext());
        _cookieOtherAction = new NaverCookieOtherAction(manager.getWebView().getContext());

        TouchInjector injector = new TouchInjector(manager.getWebView().getContext());
        injector.setSoftKeyboard(new SamsungKeyboard());

        _swipeAction = new SwipeThreadAction(injector);
        getResultAction().item = item;
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        _handler = handler;

        switch (msg.what) {
            case START_MAIL_PATTERN: {
                Log.d(TAG, "# 네이버 메일 패턴 시작");
                _nnbData = UserManager.getInstance().nnbData;
                _handler.sendEmptyMessage(PICK_MAIL);
                break;
            }

            case PICK_MAIL: {
                Log.d(TAG, "# 네이버 메일 선택");
                if (_mailPageAction.pickRandomMail()) {
                    _handler.sendEmptyMessageDelayed(TOUCH_MAIL, MathHelper.randomRange(2000, 3000));
                } else {
                    if (_mailPickRetryCount < 10) {
                        if (_mailPickCount < 5) {
                            Log.d(TAG, "# 네이버 메일 선택 실패로 3초 후 다시 시도..." + _mailPickCount);
                            ++_mailPickCount;
                            _handler.sendEmptyMessageDelayed(msg.what, 3000);
                        } else {
                            Log.d(TAG, "# 네이버 메일 선택 실패로 새로고침");
                            SystemClock.sleep(MathHelper.randomRange(3500, 5000));
                            ++_mailPickRetryCount;
                            _webViewManager.reload();
                            webViewLoading(msg);
                        }
                    } else {
                        Log.d(TAG, "# 네이버 메일 선택 로딩에러로 처리 중단.");
                        _workCode = 110130;
                        _handler.sendEmptyMessageDelayed(END_MAIL_PATTERN, 3000);
                    }
                }
                break;
            }

            case TOUCH_MAIL: {
                Log.d(TAG, "# 네이버 메일 검사");
                InsideData insideData = _mailPageAction.getMailInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 메일 터치");
                        if (_mailPageAction.touchMail()) {
                            int random = (int) MathHelper.randomRange(0, 1);
                            int nextMessage = RANDOM_SCROLL;
                            long delayMillis = MathHelper.randomRange(1000, 2000);
                            _nextMessage = MAIL_BACK;

                            switch (random) {
                                case 1:
//                                    Log.d(TAG, "메일 뒤로");
                                    nextMessage = MAIL_BACK;
                                    delayMillis = MathHelper.randomRange(3000, 5000);
                                    break;

                                case 2:
//                                    Log.d(TAG, "쿠팡 모드");
//                                    nextMessage = RANDOM_SCROLL;
                                    break;

                                default:
//                                    Log.d(TAG, "랜덤 스크롤");
                                    break;
                            }

                            _handler.sendEmptyMessageDelayed(nextMessage, delayMillis);
                        } else {
                            Log.d(TAG, "# 네이버 메일 터치 실패로 패턴종료.");
                            _workCode = 110131;
                            _handler.sendEmptyMessageDelayed(END_MAIL_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 800) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _isLastUp = false;
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        if (insideData.rect.top < -800) {
                            _swipeAction.swipeUpFast(110, 200);
                        } else {
                            _swipeAction.swipeUp();
                        }
                        _isLastUp = true;
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 네이버 메일 못찾아서 패턴종료.");
                    _workCode = 110132;
                    _handler.sendEmptyMessageDelayed(END_MAIL_PATTERN, 3000);
                }
                break;
            }

            case RANDOM_SCROLL: {
//                if (_nextMessage == WEB_BACK) {
//                    if (_item.item.randomScrollCount > 0) {
//                        _randomRange = _item.item.randomScrollCount;
//                    } else if (_item.item.randomScrollCount < 0) {
//                        _randomRange = 0;
//                    }
//                }

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
    //                            int isUp = (int) MathHelper.randomRange(0, 1);
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

//            case TOUCH_BACK_BUTTON: {
//                Log.d(TAG, "# 백 버튼 터치");
//                pressBackButton();
//                webViewLoading(msg);
//                break;
//            }

            case MAIL_BACK: {
                Log.d(TAG, "# 네이버 메일 뒤로");
                webViewGoBack(msg);
                break;
            }


//            case SCROLL_BOTTOM: {
//                Log.d(TAG, "# 하단으로 이동");
//                if (_scrollType == 0) {
//                    _swipeAction.swipeDownFast(65, 80);
//                } else {
//                    // 네이버 패치로 조금 천천히로 수정 //2022.01.29
////                    _swipeAction.swipeDownFast(45, 55);
//                    _swipeAction.swipeDownFast(115, 125);
//                }
//                _handler.sendEmptyMessageDelayed(_nextMessage, MathHelper.randomRange(3000, 4000));
//                break;
//            }

//            case STAY_RANDOM: {
//                Log.d(TAG, "# 랜덤 스테이 진행");
//                _randomSwipePatternAction.randomSwipe();
//                _handler.sendEmptyMessageDelayed(WEB_BACK, MathHelper.randomRange(3000, 6000));
//                break;
//            }
//
//            case WEB_BACK: {
//                Log.d(TAG, "# 웹뷰 뒤로");
//                webViewGoBack(msg);
//                break;
//            }
//
//            case TOUCH_LOGO: {
//                Log.d(TAG, "# 로고 버튼 터치");
//                _action.touchLogoButton();
//                webViewLoading(msg);
//                break;
//            }

            case END_MAIL_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# 네이버 메일 패턴 종료");

                // 쿠키 정보 있으면 업로드.
//                if (_nnbData != null && _nnbData.naverCookieId > 0) {
//                    CookieFileManager manager = new CookieFileManager();
//                    String others = manager.getAllCookieString(_webViewManager.getWebView().getContext(), ".naver.com");
//                    _cookieOtherAction.registerNaverCookieOthers(_nnbData.naverCookieId, others);
//                }

//                if (_isLoginCookieExpired) {
//                    _workCode += 5000;
//                }

//                registerResultFinish();
                _mailPageAction.endPattern();
                _action.endPattern();
                _homeAction.endPattern();
//                sendEndPatternMessage();
                break;
            }

//            case PAUSE_PATTERN: {
//                Log.d(TAG, "# 패턴 중단");
//                break;
//            }
        }
    }

    @Override
    public void onPageLoaded(String url) {
        switch (_lastMessage) {
            case PICK_MAIL: {
                Log.d(TAG, "# 네이버 메일 선택 새로고침 후 동작");
                _handler.sendEmptyMessageDelayed(PICK_MAIL, MathHelper.randomRange(3000, 5000));
                break;
            }

            case TOUCH_MAIL: {
                Log.d(TAG, "# 네이버 메일 터치 후 동작");
//                _nextMessage = VIEW_WEB_BACK;
//                _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(4000, 6000));
                break;
            }

            case TOUCH_BACK_BUTTON: {
                Log.d(TAG, "# 백 버튼 터치 후 동작");
                _workCode = 110901;
                _handler.sendEmptyMessageDelayed(END_MAIL_PATTERN, MathHelper.randomRange(3000, 5000));
                break;
            }

            case MAIL_BACK: {
                Log.d(TAG, "# 네이버 메일 터치 후 동작");
                _handler.sendEmptyMessageDelayed(END_MAIL_PATTERN, MathHelper.randomRange(3000, 5000));
//                _nextMessage = VIEW_WEB_BACK;
//                _handler.sendEmptyMessageDelayed(RANDOM_SCROLL, MathHelper.randomRange(4000, 6000));
                break;
            }

            case WEB_BACK: {
                Log.d(TAG, "# 웹뷰 뒤로 후 동작");
//                if (_randomClickWorkCount < _randomClickCount) {
//                    // 마지막 작업은 원래 mid로 한다.
//                    if (_randomMids.size() >= _randomClickCount) {
//                        _mid = _item.mid1;
//                    } else {
//                        _foundRandomItem = false;
//                    }
//
//                    ++_randomClickWorkCount;
//                    Log.d(TAG, "# 랜덤 클릭 작업수: " + _randomClickWorkCount + " / " + _randomClickCount);
//                    _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(4000, 5000));
//                } else {
//                    SystemClock.sleep(MathHelper.randomRange(1000, 2500));
//                    _webViewManager.goBack();
//
//                    if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_ABC_BACK) {
//                        if (_searchStep < 4) {
//                            ++_searchStep;
//                            _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(4000, 5000));
//                        } else {
//                            _workCode = 110905;
//                            _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
//                        }
//                    } else if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_AI_NEWS_VIEW) {
//                        _workCode = 110906;
//                        _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
//                    } else if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_URL_CHANGE) {
//                        _workCode = 110907;
//                        _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
//                    } else {
//                        if (_randomMids.size() > 0) {
//                            _workCode = 110904;
//                        } else {
//                            _workCode = 110902;
//                        }
//                        _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(4000, 5000));
//                    }
//                }
                break;
            }

//            case TOUCH_LOGO: {
//                Log.d(TAG, "# 로고 버튼 터치 후 동작");
//                _workCode = 110903;
//                _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
//                break;
//            }

        }

        _lastMessage = -1;
    }

}
