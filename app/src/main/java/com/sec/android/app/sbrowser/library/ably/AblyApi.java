package com.sec.android.app.sbrowser.library.ably;

import com.sec.android.app.sbrowser.library.ohouse.client.OHouseClient;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public class AblyApi extends OHouseClient {

    private static final String TAG = AblyApi.class.getSimpleName();

    private static class LazyHolder {
        public static final AblyApi INSTANCE = new AblyApi();
    }

    public static AblyApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    private AblyApi() {
    }
}
