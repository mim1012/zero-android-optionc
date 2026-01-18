package com.sec.android.app.sbrowser.engine.WebEngine.Headers;

import android.text.TextUtils;
import android.util.Log;

public class EdgeClientHints extends ClientHints {

    private static final String TAG = EdgeClientHints.class.getSimpleName();

    public static final String UA_NAME = "Edg";

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

        Log.d(TAG, "Edge version: " + version + ", Chrome version: " + chromeVersion);
        int majorVersion = Integer.parseInt(majorVersionString);

        // Edge
        // Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36 Edg/119.0.0.0
        // "Microsoft Edge";v="119.0.2151.44", "Chromium";v="119.0.6045.105", "Not?A_Brand";v="24.0.0.0"
        // "Microsoft Edge";v="119", "Chromium";v="119", "Not?A_Brand";v="24"
        // Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36 Edg/118.0.2088.46
        //  "Chromium";v="118.0.5993.71", "Microsoft Edge";v="118.0.2088.46", "Not=A?Brand";v="99.0.0.0"
        //  "Chromium";v="118", "Microsoft Edge";v="118", "Not=A?Brand";v="99"
        // Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36 Edg/117.0.2045.55
        //  "Microsoft Edge";v="117.0.2045.55", "Not;A=Brand";v="8.0.0.0", "Chromium";v="117.0.5938.150"
        //  "Microsoft Edge";v="117", "Not;A=Brand";v="8", "Chromium";v="117"

        // 기본값 없는 것으로 설정.
        String secUa = null;
        String secUaFull = null;
//        String secUa = "\"Microsoft Edge\";v=\"" + majorVersion + "\", \"Not;A=Brand\";v=\"8\", \"Chromium\";v=\"" + chromeMajorVersionString + "\"";
//        String secUaFull = "\"Microsoft Edge\";v=\"" + version + "\", \"Not;A=Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";

        switch (majorVersion) {
            case 120:
                secUa = "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Microsoft Edge\";v=\"120\"";
                secUaFull = "\"Not_A Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\", \"Microsoft Edge\";v=\"" + version + "\"";
                break;

            case 119:
                secUa = "\"Microsoft Edge\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\"";
                secUaFull = "\"Microsoft Edge\";v=\"" + version + "\", \"Chromium\";v=\"" + chromeVersion + "\", \"Not?A_Brand\";v=\"24.0.0.0\"";
                break;

            case 118:
                secUa = "\"Chromium\";v=\"118\", \"Microsoft Edge\";v=\"118\", \"Not=A?Brand\";v=\"99\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Microsoft Edge\";v=\"" + version + "\", \"Not=A?Brand\";v=\"99.0.0.0\"";
                break;

            case 117:
                secUa = "\"Microsoft Edge\";v=\"117\", \"Not;A=Brand\";v=\"8\", \"Chromium\";v=\"117\"";
                secUaFull = "\"Microsoft Edge\";v=\"" + version + "\", \"Not;A=Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            default:
                break;
        }

        hints.secChUa = secUa;
        hints.secChUaFullVersionList = secUaFull;

        return hints;
    }
}
