package com.sec.android.app.sbrowser.engine.WebEngine.Headers;

import android.text.TextUtils;
import android.util.Log;

public class WhaleClientHints extends ClientHints {

    private static final String TAG = WhaleClientHints.class.getSimpleName();

    public static final String UA_NAME = "Whale";

    @Override
    public UserAgentClientHints getSecChUa(UserAgentClientHints hints, String chromeVersion) {
        if (TextUtils.isEmpty(_ua)) {
            return hints;
        }

        String version = getVersion(_ua, UA_NAME + "/([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)");

        if (TextUtils.isEmpty(version)) {
            return hints;
        }

        String majorVersionString = getVersion(_ua, UA_NAME + "/([0-9]+)\\.");
        String chromeMajorVersionString = getVersion(_ua, ChromeClientHints.UA_NAME + "/([0-9]+)\\.");

        if (TextUtils.isEmpty(majorVersionString) || TextUtils.isEmpty(chromeMajorVersionString)) {
            return hints;
        }

        Log.d(TAG, "Whale version: " + version + ", Chrome version: " + chromeVersion);
        int majorVersion = Integer.parseInt(majorVersionString);

        // Whale
        // Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Whale/3.23.214.10 Safari/537.36
        //  "Whale";v="3.23.214.10", "Not-A.Brand";v="8.0.0.0", "Chromium";v="118.0.5993.118"
        //  "Whale";v="3", "Not-A.Brand";v="8", "Chromium";v="118"
        // Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Whale/3.22.205.26 Safari/537.36
        //  "Whale";v="3.22.205.26", "Not-A.Brand";v="8.0.0.0", "Chromium";v="116.0.5845.228"
        //  "Whale";v="3", "Not-A.Brand";v="8", "Chromium";v="116"
        // Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Whale/3.22.205.18 Safari/537.36
        //  "Whale";v="3.22.205.18", "Not-A.Brand";v="8.0.0.0", "Chromium";v="116.0.5845.180"
        //  "Whale";v="3", "Not-A.Brand";v="8", "Chromium";v="116"
        // Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Whale/3.22.205.14 Safari/537.36
        //  "Whale";v="3.22.205.14", "Not-A.Brand";v="8.0.0.0", "Chromium";v="116.0.5845.141"
        //  "Whale";v="3", "Not-A.Brand";v="8", "Chromium";v="116"

        // 기본값 없는 것으로 설정.
        String secUa = null;
        String secUaFull = null;
//        String secUa = "\"Whale\";v=\"" + majorVersion + "\", \"Not-A.Brand\";v=\"8\", \"Chromium\";v=\"" + chromeMajorVersionString + "\"";
//        String secUaFull = "\"Whale\";v=\"" + version + "\", \"Not-A.Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";

        switch (majorVersion) {
            case 3:
                secUa = "\"Whale\";v=\"3\", \"Not-A.Brand\";v=\"8\", \"Chromium\";v=\"" + chromeMajorVersionString + "\"";
                secUaFull = "\"Whale\";v=\"" + version + "\", \"Not-A.Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            default:
                break;
        }

        hints.secChUa = secUa;
        hints.secChUaFullVersionList = secUaFull;

        return hints;
    }
}
