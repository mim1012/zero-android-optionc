package com.sec.android.app.sbrowser.engine.WebEngine;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

import com.sec.android.app.sbrowser.engine.WebEngine.Headers.UserAgentClientHints;
import com.sec.android.app.sbrowser.engine.WebViewManager;

import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpEngine {

    private static final String TAG = HttpEngine.class.getSimpleName();

    public static boolean useCookieManager = true;

    private HttpCookieController _cookieController = null;
    private OkHttpClient _httpClient = null;
    private String _proxyString = null;
    private int _responseCode = 0;
    private String _responseMessage = null;
    private boolean _useGenHeader = true;
    private boolean _isPc = false;
    private String _ua = null;
    private String _chromeVersion = null;
    private String _browserVersion = null;
    private String _nnb = null;
    private String _url = null;
    private String _currentUrl = null;
    private String _origin = null;
    private String _referer = null;
    private boolean _useDetailChUa = false;

    private Map<String, String> _addedHeaders = new LinkedHashMap<>();
    private List<String> _setCookieList = new LinkedList<>();

    public HttpEngine(Context applicationContext) {
        this(applicationContext, null);
    }

    public HttpEngine(Context applicationContext, String proxyString) {
        _proxyString = proxyString;
        initClient();
    }

    private void initClient() {
//        CronetEngine engine = new CronetEngine.Builder(applicationContext).build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addNetworkInterceptor(new AddCookiesInterceptor())
                .addNetworkInterceptor(loggingInterceptor)
//                            .set
//                .addNetworkInterceptor(new WebResourceController.EncodingInterceptor())
//                .addNetworkInterceptor(new LoggingInterceptor())
//                .addInterceptor(CronetInterceptor.newBuilder(engine).build())
;
        if (!TextUtils.isEmpty(_proxyString)) {
            String[] proxyParts = _proxyString.split(":");

            if (proxyParts.length >= 2) {
                String proxyHost = proxyParts[0];
                int proxyPort = Integer.parseInt(proxyParts[1]);
                Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
                builder.proxy(proxy);

                if (proxyParts.length >= 4) {
                    Authenticator.setDefault(new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            if (getRequestingHost().equalsIgnoreCase(proxyHost)) {
                                if (proxyPort == getRequestingPort()) {
                                    return new PasswordAuthentication(proxyParts[2], proxyParts[3].toCharArray());
                                }
                            }
                            return null;
                        }
                    });
                }
            }
        }

        _httpClient = builder.build();
    }

    public void setCookieController(HttpCookieController cookieController) {
        _cookieController = cookieController;
    }

    public int getResponseCode() {
        return _responseCode;
    }

    public String getResponseMessage() {
        return _responseMessage;
    }

    public void setUseGenHeader(boolean useGenHeader) {
        _useGenHeader = useGenHeader;
    }

    public void setIsPc(boolean isPc) {
        _isPc = isPc;
    }

    public void setUa(String ua) {
        _ua = ua;
    }

    public String getUa() {
        return _ua;
    }

    public void setChromeVersion(String chromeVersion) {
        _chromeVersion = chromeVersion;
    }

    public String getChromeVersion() {
        return _chromeVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        _browserVersion = browserVersion;
    }

    public String getBrowserVersion() {
        return _browserVersion;
    }

    public void setNnb(String nnb) {
        _nnb = nnb;
    }

    public String getNnb() {
        return _nnb;
    }

    public String getUrl() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
    }

    public String getCurrentUrl() {
        return _currentUrl;
    }

    public void setCurrentUrl(String url) {
        _currentUrl = url;
    }

    public void setOrigin(String origin) {
        _origin = origin;
    }

    public String getReferer() {
        return _referer;
    }

    public void setReferer(String referer) {
        _referer = referer;
    }

    public void setUseDetailChUa(boolean useDetailChUa) {
        _useDetailChUa = useDetailChUa;
    }

    public void setAddedHeader(String key, String value) {
        _addedHeaders.put(key, value);
    }

    public void clearAddedHeader() {
        _addedHeaders.clear();
    }

    public List<String> getSetCookieList() {
        return _setCookieList;
    }

    public void setCookieForList(String value) {
        String[] keyValues = value.split("=");
        int hasKeyIndex = -1;

        if (keyValues[0].startsWith("NID_") || keyValues[0].startsWith("NNB")) {
            return;
        }

        for (int i = 0; i <_setCookieList.size(); ++i) {
            if (_setCookieList.get(i).startsWith(keyValues[0])) {
                hasKeyIndex = i;
                break;
            }
        }

        if (hasKeyIndex > -1) {
            _setCookieList.set(hasKeyIndex, value);
        } else{
            _setCookieList.add(value);
        }
    }

    public String getAllSetCookieString() {
        return String.join("\n", _setCookieList);
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

//            if (lowerCase && !TextUtils.isEmpty(key)) {
//                key = key.toLowerCase();
//            }

            responseHeaders.put(key, value);
        }

        return responseHeaders;
    }

    public void resetClient() {
        initClient();
    }

    public String requestNaverNnb() {
        String url = "https://m.search.naver.com/search.naver?where=m&sm=mtp_hty.top&query=";

//        https://lcs.naver.com/m?u=https%3A%2F%2Fnid.naver.com%2Fnidlogin.login%3Fsvctype%3D262144&e=&os=MacIntel&ln=ko-KR&sr=400x897&pr=2&bw=400&bh=897&c=30&j=N&k=Y&i=&ct=&navigationStart=1674888655457&fetchStart=1674888655464&domainLookupStart=1674888655465&domainLookupEnd=1674888655512&connectStart=1674888655512&connectEnd=1674888655603&secureConnectionStart=1674888655516&requestStart=1674888655603&responseStart=1674888655621&responseEnd=1674888655623&domLoading=1674888655630&domInteractive=1674888655782&domContentLoadedEventStart=1674888655782&domContentLoadedEventEnd=1674888655782&domComplete=1674888656013&loadEventStart=1674888656013&loadEventEnd=1674888656019&first-paint=301.7999999988824&first-contentful-paint=301.7999999988824&pid=9a8adc1e08da078ecbc6b878ece2e618&ts=1674888656235&EOU

//        Accept: image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8
//        Accept-Encoding: gzip, deflate, br
//        Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
//        Connection: keep-alive
//        Host: lcs.naver.com
//        Referer: https://nid.naver.com/
//        sec-ch-ua: "Not_A Brand";v="99", "Google Chrome";v="109", "Chromium";v="109"
//        sec-ch-ua-mobile: ?1
//        sec-ch-ua-platform: "Android"
//        Sec-Fetch-Dest: image
//        Sec-Fetch-Mode: no-cors
//        Sec-Fetch-Site: same-site
//        User-Agent: Mozilla/5.0 (Linux; Android 8.0.0; SM-G930K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Mobile Safari/537.36

//        https://lcs.naver.com/m?u=https%3A%2F%2Fnid.naver.com%2Fnidlogin.login%3Fsvctype%3D262144&e=&os=Linux%20armv8l&ln=ko-KR&sr=360x640&pr=3&bw=360&bh=560&c=24&j=N&k=Y&i=&ct=&navigationStart=1674892331127&fetchStart=1674892331151&domainLookupStart=1674892331167&domainLookupEnd=1674892331167&connectStart=1674892331167&connectEnd=1674892331222&secureConnectionStart=1674892331167&requestStart=1674892331233&responseStart=1674892331255&responseEnd=1674892331260&domLoading=1674892331295&domInteractive=1674892331704&domContentLoadedEventStart=1674892331704&domContentLoadedEventEnd=1674892331704&domComplete=1674892332075&loadEventStart=1674892332075&loadEventEnd=1674892332106&first-paint=426.10000000009313&first-contentful-paint=426.29999999981374&pid=1b24ff9108430e9d483483e1e9c2cca3&ts=1674892332139&EOU
//        u:https://nid.naver.com/nidlogin.login?svctype=262144
//        e:
//        os:Linux armv8l
//        ln:ko-KR
//        sr:360x640
//        pr:3
//        bw:360
//        bh:560
//        c:24
//        j:N
//        k:Y
//        i:
//        ct:
//        navigationStart:              1674892331127
//        fetchStart:                   1674892331151
//        domainLookupStart:            1674892331167
//        domainLookupEnd:              1674892331167
//        connectStart:                 1674892331167
//        connectEnd:                   1674892331222
//        secureConnectionStart:        1674892331167
//        requestStart:                 1674892331233
//        responseStart:                1674892331255
//        responseEnd:                  1674892331260
//        domLoading:                   1674892331295
//        domInteractive:               1674892331704
//        domContentLoadedEventStart:   1674892331704
//        domContentLoadedEventEnd:     1674892331704
//        domComplete:                  1674892332075
//        loadEventStart:               1674892332075
//        loadEventEnd:                 1674892332106
//        first-paint:                  426.10000000009313
//        first-contentful-paint:       426.29999999981374
//        pid:                          1b24ff9108430e9d483483e1e9c2cca3
//        ts:                           1674892332139
//        EOU:

//        https://lcs.naver.com/m?u=https%3A%2F%2Fnid.naver.com%2Fnidlogin.login%3Fsvctype%3D262144&e=&os=Linux%20armv8l&ln=ko-KR&sr=360x640&pr=3&bw=360&bh=560&c=24&j=N&k=Y&i=&ct=&navigationStart=1674893312293&fetchStart=1674893312314&domainLookupStart=1674893312353&domainLookupEnd=1674893312353&connectStart=1674893312353&connectEnd=1674893312431&secureConnectionStart=1674893312353&requestStart=1674893312438&responseStart=1674893312462&responseEnd=1674893312471&domLoading=1674893312495&domInteractive=1674893312889&domContentLoadedEventStart=1674893312889&domContentLoadedEventEnd=1674893312889&domComplete=1674893313182&loadEventStart=1674893313183&loadEventEnd=1674893313214&first-paint=383.10000000009313&first-contentful-paint=383.20000000018626&pid=cefbe0a3776485a9f00681099fbebce4&ts=1674893313237&EOU
//        u:https://nid.naver.com/nidlogin.login?svctype=262144
//        e:
//        os:Linux armv8l
//        ln:ko-KR
//        sr:360x640
//        pr:3
//        bw:360
//        bh:560
//        c:24
//        j:N
//        k:Y
//        i:
//        ct:
//        navigationStart:              1674893312293
//        fetchStart:                   1674893312314
//        domainLookupStart:            1674893312353
//        domainLookupEnd:              1674893312353
//        connectStart:                 1674893312353
//        connectEnd:                   1674893312431
//        secureConnectionStart:        1674893312353
//        requestStart:                 1674893312438
//        responseStart:                1674893312462
//        responseEnd:                  1674893312471
//        domLoading:                   1674893312495
//        domInteractive:               1674893312889
//        domContentLoadedEventStart:   1674893312889
//        domContentLoadedEventEnd:     1674893312889
//        domComplete:                  1674893313182
//        loadEventStart:               1674893313183
//        loadEventEnd:                 1674893313214
//        first-paint:                  383.10000000009313
//        first-contentful-paint:       383.20000000018626
//        pid:                          cefbe0a3776485a9f00681099fbebce4
//        ts:                           1674893313237
//        EOU:


        // s9
//        u:https://nid.naver.com/nidlogin.login?svctype=262144
//        e:
//        os:Linux armv8l
//        ln:ko-KR
//        sr:360x740
//        pr:3
//        bw:360
//        bh:612
//        c:24
//        j:N
//        k:Y
//        i:
//        ct:
//        navigationStart:            1674894252210
//        fetchStart:                 1674894252220
//        domainLookupStart:          1674894252229
//        domainLookupEnd:            1674894252229
//        connectStart:               1674894252229
//        connectEnd:                 1674894252270
//        secureConnectionStart:      1674894252229
//        requestStart:               1674894252278
//        responseStart:              1674894252300
//        responseEnd:                1674894252303
//        domLoading:                 1674894252353
//        domInteractive:             1674894252651
//        domContentLoadedEventStart: 1674894252651
//        domContentLoadedEventEnd:   1674894252651
//        domComplete:                1674894252841
//        loadEventStart:             1674894252841
//        loadEventEnd:               1674894252872
//        first-paint:299.0999999999767
//        first-contentful-paint:299.19999999995343
//        pid:2dce45797721a0c7b795c980d92eae7a
//        ts:1674894252891
//        EOU:

//        https://lcs.naver.com/m?u=https%3A%2F%2Fnid.naver.com%2Fnidlogin.login%3Fsvctype%3D262144&e=&os=Linux%20armv8l&ln=ko&sr=360x640&pr=3&bw=360&bh=560&c=24&j=N&k=Y&i=&ls=GLTTAOY4KOHWG&ct=&navigationStart=1674890839719&fetchStart=1674890839722&domainLookupStart=1674890839730&domainLookupEnd=1674890839730&connectStart=1674890839730&connectEnd=1674890839821&secureConnectionStart=1674890839733&requestStart=1674890839829&responseStart=1674890839848&responseEnd=1674890839851&domLoading=1674890839901&domInteractive=1674890840156&domContentLoadedEventStart=1674890840157&domContentLoadedEventEnd=1674890840157&domComplete=1674890840434&loadEventStart=1674890840434&loadEventEnd=1674890840482&first-paint=490.9000000001397&first-contentful-paint=490.9000000001397&pid=304f82356eb96b07125e738e7589e4c1&ts=1674890840533&EOU
//        u:https://nid.naver.com/nidlogin.login?svctype=262144
//        e:
//        os:Linux armv8l
//        ln:ko
//        sr:360x640
//        pr:3
//        bw:360
//        bh:560
//        c:24
//        j:N
//        k:Y
//        i:
//        ls:GLTTAOY4KOHWG
//        ct:
//        navigationStart:1674890839719
//        fetchStart:1674890839722
//        domainLookupStart:1674890839730
//        domainLookupEnd:1674890839730
//        connectStart:1674890839730
//        connectEnd:1674890839821
//        secureConnectionStart:1674890839733
//        requestStart:1674890839829
//        responseStart:1674890839848
//        responseEnd:1674890839851
//        domLoading:1674890839901
//        domInteractive:1674890840156
//        domContentLoadedEventStart:1674890840157
//        domContentLoadedEventEnd:1674890840157
//        domComplete:1674890840434
//        loadEventStart:1674890840434
//        loadEventEnd:1674890840482
//        first-paint:490.9000000001397
//        first-contentful-paint:490.9000000001397
//        pid:304f82356eb96b07125e738e7589e4c1
//        ts:1674890840533
//        EOU:

        return requestUrlWithOkHttpClient(url, null);
    }

    public String requestNaverApiSbth() {
        String url = "http://54.180.205.28:3001/sbth";
        return requestUrlWithOkHttpClient(url, null);
    }

    public String requestNaverLcsPidFromUrl(String nnb, String locationUrl, long timeString) {
        String url = "http://54.180.205.28:3001/?";
        return requestUrlWithOkHttpClient(url + "nnb=" + nnb + "&url=" + locationUrl + "&time=" + timeString, null);
    }

    public String requestNaverShoppingProductApi(String keyword, int pageIndex) {
        String url = "https://msearch.shopping.naver.com/api/search/all?adQuery=" + keyword +
                "&origQuery=" + keyword + "&pagingIndex=" + pageIndex + "&pagingSize=40&productSet=total&query=" + keyword + "&sort=rel&viewType=list";
        String referer = _currentUrl;
        _currentUrl = url;

        if (pageIndex <= 1) {
            referer = "https://msearch.shopping.naver.com/search/all?query=" + keyword + "&vertical=search";
        }

        _referer = referer;

//        _httpEngine.setAddedHeader("cookie", "NNB=JJCPIG2XJWKGK; SHP_BUCKET_ID=4; ncpa=5585524|lt031kmo|57850f7c104a25c33a512a13d2ed1ad693b5e70a|s_213cbdcdc7145|9226b5ac54255f8af3ede7f1a578ee8f3e6bfc6e:1063988|lt075f5s|3e7edb0e16f8f9febfbeaab62cae7a3e9c6c7c4e|s_2c4f553a85173|658bbf987d8e802164b3b64810c70c830141072f:4027379|lt079eog|0d03e17f421f0583917dd2b0fbc3a5989e860f7d|s_9cea49fa27ec|58072db9166a0566406d2e715be9a2b890298889:299687|lurwycg8|a83cef99915a0418ea594e236c5996bc59584b83|s_90763894633079556|e283915366671cf3916cb13b385a0a4402edfb2d");

//        headers.put("referer", "https://msearch.shopping.naver.com/search/all?query=%EA%B0%95%EC%95%84%EC%A7%80%20%EB%85%B8%EC%A6%88%EC%9B%8C%ED%81%AC&prevQuery=%EB%85%B8%EC%A6%88%EC%9B%8C%ED%81%AC&vertical=search");
//        headers.put("logic", "PART");
//        headers.put("sbth", sbth);
//        headers.put("cookie", "NNB=JJCPIG2XJWKGK; SHP_BUCKET_ID=4; ncpa=5585524|lt031kmo|57850f7c104a25c33a512a13d2ed1ad693b5e70a|s_213cbdcdc7145|9226b5ac54255f8af3ede7f1a578ee8f3e6bfc6e:1063988|lt075f5s|3e7edb0e16f8f9febfbeaab62cae7a3e9c6c7c4e|s_2c4f553a85173|658bbf987d8e802164b3b64810c70c830141072f:4027379|lt079eog|0d03e17f421f0583917dd2b0fbc3a5989e860f7d|s_9cea49fa27ec|58072db9166a0566406d2e715be9a2b890298889:299687|lurwycg8|a83cef99915a0418ea594e236c5996bc59584b83|s_90763894633079556|e283915366671cf3916cb13b385a0a4402edfb2d");
//        headers.put("user-agent", "PostmanRuntime/7.37.3");

        setAddedHeader("Accept", "application/json, text/plain, */*");
        setAddedHeader("Accept-Encoding", "gzip, deflate, br");
//        setAddedHeader("User-Agent", "PostmanRuntime/7.37.3");
        setAddedHeader("User-Agent", _ua);

        UserAgentClientHints hints = HttpHeader.getUserAgentClientHints(_ua, _chromeVersion, _browserVersion);
        if (hints != null && !TextUtils.isEmpty(hints.secChUa)) {
            setAddedHeader("sec-ch-ua", hints.secChUa);
        }

        if (!TextUtils.isEmpty(referer)) {
            setAddedHeader("Referer", referer);
        }

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(url);
        Log.d(TAG, "host: " + url + ", cookies: " + cookies);

        if (!TextUtils.isEmpty(cookies)) {
            setAddedHeader("Cookie", cookies);
        }

        // 내장 HttpURLConnection 을 사용하지 않는 이유는 s4 에 탑재된 버전이 너무 낮아 제대로 처리되지 않기 때문.
//        if (BuildConfig.FLAVOR_mode.contains("rank")) {
//            return requestUrlFetch(url);
//        } else {
            return requestUrlWithOkHttpClientFetch(url);
//        }
    }

    public String getNaverMobileSearchUrl(String keyword) {
        String parsed = WebViewManager.keywordEncodeForNaver(keyword);
//        String url = "https://m.search.naver.com/search.naver?where=m&sm=mtp_hty.top&query=" + parsed;
        return "https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m&query=" + parsed;
    }

    public String getNaverSearchUrl(String keyword) {
        String parsed = WebViewManager.keywordEncodeForNaver(keyword);
        return "https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=" + parsed;
    }

    public String getNaverSearchSecondUrl(String keyword, String oldKeyword, String tqi) {
        String parsed = WebViewManager.keywordEncodeForNaver(keyword);
        String oldParsed = WebViewManager.keywordEncodeForNaver(oldKeyword);
//        String url = "https://m.search.naver.com/search.naver?where=m&sm=mtp_hty.top&query=" + parsed;
//              https://m.search.naver.com/search.naver?sm=mtb_hty.top&where=m&oquery=&tqi=h%2FVL8spr44VssS5wgWsssssssYh-217647&query=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80
        return "https://m.search.naver.com/search.naver?sm=mtb_hty.top&where=m&oquery=" + oldParsed + "&tqi=" + tqi + "&query=" + parsed;
    }

    // 모통홈
    public String requestNaverMobileSearch(String keyword) {
        return requestUrlWithOkHttpClient(getNaverMobileSearchUrl(keyword), "same-site");
    }

    // 모통홈
    public String requestNaverMobileSearchFirst(String keyword) {
        return requestUrlWithOkHttpClient(getNaverMobileSearchUrl(keyword), null);
    }

    //
    public String requestNaverShopMobileSearchShoppingTab(String keyword) {
        String parsed = WebViewManager.keywordEncodeForNaver(keyword);
        String url = "https://msearch.shopping.naver.com/search/all?query=" + parsed + "";
        return requestUrlWithOkHttpClient(url);
    }

    public String requestNaverShopMobileSearch(String keyword) {
        String parsed = WebViewManager.keywordEncodeForNaver(keyword);
//        String url = "https://m.search.naver.com/search.naver?where=m&sm=mtp_hty.top&query=" + parsed;
        String url = "https://msearch.shopping.naver.com/search/all?query=" + parsed + "&frm=NVSHSRC&vertical=home&fs=true";
        return requestUrlWithOkHttpClient(url, "same-site");
    }

    public String requestNaverShopMobileSearchFirst(String keyword) {
        String parsed = WebViewManager.keywordEncodeForNaver(keyword);
//        https://msearch.shopping.naver.com/search/all?query=%ED%8D%BC%ED%93%B8%EC%83%B4%ED%91%B8&frm=NVSHSRC&vertical=home&fs=true
        String url = "https://msearch.shopping.naver.com/search/all?query=" + parsed + "&frm=NVSHSRC&vertical=home&fs=true";
        return requestUrlWithOkHttpClient(url, null);
    }

    public String requestNaverShopMobileGoBuy(String mid) {
        String url = "https://msearch.shopping.naver.com/product/" + mid;
        return requestUrlWithOkHttpClient(url, null);
    }

    public String requestNaverShopGoBuy(String mid) {
        String url = "https://search.shopping.naver.com/product/" + mid;
        return requestUrlWithOkHttpClient(url, null);
    }

    // 미사용
    public String requestNaverMobileCatalogAllCompany(String keyword, String mid) {
        // 전체판매처보기
//        https://msearch.shopping.naver.com/catalog/33055988618/products?NaPm=ct%3Dle2qpglk%7Cci%3D16e6e7e2c1ea96fb18b003352493afea025bbe0a%7Ctr%3Dsls%7Csn%3D95694%7Chk%3Dc14fbe688a02e429979d128b0e91535c7ae786cb&cat_id=50004594&frm=MOSCPRO&fromWhere=CATALOG&query=%EA%B0%A4%EB%9F%AD%EC%8B%9C22%20%EC%BC%80%EC%9D%B4%EC%8A%A4&sort=LOW_PRICE
        // api 레퍼럴
//        https://msearch.shopping.naver.com/catalog/33055988618?NaPm=ct%3Dle2qpglk%7Cci%3D16e6e7e2c1ea96fb18b003352493afea025bbe0a%7Ctr%3Dsls%7Csn%3D95694%7Chk%3Dc14fbe688a02e429979d128b0e91535c7ae786cb&cat_id=50004594&frm=MOSCPRO&query=%EA%B0%A4%EB%9F%AD%EC%8B%9C22%20%EC%BC%80%EC%9D%B4%EC%8A%A4&sort=LOW_PRICE


        String parsed = WebViewManager.keywordEncodeForNaver(keyword);
//        https://m.search.naver.com/search.naver?where=m&sm=mtp_hty.top&query=85%EC%9D%B8%EC%B9%98%ED%8B%B0%EB%B9%84%EC%9E%A5
//        https://msearch.shopping.naver.com/catalog/28417036554/products?NaPm=ct%3Dld73np1c%7Cci%3Db366857ce125a77699d39666e7b95fb3d9be4526%7Ctr%3Dslsl%7Csn%3D95694%7Chk%3D20847a36f24f0cef8862672e18d0e2d5bc95d909&fromWhere=CATALOG&query=%EC%9E%90%EC%A0%84%EA%B1%B0&sort=LOW_PRICE
        String url = "https://msearch.shopping.naver.com/product/" + mid + "?where=m&sm=mtp_hty.top&query=" + parsed;
//        String url = "https://msearch.shopping.naver.com/product/83618526081?where=m&sm=mtp_hty.top&query=85%EC%9D%B8%EC%B9%98%ED%8B%B0%EB%B9%84%EC%9E%A5";
        return requestUrlWithOkHttpClient(url, null);
    }

    public String requestNaverMobileCatalogSearchEx(String keyword, String mid) {
        String parsed = WebViewManager.keywordEncodeForNaver(keyword);
//        https://m.search.naver.com/search.naver?where=m&sm=mtp_hty.top&query=85%EC%9D%B8%EC%B9%98%ED%8B%B0%EB%B9%84%EC%9E%A5
        String url = "https://msearch.shopping.naver.com/product/" + mid + "?fromWhere=CATALOG&query=" + parsed;
//        String url = "https://msearch.shopping.naver.com/product/83618526081?fromWhere=CATALOG&query=85%EC%9D%B8%EC%B9%98%ED%8B%B0%EB%B9%84%EC%9E%A5";
        return requestUrlWithOkHttpClient(url, null);
    }

    public String requestNaverMobileCatalogSearchFromShop(String keyword, String mid) {
        String parsed = WebViewManager.keywordEncodeForNaver(keyword);
        String url = "https://msearch.shopping.naver.com/product/" + mid + "?query=" + parsed + "&frm=NVSHSRC&vertical=home&fs=true";
//        String url = "https://msearch.shopping.naver.com/product/83618526081?query=85%EC%9D%B8%EC%B9%98%ED%8B%B0%EB%B9%84%EC%9E%A5&frm=NVSHSRC&vertical=home&fs=true";
        return requestUrlWithOkHttpClient(url, null);
    }

//    public String getActionClickScript() {
//        String jsScript = "(function() {\n" +
//                "var obj = document.querySelectorAll('._product a[href*=\"84610140761\"]')[0];\n" +
//                "var rt = obj.getBoundingClientRect();\n" +
//                "var rtl = rt.left + 20;\n" +
//                "var rtr = rt.right - 20;\n" +
//                "var rx = parseInt(Math.random() * (rtr - rtl) + rtl)\n" +
//                "var rtt = rt.top + 20;\n" +
//                "var rtb = rt.bottom - 20;\n" +
//                "var ry = parseInt(Math.random() * (rtb - rtt) + rtt)\n" +
//                "console.log(obj + \", \" + rx + \", \" + ry)\n" +
//                "\n" +
//                "var evt = new PointerEvent(\"click\", {\n" +
//                "  isTrusted: true,\n" +
//                "  view: window,\n" +
//                "  bubbles: true,\n" +
//                "  composed: true,\n" +
//                "  cancelable: true,\n" +
//                "  clientX: rx,\n" +
//                "  clientY: ry,\n" +
//                "  detail: 1,\n" +
//                "  pointerType: \"touch\",\n" +
//                "});\n" +
//                "obj.dispatchEvent(evt);\n" +
//                "})();\n";
//    }

    public String requestNaverMobileContent(String url) {
        return requestUrlWithOkHttpClient(url, "same-site");
    }

    public String requestNaverMobileContentFromRd(String url) {
        return requestUrlWithOkHttpClient(url, null);
    }

    public String requestNaverMobileContentFromEr(String url) {
        //https://s.search.naver.com/n/scrolllog/v2?u=https%3A%2F%2Fm.search.naver.com%2Fsearch.naver%3Fsm%3Dmtp_hty.top%26where%3Dm%26query%3D%25EC%259E%2590%25EC%25A0%2584%25EA%25B1%25B0%2B%25EB%25A7%25A4%25ED%258A%25B8&q=%EC%9E%90%EC%A0%84%EA%B1%B0+%EB%A7%A4%ED%8A%B8&p=h%2FZT1wqVbxVss75kniCssssstAC-080982&sscode=tab.m.all&slogs=%5B%7B%22t%22%3A%22first%22%2C%22pt%22%3A1676744407838%2C%22al%22%3A%22pwl%3A118%3A867%3A0%3A811%7Cshp_tli%3A985%3A1348%3A0%3A0%7Cimg%3A2333%3A575%3A0%3A0%7Crvw%3A2917%3A1254%3A0%3A0%7Ckin%3A4171%3A866%3A0%3A0%7Cweb_gen%3A5037%3A1000%3A0%3A0%7Cvdo_lst%3A6037%3A566%3A0%3A0%22%2C%22cl%22%3A%22%22%2C%22si%22%3A%226954%3A929%3A400%22%7D%5D&EOU
        //https://lcs.naver.com/m?u=https%3A%2F%2Fm.search.naver.com%2Fsearch.naver%3Fsm%3Dmtp_hty.top%26where%3Dm%26query%3D%25EC%259E%2590%25EC%25A0%2584%25EA%25B1%25B0%2B%25EB%25A7%25A4%25ED%258A%25B8&e=https%3A%2F%2Fm.naver.com%2F&os=MacIntel&ln=ko-KR&sr=400x929&pr=2&bw=400&bh=929&c=30&j=N&k=Y&i=&ls=HMJGGKGOC3YWG&ect=4g&navigationStart=1676744407253&fetchStart=1676744407271&domainLookupStart=1676744407271&domainLookupEnd=1676744407271&connectStart=1676744407271&connectEnd=1676744407271&requestStart=1676744407275&responseStart=1676744407285&responseEnd=1676744407607&domLoading=1676744407305&domInteractive=1676744407861&domContentLoadedEventStart=1676744407861&domContentLoadedEventEnd=1676744407863&domComplete=1676744407869&loadEventStart=1676744407869&loadEventEnd=1676744407872&first-paint=236.89999997615814&first-contentful-paint=280.0999999642372&pid=h%2FZT1wqVbxVss75kniCssssstAC-080982&ssc=tab.m.all&ts=1676744407884&EOU
        //https://er.search.naver.com/er?v=2&navt=0:0:0:0:0:18:18:18:18:18:22:32:354:52:608:608:610:616:616:619:0:0&page_id=h%2FZT1wqVbxVss75kniCssssstAC-080982&ssc=tab.m.all&tags=conn_r_TLSv1.3_.:alpn.h2:nqx_theme.shopping:_ssl:_and6_web:csdark.0
        //https://m.search.naver.com/p/crd/rd?m=0&px=0&py=0&sx=-1&sy=-1&p=h%2FZT1wqVbxVss75kniCssssstAC-080982&q=%EC%9E%90%EC%A0%84%EA%B1%B0+%EB%A7%A4%ED%8A%B8&ie=utf8&rev=1&ssc=tab.m.all&f=m&w=m&s=t0uTZ6CpYarVrH0zo1AMHA%3D%3D&time=1676744408642&abt=%5B%7B%22eid%22%3A%22FBL-MAXCOLL%22%2C%22vid%22%3A%2224%22%7D%2C%7B%22eid%22%3A%22SBR1%22%2C%22vid%22%3A%22754%22%7D%5D&u=javascript&r=&i=&a=shf_tli.rkey
        return requestUrlWithOkHttpClientImage(url);
    }

    public String requestNaverMobileContentFromLcs(String url) {
        return requestUrlWithOkHttpClientImage(url);
    }

    public String requestNaverMobileContentFromRd0(String url) {
        return requestUrlWithOkHttpClientImage(url);
    }

    public String requestNaverMobileContentFromWcs(String url) {
        return requestUrlWithOkHttpClientImage(url);
    }

    public String requestNaverMobileContentFromProductLog(String url, String json) {
        return requestUrlPostWithOkHttpClient(url, "application/json, text/plain, */*, application/json", "application/json", "cors", json);
    }

    public String requestUrlPostWithOkHttpClientFetch(String url, String json) {
//        return requestUrlPostWithOkHttpClient(url, "application/json, text/plain, */*", null, "cors", json);
        return requestUrlPostWithOkHttpClient(url, "application/json, text/plain, */*", "application/json;charset=UTF-8", "cors", json);
    }

    public String requestNaverMobileCompanyContentFromGraphql(String json) {
        return requestUrlPostWithOkHttpClient("https://msearch.shopping.naver.com/api/graphql", "application/json, application/graphql-response+json", "application/json", "cors", json);
//        return requestUrlPostWithOkHttpClientJson("https://msearch.shopping.naver.com/api/graphql", json);
    }

    public String requestNaverMobileCompanyContentFromCe(String text) {
        return requestUrlPostWithOkHttpClientText("https://volts.shopping.naver.com/ce", text);
    }

    public String requestNaver2MobileContent(String url) {
//        가격비교 사러가기 들어가는법 -> 사러가기
//        https://msearch.shopping.naver.com/product/미드값

//        https://cr.shopping.naver.com/adcr.nhn?x=GcaQYoI5RQTbvrpxE8S9gP%2F%2F%2Fw%3D%3DsRyhDt1iJ7ghxwqNaj4NcqSVHArbaeY%2F%2BRQfdB7tywdmBkz73xyI9f%2BkTUTFhj7l%2FFnHxHJAZW6hyrCKsLhg1GdkvT5QZBwVHz0zgkzeh3C4EsVQswWT%2FYbSkX5G4%2F0arcYn18Njamkp2pTGu7iiZ7oriPkxPAXjk2LX8wmqtKDSuByul5c0B0u6YZpLadftfQWT%2F%2BGYMzarx9MD0suTWPAkm1F%2BjGN4zypFqgAg53qMQhkkHXK4%2FmSRy5icYE%2F27GxHYqVzKkfMjQesc6F6EewFGM36LMZaaSR0EAYdI7zY8rXUijfN3AhPYWcDHy4PcxMedvhEpFoNC5RiVrBBwDev487cF5UqM2xRBtY72PtLnAduBn1K7gQLaqEG4sDgiz6v1EZVOUjbUJbrUiknjfH3c5E1rSg%2Bvr7qkXmMoRYPqSdlcL5CiTlNOvuXiSJYz8UZ6O6FJBXewl51ttHcRz0I6YQQ8IjdmqVRv4AbvAIfNWn%2B0%2F0lpURU%2FSsbvq2B9Jaa8frOchYe05dqRE9%2BujYgg6o7ccC4VWLqYhhSHipWINN9RwalG9jaYNyWn%2FJbyy1Vhq%2FH77x9Zlr1IFzyuow%3D%3D&nvMid=85016453275&catId=50007070
        return requestUrlWithOkHttpClient(url, "same-site");
    }

    public String makeProductLogApiBody(JSONObject baseJsonObject, String tr, String referer) {
        String body = null;

        try {
            JSONObject productVariables = baseJsonObject.getJSONObject("product");
            JSONObject aVariables = productVariables.getJSONObject("A");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", aVariables.getString("id"));
            jsonObject.put("channel", aVariables.getJSONObject("channel"));
            jsonObject.put("channelServiceType", aVariables.getString("channelServiceType"));
            jsonObject.put("category", aVariables.getJSONObject("category"));
            jsonObject.put("tr", tr);
            jsonObject.put("planNo", "");
            jsonObject.put("referer", referer);

            body = jsonObject.toString().replaceAll("\\\\" ,"");
//            resultObj.toString().replaceAll("\"\\[" ,"\\[").replaceAll("\\]\"" ,"\\]").replaceAll("\\\\" ,"");
//            Log.d("JSON", body);

        /*
{
  "id": "5934448641",
  "channel": {
    "accountNo": 100461897,
    "channelNo": "100508024",
    "channelName": "아리아리 컴퍼니",
    "representName": "아리아리",
    "channelSiteUrl": "365ari",
    "channelSiteFullUrl": "https://smartstore.naver.com/365ari",
    "channelSiteMobileUrl": "https://m.smartstore.naver.com/365ari",
    "accountId": "ncp_1nt8w9_01",
    "naverPaySellerNo": "510471793",
    "sellerExternalStatusType": "NORMAL",
    "logoWidth": 160,
    "logoHeight": 160,
    "logoUrl": "http://shop1.phinf.naver.net/20191014_231/1571055916534oAfxp_JPEG/8419305078254180_1623670905.jpg",
    "channelTypeCode": "STOREFARM"
  },
  "channelServiceType": "STOREFARM",
  "category": {
    "categoryId": "50002830",
    "categoryName": "거치대",
    "category1Id": "50000007",
    "category2Id": "50000161",
    "category3Id": "50001109",
    "category4Id": "50002830",
    "category1Name": "스포츠/레저",
    "category2Name": "자전거",
    "category3Name": "자전거용품",
    "category4Name": "거치대",
    "wholeCategoryId": "50000007>50000161>50001109>50002830",
    "wholeCategoryName": "스포츠/레저>자전거>자전거용품>거치대",
    "categoryLevel": 4,
    "lastLevel": true,
    "sortOrder": 1,
    "validCategory": true,
    "receiptIssue": true,
    "exceptionalCategoryTypes": [
      "FREE_RETURN_INSURANCE",
      "REGULAR_SUBSCRIPTION"
    ]
  },
  "tr": "sls",
  "planNo": "",
  "referer": "https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m&query=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80"
}
         */

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return body;
    }

    public static String makeCompanyListApiBody(String mid, String sort, String purchaseConditionSequence, int lowestPrice, int page, String query, String napm, boolean hasOfficialOrCertified) {
        String body = null;
        String baseString = "{\"query\":\"\\n      \\n  fragment DiscountSummaryItem on DiscountSummaryItem {\\n    itemId\\n    itemNo\\n    productClassType\\n    itemDiscountAmount\\n  }\\n\\n  fragment DiscountSummary on DiscountSummary {\\n    policyUniqueId\\n    discountAmount\\n    selectedItems {\\n      ...DiscountSummaryItem\\n    }\\n  }\\n\\n  fragment DiscountInfo on DiscountInfo {\\n    unitType\\n    discountValue\\n    minOrderAmount\\n    maxDiscountAmount\\n  }\\n\\n  fragment CouponValidPeriodInfo on CouponValidPeriodInfo {\\n    couponValidPeriodType\\n    validDay\\n    issueValidStartDateTime\\n    issueValidEndDateTime\\n    couponNo\\n  }\\n\\n  fragment CouponPolicy on CouponPolicy {\\n    policySeq\\n    cardCompanyCode\\n    cardProductGroupCode\\n    isAffiliateCard\\n    couponPublicationNo\\n    displayName\\n    displayDescription\\n    discountProviderType\\n    applyObjectType\\n    benefitTargetType\\n    customerLimitType\\n    usageConditionType\\n    interestTypes\\n    benefitStartDateTime\\n    benefitEndDateTime\\n    multiIssueCount\\n    isIssuable\\n    userUsableCouponQuantity\\n    policyIssuableCouponQuantity\\n    discountInfo {\\n      ...DiscountInfo\\n    }\\n    couponValidPeriodInfo {\\n      ...CouponValidPeriodInfo\\n    }\\n    registeredDateTime\\n    startDateTime\\n    endDateTime\\n    overlapParticipationType\\n    maxOverlapCount\\n    remainParticipationCount\\n    isUserRegisteredCard\\n    userParticipation\\n  }\\n\\n  fragment DiscountInfoWithPolicy on DiscountInfoWithPolicy {\\n    summary {\\n      ...DiscountSummary\\n    }\\n    couponPolicy {\\n      ...CouponPolicy\\n    }\\n  }\\n\\n  fragment CouponDiscountInfo on CouponDiscountInfo {\\n    totalCouponDiscountPrice\\n    productDiscountInfos {\\n      ...DiscountInfoWithPolicy\\n    }\\n    productDuplicationDiscountInfos {\\n      ...DiscountInfoWithPolicy\\n    }\\n    storeDiscountInfo {\\n      ...DiscountInfoWithPolicy\\n    }\\n  }\\n\\n\\n      query CatalogProducts($param: catalog_ProductParam, $exposeReportParam: ExposeReportParam) {\\n        catalog_Products(param: $param, exposeReportParam: $exposeReportParam) {\\n          totalCount\\n          pagingCount\\n          products {\\n            nvMid\\n            cardName\\n            channelName\\n            mallName\\n            mallPid\\n            productName\\n            deliveryFee\\n            deliveryContent\\n            regularDeliveryContent\\n            dawnDeliveryContent\\n            fastDeliveryContent\\n            rmid\\n            pcPrice\\n            price\\n            listPrice\\n            mobileProductUrl\\n            cardPrice\\n            defaultPayType\\n            unableProductNaverPay\\n            unableAccumProductNaverPay\\n            preorderCont\\n            mobileReviewCount\\n            purchaseCount\\n            foldingCount\\n            mallSequence\\n            windowVerticalCode\\n            windowVerticalName\\n            savingPoint\\n            savingStorePoint\\n            savingContentParsed {\\n              name\\n              point\\n            }\\n            savingContent\\n            couponContent\\n            promotionContent\\n            differentDeliveryFeeContent\\n            categoryId\\n            channelSequence\\n            matchNvMid\\n            shopN\\n            adsrType\\n            naverPaySellerId\\n            naverPaySaveRatio\\n            rentalContent\\n            eventText\\n\\n            isOfficialMall\\n            isAutoStdValue\\n            isAutoStdOptionUsed\\n            isMobileNaverPay\\n            isBrandCertification\\n            benefitPrice\\n            listPrice\\n            myDiscountPrice\\n            couponDiscountPrice\\n            couponDiscountRatio\\n            lgstDlvCont\\n            istlCont\\n            installation {\\n              isAddInstallFee\\n              isInstall\\n            }\\n            orgncMallPid\\n            mallId\\n            sellerDeliveryInfo {\\n              sellerArrivalTime\\n              sellerOrderClosingTime\\n              hasError\\n            }\\n            parsedDeliveryContents {\\n              deliveryType\\n              orderClosingTime\\n              deliveryMethod\\n              estimatedDeliveryDate\\n              exchangeFee\\n              returnFee\\n              noDeliveryDateList\\n              deliveryAgent\\n            }\\n            contractConditionId\\n            condProdTpCd\\n            crUrl\\n            membershipBenefit\\n            isRegularDelivery\\n            isMemberDeliveryFree\\n            stdGrpId\\n            stdPrchOptGuideId\\n            stdPrchOptCont\\n            couponDiscountInfo {\\n              ...CouponDiscountInfo\\n            }\\n          }\\n        }\\n      }\\n    \",\"variables\":{\"param\":{\"nvMid\":\"30405967298\",\"deliveryCharge\":true,\"cardDiscount\":false,\"purchaseConditionSequence\":\"20051177\",\"page\":1,\"pageSize\":20,\"deliveryToday\":false,\"deviceType\":\"MOBILE\",\"isOfficialOrCerti\":false,\"isViewAllEnable\":false,\"cCatalogId\":\"30405967298\"},\"exposeReportParam\":{\"query\":\"휴대폰 케이블\",\"fromWhere\":\"CATALOG\",\"napm\":\"ct=md2y9h80|ci=e9883ef0f52915aec2190260ac54e3069d9dc5b8|tr=slsl|sn=95694|hk=08b7b15830b70e7a495837faeaa9aceb6425c38c\",\"catalogType\":\"BRAND\",\"providerTypeCode\":\"P13001\",\"naverAnalyticsAccountId\":\"\",\"hasOfficialOrCertified\":false,\"officialOrCertiTabType\":\"ALL\"}},\"operationName\":\"CatalogProducts\"}";

        try {
//            Log.d("JSON", baseString);
            JSONObject jsonObject = new JSONObject(baseString);
            JSONObject objVariables = jsonObject.getJSONObject("variables");
            JSONObject paramVariables = objVariables.getJSONObject("param");
            JSONObject exposeReportParamVariables = objVariables.getJSONObject("exposeReportParam");

            paramVariables.put("nvMid", mid);
            paramVariables.put("cCatalogId", mid);
            paramVariables.put("page", page);

            if (TextUtils.isEmpty(purchaseConditionSequence)) {
                paramVariables.remove("purchaseConditionSequence");
            } else {
                paramVariables.put("purchaseConditionSequence", purchaseConditionSequence);
            }

            exposeReportParamVariables.put("query", query);

            if (TextUtils.isEmpty(napm)) {
                exposeReportParamVariables.remove("napm");
            } else {
                exposeReportParamVariables.put("napm", napm);
            }

//            exposeReportParamVariables.put("hasOfficialOrCertified", hasOfficialOrCertified);

            body = jsonObject.toString();
//            Log.d("JSON", body);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return body;
    }

    public static String makeCeApiBody(String urlString) {
        String body = "[]";

        try {
            URL url = new URL(urlString);
            String value = url.getQuery();

            if (value.startsWith("x=")) {
                value = value.substring(2);
            }

            body = "[\"" + value + "\"]";
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return body;
    }

    public String requestUrl(String url, String accept, String mediaType, String fetchMode, String fetchDest) {
        _url = url;

        // 원래는 이게 아니라 doc 타입일때만 현재 주소값을 설정해야한다.
        if (TextUtils.isEmpty(accept)) {
            _currentUrl = url;
        }
        boolean isSsl = url.startsWith("https");
//        String ua = UserManager.getInstance().ua;
//        String nnb = UserManager.getInstance().nnb;
        Log.d(TAG, "nnb: " + _nnb + " / ua: " + _ua);
        String data = null;

        HttpURLConnection conn = null;
        Map<String, String> addHeaders;

        if (_useGenHeader) {
            addHeaders = genHeader(isSsl, accept, mediaType, fetchMode, fetchDest);
        } else {
            addHeaders = new HashMap<>();

            for (Map.Entry<String, String> addedHeader : _addedHeaders.entrySet()) {
                String name = addedHeader.getKey();
                String value = addedHeader.getValue();
                addHeaders.put(name, value);
            }
        }

        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            for (Map.Entry<String, String> entry : addHeaders.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
//                conn.setRequestProperty(entry.getKey().toLowerCase(), entry.getValue());
            }

            Log.i("ttt", "[" + url + "] complete request header: " + conn.getRequestProperties());
            conn.setDoOutput(false);
            conn.setDoInput(true);

            final long startMillis = System.currentTimeMillis();

            Log.v("ttt Response", "HttpURLConnection [" + url + "] headers: " + conn.getHeaderFields());
            final long dtMillis = System.currentTimeMillis() - startMillis;
            Log.d(TAG, "Got response: after " + dtMillis + "ms");

            Map<String, String> responseHeaders = convertResponseHeaders(url, conn.getHeaderFields());
            String contentType = null;
            String mimeType = null;
            String encoding = null;
            int responseCode = 0;
            String message = null;
            InputStream stream = null;

            encoding = conn.getContentEncoding();
            responseCode = conn.getResponseCode();
            _responseCode = responseCode;
            message = conn.getResponseMessage();
            stream = conn.getInputStream();

            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");

            if (cookiesHeader != null) {
                for (String cookieString : cookiesHeader) {
                    List<HttpCookie> cookieList = HttpCookie.parse(cookieString);

                    for (HttpCookie cookie : cookieList) {
                        Log.d(TAG, "cookie value: " + cookie.getValue() + " / " + cookie.toString());
//                        cookieManager.setCookie(cookie.getDomain(), cookieString);
                    }
                }
            }

            if ("gzip".equals(encoding)) {
                Log.d("ttt Response", "gzip 파서!");
                GZIPInputStream gzipInputStream = new GZIPInputStream(stream);
                data = streamToString(gzipInputStream);
                gzipInputStream.close();
            } else if ("br".equals(encoding)) {
                Log.d("ttt Response", "brotli 파서!");
                BrotliCompressorInputStream compressorInputStream = new BrotliCompressorInputStream(stream);
                data = streamToString(compressorInputStream);
                compressorInputStream.close();
            } else {
                Log.d("ttt Response", "일반 파서!");
                data = streamToString(stream);
            }

            stream.close();
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "SocketTimeoutException: " + e);
            e.printStackTrace();
//            SystemClock.sleep(5000);
//            return loadWithOkHttpClient(webView, request, url, recursiveCount - 1);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e);
            e.printStackTrace();
//            SystemClock.sleep(5000);
//            return loadWithOkHttpClient(webView, request, url, recursiveCount - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public Map<String, String> genHeader(boolean isSsl, String accept, String contentType, String fetchMode, String fetchDest) {
        Map<String, String> addHeaders = new HashMap<>();

//        accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
//accept-encoding:gzip, deflate, br
//accept-language:ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7
//cookie:NNB=65PZYMWNYW5GG
//sec-ch-ua:"Not?A_Brand";v="8", "Chromium";v="108", "Google Chrome";v="108"
//sec-ch-ua-mobile:?1
//sec-ch-ua-platform:"Android"
//sec-fetch-dest:document
//sec-fetch-mode:navigate
//sec-fetch-site:none
//sec-fetch-user:?1
//upgrade-insecure-requests:1
//user-agent:Mozilla/5.0 (Linux; Android 10; SM-N960N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Mobile Safari/537.36

//        addOkHttpHeaders(currentUrl, originalUrl, request, new URL(url), builder);

        if (TextUtils.isEmpty(accept)) {
            accept = HttpHeader.getAccept(_ua);
        }

        addHeaders.put("Accept", accept);

        if (!_ua.contains("SamsungBrowser") && !TextUtils.isEmpty(fetchDest) && fetchDest.equals("iframe")) {
            addHeaders.put("Accept-Encoding", "gzip, deflate");
        } else {
            addHeaders.put("Accept-Encoding", "gzip, deflate, br, zstd");
        }

        addHeaders.put("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");

        if (!TextUtils.isEmpty(contentType)) {
            addHeaders.put("Content-Type", contentType);
        }

//        if (!TextUtils.isEmpty(nnb)) {
//            addHeaders.put("Cookie", "NNB=" + nnb);
//        }

//        CookieManager cookieManager = CookieManager.getInstance();
//        String cookies = cookieManager.getCookie(url);
//        Log.d(TAG, "host: " + url + ", cookies: " + cookies);
//
//        if (!TextUtils.isEmpty(cookies)) {
//            addHeaders.put("Cookie", cookies);
//        }

        if (!TextUtils.isEmpty(_origin)) {
            addHeaders.put("Origin", _origin);
        }

        if (!TextUtils.isEmpty(_referer)) {
            addHeaders.put("Referer", _referer);
        }

        if (isSsl && !TextUtils.isEmpty(_ua)) {
            Map<String, String> chUa = HttpHeader.getSecChUa(_ua, _chromeVersion, _browserVersion, _isPc, _useDetailChUa, false);
            _useDetailChUa = false;
            addHeaders.putAll(chUa);

            if (_ua.contains("SamsungBrowser")) {
                addHeaders.put("DNT", "1");
            }
        }

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

        if (TextUtils.isEmpty(fetchMode)) {
            addHeaders.put("Sec-Fetch-Dest", "document");
            addHeaders.put("Sec-Fetch-Mode", "navigate");

            if (isSsl) {
                addHeaders.put("Sec-Fetch-User", "?1");
                addHeaders.put("Upgrade-Insecure-Requests", "1");
            }
        } else {
            if (TextUtils.isEmpty(fetchDest)) {
                addHeaders.put("Sec-Fetch-Dest", "empty");
            } else {
                addHeaders.put("Sec-Fetch-Dest", fetchDest);
            }

            addHeaders.put("Sec-Fetch-Mode", fetchMode);

            if (!TextUtils.isEmpty(fetchDest) && fetchDest.equals("iframe")) {
                addHeaders.put("Upgrade-Insecure-Requests", "1");
            }
        }

//        if (!TextUtils.isEmpty(secFetchSite)) {
//            addHeaders.put("Sec-Fetch-Site", secFetchSite);
//        } else {
            addHeaders.put("Sec-Fetch-Site", "none");
//        }

        addHeaders.put("User-Agent", _ua);

        for (Map.Entry<String, String> addedHeader : _addedHeaders.entrySet()) {
            String name = addedHeader.getKey();
            String value = addedHeader.getValue();
            addHeaders.put(name, value);
        }

        if (_ua.contains("wv")) {
            // 강제로 캐시슬라이드로 설정.
//            addHeaders.put("X-Requested-With", "com.cashslide");
//            addHeaders.put("X-Requested-With", "com.cashwalk.cashwalk");
        }

        return addHeaders;
    }

    public String getResponseData(Response response) throws IOException {
        String data = null;
        String encoding = response.header("Content-Encoding", null);
        InputStream stream = response.body().byteStream();

        if ("gzip".equals(encoding)) {
            Log.d("ttt Response", "gzip 파서!");
            GZIPInputStream gzipInputStream = new GZIPInputStream(stream);
            data = streamToString(gzipInputStream);
            gzipInputStream.close();
        } else if ("br".equals(encoding)) {
            Log.d("ttt Response", "brotli 파서!");
            BrotliCompressorInputStream compressorInputStream = new BrotliCompressorInputStream(stream);
            data = streamToString(compressorInputStream);
            compressorInputStream.close();
        } else {
            Log.d("ttt Response", "일반 파서!");
            data = streamToString(stream);
        }

        stream.close();

        return data;
    }

    public String requestUrlWithOkHttpClient(String url, String secFetchSite) {
        return requestUrlWithOkHttpClient(url, null, null, null, null);
    }

    public String requestUrlWithOkHttpClient(String url) {
        return requestUrlWithOkHttpClient(url, null, null, null, null);
    }

    public String requestUrl(String url) {
        return requestUrl(url, null, null, null, null);
    }

    public String requestUrlFetch(String url) {
        return requestUrl(url, "application/json, text/plain, */*", null, "cors", null);
    }

    public String requestUrlWithOkHttpClientFetch(String url) {
        return requestUrlWithOkHttpClient(url, "application/json, text/plain, */*", null, "cors", null);
    }

    public String requestUrlWithOkHttpClient(String url, String accept, String mediaType, String fetchMode, String fetchDest) {
        _url = url;

        // 원래는 이게 아니라 doc 타입일때만 현재 주소값을 설정해야한다.
//        if (TextUtils.isEmpty(accept)) {
//            _currentUrl = url;
//        }
        boolean isSsl = url.startsWith("https");
//        String ua = UserManager.getInstance().ua;
//        String nnb = UserManager.getInstance().nnb;
        Log.d(TAG, "nnb: " + _nnb + " / ua: " + _ua);
        String data = null;

        Request.Builder builder = new Request.Builder()
                .url(url.trim());

        Map<String, String> addHeaders;

        if (_useGenHeader) {
            addHeaders = genHeader(isSsl, accept, mediaType, fetchMode, fetchDest);
        } else {
            addHeaders = new HashMap<>();

            for (Map.Entry<String, String> addedHeader : _addedHeaders.entrySet()) {
                String name = addedHeader.getKey();
                String value = addedHeader.getValue();
                addHeaders.put(name, value);
            }
        }

        for (Map.Entry<String, String> entry : addHeaders.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
//            builder.addHeader(entry.getKey().toLowerCase(), entry.getValue());
        }

        try {
            Request httpRequest = builder.build();
            Log.i("ttt", "[" + url + "] complete request header: " + getHeadersLog(httpRequest.headers()));

            final long startMillis = System.currentTimeMillis();
            Response response = _httpClient.newCall(httpRequest).execute();
            final long dtMillis = System.currentTimeMillis() - startMillis;
            Log.d(TAG, "Got response: after " + dtMillis + "ms");
            _responseCode = response.code();

            Map<String, String> responseHeaders = convertResponseHeaders(url, response.headers().toMultimap());
            Log.w("ttt Response", response.toString());
            Log.d("ttt Response", "OkHttpClient [" + url + "] headers: " + responseHeaders);

//            Map<String, List<String>> headerFields = response.headers().toMultimap();
//            List<String> cookiesHeader = headerFields.get("Set-Cookie");
//
//            if (cookiesHeader != null) {
//                for (String cookieString : cookiesHeader) {
//                    List<HttpCookie> cookieList = HttpCookie.parse(cookieString);
//
//                    for (HttpCookie cookie : cookieList) {
//                        Log.d(TAG, "cookie value: " + cookie.getValue() + " / " + cookie.toString());
//                        cookieManager.setCookie(cookie.getDomain(), cookieString);
//                    }
//                }
//            }

            data = getResponseData(response);
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "SocketTimeoutException: " + e);
            e.printStackTrace();
//            SystemClock.sleep(5000);
//            return loadWithOkHttpClient(webView, request, url, recursiveCount - 1);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e);
            e.printStackTrace();
//            SystemClock.sleep(5000);
//            return loadWithOkHttpClient(webView, request, url, recursiveCount - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public String requestUrlWithOkHttpClientImage(String url) {
        boolean isSsl = url.startsWith("https");
//        String ua = UserManager.getInstance().ua;
//        String nnb = UserManager.getInstance().nnb;
        Log.d(TAG, "nnb: " + _nnb + " / ua: " + _ua);
        String data = null;

        Request.Builder builder = new Request.Builder()
                .url(url.trim());

        Map<String, String> addHeaders = genHeader(isSsl, "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8", null, "no-cors", "image");

        for (Map.Entry<String, String> entry : addHeaders.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
//            builder.addHeader(entry.getKey().toLowerCase(), entry.getValue());
        }

        try {
            Request httpRequest = builder.build();
            Log.i("ttt", "[" + url + "] complete request header: " + getHeadersLog(httpRequest.headers()));

            final long startMillis = System.currentTimeMillis();
            Response response = _httpClient.newCall(httpRequest).execute();
            final long dtMillis = System.currentTimeMillis() - startMillis;
            Log.d(TAG, "Got response: after " + dtMillis + "ms");
            _responseCode = response.code();

            Map<String, String> responseHeaders = convertResponseHeaders(url, response.headers().toMultimap());
            Log.w("ttt Response", response.toString());
            Log.d("ttt Response", "OkHttpClient [" + url + "] headers: " + responseHeaders);

//            Map<String, List<String>> headerFields = response.headers().toMultimap();
//            List<String> cookiesHeader = headerFields.get("Set-Cookie");
//
//            if (cookiesHeader != null) {
//                for (String cookieString : cookiesHeader) {
//                    List<HttpCookie> cookieList = HttpCookie.parse(cookieString);
//
//                    for (HttpCookie cookie : cookieList) {
//                        Log.d(TAG, "cookie value: " + cookie.getValue() + " / " + cookie.toString());
//                        cookieManager.setCookie(cookie.getDomain(), cookieString);
//                    }
//                }
//            }

            data = getResponseData(response);
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "SocketTimeoutException: " + e);
            e.printStackTrace();
//            SystemClock.sleep(5000);
//            return loadWithOkHttpClient(webView, request, url, recursiveCount - 1);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e);
            e.printStackTrace();
//            SystemClock.sleep(5000);
//            return loadWithOkHttpClient(webView, request, url, recursiveCount - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public String requestUrlPostWithOkHttpClientJson(String url, String json) {
        return requestUrlPostWithOkHttpClient(url, "application/json", "cors", json);
    }

    public String requestUrlPostWithOkHttpClientText(String url, String text) {
        return requestUrlPostWithOkHttpClient(url, "text/plain;charset=UTF-8", "no-cors", text);
    }

    public String requestUrlPostWithOkHttpClient(String url, String mediaType, String fetchMode, String body) {
        return requestUrlPostWithOkHttpClient(url, "*/*", mediaType, fetchMode, body);
    }

    public String requestUrlPostWithOkHttpClient(String url, String accept, String mediaType, String fetchMode, String json) {
        _url = url;
        boolean isSsl = url.startsWith("https");
//        String ua = UserManager.getInstance().ua;
//        String nnb = UserManager.getInstance().nnb;
        Log.d(TAG, "nnb: " + _nnb + " / ua: " + _ua);
        String data = null;

        Request.Builder builder = new Request.Builder()
                .url(url.trim());

        Map<String, String> addHeaders = genHeader(isSsl, accept, mediaType, fetchMode, null);

        for (Map.Entry<String, String> entry : addHeaders.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
//            builder.addHeader(entry.getKey().toLowerCase(), entry.getValue());
        }

        RequestBody body = RequestBody.create(MediaType.parse(mediaType), json);
        builder.post(body);

        try {
            Request httpRequest = builder.build();
            Log.i("ttt", "[" + url + "] complete request header: " + getHeadersLog(httpRequest.headers()));

            final long startMillis = System.currentTimeMillis();
            Response response = _httpClient.newCall(httpRequest).execute();
            final long dtMillis = System.currentTimeMillis() - startMillis;
            Log.d(TAG, "Got response: after " + dtMillis + "ms");
            _responseCode = response.code();

            Map<String, String> responseHeaders = convertResponseHeaders(url, response.headers().toMultimap());
            Log.w("ttt Response", response.toString());
            Log.d("ttt Response", "OkHttpClient [" + url + "] headers: " + responseHeaders);

//            Map<String, List<String>> headerFields = response.headers().toMultimap();
//            List<String> cookiesHeader = headerFields.get("Set-Cookie");
//
//            if (cookiesHeader != null) {
//                for (String cookieString : cookiesHeader) {
//                    List<HttpCookie> cookieList = HttpCookie.parse(cookieString);
//
//                    for (HttpCookie cookie : cookieList) {
//                        Log.d(TAG, "cookie value: " + cookie.getValue() + " / " + cookie.toString());
//                        cookieManager.setCookie(cookie.getDomain(), cookieString);
//                    }
//                }
//            }

            data = getResponseData(response);
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "SocketTimeoutException: " + e);
            e.printStackTrace();
//            SystemClock.sleep(5000);
//            return loadWithOkHttpClient(webView, request, url, recursiveCount - 1);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e);
            e.printStackTrace();
//            SystemClock.sleep(5000);
//            return loadWithOkHttpClient(webView, request, url, recursiveCount - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public static String removeQueryFromUrlString(String url) {
        return url.split("\\?")[0];
    }

    public static Map<String, String> query2MapFromUrlString(String url) {
        String newURL = url;
        int index = url.indexOf('?');

        if (index > -1) {
            newURL = url.substring(index + 1);
        }

        return query2Map(newURL);
    }

    public static Map<String, String> query2Map(String query) {
        Map<String, String> map = new LinkedHashMap<>();
        String[] splitStrings = query.split("&");

        for (String string : splitStrings) {
            String[] keyValues = string.split("=");

            if (keyValues.length > 1) {
                map.put(keyValues[0], keyValues[1]);
            } else {
                map.put(keyValues[0], "");
            }
        }

        return map;
    }

    public String getValueFromHtml(String htmlString, String key) {
        return getValueFromHtml(htmlString, key, "=", ";", true);
    }

    public String getValueFromHtml(String htmlString, String key, String div, String endString) {
        return getValueFromHtml(htmlString, key, div, endString, true);
    }
    public String getValueFromHtml(String htmlString, String key, String div, String endString, boolean removeQuotes) {
        if (TextUtils.isEmpty(htmlString)) {
            return "";
        }

        String value = "";
        int beginIndex = htmlString.indexOf(key);

        if (beginIndex >= 0) {
            int endIndex = htmlString.indexOf(endString, beginIndex + 1);

            if (endIndex > 0) {
                String part = htmlString.substring(beginIndex, endIndex);
                String[] parts = part.split(div, 2);
                if (parts.length > 1) {
                    String find = parts[1].trim();

                    if (removeQuotes) {
                        find = find.replace("\"", "");
                    }

                    value = find;
                }
            }
        }

        return value;
    }

    public String getStringFromSource(String source, String from, String to) {
        if (TextUtils.isEmpty(source)) {
            return null;
        }

        String value = null;
        int beginIndex = source.indexOf(from);

        if (beginIndex >= 0) {
            int endIndex = source.indexOf(to, beginIndex + 1);

            if (endIndex > 0) {
                value = source.substring(beginIndex + 1, endIndex);
            }
        }

        return value;
    }

    private String streamToString(InputStream source) throws IOException {
//        InputStream in = new BufferedInputStream(source);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;

        while ((len = source.read(buffer)) > -1) {
            outputStream.write(buffer, 0, len);
        }

        outputStream.flush();
        String output = outputStream.toString();
        outputStream.close();

        return output;
    }

    public Headers headerReorder(Request.Builder builder, boolean secure) {
        // 헤더 순서 재정렬.
        Request newRequest = builder.build();
        Headers headers = newRequest.headers();
        Headers.Builder builder1 = new Headers.Builder();

//                for (String name : headers.names()) {
////                    Log.d(TAG, "##### header: " + name + ": " + headers.get(name));
//                    builder1.add(name, headers.get(name));
//                }

        Set<String> stringSet = new TreeSet<>(headers.names());
//                stringSet.remove()

        String[] fieldNameArray = {"Host", "Connection", "Content-Length",
                "sec-ch-ua", "sec-ch-ua-mobile",
                "sec-ch-ua-arch",
                "sec-ch-ua-platform",
                "sec-ch-ua-platform-version", "sec-ch-ua-model", "sec-ch-ua-bitness", "sec-ch-ua-wow64", "sec-ch-ua-full-version-list",
                "Upgrade-Insecure-Requests", "User-Agent", "Content-Type",
                "Accept",
                "Origin",
                "Sec-Fetch-Site", "Sec-Fetch-Mode", "Sec-Fetch-User", "Sec-Fetch-Dest",
                "Referer",
                "Accept-Encoding", "Accept-Language",
                "Cookie",
        };

        for (String fieldName : fieldNameArray) {
            if (stringSet.contains(fieldName)) {
                String value = headers.get(fieldName);

                if (!TextUtils.isEmpty(value) && value.equals("Keep-Alive")) {
                    value = value.toLowerCase();
                }

                builder1.add(secure ? fieldName.toLowerCase() : fieldName, value);
                stringSet.remove(fieldName);
            }
        }

        for (String name : stringSet) {
            String value = headers.get(name);
//                    Log.d(TAG, "##### header2: " + name + ": " + headers.get(name));

            if (secure) {
                name = name.toLowerCase();
            }

            builder1.add(name, value);
        }

        return builder1.build();
    }

    public static String getHeadersLog(Headers headers) {
        StringBuilder logString = new StringBuilder();
        Map<String, List<String>> headersMap = headers.toMultimap();

        for (Map.Entry<String, List<String>> item : headersMap.entrySet()) {
            String key = item.getKey();
            String value = TextUtils.join("\n,", item.getValue());

            if (key.equalsIgnoreCase("cookie")) {
                value = value.replace("; \n", "\n");
                value = value.replace("; ", "\n");
            }

            logString.append(key);
            logString.append(": ");
            logString.append(value);
            logString.append("\n");
        }

        return logString.toString();
//        return headers.toString();
    }

    public class AddCookiesInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Connection connection = chain.connection();
            Request request = chain.request();
            Request.Builder builder = request.newBuilder();
            boolean secure = false;

            if (connection != null) {
                Log.d(TAG, "protocol: " + connection.protocol() + " / handshake: " + connection.handshake() + " / route: " + connection.route());
                if (connection.protocol().equals(Protocol.HTTP_2)) {
                    secure = true;
                }
            }

            // 헤더 정보 입력.
            String refererKey = "Referer";
            String secFetchSiteKey = "Sec-Fetch-Site";

//            if (request.url().isHttps()) {
//                refererKey = refererKey.toLowerCase();
//                secFetchSiteKey = secFetchSiteKey.toLowerCase();
//            }

            if (request.header("Sec-Fetch-User") != null) {
                _currentUrl = request.url().toString();
                Log.d(TAG, "current: " + _currentUrl);
            }

            if (!TextUtils.isEmpty(_url) && !_url.equals(_currentUrl)) {
                builder.removeHeader("sec-ch-ua-arch");
                builder.removeHeader("sec-ch-ua-bitness");
                builder.removeHeader("sec-ch-ua-full-version-list");
                builder.removeHeader("sec-ch-ua-model");
                builder.removeHeader("sec-ch-ua-platform-version");
                builder.removeHeader("sec-ch-ua-wow64");
            }

            List<String> refererList = request.headers(refererKey);
            String referer = null;

            if (!refererList.isEmpty()) {
                referer = request.headers(refererKey).get(0);
            }

            if (_useGenHeader) {
                builder.header(secFetchSiteKey, HttpHeader.getSecFetchSite(request.url().toString(), referer));
            }

            if (secure) {
                builder.removeHeader("Connection");
            }

            if (useCookieManager) {
                CookieManager cookieManager = CookieManager.getInstance();
                String cookies = cookieManager.getCookie(request.url().toString());
                Log.d(TAG, "host: " + request.url().toString() + ", cookies: " + cookies);

                if (!TextUtils.isEmpty(cookies)) {
                    builder.addHeader("Cookie", cookies);
                }

                // 헤더 순서 재정렬.
                builder.headers(headerReorder(builder, secure));

                Response response = chain.proceed(builder.build());

                Map<String, List<String>> headerFields = response.headers().toMultimap();
                List<String> cookiesHeader = headerFields.get("Set-Cookie");

                if (cookiesHeader != null) {
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

                return response;
            } else {
//                HttpCookieManager cookieManager = HttpCookieManager.getInstance();
                HttpCookieController cookieManager = _cookieController;
                String cookies = cookieManager.getCookie(request.url().toString());
                Log.d(TAG, "host: " + request.url().toString() + ", cookies: " + cookies);

                if (!TextUtils.isEmpty(cookies)) {
                    builder.addHeader("Cookie", cookies);
                }

                // 헤더 순서 재정렬.
                builder.headers(headerReorder(builder, secure));

                Response response = chain.proceed(builder.build());

                Map<String, List<String>> headerFields = response.headers().toMultimap();
                List<String> cookiesHeader = headerFields.get("Set-Cookie");

                if (cookiesHeader != null) {
                    for (String cookieString : cookiesHeader) {
                        setCookieForList(cookieString);
                        List<HttpCookie> cookieList = HttpCookie.parse(cookieString);

                        for (HttpCookie cookie : cookieList) {
                            Log.d(TAG, "cookie value: " + cookie.getValue() + " / " + cookie.toString() + " / " + cookie.getDomain() + " / " + cookieString);

                            if (!TextUtils.isEmpty(cookie.getDomain())) {
                                String[] values = cookieString.split(";");

                                if (!cookie.hasExpired()) {
                                    cookieManager.setCookie(cookie.getDomain(), values[0].trim());
                                } else {
                                    cookieManager.deleteCookie(cookie.getDomain(), values[0].trim());
                                }
                            }
                        }
                    }
                }

                return response;
            }
        }
    }

    static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Log.i("LoggingInterceptor", String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Log.i("LoggingInterceptor", String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            return response;
        }
    }
}
