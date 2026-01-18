package com.sec.android.app.sbrowser.pattern.naver;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.R;
import com.sec.android.app.sbrowser.engine.ImageFinder;
import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.NaverAccount;
import com.sec.android.app.sbrowser.engine.NaverAccountManager;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;
import com.sec.android.app.sbrowser.pattern.action.NaverLoginCookieAction;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;

import java.util.List;

public class NaverLoginChromePatternMessage extends NaverPatternMessage {

    private static final String TAG = NaverLoginChromePatternMessage.class.getSimpleName();

    private static final int MAX_SCROLL_COUNT = 10;

    public static final int CHECK_LOGIN = 7000;
    public static final int CHECK_LOGIN_BUTTON = CHECK_LOGIN + 1;
    public static final int CHECK_LOGOUT_BUTTON = CHECK_LOGIN + 2;

    public static final int GET_AUTH_ACCOUNT = 7010;
    public static final int CHECK_ID_FIELD = GET_AUTH_ACCOUNT + 1;
    public static final int TOUCH_NEXT_KEYBOARD = GET_AUTH_ACCOUNT + 2;
    public static final int CHECK_PASSWORD_FIELD = GET_AUTH_ACCOUNT + 3;
    public static final int INPUT_PASSWORD = GET_AUTH_ACCOUNT + 4;

    public static final int CHECK_PASSWORD_SAVE = 7020;
    public static final int CHECK_PASSWORD_IGNORE = CHECK_PASSWORD_SAVE + 1;

    public static final int CHECK_LOGIN_RESULT_LOADING = CHECK_PASSWORD_SAVE + 2;
    public static final int CHECK_LOGIN_RESULT = CHECK_LOGIN_RESULT_LOADING + 1;
    public static final int CHECK_LOGIN_FAILED = CHECK_LOGIN_RESULT_LOADING + 2;
    public static final int CHECK_LOGIN_CLEAR = CHECK_LOGIN_RESULT_LOADING + 3;


    protected final NaverLoginCookieAction _loginCookieAction;
    private final NaverPatternAction _action;
    private final SwipeThreadAction _swipeAction;

    private final Context _context;

    private int _loginScrollCount = 0;

    private int _nextMessage = 0;
    protected int _currentPageResourceId = 0;
    protected int _loginNextMessage = 0;

    public NaverLoginChromePatternMessage(WebViewManager manager) {
        super(manager);
        _context = manager.getWebView().getContext();

        _loginCookieAction = new NaverLoginCookieAction(manager.getWebView().getContext());

        _action = new NaverPatternAction(manager.getWebView());

        TouchInjector injector = new TouchInjector(manager.getWebView().getContext());
        injector.setSoftKeyboard(new SamsungKeyboard());

        _swipeAction = new SwipeThreadAction(injector);
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case CHECK_LOGIN: {
                Log.d(TAG, "# 네이버 로그인 검사");
                NaverAccountManager accountManager = NaverAccountManager.getInstance();
                NaverAccount account = accountManager.getAccountForNidAut(_loginCookieAction.getNidAutChrome());

                if (_loginCookieAction.isLoginChrome()) {
                    Log.d(TAG, "# 이미 로그인 중이라 다음으로");

                    if (account != null) {
                        if (account.loginCookieId <= 0) {
                            String cookiesJson = _loginCookieAction.getCookieJsonStringChrome();
                            // 쿠키 업로드.
                            int result = _accountAction.registerNaverAuthAccountCookie(account.accountId, cookiesJson, "");

                            if (result > 0) {
                                account.loginCookieId = _accountAction.getLoginCookieId();
                                accountManager.saveData(_context);
                            }
                        }
                    } else {
                        // 만약 로그인 중인 계정을 찾을수 없다면, 서버에서 계정정보를 받아서 연결해준다.
                        // 사실 이 상황은 발생하지 않는다. 기존 버전에 연결되어 있지 않기 때문에 초기 호환용으로 만들어 준다.
                    }

                    _handler.sendEmptyMessageDelayed(_loginNextMessage, 100);
                } else {
                    if (account != null) {
                        // aut 정보가 있을경우 해당 쿠키 죽은 것을 업로드 해준다.
                        _cookieStatusAction.registerNaverCookieStatus(account.loginCookieId, 2);
                    } else {

                    }

                    _accountAction.loginId = UserManager.getInstance().getLoginId(_context);

                    _handler.sendEmptyMessageDelayed(CHECK_LOGIN_BUTTON, 1000);
                }
                break;
            }

            case CHECK_LOGIN_BUTTON: {
                Log.d(TAG, "# 네이버 로그인 버튼 검사");
                Rect rc = findResourceFromScreen(R.drawable.t_search_login);
                if (rc != null) {
                    Log.d(TAG, "# 네이버 로그인 버튼 터치: " + rc);
                    _action.touchScreen(rc, 15);
                    sendMessageDelayed(GET_AUTH_ACCOUNT, 100);
                } else {
                    if (_loginScrollCount < MAX_SCROLL_COUNT) {
                        Log.d(TAG, "# 네이버 로그인 버튼 못찾아서 아래로 스크롤..." + _loginScrollCount);
                        ++_loginScrollCount;
//                        _swipeAction.swipeDownFast(110, 200);
                        _swipeAction.swipeDown(false);
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 네이버 로그인 버튼 못찾아서 패턴 종료..." + _loginScrollCount);
                        _workCode = 101001;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    }
                }
                break;
            }

            case GET_AUTH_ACCOUNT: {
                Log.d(TAG, "# 네이버 인증 계정 가져오기");
                if (_accountAction.requestGetAuthAccount() == 1) {
                    _accountItem = _accountAction.getAccount();

                    if (_accountItem == null) {
                        Log.d(TAG, "# 계정이 없어서 패턴종료.");
                        _workCode = 101002;
                        sendMessageDelayed(END_PATTERN, 100);
                    } else {
                        NaverAccount account = new NaverAccount();
                        account.accountId = _accountItem.id;

                        NaverAccountManager accountManager = NaverAccountManager.getInstance();
                        accountManager.addAccount(account);
                        accountManager.saveData(_context);

                        sendMessageDelayed(CHECK_ID_FIELD, MathHelper.randomRange(2000, 2500));
                    }
                } else {
                    Log.d(TAG, "# 계정 가져오기 실패로 패턴 종료...");
                    _workCode = 101003;
                    sendMessageDelayed(END_PATTERN, 100);
                }
                break;
            }

            case CHECK_ID_FIELD: {
                Log.d(TAG, "# 네이버 아이디 필드 검사");
                Rect rc = findResourceFromScreen(R.drawable.t_login_id);
                if (rc != null) {
                    Log.d(TAG, "# 네이버 아이디 필드 터치: " + rc);
                    _action.touchScreen(rc, 15);
                    // 시간이 길어지면 붙여넣기가 안되서 약간 빠르게 처리.
                    SystemClock.sleep(MathHelper.randomRange(1000, 1200));
                    inputText("아이디", _accountItem.id);
//                    sendMessageDelayed(TOUCH_NEXT_KEYBOARD, MathHelper.randomRange(1300, 3000));
                    sendMessageDelayed(CHECK_PASSWORD_FIELD, MathHelper.randomRange(1300, 3000));
                } else {
                    Log.d(TAG, "네이버 아이디 필드 못찾아서 다시검사: " + _retryCount);
                    if (!resendMessageDelayed(msg.what, 2000, 10)) {
                        Log.d(TAG, "네이버 아이디 필드 못찾아서 패턴 종료!!");
                        _workCode = 101010;
                        sendMessageDelayed(END_PATTERN, 3000);
                    }
                }
                break;
            }

            case TOUCH_NEXT_KEYBOARD: {
                Log.d(TAG, "# 키보드 다음 버튼 터치");
                _action.touchSearchButton();
                sendMessageDelayed(INPUT_PASSWORD, MathHelper.randomRange(1000, 2500));
                break;
            }

            case CHECK_PASSWORD_FIELD: {
                Log.d(TAG, "# 네이버 비밀번호 필드 검사");
                Rect rc = findResourceFromScreen(R.drawable.t_login_pw);
                if (rc != null) {
                    Log.d(TAG, "# 네이버 비밀번호 필드 터치: " + rc);
                    _action.touchScreen(rc, 15);
                    // 시간이 길어지면 붙여넣기가 안되서 약간 빠르게 처리.
                    SystemClock.sleep(MathHelper.randomRange(1000, 1200));
                    inputText("비밀번호", _accountItem.password);
                    _action.touchSearchButton();
                    sendMessageDelayed(CHECK_PASSWORD_SAVE, MathHelper.randomRange(3000, 5000));
                } else {
                    Log.d(TAG, "네이버 비밀번호 필드 못찾아서 다시검사: " + _retryCount);
                    if (!resendMessageDelayed(msg.what, 2000, 10)) {
                        Log.d(TAG, "네이버 비밀번호 필드 못찾아서 패턴 종료!!");
                        _workCode = 101011;
                        sendMessageDelayed(END_PATTERN, 3000);
                    }
                }
                break;
            }

            case INPUT_PASSWORD: {
                Log.d(TAG, "# 비밀번호 입력");
                // 시간이 길어지면 붙여넣기가 안되서 약간 빠르게 처리.
                SystemClock.sleep(MathHelper.randomRange(1000, 1200));
                inputText("비밀번호", _accountItem.password);
                _action.touchSearchButton();
                sendMessageDelayed(CHECK_PASSWORD_SAVE, MathHelper.randomRange(3000, 5000));
                break;
            }

            case CHECK_PASSWORD_SAVE: {
                Log.d(TAG, "# 비밀번호 저장 버튼 검사");
                Rect rc = findResourceFromScreen(R.drawable.t_cr_pw_setting);
                if (rc != null) {
                    Log.d(TAG, "# 비밀번호 저장 버튼 터치: " + rc);
                    _action.touchScreen(rc, 15);
                    _handler.sendEmptyMessageDelayed(CHECK_PASSWORD_IGNORE, 1500);
                } else {
                    Log.d(TAG, "비밀번호 저장 버튼 못찾아서 다시검사: " + _retryCount);
                    if (!resendMessageDelayed(msg.what, 2000, 2)) {
                        Log.d(TAG, "비밀번호 저장 버튼 못찾아서 다음으로");
                        _handler.sendEmptyMessageDelayed(CHECK_LOGIN_RESULT_LOADING, 100);
                    }
                }
                break;
            }

            case CHECK_PASSWORD_IGNORE: {
                Log.d(TAG, "# 비밀번호 제외 버튼 검사");
                Rect rc = findResourceFromScreen(R.drawable.t_cr_pw_ignore);
                if (rc != null) {
                    Log.d(TAG, "# 비밀번호 제외 버튼 터치: " + rc);
                    _action.touchScreen(rc, 15);
                    _handler.sendEmptyMessageDelayed(CHECK_LOGIN_RESULT_LOADING, 1500);
                } else {
                    Log.d(TAG, "비밀번호 제외 버튼 다음으로");
                    _handler.sendEmptyMessageDelayed(CHECK_LOGIN_RESULT_LOADING, 100);
                }
                break;
            }

            case CHECK_LOGIN_RESULT_LOADING: {
                Log.d(TAG, "# 네이버 로그인 결과 로딩 검사");
                Rect rc = findResourceFromScreen(_currentPageResourceId);
                if (rc != null) {
                    Log.d(TAG, "# 네이버 로그인 결과 로딩 완료: " + rc);
                    _handler.sendEmptyMessageDelayed(CHECK_LOGIN_RESULT, 100);
                } else {
                    Log.d(TAG, "네이버 로그인 결과 못찾아서 다시검사: " + _retryCount);
                    if (!resendMessageDelayed(msg.what, 2000, 10)) {
                        Log.d(TAG, "네이버 로그인 결과 못찾아서 다음으로");
                        _handler.sendEmptyMessageDelayed(CHECK_LOGIN_FAILED, 100);
                    }
                }
                break;
            }

            case CHECK_LOGIN_RESULT: {
                // 로그인 결과 정상인경우: 원래 페이지로 넘어가고, 쿠키가 생성된다.
                // 실패하면, 로그인페이지에 남는다.
                // 실패를 기준으로 체크 하느냐, 성공을 기준으로 하느냐 인데, 성공은 페이지가 다를수 있다.
                Log.d(TAG, "# 네이버 로그인 결과 검사");
                if (_loginCookieAction.isLoginChrome()) {
                    // 쿠키 업로드 및 다음으로.
                    Log.d(TAG, "# 로그인 중이라 다음으로");

                    NaverAccountManager accountManager = NaverAccountManager.getInstance();
                    NaverAccount account = accountManager.getAccount(_accountItem.id);

                    if (account == null) {
                        account = new NaverAccount();
                        account.accountId = _accountItem.id;
                        accountManager.addAccount(account);
                    }

                    account.nidAut = _loginCookieAction.getNidAutChrome();
                    accountManager.saveData(_context);

                    String cookiesJson = _loginCookieAction.getCookieJsonStringChrome();
//                    String encodedData = cookiesJson;
//
//                    try {
//                        encodedData = URLEncoder.encode(cookiesJson, "UTF-8");
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }

                    // 쿠키 업로드.
                    int result = _accountAction.registerNaverAuthAccountCookie(account.accountId, cookiesJson, "");

                    if (result > 0) {
                        account.loginCookieId = _accountAction.getLoginCookieId();
                        accountManager.saveData(_context);
                    }

                    _handler.sendEmptyMessageDelayed(_loginNextMessage, 100);
                } else {
                    // 여기들어올 확률은 없다. 하지만 안전장치로 넣어둔다.
                    _handler.sendEmptyMessageDelayed(CHECK_LOGIN_BUTTON, 1000);
                }
                break;
            }

            case CHECK_LOGIN_FAILED: {
                Log.d(TAG, "# 네이버 로그인 실패 검사");
                // 일단은 심플하게 하고, 오류가 많이 난다면 빨간 텍스트 검사로 바꿔야한다.
                Rect rc = findResourceFromScreen(R.drawable.t_login_failed);
                if (rc != null) {
                    Log.d(TAG, "# 네이버 로그인 실패 검사 완료: " + rc);
                    _handler.sendEmptyMessageDelayed(CHECK_LOGIN_CLEAR, 100);
                } else {
                    Log.d(TAG, "네이버 로그인 실패 못찾아서 패턴 종료!!");
                    _workCode = 101020;
                    sendMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case CHECK_LOGIN_CLEAR: {
                Log.d(TAG, "# 네이버 로그인 아이디 삭제 버튼 검사");
                Rect rc = findResourceFromScreen(R.drawable.t_login_id_delete);
                if (rc != null) {
                    Log.d(TAG, "# 네이버 로그인 아이디 삭제 버튼 터치: " + rc);
                    _action.touchScreen(rc, 15);

                    // 아이디 상태 업데이트
                    _accountAction.updateStatus(_accountItem.id, 5);
                    NaverAccountManager accountManager = NaverAccountManager.getInstance();
                    accountManager.removeAccount(_accountItem.id);
                    _accountItem = null;

                    sendMessageDelayed(GET_AUTH_ACCOUNT, 100);
                } else {
                    // 여기 들어올일은 없다.
                    Log.d(TAG, "네이버 로그인 아이디 삭제 버튼 못찾아서 패턴 종료!!");
                    _workCode = 101021;
                    sendMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }
        }
    }

    protected Rect findResourceFromScreen(int id) {
        List<Rect> rcList = ImageFinder.getInstance().findResourceFromScreen(_context, id);

        if (rcList != null && rcList.size() > 0) {
            return rcList.get(0);
        }

        return null;
    }

    public void inputText(String title, String text) {
//        if (_item.item.workType == KeywordItem.WORK_TYPE_CLIPBOARD) {
            Log.d(TAG, "# " + title + " 클립보드 복사: " + text);
            _action.copyToClipboard(_webViewManager.getWebView().getContext(), text);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SystemClock.sleep(MathHelper.randomRange(1000, 1500));
                Log.d(TAG, "# " + title + " 붙여넣기");
                _action.pasteClipboard();
            } else {
                // 일단 s7 만 지원.
//                Log.d(TAG, "# 검색창 롱터치");
//                if (!_homeAction.touchSearchBarLong(true)) {
//                    Log.d(TAG, "# 검색창 롱터치에 실패해서 패턴종료.");
//                    _workCode = 130012;
//                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
//                    return;
//                }
//
//                SystemClock.sleep(MathHelper.randomRange(1000, 1500));
//                Log.d(TAG, "# 검색어 붙여넣기");
//                _homeAction.touchPasteButton();
            }
//        } else if (_item.item.workType == 5) {
//            // 추가 가능한 기능있으면 추가 예정..
//        } else {
//            Log.d(TAG, "# " + title + " 입력: " + text);
//            _action.inputKeywordForTyping(text);
//            _handler.sendEmptyMessageDelayed(_nextMessage, MathHelper.randomRange(1000, 3000));
//        }
    }
}
