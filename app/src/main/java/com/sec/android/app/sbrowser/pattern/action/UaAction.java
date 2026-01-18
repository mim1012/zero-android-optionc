package com.sec.android.app.sbrowser.pattern.action;

import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.NetworkEngine;
import com.sec.android.app.sbrowser.engine.ThreadMutex;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.models.NnbData;

public class UaAction {

    private static final String TAG = UaAction.class.getSimpleName();

    private static final int MAX_RETRY_COUNT = 7;

    private final ThreadMutex _mutex = new ThreadMutex();

    public String loginId = null;
    public String imei = null;

    private int _retryCount = 0;
    private int _result = 0;
    private NnbData _nnbData = null;

    public UaAction() {
        imei = UserManager.getInstance().imei;
    }

    public NnbData getNnbData() {
        return _nnbData;
    }

    public String getUserAgent() {
        return _nnbData.ua;
    }

    public int requestUa() {
        _retryCount = 0;
        _result = 0;
        _nnbData = null;
        getUaFromServer();
        _mutex.threadWait();

        return _result;
    }

    private void getUaFromServer() {
        NetworkEngine.getInstance().getUa(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                _nnbData = (NnbData) data;

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
                        getUaFromServer();
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        }, loginId, imei);
    }

    public int requestUaPc() {
        _retryCount = 0;
        _result = 0;
        _nnbData = null;
        getUaPcFromServer();
        _mutex.threadWait();

        return _result;
    }

    private void getUaPcFromServer() {
        NetworkEngine.getInstance().getUaPc(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                _nnbData = (NnbData) data;

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
                        getUaPcFromServer();
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        }, loginId, imei);
    }
}
