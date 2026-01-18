package com.sec.android.app.sbrowser.library.proxy.awm_proxy.retrofit;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public interface AwmProxyService {

    @GET("proxy/{key}")
    Call<ResponseBody> getProxyIp(
            @Path("key") String key,
            @QueryMap Map<String, String> options);
}
