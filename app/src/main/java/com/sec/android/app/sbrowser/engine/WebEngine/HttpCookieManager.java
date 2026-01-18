package com.sec.android.app.sbrowser.engine.WebEngine;

public class HttpCookieManager extends HttpCookieController {

    private static final String TAG = HttpCookieManager.class.getSimpleName();

    public static HttpCookieManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        public static final HttpCookieManager INSTANCE = new HttpCookieManager();
    }

    private HttpCookieManager() {
    }
}
