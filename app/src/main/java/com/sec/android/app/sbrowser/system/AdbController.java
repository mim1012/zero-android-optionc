package com.sec.android.app.sbrowser.system;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.SuCommander;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class AdbController {

    private static final String TAG = AdbController.class.getSimpleName();

    public static void launchAppAction(String schema, String packageName) {
        Log.d(TAG, "launchAppAction " + packageName);
        String cmd = "am start -W -a android.intent.action.VIEW -d \"" + schema + "\" " + packageName;

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void killApp(String packageName) {
        Log.d(TAG, "kill " + packageName);
        String cmd = "am force-stop " + packageName;
//        String cmd = "/system/bin/am force-stop " + packageName;

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        ActivityManager activityManager = (ActivityManager) getApplicationContext()
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        activityManager.killBackgroundProcesses(PACKAGE_NAME_SBROWSER);
    }

    public static void killAppThread(final String packageName) {
        threadStartJoin(new Thread(new Runnable() {
            @Override
            public void run() {
                killApp(packageName);
            }
        }));
    }

//    private void checkAppRunning(String packageName) {
//        Log.d(TAG, "kill " + packageName);
//        String cmd = "am force-stop " + packageName;
////        String cmd = "/system/bin/am force-stop " + packageName;
//
//        try {
//            SuCommander.execute(cmd);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        "am start -n \"com.sec.android.app.sbrowser/com.sec.android.app.sbrowser.ActivityMCloud\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER"
////        if (!checkProcessRunning(packageName)) {
////            Log.d(TAG, "앱이 실행중이 아니어서 강제 실행.");
//
//            // 앱 자동실행. 실행중이지 않으면 실행해준다.
////            PackageManager packageManager = mContext.getPackageManager();
////            Intent intent = packageManager.getLaunchIntentForPackage(packageName);
////            if (intent != null) {
////                mContext.startActivity(intent);
////            }
////        } else {
////            Log.d(TAG, "앱이 실행중..");
////        }
//    }

    public static void startPackage(Context context, String packageName) {
        Log.d(TAG, "start " + packageName);
//        if (!checkProcessRunning(packageName)) {
//            Log.d(TAG, "앱이 실행중이 아니어서 강제 실행.");

        // 앱 자동실행. 실행중이지 않으면 실행해준다.
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            context.startActivity(intent);
        }
//        } else {
//            Log.d(TAG, "앱이 실행중..");
//        }
    }

    public static void reboot() {
        try {
            SuCommander.execute("reboot");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void clearPackageData(String packageName) {
        try {
            SuCommander.execute("pm clear " + packageName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void getAndroidId() {
        try {
            SuCommander.execute("settings get secure android_id");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void setAndroidId(String androidId) {
        try {
            SuCommander.execute("settings put secure android_id " + androidId);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void setDataNetwork(boolean on) {
        try {
            SuCommander.execute("svc data " + (on ? "enable" : "disable"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void setDataNetworkThread(final boolean on) {
        threadStartJoin(new Thread(new Runnable() {
            @Override
            public void run() {
                setDataNetwork(on);
            }
        }));
    }

    public static void setGps(boolean on) {
        String cmd = "settings put secure location_providers_allowed +gps,network";

        if (!on) {
            cmd += "\n";
            cmd += "settings put secure location_providers_allowed -gps,network";
        }

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAirplaneMode() {
        boolean isOn = false;
        try {
            String command = "settings get global airplane_mode_on";
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

            outputStream.writeBytes(command + "\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            outputStream.close();

            InputStreamReader streamReader = new InputStreamReader(process.getInputStream());
            BufferedReader in = new BufferedReader(streamReader);
            String line;

            while ((line = in.readLine()) != null) {
                if (line.length() > 1) {
                    continue;
                }

                int result = Integer.valueOf(line);
                isOn = (result == 1);
                break;
            }

            in.close();
            streamReader.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOn;
    }

    public static void setAirplaneMode(boolean on) {
        String cmd = "settings put global airplane_mode_on " + (on ? "1" : "0");
        cmd += "\n";
        cmd += "am broadcast -a android.intent.action.AIRPLANE_MODE";

        if (on) {
            cmd += " --ez state true";
        } else {
            cmd += " --ez state false";
        }

        try {
            SuCommander.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isPackageVerifier() {
        boolean isOn = false;
        try {
            String command = "settings get global package_verifier_enable";
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

            outputStream.writeBytes(command + "\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            outputStream.close();

            InputStreamReader streamReader = new InputStreamReader(process.getInputStream());
            BufferedReader in = new BufferedReader(streamReader);
            String line;

            while ((line = in.readLine()) != null) {
                if (line.length() > 1) {
                    continue;
                }

                int result = Integer.valueOf(line);
                isOn = (result == 1);
                break;
            }

            in.close();
            streamReader.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOn;
    }

    public static void setPackageVerifier(boolean enabled) {
        try {
            SuCommander.execute("settings put global package_verifier_enable " + (enabled ? "1" : "0"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void threadStartJoin(Thread thread) {
        if (thread != null) {
            thread.start();

            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
