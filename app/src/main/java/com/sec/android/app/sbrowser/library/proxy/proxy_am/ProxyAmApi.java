package com.sec.android.app.sbrowser.library.proxy.proxy_am;

import com.sec.android.app.sbrowser.library.proxy.proxy_am.client.ProxyAmClient;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public class ProxyAmApi extends ProxyAmClient {

    private static final String TAG = ProxyAmApi.class.getSimpleName();

    private static class LazyHolder {
        public static final ProxyAmApi INSTANCE = new ProxyAmApi();
    }

    public static ProxyAmApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    private ProxyAmApi() {
    }
}
