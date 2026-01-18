package com.sec.android.app.sbrowser.library.proxy.common.client;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ProxyClient {

    private static final String TAG = ProxyClient.class.getSimpleName();

    public static final int RESPONSE_NO_BODY = -1000;
    public static final int RESPONSE_FAILED = -2000;

    protected ProxyClient() {
    }

    // Ssl Proxy need okhttp3 3.12 over.
//    public static final String PROXY_HOST = BuildConfig.PROXY_HOST;
//    public static final int PROXY_PORT = BuildConfig.PROXY_PORT;
//    public static final String PROXY_USERNAME = BuildConfig.PROXY_USERNAME;
//    public static final String PROXY_PASSWORD = BuildConfig.PROXY_PASSWORD;

    protected Retrofit createRetrofit(String url) {
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
                .baseUrl(url)
                .client(client)
                .build();

        return retrofit;
    }

    public interface Callback {
        <T> void finishSuccess(T data);
        void finishFailed(int response, int code, String message);
    }


    protected Map<String, String> parseOptions(String options) {
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
    protected <T> T convertResponse(Callback callback, Response<ResponseBody> response, Class<T> classOfT) {
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

    protected <T> void finishSuccessCallback(Callback callback, T data) {
        if (callback != null) {
            callback.finishSuccess(data);
        }
    }

    protected void finishFailedCallback(Callback callback, int response, int code, String message) {
        Log.d(TAG, (message != null) ? message : "finishFailed");

        if (callback != null) {
            callback.finishFailed(response, code, message);
        }
    }
}
