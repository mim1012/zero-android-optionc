package com.sec.android.app.sbrowser.pattern.naver.packet;

import android.text.TextUtils;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpEngine;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpHeader;
import com.sec.android.app.sbrowser.engine.WebEngine.WebPageData;
import com.sec.android.app.sbrowser.pattern.BasePatternAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

public class NaverPacketCommon extends NaverPacket {

    private static final String TAG = NaverPacketCommon.class.getSimpleName();

    private HttpEngine _httpEngine = null;
    private WebPageData _pageData = null;
    private BasePatternAction _action = null;

    public NaverPacketCommon(HttpEngine httpEngine, WebPageData pageData) {
        super();

        _httpEngine = httpEngine;
        _pageData = pageData;
    }

    public void setPatternAction(BasePatternAction action) {
        _action = action;
    }

    public String getLcsUrl(String url, String sti) {
        return "https://lcs.naver.com/m?" + getParams(url, sti, false, false);
    }

    public String getParams(String urlString, String sti, boolean hasBrands, boolean isSlc) {
        String htmlString = _pageData.htmlString;
        String referer = _pageData.docReferer;

        Log.d(TAG, "getParams: " + urlString + ", " + referer);
        StringBuilder paramString = new StringBuilder();

        if (TextUtils.isEmpty(referer)) {
            referer = "";
        }

        try {
            String _ua = _httpEngine.getUa();
            String _nnb = _httpEngine.getNnb();

            paramString.append("u=").append(URLEncoder.encode(urlString, "UTF-8"));
            // e: referer
            paramString.append("&e=").append(URLEncoder.encode(referer, "UTF-8"));

            // lcs_bc
            String os = null;
            String ln = null;
            String sr = null;
            String pr = null;
            String bw = null;
            String bh = null;
            String c = null;
            String j = null;
            String k = null;

            // lcs_add
            String i = null;        // ""
            String ls = null;       // nnb
            String ct = null;       // connection.type
            String ect = null;      // connection.effectiveType

            // lcs_perf
            // - performance.timing

            // - performance.getEntriesByType("paint")
            String firstPaint = null;               // first-paint: performanceEntry.startTime
            String firstContentfulPaint = null;     // first-contentful-paint: performanceEntry.startTime

            String ngt = null;                      // performance.navigation.type

            // etc
            String pid = null;                      //
            String ssc = null;                      //

            String ts = null;                       // time

            if (_action != null) {
                // 소스 개선 필요. 위 각각의 변수를 배열로 만드는 것이 좋다.
                os = _action.getSystemValue("navigator.platform");

                if (os == null || os.equals("undefined")) {
                    os = "";
                }

                paramString.append("&os=").append(URLEncoder.encode(os, "UTF-8"));

                ln = _action.getSystemValue("navigator.userLanguage");

                if (ln == null || ln.equals("undefined")) {
                    ln = _action.getSystemValue("navigator.language");

                    if (ln == null || ln.equals("undefined")) {
                        ln = "";
                    }
                }

                paramString.append("&ln=").append(ln);

                sr = "";

                if (_action.getJsonSystemValue("window.screen") != null &&
                        !TextUtils.isEmpty(_action.getSystemValue("screen.width")) &&
                        !TextUtils.isEmpty(_action.getSystemValue("screen.height"))) {
                    sr = _action.getSystemValue("screen.width") + "x" + _action.getSystemValue("screen.height");
                }

                paramString.append("&sr=").append(sr);

                pr = _action.getSystemValue("window.devicePixelRatio");

                if (TextUtils.isEmpty(pr)) {
                    pr = "1";
                }

                paramString.append("&pr=").append(pr);

                bw = _action.getSystemValue("document.documentElement.clientWidth");

                if (TextUtils.isEmpty(bw)) {
                    bw = _action.getSystemValue("document.body.clientWidth");
                }

                paramString.append("&bw=").append(bw);

                bh = _action.getSystemValue("document.documentElement.clientHeight");

                if (TextUtils.isEmpty(bw)) {
                    bh = _action.getSystemValue("document.body.clientHeight");
                }

                paramString.append("&bh=").append(bh);

                c = "";     // 24

                if (_action.getJsonSystemValue("window.screen") != null) {
                    c = _action.getSystemValue("screen.colorDepth");

                    if (c == null || c.equals("undefined")) {
                        c = _action.getSystemValue("screen.pixelDepth");
                    }
                }

                paramString.append("&c=").append(c);

                j = "N";    // navigator.javaEnabled()
                paramString.append("&j=").append(j);

                k = "Y";    // navigator.cookieEnabled
                paramString.append("&k=").append(k);

                i = "";
                paramString.append("&i=").append(i);

                ls = _nnb;

                if (!TextUtils.isEmpty(ls)) {
                    paramString.append("&ls=").append(ls);
                }

                ct = null;
                ect = null;

                if (_action.getJsonSystemValue("navigator.connection") != null) {
                    String connectionType = _action.getSystemValue("navigator.connection.type");
                    String effectiveType = _action.getSystemValue("navigator.connection.effectiveType");

                    if (!TextUtils.isEmpty(connectionType) && !connectionType.equals("undefined")) {
                        ct = connectionType;
                        paramString.append("&ct=").append(ct);
                    }

                    if (!TextUtils.isEmpty(effectiveType) && !effectiveType.equals("undefined")) {
                        ect = effectiveType;
                        paramString.append("&ect=").append(ect);
                    }
                }

//                JSONObject timing = _action.getJsonSystemValue("window.performance.timing");
//
//                if (timing != null) {
//                    Iterator<String> it = timing.keys();
//                    while (it.hasNext()) {
//                        String key = it.next();
//                        try {
//                            Long value = timing.getLong(key);
//                            paramString.append("&").append(key).append("=").append(value);
//                        } catch (JSONException e) {
//                            Log.d(TAG, "timing: " + e.getMessage());
//                        }
//                    }
//                }

                String timingScript = "" +
                        " var paramString = '';" +
                        " var performance = window.performance || {};" +
                        " if (performance.timing) {" +
                        "     var pt = performance.timing;" +
                        "     for (var key in pt) {" +
                        "         var value = pt[key];" +
                        "         if (typeof value === \"number\" && value) {" +
                        "             paramString += '&' + key + '=' + value;" +
                        "         }" +
                        "     }" +
                        " }" +
                        " return paramString;";

                String timing = _action.getValueFromScript(timingScript);

                if (!TextUtils.isEmpty(timing)) {
                    paramString.append(timing);
                }

                firstPaint = "";
                firstContentfulPaint = "";

                JSONObject firstPaintObj = _action.getJsonSystemValue("{\"val\": window.performance.getEntriesByType(\"paint\")}");
                try {
                    JSONArray objs = firstPaintObj.getJSONArray("val");

                    for (int oi = 0; oi < objs.length(); ++oi) {
                        JSONObject row = objs.getJSONObject(oi);
                        String paintName = row.getString("name");

                        if (paintName.equals("first-paint") || paintName.equals("first-contentful-paint")) {
                            paramString.append("&").append(paintName).append("=").append(row.getString("startTime"));
                        }
                    }

                } catch (JSONException e) {
                    Log.d(TAG, "firstPaintObj: " + e.getMessage());
                }

                ngt = null;
                pid = null;
                ssc = null;

                paramString.append("&pid=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_puid"), "UTF-8"));
                paramString.append("&ssc=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_ssc"), "UTF-8"));

                Date mDate = new Date();
                long time = mDate.getTime();
                _pageData.performanceTime = time;

                paramString.append("&ts=").append(_pageData.performanceTime);

//                _action.getSystemValue("")
            } else {
                // TODO: 개선필요. 장비 부분, ua 따라 설정해줘야한다.
                //os: Linux aarch64
                //ln: ko-KR
                //sr: 412x869
                //pr: 2.625
                //bw: 412
                //bh: 722
                //c: 24

                os = "Linux%20armv8l";

                if (_ua.contains("SamsungBrowser")) {
                    os = "Linux%20aarch64";
                }

                paramString.append("&os=").append(os);
                paramString.append("&ln=").append(URLEncoder.encode("ko-KR", "UTF-8"));
                paramString.append("&sr=").append(URLEncoder.encode("360x640", "UTF-8"));
                paramString.append("&pr=").append("3");
                paramString.append("&bw=").append("360");
                paramString.append("&bh=").append(HttpHeader.getDeviceHeight(_ua));
                paramString.append("&c=").append("24");

                paramString.append("&j=N");
                paramString.append("&k=Y");
                paramString.append("&i=");

                if (!TextUtils.isEmpty(_nnb)) {
                    paramString.append("&ls=").append(_nnb);
                }

                Date mDate = new Date();
                long time = mDate.getTime();

                if (TextUtils.isEmpty(_pageData.performanceString)) {
                    getPerformanceString(sti);
                } else {
                    _pageData.performanceTime += MathHelper.randomRange(1, 4);
                }

                paramString.append(_pageData.performanceString);

                if (hasBrands) {
                    String uaParam = getUaParam(sti.equals("m_main_home"), false);

                    if (!TextUtils.isEmpty(uaParam)) {
                        paramString.append(uaParam);
                    }

                    paramString.append("&sti=").append(sti);

                    if (sti.equals("m_main_home")) {
                        paramString.append("&pid=").append(getPid(htmlString));
                        paramString.append("&ugr=").append("newmain");
                        paramString.append("&pmd=").append("home");
                    } else {
                        if (isSlc) {
                            if (sti.equals("m_smartstore_products")) {
//                    ctp=chnl_prod&sid=101513672&ctg=50000252&cpn=6572783406&serName=slc.commerce.naver.com
                                paramString.append("&ctp=").append("chnl_prod");
                                //채널번호
                                paramString.append("&sid=").append(_httpEngine.getValueFromHtml(htmlString, "?channelNo", "=", "\""));
                                //카테고리
                                paramString.append("&ctg=").append(_httpEngine.getValueFromHtml(htmlString, "\"categoryId\"", ":", ","));
                                //상품코드
                                paramString.append("&cpn=").append(_httpEngine.getValueFromHtml(htmlString, "\"productID\"", ":", ","));
                            } else {
                                //채널번호
                                paramString.append("&sid=").append(_httpEngine.getValueFromHtml(htmlString, "\"channelNo\"", ":", ","));
                            }

                            paramString.append("&serName=").append("slc.commerce.naver.com");
                        } else {
                            paramString.append("&serName=").append("lcs.naver.com");
                        }

                        // 여기서는 url무시한다.
                        String currentUrl = _httpEngine.getCurrentUrl();
                        String htmlString2 = _httpEngine.requestNaverLcsPidFromUrl(_nnb, URLEncoder.encode(urlString, "UTF-8"), _pageData.performanceTime);
                        _httpEngine.setCurrentUrl(currentUrl);

                        if (!TextUtils.isEmpty(htmlString2)) {
                            Log.d(TAG, "requestNaverLcsPidFromUrl: " + htmlString2);
                            String pid2 = htmlString2.trim();

                            if (isSlc) {
                                _pid = pid2;
                            }

                            paramString.append("&pid=").append(pid2);
                        }
                    }
                } else {
                    paramString.append("&pid=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_puid"), "UTF-8"));
                    paramString.append("&ssc=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_ssc"), "UTF-8"));
                }

                paramString.append("&ts=").append(_pageData.performanceTime);
            }

            paramString.append("&EOU");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        navigationStart:              1676829907951               1676745656855           1676833331167
//        fetchStart:                   1676829907956               1676745656869           1676833331175
//        domainLookupStart:            1676829907956               1676745656869           1676833331175
//        domainLookupEnd:              1676829907956               1676745656869           1676833331175
//        connectStart:                 1676829907956               1676745656869           1676833331175
//        connectEnd:                   1676829907956               1676745656869           1676833331175
//        requestStart:                 1676829907959               1676745656921           1676833331185
//        responseStart:                1676829907970               1676745656932           1676833331206
//        responseEnd:                  1676829908277               1676745657270           1676833331516
//        domLoading:                   1676829907990               1676745656983           1676833331250
//        domInteractive:               1676829908584               1676745658684           1676833331982
//        domContentLoadedEventStart:   1676829908584               1676745658684           1676833331982
//        domContentLoadedEventEnd:     1676829908586               1676745658694           1676833331985
//        domComplete:                  1676829908587               1676745658696           1676833331986
//        loadEventStart:               1676829908587               1676745658696           1676833331986
//        loadEventEnd:                 1676829908589               1676745658712           1676833331995
//        first-paint:                  228.4000000357628           998.6000000014901       387.19999999925494
//        first-contentful-paint:       268.10000002384200          1193.1000000014901      473.59999999962747
//        ts:                           1676829908636               1676745658771           1676833332017

        return paramString.toString();
    }

    private String getPerformanceString(String sti) {
        if (TextUtils.isEmpty(_pageData.performanceString)) {
            StringBuilder performanceString = new StringBuilder();
            Date mDate = new Date();
            long time = mDate.getTime();

//            if (_isWifi) {
                performanceString.append("&ct=").append("wifi");
//            }

            performanceString.append("&ect=").append("4g");

            performanceString.append("&navigationStart=").append(time);

            if (!sti.equals("m_main_home") && !sti.equals("m_smartstore_products")) {
                long newTime = time + MathHelper.randomRange(200, 400);
                performanceString.append("&unloadEventStart=").append(newTime);
                performanceString.append("&unloadEventEnd=").append(newTime);
            }

            time += MathHelper.randomRange(7, 17);
            performanceString.append("&fetchStart=").append(time);
            performanceString.append("&domainLookupStart=").append(time);
            performanceString.append("&domainLookupEnd=").append(time);
            performanceString.append("&connectStart=").append(time);
            performanceString.append("&connectEnd=").append(time);
//            performanceString.append("&secureConnectionStart=").append(time);

            time += MathHelper.randomRange(10, 60);
            performanceString.append("&requestStart=").append(time);

            time += MathHelper.randomRange(10, 25);
            performanceString.append("&responseStart=").append(time);

            time += MathHelper.randomRange(300, 350);
            performanceString.append("&responseEnd=").append(time);

            time += MathHelper.randomRange(40, 60);
            performanceString.append("&domLoading=").append(time);

            time += MathHelper.randomRange(700, 1800);
            performanceString.append("&domInteractive=").append(time);
            performanceString.append("&domContentLoadedEventStart=").append(time);

            time += MathHelper.randomRange(3, 15);
            performanceString.append("&domContentLoadedEventEnd=").append(time);

            time += MathHelper.randomRange(1, 2);
            performanceString.append("&domComplete=").append(time);
            performanceString.append("&loadEventStart=").append(time);

            time += MathHelper.randomRange(7, 20);
            performanceString.append("&loadEventEnd=").append(time);

            String paint = paintValue(0);
            String[] paintStrings = paint.split("\\.");
            long minValue = 0;

            if (paintStrings.length > 0) {
                minValue = Long.parseLong(paintStrings[0]);
            }
            performanceString.append("&first-paint=").append(paint);
            performanceString.append("&first-contentful-paint=").append(paintValue(minValue));

            time += MathHelper.randomRange(20, 60);
            _pageData.performanceTime = time;
            _pageData.performanceString = performanceString.toString();
        } else {
            _pageData.performanceTime += MathHelper.randomRange(1, 4);
        }

        return _pageData.performanceString;
    }

    public String paintValue(long min) {
        Log.d(TAG, "min: " + min);
        long max = 1000;

        if (min == 0) {
            min = 300;
        } else {
            max = min + 200;
            min += 100;
        }

        StringBuilder createdUrl = new StringBuilder();
        createdUrl.append((int) MathHelper.randomRange(min, max));
        long calc = MathHelper.randomRange(0, 9999999);

        if (calc > 0) {
            createdUrl.append(".");
            int firstValue = (int) MathHelper.randomRange(1, 8);
            long baseValue;
            long changeValue;

            if ((int) MathHelper.randomRange(0, 1) == 0) {
                //          10000002384186
                baseValue = 10000000000000L * firstValue;
            } else {
                //          4000000357628
                baseValue = 1000000000000L * firstValue;
            }

            if ((int) MathHelper.randomRange(0, 1) == 0) {
                changeValue = baseValue - calc;
            } else {
                changeValue = baseValue + calc;
            }

            createdUrl.append(changeValue);
        }

        return createdUrl.toString();
    }

    private String getPid(String htmlString) {
        if (TextUtils.isEmpty(_mainPid)) {
            String _nnb = _httpEngine.getNnb();
            StringBuilder pid = new StringBuilder();

            if (!TextUtils.isEmpty(_nnb)) {
                pid.append(_nnb);
            } else {
                pid.append("0.");
                // Math.random().toString(16).substring(0, 2).toUpperCase()
            }

            pid.append("-").append("SEARCH");
            String svt = _httpEngine.getValueFromHtml(htmlString, "window.svt", "=", ",");
            pid.append("-").append(svt);
            // (1e7 * Math.random()).toFixed()
            pid.append("-").append((int) Math.round(1e7 * Math.random()));

            _mainPid = pid.toString();
        }

        return _mainPid;
    }

    private String getUaParam(boolean hasModel, boolean hasBrands) throws UnsupportedEncodingException {
        String _ua = _httpEngine.getUa();
        Map<String, String> chUa = HttpHeader.getSecChUa(_ua);
        String secUa = null;
        StringBuilder paramString = new StringBuilder();

        for (Map.Entry<String, String> entry : chUa.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("sec-ch-ua")) {
                secUa = entry.getValue();
                break;
            }
        }

        if (!TextUtils.isEmpty(secUa)) {
            paramString.append("&ua_mobile=").append("true");

            String[] secUas = secUa.split(",");

            for (int i = 0; i < secUas.length; ++i) {
                String partUa = secUas[i].trim().replaceAll("\"", "");
                String[] keyVal = partUa.split(";v=");

                String val0 = URLEncoder.encode(keyVal[0], "UTF-8");
                val0 = val0.replaceAll("\\+", "%20");
                val0 = val0.replaceAll("%28", "(");
                val0 = val0.replaceAll("%29", ")");
                paramString.append("&ua_brand_").append(i).append("=").append(val0);

                if (keyVal.length > 1) {
                    paramString.append("&ua_version_").append(i).append("=").append(URLEncoder.encode(keyVal[1], "UTF-8"));
                }
            }

            if (hasBrands) {
                // &ua_brands=%5Bobject%20Object%5D%2C%5Bobject%20Object%5D%2C%5Bobject%20Object%5D
                String value = "[object Object],[object Object],[object Object]";
                value = URLEncoder.encode(value, "UTF-8");
                value = value.replaceAll("\\+", "%20");
                paramString.append("&ua_brands=").append(value);
            }

            if (hasModel && !TextUtils.isEmpty(_httpEngine.getBrowserVersion())) {
                String[] uaParts = _ua.split("\\)", 2);
                int begin = uaParts[0].indexOf("(");
                String deviceInfo = uaParts[0];

                if (begin > -1) {
                    deviceInfo = uaParts[0].substring(begin + 1);
                }

                //Linux; Android 6.0; Nexus 5 Build/MRA58N
                String[] infoParts = deviceInfo.split(";");
                String model = "";
                String platform = "";
                String platformVersion = "";

                if (infoParts.length > 2) {
                    model = infoParts[2].trim();

                    if (model.contains("Build")) {
                        model = model.substring(0, model.indexOf("Build"));
                    }

                    if (model.contains("/")) {
                        model = model.substring(0, model.indexOf("/"));
                    }

                    model = model.trim();
                }

                if (infoParts.length > 1) {
                    String platformInfo = infoParts[1].trim();
                    String[] parts = platformInfo.split(" ");
                    platform = parts[0].trim();

                    if (parts.length > 1) {
                        platformVersion = parts[1].trim();
                    }
                }

                paramString.append("&ua_model=").append(model);
                paramString.append("&ua_platform=").append(platform);
                paramString.append("&ua_platformVersion=").append(platformVersion);
                paramString.append("&ua_uaFullVersion=").append(_httpEngine.getBrowserVersion());
            }
        }

        return paramString.toString();
    }
}
