package com.sec.android.app.sbrowser.pattern.action;

import android.util.Log;

import com.sec.android.app.sbrowser.engine.NetworkEngine;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;

public class RankResultAction {

    private static final String TAG = RankResultAction.class.getSimpleName();

    private final Object _mutex = new Object();

    private boolean _retry = false;
    private int _maxRetryCount = 3;

    public String loginId = null;
    public String imei = null;
    public KeywordItemMoon item = null;

    public RankResultAction() {
        imei = UserManager.getInstance().imei;
    }

    public void registerFinish(int rank) {
//        if (rank < 0) {
//            Log.d(TAG, "순위 정보 못찾아서 중단, rank: " + rank + ", uid: " + item.uid + ", keyword: " + item.keyword + ", mid1: " + item.mid1);
//            return;
//        }

        for (int i = 0; i < _maxRetryCount; ++i) {
            registerRankToServer(rank, 0);
            threadWait();

            if (!_retry) {
                break;
            }
        }
    }

    public void registerFinish(int rank, int shopRank) {
//        if (rank < 0) {
//            Log.d(TAG, "순위 정보 못찾아서 중단, rank: " + rank + ", uid: " + item.uid + ", keyword: " + item.keyword + ", mid1: " + item.mid1);
//            return;
//        }

        for (int i = 0; i < _maxRetryCount; ++i) {
            registerRankToServer(rank, shopRank);
            threadWait();

            if (!_retry) {
                break;
            }
        }
    }

    public void registerRankToServer(final int rank, final int shopRank) {
        NetworkEngine.getInstance().updateKeywordRank(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                Log.d(TAG, "결과등록 성공 (rank: " + rank + ", shopRank: " + shopRank + ", kid: " + item.item.keywordId + ", uid: " + item.uid + ", keyword: " + item.keyword + ", mid1: " + item.mid1 + ")");
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

                Log.d(TAG, "알수 없는 에러 (code: " + code + "), rank: " + rank + ", shopRank: " + shopRank + ", kid: " + item.item.keywordId + ", uid: " + item.uid + ", keyword: " + item.keyword + ", mid1: " + item.mid1);

                // 일단은 실패해도 넘어간다.
                synchronized (_mutex) {
                    _mutex.notify();
                }
            }
        }, item.item.keywordId, loginId, imei, rank, shopRank);

//        NetworkEngine.getInstance().registerRank(new NetworkEngine.Callback() {
//            @Override
//            public <T> void finishSuccess(T data) {
//                Log.d(TAG, "결과등록 성공 (rank: " + rank + ", shopRank: " + shopRank + ", uid: " + item.uid + ", keyword: " + item.keyword + ", mid1: " + item.mid1 + ")");
//                _retry = false;
//
//                synchronized (_mutex) {
//                    _mutex.notify();
//                }
//            }
//
//            @Override
//            public void finishFailed(int response, int code, String message) {
//                if (response != 200) {
//                    _retry = false;
//                } else {
//                    _retry = true;
//                }
//
//                Log.d(TAG, "알수 없는 에러 (code: " + code + "), rank: " + rank + ", shopRank: " + shopRank + ", uid: " + item.uid + ", keyword: " + item.keyword + ", mid1: " + item.mid1);
//
//                // 일단은 실패해도 넘어간다.
//                synchronized (_mutex) {
//                    _mutex.notify();
//                }
//            }
//        }, "rank1", item.category, String.valueOf(item.uid), item.mid1,
//                item.mid2.equals(".") ? "" : item.mid2,
//                rank > 0 ? 1 : 0, rank, shopRank > 0 ? 1 : 0, shopRank);
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
