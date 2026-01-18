package com.sec.android.app.sbrowser.engine.WebEngine.Headers;

import android.text.TextUtils;
import android.util.Log;

public class SamsungInternetClientHints extends ClientHints {

    private static final String TAG = SamsungInternetClientHints.class.getSimpleName();

    public static final String UA_NAME = "SamsungBrowser";

    @Override
    public UserAgentClientHints getSecChUa(UserAgentClientHints hints, String chromeVersion) {
        return null;
    }

    @Override
    public UserAgentClientHints getSecChUa(UserAgentClientHints hints, String chromeVersion, String version) {
        if (TextUtils.isEmpty(_ua) || TextUtils.isEmpty(version)) {
            return hints;
        }

        String majorVersionString = getVersion(_ua, UA_NAME + "/([0-9]+)\\.");
        String chromeMajorVersionString = getVersion(_ua, ChromeClientHints.UA_NAME + "/([0-9]+)\\.");

        if (TextUtils.isEmpty(majorVersionString) || TextUtils.isEmpty(chromeMajorVersionString)) {
            return hints;
        }

        Log.d(TAG, "Samsung Internet version: " + version + ", Chrome version: " + chromeVersion);
        int majorVersion = Integer.parseInt(majorVersionString);

        // Accept:
        //  text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7
        // Accept-Encoding:
        //  gzip, deflate, br
        // Accept-Language:
        //  ko-KR,ko,en-US,en

        // Mozilla/5.0 (Linux; Android 13; SAMSUNG SM-S908N) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/23.0 Chrome/115.0.0.0 Mobile Safari/537.36
        //  "Not/A)Brand";v="99.0.0.0", "Samsung Internet";v="23.0.0.47", "Chromium";v="115.0.5790.168"
        //  "Not/A)Brand";v="99.0.0.0", "Samsung Internet";v="23.0.0.47", "Chromium";v="115.0.5790.168"

        // 기본값 없는 것으로 설정.
        String secUa = null;
        String secUaFull = null;
//        String secUa = "\"Opera\";v=\"" + majorVersion + "\", \"Not;A=Brand\";v=\"8\", \"Chromium\";v=\"" + chromeMajorVersionString + "\"";
//        String secUaFull = "\"Opera\";v=\"" + version + "\", \"Not;A=Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";

        switch (majorVersion) {
            case 23:
                secUa = "\"Not/A)Brand\";v=\"99\", \"Samsung Internet\";v=\"23.0\", \"Chromium\";v=\"" + chromeMajorVersionString + "\"";
                secUaFull = "\"Not/A)Brand\";v=\"99.0.0.0\", \"Samsung Internet\";v=\"" + version + "\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            default:
                break;
        }

        hints.secChUa = secUa;
        hints.secChUaFullVersionList = secUaFull;

        return hints;
    }
}
