package com.sec.android.app.sbrowser.pattern.action;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.NetworkEngine;
import com.sec.android.app.sbrowser.engine.ThreadMutex;
import com.sec.android.app.sbrowser.engine.UserManager;

public class NaverCookieOtherAction {

    private static final String TAG = NaverCookieOtherAction.class.getSimpleName();

    private static final int MAX_RETRY_COUNT = 7;

    private final ThreadMutex _mutex = new ThreadMutex();
    private final Context _context;

    private int _retryCount = 0;
    private int _result = 0;

    public NaverCookieOtherAction(Context context) {
        _context = context;
    }

    public int registerNaverCookieOthers(long cookieId, String others) {
        _retryCount = 0;
        _result = 0;
        registerNaverCookieOthersToServer(cookieId, others);
        _mutex.threadWait();

        return _result;
    }

    private void registerNaverCookieOthersToServer(final long cookieId, final String others) {
        Log.d(TAG, "### 쿠키 등록 -> " + cookieId + " / others: " + others);
        NetworkEngine.getInstance().registerNaverCookieOthers(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                Log.d(TAG, "쿠키 등록 성공");
                _mutex.threadWakeUp();
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (_retryCount < MAX_RETRY_COUNT) {
                    Log.d(TAG, "응답 실패로 10초후 다시 시도..." + _retryCount);
                    ++_retryCount;
                    SystemClock.sleep(10000);
                    registerNaverCookieOthersToServer(cookieId, others);
                } else {
                    Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                    _result = -1;
                    _mutex.threadWakeUp();
                }
            }
        }, UserManager.getInstance().getLoginId(_context), UserManager.getInstance().imei, cookieId, others);
    }
}
