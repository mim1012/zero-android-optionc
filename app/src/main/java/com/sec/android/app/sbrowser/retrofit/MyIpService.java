package com.sec.android.app.sbrowser.retrofit;

import com.sec.android.app.sbrowser.models.MyIpData;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public interface MyIpService {

    @GET("/")
    Call<MyIpData> getMyIp();
}
