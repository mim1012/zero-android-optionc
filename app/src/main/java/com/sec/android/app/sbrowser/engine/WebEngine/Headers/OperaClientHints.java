package com.sec.android.app.sbrowser.engine.WebEngine.Headers;

import android.text.TextUtils;
import android.util.Log;

public class OperaClientHints extends ClientHints {

    private static final String TAG = OperaClientHints.class.getSimpleName();

    public static final String UA_NAME = "OPR";

    @Override
    public UserAgentClientHints getSecChUa(UserAgentClientHints hints, String chromeVersion) {
        return getSecChUa(hints, chromeVersion, null);
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

        Log.d(TAG, "Opera version: " + version + ", Chrome version: " + chromeVersion);
        int majorVersion = Integer.parseInt(majorVersionString);

        // Opera
        // Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36 OPR/104.0.0.0
        //  "Chromium";v="118.0.5993.118", "Opera";v="104.0.4944.36", "Not=A?Brand";v="99.0.0.0"
        //  "Chromium";v="118.0.5993.96", "Opera";v="104.0.4944.33", "Not=A?Brand";v="99.0.0.0"
        //  "Chromium";v="118.0.5993.89", "Opera";v="104.0.4944.28", "Not=A?Brand";v="99.0.0.0"
        //  "Chromium";v="118.0.5993.89", "Opera";v="104.0.4944.23", "Not=A?Brand";v="99.0.0.0"
        //  "Chromium";v="118", "Opera";v="104", "Not=A?Brand";v="99"
        // Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 OPR/103.0.0.0
        //  "Opera";v="103.0.4928.34", "Not;A=Brand";v="8.0.0.0", "Chromium";v="117.0.5938.150"
        //  "Opera";v="103.0.4928.26", "Not;A=Brand";v="8.0.0.0", "Chromium";v="117.0.5938.150"
        //  "Opera";v="103", "Not;A=Brand";v="8", "Chromium";v="117"

        // 기본값 없는 것으로 설정.
        String secUa = null;
        String secUaFull = null;
//        String secUa = "\"Opera\";v=\"" + majorVersion + "\", \"Not;A=Brand\";v=\"8\", \"Chromium\";v=\"" + chromeMajorVersionString + "\"";
//        String secUaFull = "\"Opera\";v=\"" + version + "\", \"Not;A=Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";

        switch (majorVersion) {
            case 105:
                secUa = "\"Opera\";v=\"105\", \"Chromium\";v=\"" + chromeMajorVersionString + "\", \"Not?A_Brand\";v=\"24\"";
                secUaFull = "\"Opera\";v=\"" + version + "\", \"Chromium\";v=\"" + chromeVersion + "\", \"Not?A_Brand\";v=\"24.0.0.0\"";
                break;

            case 104:
                secUa = "\"Chromium\";v=\"" + chromeMajorVersionString + "\", \"Opera\";v=\"104\", \"Not=A?Brand\";v=\"99\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Opera\";v=\"" + version + "\", \"Not=A?Brand\";v=\"99.0.0.0\"";
                break;

            case 103:
                secUa = "\"Opera\";v=\"103\", \"Not;A=Brand\";v=\"8\", \"Chromium\";v=\"" + chromeMajorVersionString + "\"";
                secUaFull = "\"Opera\";v=\"" + version + "\", \"Not;A=Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            default:
                break;
        }

        hints.secChUa = secUa;
        hints.secChUaFullVersionList = secUaFull;

        return hints;
    }
}
