package com.sec.android.app.sbrowser.pattern.naver.shop.api;

public class ProductPageApi {

    private static final String TAG = ProductPageApi.class.getSimpleName();

//    public boolean runFetchStoreProducts(String urlString, String htmlString) {
//        if (_useProductLog) {
//            Log.d(TAG, "current url: " + urlString);
//            String genUrl = getFetchStoreProductsUrl(urlString, htmlString);
//            Log.d(TAG, "### getFetchStoreProductsUrl: " + genUrl);
//
//            if (!TextUtils.isEmpty(genUrl)) {
//                String jsonBody = getProductLogJsonString(htmlString);
//                Log.d(TAG, "productLog jsonBody: " + jsonBody);
//                _httpEngine.setOrigin("https://m.smartstore.naver.com");
//                _httpEngine.setReferer(_httpEngine.getCurrentUrl());
//                String htmlString3 = _httpEngine.requestNaverMobileContentFromProductLog(genUrl, jsonBody);
//                _httpEngine.setOrigin(null);
//                Log.d(TAG, "productLog result: " + htmlString3);
//
//                if (TextUtils.isEmpty(htmlString3)) {
//                    return false;
//                }
//            }
//        }
//
//        return true;
//    }
//
//    public String getFetchStoreProductsUrl(String urlString, String channelNo) {
//        if (!urlString.contains("naver.com") || urlString.contains("naver.com/play/") || urlString.contains("naver.com/beauty/") || urlString.contains("naver.com/window-products/") || urlString.contains("naver.com/fresh/")) {
//            return null;
//        }
//
//        if (TextUtils.isEmpty(channelNo)) {
//            return null;
//        }
//
//        return "https://m.smartstore.naver.com/i/v1/stores/" + channelNo + "/products?page=1&count=6";
//    }
}
