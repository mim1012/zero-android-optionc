package com.sec.android.app.sbrowser.library.naver.client;

import android.util.Log;

import com.sec.android.app.sbrowser.library.common.client.CommonClient;
import com.sec.android.app.sbrowser.library.naver.retrofit.NaverShoppingService;
import com.sec.android.app.sbrowser.library.naver.retrofit.models.ShoppingSearchData;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class NaverShoppingClient extends CommonClient {

    private static final String TAG = NaverShoppingClient.class.getSimpleName();
    private static final String BASE_URL = "https://msearch.shopping.naver.com/api/";

    private NaverShoppingService _service = null;

    protected NaverShoppingClient() {
        // Create an instance of our API interface.
        _service = createRetrofit(BASE_URL).create(NaverShoppingService.class);
    }

    public void getSearchAll(Map<String, String> headers, String query, int pagingIndex, final Callback callback) {
        String adQuery = query;
        String origQuery = query;
        int pagingSize = 40;
        String productSet = "total";
        String sort = "rel";
        String viewType = "list";

        //adQuery=%EC%9E%90%EC%A0%84%EA%B1%B0&origQuery=%EC%9E%90%EC%A0%84%EA%B1%B0&pagingIndex=2&pagingSize=40&productSet=total&query=%EC%9E%90%EC%A0%84%EA%B1%B0&sort=rel&viewType=list
        Call<ResponseBody> call = _service.getSearchAll(headers, adQuery, origQuery, pagingIndex, pagingSize, productSet, query, sort, viewType);
        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ShoppingSearchData data = convertResponse(callback, response, ShoppingSearchData.class);

                if (data != null) {
//                    try {
                        Log.d(TAG, response.toString());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    Log.d(TAG, "getProxyIp success: " + data.shoppingResult.total + "개 중 " + pagingIndex + "p " + data.shoppingResult.productCount + "개");
                    finishSuccessCallback(callback, data);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                finishFailedCallback(callback, -1, -1, "실패 3 통신 에러 " + t.getLocalizedMessage());
            }
        });
    }
}
