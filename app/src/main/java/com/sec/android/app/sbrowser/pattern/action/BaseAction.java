package com.sec.android.app.sbrowser.pattern.action;

import com.sec.android.app.sbrowser.engine.ThreadMutex;

public class BaseAction {

    public static final int MAX_RETRY_COUNT = 7;

    protected final ThreadMutex _mutex = new ThreadMutex();

    protected int _retryCount = 0;
    protected int _result = 0;

    public BaseAction() {
//        _mutex.timeout = 30000;
    }
}
