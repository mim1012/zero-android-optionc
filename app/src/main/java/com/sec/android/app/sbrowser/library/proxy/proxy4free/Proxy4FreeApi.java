package com.sec.android.app.sbrowser.library.proxy.proxy4free;

import com.sec.android.app.sbrowser.library.proxy.proxy4free.client.Proxy4FreeClient;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public class Proxy4FreeApi extends Proxy4FreeClient {

    private static final String TAG = Proxy4FreeApi.class.getSimpleName();

    private static class LazyHolder {
        public static final Proxy4FreeApi INSTANCE = new Proxy4FreeApi();
    }

    public static Proxy4FreeApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    private Proxy4FreeApi() {
    }
}
