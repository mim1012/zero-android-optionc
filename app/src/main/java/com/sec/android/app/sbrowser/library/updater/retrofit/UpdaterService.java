package com.sec.android.app.sbrowser.library.updater.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public interface UpdaterService {

    @GET("v1/mobile/version")
    Call<ResponseBody> getVersion(
            @Query("app") int appNo,
            @Query("version_code") int versionCode);
}
