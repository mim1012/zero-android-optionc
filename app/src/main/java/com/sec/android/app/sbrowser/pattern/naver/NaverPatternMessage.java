package com.sec.android.app.sbrowser.pattern.naver;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

import com.sec.android.app.sbrowser.engine.Config;
import com.sec.android.app.sbrowser.engine.CookieFileManager;
import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpCookieController;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpEngine;
import com.sec.android.app.sbrowser.engine.WebEngine.WebPageData;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;
import com.sec.android.app.sbrowser.models.AccountItem;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.models.NnbData;
import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.PatternMessage;
import com.sec.android.app.sbrowser.pattern.action.NaverCookieOtherAction;
import com.sec.android.app.sbrowser.pattern.action.NaverCookieStatusAction;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.naver.packet.NaverPacketCommon;

public class NaverPatternMessage extends PatternMessage {

    private static final String TAG = NaverPatternMessage.class.getSimpleName();

    public static final String HOME_URL = "https://naver.com";

    protected static final int DEFAULT_RANDOM_RANGE = 6;
//    protected static final int DEFAULT_RANDOM_RANGE = 6;

    protected static final int GO_HOME = 1011;
    protected static final int GO_SEARCH_HOME_EMPTY = 1012;
    protected static final int GO_SEARCH_HOME_DIRECT = 1013;
    protected static final int WEB_BACK = 1014;

    protected static final int INPUT_KEYWORD = 1021;
    protected static final int REGISTER_FINISH = 1022;
    protected static final int REGISTER_RANK = 1023;

    protected static final int TOUCH_LOGO = 1031;
    protected static final int TOUCH_SEARCH_BAR = 1032;
    protected static final int TOUCH_SEARCH_BUTTON = 1033;
    protected static final int TOUCH_RANDOM_CONTENT = 1034;
    protected static final int TOUCH_NEW_POPUP_OK = 1035;
    protected static final int TOUCH_NEW_POPUP2_OK = 1036;
    protected static final int CLEAR_SEARCH_BAR = 1037;

    protected static final int STAY_RANDOM = 1041;

    // 로그인 패턴 삽입.
    protected static final int GO_LOGIN = 2001;
    protected static final int TOUCH_ID_INPUT = 2002;
    protected static final int INPUT_ID = 2003;
    protected static final int TOUCH_PW_INPUT = 2004;
    protected static final int INPUT_PW = 2005;
    protected static final int TOUCH_LOGIN_BUTTON = 2006;
    protected static final int CHECK_LOGIN = 2007;
    protected static final int TOUCH_PHONE_RADIO_BUTTON = 2008;
    protected static final int TOUCH_PHONE_OK_BUTTON = 2009;
    protected static final int GET_ACCOUNT = 2010;
    protected static final int RUN_NEXT = 2011;

    private HttpCookieController _cookieController;
    private HttpEngine _httpEngine;

    protected final NaverCookieStatusAction _cookieStatusAction;
    protected final NaverCookieOtherAction _cookieOtherAction;

    protected final NaverSearchBarAction _searchBarAction;
    protected final NaverSearchBarCheckPatternAction _searchBarCheckPatternAction;
    protected final NaverSearchBarClearPatternAction _searchBarClearPatternAction;
    protected final NaverLoginPageAction _loginPageAction;
    protected final NaverAccountAction _accountAction;

    protected final NaverPatternAction _action;
    protected final SwipeThreadAction _swipeAction;

    protected KeywordItemMoon _item = null;
    protected AccountItem _accountItem = null;
    protected boolean _isLoginCookieExpired = false;
    protected int _randomRange = DEFAULT_RANDOM_RANGE;

    private int _checkErrorCount = 0;
    private int _checkCaptchaCount = 0;

    public boolean needIpChange = false;

    public NaverPatternMessage(WebViewManager manager) {
        this(manager, true);
    }

    public NaverPatternMessage(WebViewManager manager, boolean hasActions) {
        super(manager);

        // 공통 액션.
        _cookieStatusAction = new NaverCookieStatusAction(manager.getWebView().getContext());
        _cookieOtherAction = new NaverCookieOtherAction(manager.getWebView().getContext());

        if (hasActions) {
            _action = new NaverPatternAction(manager.getWebView());
            _searchBarAction = new NaverSearchBarAction(manager.getWebView(), null);
            _searchBarCheckPatternAction = new NaverSearchBarCheckPatternAction(manager.getWebView());
            _searchBarClearPatternAction = new NaverSearchBarClearPatternAction(manager.getWebView());
            _loginPageAction = new NaverLoginPageAction(manager.getWebView());
            _accountAction = new NaverAccountAction();

            TouchInjector injector = new TouchInjector(manager.getWebView().getContext());
            injector.setSoftKeyboard(new SamsungKeyboard());

            _swipeAction = new SwipeThreadAction(injector);
        } else {
            _action = null;
            _searchBarAction = null;
            _searchBarCheckPatternAction = null;
            _searchBarClearPatternAction = null;
            _loginPageAction = null;
            _accountAction = null;
            _swipeAction = null;
        }
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                _checkCaptchaCount = 0;
                break;
            }

//            case GO_HOME: {
//                Log.d(TAG, "# 네이버 홈으로 이동");
//                webViewLoad(msg, Config.HOME_URL);
//                break;
//            }

            case GO_LOGIN: {
                Log.d(TAG, "# 네이버 로그인으로 이동");

                if (_accountItem == null) {
                    Log.d(TAG, "# 아이디 정보가 없어서 패턴종료.");
                    _workCode = 100010;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                } else {
                    _accountAction.item = _item;
                    _accountAction.loginId = UserManager.getInstance().getLoginId(_webViewManager.getWebView().getContext());
//                    _webViewManager.loadUrl("http://google.com");
                    webViewLoad(msg, Config.NAVER_LOGIN_URL);
                }
                break;
            }

            case TOUCH_ID_INPUT: {
                Log.d(TAG, "# 아이디 입력창 터치");
                if (_loginPageAction.touchIdInput()) {
                    _handler.sendEmptyMessageDelayed(INPUT_ID, MathHelper.randomRange(1500, 2500));
                } else {
                    Log.d(TAG, "# 아이디 입력창 터치에 실패해서 패턴종료.");
                    _workCode = 100011;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                }
                break;
            }

            case INPUT_ID: {
                Log.d(TAG, "# 아이디 입력: " + _accountItem.id);
//                _action.inputText(_accountItem.id);
                _action.extractStrings(_accountItem.id);
                _action.inputKeyword();

                Log.d(TAG, "# 키보드 다음 버튼 터치");
                _action.touchSearchButton();
                _handler.sendEmptyMessageDelayed(INPUT_PW, MathHelper.randomRange(1500, 2500));
                break;
            }

            // 미사용.
            case TOUCH_PW_INPUT: {
                Log.d(TAG, "# 비밀번호 입력창 터치");
                if (_loginPageAction.touchPwInput()) {
                    _handler.sendEmptyMessageDelayed(INPUT_PW, MathHelper.randomRange(1500, 2500));
                } else {
                    Log.d(TAG, "# 비밀번호 입력창 터치에 실패해서 패턴종료.");
                    _workCode = 100012;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                }
                break;
            }

            case INPUT_PW: {
                Log.d(TAG, "# 비밀번호 입력: " + _accountItem.password);
//                _action.inputText(_accountItem.password);
                _action.extractStrings(_accountItem.password);
                _action.inputKeyword();

                _handler.sendEmptyMessageDelayed(TOUCH_LOGIN_BUTTON, MathHelper.randomRange(500, 1500));
                break;
            }

            case TOUCH_LOGIN_BUTTON: {
//                Log.d(TAG, "# 로그인 버튼 터치");
                // 로그인을 했다면 아이피는 무조건 한번 바꿔준다.
                needIpChange = true;

                Log.d(TAG, "# 로그인 버튼(키보드 다음 버튼) 터치");
                _checkErrorCount = 0;
                _action.touchSearchButton();
                webViewLoading(msg);
                break;
            }

            case CHECK_LOGIN: {
                Log.d(TAG, "# 로그인 검사");
                if (_loginPageAction.checkSuccess()) {
                    Log.d(TAG, "# 로그인 성공");
                    _accountAction.updateStatus(NaverAccountAction.STATUS_NORMAL);
                    // 로그인 성공시 작업코드 남겨둠.
                    _workCode = 100020;
                    _handler.sendEmptyMessageDelayed(RUN_NEXT, MathHelper.randomRange(2000, 3000));
                } else {
                    int error = _loginPageAction.getError();

                    if (error != NaverLoginPageAction.ERROR_NONE) {
                        if (error == NaverAccountAction.STATUS_ERROR_CAPTCHA) {
                            if (_checkCaptchaCount >= 4) {
                                Log.d(TAG, "# 캡챠 화면 실패로 패턴종료.");
                                // 캡챠는 아이디 에러보다는 장비에러가 많으므로 아이디 상태값을 바꾸지 안는다.
//                                _accountAction.updateStatus(error);
                                _workCode = 100021;
                                sendMessageDelayed(END_PATTERN, 100);
                            } else {
                                Log.d(TAG, "# 캡챠 화면 로그인으로 다시 이동.");
                                ++_checkCaptchaCount;
                                _handler.sendEmptyMessageDelayed(GO_LOGIN, MathHelper.randomRange(1500, 2500));
                            }
                        } else if (error == NaverAccountAction.STATUS_ERROR_PHONE_PICK) {
                            Log.d(TAG, "# 폰 선택화면 처리.");
                            _accountAction.updateStatus(error);
                            _handler.sendEmptyMessageDelayed(TOUCH_PHONE_RADIO_BUTTON, MathHelper.randomRange(1000, 2000));
                        } else {
                            Log.d(TAG, "# 로그인 실패로 패턴종료.");
//                            Log.d(TAG, "# 로그인 실패로 계정 다시 요청.");
                            _accountAction.updateStatus(error);
                            _workCode = 100022;
//                            _handler.sendEmptyMessageDelayed(GET_ACCOUNT, 100);
//                            needIpChange = true;
                            sendMessageDelayed(END_PATTERN, 100);
                        }
                    } else {
                        if (_checkErrorCount >= 4) {
                            Log.d(TAG, "# 로그인 실패(알수없는 에러)로 패턴종료.");
//                            Log.d(TAG, "# 로그인 실패(알수없는 에러)로 계정 다시 요청.");
//                            _accountAction.updateStatus(NaverAccountAction.STATUS_ERROR_UNKNOWN);
                            _workCode = 100023;
//                            _handler.sendEmptyMessageDelayed(GET_ACCOUNT, 100);
//                            needIpChange = true;
                            sendMessageDelayed(END_PATTERN, 100);
                        } else {
                            Log.d(TAG, "# 로그인 실패로 로그인으로 다시 이동.");
//                            Log.d(TAG, "# 로그인 실패로 다시 검사");
                            ++_checkErrorCount;
//                            _workCode = 100024;
                            _handler.sendEmptyMessageDelayed(GO_LOGIN, MathHelper.randomRange(1500, 2500));
//                            _handler.sendEmptyMessageDelayed(CHECK_LOGIN, 5000);
                        }
                    }
                }
                break;
            }

            case TOUCH_PHONE_RADIO_BUTTON: {
                Log.d(TAG, "# 폰 라디오 버튼 검사");
                InsideData insideData = _loginPageAction.getPhoneNumberRadioButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 폰 라디오 버튼 검사 터치");
                        if (_loginPageAction.touchButton(NaverLoginPageAction.BUTTON_PHONE_RADIO)) {
                            _handler.sendEmptyMessageDelayed(TOUCH_PHONE_OK_BUTTON, MathHelper.randomRange(1000, 2000));
                        } else {
                            Log.d(TAG, "# 폰 라디오 버튼 터치 실패로 패턴종료.");
                            _workCode = 100030;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 폰 라디오 버튼 못찾아서 패턴종료.");
                    _workCode = 100031;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case TOUCH_PHONE_OK_BUTTON: {
                Log.d(TAG, "# 폰 확인 버튼 검사");
                InsideData insideData = _loginPageAction.getPhoneNumberRadioButtonInsideData();
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 폰 확인 버튼 검사 터치");
                        if (_loginPageAction.touchButton(NaverLoginPageAction.BUTTON_PHONE_OK)) {
                            webViewLoading(msg);
                        } else {
                            Log.d(TAG, "# 폰 확인 버튼 터치 실패로 패턴종료.");
                            _workCode = 100032;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 폰 확인 버튼 못찾아서 패턴종료.");
                    _workCode = 100033;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case GET_ACCOUNT: {
                Log.d(TAG, "# 계정 가져오기");
                if (_accountAction.requestGetAccount() == 1) {
                    _accountItem = _accountAction.getAccount();

                    if (_accountItem == null) {
                        Log.d(TAG, "# 계정이 없어서 패턴종료.");
                        _workCode = 100040;
                        sendMessageDelayed(END_PATTERN, 100);
                    } else {
                        // 계정 정보도 변경. -> 외부에서 사용하는게 없으므로 굳이 필요없기도 함.
                        _item.item.account = _accountItem;
//                        needIpChange = true;
                        _workCode = 100041;
                        sendMessageDelayed(END_PATTERN, 100);
                    }
                } else {
                    Log.d(TAG, "# 계정 가져오기 실패로 패턴 종료...");
                    _workCode = 100042;
                    sendMessageDelayed(END_PATTERN, 100);
                }
                break;
            }

            case END_PATTERN: {
                Log.d(TAG, "# NaverPatternMessage 패턴 종료");
                if (_searchBarCheckPatternAction != null) {
                    _searchBarCheckPatternAction.endPattern();
                }
                if (_searchBarAction != null) {
                    _searchBarAction.endPattern();
                }
                break;
            }
        }
    }

    @Override
    public void onPageLoaded(String url) {
        super.onPageLoaded(url);
        boolean worked = false;

        switch (_lastMessage) {
            case GO_LOGIN: {
                worked = true;
                Log.d(TAG, "# 로그인 이동 후 동작");
                _handler.sendEmptyMessageDelayed(TOUCH_ID_INPUT, MathHelper.randomRange(3000, 4000));
                break;
            }

            case TOUCH_LOGIN_BUTTON: {
                worked = true;
//                Log.d(TAG, "# 로그인 버튼 터치 후 동작");
                Log.d(TAG, "# 로그인 버튼(키보드 다음 버튼) 터치 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_LOGIN, MathHelper.randomRange(3000, 4000));
                break;
            }

            case TOUCH_PHONE_OK_BUTTON: {
                worked = true;
                Log.d(TAG, "# 폰 확인 버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_LOGIN, MathHelper.randomRange(3000, 4000));
                break;
            }
        }

        if (worked) {
            _lastMessage = -1;
        }
    }

    @Override
    public void onPageLoadFailed(String url) {
        if (_lastMessage == TOUCH_LOGIN_BUTTON) {
            Log.d(TAG, "onPageLoadFailed #TOUCH_LOGIN_BUTTON: " + url);
            reloading();
        } else {
            super.onPageLoadFailed(url);
        }
    }

    public void uploadLoginCookieStatusInWebView() {
        NnbData nnbData = UserManager.getInstance().nnbData;

        if (nnbData != null && !TextUtils.isEmpty(nnbData.nidSes)) {
            CookieManager cookieManager = CookieManager.getInstance();
            String cookies = cookieManager.getCookie(".naver.com");

            if (!cookies.contains("NID_AUT") && !cookies.contains("NID_SES")) {
                Log.d(TAG, "# 로그인 쿠키가 유요하지 않아 상태 업로드");
                _cookieStatusAction.registerNaverCookieStatus(nnbData.loginCookieId, 2);
                _isLoginCookieExpired = true;
            }
        }
    }

    public void uploadOtherCookieInWebView() {
        NnbData nnbData = UserManager.getInstance().nnbData;

        // 쿠키 정보 있으면 업로드.
        if (nnbData != null && nnbData.naverCookieId > 0) {
            CookieFileManager manager = new CookieFileManager();
            String others = manager.getAllCookieString(_webViewManager.getWebView().getContext(), ".naver.com");

            if (!TextUtils.isEmpty(others)) {
                _cookieOtherAction.registerNaverCookieOthers(nnbData.naverCookieId, others);
            }
        }
    }

    public HttpEngine getHttpEngine() {
        if (_httpEngine == null) {
            _cookieController = new HttpCookieController();
            _httpEngine = new HttpEngine(_webViewManager.getWebView().getContext());
            _httpEngine.setCookieController(_cookieController);
            _httpEngine.setUa(UserManager.getInstance().ua);
            _httpEngine.setChromeVersion(UserManager.getInstance().chromeVersion);
            _httpEngine.setBrowserVersion(UserManager.getInstance().browserVersion);
            _httpEngine.setNnb(UserManager.getInstance().nnb);
        }

        return _httpEngine;
    }

    public void setPacketCookie(String url) {
        getHttpEngine();
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(url);
        String[] cookieArray = cookies.split(";");

        for (String cookie : cookieArray) {
            _cookieController.setCookie(url, cookie);
        }
    }

    public void createPageData(BasePatternAction action) {
        WebPageData pageData = new WebPageData();
        pageData.htmlString = action.getHtml();

//        _pageData

        _pageData = pageData;
    }


    public boolean sendPacketLcs(BasePatternAction action) {
        if (_pageData == null) {
            createPageData(action);
        }

        HttpEngine httpEngine = getHttpEngine();
        String currentUrl = _webViewManager.getCurrentUrl();
//        String currentUrl = _action.getSystemValue("window.location.href");
        String referrer = action.getSystemValue("document.referrer");

        if (TextUtils.isEmpty(referrer)) {
            referrer = _httpEngine.getReferer();

            if (TextUtils.isEmpty(referrer)) {
                referrer = "https://m.naver.com/";
            }
        }

        _pageData.docReferer = referrer;

        setPacketCookie(currentUrl);

        NaverPacketCommon packetCommon = new NaverPacketCommon(httpEngine, _pageData);
        packetCommon.setPatternAction(action);

        String url = packetCommon.getLcsUrl(currentUrl, "m_smartstore_products");
        _httpEngine.setUrl(url);
        _httpEngine.setNnb(UserManager.getInstance().nnb);
        _httpEngine.setCurrentUrl(currentUrl);
        _httpEngine.setReferer(currentUrl);
        Log.d(TAG, "### lcsGet Url: " + url);
        String resultString = httpEngine.requestNaverMobileContentFromLcs(url);

        if (resultString == null) {
            Log.d(TAG, "# 통신 오류로 패턴종료.");
            return false;
        }

        _pageData = null;

        return true;
    }
}
