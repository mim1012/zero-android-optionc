package com.sec.android.app.sbrowser.library.updater;

import com.sec.android.app.sbrowser.library.updater.client.UpdaterClient;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public class UpdaterApi extends UpdaterClient {

    private static final String TAG = UpdaterApi.class.getSimpleName();

    private static class LazyHolder {
        public static final UpdaterApi INSTANCE = new UpdaterApi();
    }

    public static UpdaterApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    private UpdaterApi() {
    }
}
