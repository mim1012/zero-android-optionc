package com.sec.android.app.sbrowser.engine.WebEngine;

import android.text.TextUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpCookieController {

    private static final String TAG = HttpCookieController.class.getSimpleName();

    private final Map<String, List<String>> _cookieMap = new LinkedHashMap<>();

    public void clear() {
        _cookieMap.clear();
    }

    public void setCookie(String url, String value) {
        List<String> list = _cookieMap.get(url);

        if (list == null) {
            list = new ArrayList<>();
        }

        String[] keyValues = value.split("=");
        int hasKeyIndex = -1;

        for (int i = 0; i <list.size(); ++i) {
            if (list.get(i).startsWith(keyValues[0])) {
                hasKeyIndex = i;
                break;
            }
        }

        if (hasKeyIndex > -1) {
            list.set(hasKeyIndex, value);
        } else{
            list.add(value);
        }

        _cookieMap.put(url, list);
    }

    public void deleteCookie(String url, String value) {
        List<String> list = _cookieMap.get(url);

        if (list == null) {
            list = new ArrayList<>();
        }

        String[] keyValues = value.split("=");
        int hasKeyIndex = -1;
        for (int i = 0; i <list.size(); ++i) {
            if (list.get(i).startsWith(keyValues[0])) {
                hasKeyIndex = i;
                break;
            }
        }

        if (hasKeyIndex > -1) {
            list.remove(hasKeyIndex);
        }

        _cookieMap.put(url, list);
    }

    public String getCookie(String url) {
        StringBuilder cookies = new StringBuilder();

        try {
            URL url1 = new URL(url);

            for (Map.Entry<String, List<String>> item : _cookieMap.entrySet()) {
                String key = item.getKey();

                if (!url1.getHost().contains(key)) {
                    continue;
                }

                if (cookies.length() > 0) {
                    cookies.append("; ");
                }

                cookies.append(TextUtils.join("; ", item.getValue()));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return cookies.toString();
    }
}
