package com.sec.android.app.sbrowser.pattern.naver.shop;

import static com.sec.android.app.sbrowser.pattern.BasePatternAction.getRandomName;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Locale;

public class NaverShopRankPatternAction {

    private static final String TAG = NaverShopRankPatternAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "RankView";

    private WebView _webView = null;
    private final Object _mutex = new Object();
    private int _rank = -1;
    String url_;
    public NaverShopRankPatternAction(WebView webView,String url) {
        _webView = webView;

        url_ = url;
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.addJavascriptInterface(new GetHtmlJavascriptInterface(), JS_INTERFACE_NAME);
            }
        });
    }

    public void endPattern() {
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.removeJavascriptInterface(JS_INTERFACE_NAME);
            }
        });
    }

    public void registerRank(String keyword, String url) {
        getRankInWebView();
        threadWait();

        if (_rank >= 0) {
            registerRankToServer(keyword, url);
            threadWait();
        }
    }

    public void registerRankToServer(String loginId, String imei) {
//        String serviceType = getServiceName(item.type);
//
//        if (serviceType == null) {
//            Log.d(TAG, "알수없는 서비스 타입.");
//            return;
//        }
        try {
            HttpClient client = new DefaultHttpClient();
            final String URL = "http://125.131.133.11/api/keyword/request/";
            String simpleData = "?token=ed0ad568abaeb575745e6a5345bbfa34&worker=1";
            HttpPost post = new HttpPost(URL + simpleData);
            client.execute(post);
        } catch (IOException ex) {

        }


    }

    private String getSubScriptQuery(String funcName, String param) {
        return String.format(Locale.getDefault(), "window.%s.%s(%s);",
                JS_INTERFACE_NAME, funcName, param);
    }

    private void getRankInWebView() {
        _webView.post(new Runnable() {
            @Override
            public void run() {
                String query = "javascript:(function() {"
                        + "var nodes = document.querySelectorAll('._panel .total_tit');"
                        + "var rank = 0;"
                        + "for (var i = 0; i < nodes.length; ++i) {"
                        + "  if (nodes[i].href === '" + url_ + "') {"
                        + "    rank = i + 1;"
                        + "    break;"
                        + "  }"
                        + "}"
                        + getSubScriptQuery("checkRank", "rank")
                        + "})();";
                _webView.loadUrl(query);
            }
        });
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

    public class GetHtmlJavascriptInterface {
        @JavascriptInterface
        public void checkRank(int rank) {
            Log.d(TAG, "target rank:" + rank);
            _rank = rank;

            synchronized (_mutex) {
                Log.d(TAG, "락 해제");
                _mutex.notify();
            }
        }
    }
}
