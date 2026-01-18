package com.sec.android.app.sbrowser.library.proxy.ip2world.client;

import android.text.TextUtils;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.Utility;
import com.sec.android.app.sbrowser.library.proxy.common.client.ProxyClient;
import com.sec.android.app.sbrowser.library.proxy.common.retrofit.models.ProxyUserAuthData;
import com.sec.android.app.sbrowser.library.proxy.ip2world.retrofit.Ip2WorldService;
import com.sec.android.app.sbrowser.library.proxy.ip2world.retrofit.models.Ip2WorldData;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class Ip2WorldClient extends ProxyClient {

    private static final String TAG = Ip2WorldClient.class.getSimpleName();
    private static final String BASE_URL = "http://api.proxy.ip2world.com/";

    private Ip2WorldService _service = null;

    protected Ip2WorldClient() {

        // Create an instance of our API interface.
        _service = createRetrofit(BASE_URL).create(Ip2WorldService.class);
    }

    public void getProxyIp(String options, final Callback callback) {
        String count = "1";
        String returnType = "json";
        String protocol = "http";
        String country = null;
        String lb = "1";

        if (!TextUtils.isEmpty(options)) {
            Map<String, String> parsedOptions = parseOptions(options);
            if (parsedOptions.get("num") != null) {
                count = parsedOptions.get("num");
            }

            if (parsedOptions.get("return_type") != null) {
                returnType = parsedOptions.get("return_type");
            }

            if (parsedOptions.get("protocol") != null) {
                protocol = parsedOptions.get("protocol");
            }

            if (parsedOptions.get("regions") != null) {
                country = parsedOptions.get("regions");
            }

            if (parsedOptions.get("lb") != null) {
                lb = parsedOptions.get("lb");
            }
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("num", count);
        parameters.put("return_type", returnType);
        parameters.put("protocol", protocol);

        if (country != null) {
            parameters.put("regions", country);
        }

        parameters.put("lb", lb);

//        http://api.proxy.ip2world.com/getProxyIp?return_type=json&protocol=http&num=1&regions=in&lb=1
//        http://api.proxy.ip2world.com/getProxyIp?lb=1&return_type=json&protocol=socks5&num=1&regions=in
//        http://api.proxy.ip2world.com/getProxyIp?return_type=json&protocol=http&num=1&lb=1

        Call<ResponseBody> call = _service.getProxyIp(parameters);
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Ip2WorldData data = convertResponse(callback, response, Ip2WorldData.class);

                if (data != null) {
//                    try {
                        Log.d(TAG, response.toString());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    int size = 0;
                    if (data.data != null) {
                        size = data.data.size();
                    }

                    Log.d(TAG, "getProxyIp success: " + size + "개");
                    finishSuccessCallback(callback, data);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }


    public ProxyUserAuthData getProxyData(String options) {
        ProxyUserAuthData data = new ProxyUserAuthData();

        if (!TextUtils.isEmpty(options)) {
            //pr-us.ip2world.com:6001:some70-zone-resi-session-038b23c53651-sessTime-5:1212qqqq
            //pr-sg.ip2world.com:6001:some70-zone-resi-region-in-st-kerala-city-lucknow-session-69026a1f4ac1-sessTime-5:1212qqqq
            //host=pr-sg.ip2world.com&port=6001&id=some70&session=12&sessTime=sessTime-5&pw=1212qqqq
            Map<String, String> parameters = parseOptions(options);

            if (parameters.get("host") != null) {
                data.host = parameters.get("host");
            }

            if (parameters.get("port") != null) {
                data.port = parameters.get("port");
            }

            if (parameters.get("id") != null) {
                data.id = parameters.get("id") + "-zone-resi";
            }

            if (!TextUtils.isEmpty(data.id) && parameters.get("session") != null) {
                int session = Integer.parseInt(parameters.get("session"));
                String randomString = Utility.getRandomString(session);
                data.id += "-session-" + randomString.toLowerCase();
            }

            if (!TextUtils.isEmpty(data.id) && parameters.get("sessTime") != null) {
                data.id += "-" + parameters.get("sessTime");
            }

            if (parameters.get("pw") != null) {
                data.pw = parameters.get("pw");
            }

            Log.d(TAG, "proxy - " + data.host + ":" + data.port + "@" + data.id + ":" + data.pw);
        }

        return data;
    }
}
