//package com.sec.android.app.sbrowser.pattern.naver;
//
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//
//import com.sec.android.app.sbrowser.engine.MathHelper;
//import com.sec.android.app.sbrowser.engine.WebViewManager;
//import com.sec.android.app.sbrowser.pattern.PatternMessage;
//
//public class NaverMobilePatternMessage extends NaverPatternMessage {
//
//    private static final String TAG = NaverMobilePatternMessage.class.getSimpleName();
//
//    protected static final int GO_LOGIN = 1013;
//
//    protected static final int INPUT_KEYWORD = 1021;
//    protected static final int REGISTER_FINISH = 1022;
//    protected static final int REGISTER_RANK = 1023;
//
//    protected static final int TOUCH_LOGO = 1031;
//    protected static final int TOUCH_SEARCH_BAR = 1032;
//    protected static final int TOUCH_SEARCH_BUTTON = 1033;
//    protected static final int TOUCH_RANDOM_CONTENT = 1034;
//    protected static final int TOUCH_NEW_POPUP_OK = 1035;
//    protected static final int TOUCH_NEW_POPUP2_OK = 1036;
//
//    protected static final int STAY_RANDOM = 1041;
//
//    protected final NaverSearchBarAction _searchBarAction;
//    protected final NaverSearchBarCheckPatternAction _searchBarCheckPatternAction;
//
//    public NaverMobilePatternMessage(WebViewManager manager) {
//        super(manager);
//
//        _searchBarAction = new NaverSearchBarAction(manager.getWebView(), null);
//        _searchBarCheckPatternAction = new NaverSearchBarCheckPatternAction(manager.getWebView());
//    }
//
//    @Override
//    public void onHandleMessage(Handler handler, Message msg) {
//        super.onHandleMessage(handler, msg);
//
//        switch (msg.what) {
////            case GO_HOME: {
////                Log.d(TAG, "# 네이버 홈으로 이동");
////                webViewLoad(msg, Config.HOME_URL);
////                break;
////            }
//
//            case GO_LOGIN: {
//                Log.d(TAG, "# 네이버 로그인으로 이동");
//                webViewLoad(msg, "https://nid.naver.com/nidlogin.login?svctype=262144");
//                break;
//            }
//
//            case TOUCH_ID_AREA: {
//                Log.d(TAG, "# 검색창 터치");
//                if (_startShopHome) {
//                    if (_shopPageAction.touchSearchBar()) {
//                        _waitCount = 0;
//                        _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
//                    } else {
//                        Log.d(TAG, "# 검색창 터치에 실패해서 패턴종료.");
//                        _workCode = 110002;
//                        _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
//                    }
//                } else {
//                    if (_homeAction.touchSearchBar(true)) {
//                        _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
//                    } else {
//                        Log.d(TAG, "# 검색창 터치에 실패해서 패턴종료.");
//                        _workCode = 110001;
//                        _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
//                    }
//                }
//                break;
//            }
//
//            case INPUT_KEYWORD: {
//                Log.d(TAG, "# 검색창 검사");
//                if (_startShopHome) {
//                    if (!_shopPageAction.searchBarShown()) {
//                        if (_findBarCount > 3) {
//                            Log.d(TAG, "# 로딩에러로 처리 중단.");
//                            _workCode = 110012;
//                            _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
//                        } else {
//                            if (_waitCount < 5) {
//                                Log.d(TAG, "# 검색창이 떠있지 않아서 3초 후 다시 시도..." + _waitCount);
//                                ++_waitCount;
//                                _handler.sendEmptyMessageDelayed(msg.what, 3000);
//                            } else {
//                                Log.d(TAG, "# 검색창이 떠있지 않아서 새로고침");
//                                ++_findBarCount;
//                                _webViewManager.reload();
//                                webViewLoading(msg);
////                            Log.d(TAG, "# 검색창이 떠있지 않아서 다시 터치");
////                            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, 5000);
//                            }
//                        }
//                    } else {
//                        Log.d(TAG, "# 검색어 입력: " + _keyword);
//                        _action.extractStrings(_keyword);
//                        _action.inputKeyword();
//                        _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1000, 3000));
//                    }
//                } else {
//                    _searchBarCheckPatternAction.checkSearchBarShown();
//                    if (!_searchBarCheckPatternAction.isFocus()) {
//                        if (_findBarCount > 3) {
//                            Log.d(TAG, "# 로딩에러로 처리 중단.");
//                            _workCode = 110011;
//                            _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
//                        } else {
//                            Log.d(TAG, "# 검색창이 떠있지 않아서 다시 터치");
//                            ++_findBarCount;
//                            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, 5000);
//                        }
//                    } else {
//                        Log.d(TAG, "# 검색어 입력: " + _keyword);
//                        _action.extractStrings(_keyword);
//                        _action.inputKeyword();
//                        _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1000, 3000));
//                    }
//                }
//                break;
//            }
//
//            case TOUCH_SEARCH_BUTTON: {
//                Log.d(TAG, "# 검색버튼 터치");
//                _step = 0;
//                _scrollCount1 = 0;
//
//                if (_startShopHome) {
//                    _currentPage = 2;   // 실제 페이지가 아니라 단순 구분용이므로. 메인이 아닌것으로 설정.
////                    _shopSearchBarAction.submitSearchButton();
//                    _action.touchSearchButton();
//                } else {
//                    _currentPage = 1;
//                    _searchBarAction.submitSearchButton();
////                    _action.touchSearchButton();
//                }
//                webViewLoading(msg);
//                break;
//            }
//
//            case END_PATTERN: {
//                Log.d(TAG, "# NaverPatternMessage 패턴 종료");
//                _searchBarCheckPatternAction.endPattern();
//                _searchBarAction.endPattern();
//                break;
//            }
//        }
//    }
//
//    @Override
//    public void onPageLoaded(String url) {
//        super.onPageLoaded(url);
//
//        switch (_lastMessage) {
//            case GO_LOGIN: {
//                Log.d(TAG, "# 네이버 로그인으로 이동 후 동작");
//                _findBarCount = 0;
//                _handler.sendEmptyMessageDelayed(TOUCH_CONTENT, MathHelper.randomRange(5000, 6000));
//                break;
//            }
//        }
//
//
//    }
//}
