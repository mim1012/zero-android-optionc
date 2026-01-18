package com.sec.android.app.sbrowser.pattern.action;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.NetworkEngine;
import com.sec.android.app.sbrowser.engine.ThreadMutex;
import com.sec.android.app.sbrowser.engine.UserManager;

public class NaverCookieStatusAction {

    private static final String TAG = NaverCookieStatusAction.class.getSimpleName();

    private static final int MAX_RETRY_COUNT = 7;

    private final ThreadMutex _mutex = new ThreadMutex();
    private final Context _context;

    private int _retryCount = 0;
    private int _result = 0;

    public NaverCookieStatusAction(Context context) {
        _context = context;
    }

    public int registerNaverCookieStatus(long cookieId, int status) {
        _retryCount = 0;
        _result = 0;
        registerNaverCookieStatusToServer(cookieId, status);
        _mutex.threadWait();

        return _result;
    }

    private void registerNaverCookieStatusToServer(final long loginCookieId, final int status) {
        Log.d(TAG, "### 쿠키 상태 등록 -> " + loginCookieId + " / status: " + status);
        NetworkEngine.getInstance().registerNaverCookieStatus(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                Log.d(TAG, "쿠키 상태 등록 성공");
                _mutex.threadWakeUp();
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (_retryCount < MAX_RETRY_COUNT) {
                    Log.d(TAG, "응답 실패로 10초후 다시 시도..." + _retryCount);
                    ++_retryCount;
                    SystemClock.sleep(10000);
                    registerNaverCookieStatusToServer(loginCookieId, status);
                } else {
                    Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                    _result = -1;
                    _mutex.threadWakeUp();
                }
            }
        }, UserManager.getInstance().getLoginId(_context), UserManager.getInstance().imei, loginCookieId, status);
    }
}
