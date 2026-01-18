package com.sec.android.app.sbrowser.pattern;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class PatternHandlerThread extends HandlerThread {

    private static final String TAG = PatternHandlerThread.class.getSimpleName();

    public static final int MESSAGE_PREPARED_THREAD = -1;

    private Handler _handler = null;
    private OnHandleMessageListener _onHandleMessageListener = null;
    private long _lastMessageTime = 0;

    public PatternHandlerThread() {
        super(TAG);
    }

    public PatternHandlerThread(String name) {
        super(name);
    }

    public OnHandleMessageListener getOnHandleMessageListener() {
        return _onHandleMessageListener;
    }

    public long getLastMessageTime() {
        return _lastMessageTime;
    }

    public void setOnHandleMessageListener(OnHandleMessageListener listener) {
        _onHandleMessageListener = listener;
    }

    public Handler getHandler() {
        return _handler;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();

        _handler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                _lastMessageTime = System.currentTimeMillis();

                if (_onHandleMessageListener != null) {
                    _onHandleMessageListener.onHandleMessage(_handler, msg);
                }
            }
        };

        _handler.sendEmptyMessageDelayed(MESSAGE_PREPARED_THREAD, 500);
    }

    public interface OnHandleMessageListener {
        void onHandleMessage(Handler handler, Message msg);
    }
}
