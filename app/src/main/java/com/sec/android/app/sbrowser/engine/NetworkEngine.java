package com.sec.android.app.sbrowser.engine;

import android.util.Log;

import com.sec.android.app.sbrowser.BuildConfig;
import com.sec.android.app.sbrowser.models.AccountData;
import com.sec.android.app.sbrowser.models.BaseData;
import com.sec.android.app.sbrowser.models.BookmarkData;
import com.sec.android.app.sbrowser.models.DeviceInfoData;
import com.sec.android.app.sbrowser.models.DeviceIpData;
import com.sec.android.app.sbrowser.models.KeywordData;
import com.sec.android.app.sbrowser.models.LoginCookieData;
import com.sec.android.app.sbrowser.models.MyIpData;
import com.sec.android.app.sbrowser.models.NnbData;
import com.sec.android.app.sbrowser.retrofit.MyIpService;
import com.sec.android.app.sbrowser.retrofit.Service;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public class NetworkEngine {

    private static final String TAG = "NetworkEngine";

    public static final int RESPONSE_NO_BODY = -1000;
    public static final int RESPONSE_FAILED = -2000;


    private Service _service = null;
    private Service _serviceMoon = null;
    private MyIpService _myIpService = null;
    private String  _authCode = null;

    private static class LazyHolder {
        public static final NetworkEngine INSTANCE = new NetworkEngine();
    }

    public static NetworkEngine getInstance() {
        return LazyHolder.INSTANCE;
    }

    public Service getService() {
        return _service;
    }

    public MyIpService getMyIpService() {
        return _myIpService;
    }

    public void getMyIp(final Callback callback) {
        Call<MyIpData> call = getMyIpService().getMyIp();

        call.enqueue(new retrofit2.Callback<MyIpData>() {
            @Override
            public void onResponse(Call<MyIpData> call, Response<MyIpData> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                        Log.d(TAG, "getMyIp success");
                        MyIpData data = response.body();
                        callback.finishSuccess(data);
                    } else {
                        Log.d(TAG, "[" + response.code() + "] 실패 1 response 내용이 없음");
                        if (callback != null) {
                            callback.finishFailed(response.code(), RESPONSE_NO_BODY, "response 내용이 없음");
                        }
                    }
                } else {
                    Log.d(TAG, "[" + response.code() + "] 실패 2 서버 에러" + response.body() + "/" + response.message());
                    if (callback != null) {
                        callback.finishFailed(response.code(), RESPONSE_FAILED, "서버 에러");
                    }
                }
            }

            @Override
            public void onFailure(Call<MyIpData> call, Throwable t) {
                Log.d(TAG, "실패 3 통신 에러 " + t.getLocalizedMessage());
                if (callback != null) {
                    callback.finishFailed(-1, -1, "통신 에러 " + t.getLocalizedMessage());
                }
            }
        });
    }

    public void getNnb(final Callback callback, String loginId, String imei) {
        Call<NnbData> call = getService().getNnb(loginId, imei);
        call.enqueue(new retrofit2.Callback<NnbData>() {
            @Override
            public void onResponse(Call<NnbData> call, Response<NnbData> response) {
                NnbData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    Log.d(TAG, "getNnb success: " + data.nnb + " / " + data.uaId + ", " + data.ua);
                    finishSuccessCallback(callback, data);
                }
            }

            @Override
            public void onFailure(Call<NnbData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void getNnbPc(final Callback callback, String loginId, String imei) {
        Call<NnbData> call = getService().getNnbPc(loginId, imei);
        call.enqueue(new retrofit2.Callback<NnbData>() {
            @Override
            public void onResponse(Call<NnbData> call, Response<NnbData> response) {
                NnbData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    Log.d(TAG, "getNnb Pc success: " + data.nnb + " / " + data.uaId + ", " + data.ua);
                    finishSuccessCallback(callback, data);
                }
            }

            @Override
            public void onFailure(Call<NnbData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void registerNnb(final Callback callback, String nnb, String ua) {
        Call<NnbData> call = _serviceMoon.getNnb();
        call.enqueue(new retrofit2.Callback<NnbData>() {
            @Override
            public void onResponse(Call<NnbData> call, Response<NnbData> response) {
                NnbData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    Log.d(TAG, "registerNnb success: " + data.nnb + " / " + data.uaId + ", " + data.ua);
                    finishSuccessCallback(callback, data);
                }
            }

            @Override
            public void onFailure(Call<NnbData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });

//        Call<BaseData> call = getService().registerNnb(nnb, ua);
//        call.enqueue(new retrofit2.Callback<BaseData>() {
//            @Override
//            public void onResponse(Call<BaseData> call, Response<BaseData> response) {
//                BaseData data = convertResponse(callback, response);
//
//                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
//                    if (data.status != 0) {
//                        Log.d(TAG, "[" + response.code() + "] error(" + data.error.code + "): " + data.error.message);
//                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
//                    } else {
//                        Log.d(TAG, "registerNnb success");
//                        finishSuccessCallback(callback, data);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<BaseData> call, Throwable t) {
//                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
//            }
//        });
    }

    public void getUa(final Callback callback, String loginId, String imei) {
        Call<NnbData> call = getService().getUa(loginId, imei);
        call.enqueue(new retrofit2.Callback<NnbData>() {
            @Override
            public void onResponse(Call<NnbData> call, Response<NnbData> response) {
                NnbData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    Log.d(TAG, "getUa success: " + data.uaId + ", " + data.ua);
                    finishSuccessCallback(callback, data);
                }
            }

            @Override
            public void onFailure(Call<NnbData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void getUaPc(final Callback callback, String loginId, String imei) {
        Call<NnbData> call = getService().getUaPc(loginId, imei);
        call.enqueue(new retrofit2.Callback<NnbData>() {
            @Override
            public void onResponse(Call<NnbData> call, Response<NnbData> response) {
                NnbData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    Log.d(TAG, "getUaPc success: " + data.uaId + ", " + data.ua);
                    finishSuccessCallback(callback, data);
                }
            }

            @Override
            public void onFailure(Call<NnbData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void registerPlaceFinish(final Callback callback, String userid, String uid, String keyword, String company, String url, String agency, String account, String ranking) {
        Call<BaseData> call = getService().registerPlaceFinish(userid, uid, keyword, company, url, agency, account, ranking);
        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {
                BaseData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error(" + data.error.code + "): " + data.error.message);
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "registerPlaceFinish success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void registerRank(final Callback callback, String worker, String category, String uid, String mid1, String mid2, int rank1Page, int rank1Grade, int rank2Page, int rank2Grade) {
        Call<BaseData> call = getService().registerRank(worker, category, uid, mid1, mid2, rank1Page, rank1Grade, rank2Page, rank2Grade);
        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {
                BaseData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error(" + data.error.code + "): " + data.error.message);
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "registerRank success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void getDeviceIp(final Callback callback) {
        Call<DeviceIpData> call = getService().getDeviceIp();

        call.enqueue(new retrofit2.Callback<DeviceIpData>() {
            @Override
            public void onResponse(Call<DeviceIpData> call, Response<DeviceIpData> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.

                        DeviceIpData data = response.body();

                        if (data.status != 0) {
                            Log.d(TAG, "[" + response.code() + "] error(" + data.error.code + "): " + data.error.message);
                            if (callback != null) {
                                callback.finishFailed(response.code(), data.error.code, data.error.message);
                            }
                        } else {
                            Log.d(TAG, "getDeviceIp success");
                            if (callback != null) {
                                callback.finishSuccess(data);
                            }
                        }
                    } else {
                        Log.d(TAG, "[" + response.code() + "] 실패 1 response 내용이 없음");
                        if (callback != null) {
                            callback.finishFailed(response.code(), RESPONSE_NO_BODY, "response 내용이 없음");
                        }
                    }
                } else {
                    Log.d(TAG, "[" + response.code() + "] 실패 2 서버 에러" + response.body() + "/" + response.message());
                    if (callback != null) {
                        callback.finishFailed(response.code(), RESPONSE_FAILED, "서버 에러");
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceIpData> call, Throwable t) {
                Log.d(TAG, "실패 3 통신 에러 " + t.getLocalizedMessage());
                if (callback != null) {
                    callback.finishFailed(-1, -1, "통신 에러 " + t.getLocalizedMessage());
                }
            }
        });
    }

    public void getDeviceInfo(final Callback callback, String loginId, String imei) {
        Call<DeviceInfoData> call = getService().getDeviceInfo(
                loginId,
                imei);

        call.enqueue(new retrofit2.Callback<DeviceInfoData>() {
            @Override
            public void onResponse(Call<DeviceInfoData> call, Response<DeviceInfoData> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.

                        DeviceInfoData data = response.body();

                        if (data.status != 0) {
                            Log.d(TAG, "[" + response.code() + "] error(" + data.error.code + "): " + data.error.message);
                            if (callback != null) {
                                callback.finishFailed(response.code(), data.error.code, data.error.message);
                            }
                        } else {
                            Log.d(TAG, "getDeviceInfo success");
                            if (callback != null) {
                                callback.finishSuccess(data);
                            }
                        }
                    } else {
                        Log.d(TAG, "[" + response.code() + "] 실패 1 response 내용이 없음");
                        if (callback != null) {
                            callback.finishFailed(response.code(), RESPONSE_NO_BODY, "response 내용이 없음");
                        }
                    }
                } else {
                    Log.d(TAG, "[" + response.code() + "] 실패 2 서버 에러" + response.body() + "/" + response.message());
                    if (callback != null) {
                        callback.finishFailed(response.code(), RESPONSE_FAILED, "서버 에러");
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceInfoData> call, Throwable t) {
                Log.d(TAG, "실패 3 통신 에러 " + t.getLocalizedMessage());
                if (callback != null) {
                    callback.finishFailed(-1, -1, "통신 에러 " + t.getLocalizedMessage());
                }
            }
        });
    }

    public void checkIp(final Callback callback, String loginId, String imei) {
    //   Call<BaseData> call = _service.checkIp(loginId, imei);
        Call<BaseData> call = getService().checkIp(loginId, imei);
        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {
                BaseData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error(" + data.error.code + "): " + data.error.message);
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "checkIp success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }


    public void registerDevice(final Callback callback, String loginId, String imei,
                               String versionCode, String gmsVersion, String webviewVersion, String updaterVersion,
                               String model, String telecom, int battery, int batteryHealth) {
        Call<DeviceInfoData> call = getService().registerDevice(
                loginId, imei, versionCode, gmsVersion, webviewVersion, updaterVersion, model, telecom, battery, batteryHealth);

        call.enqueue(new retrofit2.Callback<DeviceInfoData>() {
            @Override
            public void onResponse(Call<DeviceInfoData> call, Response<DeviceInfoData> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.

                        BaseData data = response.body();

                        if (data.status != 0) {
                            Log.d(TAG, "[" + response.code() + "] error(" + data.error.code + "): " + data.error.message);
                            if (callback != null) {
                                callback.finishFailed(response.code(), data.error.code, data.error.message);
                            }
                        } else {
                            Log.d(TAG, "registerDevice success");
                            if (callback != null) {
                                callback.finishSuccess(data);
                            }
                        }
                    } else {
                        Log.d(TAG, "[" + response.code() + "] 실패 1 response 내용이 없음");
                        if (callback != null) {
                            callback.finishFailed(response.code(), RESPONSE_NO_BODY, "response 내용이 없음");
                        }
                    }
                } else {
                    Log.d(TAG, "[" + response.code() + "] 실패 2 서버 에러" + response.body() + "/" + response.message());
                    if (callback != null) {
                        callback.finishFailed(response.code(), RESPONSE_FAILED, "서버 에러");
                    }
                }
            }

            @Override
            public void onFailure(Call<DeviceInfoData> call, Throwable t) {
                Log.d(TAG, "실패 3 통신 에러 " + t.getLocalizedMessage());
                if (callback != null) {
                    callback.finishFailed(-1, -1, "통신 에러 " + t.getLocalizedMessage());
                }
            }
        });
    }

    public void getKeywords(final Callback callback, String loginId, String imei, int uaId) {
        Call<KeywordData> call = getService().getKeywords(loginId, imei, uaId);
        call.enqueue(new retrofit2.Callback<KeywordData>() {
            @Override
            public void onResponse(Call<KeywordData> call, Response<KeywordData> response) {
                KeywordData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "getKeywords success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<KeywordData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void registerFinish(final Callback callback, int keywordId, String loginId, String imei, int workId, int result, int workCode) {
        Call<BaseData> call = getService().registerFinish(keywordId, loginId, imei, workId, result, workCode);
        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {
                BaseData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "registerFinish success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void checkBookmark(final Callback callback, String loginId, String imei, int accountAuthId) {
        Call<BookmarkData> call = getService().checkBookmark(loginId, imei, accountAuthId);
        call.enqueue(new retrofit2.Callback<BookmarkData>() {
            @Override
            public void onResponse(Call<BookmarkData> call, Response<BookmarkData> response) {
                BookmarkData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "checkBookmark success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<BookmarkData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void checkBookmark(final Callback callback, String loginId, String imei, String nidAut) {
        Call<BookmarkData> call = getService().checkBookmark(loginId, imei, nidAut);
        call.enqueue(new retrofit2.Callback<BookmarkData>() {
            @Override
            public void onResponse(Call<BookmarkData> call, Response<BookmarkData> response) {
                BookmarkData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "checkBookmark success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<BookmarkData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void registerBookmarkFinish(final Callback callback, String loginId, String imei, int bookmarkId, int result, int workCode) {
        Call<BaseData> call;

        if (workCode > 0) {
            call = getService().registerBookmarkFinish(loginId, imei, bookmarkId, result, workCode);
        } else {
            call = getService().registerBookmarkFinish(loginId, imei, bookmarkId, result);
        }

        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {
                BaseData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "registerBookmarkFinish success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void updateNaverAccountStatus(final Callback callback, String accountId, String loginId, String imei, int status) {
        Call<BaseData> call = getService().updateNaverAccountStatus(accountId, loginId, imei, status);
        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {
                BaseData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "updateNaverAccountStatus success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void getNaverAccount(final Callback callback, String loginId, String imei, int trafficId, int keywordId) {
        Call<AccountData> call = getService().getNaverAccount(loginId, imei, trafficId, keywordId);
        call.enqueue(new retrofit2.Callback<AccountData>() {
            @Override
            public void onResponse(Call<AccountData> call, Response<AccountData> response) {
                AccountData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "getNaverAccount success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<AccountData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void getNaverAuthAccount(final Callback callback, String loginId, String imei) {
        Call<AccountData> call = getService().getNaverAuthAccount(loginId, imei, 1);
        call.enqueue(new retrofit2.Callback<AccountData>() {
            @Override
            public void onResponse(Call<AccountData> call, Response<AccountData> response) {
                AccountData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "getNaverAuthAccount success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<AccountData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void registerNaverAuthAccountCookie(final Callback callback, String loginId, String imei, String naverId, String data, String ua) {
        Call<LoginCookieData> call = getService().registerNaverAuthAccountCookie(loginId, imei, naverId, data, ua);
        call.enqueue(new retrofit2.Callback<LoginCookieData>() {
            @Override
            public void onResponse(Call<LoginCookieData> call, Response<LoginCookieData> response) {
                LoginCookieData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "updateNaverAuthAccountCookie success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginCookieData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void getKeywordsForRankCheck(final Callback callback, String loginId, String imei) {
        Call<KeywordData> call = getService().getKeywordsForRankCheck(loginId, imei);
        call.enqueue(new retrofit2.Callback<KeywordData>() {
            @Override
            public void onResponse(Call<KeywordData> call, Response<KeywordData> response) {
                KeywordData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "getKeywordsForRankCheck success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<KeywordData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void updateKeywordRank(final Callback callback, int keywordId, String loginId, String imei, int rank, int subRank) {
        Call<BaseData> call = getService().updateKeywordRank(keywordId, loginId, imei, rank, subRank);
        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {
                BaseData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "updateKeywordRank success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void updateProductInfo(final Callback callback, int keywordId, String loginId, String imei, String productName, String storeName, String mallId, String catId, String productUrl, String sourceType, String sourceUrl) {
        Call<BaseData> call = getService().updateProductInfo(keywordId, loginId, imei, productName, storeName, mallId, catId, productUrl, sourceType, sourceUrl);
        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {
                BaseData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "updateProductInfo success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

//    public void getKeywords(final Callback callback, String loginId, String imei) {
//        Call<KeywordData> call = getService().getKeywords(
//                loginId,
//                imei);
//
//        call.enqueue(new retrofit2.Callback<KeywordData>() {
//            @Override
//            public void onResponse(Call<KeywordData> call, Response<KeywordData> response) {
//
//                if (response.isSuccessful()) {
//                    if (response.body() != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
//
//                        KeywordData data = response.body();
//
//                        if (data.status != 0) {
//                            Log.d(TAG, "[" + response.code() + "] error(" + data.error.code + "): " + data.error.message);
//                            if (callback != null) {
//                                callback.finishFailed(response.code(), data.error.code, data.error.message);
//                            }
//                        } else {
//                            Log.d(TAG, "getKeywords success");
//                            if (callback != null) {
//                                callback.finishSuccess(data);
//                            }
//                        }
//                    } else {
//                        Log.d(TAG, "[" + response.code() + "] 실패 1 response 내용이 없음");
//                        if (callback != null) {
//                            callback.finishFailed(response.code(), RESPONSE_NO_BODY, "response 내용이 없음");
//                        }
//                    }
//                } else {
//                    Log.d(TAG, "[" + response.code() + "] 실패 2 서버 에러" + response.body() + "/" + response.message());
//                    if (callback != null) {
//                        callback.finishFailed(response.code(), RESPONSE_FAILED, "서버 에러");
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<KeywordData> call, Throwable t) {
//                Log.d(TAG, "실패 3 통신 에러 " + t.getLocalizedMessage());
//                if (callback != null) {
//                    callback.finishFailed(-1, -1, "통신 에러 " + t.getLocalizedMessage());
//                }
//            }
//        });
//    }

    public void registerNaverKeywordRank(final Callback callback, int keywordId, String loginId, String imei, int rank) {
        Call<BaseData> call = getService().registerNaverKeywordRank(
                keywordId,
                loginId,
                imei,
                rank);

        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.

                        BaseData data = response.body();

                        if (data.status != 0) {
                            Log.d(TAG, "[" + response.code() + "] error(" + data.error.code + "): " + data.error.message);
                            if (callback != null) {
                                callback.finishFailed(response.code(), data.error.code, data.error.message);
                            }
                        } else {
                            Log.d(TAG, "registerNaverKeywordRank success");
                            if (callback != null) {
                                callback.finishSuccess(data);
                            }
                        }
                    } else {
                        Log.d(TAG, "[" + response.code() + "] 실패 1 response 내용이 없음");
                        if (callback != null) {
                            callback.finishFailed(response.code(), RESPONSE_NO_BODY, "response 내용이 없음");
                        }
                    }
                } else {
                    Log.d(TAG, "[" + response.code() + "] 실패 2 서버 에러" + response.body() + "/" + response.message());
                    if (callback != null) {
                        callback.finishFailed(response.code(), RESPONSE_FAILED, "서버 에러");
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                Log.d(TAG, "실패 3 통신 에러 " + t.getLocalizedMessage());
                if (callback != null) {
                    callback.finishFailed(-1, -1, "통신 에러 " + t.getLocalizedMessage());
                }
            }
        });
    }

    public void finishNaverKeyword(final Callback callback, int keywordId, String loginId, String imei, int rank) {
        Call<BaseData> call = getService().finishNaverKeyword(
                keywordId,
                loginId,
                imei,
                rank);

        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.

                        BaseData data = response.body();

                        if (data.status != 0) {
                            Log.d(TAG, "[" + response.code() + "] error(" + data.error.code + "): " + data.error.message);
                            if (callback != null) {
                                callback.finishFailed(response.code(), data.error.code, data.error.message);
                            }
                        } else {
                            Log.d(TAG, "finishNaverKeyword success");
                            if (callback != null) {
                                callback.finishSuccess(data);
                            }
                        }
                    } else {
                        Log.d(TAG, "[" + response.code() + "] 실패 1 response 내용이 없음");
                        if (callback != null) {
                            callback.finishFailed(response.code(), RESPONSE_NO_BODY, "response 내용이 없음");
                        }
                    }
                } else {
                    Log.d(TAG, "[" + response.code() + "] 실패 2 서버 에러" + response.body() + "/" + response.message());
                    if (callback != null) {
                        callback.finishFailed(response.code(), RESPONSE_FAILED, "서버 에러");
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                Log.d(TAG, "실패 3 통신 에러 " + t.getLocalizedMessage());
                if (callback != null) {
                    callback.finishFailed(-1, -1, "통신 에러 " + t.getLocalizedMessage());
                }
            }
        });
    }

    // 만약 생성한 nnb 를 사용하게 된다면 useNnb 값을 true 로 넘겨서 사용처리를 해준다.
    public void registerNaverData(final Callback callback, String loginId, String imei, String nnb, String ua, boolean useNnb) {
        Call<BaseData> call;

        if (useNnb) {
            call = getService().registerNaverData(loginId, imei, nnb, ua, true);
        } else {
            call = getService().registerNaverData(loginId, imei, nnb, ua);
        }

        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.

                        BaseData data = response.body();

                        if (data.status != 0) {
                            Log.d(TAG, "[" + response.code() + "] error(" + data.error.code + "): " + data.error.message);
                            if (callback != null) {
                                callback.finishFailed(response.code(), data.error.code, data.error.message);
                            }
                        } else {
                            Log.d(TAG, "registerNaverData success");
                            if (callback != null) {
                                callback.finishSuccess(data);
                            }
                        }
                    } else {
                        Log.d(TAG, "[" + response.code() + "] 실패 1 response 내용이 없음");
                        if (callback != null) {
                            callback.finishFailed(response.code(), RESPONSE_NO_BODY, "response 내용이 없음");
                        }
                    }
                } else {
                    Log.d(TAG, "[" + response.code() + "] 실패 2 서버 에러" + response.body() + "/" + response.message());
                    if (callback != null) {
                        callback.finishFailed(response.code(), RESPONSE_FAILED, "서버 에러");
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                Log.d(TAG, "실패 3 통신 에러 " + t.getLocalizedMessage());
                if (callback != null) {
                    callback.finishFailed(-1, -1, "통신 에러 " + t.getLocalizedMessage());
                }
            }
        });
    }

    public void registerNaverCookieOthers(final Callback callback, String loginId, String imei, long cookieId, String others) {
        Call<BaseData> call = getService().registerNaverCookieOthers(loginId, imei, cookieId, others);
        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {
                BaseData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "registerNaverCookieOthers success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public void registerNaverCookieStatus(final Callback callback, String loginId, String imei, long loginCookieId, int status) {
        Call<BaseData> call = getService().registerNaverCookieStatus(loginId, imei, loginCookieId, status);
        call.enqueue(new retrofit2.Callback<BaseData>() {
            @Override
            public void onResponse(Call<BaseData> call, Response<BaseData> response) {
                BaseData data = convertResponse(callback, response);

                if (data != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                    if (data.status != 0) {
                        Log.d(TAG, "[" + response.code() + "] error.");
                        finishFailedCallback(callback, response.code(), data.error.code, data.error.message);
                    } else {
                        Log.d(TAG, "registerNaverCookieStatus success");
                        finishSuccessCallback(callback, data);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseData> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }

    public interface Callback { // 인터페이스는 외부에 구현해도 상관 없습니다.
        <T> void finishSuccess(T data);
        void finishFailed(int response, int code, String message);
    }

    /**
     * If return value is null, called finishFailedCallback.
     *
     * @param callback
     * @param response
     * @param <T>
     * @return
     */
    private <T> T convertResponse(Callback callback, Response<T> response) {
        int responseCode = response.code();
        if (response.isSuccessful()) {
            if (response.body() != null) { //null 뿐 아니라 오류 값이 들어올 때도 처리해줘야 함.
                return response.body();
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
        Log.d(TAG, message);

        if (callback != null) {
            callback.finishFailed(response, code, message);
        }
    }


    private NetworkEngine() {
        //Setup the client
//        OkHttpClient client = new OkHttpClient.Builder()
//                .cookieJar(new CookieJar() {
//
//                    @Override
//                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
//                    }
//
//                    @Override
//                    public List<Cookie> loadForRequest(HttpUrl url) {
////                        CookieSyncManager syncManager = CookieSyncManager.getInstance().;
//                        CookieManager cookieManager = CookieManager.getInstance();
//                        String cookie = cookieManager.getCookie(url.url().toString());
//                        return !cookie.isEmpty() ? Arrays.asList(Cookie.parse(url, cookie)) : Collections.EMPTY_LIST;
//                    }
//                })
//                .build();

        String url = BuildConfig.SERVER_URL;

//        if (BuildConfig.FLAVOR_build.contains("magic") && !BuildConfig.FLAVOR_mode.contains("child")) {
//        if (BuildConfig.FLAVOR_build.contains("magic")) {
//            String[] ips = {
//                    BuildConfig.SERVER_URL,
//                    BuildConfig.SERVER_URL,
//                    BuildConfig.SERVER_URL,
//                    BuildConfig.SERVER_URL,
//
//                    "http://52.79.98.223/zero/api/",    // api-1
//                    "http://52.79.98.223/zero/api/",    // api-1
//                    "http://52.79.98.223/zero/api/",    // api-1
//
//                    "http://43.200.65.170/zero/api/",    // api-2
//                    "http://43.200.65.170/zero/api/",    // api-2
//                    "http://43.200.65.170/zero/api/",    // api-2
//
//                    "http://13.209.118.52/zero/api/",    // api-3
//                    "http://13.209.118.52/zero/api/",    // api-3
//                    "http://13.209.118.52/zero/api/",    // api-3
//
//                    "http://15.165.171.135/zero/api/",    // api-4
//                    "http://15.165.171.135/zero/api/",    // api-4
//                    "http://15.165.171.135/zero/api/",    // api-4
//
//                    "http://52.79.251.139/zero/api/",    // api-5
//                    "http://52.79.251.139/zero/api/",    // api-5
//                    "http://52.79.251.139/zero/api/",    // api-5
//
//                    "http://13.125.158.94/zero/api/",    // api-6
//                    "http://13.125.158.94/zero/api/",    // api-6
//                    "http://13.125.158.94/zero/api/",    // api-6
//
//                    "http://13.209.47.166/zero/api/",    // api-7
//                    "http://13.209.47.166/zero/api/",    // api-7
//                    "http://13.209.47.166/zero/api/",    // api-7
//
//                    "http://3.34.98.242/zero/api/",    // api-8
//                    "http://3.34.98.242/zero/api/",    // api-8
//                    "http://3.34.98.242/zero/api/",    // api-8
//            };
//
//            int index = (int) MathHelper.randomRange(0, ips.length - 1);
//            url = ips[index];
//            Log.d(TAG, "대상인덱스: " + index + "");
//        }

        // timeout setting 해주기
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
                .build();

        // Create an instance of our API interface.
        _service = retrofit.create(Service.class);

        Retrofit myIpRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.myip.com")
                .addConverterFactory(GsonConverterFactory.create())
//                .client(client)
                .build();

        // Create an instance of our API interface.
        _myIpService = myIpRetrofit.create(MyIpService.class);

        // 서버 미사용.
//        Retrofit retrofitMoon = new Retrofit.Builder()
//                .baseUrl("http://198.13.48.146")
//                .addConverterFactory(GsonConverterFactory.create())
////                .client(client)
//                .build();
//
//        // Create an instance of our API interface.
//        _serviceMoon = retrofitMoon.create(Service.class);
    }
}
