package com.sec.android.app.sbrowser.pattern.naver.rank;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.sec.android.app.sbrowser.engine.ThreadMutex;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpEngine;
import com.sec.android.app.sbrowser.library.common.client.CommonClient;
import com.sec.android.app.sbrowser.library.naver.NaverShoppingApi;
import com.sec.android.app.sbrowser.library.naver.retrofit.models.ShoppingSearchData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NaverShopProductsAction {

    private static final String TAG = NaverShopProductsAction.class.getSimpleName();

    private static final int MAX_RETRY_COUNT = 7;

    private final ThreadMutex _mutex = new ThreadMutex();

    private HttpEngine _httpEngine = null;
    private int _retryCount = 0;
    private int _result = 0;
    private ShoppingSearchData _searchData = null;

    public NaverShopProductsAction() {
    }

    public void setHttpEngine(HttpEngine httpEngine) {
        _httpEngine = httpEngine;
    }

    public ShoppingSearchData getSearchData() {
        return _searchData;
    }

    public int requestProducts(String sbth, String query, int pageIndex) {
        _retryCount = 0;
        _result = 0;
        _searchData = null;
        getProductFromServer2(sbth, query, pageIndex);
//        getProductFromServer(sbth, query, pageIndex);
//        _mutex.threadWait();

        return _result;
    }

    private void getProductFromServer(String sbth, String query, int pageIndex) {
//        String referer = "";
        Map<String, String> headers = new HashMap<>();
        headers.put("accept", "application/json, text/plain, */*");
        headers.put("accept-encoding", "gzip, deflate, br");
        headers.put("referer", "https://msearch.shopping.naver.com/search/all?query=%EA%B0%95%EC%95%84%EC%A7%80%20%EB%85%B8%EC%A6%88%EC%9B%8C%ED%81%AC&prevQuery=%EB%85%B8%EC%A6%88%EC%9B%8C%ED%81%AC&vertical=search");
        headers.put("logic", "PART");
        headers.put("sbth", sbth);
        headers.put("cookie", "NNB=JJCPIG2XJWKGK; SHP_BUCKET_ID=4; ncpa=5585524|lt031kmo|57850f7c104a25c33a512a13d2ed1ad693b5e70a|s_213cbdcdc7145|9226b5ac54255f8af3ede7f1a578ee8f3e6bfc6e:1063988|lt075f5s|3e7edb0e16f8f9febfbeaab62cae7a3e9c6c7c4e|s_2c4f553a85173|658bbf987d8e802164b3b64810c70c830141072f:4027379|lt079eog|0d03e17f421f0583917dd2b0fbc3a5989e860f7d|s_9cea49fa27ec|58072db9166a0566406d2e715be9a2b890298889:299687|lurwycg8|a83cef99915a0418ea594e236c5996bc59584b83|s_90763894633079556|e283915366671cf3916cb13b385a0a4402edfb2d");
        headers.put("user-agent", "PostmanRuntime/7.37.3");

        NaverShoppingApi.getInstance().getSearchAll(headers, query, pageIndex, new CommonClient.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                _searchData = (ShoppingSearchData) data;
                _result = 1;
                _mutex.threadWakeUp();
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response == 200) {
                    _mutex.threadWakeUp();
                } else {
                    if (_retryCount < MAX_RETRY_COUNT) {
                        Log.d(TAG, "응답 실패로 10초후 다시 시도..." + _retryCount);
                        ++_retryCount;
                        SystemClock.sleep(10000);
                        getProductFromServer(sbth, query, pageIndex);
                    } else {
                        Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
                        _result = -1;
                        _mutex.threadWakeUp();
                    }
                }
            }
        });
    }

    protected void getProductFromServer2(String sbth, String query, int pageIndex) {
        Log.d(TAG, "# 쇼핑 상품 가져오기 Page" + pageIndex);

        if (_httpEngine == null) {
            Log.d(TAG, "http engine is null.");
            _result = -1;
        }

        _httpEngine.setAddedHeader("logic", "PART");

        // 더이상 사용되지 않음. 250218
//        _httpEngine.setAddedHeader("sbth", sbth);

//        _httpEngine.setUseDetailChUa(true);
        String htmlString = _httpEngine.requestNaverShoppingProductApi(query, pageIndex);
        _httpEngine.clearAddedHeader();
        int response = _httpEngine.getResponseCode();

        if (response == 200) {
            if (!TextUtils.isEmpty(htmlString)) {
//                Log.d(TAG, htmlString);
                try {
                    // Covert json body to object.
                    Gson gson = new Gson();
                    _searchData = gson.fromJson(htmlString, ShoppingSearchData.class);
                    _result = 1;
                    // currentUrl 을 작업 url 로 설정.
                    _httpEngine.setCurrentUrl(_httpEngine.getUrl());
                } catch (Exception e) {
                    // Support text body.
                    //e.printStackTrace();
                    Log.d(TAG, e.getMessage());
                    _result = -1;
                }
            }
        } else {
            _result = -1;

//            if (_retryCount < MAX_RETRY_COUNT) {
//                Log.d(TAG, "응답 실패로 10초후 다시 시도..." + _retryCount);
//                ++_retryCount;
//                SystemClock.sleep(10000);
//                getProductFromServer2(sbth, query, pageIndex);
//            } else {
//                Log.d(TAG, "응답 실패로 처리 종료..." + _retryCount);
//                _result = -1;
//            }
        }
    }


    private boolean validateData() {
        if (_searchData == null) {
            return false;
        }

        if (_searchData.shoppingResult == null) {
            return false;
        }

        return true;
    }

    public int getProductRank(String mid) {
        if (!validateData()) {
            return -1;
        }

        ArrayList<ShoppingSearchData.Product> products = _searchData.shoppingResult.products;

        if (products == null) {
            if (_searchData.multiModalSasResult != null) {
                products = _searchData.multiModalSasResult.products;
            }
        }

        if (products != null) {
            for (ShoppingSearchData.Product product : products) {
                if (product.id.equals(mid)) {
                    return Integer.parseInt(product.rank);
                }
            }
        }

        return -1;
    }

    public int getProductRankForPid(String pid) {
        if (!validateData()) {
            return -1;
        }

        ArrayList<ShoppingSearchData.Product> products = _searchData.shoppingResult.products;

        if (products == null) {
            if (_searchData.multiModalSasResult != null) {
                products = _searchData.multiModalSasResult.products;
            }
        }

        if (products != null) {
            for (ShoppingSearchData.Product product : products) {
                if (product.mallProductId.equals(pid)) {
                    return Integer.parseInt(product.rank);
                }
            }
        }

        return -1;
    }

    public int getProductCount() {
        if (!validateData()) {
            return -1;
        }

        return _searchData.shoppingResult.productCount;
    }

    public ShoppingSearchData.Product getProduct(String mid) {
        if (!validateData()) {
            return null;
        }

        for (ShoppingSearchData.Product product : _searchData.shoppingResult.products) {
            if (product.id.equals(mid)) {
                return product;
            }
        }

        return null;
    }

    public ShoppingSearchData.Product getProductForPid(String pid) {
        if (!validateData()) {
            return null;
        }

        for (ShoppingSearchData.Product product : _searchData.shoppingResult.products) {
            if (product.mallProductId.equals(pid)) {
                return product;
            }
        }

        return null;
    }
}
