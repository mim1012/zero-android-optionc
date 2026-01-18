package com.loveplusplus.update;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

public class TouchInjector {

    private static final String TAG = TouchInjector.class.getSimpleName();

    private Thread _thread = null;

    public void touchScreen(int x, int y) {
        final String xy = String.format("%s %s", x, y);

//        threadJoin();

        _thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String cmd = "input tap " + xy + "\n";
//                    String cmd = "/system/bin/input tap " + xy + "\n";
                    executeCommand(cmd);

                    Log.d(TAG, "터치: " + xy);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        _thread.start();
    }


    /**
     * Runs given command in shell as superuser
     * @param command Command to execute
     * @return If execution of shell-command was successful or not
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean executeCommand(String command) throws IOException, InterruptedException
    {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

//        outputStream.writeBytes(command + "\n");
//        outputStream.flush();
//        outputStream.writeBytes("exit\n");
//        outputStream.flush();

        outputStream.writeBytes(command);
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        outputStream.close();

        return process.waitFor() == 0;
    }
}
