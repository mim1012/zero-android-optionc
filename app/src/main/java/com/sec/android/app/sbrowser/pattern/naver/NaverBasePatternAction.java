package com.sec.android.app.sbrowser.pattern.naver;

import android.graphics.Rect;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.keyboard.MonkeyScript;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;

public class NaverBasePatternAction {

    private static final String TAG = NaverBasePatternAction.class.getSimpleName();

    private final MonkeyScript _monkeyScript;
    protected final TouchInjector _touchInjector;

    public NaverBasePatternAction(WebView webView) {
        _monkeyScript = new MonkeyScript(webView.getContext());

        // 키보드 셋팅.
        _touchInjector = new TouchInjector(webView.getContext());
        _touchInjector.setSoftKeyboard(new SamsungKeyboard());
    }

    public void touchScreen(int x, int y) {
        _monkeyScript.runTouch(x, y);
//        _touchInjector.touchScreen(x, y);
    }

    public void touchScreen(Rect rc) {
        touchScreen(rc, 10);
    }

    public void touchScreen(Rect rc, int inset) {
        int x = (int) MathHelper.randomRange(rc.left + inset, rc.right - inset);
        int y = (int) MathHelper.randomRange(rc.top + inset, rc.bottom - inset);

        touchScreen(x, y);
    }
}
