package com.sec.android.app.sbrowser.pattern.naver;

import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.NetworkEngine;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.models.AccountData;
import com.sec.android.app.sbrowser.models.AccountItem;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.models.LoginCookieData;
import com.sec.android.app.sbrowser.pattern.action.BaseAction;

public class NaverAccountAction extends BaseAction {

    private static final String TAG = NaverAccountAction.class.getSimpleName();

    public static final int STATUS_NONE = 0;
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_READY = 11;
    public static final int STATUS_SIGN_UP_DOING = 12;
    public static final int STATUS_SIGN_UP_COMPLETED = 13;
    public static final int STATUS_SIGN_UP_FAILED = 14;
    public static final int STATUS_ERROR_LOGIN_FAILED = 51;     // 로그인실패
    public static final int STATUS_ERROR_ID_LOCK = 52;          // 보호조치
    public static final int STATUS_ERROR_IP_PROTECTION = 53;    // 아이피보호조치
    public static final int STATUS_ERROR_PHONE_PICK = 54;       // 전화번호 선택
    public static final int STATUS_ERROR_CAPTCHA = 55;          // 영수증 캡차 (로그인페이지 접근IP와 로그인 버튼 누를때 IP가 다르면 발생)
    public static final int STATUS_ERROR_MASS_CREATION = 56;    // 대량생성
    public static final int STATUS_ERROR_SPAM_PROTECTION = 57;  // 스팸보호조치
    public static final int STATUS_ERROR_UNKNOWN = 100;
    public static final int STATUS_ERROR_UNKNOWN_MESSAGE = 101;
    public static final int STATUS_ERROR_UNKNOWN_ID = 102;
    public static final int STATUS_ERROR_UNKNOWN_IP = 103;
    public static final int STATUS_ERROR_UNKNOWN_PHONE = 104;
    public static final int STATUS_ERROR_UNKNOWN_CAPTCHA = 105;
    public static final int STATUS_ERROR_UNKNOWN_CREATION = 106;

    public String loginId = null;
    public String imei = null;
    public KeywordItemMoon item = null;

    private AccountItem _accountItem = null;
    private long _loginCookieId = -1;

    public NaverAccountAction() {
        imei = UserManager.getInstance().imei;
    }

    public AccountItem getAccount() {
        return _accountItem;
    }

    public long getLoginCookieId() {
        return _loginCookieId;
    }

    public int updateStatus(int status) {
        _retryCount = 0;
        _result = 0;
        updateStatusToServer(item.item.account.id, status);
        _mutex.threadWait();

        return _result;
    }

    public int updateStatus(String accountId, int status) {
        _retryCount = 0;
        _result = 0;
        updateStatusToServer(accountId, status);
        _mutex.threadWait();

        return _result;
    }

    private void updateStatusToServer(final String accountId, final int status) {
        NetworkEngine.getInstance().updateNaverAccountStatus(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                Log.d(TAG, "상태등록 성공 (status: " + status + ", accountId: " + accountId + ")");
                _result = 1;
                _mutex.threadWakeUp();
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response == 200) {
                    _mutex.threadWakeUp();
                } else {
                    Log.d(TAG, "알수 없는 에러 (code: " + code + "), status: " + status + ", accountId: " + accountId);

                    if (_retryCount < MAX_RETRY_COUNT) {
                        Log.d(TAG, "응답 실패로 10초후 다시 시도..." + _retryCount);
                        ++_retryCount;
                        SystemClock.sleep(10000);
                        updateStatusToServer(accountId, status);
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        }, accountId, loginId, imei, status);
    }

    public int requestGetAccount() {
        _retryCount = 0;
        _result = 0;
        _accountItem = null;
        requestGetAccountToServer();
        _mutex.threadWait();
        return _result;
    }

    private void requestGetAccountToServer() {
        NetworkEngine.getInstance().getNaverAccount(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                AccountData accountData = (AccountData) data;
                _accountItem = accountData.data;
                _result = 1;
                _mutex.threadWakeUp();
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response == 200) {
                    _mutex.threadWakeUp();
                } else {
                    if (_retryCount < MAX_RETRY_COUNT) {
                        Log.d(TAG, "응답 실패로 10초후 다시 시도..." + _retryCount);
                        ++_retryCount;
                        SystemClock.sleep(10000);
                        requestGetAccountToServer();
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        }, loginId, imei, item.item.trafficId, item.item.keywordId);
    }


    // 신규 계정 로그인 처리용.
    public int requestGetAuthAccount() {
        _retryCount = 0;
        _result = 0;
        _accountItem = null;
        requestGetAuthAccountToServer();
        _mutex.threadWait();
        return _result;
    }

    private void requestGetAuthAccountToServer() {
        NetworkEngine.getInstance().getNaverAuthAccount(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                AccountData accountData = (AccountData) data;
                _accountItem = accountData.data;
                _result = 1;
                _mutex.threadWakeUp();
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response == 200) {
                    _mutex.threadWakeUp();
                } else {
                    if (_retryCount < MAX_RETRY_COUNT) {
                        Log.d(TAG, "응답 실패로 10초후 다시 시도..." + _retryCount);
                        ++_retryCount;
                        SystemClock.sleep(10000);
                        requestGetAuthAccountToServer();
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        }, loginId, imei);
    }
    public int registerNaverAuthAccountCookie(String naverId, String data, String ua) {
        _retryCount = 0;
        _result = 0;
        _loginCookieId = -1;
        registerNaverAuthAccountCookieToServer(naverId, data, ua);
        _mutex.threadWait();

        return _result;
    }

    private void registerNaverAuthAccountCookieToServer(final String naverId, final String cookieData, final String ua) {
        NetworkEngine.getInstance().registerNaverAuthAccountCookie(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                Log.d(TAG, "쿠키등록 성공 (naverId: " + naverId + ", data: " + cookieData + ", ua: " + ua + ")");
                LoginCookieData loginCookieData = (LoginCookieData) data;
                _loginCookieId = loginCookieData.loginCookieId;
                _result = 1;
                _mutex.threadWakeUp();
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response == 200) {
                    _mutex.threadWakeUp();
                } else {
                    Log.d(TAG, "알수 없는 에러 (code: " + code + "), naverId: " + naverId + ", data: " + cookieData + ", ua: " + ua);

                    if (_retryCount < MAX_RETRY_COUNT) {
                        Log.d(TAG, "응답 실패로 10초후 다시 시도..." + _retryCount);
                        ++_retryCount;
                        SystemClock.sleep(10000);
                        registerNaverAuthAccountCookieToServer(naverId, cookieData, ua);
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        }, loginId, imei, naverId, cookieData, ua);
    }
}
