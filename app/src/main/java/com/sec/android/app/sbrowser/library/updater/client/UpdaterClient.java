package com.sec.android.app.sbrowser.library.updater.client;

import android.util.Log;

import com.google.gson.Gson;
import com.sec.android.app.sbrowser.BuildConfig;
import com.sec.android.app.sbrowser.library.updater.retrofit.UpdaterService;
import com.sec.android.app.sbrowser.library.updater.retrofit.models.VersionData;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UpdaterClient {

    private static final String TAG = UpdaterClient.class.getSimpleName();

    public static final int RESPONSE_NO_BODY = -1000;
    public static final int RESPONSE_FAILED = -2000;

//    private static final String BASE_URL = "http://54.180.205.28/zero/api/";
    private static final String BASE_URL = BuildConfig.SERVER_URL;

    private UpdaterService _service;

    protected UpdaterClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .build();

        // Create an instance of our API interface.
        _service = retrofit.create(UpdaterService.class);
    }

    public void getVersion(final Callback callback, int appNo, int versionCode) {
        Call<ResponseBody> call = _service.getVersion(appNo, versionCode);
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                VersionData data = convertResponse(callback, response, VersionData.class);

                if (data != null) {
                    Log.d(TAG, "getVersion success: " + data.versionCode);
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
