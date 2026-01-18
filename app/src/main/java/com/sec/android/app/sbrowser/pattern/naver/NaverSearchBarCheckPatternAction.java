package com.sec.android.app.sbrowser.pattern.naver;

import static com.sec.android.app.sbrowser.pattern.BasePatternAction.getRandomName;

import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.util.Locale;

public class NaverSearchBarCheckPatternAction {

    private static final String TAG = NaverSearchBarCheckPatternAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zsbch";

    private final Object _mutex;
    private final WebView _webView;

    private int _findCount = 0;

    protected Handler _handler = null;

    public NaverSearchBarCheckPatternAction(WebView webView) {
        _mutex = new Object();
        _webView = webView;

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

    public boolean isFocus() {
        return (_findCount > 0);
    }


    // 핸들러 방식으로 하려면, 웹뷰처럼 타임아웃을 주는 방법으로 해야한다.
    // 스크립트 핸들링에 오류가 발생하면 일반적인 방법으로는 처리가 멈춘다.
//    public void checkSearch(Handler handler) {
//        getSearchBarFocus();
//        threadWait();
//    }

    public void checkSearchBarShown() {
        _findCount = 0;
        getSearchBarFocus();
        threadWait();
    }

    public void checkPcSearchBarShown() {
        _findCount = 0;
        getPcSearchBarFocus();
        threadWait();
    }

    private String getScriptQuery(String funcName, String param) {
        return String.format(Locale.getDefault(), "javascript:window.%s.%s(%s);",
                JS_INTERFACE_NAME, funcName, param);
    }

    private String getSubScriptQuery(String funcName, String param) {
        return String.format(Locale.getDefault(), "window.%s.%s(%s);",
                JS_INTERFACE_NAME, funcName, param);
    }

    private String getSearchBarFocusQuery() {
        return "document.querySelectorAll('.sch_focus')";
    }

    private String getPcSearchBarFocusQuery() {
        return "document.querySelectorAll('.search_area.is_focus')";
//        return "document.querySelectorAll('.green_window.window_focus')";
//        return "document.querySelectorAll('#autoFrame:not([style*=\"display:none\"]):not([style*=\"display: none\"])')";
    }

    private void getSearchBarFocus() {
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
//                String query = getScriptQuery("getCount", getSearchBarFocusQuery() + ".length");
//                Log.d(TAG, "getSearchBarFocus:" + query);
                _webView.loadUrl(getScriptQuery("getCount", getSearchBarFocusQuery() + ".length"));
            }
        }, 100);
    }

    private void getPcSearchBarFocus() {
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
//                String query = getScriptQuery("getCount", getSearchBarFocusQuery() + ".length");
//                Log.d(TAG, "getSearchBarFocus:" + query);
                _webView.loadUrl(getScriptQuery("getCount", getPcSearchBarFocusQuery() + ".length"));
            }
        }, 100);
    }

//    private void getRankInWebView() {
//        _webView.post(new Runnable() {
//            @Override
//            public void run() {
//                String query = "javascript:(function() {"
//                        + "var nodes = document.querySelectorAll('._list ._item .total_tit');"
//                        + "var rank = 0;"
//                        + "for (var i = 0; i < nodes.length; ++i) {"
//                        + "  if (nodes[i].href === '" + item.url + "') {"
//                        + "    rank = i + 1;"
//                        + "    break;"
//                        + "  }"
//                        + "}"
//                        + getSubScriptQuery("checkRank", "rank")
//                        + "})();";
//                _webView.loadUrl(query);
//            }
//        });
//    }

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
        public void getCount(int count) {
            _findCount = count;

            synchronized (_mutex) {
                _mutex.notify();
            }
        }

//        @JavascriptInterface
//        public void checkRank(int rank) {
//            Log.d(TAG, "target rank:" + rank);
//            _rank = rank;
//
//            synchronized (_mutex) {
//                Log.d(TAG, "락 해제");
//                _mutex.notify();
//            }
//        }
    }
}
