package com.sec.android.app.sbrowser.library.proxy.proxy4free.client;

import android.text.TextUtils;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.Utility;
import com.sec.android.app.sbrowser.library.proxy.proxy4free.retrofit.models.Proxy4FreeData;

import java.util.HashMap;
import java.util.Map;

public class Proxy4FreeClient {

    private static final String TAG = Proxy4FreeClient.class.getSimpleName();

    protected Proxy4FreeClient() {
    }

    public Proxy4FreeData getProxyData(String options) {
        Proxy4FreeData data = new Proxy4FreeData();

        if (!TextUtils.isEmpty(options)) {
            //host=proxy.proxy4free.net&port=1000&id=pf-some70&life=life-5&session=10&pw=1212qqqq
            Map<String, String> parameters = parseOptions(options);

            if (parameters.get("host") != null) {
                data.host = parameters.get("host");
            }

            if (parameters.get("port") != null) {
                data.port = parameters.get("port");
            }

            if (parameters.get("id") != null) {
                data.id = parameters.get("id");
            }

            if (!TextUtils.isEmpty(data.id) && parameters.get("session") != null) {
                int session = Integer.parseInt(parameters.get("session"));
                String randomString = Utility.getRandomString(session);
                data.id += "_session-" + randomString;
            }

            if (!TextUtils.isEmpty(data.id) && parameters.get("life") != null) {
                data.id += "_" + parameters.get("life");
            }

            if (parameters.get("pw") != null) {
                data.pw = parameters.get("pw");
            }

            Log.d(TAG, "proxy - " + data.host + ":" + data.port + "@" + data.id + ":" + data.pw);
        }

        return data;
    }

    private Map<String, String> parseOptions(String options) {
        Map<String, String> parameters = new HashMap<>();
        String[] optionList = options.split("&");

        for (String option : optionList) {
            String[] optionPair = option.split("=", 2);
            parameters.put(optionPair[0], (optionPair.length > 1) ? optionPair[1] : "");
        }

        return parameters;
    }
}
