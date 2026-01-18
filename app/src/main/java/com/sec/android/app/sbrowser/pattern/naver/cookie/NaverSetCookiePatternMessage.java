package com.sec.android.app.sbrowser.pattern.naver.cookie;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebStorage;

import com.sec.android.app.sbrowser.engine.Config;
import com.sec.android.app.sbrowser.engine.CookieFileManager;
import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.models.NnbData;
import com.sec.android.app.sbrowser.pattern.action.NnbAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;

import java.net.HttpCookie;
import java.util.List;

public class NaverSetCookiePatternMessage extends NaverPatternMessage {

    private static final String TAG = NaverSetCookiePatternMessage.class.getSimpleName();
    private static final String NAVER_URL = ".naver.com";
    private static final String KEY_NNB = "NNB";
    private static final String KEY_NID_INF = "nid_inf";
    private static final String KEY_NID_AUT = "NID_AUT";
    private static final String KEY_NID_JKL = "NID_JKL";
    private static final String KEY_NID_SES = "NID_SES";

    public static final int CHECK_COOKIE_VALUE = 5100;
    public static final int UPLOAD_COOKIE = 5101;
    public static final int GET_COOKIE = 5102;
    public static final int CHANGE_COOKIE_VALUE = 5103;

    private int _retryCount = 0;
    private String _nnb = null;
    private NnbData _nnbData = null;
    private String _getNnb = null;
    private String _ua = null;

    private boolean _changed = false;

    public boolean isPc = false;
    public boolean deleteLocalStorage = true;
    public KeywordItemMoon item = null;
    public long _checkDelayTime = 2000;

    public NaverSetCookiePatternMessage(WebViewManager manager) {
        super(manager);
        _ua = _webViewManager.getUserAgentString();
    }

    public boolean isChanged() {
        return _changed;
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 쿠키 설정 패턴 시작");
//                sendMessageDelayed(TOUCH_URL_BAR, 5000);
                _webViewManager.setLoadsImagesAutomatically(item.item.cookieUseImage == 1);
                _handler.sendEmptyMessage(GO_HOME);
                break;
            }

            case GO_HOME: {
                if (item.item.cookieHomeMode == 1) {
                    if (item.category.contains("nshop")) {
                        if (item.item.shopHome == 1 || item.item.shopHome == 3) {
                            Log.d(TAG, "# 네이버 쇼핑 홈으로 이동");
                            _checkDelayTime = 6000;
                            webViewLoad(msg, Config.NAVER_SHOP_HOME_URL);
                        } else if (item.item.shopHome == 2) {
                            Log.d(TAG, "# 네이버 빈검색결과로 이동");
                            // 빈검색결과는 반드시 이미지 로드해야함.
                            _webViewManager.setLoadsImagesAutomatically(true);
                            webViewLoad(msg, Config.NAVER_EMPTY_SEARCH_URL);
                        } else {
                            Log.d(TAG, "# 네이버 홈으로 이동");
                            webViewLoad(msg, Config.NAVER_HOME_URL);
                        }
                    } else if (item.category.equals("nshop_pc")) {
                        Log.d(TAG, "# 네이버 쇼핑 PC 홈으로 이동");
                        _checkDelayTime = 6000;
                        webViewLoad(msg, Config.NAVER_SHOP_HOME_URL);
                    } else {
                        Log.d(TAG, "# 네이버 로그인으로 이동");
                        // 네이버 로그인은 반드시 이미지 로드해야함.
                        _webViewManager.setLoadsImagesAutomatically(true);
                        webViewLoad(msg, Config.NAVER_LOGIN_URL);
//                        webViewLoad(msg, "https://weather.naver.com/");
//                        webViewLoad(msg, Config.NAVER_HOME_URL);
                    }
                } else {
                    Log.d(TAG, "# 네이버 로그인으로 이동");
                    // 네이버 로그인은 반드시 이미지 로드해야함.
                    _webViewManager.setLoadsImagesAutomatically(true);
                    webViewLoad(msg, Config.NAVER_LOGIN_URL);
//                    webViewLoad(msg, "https://weather.naver.com/");
//                    webViewLoad(msg, Config.NAVER_HOME_URL);
                }
                break;
            }

            case CHECK_COOKIE_VALUE: {
                _webViewManager.getWebView().post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "# 쿠키 flush.");
                        CookieManager manager2 = CookieManager.getInstance();
                        manager2.flush();
                    }
                });

//                _nnb = _webViewManager.getCookie(NAVER_URL, KEY_NNB);
                CookieFileManager manager = new CookieFileManager();
                _nnb = manager.getCookieValue(_webViewManager.getWebView().getContext(), KEY_NNB);

                if (_nnb == null) {
                    Log.d(TAG, "# 쿠키 값이 생성되지 않아서 네이버 홈 다시 시도.");
                    sendMessageDelayed(GO_HOME, (item.item.lowDelay > 0) ? 50 : 1000);
                } else {
                    UserManager.getInstance().nnb = _nnb;
                    Log.d(TAG, "# 쿠키 값이 생성되어 브라우저 종료 후 쿠기 변경: " + _nnb);
                    _webViewManager.stopLoading();
//                    killSbrowser();
                    sendMessageDelayed(UPLOAD_COOKIE, (item.item.lowDelay > 0) ? 50 : 5000);
                }
                break;
            }

            case UPLOAD_COOKIE: {
                Log.d(TAG, "# 쿠기 업로드.");
                NnbAction action = new NnbAction();
                action.loginId = UserManager.getInstance().getLoginId(_webViewManager.getWebView().getContext());
                action.registerNnb(_nnb, _ua, false);

                if (item.item.isPacketBoostPattern()) {
                    _changed = true;
                    sendMessageDelayed(END_PATTERN, (item.item.lowDelay > 0) ? 50 : 500);
                } else {
                    sendMessageDelayed(GET_COOKIE, 100);
                }
                break;
            }

            case GET_COOKIE: {
                Log.d(TAG, "# 쿠기 가져오기.");
                NnbAction action = new NnbAction();
                action.loginId = UserManager.getInstance().getLoginId(_webViewManager.getWebView().getContext());
                int result;

                if (isPc) {
                    result = action.requestNnbPc();
                } else {
                    result = action.requestNnb();
                }

                if (result == 1) {
                    _nnbData = action.getNnbData();
                    _getNnb = action.getNnb();
                    String ua = action.getUserAgent();

                    if (UserManager.getInstance().nnbData == null) {
                        UserManager.getInstance().nnbData = _nnbData;
                    } else {
                        UserManager.getInstance().nnbData.loginCookieId = _nnbData.loginCookieId;
                        UserManager.getInstance().nnbData.naverCookieId = _nnbData.naverCookieId;
                        UserManager.getInstance().nnbData.nnb = _getNnb;
                        UserManager.getInstance().nnbData.nidInf = _nnbData.nidInf;
                        UserManager.getInstance().nnbData.nidAut = _nnbData.nidAut;
                        UserManager.getInstance().nnbData.nidJkl = _nnbData.nidJkl;
                        UserManager.getInstance().nnbData.nidSes = _nnbData.nidSes;
                        UserManager.getInstance().nnbData.naverCookieOther = _nnbData.naverCookieOther;
                    }

                    UserManager.getInstance().nnb = _getNnb;

                    if ((ua != null) && (ua.length() > 0)) {
                        Log.d(TAG, "# UA 변경 시도: " + _webViewManager.getUserAgentString());
                        _webViewManager.setUserAgentString(ua);
                        UserManager.getInstance().ua = ua;
                        UserManager.getInstance().chromeVersion = action.getChromeVersion();
                        UserManager.getInstance().browserVersion = action.getBrowserVersion();
                        Log.d(TAG, "# UA 변경 완료: " + _webViewManager.getUserAgentString());
                    }

                    if (_getNnb == null) {
                        // 내려받은 쿠키가 없다면 바로 사용한다.
                        sendMessageDelayed(END_PATTERN, (item.item.lowDelay > 0) ? 50 : 500);
                    } else {
                        // 내려받은 쿠키가 있다면 변경한다.
                        sendMessageDelayed(CHANGE_COOKIE_VALUE, (item.item.lowDelay > 0) ? 50 : 500);
                    }
                } else {
                    Log.d(TAG, "# 쿠키 가져오기 실패로 패턴 종료...");
                    sendMessageDelayed(END_PATTERN, (item.item.lowDelay > 0) ? 50 : 500);
                }
                break;
            }

            case CHANGE_COOKIE_VALUE: {
                Log.d(TAG, "# 쿠기 변경 시도.");

                // 에러 방지.
                if (_getNnb != null) {
//                    _webViewManager.setCookie(NAVER_URL, KEY_NNB, _getNnb, true);
                    _webViewManager.setCookieString("https://naver.com", KEY_NNB + "=" + _getNnb + "; expires=Sat, 01 Jan 2050 09:00:00 GMT; path=/; domain=.naver.com; SameSite=None; Secure");
                    Log.d(TAG, "changed: " +  _nnb + " -> " + _webViewManager.getCookie("https://naver.com", KEY_NNB));

                    if (_nnbData != null && !TextUtils.isEmpty(_nnbData.nidSes)) {
                        _webViewManager.setCookieString(NAVER_URL, "NID_AUT=" + _nnbData.nidAut + "; path=/; domain=.naver.com; HttpOnly");
                        _webViewManager.setCookieString("https://naver.com", "NID_JKL=" + _nnbData.nidJkl + "; path=/; domain=.naver.com; Secure;");
                        _webViewManager.setCookieString(NAVER_URL, "NID_SES=" + _nnbData.nidSes + "; path=/; domain=.naver.com;");
                        _webViewManager.setCookieString(NAVER_URL, "nid_inf=" + _nnbData.nidInf + "; path=/; domain=.naver.com;");

//                        _webViewManager.setCookie(NAVER_URL, KEY_NID_INF, _nnbData.nidInf, false);
//                        _webViewManager.setCookie(NAVER_URL, KEY_NID_AUT, _nnbData.nidAut, false);
//                        _webViewManager.setCookie(NAVER_URL, KEY_NID_JKL, _nnbData.nidJkl, true);
//                        _webViewManager.setCookie(NAVER_URL, KEY_NID_SES, _nnbData.nidSes, false);

                        Log.d(TAG, "changed: " +  KEY_NID_INF + " -> " + _webViewManager.getCookie("https://naver.com", KEY_NID_INF));
                        Log.d(TAG, "changed: " +  KEY_NID_AUT + " -> " + _webViewManager.getCookie("https://naver.com", KEY_NID_AUT));
                        Log.d(TAG, "changed: " +  KEY_NID_JKL + " -> " + _webViewManager.getCookie("https://naver.com", KEY_NID_JKL));
                        Log.d(TAG, "changed: " +  KEY_NID_SES + " -> " + _webViewManager.getCookie("https://naver.com", KEY_NID_SES));

                        if (!TextUtils.isEmpty(_nnbData.naverCookieOther)) {
                            String[] cookieStrings = _nnbData.naverCookieOther.split("\n");

                            for (String cookieString : cookieStrings) {
                                try {
                                    List<HttpCookie> cookieList = HttpCookie.parse(cookieString);
                                    Log.d(TAG, "@@ Cookie (size: " + cookieList.size() + "): " + cookieString);

                                    for (HttpCookie cookie : cookieList) {
                                        String domain = cookie.getDomain();

                                        if (!TextUtils.isEmpty(domain) && !cookie.hasExpired()) {
                                            String[] parts = cookieString.split(";");
                                            StringBuilder partJoiner = new StringBuilder();

                                            for (int i = 0; i < parts.length; ++i) {
                                                String part = parts[i].trim();

                                                if (i == 0 && part.indexOf(' ') > -1) {
                                                    // 쿠키 값에 공백이 있다면 그뒤에 잘라냄.
                                                    Log.d(TAG, "@@ > Fix value.");
                                                    String[] kvs = part.split(" ");
                                                    part = kvs[0];
                                                } else if (part.toLowerCase().startsWith("expires=") && part.contains(" path=")) {
                                                    Log.d(TAG, "@@ > Fix expires.");
                                                    String[] expires = part.split(" path=");
                                                    part = expires[0];
                                                } else if (part.toLowerCase().startsWith("domain=") && !domain.startsWith(".")) {
                                                    Log.d(TAG, "@@ > Remove domain!");
                                                    continue;
                                                }

                                                if (i > 0) {
                                                    partJoiner.append("; ");
                                                }

                                                partJoiner.append(part);
                                            }

                                            if (cookie.getSecure()) {
                                                if (domain.startsWith(".")) {
                                                    domain = domain.substring(1);
                                                }

                                                domain = "https://" + domain;
                                            }

                                            cookieString = partJoiner.toString();
                                            _webViewManager.setCookieString(domain, cookieString);
                                            Log.d(TAG, "@@ = Set cookie(" + domain + "): " + cookieString);
                                        }
                                    }
                                } catch (Exception e) {
//                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    if (deleteLocalStorage) {
                        Log.d(TAG, "# 쿠키 변경 성공. 로컬스토리지 삭제");
                        // 로컬스토리지 삭제.
                        WebStorage.getInstance().deleteAllData();
                    } else {
                        Log.d(TAG, "# 쿠키 변경 성공");
                    }

                    _changed = true;
                } else {
                    Log.d(TAG, "# 쿠키 값 없어서 패턴 종료...");
                }
                sendMessageDelayed(END_PATTERN, (item.item.lowDelay > 0) ? 50 : 3000);
                break;
            }

            case END_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# 쿠키 설정 패턴 종료");
                _webViewManager.goBlankPage();
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
                Log.d(TAG, "# 홈 이동 후 동작");
                // 홈에서 nnb가 생성이 안되므로 메일 화면으로 검사.
//                sendMessageDelayed(TOUCH_MAIL_BUTTON, 2000);
//                sendMessageDelayed(CHECK_COOKIE_VALUE, MathHelper.randomRange(2000, 3000));
                sendMessageDelayed(CHECK_COOKIE_VALUE, _checkDelayTime);
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

    protected void sendMessageDelayed(int what, long delayMillis) {
        _retryCount = 0;
        _handler.sendEmptyMessageDelayed(what, delayMillis);
    }

    protected boolean resendMessageDelayed(int what, long delayMillis, int maxRetryCount) {
        if (_retryCount < maxRetryCount) {
            ++_retryCount;
            _handler.sendEmptyMessageDelayed(what, delayMillis);
            return true;
        }

        return false;
    }
}
