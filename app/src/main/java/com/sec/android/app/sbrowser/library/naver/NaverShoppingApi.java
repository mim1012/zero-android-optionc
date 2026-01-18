package com.sec.android.app.sbrowser.library.naver;

import com.sec.android.app.sbrowser.library.naver.client.NaverShoppingClient;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public class NaverShoppingApi extends NaverShoppingClient {

    private static final String TAG = NaverShoppingApi.class.getSimpleName();

    private static class LazyHolder {
        public static final NaverShoppingApi INSTANCE = new NaverShoppingApi();
    }

    public static NaverShoppingApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    private NaverShoppingApi() {
    }
}
