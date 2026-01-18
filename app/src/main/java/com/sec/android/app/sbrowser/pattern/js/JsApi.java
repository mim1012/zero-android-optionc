package com.sec.android.app.sbrowser.pattern.js;

import android.util.Log;
import android.webkit.WebView;

public class JsApi {

    private static final String TAG = JsApi.class.getSimpleName();

    public static final int NO_ERROR = 0;
    public static final int ERR_UNDEFINED = 1;

    private Callback _callback = null;

    private final String _jsInterfaceName;
    private final WebView _webView;

    public JsApi(String jsInterfaceName, WebView webView) {
        _jsInterfaceName = jsInterfaceName;
        _webView = webView;
    }

    public String getInterfaceName() {
        return _jsInterfaceName;
    }

    public void register(final HtmlJsInterface htmlInterface) {
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.addJavascriptInterface(htmlInterface, _jsInterfaceName);
            }
        });
    }

    public void unregister() {
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.removeJavascriptInterface(_jsInterfaceName);
            }
        });
    }

    public void postQuery(final String query) {
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl(query);
            }
        },100);
    }

    public void setCallback(Callback callback) {
        _callback = callback;
    }

    public <T> void callbackOnSuccess(T data) {
        if (_callback != null) {
            _callback.onSuccess(data);
        }
    }

    public void callbackOnFailed(int code, String message) {
        Log.d(TAG, message);

        if (_callback != null) {
            _callback.onFailed(code, message);
        }
    }

    public interface Callback {
        <T> void onSuccess(T data);
        void onFailed(int code, String message);
    }
}
