package com.sec.android.app.sbrowser.keyboard;

import android.app.UiAutomation;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.core.view.InputDeviceCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TouchInjectorHelper {


    //=========================================================================
    //==                        Utility Methods                             ===
    //=========================================================================
    /**
     * Helper method injects a click event at a point on the active screen via the UiAutomation object.
     * @param x the x position on the screen to inject the click event
     * @param y the y position on the screen to inject the click event
     * @param automation a UiAutomation object rtreived through the current Instrumentation
     */
    static void injectClickEvent(float x, float y, UiAutomation automation){
        //A MotionEvent is a type of InputEvent.
        //The event time must be the current uptime.
        final long eventTime = SystemClock.uptimeMillis();

        //A typical click event triggered by a user click on the touchscreen creates two MotionEvents,
        //first one with the action KeyEvent.ACTION_DOWN and the 2nd with the action KeyEvent.ACTION_UP
        MotionEvent motionDown = MotionEvent.obtain(eventTime, eventTime, KeyEvent.ACTION_DOWN,
                x,  y, 0);
        //We must set the source of the MotionEvent or the click doesn't work.
        motionDown.setSource(InputDeviceCompat.SOURCE_TOUCHSCREEN);
        automation.injectInputEvent(motionDown, true);
        MotionEvent motionUp = MotionEvent.obtain(eventTime, eventTime, KeyEvent.ACTION_UP,
                x, y, 0);
        motionUp.setSource(InputDeviceCompat.SOURCE_TOUCHSCREEN);
        automation.injectInputEvent(motionUp, true);
        //Recycle our events back to the system pool.
        motionUp.recycle();
        motionDown.recycle();
    }


    Method injectInputEventMethod;
    InputManager im;

    public void eventInput() throws Exception {
        //Get the instance of InputManager class using reflection
        String methodName = "getInstance";
        Object[] objArr = new Object[0];
        im = (InputManager) InputManager.class.getDeclaredMethod(methodName, new Class[0])
                .invoke(null, objArr);

        //Make MotionEvent.obtain() method accessible
        methodName = "obtain";
        MotionEvent.class.getDeclaredMethod(methodName, new Class[0]).setAccessible(true);

        //Get the reference to injectInputEvent method
        methodName = "injectInputEvent";
        injectInputEventMethod = InputManager.class.getMethod(
                methodName, new Class[]{InputEvent.class, Integer.TYPE});
    }

    public void injectMotionEvent(int inputSource, int action, long when, float x, float y,
                                  float pressure) throws InvocationTargetException, IllegalAccessException {
        MotionEvent event = MotionEvent.obtain(when, when + 100, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
        event.setSource(inputSource);
        injectInputEventMethod.invoke(im, new Object[]{event, Integer.valueOf(0)});
    }

    private void injectKeyEvent(KeyEvent event)
            throws InvocationTargetException, IllegalAccessException {
        injectInputEventMethod.invoke(im, new Object[]{event, Integer.valueOf(0)});
    }
}
