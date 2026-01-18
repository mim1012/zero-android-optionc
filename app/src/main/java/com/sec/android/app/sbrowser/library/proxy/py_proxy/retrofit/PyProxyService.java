package com.sec.android.app.sbrowser.library.proxy.py_proxy.retrofit;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public interface PyProxyService {

    @GET("getProxyIp")
    Call<ResponseBody> getProxyIp(@QueryMap Map<String, String> options);
}
