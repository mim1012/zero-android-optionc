package com.sec.android.app.sbrowser.pattern.naver.place;

import android.util.Log;

import com.sec.android.app.sbrowser.engine.NetworkEngine;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;

public class NaverPlaceResultAction {

    private static final String TAG = NaverPlaceResultAction.class.getSimpleName();

    private final Object _mutex = new Object();

    private boolean _retry = false;
    private int _maxRetryCount = 3;

    public String imei = null;
    public KeywordItemMoon item = null;

    public NaverPlaceResultAction() {
    }

    public void registerFinish(int rank) {
        for (int i = 0; i < _maxRetryCount; ++i) {
            registerFinishToServer(rank);
            threadWait();

            if (!_retry) {
                break;
            }
        }
    }

    public void registerFinishToServer(final int rank) {
        NetworkEngine.getInstance().registerPlaceFinish(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                Log.d(TAG, "결과등록 성공 (rank: " + rank + ", uid: " + item.uid + ", keyword: " + item.keyword + ")");
                _retry = false;

                synchronized (_mutex) {
                    _mutex.notify();
                }
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response != 200) {
                    _retry = false;
                } else {
                    _retry = true;
                }

                Log.d(TAG, "알수 없는 에러 (code: " + code + "), rank: " + rank + ", uid: " + item.uid + ", keyword: " + item.keyword);

                // 일단은 실패해도 넘어간다.
                synchronized (_mutex) {
                    _mutex.notify();
                }
            }
        }, "admecca", String.valueOf(item.uid), item.keyword, item.mid1, item.url, item.agency, item.account, String.valueOf(rank));
    }

    private void threadWait() {
        synchronized (_mutex) {
            try {
                _mutex.wait(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
