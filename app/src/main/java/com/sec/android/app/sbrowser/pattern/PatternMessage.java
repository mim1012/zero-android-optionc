package com.sec.android.app.sbrowser.pattern;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sec.android.app.sbrowser.ActivityMCloud;
import com.sec.android.app.sbrowser.engine.SuCommander;
import com.sec.android.app.sbrowser.engine.WebEngine.WebPageData;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.pattern.action.ResultAction;

import java.io.IOException;

public class PatternMessage extends WebViewMessage {

    private static final String TAG = PatternMessage.class.getSimpleName();

    public static final int START_PATTERN = 20001;
    public static final int END_PATTERN = 20002;
    public static final int PAUSE_PATTERN = 20003;

    public static final int CHECK_PATTERN = 20011;

    private final Object _lock = new Object();

    protected int _retryCount;
    protected int _lastMessage = -1;
    protected int _result = ResultAction.FAILED;
    protected int _workCode = 0;
    protected int _workCodeAddition = 0;
    protected WebPageData _pageData = null;

    private PatternHandlerThread _patternHandlerThread = null;
    private Handler _endHandler = null;
    private boolean _isRunning = false;
    private ResultAction _resultAction = null;

    public PatternMessage(WebViewManager manager) {
        super(manager);
        _retryCount = 0;
    }

    public PatternHandlerThread getPatternHandlerThread() {
        return _patternHandlerThread;
    }

    public void setPatternHandlerThread(PatternHandlerThread thread) {
        _patternHandlerThread = thread;
    }

    public void setEndHandler(Handler handler) {
        _endHandler = handler;
    }

    public ResultAction getResultAction() {
        if (_resultAction == null) {
            _resultAction = new ResultAction(_webViewManager.getWebView().getContext());
        }

        return _resultAction;
    }

    // 핸들러 실행중인지 검사.
    public boolean isRunning() {
        synchronized (_lock) {
            return _isRunning;
        }
    }

    public void setIsRunning(boolean isRunning) {
        synchronized (_lock) {
            _isRunning = isRunning;
        }
    }

    public void registerResultFinish(int workCode) {
        getResultAction().registerFinish(_result, workCode);
    }

    protected void registerResultFinish() {
        getResultAction().registerFinish(_result, _workCode + _workCodeAddition);
    }

    public void pressHomeButton() {
        String cmd = "/system/bin/input keyevent KEYCODE_HOME";

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pressBackButton() {
        String cmd = "/system/bin/input keyevent KEYCODE_BACK";

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void sendPatternMessage(Message msg) {
        if (_endHandler != null) {
            _endHandler.sendMessage(msg);
        }
    }

    protected void sendObjectPatternMessage(int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = 1;
        msg.obj = obj;
        sendPatternMessage(msg);
    }

    protected void sendStringPatternMessage(String string) {
        Message msg = Message.obtain();
        msg.what = ActivityMCloud.UPDATE_MESSAGE;
        msg.arg1 = 1;
        msg.obj = string;
        sendPatternMessage(msg);
    }

    protected void sendEndPatternMessage() {
        if (_endHandler != null) {
            Message msg = Message.obtain();
            msg.what = END_PATTERN;
            msg.obj = _patternHandlerThread;
            _endHandler.sendMessage(msg);
//            _endHandler.sendEmptyMessage(END_PATTERN);
        }
    }

    protected void sendMessageDelayed(int what, long delayMillis) {
        _retryCount = 0;
        _handler.sendEmptyMessageDelayed(what, delayMillis);
    }

    protected boolean resendMessageDelayed(int what, long delayMillis, int maxRetryCount) {
        if (_retryCount < maxRetryCount) {
            ++_retryCount;
            _handler.sendEmptyMessageDelayed(what, delayMillis);
            return true;
        }

        return false;
    }

    protected void webViewLoad(Message msg, String url) {

        _lastMessage = msg.what;
        Message newMsg = Message.obtain();
        newMsg.what = MSG_PAGE_LOAD_URL;
        newMsg.obj = url;
        _handler.sendMessage(newMsg);
      // _webViewManager.loadUrl(Config.HOME_URL);
    }

    protected void webViewGoBack(Message msg) {
        _lastMessage = msg.what;
        _handler.sendEmptyMessage(MSG_GO_BACK);
    }

    protected void webViewLoading(Message msg) {
        _lastMessage = msg.what;
        _handler.sendEmptyMessage(MSG_PAGE_LOADING);
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case MSG_PAGE_AUTO_RELOAD_FAILED: {
                Log.d(TAG, "# MSG_PAGE_AUTO_RELOAD_FAILED");
                Log.d(TAG, "- 페이지 자동 재로딩 실패로 패턴 종료.");
                _handler.sendEmptyMessage(END_PATTERN);
                break;
            }
        }
    }
}
