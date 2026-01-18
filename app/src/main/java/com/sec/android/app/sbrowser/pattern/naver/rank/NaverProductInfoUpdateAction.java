package com.sec.android.app.sbrowser.pattern.naver.rank;

import android.util.Log;

import com.sec.android.app.sbrowser.engine.NetworkEngine;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;

public class NaverProductInfoUpdateAction {

    private static final String TAG = NaverProductInfoUpdateAction.class.getSimpleName();

    private final Object _mutex = new Object();

    private boolean _retry = false;
    private int _maxRetryCount = 3;

    public String loginId = null;
    public String imei = null;
    public KeywordItemMoon item = null;

    public NaverProductInfoUpdateAction() {
        imei = UserManager.getInstance().imei;
    }

    public void registerInfo(String productName, String storeName, String mallId, String catId, String productUrl, String sourceType, String sourceUrl) {
        for (int i = 0; i < _maxRetryCount; ++i) {
            registerRankToServer(productName, storeName, mallId, catId, productUrl, sourceType, sourceUrl);
            threadWait();

            if (!_retry) {
                break;
            }
        }
    }

    public void registerRankToServer(final String productName, final String storeName, final String mallId, final String catId, final String productUrl, String sourceType, String sourceUrl) {
        String logString = "productTitle: " + productName + ", storeName: " + storeName + ", mallId: " + mallId + ", catId: " + catId + ", productUrl: " + productUrl + ", sourceType: " + sourceType + ", sourceUrl: " + sourceUrl + ", kid: " + item.item.keywordId + ", uid: " + item.uid + ", keyword: " + item.keyword + ", mid1: " + item.mid1;

        NetworkEngine.getInstance().updateProductInfo(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                Log.d(TAG, "결과등록 성공 (" + logString + ")");
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

                Log.d(TAG, "알수 없는 에러 (code: " + code + "), " + logString);

                // 일단은 실패해도 넘어간다.
                synchronized (_mutex) {
                    _mutex.notify();
                }
            }
        }, item.item.keywordId, loginId, imei, productName, storeName, mallId, catId, productUrl, sourceType, sourceUrl);
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
