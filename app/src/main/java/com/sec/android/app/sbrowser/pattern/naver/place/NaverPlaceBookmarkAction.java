package com.sec.android.app.sbrowser.pattern.naver.place;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.NetworkEngine;
import com.sec.android.app.sbrowser.engine.ThreadMutex;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.models.BookmarkData;
import com.sec.android.app.sbrowser.models.BookmarkItem;

public class NaverPlaceBookmarkAction {

    private static final String TAG = NaverPlaceBookmarkAction.class.getSimpleName();

    private static final int MAX_RETRY_COUNT = 7;

    private final ThreadMutex _mutex = new ThreadMutex();
    private final Context _context;

    private int _retryCount = 0;
    private int _result = 0;
    private BookmarkItem _bookmarkItem = null;

    public NaverPlaceBookmarkAction(Context context) {
        _context = context;
    }

    public BookmarkItem getBookmarkItem() {
        return _bookmarkItem;
    }


    public int checkBookmark(int accountAuthId) {
        _retryCount = 0;
        _result = 0;
        _bookmarkItem = null;
        checkBookmarkToServer(accountAuthId);
        _mutex.threadWait();

        return _result;
    }

    private void checkBookmarkToServer(final int accountAuthId) {
        Log.d(TAG, "### 북마크 여부 요청 -> " + accountAuthId);
        NetworkEngine.getInstance().checkBookmark(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                Log.d(TAG, "북마크 여부 요청 성공");
                BookmarkData bookmarkData = (BookmarkData) data;
                _bookmarkItem = bookmarkData.data;
                _result = 1;
                _mutex.threadWakeUp();
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response == 200) {
                    _mutex.threadWakeUp();
                } else {
                    Log.d(TAG, "알수 없는 에러 (code: " + code + "), accountAuthId: " + accountAuthId);

                    if (_retryCount < MAX_RETRY_COUNT) {
                        Log.d(TAG, "응답 실패로 10초후 다시 시도..." + _retryCount);
                        ++_retryCount;
                        SystemClock.sleep(10000);
                        checkBookmarkToServer(accountAuthId);
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        }, UserManager.getInstance().getLoginId(_context), UserManager.getInstance().imei, accountAuthId);
    }

    public int checkBookmarkAut(String nidAut) {
        _retryCount = 0;
        _result = 0;
        _bookmarkItem = null;
        checkBookmarkAutToServer(nidAut);
        _mutex.threadWait();

        return _result;
    }

    private void checkBookmarkAutToServer(final String nidAut) {
        Log.d(TAG, "### 북마크 여부 요청 -> " + nidAut.substring(0, 6));
        NetworkEngine.getInstance().checkBookmark(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                Log.d(TAG, "북마크 여부 요청 성공");
                BookmarkData bookmarkData = (BookmarkData) data;
                _bookmarkItem = bookmarkData.data;
                _result = 1;
                _mutex.threadWakeUp();
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response == 200) {
                    _mutex.threadWakeUp();
                } else {
                    Log.d(TAG, "알수 없는 에러 (code: " + code + "), nidAut: " + nidAut.substring(0, 6));

                    if (_retryCount < MAX_RETRY_COUNT) {
                        Log.d(TAG, "응답 실패로 10초후 다시 시도..." + _retryCount);
                        ++_retryCount;
                        SystemClock.sleep(10000);
                        checkBookmarkAutToServer(nidAut);
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        }, UserManager.getInstance().getLoginId(_context), UserManager.getInstance().imei, nidAut);
    }

    public int registerBookmarkFinish(int bookmarkId, int result, int workCode) {
        _retryCount = 0;
        _result = 0;
        registerBookmarkFinishToServer(bookmarkId, result, workCode);
        _mutex.threadWait();

        return _result;
    }

    private void registerBookmarkFinishToServer(final int bookmarkId, final int result, final int workCode) {
        Log.d(TAG, "### 북마크 결과 등록 -> " + bookmarkId + ": " + result + " / status: " + workCode);
        NetworkEngine.getInstance().registerBookmarkFinish(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                Log.d(TAG, "북마크 결과 등록 성공");
                _result = 1;
                _mutex.threadWakeUp();
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (_retryCount < MAX_RETRY_COUNT) {
                    Log.d(TAG, "응답 실패로 10초후 다시 시도..." + _retryCount);
                    ++_retryCount;
                    SystemClock.sleep(10000);
                    registerBookmarkFinishToServer(bookmarkId, result, workCode);
                } else {
                    Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                    _result = -1;
                    _mutex.threadWakeUp();
                }
            }
        }, UserManager.getInstance().getLoginId(_context), UserManager.getInstance().imei, bookmarkId, result, workCode);
    }
}
