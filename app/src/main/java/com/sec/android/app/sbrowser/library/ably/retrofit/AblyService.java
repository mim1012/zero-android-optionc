package com.sec.android.app.sbrowser.library.ably.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public interface AblyService {

    @GET("/productions/feed.json")
    Call<ResponseBody> getFeeds(
            @Query("v") String v,
            @Query("type") String type,
            @Query("query") String query,
            @Query("page") int page,
            @Query("per") int per);

    @GET("/productions/feed.json")
    Call<ResponseBody> getFeeds2(
            @Query("v") String v,
            @Query("query") String query,
            @Query("search_affect_type") String searchAffectType,
            @Query("page") int page,
            @Query("per") int per);
}
