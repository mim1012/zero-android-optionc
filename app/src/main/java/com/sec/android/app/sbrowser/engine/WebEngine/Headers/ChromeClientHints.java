package com.sec.android.app.sbrowser.engine.WebEngine.Headers;

import android.text.TextUtils;

public class ChromeClientHints extends ClientHints {

    private static final String TAG = ChromeClientHints.class.getSimpleName();

    public static final String UA_NAME = "Chrome";

    @Override
    public UserAgentClientHints getSecChUa(UserAgentClientHints hints, String chromeVersion) {
        if (TextUtils.isEmpty(_ua)) {
            return hints;
        }

        String majorVersionString = getVersion(_ua, UA_NAME + "/([0-9]+)\\.");

        if (TextUtils.isEmpty(majorVersionString)) {
            return hints;
        }

//        Log.d(TAG, "Chrome version: " + majorVersionString);
        int majorVersion = Integer.parseInt(majorVersionString);

        // 기본값 없는 것으로 설정.
        String secUa = null;
        String secUaFull = null;
//        String secUa = "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"" + majorVersionString + "\", \"Google Chrome\";v=\"" + majorVersionString + "\"";
//        String secUaFull = "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"" + version + "\", \"Google Chrome\";v=\"" + version + "\"";

        switch (majorVersion) {
            case 142:
                secUa = "\"Chromium\";v=\"142\", \"Google Chrome\";v=\"142\", \"Not_A Brand\";v=\"99\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Not_A Brand\";v=\"99.0.0.0\"";
                break;

            case 141:
                secUa = "\"Google Chrome\";v=\"141\", \"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"141\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Not?A_Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 140:
                secUa = "\"Chromium\";v=\"140\", \"Not=A?Brand\";v=\"24\", \"Google Chrome\";v=\"140\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Not=A?Brand\";v=\"24.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 139:
                secUa = "\"Not;A=Brand\";v=\"99\", \"Google Chrome\";v=\"139\", \"Chromium\";v=\"139\"";
                secUaFull = "\"Not;A=Brand\";v=\"99.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 138:
                secUa = "\"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"138\", \"Google Chrome\";v=\"138\"";
                secUaFull = "\"Not)A;Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 137:
                secUa = "\"Google Chrome\";v=\"137\", \"Chromium\";v=\"137\", \"Not/A)Brand\";v=\"24\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\", \"Not/A)Brand\";v=\"24.0.0.0\"";
                break;

            case 136:
                secUa = "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Not.A/Brand\";v=\"99.0.0.0\"";
                break;

            case 135:
                secUa = "\"Google Chrome\";v=\"135\", \"Not-A.Brand\";v=\"8\", \"Chromium\";v=\"135\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Not-A.Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 134:
                secUa = "\"Chromium\";v=\"134\", \"Not:A-Brand\";v=\"24\", \"Google Chrome\";v=\"134\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Not:A-Brand\";v=\"24.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 133:
                secUa = "\"Not(A:Brand\";v=\"99\", \"Google Chrome\";v=\"133\", \"Chromium\";v=\"133\"";
                secUaFull = "\"Not(A:Brand\";v=\"99.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 132:
                secUa = "\"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\", \"Google Chrome\";v=\"132\"";
                secUaFull = "\"Not A(Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 131:
                secUa = "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\", \"Not_A Brand\";v=\"24.0.0.0\"";
                break;

            case 130:
                secUa = "\"Chromium\";v=\"130\", \"Google Chrome\";v=\"130\", \"Not?A_Brand\";v=\"99\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Not?A_Brand\";v=\"99.0.0.0\"";
                break;

            case 129:
                secUa = "\"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Not=A?Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 128:
                secUa = "\"Chromium\";v=\"128\", \"Not;A=Brand\";v=\"24\", \"Google Chrome\";v=\"128\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Not;A=Brand\";v=\"24.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 127:
                secUa = "\"Not)A;Brand\";v=\"99\", \"Google Chrome\";v=\"127\", \"Chromium\";v=\"127\"";
                secUaFull = "\"Not)A;Brand\";v=\"99.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 126:
                secUa = "\"Not/A)Brand\";v=\"8\", \"Chromium\";v=\"126\", \"Google Chrome\";v=\"126\"";
                secUaFull = "\"Not/A)Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 125:
                secUa = "\"Google Chrome\";v=\"125\", \"Chromium\";v=\"125\", \"Not.A/Brand\";v=\"24\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\", \"Not.A/Brand\";v=\"24.0.0.0\"";
                break;

            case 124:
                secUa = "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Not-A.Brand\";v=\"99.0.0.0\"";
                break;

            case 123:
                secUa = "\"Google Chrome\";v=\"123\", \"Not:A-Brand\";v=\"8\", \"Chromium\";v=\"123\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Not:A-Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 122:
                secUa = "\"Chromium\";v=\"122\", \"Not(A:Brand\";v=\"24\", \"Google Chrome\";v=\"122\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Not(A:Brand\";v=\"24.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 121:
                secUa = "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\"";
                secUaFull = "\"Not A(Brand\";v=\"99.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 120:
                secUa = "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"";
                secUaFull = "\"Not_A Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 119:
                secUa = "\"Google Chrome\";v=\"119\", \"Chromium\";v=\"119\", \"Not?A_Brand\";v=\"24\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\", \"Not?A_Brand\";v=\"24.0.0.0\"";
                break;

            case 118:
                secUa = "\"Chromium\";v=\"118\", \"Google Chrome\";v=\"118\", \"Not=A?Brand\";v=\"99\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Not=A?Brand\";v=\"99.0.0.0\"";
                break;

            case 117:
                secUa = "\"Google Chrome\";v=\"117\", \"Not;A=Brand\";v=\"8\", \"Chromium\";v=\"117\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Not;A=Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 116:
                secUa = "\"Chromium\";v=\"116\", \"Not)A;Brand\";v=\"24\", \"Google Chrome\";v=\"116\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Not)A;Brand\";v=\"24.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 115:
                secUa = "\"Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"115\", \"Chromium\";v=\"115\"";
                secUaFull = "\"Not/A)Brand\";v=\"99.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 114:
                secUa = "\"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Google Chrome\";v=\"114\"";
                secUaFull = "\"Not.A/Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 113:
                secUa = "\"Google Chrome\";v=\"113\", \"Chromium\";v=\"113\", \"Not-A.Brand\";v=\"24\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\", \"Not-A.Brand\";v=\"24.0.0.0\"";
                break;

            case 112:
                secUa = "\"Chromium\";v=\"112\", \"Google Chrome\";v=\"112\", \"Not:A-Brand\";v=\"99\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Not:A-Brand\";v=\"99.0.0.0\"";
                break;

            case 111:
                secUa = "\"Google Chrome\";v=\"111\", \"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"111\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Not(A:Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 110:
                secUa = "\"Chromium\";v=\"110\", \"Not A(Brand\";v=\"24\", \"Google Chrome\";v=\"110\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Not A(Brand\";v=\"24.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 109:
                secUa = "\"Not_A Brand\";v=\"99\", \"Google Chrome\";v=\"109\", \"Chromium\";v=\"109\"";
                secUaFull = "\"Not_A Brand\";v=\"99.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 108:
                secUa = "\"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"108\", \"Google Chrome\";v=\"108\"";
                secUaFull = "\"Not?A_Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 107:
                secUa = "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\", \"Not=A?Brand\";v=\"24.0.0.0\"";
                break;

            case 106:
                secUa = "\"Chromium\";v=\"106\", \"Google Chrome\";v=\"106\", \"Not;A=Brand\";v=\"99\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Not;A=Brand\";v=\"99.0.0.0\"";
                break;

            case 105:
                secUa = "\"Google Chrome\";v=\"105\", \"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"105\"";
                secUaFull = "\"Google Chrome\";v=\"" + chromeVersion + "\", \"Not)A;Brand\";v=\"8.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 104:
                secUa = "\"Chromium\";v=\"104\", \" Not A;Brand\";v=\"99\", \"Google Chrome\";v=\"104\"";
                secUaFull = "\"Chromium\";v=\"" + chromeVersion + "\", \" Not A;Brand\";v=\"99.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 103:
                secUa = "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"";
                secUaFull = "\".Not/A)Brand\";v=\"99.0.0.0\", \"Google Chrome\";v=\"" + chromeVersion + "\", \"Chromium\";v=\"" + chromeVersion + "\"";
                break;

            case 102:
                secUa = "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"102\", \"Google Chrome\";v=\"102\"";
                secUaFull = "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 101:
                secUa = "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"101\", \"Google Chrome\";v=\"101\"";
                secUaFull = "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            case 100:
                secUa = "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"";
                secUaFull = "\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"" + chromeVersion + "\", \"Google Chrome\";v=\"" + chromeVersion + "\"";
                break;

            default:
                break;
        }

        hints.secChUa = secUa;
        hints.secChUaFullVersionList = secUaFull;

        return hints;
    }
}
