package com.sec.android.app.sbrowser.system;

import android.content.Context;
import android.widget.Toast;

public class ToastManager {

    private static final String TAG = ToastManager.class.getSimpleName();

    private static final int MIN_DELAY_MILLIS = 1000;

    private static class LazyHolder {
        public static final ToastManager INSTANCE = new ToastManager();
    }

    public static ToastManager getInstance() {
        return ToastManager.LazyHolder.INSTANCE;
    }

    private long _prevShowTime = 0;
    private Toast _toast = null;
    private CharSequence _text = null;

    public void showText(Context context, CharSequence text, int duration) {
        if (_toast != null) {
            _toast.cancel();
        }

        _toast = Toast.makeText(context, text, duration);
        _toast.show();

//        if (_toast != null) {
////            _toast.getView().isShown()
//            if (System.currentTimeMillis() - _showTime > MIN_DELAY_MILLS) {
//                _toast.cancel();
//                _toast = null;
//            }
//        }

//        if (System.currentTimeMillis() - _prevShowTime > MIN_DELAY_MILLIS) {
//            if (_toast != null) {
//                _toast.cancel();
//                _toast = null;
//            }
//        }
//
//        if (_toast == null) {
//            _text = text;
//            _prevShowTime = System.currentTimeMillis();
//            _toast = Toast.makeText(context, text, duration);
//            _toast.show();
//        } else {
//            if (!_text.equals(text)) {
//                _text = text;
//
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
////                        _toast.cancel();
//                        _toast.setText(text);
//                        _toast.show();
//                    }
//                });
//            }
//        }
    }

    private ToastManager() {
    }
}
