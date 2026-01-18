package com.sec.android.app.sbrowser.pattern.naver.view;

import static com.sec.android.app.sbrowser.pattern.BasePatternAction.getRandomName;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.BuildConfig;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;
public class NaverViewRankPatternAction {

    private static final String TAG = NaverViewRankPatternAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "RankView";

    private WebView _webView = null;
    private final Object _mutex = new Object();
    private int _rank = -1;
    String url_;
    public NaverViewRankPatternAction(WebView webView,String url) {
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

    public void registerRank(String uid ,String url) {
        getRankInWebView();
        threadWait();

        if (_rank >= 0) {
            registerRankToServer(uid,url);
            threadWait();
        }
    }

    public void registerRankToServer(String uid,String url) {
//        String serviceType = getServiceName(item.type);
//
//        if (serviceType == null) {
//            Log.d(TAG, "알수없는 서비스 타입.");
//            return;
//        }
        try {
            String 데이터 = "";
            String URL = BuildConfig.SERVER_URL+"/main/nview/rankset.php";
            url =  URLEncoder.encode(url, "UTF-8");
            HttpClient client = new DefaultHttpClient();

            String simpleData = "?uid="+uid+"&rank="+_rank+"&mid1="+url;
            HttpGet get = new HttpGet(URL + simpleData);
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                데이터 = EntityUtils.toString(entity);
            }

            JSONObject data = new JSONObject(데이터);
            Log.d("랭킹 등록 결과", data.getString("status"));

            /*
            {""status"": 0 }  : 성공
            {""status"": 1 } : 실패
             */

        } catch (IOException ex) {

        }catch (JSONException e) {
            e.printStackTrace();
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
                        + "var nodes = document.querySelectorAll('._panel ._svp_item:not(._pwr_content) .total_tit');"
                        + "var rank = 0;"
                        + "for (var i = 0; i < nodes.length; ++i) {"
                        + "  if (nodes[i].href.includes('" + url_ + "')) {"
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
