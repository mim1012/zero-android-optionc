package com.loveplusplus.update.xample.pattern;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class PatternHandlerThread extends HandlerThread {

    private static final String TAG = PatternHandlerThread.class.getSimpleName();

    protected Handler _handler = null;
    private OnHandleMessageListener _onHandleMessageListener = null;

    public PatternHandlerThread() {
        super(TAG);
    }

    public PatternHandlerThread(String name) {
        super(name);
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

                if (_onHandleMessageListener != null) {
                    _onHandleMessageListener.onHandleMessage(_handler, msg);
                }
            }
        };
    }

    public interface OnHandleMessageListener {
        void onHandleMessage(Handler handler, Message msg);
    }
}
