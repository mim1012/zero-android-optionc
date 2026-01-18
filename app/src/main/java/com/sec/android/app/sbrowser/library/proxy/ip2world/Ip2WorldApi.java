package com.sec.android.app.sbrowser.library.proxy.ip2world;

import com.sec.android.app.sbrowser.library.proxy.ip2world.client.Ip2WorldClient;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public class Ip2WorldApi extends Ip2WorldClient {

    private static final String TAG = Ip2WorldApi.class.getSimpleName();

    private static class LazyHolder {
        public static final Ip2WorldApi INSTANCE = new Ip2WorldApi();
    }

    public static Ip2WorldApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    private Ip2WorldApi() {
    }
}
