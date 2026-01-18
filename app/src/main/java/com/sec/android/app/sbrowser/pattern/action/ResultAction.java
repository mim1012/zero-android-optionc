package com.sec.android.app.sbrowser.pattern.action;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.NetworkEngine;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;

public class ResultAction {

    private static final String TAG = ResultAction.class.getSimpleName();

    public static final int SUCCESS = 0;
    public static final int FAILED = 1;
//    public static final int ACCOUNT_PROBLEM = 2;    // 계정문제
//    public static final int VIOLATE_TERMS = 3;      // 약관위반, 계정못찾음
//    public static final int NOT_FOUND_IMAGE = 4;    // 이미지를 못찾음
//    public static final int BLOCK_TEMPORARILY = 5;  // 일시적 차단
//    public static final int LIKE_FAILED = 6;        // 좋아요 실패
//    public static final int RETRY_AFTER = 7;        // 몇 분 후에 다시 시도해주세요.
//    public static final int NETWORK_PROBLEM = 8;    // 네트워크 문제
//    public static final int DELETED_POST = 9;       // 삭제된 게시물
//    public static final int NOT_FOUND_TAG = 10;     // 태그를 못찾음
//    public static final int AUTH_EMAIL_FAILED = 11; // 이메일 인증 실패

    private final Object _mutex = new Object();
    private final Context _context;

    private boolean _retry = false;
    private int _maxRetryCount = 7;

    public KeywordItemMoon item = null;

    public ResultAction(Context context) {
        _context = context;
    }

    public void registerFinish(int result, int workCode) {
        for (int i = 0; i < _maxRetryCount; ++i) {
            registerFinishToServer(result, workCode);
            threadWait();

            if (!_retry) {
                break;
            }

            SystemClock.sleep(10000);
        }
    }

    public void registerFinishToServer(final int result, final int workCode) {
        Log.d(TAG, "### 결과등록 -> " + result + " / workCode: " + workCode);
        NetworkEngine.getInstance().registerFinish(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                Log.d(TAG, "결과등록 성공(" + result + " / workCode: " + workCode + ") workId: " + item.uid);
                _retry = false;

                synchronized (_mutex) {
                    _mutex.notify();
                }
            }

            @Override
            public void finishFailed(int response, int code, String message) {
//                if (response != 200) {
//                    _retry = false;
//                } else {
                    _retry = true;
//                }

                Log.d(TAG, "알수 없는 에러 (response: " + response + ", code: " + code + ", " + result + ") workId: " + item.uid);

                // 일단은 실패해도 넘어간다.
                synchronized (_mutex) {
                    _mutex.notify();
                }
            }
            // 키워드 아이디 일단 미사용.
        }, item.uid, UserManager.getInstance().getLoginId(_context), UserManager.getInstance().imei, item.uid, result, workCode);
//        }, item.item.keywordId, loginId, imei, rank, shopRank);

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
