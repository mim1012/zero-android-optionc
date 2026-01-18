package com.sec.android.app.sbrowser.engine.WebEngine;

import android.text.TextUtils;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.DnsParser;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebEngine.Headers.ChromeClientHints;
import com.sec.android.app.sbrowser.engine.WebEngine.Headers.ClientHints;
import com.sec.android.app.sbrowser.engine.WebEngine.Headers.EdgeClientHints;
import com.sec.android.app.sbrowser.engine.WebEngine.Headers.OperaClientHints;
import com.sec.android.app.sbrowser.engine.WebEngine.Headers.UserAgentClientHints;
import com.sec.android.app.sbrowser.engine.WebEngine.Headers.WhaleClientHints;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpHeader {

    private static final String TAG = HttpHeader.class.getSimpleName();

    public static String getValueFromMap(Map<String, String> map, String key) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static int getDeviceHeight(String ua) {
        if (ua.contains("SamsungBrowser")) {
            return 520;
        }

        return 560;
    }

    public static String getAccept(String ua) {
        String majorVersion = null;
        if (ua.contains("Chrome/")) {
            String[] parts = ua.split("Chrome/");

            if (parts.length > 1) {
                String[] subParts = parts[1].trim().split("\\.");
                majorVersion = subParts[0];
            }
        }

        Log.d(TAG, "- getAccept majorVersion: " + majorVersion);

        // 어디 버전에서 다른지 확인 필요.
        if (!TextUtils.isEmpty(majorVersion) && Integer.parseInt(majorVersion) < 109) {
            return "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9";
        }

        return "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7";
    }

    public static UserAgentClientHints getUserAgentClientHints(String ua, String chromeVersion, String browserVersion) {
        UserAgentClientHints hints = new UserAgentClientHints();
        ClientHints clientHints = null;

        if (ua.contains(WhaleClientHints.UA_NAME)) {
            clientHints = new WhaleClientHints();
            clientHints.setUserAgent(ua);
            hints = clientHints.getSecChUa(hints, chromeVersion);
        } else if (ua.contains(EdgeClientHints.UA_NAME)) {
            clientHints = new EdgeClientHints();
            clientHints.setUserAgent(ua);
            hints = clientHints.getSecChUa(hints, chromeVersion, browserVersion);
        } else if (ua.contains(OperaClientHints.UA_NAME)) {
            clientHints = new OperaClientHints();
            clientHints.setUserAgent(ua);
            hints = clientHints.getSecChUa(hints, chromeVersion, browserVersion);
        } else if (ua.contains(ChromeClientHints.UA_NAME)) {
            clientHints = new ChromeClientHints();
            clientHints.setUserAgent(ua);
            hints = clientHints.getSecChUa(hints, chromeVersion);
            browserVersion = chromeVersion;
        } else {
        }

        return hints;
    }

    public static Map<String, String> getSecChUa(String ua) {
        return getSecChUa(ua, null, null, false, false, false);
    }

    public static Map<String, String> getSecChUa(String ua, String chromeVersion, String browserVersion, boolean isPc, boolean hasDetail, boolean inclFullVersion) {
        Map<String, String> headers = new HashMap<>();

        // 삼성브라우저UA, 카카오톡UA 면 추가하지 않는다.
        if (ua.contains("SamsungBrowser") || ua.contains("KAKAOTALK") || ua.contains("wv")) {
            return headers;
        }

        UserAgentClientHints hints = new UserAgentClientHints();
        ClientHints clientHints = null;

        if (ua.contains(WhaleClientHints.UA_NAME)) {
            clientHints = new WhaleClientHints();
            clientHints.setUserAgent(ua);
            hints = clientHints.getSecChUa(hints, chromeVersion);
        } else if (ua.contains(EdgeClientHints.UA_NAME)) {
            clientHints = new EdgeClientHints();
            clientHints.setUserAgent(ua);
            hints = clientHints.getSecChUa(hints, chromeVersion, browserVersion);
        } else if (ua.contains(OperaClientHints.UA_NAME)) {
            clientHints = new OperaClientHints();
            clientHints.setUserAgent(ua);
            hints = clientHints.getSecChUa(hints, chromeVersion, browserVersion);
        } else if (ua.contains(ChromeClientHints.UA_NAME)) {
            clientHints = new ChromeClientHints();
            clientHints.setUserAgent(ua);
            hints = clientHints.getSecChUa(hints, chromeVersion);
            browserVersion = chromeVersion;
        } else {
        }

        if (clientHints == null || TextUtils.isEmpty(hints.secChUa)) {
            return headers;
        }

//        headers.put("Sec-Ch-Ua", secUa);
//        headers.put("Sec-Ch-Ua-Mobile", "?1");
//        headers.put("Sec-Ch-Ua-Platform", "\"Android\"");
//        headers.put("Sec-CH-UA", secUa);
//        headers.put("Sec-CH-UA-Mobile", "?1");
//        headers.put("Sec-CH-UA-Platform", "\"Android\"");
        headers.put("sec-ch-ua", hints.secChUa);

        String model = null;
        String platformVersion = null;

        if (!TextUtils.isEmpty(chromeVersion) && hasDetail) {
//                sec-ch-ua-arch: ""
//                sec-ch-ua-bitness: ""
//                sec-ch-ua-full-version-list: "Google Chrome";v="111.0.5563.115", "Not(A:Brand";v="8.0.0.0", "Chromium";v="111.0.5563.115"
            if (isPc) {
                headers.put("sec-ch-ua-arch", "\"x86\"");
                headers.put("sec-ch-ua-bitness", "\"64\"");
                headers.put("sec-ch-ua-form-factors", "\"Desktop\"");
            } else {
                headers.put("sec-ch-ua-arch", "\"\"");
                headers.put("sec-ch-ua-bitness", "\"\"");
                headers.put("sec-ch-ua-form-factors", "\"Mobile\"");
            }

            if (inclFullVersion) {
                headers.put("sec-ch-ua-full-version", "\"" + browserVersion + "\"");
            }

            headers.put("sec-ch-ua-full-version-list", hints.secChUaFullVersionList);

            // parse
            String[] uaParts = ua.split("\\)", 2);
            int begin = uaParts[0].indexOf("(");
            String deviceInfo = uaParts[0];

            if (begin > -1) {
                deviceInfo = uaParts[0].substring(begin + 1);
            }

            //Linux; Android 6.0; Nexus 5 Build/MRA58N
            String[] infoParts = deviceInfo.split(";");

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
//                    platform = parts[0].trim();

                if (parts.length > 1) {
                    platformVersion = parts[parts.length - 1].trim();
                }
            }

        }

//            sec-ch-ua-model: "SM-G930K"
//            sec-ch-ua-platform: "Android"
//            sec-ch-ua-platform-version: "8.0.0"
//            sec-ch-ua-wow64: ?0

        if (isPc) {
            headers.put("sec-ch-ua-mobile", "?0");

            if (!TextUtils.isEmpty(chromeVersion) && hasDetail) {
                headers.put("sec-ch-ua-model", "\"\"");
            }

            if (ua.contains("Macintosh")) {
                headers.put("sec-ch-ua-platform", "\"macOS\"");

                if (!TextUtils.isEmpty(chromeVersion) && hasDetail) {
                    headers.put("sec-ch-ua-platform-version", "\"13.5.1\"");
                }
            } else {
                headers.put("sec-ch-ua-platform", "\"Windows\"");

                if (!TextUtils.isEmpty(chromeVersion) && hasDetail) {
                    headers.put("sec-ch-ua-platform-version", "\"19.0.0\"");
                }
            }

            if (!TextUtils.isEmpty(chromeVersion) && hasDetail) {
                headers.put("sec-ch-ua-wow64", "?0");
            }
        } else {
            headers.put("sec-ch-ua-mobile", "?1");

            if (!TextUtils.isEmpty(chromeVersion) && hasDetail) {
                if (!TextUtils.isEmpty(UserManager.getInstance().nnbData.model)) {
                    model = UserManager.getInstance().nnbData.model;
                }

                headers.put("sec-ch-ua-model", "\"" + model + "\"");
            }

            headers.put("sec-ch-ua-platform", "\"Android\"");

            if (!TextUtils.isEmpty(chromeVersion) && hasDetail) {
                if (!TextUtils.isEmpty(UserManager.getInstance().nnbData.platformVersion)) {
                    platformVersion = UserManager.getInstance().nnbData.platformVersion + ".0.0";
                }

                headers.put("sec-ch-ua-platform-version", "\"" + platformVersion + "\"");
                headers.put("sec-ch-ua-wow64", "?0");
            }
        }

        return headers;
    }

    public static String getSecFetchSite(String prevUrl, String url) throws MalformedURLException {
        if (TextUtils.isEmpty(prevUrl) || TextUtils.isEmpty(url)) {
            return "none";
        }

        URL urlPrev = new URL(prevUrl);
        URL urlBase = new URL(url);

        int portBase = (urlBase.getPort() < 0) ? urlBase.getDefaultPort() : urlBase.getPort();
        int portReferer = (urlPrev.getPort() < 0) ? urlPrev.getDefaultPort() : urlPrev.getPort();

//            Log.e("TAG", "current: " + urlBase.getHost() + " / ref: " + urlReferer.getHost() + "/ current: " + urlBase.getAuthority() + " / ref: " + urlReferer.getAuthority());
        if (urlBase.getHost().equals(urlPrev.getHost()) &&
                urlBase.getProtocol().equals(urlPrev.getProtocol()) &&
                portBase == portReferer) {
            return "same-origin";
        } else {
            String domainBase = DnsParser.getDomain(urlBase.getHost());
            String domainReferer = DnsParser.getDomain(urlPrev.getHost());

            if (!TextUtils.isEmpty(domainBase) && domainBase.equals(domainReferer)) {
                return "same-site";
            } else {
                return "cross-site";
            }
        }
    }
}
