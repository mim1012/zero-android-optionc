package com.sec.android.app.sbrowser.system;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import com.sec.android.app.sbrowser.engine.SuCommander;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemController {

    static public void reboot() {
        try {
            SuCommander.execute("reboot");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static public void clearPackageData(String packageName) {
        try {
            SuCommander.execute("pm clear " + packageName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        try {
//            Process process = Runtime.getRuntime().exec("pm clear " + packageName);
//            process.waitFor();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    static public void dataNetwork(boolean on) {
        String enable = on ? "enable" : "disable";
        try {
            SuCommander.execute("svc data " + enable);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Airplane Mode
    @SuppressLint("NewApi")
    static public void setAirplaneMode(Context context, boolean enabled) {
        Context applicationContext = context.getApplicationContext();

        // toggle airplane mode
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Settings.System.putInt(
                    applicationContext.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, !enabled ? 0 : 1);
        } else {
            Settings.Global.putInt(
                    applicationContext.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, !enabled ? 0 : 1);
        }

        // broadcast an intent to inform
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enabled);
        applicationContext.sendBroadcast(intent);
    }

    @SuppressLint("NewApi")
    static public boolean isAirplaneMode(Context context) {
        Context applicationContext = context.getApplicationContext();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(applicationContext.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(applicationContext.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    public void thermal() {
        String temp, type;
        for (int i = 0; i < 29; i++) {
            temp = thermalTemp(i);
            if (!temp.contains("0.0")) {
                type = thermalType(i);
                if (type != null) {
                    System.out.println(i + " ThermalValues " + type + " : " + temp + "\n");
                }
            } else {
                System.out.println(i + " ThermalValues : " + temp + "\n");
            }
        }
    }

    public String thermalTemp(int i) {
        Process process;
        BufferedReader reader;
        String line;
        String t = null;
        float temp = 0;
        try {
            process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone" + i + "/temp");
            process.waitFor();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            line = reader.readLine();
            if (line != null) {
                temp = Float.parseFloat(line);
            }
            reader.close();
            process.destroy();
            if (!((int) temp == 0)) {
                if ((int) temp > 10000) {
                    temp = temp / 1000;
                } else if ((int) temp > 1000) {
                    temp = temp / 100;
                } else if ((int) temp > 100) {
                    temp = temp / 10;
                }

                t = temp + "";
            } else
                t = "0.0";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public String thermalType(int i) {
        Process process;
        BufferedReader reader;
        String line, type = null;
        try {
            process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone" + i + "/type");
            process.waitFor();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            line = reader.readLine();
            if (line != null) {
                type = line;
            }
            reader.close();
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return type;
    }
}
