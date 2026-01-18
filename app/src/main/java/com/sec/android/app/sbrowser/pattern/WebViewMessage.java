package com.sec.android.app.sbrowser.pattern;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.WebViewManager;

public class WebViewMessage implements PatternHandlerThread.OnHandleMessageListener {

    private static final String TAG = WebViewMessage.class.getSimpleName();
    private static final boolean DEBUG_MESSAGE = true;

    public static final int AUTO_RELOAD_DELAY = 5000;
    public static final int MAX_AUTO_RELOAD_COUNT = 10;
    public static final int MAX_AUTO_RELOADING_COUNT = 40;

    public static final int MSG_PAGE_LOADING = 10001;
    public static final int MSG_PAGE_LOAD_URL = 10002;
    public static final int MSG_PAGE_RELOAD = 10003;
    public static final int MSG_PAGE_RELOADING = 10004; // Form 제출 등의 액션이후의 페이지 에러시 이것으로 로딩해야한다.
    public static final int MSG_GO_BACK = 10005;

    public static final int MSG_PAGE_STARTED = 10011;
    public static final int MSG_PAGE_LOADED = 10012;
    public static final int MSG_PAGE_LOAD_FAILED = 10013;
    public static final int MSG_PAGE_LOAD_TIMEOUT = 10014;
    public static final int MSG_PAGE_AUTO_RELOAD_FAILED = 10015;    // 새로고침 시도 초과로 처리 불가 상태 메시지.

    protected final WebViewManager _webViewManager;

    protected Handler _handler = null;
    private int _autoReloadCount = 0;

    public WebViewMessage(WebViewManager manager) {
        _webViewManager = manager;
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        _handler = handler;

        switch (msg.what) {
            case MSG_PAGE_LOADING: {
                if (DEBUG_MESSAGE) Log.d(TAG, "# MSG_PAGE_LOADING");
                _webViewManager.startWebViewLoading();
                _autoReloadCount = 0;
                sendTimeoutMessage();
                break;
            }

            case MSG_PAGE_LOAD_URL: {
                if (DEBUG_MESSAGE) Log.d(TAG, "# MSG_PAGE_LOAD_URL: " + msg.obj);
                _webViewManager.loadUrl((String) msg.obj);
                _autoReloadCount = 0;
                sendTimeoutMessage();
                break;
            }

            case MSG_PAGE_RELOAD: {
                if (DEBUG_MESSAGE) Log.d(TAG, "# MSG_PAGE_RELOAD: " + _autoReloadCount);
                _webViewManager.reload();
                sendTimeoutMessage();
                break;
            }

            case MSG_PAGE_RELOADING: {
                if (DEBUG_MESSAGE) Log.d(TAG, "# MSG_PAGE_RELOADING: " + _autoReloadCount);
                _webViewManager.reloading();
                sendTimeoutMessage();
                break;
            }

            case MSG_GO_BACK: {
                if (DEBUG_MESSAGE) Log.d(TAG, "# MSG_GO_BACK");
                _webViewManager.goBack();
                _autoReloadCount = 0;
                sendTimeoutMessage();
                break;
            }

            case MSG_PAGE_LOADED: {
                if (DEBUG_MESSAGE) Log.d(TAG, "# MSG_PAGE_LOADED");
                removePendingMessages();
                onPageLoaded((String) msg.obj);
                break;
            }

            case MSG_PAGE_LOAD_FAILED: {
                if (DEBUG_MESSAGE) Log.d(TAG, "# MSG_PAGE_LOAD_FAILED");
                removePendingMessages();
                onPageLoadFailed((String) msg.obj);
                break;
            }

            case MSG_PAGE_LOAD_TIMEOUT: {
                if (DEBUG_MESSAGE) Log.d(TAG, "# MSG_PAGE_LOAD_TIMEOUT");
                onPageLoadTimeout();
                break;
            }

            case MSG_PAGE_AUTO_RELOAD_FAILED: {
                if (DEBUG_MESSAGE) Log.d(TAG, "# MSG_PAGE_AUTO_RELOAD_FAILED");
                break;
            }
        }
    }

    public void onPageLoaded(String url) {
        Log.d(TAG, "onPageLoaded: " + url);
    }

    public void onPageLoadFailed(String url) {
        Log.d(TAG, "onPageLoadFailed: " + url);
        reload();
    }

    public void onPageLoadTimeout() {
        Log.d(TAG, "onPageLoadTimeout");
        reload();
    }

    private void sendTimeoutMessage() {
        Log.d(TAG, "sendTimeoutMessage");

        if (_handler != null) {
            // 60초동안 페이지 완료가 되지 않으면 페이지를 다시 로드하도록 유도해준다.
            _handler.sendEmptyMessageDelayed(MSG_PAGE_LOAD_TIMEOUT, 60000);
//            _handler.sendEmptyMessageDelayed(MSG_PAGE_LOAD_TIMEOUT, 500);
        }
    }

    private void removePendingMessages() {
        Log.d(TAG, "removePendingMessages");
        // 성공이든 실패든 결과처리를 받았다면 대기중인 메시지들은 삭제한다.
        if (_handler != null) {
            _handler.removeMessages(MSG_PAGE_LOAD_TIMEOUT);
            _handler.removeMessages(MSG_PAGE_RELOAD);
            _handler.removeMessages(MSG_PAGE_RELOADING);
        }
    }

    private void reload() {
        if (_handler != null) {
            // 지정 회수까지 계속 재로딩 시도한다.
            if (_autoReloadCount < MAX_AUTO_RELOAD_COUNT) {
                ++_autoReloadCount;
                _handler.sendEmptyMessageDelayed(MSG_PAGE_RELOAD, AUTO_RELOAD_DELAY);
            } else {
                // 최대 재시도 회수를 초과하면 재로딩 실패이벤트를 날린다.
                _handler.sendEmptyMessage(MSG_PAGE_AUTO_RELOAD_FAILED);
            }
        }
    }

    protected void reloading() {
        if (_handler != null) {
            // 지정 회수까지 계속 재로딩 시도한다.
            if (_autoReloadCount < MAX_AUTO_RELOADING_COUNT) {
                ++_autoReloadCount;
                _handler.sendEmptyMessageDelayed(MSG_PAGE_RELOADING, AUTO_RELOAD_DELAY);
            } else {
                // 최대 재시도 회수를 초과하면 재로딩 실패이벤트를 날린다.
                _handler.sendEmptyMessage(MSG_PAGE_AUTO_RELOAD_FAILED);
            }
        }
    }
}
