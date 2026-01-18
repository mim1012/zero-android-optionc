package com.sec.android.app.sbrowser.library.proxy.luna_proxy.client;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.sec.android.app.sbrowser.library.proxy.luna_proxy.retrofit.LunaProxyService;
import com.sec.android.app.sbrowser.library.proxy.luna_proxy.retrofit.models.LunaProxyData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LunaProxyClient {

    private static final String TAG = LunaProxyClient.class.getSimpleName();

    public static final int RESPONSE_NO_BODY = -1000;
    public static final int RESPONSE_FAILED = -2000;

    private static final String BASE_URL = "https://tq.lunaproxy.com/";

    private LunaProxyService _service = null;


    // Ssl Proxy need okhttp3 3.12 over.
//    public static final String PROXY_HOST = BuildConfig.PROXY_HOST;
//    public static final int PROXY_PORT = BuildConfig.PROXY_PORT;
//    public static final String PROXY_USERNAME = BuildConfig.PROXY_USERNAME;
//    public static final String PROXY_PASSWORD = BuildConfig.PROXY_PASSWORD;

    protected LunaProxyClient() {
//        Authenticator proxyAuthenticator = new Authenticator() {
//            @Override
//            public Request authenticate(Route route, okhttp3.Response response) throws IOException {
//                String credential = Credentials.basic(PROXY_USERNAME, PROXY_PASSWORD);
//                return response.request().newBuilder()
//                        .header("Proxy-Authorization", credential)
//                        .build();
//            }
//        };

        //Setup the client
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
//                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT)))
//                .proxyAuthenticator(proxyAuthenticator)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .build();

        // Create an instance of our API interface.
        _service = retrofit.create(LunaProxyService.class);
    }

    public void getProxyIp(String key, String options, final Callback callback) {
        String count = "1";
        String country = null;
        String service = null;
        String ipMode = null;

        if (!TextUtils.isEmpty(options)) {
            Map<String, String> parameters = parseOptions(options);
            if (parameters.get("service") != null) {
                service = parameters.get("service");
            }

            if (parameters.get("count") != null) {
                count = parameters.get("count");
            }

            if (parameters.get("country") != null) {
                country = parameters.get("country");
            }

            if (parameters.get("ip_si") != null) {
                ipMode = parameters.get("ip_si");
            }
        }

        getProxyIp(service, key, count, country, ipMode, callback);
    }

    public void getProxyIp(String service, String key, String count, String country, String ipMode, final Callback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("neek", "" + key);
        parameters.put("num", "" + count);

//        parameters.put("type", "1");    //TXT
        parameters.put("type", "2");

        parameters.put("sep", "1");

        if (country != null) {
            parameters.put("regions", country);
        } else {
            parameters.put("regions", "all");
        }

//        https://tq.lunaproxy.com/getflowip?neek=1086874&num=1&type=2&sep=1&regions=all&ip_si=1&level=1&sb=
//        https://tq.lunaproxy.com/get_dynamic_ip?neek=1086877&num=1&type=2&sep=1&regions=all&ip_si=5&sb=
        Call<ResponseBody> call;

        if (service.equals("get_dynamic_ip")) {
            if (TextUtils.isEmpty(ipMode)) {
                parameters.put("ip_si", "5");   // Sticky IP
//                parameters.put("ip_si", "6");   // Rotation IP
            } else {
                parameters.put("ip_si", ipMode);
            }

            parameters.put("sb", "");
            call = _service.getDynamicIp(parameters);
        } else {
            if (TextUtils.isEmpty(ipMode)) {
                parameters.put("ip_si", "1");   // Sticky IP
//                parameters.put("ip_si", "2");   // Rotation IP
            } else {
                parameters.put("ip_si", ipMode);
            }

            parameters.put("level", "1");
            parameters.put("sb", "");
            call = _service.getFlowIp(parameters);
        }

        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                LunaProxyData data = convertResponse(callback, response, LunaProxyData.class);

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

    public interface Callback {
        <T> void finishSuccess(T data);
        void finishFailed(int response, int code, String message);
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

    /**
     * If return value is null, called finishFailedCallback.
     *
     * @param callback
     * @param response
     * @param classOfT
     * @param <T>
     * @return
     */
    private <T> T convertResponse(Callback callback, Response<ResponseBody> response, Class<T> classOfT) {
        int responseCode = response.code();
        if (response.isSuccessful()) {
            if (response.body() != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                try {
                    String body = response.body().string();

                    try {
                        // Covert json body to object.
                        Gson gson = new Gson();
                        return gson.fromJson(body, classOfT);
                    } catch (Exception e) {
                        // Support text body.
                        //e.printStackTrace();
                        Log.d(TAG, "[" + responseCode + "] " + body);
                        finishFailedCallback(callback, responseCode, -2, body);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    finishFailedCallback(callback, responseCode, -3, "IOException");
                }
            } else {
                Log.d(TAG, "[" + responseCode + "] 실패 1 response 내용이 없음");
                finishFailedCallback(callback, responseCode, RESPONSE_NO_BODY, "실패 1 response 내용이 없음");
            }
        } else {
            Log.d(TAG, "[" + responseCode + "] 실패 2 서버 에러" + response.body() + "/" + response.message());
            finishFailedCallback(callback, responseCode, RESPONSE_FAILED, "실패 2 서버 에러");
        }

        return null;
    }

    private <T> void finishSuccessCallback(Callback callback, T data) {
        if (callback != null) {
            callback.finishSuccess(data);
        }
    }

    private void finishFailedCallback(Callback callback, int response, int code, String message) {
        Log.d(TAG, (message != null) ? message : "finishFailed");

        if (callback != null) {
            callback.finishFailed(response, code, message);
        }
    }
}
