package com.sec.android.app.sbrowser.pattern;

import android.os.Handler;
import android.os.HandlerThread;

public class PatternThread extends HandlerThread {

//    protected static final int WAIT = 0;
    public static final int START_PATTERN = 201;
    public static final int END_PATTERN = 202;
    public static final int PAUSE_PATTERN = 203;

    protected static final int INPUT_KEYWORD = 205;
    protected static final int REGISTER_FINISH = 208;
    protected static final int REGISTER_RANK = 209;

    protected static final int TOUCH_LOGO = 210;
    protected static final int TOUCH_SEARCH_BAR = 211;
    protected static final int TOUCH_SEARCH_BUTTON = 212;
    protected static final int TOUCH_RANDOM_CONTENT = 213;

    protected static final int STAY_RANDOM = 220;

    protected static final int WEB_BACK = 230;


    private Handler _handler = null;

 //   protected NaverFinishPatternAction _finishPatternAction = null;


    // 임시 처리..
    public String loginId = null;
    public String imei = null;


    public void setHandler(Handler handler) {
        _handler = handler;
    }

    public PatternThread(String name) {
        super(name);
    }

    protected void sendEndPatternMessage() {
        if (_handler != null) {
            _handler.sendEmptyMessage(END_PATTERN);
        }
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();

      //  _finishPatternAction = new NaverFinishPatternAction();
    }
}
