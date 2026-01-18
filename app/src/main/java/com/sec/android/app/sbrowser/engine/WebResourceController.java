package com.sec.android.app.sbrowser.engine;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.BuildConfig;
import com.sec.android.app.sbrowser.engine.WebEngine.Headers.EdgeClientHints;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpEngine;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpHeader;

import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;
import org.brotli.dec.BrotliInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.BufferedSource;
import okio.Okio;

public class WebResourceController {

    private static final String TAG = WebResourceController.class.getSimpleName();
    private static final String AJAX_ACTION = "AJAXINTERCEPT";
    private static final String BEACON_ACTION = "BCINTERCEPT";

    public static final int MAX_RECURSIVE_COUNT = 5;

    private WebViewManager _webViewManager = null;
    private Map<String, WebResourceResponse> _resourceResponseMap = new HashMap<>();
    private boolean _firstPage = true;

    private OkHttpClient _httpClient = null;

    private String _currentUrl = null;
    private String _originalUrl = null;

    private boolean _showLog = false;

    public WebResourceController(WebViewManager manager) {
        _webViewManager = manager;

        if (BuildConfig.FLAVOR_build.contains("beta")) {
            _showLog = true;
        }
    }

    public void setHttpClient(OkHttpClient client) {
        _httpClient = client;
    }

    private boolean isAjaxRequest(String url) {
        return url.contains(AJAX_ACTION);
    }

    private boolean isBeaconRequest(String url) {
        return url.contains(BEACON_ACTION);
    }

    private String getAjaxRequestBody(WebResourceRequest request) {
        String requestID = getUrlSegments(request, AJAX_ACTION)[1];
        return _webViewManager.getPatternAction().getAjaxRequestBodyByID(requestID);
    }

    private String getBeaconRequestBody(WebResourceRequest request) {
        String requestID = getUrlSegments(request, BEACON_ACTION)[1];
        return _webViewManager.getPatternAction().getAjaxRequestBodyByID(requestID);
    }

    private String[] getUrlSegments(WebResourceRequest request, String divider) {
        String urlString = request.getUrl().toString();
        return urlString.split(divider);
    }

    private String getOriginalRequestUrl(WebResourceRequest request, String marker) {
        return getUrlSegments(request, marker)[0];
    }

    public String getCustomAjaxScript(String baseInterfaceName) {
        String query = "<script language=\"JavaScript\">" +
                "function generateRandom() {" +
                "      return Math.floor((1 + Math.random()) * 0x10000)" +
                "        .toString(16)" +
                "        .substring(1);" +
                "    }" +
                "    requestID = null;" +
                "    XMLHttpRequest.prototype.reallyOpen = XMLHttpRequest.prototype.open;" +
                "    XMLHttpRequest.prototype.open = function(method, url, async, user, password) {" +
                "        requestID = generateRandom()\n" +
                "        var signed_url = url + \"" + AJAX_ACTION + "\" + requestID;\n" +
                "        this.reallyOpen(method, signed_url , async, user, password);\n" +
                "    };\n" +
                "    XMLHttpRequest.prototype.reallySend = XMLHttpRequest.prototype.send;\n" +
                "    XMLHttpRequest.prototype.send = function(body) {\n" +
                "        " + baseInterfaceName + ".fixedAj(requestID, body);\n" +
                "        this.reallySend(body);\n" +
                "    };\n" +
                "</script>";
        return query;
    }

    public String getCustomBeaconScript(String baseInterfaceName) {
        String query = "<script language=\"JavaScript\">" +
                " function generateRandom() {" +
                "  return Math.floor((1 + Math.random()) * 0x10000)" +
                "   .toString(16)" +
                "   .substring(1);" +
                " }" +
                " requestID = null;" +
                " navigator.reallySendBeacon = navigator.sendBeacon;" +
                " navigator.sendBeacon = function(url, data) {" +
//                "  console.log(data);" +
//                "  console.log(typeof data);" +
                "  if (typeof data === 'string') {" +
                "   requestID = generateRandom();" +
                "   var signed_url = url + \"" + BEACON_ACTION + "\" + requestID;" +
                "   " + baseInterfaceName + ".fixedAj(requestID, data);" +
                "   this.reallySendBeacon(signed_url, data);" +
                "  } else {" +
                "   this.reallySendBeacon(url, data);" +
                "  }" +
                " };" +
                "</script>";
        return query;
    }


    private void detect(WebView webView, WebResourceRequest request, String[] urls) {
        Map<String, String> headers = request.getRequestHeaders();
        String acceptValue = HttpHeader.getValueFromMap(headers, "Accept");

        if (!TextUtils.isEmpty(acceptValue) && !TextUtils.isEmpty(_currentUrl)) {
            if (!acceptValue.startsWith("text/html")) {
                urls[0] = _currentUrl;
                urls[1] = _originalUrl;
                return;
            }
        }

        final ThreadMutex mutex = new ThreadMutex();
//        _mutex.timeout = 500;
//        String[] currentUrl = {""};
//        final String[] originalUrl = {""};  // 이동전 url
        Log.e("ttt detect", "detect");

        webView.post(() -> {
            _currentUrl = webView.getUrl();
            _originalUrl = webView.getOriginalUrl();

            if (!TextUtils.isEmpty(_currentUrl) && _currentUrl.startsWith("http")) {
                urls[0] = webView.getUrl();
            }

            if (!TextUtils.isEmpty(_originalUrl) && _originalUrl.startsWith("http")) {
                urls[1] = webView.getOriginalUrl();
            }

            Log.e("ttt detect", "URL: [" + webView.getUrl().equals(webView.getOriginalUrl()) + "] " + webView.getUrl() + " / " + webView.getOriginalUrl());
            SystemClock.sleep(10);
            mutex.threadWakeUp();
        });

        mutex.threadWait();
    }

    public void test(WebView webView, WebResourceRequest request, String url) {
        final String[] urls = {"", ""};
        detect(webView, request, urls);
        Log.e("ttt", "URL?: " + urls[0] + " / " + urls[1]);
    }

    public WebResourceResponse loadWithHttpURLConnection(final WebView webView, WebResourceRequest request, String url, int recursiveCount) {
        if (recursiveCount <= 0) {
            Log.e(TAG, "recursive Stop: " + recursiveCount);
            return null;
        }

        final String[] urls = {"", ""};
        detect(webView, request, urls);
        String currentUrl = urls[0];
        String originalUrl = urls[1];
        Log.e("ttt", "URL?: " + currentUrl + " / " + originalUrl);

        if (_resourceResponseMap.containsKey(url)) {
            Log.d(TAG, "이전에 로드한 정보가 있으므로 스킵: " + url);
            WebResourceResponse resourceResponse = _resourceResponseMap.get(url);
            _resourceResponseMap.remove(url);
            return resourceResponse;
        }

        String method = request.getMethod();

        HttpURLConnection conn = null;
        boolean isBaseUrl = url.equals(currentUrl);
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

        try {
            // loop version
            do {
                byte[] postDataBytes = null;
//                if (isAjaxRequest(url)) {
//                    requestBody = getRequestBody(request);
//                    url = getOriginalRequestUri(request, AJAX_ACTION);
//                }

                if ("POST".equalsIgnoreCase(method)) {
                    if (isBeaconRequest(url)) {
                        String requestBody = getBeaconRequestBody(request);
                        if (!TextUtils.isEmpty(requestBody)) {
                            requestBody = requestBody.replace("Linux%20armv7l", "Linux%20armv8l");
                            Log.w("ttt", "[Replaced Body] " + requestBody);
                            postDataBytes = requestBody.getBytes("UTF-8");
                        }
                        url = getOriginalRequestUrl(request, BEACON_ACTION);
                    } else {
                        Log.w("ttt", "unknown post!");
                        return null;
//                        throw new Exception("unknown post!");
                    }
//                } else {
////                    String qq = urlBase.getQuery();
////                    String q2 = qq.replace("Linux%20armv7l", "Linux%20armv8l");
//                    url = url.replace("Linux%20armv7l", "Linux%20armv8l");
                }

                if (url.startsWith("https://lcs.naver.com/m?")) {
                    Map<String, String> queries = HttpEngine.query2MapFromUrlString(url);
                    if (TextUtils.isEmpty(queries.get("e"))) {
                        queries.put("e", URLEncoder.encode("https://m.naver.com/", "UTF-8"));

                        StringBuilder newUrl = new StringBuilder();
                        newUrl.append(HttpEngine.removeQueryFromUrlString(url));

                        int index = 0;

                        for (Map.Entry<String, String> query : queries.entrySet()) {
                            if (index == 0) {
                                newUrl.append("?");
                            } else {
                                newUrl.append("&");
                            }

                            newUrl.append(query.getKey());

                            if (!query.getKey().equals("EOU")) {
                                newUrl.append("=").append(query.getValue());
                            }

                            ++index;
                        }

                        Log.w("ttt", "[Url Change] " + url + " -> " + newUrl);
                        url = newUrl.toString();
                    }
                }

                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod(method);
                conn.setConnectTimeout(100);
                conn.setReadTimeout(5000);

                addHttpHeaders(currentUrl, originalUrl, request, conn);

                if (postDataBytes != null && postDataBytes.length > 0) {
                    conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                    Log.i("ttt", "[" + url + "] complete request header: " + conn.getRequestProperties());
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.getOutputStream().write(postDataBytes);
                } else {
                    Log.i("ttt", "[" + url + "] complete request header: " + conn.getRequestProperties());
                    conn.setDoOutput(false);
                    conn.setDoInput(true);
                }
//                                conn.setUseCaches(false);

                final long startMillis = System.currentTimeMillis();

                Log.v("ttt Response", "HttpURLConnection [" + url + "] headers: " + conn.getHeaderFields());
                final long dtMillis = System.currentTimeMillis() - startMillis;
                Log.d(TAG, "Got response: after " + dtMillis + "ms");

                isRedirect = false;
                responseHeaders = convertResponseHeaders(url, conn.getHeaderFields());
                contentType = conn.getContentType();
                mimeType = getMimeTypeFromContentType(contentType);
                encoding = conn.getContentEncoding();
                responseCode = conn.getResponseCode();
                message = conn.getResponseMessage();
                stream = conn.getInputStream();

                if ("gzip".equalsIgnoreCase(encoding)) {
                    Log.d("ttt Response", "gzip 파서!");
                    GZIPInputStream gzipInputStream = new GZIPInputStream(stream);
                    //GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(stream);
                    data = cloneInputStream(gzipInputStream);
                    gzipInputStream.close();
                    encoding = null;
                } else if ("deflate".equalsIgnoreCase(encoding)) {
                    Log.d("ttt Response", "deflate 파서!");
                    DeflaterInputStream deflaterInputStream = new DeflaterInputStream(stream);
                    //DeflateCompressorInputStream deflaterInputStream = new DeflateCompressorInputStream(stream);
                    data = cloneInputStream(deflaterInputStream);
                    deflaterInputStream.close();
                    encoding = null;
                } else if ("br".equalsIgnoreCase(encoding)) {
                    Log.d("ttt Response", "brotli 파서!");
//                    BrotliInputStream brotliInputStream = new BrotliInputStream(stream);
                    BrotliCompressorInputStream brotliInputStream = new BrotliCompressorInputStream(stream);
                    data = cloneInputStream(brotliInputStream);
                    brotliInputStream.close();
                    encoding = null;
                } else {
                    Log.d("ttt Response", "일반 파서!");
                    data = cloneInputStream(stream);
                }

                stream.close();

                // 쿠키 처리.
                List<String> cookiesHeader = conn.getHeaderFields().get("Set-Cookie");

                if (cookiesHeader != null) {
                    CookieManager cookieManager = CookieManager.getInstance();

                    for (String cookieString : cookiesHeader) {
                        List<HttpCookie> cookieList = HttpCookie.parse(cookieString);

                        for (HttpCookie cookie : cookieList) {
                            Log.d(TAG, "cookie value: " + cookie.getValue() + " / " + cookie.toString() + " / " + cookie.getDomain() + " / " + cookieString);

                            if (!TextUtils.isEmpty(cookie.getDomain())) {
                                cookieManager.setCookie(cookie.getDomain(), cookieString);
                            }
                        }
                    }
                }

                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                        responseCode == 307) {
                    URL baseUrl = conn.getURL();
                    String location = conn.getHeaderField("Location");
                    URL target = (location == null) ? null : new URL(baseUrl, location);
                    conn.disconnect();

                    if (target == null || !(target.getProtocol().equals("http") || target.getProtocol().equals("https")) || redirectCount >= 10) {
                        throw new SecurityException("illegal URL redirect");
                    }

                    url = target.toString();
//                            final String finalUrl = target.toString();
                    isRedirect = true;
                    recursiveCount = MAX_RECURSIVE_COUNT;
                    redirectCount++;

                    Log.e("ttt Redirect", "[" + responseCode + " " + message + "] Redirect: " + url);

//                            final Map<String, String> additionalHttpHeaders = new HashMap<>();
//                            additionalHttpHeaders.put("Z-Zero-Redirect-Code", "" + responseCode);
//                            _webView.post(() -> {
//                                _webView.loadUrl(finalUrl, additionalHttpHeaders);
////                                            _userAgent = _webView.getSettings().getUserAgentString();
//                            });
//                            return new WebResourceResponse(
//                                    mimeType,
//                                    encoding,
//                                    100,
//                                    message,
//                                    responseHeaders,
//                                    data
//                            );
//                            return new WebResourceResponse(mimeType, encoding, data);
//
//                            Field f = WebView.class.getDeclaredField("mStatusCode");
//                            f.setAccessible(true);
//                            f.setInt(view, 302);
//                            return redirectResponse;
                }
            } while (isRedirect);

            Log.d("ttt Response", conn.toString());
            conn.disconnect();

            if (TextUtils.isEmpty(message)) {
                switch (responseCode) {
                    case 200:
                        message = "OK";
                        break;

                    default:
                        message = "OK";
                        break;
                }
            }

            String acceptValue = HttpHeader.getValueFromMap(request.getRequestHeaders(), "Accept");

//            if ((_webViewManager != null) && !TextUtils.isEmpty(acceptValue) && acceptValue.startsWith("text/html")) {
//                org.jsoup.nodes.Document doc = Jsoup.parse(new String(consumeInputStream(data)));
////                doc.outputSettings().prettyPrint(true);
//
//                // Prefix every script to capture submits
//                // Make sure our interception is the first element in the
//                // header
//                org.jsoup.select.Elements element = doc.getElementsByTag("head");
//                if (element.size() > 0) {
//                    String baseInterfaceName = _webViewManager.getPatternAction().getJsApi().getInterfaceName();
////                    element.get(0).prepend(getCustomAjaxScript(baseInterfaceName));
//                    element.get(0).prepend(getCustomBeaconScript(baseInterfaceName));
//                }
//
//                String pageContents = doc.toString();
//                data.close();
//                ByteArrayInputStream inputStream = new ByteArrayInputStream(pageContents.getBytes());
//                data = inputStream;
//                Log.e(TAG, "Added custom ajax!!");
//            }

            WebResourceResponse resourceResponse = new WebResourceResponse(
                    null,
                    encoding,
                    responseCode,
                    message,
                    responseHeaders,
                    data
            );

//            boolean workReload = false;
//
//            if (!TextUtils.isEmpty(acceptValue) && acceptValue.startsWith("text/html")) {
//                workReload = true;
//            }

            if (redirectCount > 0 && isBaseUrl) {
                Log.e("ttt Redirect", "Reload [" + responseCode + " " + message + "] Redirect: " + url);
                _resourceResponseMap.put(url, resourceResponse);

                final String finalUrl = url;
                final Map<String, String> additionalHttpHeaders = new HashMap<>();
                additionalHttpHeaders.put("Z-Zero-Redirect-Code", "" + responseCode);
                webView.post(() -> {
                    webView.loadUrl(finalUrl, additionalHttpHeaders);
//                    _userAgent = _webView.getSettings().getUserAgentString();
                });

                return new WebResourceResponse(
                        mimeType,
                        encoding,
                        100,
                        message,
                        responseHeaders,
                        new ByteArrayInputStream("".getBytes())
                );
            }

            Log.d("ttt Response", "type: " + contentType + " | mimeT: " + mimeType + " | encoding: " + encoding + " | code: " + responseCode + " | message: " + message);

//                            if ("application/javascript".equals(mimeType)) {
////                            if ("text/css".equals(mimeType) || "application/javascript".equals(mimeType)) {
//                                conn.disconnect();
//                                return null;
//                            }

            _firstPage = false;

            return resourceResponse;
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "SocketTimeoutException Retry(" + recursiveCount + "): " + e);
            e.printStackTrace();
            SystemClock.sleep(500);
            return loadWithHttpURLConnection(webView, request, url, recursiveCount - 1);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException Retry(" + recursiveCount + "): " + e);
            e.printStackTrace();
            SystemClock.sleep(500);
            return loadWithHttpURLConnection(webView, request, url, recursiveCount - 1);

//            return new WebResourceResponse(
//                    mimeType,
//                    encoding,
//                    100,
//                    message,
//                    responseHeaders,
//                    new ByteArrayInputStream("".getBytes())
//            );
        } catch (Exception e) {
            Log.e(TAG, "shouldInterceptRequest error: " + e);
            e.printStackTrace();
        } finally {
            Log.i(TAG, "finally!!?");

            if (conn != null) {
                Log.i(TAG, "연결해제!!");
                conn.disconnect();
            }
        }

        return null;
    }

    public WebResourceResponse loadWithOkHttpClient(final WebView webView, WebResourceRequest request, String url, int recursiveCount) {
        if (recursiveCount <= 0) {
            Log.e(TAG, "recursive Stop: " + recursiveCount);
            return null;
        }

        final String[] urls = {"", ""};
        detect(webView, request, urls);
        String currentUrl = urls[0];
        String originalUrl = urls[1];
        Log.e("ttt Ok", "URL?: " + currentUrl + " / " + originalUrl);

        if (_resourceResponseMap.containsKey(url)) {
            Log.d(TAG, "이전에 로드한 정보가 있으므로 스킵: " + url);
            WebResourceResponse resourceResponse = _resourceResponseMap.get(url);
            _resourceResponseMap.remove(url);
            return resourceResponse;
        }

        String method = request.getMethod();

        boolean isBaseUrl = url.equals(currentUrl);
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

        try {
            do {
                if (url.startsWith("https://lcs.naver.com/m?")) {
                    Map<String, String> queries = HttpEngine.query2MapFromUrlString(url);
                    if (TextUtils.isEmpty(queries.get("e"))) {
                        queries.put("e", URLEncoder.encode("https://m.naver.com/", "UTF-8"));

                        StringBuilder newUrl = new StringBuilder();
                        newUrl.append(HttpEngine.removeQueryFromUrlString(url));

                        int index = 0;

                        for (Map.Entry<String, String> query : queries.entrySet()) {
                            if (index == 0) {
                                newUrl.append("?");
                            } else {
                                newUrl.append("&");
                            }

                            newUrl.append(query.getKey());

                            if (!query.getKey().equals("EOU")) {
                                newUrl.append("=").append(query.getValue());
                            }

                            ++index;
                        }

                        Log.w("ttt", "[Url Change] " + url + " -> " + newUrl);
                        url = newUrl.toString();
                    }
                }

                Request.Builder builder = new Request.Builder()
                        .url(url.trim());

                if (method.equals("POST")) {
//                            builder.get();
                } else {
                    builder.get();
                }

//                Request httpRequest = new Request.Builder()
//                        .url(url.trim())
////                        .addHeader("<your-custom-header-name>", "<your-custom-header-value>")
//                        .build();
//
//                        String ua = null;

                addOkHttpHeaders(currentUrl, originalUrl, request, new URL(url), builder);

//                        for (Map.Entry<String, String> entry : headers.entrySet()) {
//                            if (entry.getKey().equalsIgnoreCase("user-agent")) {
//                                ua = entry.getValue();
//                            } else if (entry.getKey().equalsIgnoreCase("x-requested-with")) {
//                                continue;
//                            }
//
//                            builder.addHeader(entry.getKey(), entry.getValue());
//                        }
//
//                        Map<String, String> chUa = getSecChUa(ua);
//                        for (Map.Entry<String, String> entry : chUa.entrySet()) {
//                            builder.header(entry.getKey(), entry.getValue());
////                        builder.addHeader(entry.getKey(), entry.getValue());
//                        }
//
//                        builder.addHeader("Grace-Tan", "11");
//                        builder.addHeader("Grace-tan2", "11");
//                        builder.addHeader("grace-tan3", "11");
//
//                    builder.header("accept-encoding", "gzip, deflate, br");
//                    builder.header("accept-language", "ko-KR,ko;q=0.9");
//                    builder.header("cache-control", "max-age=0");
//                    builder.header("sec-fetch-dest", "document");
//                    builder.header("sec-fetch-mode", "navigate");
//                    builder.header("sec-fetch-site", "none");
//                    builder.header("sec-fetch-user", "?1");

                Request httpRequest = builder.build();

                if (_showLog) {
                    Log.i("ttt", "[" + url + "] complete request header: " + HttpEngine.getHeadersLog(httpRequest.headers()));
                }

                final long startMillis = System.currentTimeMillis();

                Response response = null;
                response = _httpClient.newCall(httpRequest).execute();
                final long dtMillis = System.currentTimeMillis() - startMillis;
                Log.d(TAG, "Got response: after " + dtMillis + "ms");

//                        if (response.isSuccessful())

//                        return new WebResourceResponse(
//                                mime,
////                            response.header("content-type", response.body().contentType().type()), // You can set something other as default content-type
//                                response.header("content-encoding", "utf-8"),  // Again, you can set another encoding as default
//                                response.body().byteStream()
//                        );

                isRedirect = false;
                responseHeaders = convertResponseHeaders(url, response.headers().toMultimap());

                Log.w("ttt Response", response.toString());
                if (_showLog) {
                    Log.d("ttt Response", "OkHttpClient [" + url + "] headers: " + responseHeaders);
                }

                contentType = response.header("content-type");
                mimeType = getMimeTypeFromContentType(contentType);
                encoding = response.header("Content-Encoding", null);
                responseCode = response.code();
                message = response.message();
                stream = response.body().byteStream();

                if ("gzip".equalsIgnoreCase(encoding)) {
                    Log.d("ttt Response", "gzip 파서!");
                    GZIPInputStream gzipInputStream = new GZIPInputStream(stream);
                    data = cloneInputStream(gzipInputStream);
                    gzipInputStream.close();
                    encoding = null;
                } else if ("deflate".equalsIgnoreCase(encoding)) {
                    Log.d("ttt Response", "deflate 파서!");
                    DeflaterInputStream deflaterInputStream = new DeflaterInputStream(stream);
                    //DeflateCompressorInputStream deflaterInputStream = new DeflateCompressorInputStream(stream);
                    data = cloneInputStream(deflaterInputStream);
                    deflaterInputStream.close();
                    encoding = null;
                } else if ("br".equalsIgnoreCase(encoding)) {
                    Log.d("ttt Response", "brotli 파서!");
                    BrotliCompressorInputStream compressorInputStream = new BrotliCompressorInputStream(stream);
                    data = cloneInputStream(compressorInputStream);
                    compressorInputStream.close();
                    encoding = null;
                } else {
                    Log.d("ttt Response", "일반 파서!");
                    data = cloneInputStream(stream);
                }

                stream.close();

                // 쿠키 처리.
                List<String> cookiesHeader = response.headers().toMultimap().get("Set-Cookie");

                if (cookiesHeader != null) {
                    CookieManager cookieManager = CookieManager.getInstance();

                    for (String cookieString : cookiesHeader) {
                        List<HttpCookie> cookieList = HttpCookie.parse(cookieString);

                        for (HttpCookie cookie : cookieList) {
                            Log.d(TAG, "cookie value: " + cookie.getValue() + " / " + cookie.toString() + " / " + cookie.getDomain() + " / " + cookieString);

                            if (!TextUtils.isEmpty(cookie.getDomain())) {
                                cookieManager.setCookie(cookie.getDomain(), cookieString);
                            }
                        }
                    }
                }

                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                        responseCode == 307) {
                    URL baseUrl = new URL(url);
                    String location = response.header("Location");
                    URL target = (location == null) ? null : new URL(baseUrl, location);
                    response.close();

                    if (target == null || !(target.getProtocol().equals("http") || target.getProtocol().equals("https")) || redirectCount >= 10) {
                        throw new SecurityException("illegal URL redirect");
                    }

                    url = target.toString();
//                            final String finalUrl = target.toString();
                    isRedirect = true;
                    recursiveCount = MAX_RECURSIVE_COUNT;
                    redirectCount++;

                    Log.e("ttt Redirect", "[" + responseCode + " " + message + "] Redirect: " + url);
                }

            } while (isRedirect);

            if (TextUtils.isEmpty(message)) {
                switch (responseCode) {
                    case 200:
                        message = "OK";
                        break;

                    default:
                        message = "Em";
                        break;
                }
            }

            WebResourceResponse resourceResponse = new WebResourceResponse(
                    null,
                    encoding,
                    responseCode,
                    message,
                    responseHeaders,
                    data
            );

//            String acceptValue = HttpHeader.getValueFromMap(request.getRequestHeaders(), "Accept");
//            boolean workReload = false;
//
//            if (!TextUtils.isEmpty(acceptValue) && acceptValue.startsWith("text/html")) {
//                workReload = true;
//            }

            if (redirectCount > 0 && isBaseUrl) {
                // 여기는 정상적인 상황일때만 와야하는데 에러가 났을때 여기 들어오는지 확인해야한다.
                Log.e("ttt Redirect", "Reload [" + responseCode + " " + message + "] Redirect: " + url);
                _resourceResponseMap.put(url, resourceResponse);

                final String finalUrl = url;
                final Map<String, String> additionalHttpHeaders = new HashMap<>();
                additionalHttpHeaders.put("Z-Zero-Redirect-Code", "" + responseCode);
                webView.post(() -> {
                    webView.loadUrl(finalUrl, additionalHttpHeaders);
//                    _userAgent = _webView.getSettings().getUserAgentString();
                });

                return new WebResourceResponse(
                        mimeType,
                        encoding,
                        100,
                        message,
                        responseHeaders,
                        new ByteArrayInputStream("".getBytes())
                );
            }

            Log.d("ttt Response", "type: " + contentType + " | mimeT: " + mimeType + " | encoding: " + encoding + " | code: " + responseCode + " | message: " + message);

//                            if ("application/javascript".equals(mimeType)) {
////                            if ("text/css".equals(mimeType) || "application/javascript".equals(mimeType)) {
//                                conn.disconnect();
//                                return null;
//                            }

            _firstPage = false;

            return resourceResponse;
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "SocketTimeoutException Retry(" + recursiveCount + "): " + e);
            e.printStackTrace();
            SystemClock.sleep(500);
            return loadWithOkHttpClient(webView, request, url, recursiveCount - 1);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException Retry(" + recursiveCount + "): " + e);
            e.printStackTrace();
            SystemClock.sleep(500);
            return loadWithOkHttpClient(webView, request, url, recursiveCount - 1);
        } catch (Exception e) {
            Log.e(TAG, "shouldInterceptRequest error: " + e);
            e.printStackTrace();
        }

        return null;
    }

    private Map<String, String> convertResponseHeaders(String url, Map<String, List<String>> headers) {
        Map<String, String> responseHeaders = new HashMap<>();
        boolean lowerCase = url.startsWith("https");

        for (Map.Entry<String, List<String>> item : headers.entrySet()) {
            if (!TextUtils.isEmpty(item.getKey()) && item.getKey().startsWith("X-Android-")) {
                continue;
            }

//            for (String headerVal : item.getValue()) {
//                Log.e(TAG, "processRequest: " + item.getKey() + " : " + headerVal);
//            }

//                    String.join(",", values);
            String value = TextUtils.join(",", item.getValue());
//                    Log.e(TAG, "processRequest: " + item.getKey() + " : " + value);

            String key = item.getKey();

            if (lowerCase && !TextUtils.isEmpty(key)) {
                key = key.toLowerCase();
            }

            responseHeaders.put(key, value);
        }

        return responseHeaders;
    }

    private String getMimeTypeFromContentType(String contentType) {
        if (!TextUtils.isEmpty(contentType)) {
            String[] types = contentType.split(";");
            return types[0];
        }

        return null;
    }

    private InputStream cloneInputStream(InputStream source) throws IOException {
//        InputStream in = new BufferedInputStream(source);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;

        while ((len = source.read(buffer)) > -1) {
            outputStream.write(buffer, 0, len);
        }

        outputStream.flush();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        outputStream.close();

        return inputStream;
    }

    private byte[] consumeInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = inputStream.read(buffer)) != -1; ) {
            outputStream.write(buffer, 0, count);
        }

        byte[] results = outputStream.toByteArray();
        outputStream.close();

        return results;
    }

    public static String getSecFetchDest(WebResourceRequest request) {
        String url = request.getUrl().toString();
        Map<String, String> headers = request.getRequestHeaders();
        String acceptValue = HttpHeader.getValueFromMap(headers, "Accept");

        if (!TextUtils.isEmpty(acceptValue)) {
            if (acceptValue.startsWith("text/html")) {
                if (request.isForMainFrame()) {
                    return "document";
                } else {
                    return "iframe";
                }
            } else if (acceptValue.startsWith("image/")) {
                return "image";
            } else if (acceptValue.startsWith("text/css")) {
                return "style";
            } else if (acceptValue.startsWith("*/*")) {
                if (url.contains(".js") || url.contains("javascript")) {
                    return "script";
                }

                // 네이버 전용.
                if (url.contains("_callback=_jsonp") || url.contains("_callback=jQuery")) {
                    return "script";
                }
            }
        }

        return "empty";
    }

    public static String getSecFetchUser(String currentUrl, String prevUrl, WebResourceRequest request) {
//        if (request.hasGesture()) {
//            return "?1";
//        }

        Map<String, String> headers = request.getRequestHeaders();
        String acceptValue = HttpHeader.getValueFromMap(headers, "Accept");

        if (!TextUtils.isEmpty(acceptValue)) {
            if (acceptValue.startsWith("text/html")) {
                if (!currentUrl.equals(prevUrl)) {
//                if (request.isForMainFrame()) {
                    return "?1";
                }
            }
        }

        return null;
    }

    public static String getSecFetchMode(WebResourceRequest request) {
        String method = request.getMethod();
        Map<String, String> headers = request.getRequestHeaders();
        String acceptValue = HttpHeader.getValueFromMap(headers, "Accept");
        String originValue = HttpHeader.getValueFromMap(headers, "origin");

        if ("POST".equalsIgnoreCase(method)) {
            // 특수 상황에 다른값이 나온다.
            return "no-cors";
        } else {
            if (!TextUtils.isEmpty(acceptValue)) {
                if (acceptValue.startsWith("text/html")) {
                    return "navigate";
                } else if (acceptValue.startsWith("application/json")) {
                    return "cors";
                } else {
                    if (TextUtils.isEmpty(originValue)) {
                        return "no-cors";
                    } else {
                        return "cors";
                    }
                }
            }
        }

        return "same-origin";
    }


    public static String getSecFetchSite(String currentUrl, String prevUrl, String url, WebResourceRequest request) throws MalformedURLException {
//        String url = request.getUrl().toString();
        Map<String, String> headers = request.getRequestHeaders();
        String acceptValue = HttpHeader.getValueFromMap(headers, "Accept");
//        String refererValue = getValueFromMap(headers, "referer");
//
//        // 네이버 전용.
//        if (url.contains("https://m.search.naver.com/search.naver")) {
//            refererValue = "https://m.naver.com/";
//        }

        if (!TextUtils.isEmpty(acceptValue)) {
            if (acceptValue.startsWith("text/html")) {
                if (TextUtils.isEmpty(prevUrl)) {
                    return "none";
                } else {
                    currentUrl = prevUrl;
                }
            }
        }

        if (!TextUtils.isEmpty(currentUrl) && !currentUrl.equals(WebViewManager.BLANK_PAGE)) {
            URL urlBase = new URL(url);
            URL urlReferer = new URL(currentUrl);

            int portBase = (urlBase.getPort() < 0) ? urlBase.getDefaultPort() : urlBase.getPort();
            int portReferer = (urlReferer.getPort() < 0) ? urlReferer.getDefaultPort() : urlReferer.getPort();

//            Log.e("TAG", "current: " + urlBase.getHost() + " / ref: " + urlReferer.getHost() + "/ current: " + urlBase.getAuthority() + " / ref: " + urlReferer.getAuthority());
            if (urlBase.getHost().equals(urlReferer.getHost()) &&
                    urlBase.getProtocol().equals(urlReferer.getProtocol()) &&
                    portBase == portReferer) {
                return "same-origin";
            } else {
                String domainBase = DnsParser.getDomain(urlBase.getHost());
                String domainReferer = DnsParser.getDomain(urlReferer.getHost());

                if (!TextUtils.isEmpty(domainBase) && domainBase.equals(domainReferer)) {
                    return "same-site";
                } else {
                    return "cross-site";
                }
            }
//        } else if (!_firstPage) {
//            return "same-origin";
        }

        return "none";
    }

    private void addHttpHeaders(String currentUrl, String originalUrl, WebResourceRequest request, HttpURLConnection conn) throws MalformedURLException {
        String url = conn.getURL().toString();
        boolean isSsl = url.startsWith("https");
        Map<String, String> headers = request.getRequestHeaders();
//                List<String> ignoreList = new ArrayList<>();
//                ignoreList.add("x-requested-with");
//                ignoreList.add("sec-ch-ua");
//                ignoreList.add("sec-ch-ua-mobile");
//                ignoreList.add("sec-ch-ua-platform");

        boolean hasReferer = false;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();

            if (key.equalsIgnoreCase("x-requested-with") ||
                    key.equalsIgnoreCase("Z-Zero-Redirect-Code") ||
                    key.toLowerCase().startsWith("sec-ch-ua")) {
                continue;
            } else if (key.equalsIgnoreCase("referer")) {
                hasReferer = true;
            }

            if (isSsl && !TextUtils.isEmpty(key)) {
                key = key.toLowerCase();
            }

            conn.setRequestProperty(key, entry.getValue());
        }

        // ssl 일때만 붙는다.
        if (isSsl) {
            Map<String, String> addHeaders = getSslAddHeaders(currentUrl, originalUrl, request, conn.getURL(), headers, hasReferer);

            for (Map.Entry<String, String> entry : addHeaders.entrySet()) {
                conn.setRequestProperty(entry.getKey().toLowerCase(), entry.getValue());
            }
        } else {
            String statusCodeString = HttpHeader.getValueFromMap(headers, "Z-Zero-Redirect-Code");
            int statusCode = TextUtils.isEmpty(statusCodeString) ? -1 : Integer.parseInt(statusCodeString);

//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
//Accept-Encoding: gzip, deflate
//Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
//Connection: keep-alive
//Cookie: NFS=2; MM_PF=SEARCH
//Host: m.naver.com
//Upgrade-Insecure-Requests: 1
//User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Mobile Safari/537.36

            // 이건 자동추가된다.
//                                conn.setRequestProperty("Upgrade-Insecure-Requests", "1");

            if (statusCode != HttpURLConnection.HTTP_MOVED_PERM && statusCode != HttpURLConnection.HTTP_MOVED_TEMP) {
                CookieManager cookieManager = CookieManager.getInstance();
                String cookies = cookieManager.getCookie(url);
                Log.d(TAG, "host: " + conn.getURL().getHost() + ", Authority: " + conn.getURL().getAuthority() + ", cookies: " + cookies);

                if (!TextUtils.isEmpty(cookies)) {
                    conn.setRequestProperty("Cookie", cookies);
                }

                conn.setRequestProperty("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
                conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                conn.setRequestProperty("Connection", "keep-alive");
            }
        }
    }

    public void addOkHttpHeaders(String currentUrl, String originalUrl, WebResourceRequest request, URL url, Request.Builder builder) throws MalformedURLException {
        String urlString = url.toString();
        boolean isSsl = urlString.startsWith("https");
        Map<String, String> headers = request.getRequestHeaders();
//                List<String> ignoreList = new ArrayList<>();
//                ignoreList.add("x-requested-with");
//                ignoreList.add("sec-ch-ua");
//                ignoreList.add("sec-ch-ua-mobile");
//                ignoreList.add("sec-ch-ua-platform");

        boolean hasReferer = false;

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();

            if (key.equalsIgnoreCase("x-requested-with") ||
                    key.equalsIgnoreCase("Z-Zero-Redirect-Code") ||
                    key.toLowerCase().startsWith("sec-ch-ua")) {
                continue;
            } else if (key.equalsIgnoreCase("referer")) {
                hasReferer = true;
            }

            if (isSsl && !TextUtils.isEmpty(key)) {
                key = key.toLowerCase();
            }

            builder.addHeader(key, entry.getValue());
        }

        // ssl 일때만 붙는다.
        if (isSsl) {
            Map<String, String> addHeaders = getSslAddHeaders(currentUrl, originalUrl, request, url, headers, hasReferer);

            for (Map.Entry<String, String> entry : addHeaders.entrySet()) {
                builder.addHeader(entry.getKey().toLowerCase(), entry.getValue());
            }
        } else {
            String statusCodeString = HttpHeader.getValueFromMap(headers, "Z-Zero-Redirect-Code");
            int statusCode = TextUtils.isEmpty(statusCodeString) ? -1 : Integer.parseInt(statusCodeString);

//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
//Accept-Encoding: gzip, deflate
//Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
//Connection: keep-alive
//Cookie: NFS=2; MM_PF=SEARCH
//Host: m.naver.com
//Upgrade-Insecure-Requests: 1
//User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Mobile Safari/537.36

            // 이건 자동추가된다.
//                                conn.setRequestProperty("Upgrade-Insecure-Requests", "1");

            if (statusCode != HttpURLConnection.HTTP_MOVED_PERM && statusCode != HttpURLConnection.HTTP_MOVED_TEMP) {
                CookieManager cookieManager = CookieManager.getInstance();
                String cookies = cookieManager.getCookie(urlString);
                Log.d(TAG, "host: " + url.getHost() + ", Authority: " + url.getAuthority() + ", cookies: " + cookies);

                if (!TextUtils.isEmpty(cookies)) {
                    builder.addHeader("Cookie", cookies);
                }

                builder.addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
                builder.addHeader("Accept-Encoding", "gzip, deflate");
                builder.addHeader("Connection", "keep-alive");
            }
        }
    }

    private Map<String, String> getSslAddHeaders(String currentUrl, String originalUrl, WebResourceRequest request, URL url, Map<String, String> headers, boolean hasReferer) throws MalformedURLException {
        String urlString = url.toString();
        Map<String, String> addHeaders = new HashMap<>();
        String ua = HttpHeader.getValueFromMap(headers, "user-agent");
        String accept = HttpHeader.getValueFromMap(headers, "accept");

        // 네이버 전용.
//            if (url.contains("https://m.search.naver.com/search.naver")) {
//                addHeaders.put("Referer", "https://m.naver.com/");
//            }

        String fetchDest = getSecFetchDest(request);

        // chrome 124 부터 추가됨.
        if (accept.startsWith("text/html")) {
            addHeaders.put("Priority", "u=0, i");
        } else if (accept.startsWith("text/css")) {
            addHeaders.put("Priority", "u=0");
        } else if (accept.startsWith("image")) {
            addHeaders.put("Priority", "i");
        } else if (accept.startsWith("application/json")) {
            addHeaders.put("Priority", "u=1, i");
        } else {
            if (TextUtils.isEmpty(fetchDest)) {
                if (accept.startsWith("text/html")) {
                    addHeaders.put("Priority", "u=4, i");
                    addHeaders.put("Purpose", "prefetch");
                } else if (accept.startsWith("text/plain")) {
                    addHeaders.put("Priority", "u=4, i");
                }
            }
        }

        // 이건 자동추가된다.
//                                addHeaders.put("Upgrade-Insecure-Requests", "1");
        String secFetchUser = getSecFetchUser(currentUrl, originalUrl, request);
        if (!TextUtils.isEmpty(secFetchUser)) {
            addHeaders.put("Sec-Fetch-User", secFetchUser);
        }

        addHeaders.put("Sec-Fetch-Site", getSecFetchSite(currentUrl, originalUrl, urlString, request));
        addHeaders.put("Sec-Fetch-Mode", getSecFetchMode(request));
        addHeaders.put("Sec-Fetch-Dest", fetchDest);

        if (!hasReferer) {
            if (fetchDest.equals("document")) {
                if (!TextUtils.isEmpty(originalUrl) && !currentUrl.equals(originalUrl)) {
                    addHeaders.put("Referer", originalUrl);
                }
            }
        }

        boolean useDetail = getUseDetail(urlString);
        boolean inclFullVersion = false;

        if (urlString.startsWith("https://cr.shopping.naver.com/adcr.nhn?x")) {
            inclFullVersion = true;
        }

        Map<String, String> chUa = HttpHeader.getSecChUa(ua, UserManager.getInstance().chromeVersion, UserManager.getInstance().browserVersion, UserManager.getInstance().isPcUa(), useDetail, inclFullVersion);

        for (Map.Entry<String, String> entry : chUa.entrySet()) {
            addHeaders.put(entry.getKey(), entry.getValue());
        }

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(urlString);
        Log.d(TAG, "host: " + url.getHost() + ", Authority: " + url.getAuthority() + ", cookies: " + cookies);

        if (!TextUtils.isEmpty(cookies)) {
            addHeaders.put("Cookie", cookies);
        }

        String acceptLanguage = "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7";
        // header 부분 통합 필요.
        if (ua.contains(EdgeClientHints.UA_NAME)) {
            acceptLanguage = "ko,en;q=0.9,en-US;q=0.8";
        }

        addHeaders.put("Accept-Language", acceptLanguage);
        addHeaders.put("Accept-Encoding", "gzip, deflate, br, zstd");
//                                addHeaders.put("", "");

        return addHeaders;

//accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
//accept-encoding: gzip, deflate, br
//accept-language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
//cookie: NFS=2; NNB=O7R6CB4B5BIGG; MM_PF=SEARCH
//sec-ch-ua: "Chromium";v="106", "Google Chrome";v="106", "Not;A=Brand";v="99"
//sec-ch-ua-mobile: ?1
//sec-ch-ua-platform: "Android"
//sec-fetch-dest: document
//sec-fetch-mode: navigate
//sec-fetch-site: none
//sec-fetch-user: ?1
//upgrade-insecure-requests: 1
//user-agent: Mozilla/5.0 (Linux; Android 8.0.0; SM-G930K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Mobile Safari/537.36

//sec-fetch-dest: document
//sec-fetch-mode: navigate
//sec-fetch-site: none
//sec-fetch-user: ?1
//upgrade-insecure-requests: 1
//user-agent: Mozilla/5.0 (Linux; Android 8.0.0; SM-G930S Build/R16NW; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/105.0.5195.136 Mobile Safari/537.36

    }

    private boolean getUseDetail(String url) {
        List<String> useAcceptChUrlList = new ArrayList<>();
        // 특수 페이지만 이걸 사용한다.
        // 네이버 홈
        // Accept-Ch 에는 있는데 실제로 Sec-CH-UA-Full-Version-List 는 전송되지 않음. iframe 제약인 듯. 이건, 나중에 크롬 패치로 변경될 수 있음.
//            useAcceptChUrlList.add("https://m.search.naver.com/remote_frame");
//            useAcceptChUrlList.add("https://m.search.naver.com/remote_frame_preloader.js");

        // 네이버 홈 검색
        useAcceptChUrlList.add("https://m.search.naver.com/search.naver?");
        useAcceptChUrlList.add("https://m.search.naver.com/p/crd/rd?");

        // 사러가기
        //https://msearch.shopping.naver.com/product/84946754439
        //https://msearch.shopping.naver.com/remote_frame.html
        //https://msearch.shopping.naver.com/api/template/STRIP_BANNER
        //https://msearch.shopping.naver.com/remote_frame_storage.js
        useAcceptChUrlList.add("https://msearch.shopping.naver.com/product/");
        useAcceptChUrlList.add("https://msearch.shopping.naver.com/remote_frame.html");
        useAcceptChUrlList.add("https://msearch.shopping.naver.com/api/template/STRIP_BANNER");
        useAcceptChUrlList.add("https://msearch.shopping.naver.com/remote_frame_storage.js");

        // 제품상세페이지
        //https://cr.shopping.naver.com/adcr.nhn?x=YJmPL9B9yMZdiO%2FqR7zh2v%2F%2F%2Fw%3D%3DsArH3t9zz79AnXd%2FOqv75oZ8yBUd5or1JqDVSrU58Xukems9SyhZ7IAj2T9kvHyTONordetkMNsFH%2FcHRKpc4YMGKaebSEEQdiKc1zAouYezXjpjCyXDf7S7Ld3IkeGXLcYn18Njamkp2pTGu7iiZ7oriPkxPAXjk2LX8wmqtKDRkQ5r9X7si0E2Rie1hjXEQfXu6zTr%2Fk4BsW8zDx4q5mwkm1F%2BjGN4zypFqgAg53qMQhkkHXK4%2FmSRy5icYE%2F272%2BUpHb0aCSGHSQivs3iquKRniMqrxgvrORUEcihYwb48rXUijfN3AhPYWcDHy4PcmyRwq1p%2F2HazsWS48%2FCg400DpwPl0xdqxPoDW6pKYpqEZ9tpOGOJtwKbGaSBDS%2FiiSyYHExsWGyOd0%2FU728JCt6WLSqj1BKa2PJCL2u4sLN1RdQcaMMpTcq%2Bt1qpWk5rl83zm25eUn7WEuD%2F%2BOQc4VBblosE6rEBWjHjhe8Z12r2od%2FjC1JN5tW3jGQm7WStX%2Bmnkjy3h3lCV2aQIQD%2F2GdLc9UJvUYvmB%2BHDsOH4dgbGD519a5HB55Cq6g3dK0cb1NmOoRBMBsXNQGXzjfbC%2BFUQB%2FrDAzm2q1KHRsHkfE%3D&nvMid=84946754439&catId=50014220
        //https://wcs.naver.com/b
        useAcceptChUrlList.add("https://msearch.shopping.naver.com/api/recent-viewed?");
        useAcceptChUrlList.add("https://cr.shopping.naver.com/adcr.nhn?x");
        useAcceptChUrlList.add("https://wcs.naver.com/b");

        // 사러가기 pc
        useAcceptChUrlList.add("https://search.shopping.naver.com/product/");
        useAcceptChUrlList.add("https://search.shopping.naver.com/api/template/STRIP_BANNER");

        boolean useDetail = false;

        if (!TextUtils.isEmpty(UserManager.getInstance().chromeVersion)) {
            for (String chUrl : useAcceptChUrlList) {
                if (url.startsWith(chUrl)) {
                    useDetail = true;
                    break;
                }
            }
        }

        return useDetail;
    }

    static class EncodingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response response = chain.proceed(chain.request());
            return uncompress(response);
        }

        private Response uncompress(Response response) {
//            Log.d("EncodingInterceptor", "uncompress");
            if (!HttpHeaders.promisesBody(response)) {
                return response;
            }

//            HttpHeaders.receiveHeaders();

            ResponseBody body = response.body();
            String encoding = response.header("Content-Encoding");

            if (TextUtils.isEmpty(encoding)) {
                return response;
            }

            try {
                InputStream stream = body.source().inputStream();
                InputStream decompressedStream = null;

                if ("gzip".equalsIgnoreCase(encoding)) {
                    Log.d("EncodingInterceptor", "gzip 파서!");
                    decompressedStream = new GZIPInputStream(stream);
//                    GZIPInputStream gzipInputStream = new GZIPInputStream(stream);
//                    gzipInputStream.close();
                } else if ("br".equalsIgnoreCase(encoding)) {
                    Log.d("EncodingInterceptor", "brotli 파서!");
                    decompressedStream = new BrotliInputStream(stream);
//                    BrotliInputStream compressorInputStream = new BrotliInputStream(stream);
//                    compressorInputStream.close();
                } else if ("deflate".equalsIgnoreCase(encoding)) {
                    Log.d("EncodingInterceptor", "deflate 파서!");
                    decompressedStream = new DeflaterInputStream(stream);
                } else {
                    Log.d("EncodingInterceptor", "일반 파서!");
                }

                if (decompressedStream != null) {
                    BufferedSource source = Okio.buffer(Okio.source(decompressedStream));
                    ResponseBody.create(source, body.contentType(), -1);

                    return response.newBuilder()
                            .removeHeader("Content-Encoding")
                            .removeHeader("Content-Length")
                            .body(ResponseBody.create(source, body.contentType(), -1))
                            .build();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

//        fun uncompress(response: Response): Response {
//            if (!response.promisesBody()) {
//                return response
//            }
//            val body = response.body
//            val encoding = response.header("Content-Encoding") ?: return response
//
//            val decompressedSource = when {
//                encoding.equals("br", ignoreCase = true) ->
//                BrotliInputStream(body.source().inputStream()).source().buffer()
//                encoding.equals("gzip", ignoreCase = true) ->
//                GzipSource(body.source()).buffer()
//    else -> return response
//            }
//
//            return response.newBuilder()
//                    .removeHeader("Content-Encoding")
//                    .removeHeader("Content-Length")
//                    .body(decompressedSource.asResponseBody(body.contentType(), -1))
//                    .build()
//        }
    }


}
