package com.sec.android.app.sbrowser.system;

import android.os.Build;

public class DeviceController {

    static public boolean isS4() {
        if (Build.MODEL.contains("E300") || Build.MODEL.contains("E330")) {
            return true;
        }

        return false;
    }

    static public boolean isS7Under() {
        // s4
        if (Build.MODEL.contains("E300") || Build.MODEL.contains("E330")) {
            return true;
        }

        // s5
        if (Build.MODEL.contains("G90")) {
            return true;
        }

        // s6
        if (Build.MODEL.contains("G92")) {
            return true;
        }

        return false;
    }

    static public boolean isS7() {
        if (Build.MODEL.contains("G930")) {
            return true;
        }

        return false;
    }
}
