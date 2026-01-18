package com.sec.android.app.sbrowser.pattern;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;

public class RandomSwipePatternAction {

    private static final String TAG = RandomSwipePatternAction.class.getSimpleName();

    private TouchInjector _touchInjector = null;


    public RandomSwipePatternAction(Context context) {
        // 키보드 셋팅.
        _touchInjector = new TouchInjector(context);
        _touchInjector.setSoftKeyboard(new SamsungKeyboard());
    }

    public void randomSwipe() {
        randomSwipe(4, 5);
    }

    public void randomSwipe(int min, int max) {
        SwipeThreadAction swipeThreadAction = new SwipeThreadAction(_touchInjector);
        int maxCount = (int) MathHelper.randomRange(min, max);
        int runCount = 0;

        Log.d(TAG, maxCount + "번의 스크롤 행동이 설정됨");

        while (runCount < maxCount) {
            // 처음 3번은 무조건 아래로 간다.
            if (runCount < 3) {
                Log.d(TAG, "아래로 스크롤");
                swipeThreadAction.swipeDown();
            } else {
                int isUp = (int)MathHelper.randomRange(0, 1);

                if (isUp == 0) {
                    Log.d(TAG, "아래로 스크롤");
                    swipeThreadAction.swipeDown();
                } else {
                    Log.d(TAG, "위로 스크롤");
                    swipeThreadAction.swipeUp();
                }
            }

            SystemClock.sleep(MathHelper.randomRange(1300, 2500));

            ++runCount;
        }
    }
}
