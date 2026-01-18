package com.sec.android.app.sbrowser.library.proxy.awm_proxy;

import com.sec.android.app.sbrowser.library.proxy.awm_proxy.client.AwmProxyClient;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public class AwmProxyApi extends AwmProxyClient {

    private static final String TAG = AwmProxyApi.class.getSimpleName();

    private static class LazyHolder {
        public static final AwmProxyApi INSTANCE = new AwmProxyApi();
    }

    public static AwmProxyApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    private AwmProxyApi() {
    }
}
