package com.sec.android.app.sbrowser.library.proxy.luna_proxy;

import com.sec.android.app.sbrowser.library.proxy.luna_proxy.client.LunaProxyClient;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public class LunaProxyApi extends LunaProxyClient {

    private static final String TAG = LunaProxyApi.class.getSimpleName();

    private static class LazyHolder {
        public static final LunaProxyApi INSTANCE = new LunaProxyApi();
    }

    public static LunaProxyApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    private LunaProxyApi() {
    }
}
