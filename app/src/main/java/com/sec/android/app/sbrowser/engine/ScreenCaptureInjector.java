package com.sec.android.app.sbrowser.engine;

import android.util.Log;

import java.io.File;
import java.io.IOException;

public class ScreenCaptureInjector {

    private static final String TAG = ScreenCaptureInjector.class.getSimpleName();

    /**
     * @param path extension must has ".png".
     * @return fullPath.
     */
    public static String takeScreenshot(String path) {
        File pathDir = new File(path).getParentFile();

        if (!pathDir.exists()) {
            if (!pathDir.mkdirs()) {
                Log.d(TAG, "Directory not created");
                return null;
            }
        }

        String cmd = "/system/bin/screencap -p " + path;

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return path;
    }
}
