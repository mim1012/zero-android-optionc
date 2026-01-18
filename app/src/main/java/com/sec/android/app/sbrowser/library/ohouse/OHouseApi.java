package com.sec.android.app.sbrowser.library.ohouse;

import com.sec.android.app.sbrowser.library.ohouse.client.OHouseClient;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public class OHouseApi extends OHouseClient {

    private static final String TAG = OHouseApi.class.getSimpleName();

    private static class LazyHolder {
        public static final OHouseApi INSTANCE = new OHouseApi();
    }

    public static OHouseApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    private OHouseApi() {
    }
}
