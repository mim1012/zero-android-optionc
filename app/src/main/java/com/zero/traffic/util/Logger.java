package com.zero.traffic.util;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 통합 로거 (Logcat + UI 콜백)
 */
public class Logger {
    private static final String TAG = "ZeroTraffic";
    private static final ThreadLocal<SimpleDateFormat> TIME_FMT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("HH:mm:ss", Locale.getDefault()));

    /** UI 로그 콜백 — MainActivity에서 등록/해제 */
    public interface UiLogListener {
        void onLog(String line);
    }

    private static volatile UiLogListener uiListener;

    public static void setUiListener(UiLogListener l) {
        uiListener = l;
    }

    public static void i(String msg) {
        String line = format("I", msg);
        Log.i(TAG, line);
        dispatch(line);
    }

    public static void w(String msg) {
        String line = format("W", msg);
        Log.w(TAG, line);
        dispatch(line);
    }

    public static void e(String msg) {
        String line = format("E", msg);
        Log.e(TAG, line);
        dispatch(line);
    }

    public static void e(String msg, Throwable t) {
        String line = format("E", msg);
        Log.e(TAG, line, t);
        dispatch(line);
    }

    public static void step(String stepId, String action) {
        String line = format("D", String.format("[STEP] %s → %s", stepId, action));
        Log.d(TAG, line);
        dispatch(line);
    }

    public static void step(String stepId, String action, String detail) {
        String line = format("D", String.format("[STEP] %s → %s: %s", stepId, action, detail));
        Log.d(TAG, line);
        dispatch(line);
    }

    private static void dispatch(String line) {
        UiLogListener l = uiListener;
        if (l != null) {
            try { l.onLog(line); } catch (Exception ignored) {}
        }
    }

    private static String format(String level, String msg) {
        return "[" + TIME_FMT.get().format(new Date()) + "][" + level + "] " + msg;
    }
}
