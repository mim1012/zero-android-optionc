package com.sec.android.app.sbrowser.keyboard;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.SuCommander;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class MonkeyScript {

    private static final String TAG = MonkeyScript.class.getSimpleName();

    private static int ACTION_DOWN = 0;
    private static int ACTION_UP = 1;
    private static int ACTION_MOVE = 2;

    private Context _context;

    public MonkeyScript(Context context) {
        _context = context;
    }

    public int getParsedX(int x) {
        DisplayMetrics metrics = _context.getResources().getDisplayMetrics();
        int baseWidth = 1080;
        float widthRatio = metrics.widthPixels * 1.0f / baseWidth;
        return Math.round(x * widthRatio);
    }

    public int getParsedY(int y) {
        DisplayMetrics metrics = _context.getResources().getDisplayMetrics();
        int baseHeight = 1920;
        float heightRatio = metrics.heightPixels * 1.0f / baseHeight;
        return Math.round(y * heightRatio);
    }

    public void runInputStringScript(String string) {
        saveInputStringScript(string);
        run();
    }

    public void runTouch(int x, int y) {
        saveTouchScript(x, y);
        run();
    }

    public void runTouchParsed(int x, int y) {
        saveTouchScriptParsed(x, y);
        run();
    }

    public void runTouchLong(int x, int y) {
        runSwipe(x, y, x, y, 1000);
    }

    public void runTouchLong(int x, int y, long duration) {
        runSwipe(x, y, x, y, duration);
    }

    public void runTouchLongParsed(int x, int y) {
        runSwipeParsed(x, y, x, y, 1000);
    }

    public void runTouchLongParsed(int x, int y, long duration) {
        runSwipeParsed(x, y, x, y, duration);
    }

    public void runSwipe(int x1, int y1, int x2, int y2, long duration) {
        saveSwipeScript(x1, y1, x2, y2, duration);
        run();
    }

    public void runSwipeParsed(int x1, int y1, int x2, int y2, long duration) {
        saveSwipeScriptParsed(x1, y1, x2, y2, duration);
        run();
    }

    public void saveInputStringScript(String string) {
        String text = header();
        text += String.format(Locale.getDefault(), "DispatchString(%s)", string);

        save(text);
    }

    public void saveTouchScript(int x, int y) {
        String text = header();
        double sizeDown = getSize();
        double sizeUp = getSize();
        text += String.format(Locale.getDefault(), dispatchPointerFormatString(), ACTION_DOWN, x, y, sizeDown);
        text += String.format(Locale.getDefault(), dispatchPointerFormatString(), ACTION_MOVE, x, y, sizeUp);
        text += String.format(Locale.getDefault(), dispatchPointerFormatString(), ACTION_UP, x, y, sizeUp);

        save(text);
    }

    public void saveTouchScriptParsed(int x, int y) {
        x = getParsedX(x);
        y = getParsedY(y);
        saveTouchScript(x, y);
    }

    public void saveSwipeScript(int x1, int y1, int x2, int y2, long duration) {
        if (duration < 0) {
            duration = 300;
        }

        long now = SystemClock.uptimeMillis();
        long startTime = now;
        long endTime = startTime + duration;
        long sleepMs = 13;

        String text = header();
        double sizeDown = MathHelper.randomRange(0.019, 0.027);
        double sizeUp = MathHelper.randomRange(0.019, 0.027);
        double sizeLoop = MathHelper.randomRange(0.022, 0.024);
        String format = dispatchPointerFormatString2();

        text += String.format(Locale.getDefault(), dispatchPointerFormatString(), ACTION_DOWN, x1, y1, sizeDown);

        while (now < endTime) {
            long elapsedTime = now - startTime;
            float alpha = (float) elapsedTime / duration;
            SystemClock.sleep(sleepMs);
            text += sleepString(sleepMs);
            text += String.format(Locale.getDefault(), format, ACTION_MOVE,
                    lerp(x1, x2, alpha), lerp(y1, y2, alpha),
                    sizeLoop + (MathHelper.randomRange(0, 0.006) - 0.003));
            now = SystemClock.uptimeMillis();
        }

        text += sleepString(sleepMs);
        text += String.format(Locale.getDefault(), dispatchPointerFormatString(), ACTION_MOVE, x2, y2, sizeUp);
        text += String.format(Locale.getDefault(), dispatchPointerFormatString(), ACTION_UP, x2, y2, sizeUp);

        save(text);
    }

    public void saveSwipeScriptParsed(int x1, int y1, int x2, int y2, long duration) {
        x1 = getParsedX(x1);
        y1 = getParsedY(y1);
        x2 = getParsedX(x2);
        y2 = getParsedY(y2);
        saveSwipeScript(x1, y1, x2, y2, duration);
    }

    public void run() {
        try {
            String path = getSavedFile().getPath();
            String cmd = "monkey -f " + path + " 1";
            cmd += "\nsettings put system accelerometer_rotation 0";
//            Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
            SuCommander.execute(cmd);
            Log.d(TAG, "monkey: ");
//            getSavedFile().delete();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String read() {
        File file = getSavedFile();
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(file);
            stream.read(bytes);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String(bytes);
    }

    private float lerp(float a, float b, float alpha) {
        return (b - a) * alpha + a;
    }

    private double getSize() {
        return MathHelper.randomRange(0.015, 0.05);
    }

    private String header() {
        String script = "type= user\n" +
                "speed= 500\n" +
                "start data >>\n";
        return script;
    }

    private String dispatchPointerFormatString() {
        return "DispatchPointer(1, 1, %d, %d, %d, 1.0f, %f, 0, 1.0, 1.0, 11, 0)\n";
    }

    private String dispatchPointerFormatString2() {
        return "DispatchPointer(1, 1, %d, %f, %f, 1.0f, %f, 0, 1.0, 1.0, 11, 0)\n";
    }

    private String sleepString(long ms) {
        return String.format(Locale.getDefault(), "UserWait(%d)\n", ms);
    }

    private File getSavedDir() {
        File dir;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && _context != null) {
            dir = _context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        } else {
            dir = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
        }

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.d(TAG, "Directory not created");
            }
        }

        return dir;
    }

    private File getSavedFile() {
        return new File(getSavedDir(), "monkey.txt");
    }

    private void save(String text) {
        FileWriter stream = null;
        try {
            stream = new FileWriter(getSavedFile());
            stream.write(text);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
