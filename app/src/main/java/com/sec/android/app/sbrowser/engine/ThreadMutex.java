package com.sec.android.app.sbrowser.engine;


public class ThreadMutex {

    private final Object _mutex = new Object();
    private boolean _waiting = false;

    public long timeout = 90000;

    public void threadWait() {
        synchronized (_mutex) {
            try {
                if (!_waiting) {
                    _waiting = true;
                    _mutex.wait(timeout);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void threadWaitInfinite() {
        synchronized (_mutex) {
            try {
                if (!_waiting) {
                    _waiting = true;
                    _mutex.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void threadWakeUp() {
        synchronized (_mutex) {
            _waiting = false;
            _mutex.notify();
        }
    }
}
