package com.sec.android.app.sbrowser.library.proxy.py_proxy;

import com.sec.android.app.sbrowser.library.proxy.py_proxy.client.PyProxyClient;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public class PyProxyApi extends PyProxyClient {

    private static final String TAG = PyProxyApi.class.getSimpleName();

    private static class LazyHolder {
        public static final PyProxyApi INSTANCE = new PyProxyApi();
    }

    public static PyProxyApi getInstance() {
        return LazyHolder.INSTANCE;
    }

    private PyProxyApi() {
    }
}
