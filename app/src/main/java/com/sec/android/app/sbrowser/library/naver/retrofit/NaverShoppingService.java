package com.sec.android.app.sbrowser.library.naver.retrofit;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public interface NaverShoppingService {

    @GET("/search/all")
    Call<ResponseBody> getSearchAll(
            @HeaderMap Map<String, String> headers,
            @Query("adQuery") String adQuery,
            @Query("origQuery") String origQuery,
            @Query("pagingIndex") int pagingIndex,
            @Query("pagingSize") int pagingSize,
            @Query("productSet") String productSet,
            @Query("query") String query,
            @Query("sort") String sort,
            @Query("viewType") String viewType);
}
