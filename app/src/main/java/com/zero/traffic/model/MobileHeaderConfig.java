package com.zero.traffic.model;

import org.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 서버 /headers/mobile 응답 모델
 * WebView UA + Sec-CH-UA + Accept-Language 동적 적용
 */
public class MobileHeaderConfig {
    private final String userAgent;
    private final String secChUa;
    private final String secChUaMobile;
    private final String secChUaPlatform;
    private final String acceptLanguage;

    public MobileHeaderConfig(JSONObject json) {
        this.userAgent = json.optString("user_agent", "");
        this.secChUa = json.optString("sec_ch_ua", "");
        this.secChUaMobile = json.optString("sec_ch_ua_mobile", "?1");
        this.secChUaPlatform = json.optString("sec_ch_ua_platform", "\"Android\"");
        this.acceptLanguage = json.optString("accept_language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
    }

    public String getUserAgent() { return userAgent; }
    public String getSecChUa() { return secChUa; }
    public String getSecChUaMobile() { return secChUaMobile; }
    public String getSecChUaPlatform() { return secChUaPlatform; }
    public String getAcceptLanguage() { return acceptLanguage; }

    /** UA에서 Chrome 버전 추출 (예: "131") */
    public String getChromeVersion() {
        Matcher m = Pattern.compile("Chrome/(\\d+)\\.").matcher(userAgent);
        return m.find() ? m.group(1) : "131";
    }

    /** UA에서 전체 Chrome 버전 추출 (예: "131.0.6778.200") */
    public String getChromeFullVersion() {
        Matcher m = Pattern.compile("Chrome/([\\d.]+)").matcher(userAgent);
        return m.find() ? m.group(1) : "131.0.6778.200";
    }

    /** UA에서 Android 기기 모델 추출 */
    public String getDeviceModel() {
        Matcher m = Pattern.compile("; ([A-Z][A-Z0-9-]+) Build/").matcher(userAgent);
        return m.find() ? m.group(1) : "SM-G977N";
    }

    public boolean isValid() {
        return userAgent != null && !userAgent.isEmpty();
    }
}
