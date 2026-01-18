package com.sec.android.app.sbrowser.engine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import com.sec.android.app.sbrowser.BuildConfig;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpEngine;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpHeader;
import com.sec.android.app.sbrowser.pattern.BasePatternAction;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import im.delight.android.webview.AdvancedWebView;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebViewManager {

    private static final String TAG = WebViewManager.class.getSimpleName();
    public static final String BLANK_PAGE = "about:blank";

    private final WebView _webView;
    private final Object _lock;
    private final ThreadMutex _mutex = new ThreadMutex();

    private OnPageListener _onPageListener = null;
    private OnPageFinishedListener _onPageFinishedListener = null;
    private OnProgressChangedListener _onProgressChangedListener = null;

    private final Map<String, String> _httpHeaders = new HashMap<>();
    private Activity _activity = null;
    private boolean _started = false;
    private boolean _autoStart = false;
    private boolean _hasError = false;
    private String _userAgent = null;
    private boolean _blankPageClearHistory = true;
    private boolean _blankPageClearCache = true;
    private boolean _blankPageClearStorage = true;
    private int _interceptType = 0;
    private boolean _deleteLocalStorage = true;

    private BasePatternAction _patternAction = null;

    private final WebResourceController _controller;
    private OkHttpClient _httpClient = null;

    private String _proxyUsername = null;
    private String _proxyPassword = null;

    private boolean _showLog = false;

    public WebViewManager(WebView webView) {
        _webView = webView;
        _lock = new Object();
//        _userAgent = webView.getSettings().getUserAgentString();

//        val headerInterceptor = Interceptor {
//            val request = it.request()
//                    .newBuilder()
//                    .addHeader("User-Agent", USER_AGENT)
//                    .build()
//            return@Interceptor it.proceed(request)
//        }

//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        _controller = new WebResourceController(this);
        webViewSetting();

        if (BuildConfig.FLAVOR_build.contains("beta")) {
            _showLog = true;
        }
    }

    private OkHttpClient getHttpClient() {
        if (_httpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
//                .addNetworkInterceptor(loggingInterceptor)
//                            .set
                    .addNetworkInterceptor(new WebResourceController.EncodingInterceptor());

            if (_showLog) {
                builder.addNetworkInterceptor(new LoggingInterceptor());
            }

            _httpClient = builder.build();
            _controller.setHttpClient(_httpClient);
        }

        return _httpClient;
    }

    static class LoggingInterceptor implements Interceptor {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Log.i("LoggingInterceptor", String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), HttpEngine.getHeadersLog(request.headers())));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Log.i("LoggingInterceptor", String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, HttpEngine.getHeadersLog(response.headers())));

            return response;
        }
    }

    public void setActivity(Activity activity) {
        _activity = activity;
    }

    public void setOnPageListener(OnPageListener listener) {
        _onPageListener = listener;
    }

    public void setOnPageFinishedListener(OnPageFinishedListener listener) {
        _onPageFinishedListener = listener;
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        _onProgressChangedListener = listener;
    }

    // 웹뷰 로딩시 자동 시작.
    // 편의 기능이지만 네이버에서는 안쓰는것이 안정적이다.
    public void setAutoStart(boolean autoStart) {
        _autoStart = autoStart;
    }

    public boolean isBlankPageClearHistory() {
        return _blankPageClearHistory;
    }

    public void setBlankPageClearHistory(boolean clearHistory) {
        _blankPageClearHistory = clearHistory;
    }

    public boolean isBlankPageClearCache() {
        return _blankPageClearCache;
    }

    public void setBlankPageClearCache(boolean clearCache) {
        _blankPageClearCache = clearCache;
    }

    public boolean isBlankPageClearStorage() {
        return _blankPageClearStorage;
    }

    public void setBlankPageClearStorage(boolean clearStorage) {
        _blankPageClearStorage = clearStorage;
    }

    public void setInterceptType(int interceptType) {
        if (interceptType < 0) {
            _interceptType = 0;
        } else {
            _interceptType = interceptType;
        }
    }

    public boolean isDeleteLocalStorage() {
        return _deleteLocalStorage;
    }

    public void setDeleteLocalStorage(boolean deleteLocalStorage) {
        _deleteLocalStorage = deleteLocalStorage;
    }

    public BasePatternAction getPatternAction() {
        return _patternAction;
    }

    public void setPatternAction(BasePatternAction patternAction) {
        _patternAction = patternAction;
    }

    public void setProxyInfo(String username, String password) {
        _proxyUsername = username;
        _proxyPassword = password;
    }

    public WebView getWebView() {
        return _webView;
    }

    public void startWebViewLoading() {
        synchronized (_lock) {
            _started = true;
            _hasError = false;
        }

        if (_onPageListener != null) {
            _onPageListener.onPageStarted(_webView);
        }
    }

    public void goBlankPage() {
        _webView.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "# 빈페이지 이동");
                _webView.loadUrl(BLANK_PAGE);
//                _webView.loadData("", null, null);
            }
        });

        _mutex.threadWait();
    }

    public void loadUrl(final String url) {
        startWebViewLoading();

        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl(url);

//                _webView.loadUrl(url, _httpHeaders);

            }
        });
    }

    public void reload() {
        startWebViewLoading();

        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.reload();
            }
        });
    }

    public void reloading() {
        startWebViewLoading();

        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl(_webView.getUrl());
            }
        });
    }

    public void goBack() {
        startWebViewLoading();

        _webView.post(new Runnable() {
            @Override
            public void run() {
                if (_webView.canGoBack()) {
                    _webView.goBack();
                } else {
                    Log.d(TAG, "웹뷰 뒤로가기 불가");
                    pageLoaded();
                }
            }
        });
    }

    public void stopLoading() {
        _webView.post(new Runnable() {
            @Override
            public void run() {
                // 웹뷰 버전(92.0.4515.105) 버그로 인해 실행하지 않음. 실행시 스크립트 연동코드 작동안함.
                _webView.stopLoading();
            }
        });
    }

    public String getUserAgentString() {
        if (_activity != null) {
            _activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _userAgent = _webView.getSettings().getUserAgentString();
                }
            });
        } else {
            _webView.post(new Runnable() {
                @Override
                public void run() {
                    _userAgent = _webView.getSettings().getUserAgentString();
                }
            });
        }

        return _userAgent;
    }

    public void setUserAgentString(final String ua) {
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.getSettings().setUserAgentString(ua);
            }
        });

        if (_webView instanceof AdvancedWebView) {
            AdvancedWebView webView = (AdvancedWebView) _webView;
//            webView.removeHttpHeader("Sec-Ch-Ua");
//            webView.removeHttpHeader("Sec-Ch-Ua-Mobile");
//            webView.removeHttpHeader("Sec-Ch-Ua-Platform");
//            webView.removeHttpHeader("Sec-CH-UA");
//            webView.removeHttpHeader("Sec-CH-UA-Mobile");
//            webView.removeHttpHeader("Sec-CH-UA-Platform");
            webView.removeHttpHeader("sec-ch-ua");
            webView.removeHttpHeader("sec-ch-ua-mobile");
            webView.removeHttpHeader("sec-ch-ua-platform");

            Map<String, String> chUa = HttpHeader.getSecChUa(ua);
            for (Map.Entry<String, String> entry : chUa.entrySet()) {
                webView.addHttpHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    public void setLoadsImagesAutomatically(final boolean flag) {
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.getSettings().setLoadsImagesAutomatically(flag);
            }
        });
    }

//    public boolean checkTitle(final String title) {
//        final boolean[] isEqual = {false};
//        _webView.post(new Runnable() {
//            @Override
//            public void run() {
//                isEqual[0] = _webView.getTitle().equals(title);
//            }
//        });
//
//        return isEqual[0];
//    }

    public String getCurrentUrl() {
        final ThreadMutex mutex = new ThreadMutex();
        String[] currentUrl = {""};

        _webView.post(() -> {
            currentUrl[0] = _webView.getUrl();
            SystemClock.sleep(10);
            mutex.threadWakeUp();
        });

        mutex.threadWait();

        return currentUrl[0];
    }

    public String getCookie(String url, String name) {
        String cookies = CookieManager.getInstance().getCookie(url);
        if (cookies != null) {
            String[] cookieList = cookies.split(";");
            for (String cookie : cookieList) {
                if (cookie.contains(name)) {
                    String[] cookiePair = cookie.split("=", 2);
                    return cookiePair[cookiePair.length - 1].trim();
                }
            }
        }

        return null;
    }

    public void setCookie(String url, String name, String value) {
        CookieManager manager = CookieManager.getInstance();
        String str = name.trim() + "=" + value.trim();
        manager.setCookie(url, str);
        manager.flush();
    }

    public void setCookie(String url, String name, String value, boolean secure) {
        CookieManager manager = CookieManager.getInstance();
        String str = name.trim() + "=" + value.trim();
        if (secure) {
            str += "; secure";
        }
        manager.setCookie(url, str);
        manager.flush();
    }

    public void setCookieString(String url, String string) {
        CookieManager manager = CookieManager.getInstance();
        manager.setCookie(url, string);
        manager.flush();
    }

    public void clearCookie() {
        /*TogetherWebDataManager manager = TogetherWebDataManager.getInstance();
        manager.clearAppData();
        */
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeSessionCookies(null);
        cookieManager.removeAllCookies(null);

        if (_deleteLocalStorage) {
            WebStorage.getInstance().deleteAllData();
        }
    }

    public boolean setProxy(String host, String port) {
        Log.d("Debug", "Setting proxy with >= 4.4 API.");
        Context appContext = _webView.getContext().getApplicationContext();
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port + "");
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port + "");
        try {
            Field loadedApkField = appContext.getClass().getField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(appContext);
            Class loadedApkCls = Class.forName("android.app.LoadedApk");
            Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
            receiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object rec : ((ArrayMap) receiverMap).keySet()) {
                    Class clazz = rec.getClass();
                    if (clazz.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);

                        onReceiveMethod.invoke(rec, appContext, intent);
                    }
                }
            }

            Log.d("Debug", "Setting proxy with >= 4.4 API successful!");
            return true;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetProxy() {
        if (!TextUtils.isEmpty(_proxyUsername)) {
            Log.d(TAG, "# 프록시 초기화.");
            setProxy("", "");
            _proxyUsername = null;
            _proxyPassword = null;

            return true;
        }

        return false;
    }


    private void webViewSetting() {
        _webView.setWebContentsDebuggingEnabled(true);
        _webView.getSettings().setJavaScriptEnabled(true);
        _webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        _webView.getSettings().setSupportMultipleWindows(true);
        _webView.getSettings().setLoadsImagesAutomatically(true);
        _webView.getSettings().setLoadWithOverviewMode(true);
        _webView.getSettings().setUseWideViewPort(true);
        _webView.getSettings().setBuiltInZoomControls(false);
        _webView.getSettings().setSupportZoom(true);
        //_webView.getSettings().setLoadsImagesAutomatically(false); 안먹힘
        _webView.getSettings().setDatabaseEnabled(true);
        _webView.getSettings().setDomStorageEnabled(true);
        _webView.getSettings().setSaveFormData(true);
        _webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "onPageFinished");

                if (url.equals(BLANK_PAGE)) {
                    Log.d(TAG, "# 캐시 및 내역 삭제");

                    if (_blankPageClearHistory) {
                        _webView.clearHistory();
                    }

                    if (_blankPageClearCache) {
                        _webView.clearCache(true);
                    }

                    if (_blankPageClearStorage) {
                        WebStorage.getInstance().deleteAllData();
                    }

                    _mutex.threadWakeUp();
                }

                if (_onPageFinishedListener != null) {
                    _onPageFinishedListener.onPageFinished(view, url);
                }
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                Log.d(TAG, "onLoadResource: " + url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.d(TAG, "page error(" + errorCode + ": " + description + "): " + failingUrl);

                synchronized (_lock) {
                    _hasError = true;
                }

                if (_onPageListener != null) {
                    _onPageListener.onPageLoadFailed(view, errorCode, description, failingUrl);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                int errorCode = -1;
                String description = null;
                String failingUrl = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    errorCode = error.getErrorCode();
                    description = error.getDescription().toString();
                    failingUrl = request.getUrl().toString();

                    Log.d(TAG, "page error(" + errorCode + ": " + description + "): " + failingUrl);

                    synchronized (_lock) {
                        _hasError = true;
                    }

                    if (_onPageListener != null) {
                        _onPageListener.onPageLoadFailed(view, errorCode, description, failingUrl);
                    }
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                Log.d(TAG, "onReceivedHttpError: ");
            }

//            @Nullable
//            @Override
//            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//                Log.d(TAG, "shouldInterceptRequest deprecated: " + url);
////                return new WebResourceResponse(null, null, null);
//                return super.shouldInterceptRequest(view, url);
//            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                super.onReceivedSslError(view, handler, error);
                Log.d(TAG, "onReceivedSslError: " + error.toString());
                handler.proceed();
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
//                Log.d(TAG, "shouldInterceptRequest: " + url);

//                if (_interceptType == 1) {
                    // suppress favicon requests as we don't display them anywhere
//                    if (url.endsWith("/favicon.ico")) {
//                        return new WebResourceResponse("image/png", null, null);
//                    }

                    String method = request.getMethod();
                    Map<String, String> headers = request.getRequestHeaders();
                    String ext = MimeTypeMap.getFileExtensionFromUrl(url);
                    String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);

//                Log.d("ttt", "isRedirect: " + request.isRedirect());
//                    Log.d("ttt", "[" + method + "] hasGesture: " + request.hasGesture() + " | url-mime: " + mime + " | url-ext: " + ext + " | headers: " + headers);
//                    _controller.test(view, request, url);

                // Ali 전용 예외 처리.
                if (url.startsWith("https://acs.aliexpress.com/h5/mtop.aliexpress.account.login.loginconfigs.get")) {
                    setLoadsImagesAutomatically(true);
                    Log.d(TAG, "알리 로그인창 이미지 활성화.");
                }

                boolean interceptUrl = false;

                if (_interceptType == 1) {
                    List<String> interceptDomains = new ArrayList<>();
                    interceptDomains.add("naver.com");
                    interceptDomains.add("pstatic.net");
                    // 불러오는데 에러가 있으므로 스킵.
//                    interceptDomains.add("wcs.naver.net");
                    interceptDomains.add("coupang.com");


                    if (url.contains(".ico") || url.contains("favicon")) {
                    } else if (url.contains("coupang.com") && (
                            url.contains(".css") || url.contains(".js")
                            || url.contains(".png") || url.contains(".jpg") || url.contains(".gif"))) {
                        // 쿠팡은 css,js 및 이미지 파일은 건너뛴다.
                    } else {
                        for (String domain : interceptDomains) {
                            if (url.contains(domain)) {
                                interceptUrl = true;
                                break;
                            }
                        }
                    }
                }

                if (interceptUrl) {
                    boolean isRedirect = false;
                    int redirectCount = 0;
                    InputStream stream = null;

                    Map<String, String> responseHeaders = null;
                    String contentType = null;
                    String mimeType = null;
                    String encoding = null;
                    int responseCode = 0;
                    String message = null;
                    InputStream data = null;

//                    if (false) {
                    if (method.equals("GET")) {
//                    if (method.equals("GET") || method.equals("POST")) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            getHttpClient();
                            return _controller.loadWithOkHttpClient(view, request, url, WebResourceController.MAX_RECURSIVE_COUNT);
//                        } else {
//                            return _controller.loadWithHttpURLConnection(view, request, url, WebResourceController.MAX_RECURSIVE_COUNT);
//                        }
//                    } else if (method.equals("POST")) {
//                        return _controller.loadWithHttpURLConnection(view, request, url, WebResourceController.MAX_RECURSIVE_COUNT);
                    } else if (false) {
                        getHttpClient();
                        return _controller.loadWithOkHttpClient(view, request, url, WebResourceController.MAX_RECURSIVE_COUNT);
                    }

                    return null;
                } else {
                    return super.shouldInterceptRequest(view, request);
                }
            }


//            @Nullable
//            @Override
//            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//                String method = request.getMethod();
//                String url = request.getUrl().toString();
//                Map<String, String> headers = request.getRequestHeaders();
////                Log.d("ttt", "isRedirect: " + request.isRedirect());
//                Log.d("ttt", "hasGesture: " + request.hasGesture());
//                Log.d("ttt", "[" + request.getMethod() + "] headers: " + headers);
//

//
//
////                try {
////                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
////                    conn.setRequestMethod(method);
////                    String ua = null;
////
////                    for (Map.Entry<String, String> entry : headers.entrySet()) {
////                        if (entry.getKey().equals("User-Agent")) {
////                            ua = entry.getValue();
////                        } else if (entry.getKey().equals("x-requested-with")) {
////                            continue;
////                        }
////
////                        conn.setRequestProperty(entry.getKey(), entry.getValue());
////                    }
////
////                    Map<String, String> chUa = getSecChUa(ua);
////                    for (Map.Entry<String, String> entry : chUa.entrySet()) {
////                        conn.setRequestProperty(entry.getKey(), entry.getValue());
////                    }
////
////                    Log.d(TAG, "HttpURLConnection header: " + conn.getRequestProperties());
////
//////                    conn.setRequestProperty("Sample-Header", "hello");
////                    conn.setDoInput(true);
////                    conn.setUseCaches(false);
////
////                    Log.d("ttt", "[" + request.getMethod() + "] headers: " + conn.getHeaderFields());
////                    Map<String, String> responseHeaders = convertResponseHeaders(conn.getHeaderFields());
////
//////                    if (true)
//////                        return new WebResourceResponse(mime, conn.getContentEncoding(), conn.getInputStream());
////
//////                    responseHeaders.put("Sample-Header2", "hello");
////
////                    return new WebResourceResponse(
////                            mime,
////                            conn.getContentEncoding(),
////                            conn.getResponseCode(),
////                            conn.getResponseMessage(),
////                            responseHeaders,
////                            conn.getInputStream()
////                    );
////
////                } catch (Exception e) {
////                    Log.e(TAG, "shouldInterceptRequest error: " + e);
////                }
//
//                return null;
//
////
//////                return new WebResourceResponse(
//////                        null,
//////                        response.header("content-encoding", "utf-8"),
//////                        response.body().byteStream()
//////                );
////
////
////                Log.d(TAG, "shouldInterceptRequest: " + request.getUrl().toString());
////                return super.shouldInterceptRequest(view, request);
//            }

//            @Nullable
//            @Override
//            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
//                Map<String, String> headers = request.getRequestHeaders();
//                Log.d(TAG, "shouldInterceptRequest: headers");
//                Log.d(TAG, headers.toString());
////                return new WebResourceResponse("", "", null);
//                request.getRequestHeaders().put("x-requested-with", "14234");
//                request.getRequestHeaders().put("ClientId", "ANDROID");
//                request.getRequestHeaders().put("x-requested-with", "14234");
//                headers = request.getRequestHeaders();
//                Log.d(TAG, headers.toString());
//
//                return super.shouldInterceptRequest(view, request);
//
////                // Check for "recursive request" (are yor header set?)
////                if (request.getRequestHeaders().containsKey("Your Header"))
////                    return null;
////
////                // Add here your headers (could be good to import original request header here!!!)
////                Map<String, String> customHeaders = new HashMap<String, String>();
////                customHeaders.put("Your Header","Your Header Value");
////                view.loadUrl(url, customHeaders);
////
////                return new WebResourceResponse("", "", null);
////
////                //---
////                try {
////                    OkHttpClient httpClient = new OkHttpClient();
////
////                    Request request = new Request.Builder()
////                            .url(url.trim())
////                            .addHeader("Authorization", "YOU_AUTH_KEY") // Example header
////                            .addHeader("api-key", "YOUR_API_KEY") // Example header
////                            .build();
////
////                    Response response = httpClient.newCall(request).execute();
////
////                    return new WebResourceResponse(
////                            null,
////                            response.header("content-encoding", "utf-8"),
////                            response.body().byteStream()
////                    );
////
////                } catch (Exception e) {
////                    return null;
////                }
////
////
////                //---
////                try {
////                    OkHttpClient httpClient = new OkHttpClient();
////                    com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
////                            .url(url.trim())
////                            .addHeader("<your-custom-header-name>", "<your-custom-header-value>")
////                            .build();
////                    com.squareup.okhttp.Response response = httpClient.newCall(request).execute();
////
////                    return new WebResourceResponse(
////                            response.header("content-type", response.body().contentType().type()), // You can set something other as default content-type
////                            response.header("content-encoding", "utf-8"),  // Again, you can set another encoding as default
////                            response.body().byteStream()
////                    );
////                } catch (ClientProtocolException e) {
////                    //return null to tell WebView we failed to fetch it WebView should try again.
////                    return null;
////                } catch (IOException e) {
////                    //return null to tell WebView we failed to fetch it WebView should try again.
////                    return null;
////                }
//
////                return super.shouldInterceptRequest(view, request);
//            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("tel:")) {
                    return true;
                } else if (url.startsWith("coupang:")) {
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, request);
//                return shouldOverrideUrlLoadingProcess(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    return true;
                } else if (url.startsWith("coupang:")) {
                    return true;
                } else if (url.startsWith("aliexpress:")) {
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
//                return shouldOverrideUrlLoadingProcess(view, url);
            }

            private boolean shouldOverrideUrlLoadingProcess(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent call_phone = new Intent(Intent.ACTION_DIAL);
                    call_phone.setData(Uri.parse(url));
                    _webView.getContext().startActivity(call_phone);
                } else {
                    view.loadUrl(url);
                }

                return true;
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                Log.d(TAG, "onReceivedHttpAuthRequest: " + host + " / " + realm);

                if (!TextUtils.isEmpty(_proxyUsername) && !TextUtils.isEmpty(_proxyPassword)) {
                    handler.proceed(_proxyUsername, _proxyPassword);
                } else {
                    super.onReceivedHttpAuthRequest(view, handler, host, realm);
                }
            }
        });

        _webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if (_onProgressChangedListener != null) {
                    _onProgressChangedListener.onProgressChanged(view, newProgress);
                }

                if (newProgress <= 10) {
                    if (_autoStart) {
                        synchronized (_lock) {
                            if (!_started) {
                                _started = true;
                            }
                        }
                    }
                } else if (newProgress >= 100) {
                    pageLoaded();
                }
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                Log.d(TAG, "onShowCustomView: ");
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.d(TAG, "onJsAlert: " + message + ", url: " + url);
                SystemClock.sleep(1000);
                result.cancel();
                return true;
//                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                Log.d(TAG, "onJsConfirm: " + message + ", url: " + url);
                SystemClock.sleep(1000);
                result.cancel();
                return true;
//                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                Log.d(TAG, "onJsPrompt: " + message + ", url: " + url);
                SystemClock.sleep(1000);
                result.cancel();
                return true;
//                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
        });
    }

    private void pageLoaded() {
        Log.d(TAG, "pageLoaded");

        synchronized (_lock) {
            if (_started && !_hasError) {
                if (_onPageListener != null) {
                    _onPageListener.onPageLoaded(_webView, _webView.getUrl());
                }

                _started = false;
            }
        }
    }

    public String changeQueryValue(String url, String key, String value) {
        String[] parts = url.split("\\?", 2);

        if (parts.length < 2) {
            return url;
        }

        String query = parts[1];

        if (TextUtils.isEmpty(query)) {
            return url;
        }

        String[] pairs = query.split("&");
        StringBuilder newQuery = new StringBuilder();
        int index = 0;

        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String queryKey = pair.substring(0, idx);
            String queryValue = pair.substring(idx + 1);

            if (queryKey.equals(key)) {
                queryValue = value;
            }

            if (index > 0) {
                newQuery.append("&");
            }

            newQuery.append(queryKey);
            newQuery.append("=");
            newQuery.append(queryValue);

            ++index;
        }

        return parts[0] + "?" + newQuery.toString();
    }

    public static String keywordEncodeForNaver(String keyword) {
        String encoded = keyword;
        Log.d(TAG, "keyword: " + keyword);

        try {
            encoded = URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encoded;

//        String[] parts = keyword.split(" ");
//        StringBuilder newQuery = new StringBuilder();
//        int index = 0;
//
//        for (String part : parts) {
//            if (index > 0) {
//                newQuery.append("+");
//            }
//
//            if (part.isEmpty()) {
//                continue;
//            }
//
//            try {
//                String val = URLEncoder.encode(part, "UTF-8");
//                newQuery.append(val);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//                return null;
//            }
//
//            ++index;
//        }
//
//        return newQuery.toString();
    }

    public static String keywordEncodeForNaverInclPlus(String keyword) {
        String encoded = keywordEncodeForNaver(keyword);
        encoded = encoded.replace("+", "%20");

        return encoded;
    }

//    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
//        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
//        String query = url.getQuery();
//        String[] pairs = query.split("&");
//        for (String pair : pairs) {
//            int idx = pair.indexOf("=");
//            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
//        }
//        return query_pairs;
//    }
//
//    public static Map<String, List<String>> splitQuery(URL url) throws UnsupportedEncodingException {
//        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
//        final String[] pairs = url.getQuery().split("&");
//        for (String pair : pairs) {
//            final int idx = pair.indexOf("=");
//            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
//            if (!query_pairs.containsKey(key)) {
//                query_pairs.put(key, new LinkedList<String>());
//            }
//            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
//            query_pairs.get(key).add(value);
//        }
//        return query_pairs;
//    }
//
//    Map<String, String> getQueryKeyValueMap(Uri uri){
//        HashMap<String, String> keyValueMap = new HashMap();
//        String key;
//        String value;
//
//        Set<String> keyNamesList = uri.getQueryParameterNames();
//        Iterator iterator = keyNamesList.iterator();
//
//        while (iterator.hasNext()){
//            key = (String) iterator.next();
//            value = uri.getQueryParameter(key);
//            keyValueMap.put(key, value);
//        }
//        return keyValueMap;
//    }

    public interface OnPageListener {
        void onPageStarted(WebView view);
        void onPageLoaded(WebView view, String url);
        void onPageLoadFailed(WebView view, int errorCode, String description, String failingUrl);
    }

    public interface OnPageFinishedListener {
        void onPageFinished(WebView view, String url);
    }

    public interface OnProgressChangedListener {
        void onProgressChanged(WebView view, int newProgress);
    }
}
