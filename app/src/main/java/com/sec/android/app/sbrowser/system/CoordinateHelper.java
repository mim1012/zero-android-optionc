package com.sec.android.app.sbrowser.system;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class CoordinateHelper {

    private static final String TAG = CoordinateHelper.class.getSimpleName();

//    private float _viewWindowHeight;
//
//    public float convertScreenToWebWindow(View view, float value) {
//        Rect rect = new Rect();
//
//        // For coordinates location relative to the parent
//        view.getLocalVisibleRect(rect);
//
//        return value * (_viewWindowHeight / rect.bottom);
//    }
//
//
//    public float viewToScreenHeight2(View view, float value) {
//        Rect rect = new Rect();
//        // For coordinates location relative to the screen/display
//        view.getGlobalVisibleRect(rect);
//
//        return value * (rect.bottom / _viewWindowHeight);
//    }

    public static float viewToScreenX(View view, float viewWidth, float viewX) {
        Rect rect = new Rect();

        // For coordinates location relative to the parent
        view.getGlobalVisibleRect(rect);

        Log.d(TAG, "viewToScreenX: rect: " + rect.toShortString() + ", viewWidth: " + viewWidth + ", viewX: " + viewX);

        // 뷰좌표를 실제 화면좌표로 얻는다.
        return rect.left + ((rect.right - rect.left) / viewWidth) * viewX;
    }

    /**
     * @param view 실제 변환할 대상 뷰.
     * @param viewHeight 현재 뷰화면의 크기(dp).
     * @param viewY 현재 뷰화면의 Y(dp).
     * @return 화면상의 실제 px 좌표.
     */
    public static float viewToScreenY(View view, float viewHeight, float viewY) {
        Rect rect = new Rect();

        // For coordinates location relative to the parent
        view.getGlobalVisibleRect(rect);

        Log.d(TAG, "viewToScreenY: rect: " + rect.toShortString() + ", viewHeight: " + viewHeight + ", viewY: " + viewY);

        // 뷰좌표를 실제 화면좌표로 얻는다.
        return rect.top + (rect.height() / viewHeight) * viewY;
    }
}
