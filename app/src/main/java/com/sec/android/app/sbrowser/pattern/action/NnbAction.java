package com.sec.android.app.sbrowser.pattern.action;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.NetworkEngine;
import com.sec.android.app.sbrowser.engine.ThreadMutex;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.models.NnbData;

public class NnbAction {

    private static final String TAG = NnbAction.class.getSimpleName();

    private static final int MAX_RETRY_COUNT = 7;

    private final ThreadMutex _mutex = new ThreadMutex();

    public String loginId = null;
    public String imei = null;

    private int _retryCount = 0;
    private int _result = 0;
    private NnbData _nnbData = null;

    public NnbAction() {
        imei = UserManager.getInstance().imei;
    }

    public NnbData getNnbData() {
        return _nnbData;
    }

    public String getNnb() {
        if (_nnbData == null) {
            return null;
        }

        return _nnbData.nnb;
    }

    public String getUserAgent() {
        if (_nnbData == null) {
            return null;
        }

        return _nnbData.ua;
    }

    public String getChromeVersion() {
        if (_nnbData == null) {
            return null;
        }

        return _nnbData.chromeVersion;
    }

    public String getBrowserVersion() {
        if (_nnbData == null) {
            return null;
        }

        return _nnbData.browserVersion;
    }

    public int requestNnb() {
        _retryCount = 0;
        _result = 0;
        _nnbData = null;
        getNnbFromServer();
        _mutex.threadWait();

        return _result;
    }

    private void getNnbFromServer() {
        NetworkEngine.getInstance().getNnb(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                NnbData nnbData = (NnbData) data;

                if (!TextUtils.isEmpty(nnbData.nnb)) {
                    _nnbData = nnbData;
                    _result = 1;
                }

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
                        getNnbFromServer();
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        }, loginId, imei);
    }

    public int requestNnbPc() {
        _retryCount = 0;
        _result = 0;
        _nnbData = null;
        getNnbPcFromServer();
        _mutex.threadWait();

        return _result;
    }

    private void getNnbPcFromServer() {
        NetworkEngine.getInstance().getNnbPc(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                NnbData nnbData = (NnbData) data;

                if (!TextUtils.isEmpty(nnbData.nnb)) {
                    _nnbData = nnbData;
                    _result = 1;
                }

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
                        getNnbPcFromServer();
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        }, loginId, imei);
    }

    public void registerNnb(String nnb, String ua, boolean useNnb) {
        _retryCount = 0;
        _result = 0;
        registerNnbToServer(nnb, ua, useNnb);
        _mutex.threadWait();
    }

    public void registerNnbToServer(final String nnb, final String ua, final boolean useNnb) {
        NetworkEngine.getInstance().registerNaverData(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                Log.d(TAG, "NNB 등록 성공 (nnb: " + nnb + ", ua: " + ua + ")");
                _result = 1;
                _mutex.threadWakeUp();
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response == 200) {
                    _mutex.threadWakeUp();
                } else {
                    Log.d(TAG, "알수 없는 에러 (nnb: " + nnb + ", ua: " + ua + ")");

                    if (_retryCount < MAX_RETRY_COUNT) {
                        Log.d(TAG, "응답 실패로 10초후 다시 시도..." + _retryCount);
                        ++_retryCount;
                        SystemClock.sleep(10000);
                        registerNnbToServer(nnb, ua, useNnb);
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        }, loginId, imei, nnb, ua, useNnb);
    }
}
