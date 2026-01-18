package com.sec.android.app.sbrowser.pattern.action;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;

public class SwipeThreadAction {

    private TouchInjector _touchInjector;

    public int stayFastMin = 80;
    public int stayFastMax = 150;

    public SwipeThreadAction(TouchInjector injector) {
      //  Assert.assertNotNull("TouchInjector can't be null", injector);

        _touchInjector = injector;
    }

    // 외부 쓰레드 내부에서 되어야 한다.
    public void swipeDown() {
        swipe(true, ((int)MathHelper.randomRange(0, 1) == 1));
    }

    public void swipeDown(boolean longSwipe) {
        swipe(true, longSwipe);
    }

    public void swipeUp() {
        swipe(false, ((int)MathHelper.randomRange(0, 1) == 1));
    }

    public void swipeUp(boolean longSwipe) {
        swipe(false, longSwipe);
    }


    public void swipeDownAi() {
        swipeDown(110, 160);
    }

    public void swipeUpAi() {
        swipeUp(110, 160);
    }

    public void swipeDown(long stayMillisMin, long stayMillisMax) {
        _touchInjector.swipeScreen(true, MathHelper.randomRange(stayMillisMin, stayMillisMax));
    }

    public void swipeDownFast(long stayMillisMin, long stayMillisMax) {
        _touchInjector.swipeScreenFast(true, MathHelper.randomRange(stayMillisMin, stayMillisMax));
    }

    public void swipeUp(long stayMillisMin, long stayMillisMax) {
        _touchInjector.swipeScreen(false, MathHelper.randomRange(stayMillisMin, stayMillisMax));
    }

    public void swipeUpFast(long stayMillisMin, long stayMillisMax) {
        _touchInjector.swipeScreenFast(false, MathHelper.randomRange(stayMillisMin, stayMillisMax));
    }

    public void swipe(boolean down, boolean longSwipe) {
        _touchInjector.swipeScreen(down, longSwipe ? MathHelper.randomRange(1200, 1700) : MathHelper.randomRange(stayFastMin, stayFastMax));
    }
}
