package com.sec.android.app.sbrowser.engine;

import java.io.DataOutputStream;
import java.io.IOException;

public class SuCommander {
    /**
     * Runs given command in shell as superuser
     * @param command Command to execute
     * @return If execution of shell-command was successful or not
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean execute(String command) throws IOException, InterruptedException
    {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

        outputStream.writeBytes(command + "\n");
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        outputStream.close();

        return process.waitFor() == 0;
    }
}
