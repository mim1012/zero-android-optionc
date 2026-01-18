package com.sec.android.app.sbrowser.pattern.ohouse;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.pattern.PatternMessage;

public class OHousePatternMessage extends PatternMessage {

    private static final String TAG = OHousePatternMessage.class.getSimpleName();

    public static final String HOME_URL = "https://ohou.se";

    protected static final int GO_HOME = 1011;
    protected static final int WEB_BACK = 1012;

    protected static final int INPUT_KEYWORD = 1021;
    protected static final int REGISTER_FINISH = 1022;
    protected static final int REGISTER_RANK = 1023;

    protected static final int TOUCH_LOGO = 1031;
    protected static final int TOUCH_SEARCH_BAR = 1032;
    protected static final int TOUCH_SEARCH_BUTTON = 1033;
    protected static final int TOUCH_RANDOM_CONTENT = 1034;
    protected static final int TOUCH_MOBILE_WEB_POPUP = 1035;

    protected static final int STAY_RANDOM = 1041;

//    protected final NaverSearchBarCheckPatternAction _searchBarCheckPatternAction;

    protected KeywordItemMoon _item = null;

    public OHousePatternMessage(WebViewManager manager) {
        super(manager);
//        _searchBarCheckPatternAction = new NaverSearchBarCheckPatternAction(manager.getWebView());

    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
//            case GO_HOME: {
//                Log.d(TAG, "# 네이버 홈으로 이동");
//                webViewLoad(msg, Config.HOME_URL);
//                break;
//            }

            case END_PATTERN: {
                Log.d(TAG, "# OHousePatternMessage 패턴 종료");
//                _searchBarCheckPatternAction.endPattern();
                break;
            }
        }
    }
}
