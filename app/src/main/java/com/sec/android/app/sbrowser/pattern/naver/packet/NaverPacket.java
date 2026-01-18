package com.sec.android.app.sbrowser.pattern.naver.packet;

import android.text.TextUtils;

public class NaverPacket {

    private static final String TAG = NaverPacket.class.getSimpleName();

    public String _mainPid = null;
    public String _pid = null;


    public NaverPacket() {

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
}
