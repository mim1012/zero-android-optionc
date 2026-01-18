package com.sec.android.app.sbrowser.pattern.ohouse.rank;

import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.ThreadMutex;
import com.sec.android.app.sbrowser.library.ohouse.OHouseApi;
import com.sec.android.app.sbrowser.library.ohouse.client.OHouseClient;
import com.sec.android.app.sbrowser.library.ohouse.retrofit.models.FeedData;

public class OHouseFeedAction {

    private static final String TAG = OHouseFeedAction.class.getSimpleName();

    private static final int MAX_RETRY_COUNT = 7;

    private final ThreadMutex _mutex = new ThreadMutex();

    private int _retryCount = 0;
    private int _result = 0;
    private FeedData _feedData = null;

    public OHouseFeedAction() {
    }

    public FeedData getFeedData() {
        return _feedData;
    }

    public int requestFeed(String query, int page) {
        _retryCount = 0;
        _result = 0;
        _feedData = null;
        getFeedsFromServer(query, page);
        _mutex.threadWait();

        return _result;
    }

    private void getFeedsFromServer(String query, int page) {
        OHouseApi.getInstance().getFeeds(query, page, new OHouseClient.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                _feedData = (FeedData) data;
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
                        getFeedsFromServer(query, page);
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        });
    }

    private boolean validateData() {
        if (_feedData == null) {
            return false;
        }

        if (_feedData.productions == null) {
            return false;
        }

        return true;
    }

    public int getProductionRank(String id) {
        if (!validateData()) {
            return -1;
        }

        int rank = 1;

        for (FeedData.Production production : _feedData.productions) {
            if (production.id.equals(id)) {
                return rank;
            }

            ++rank;
        }

        return -1;
    }

    public int getProductionCount() {
        if (!validateData()) {
            return -1;
        }

        int count = 0;

        for (FeedData.Production production : _feedData.productions) {
//            if (!production.type.equalsIgnoreCase("production")) {
//                Log.d(TAG, "production 이 아니라서 넘어감");
//                continue;
//            }

            ++count;
        }

        return count;
    }

    public FeedData.Production getProduction(String id) {
        if (!validateData()) {
            return null;
        }

        for (FeedData.Production production : _feedData.productions) {
            if (production.id.equals(id)) {
                return production;
            }
        }

        return null;
    }
}
