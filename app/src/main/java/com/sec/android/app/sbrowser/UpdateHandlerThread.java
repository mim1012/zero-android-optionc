package com.sec.android.app.sbrowser;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.loveplusplus.update.UpdateChecker;
import com.loveplusplus.update.xample.pattern.PatternHandlerThread;

import java.io.DataOutputStream;
import java.io.IOException;

public class UpdateHandlerThread extends PatternHandlerThread implements PatternHandlerThread.OnHandleMessageListener {

    private static final String TAG = UpdateHandlerThread.class.getSimpleName();

    public Context context = null;

    public UpdateHandlerThread() {
        super(TAG);
    }

    public UpdateHandlerThread(String name) {
        super(name);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();

        setOnHandleMessageListener(this);

        // 5초후 시작.
        _handler.sendEmptyMessageDelayed(0, 5000);
    }


    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        _handler = handler;

        switch (msg.what) {
            case 0: {
                Log.d(TAG, "업데이트 검사 실행");
                //_handler.sendEmptyMessageDelayed(REBOOT_CHK,1000);
                UpdateChecker.checkForBackground(context, handler);
                break;
            }
            case 1: {
                // 업데이트가 없다면 다시 검사한다.
                versionCheck();
                break;
            }

            case 2: {
                Log.d(TAG, "업데이트 중이므로 장시간 후에 검사.");
              //  killTarget("com.sec.android.app.sbrowser");

                handler.sendEmptyMessageDelayed(0, 360000);
//                handler.sendEmptyMessageDelayed(0, 120000);
                break;
            }

            case 10: {
                // 실행중인지 검사.
                // 업데이트가 없다면 다시 검사한다.
                versionCheck();
                break;
            }
        }
    }


    public void versionCheck() {
        Log.d(TAG, "검사 대기");
        // 5분에 한번씩 검사.
//        TimeUnit.MINUTES.toMillis(5)
//        _handler.sendEmptyMessageDelayed(0, 5000);
        _handler.sendEmptyMessageDelayed(0, 300000);
//        handler.sendEmptyMessageDelayed(0, 5000);
    }

    private void killTarget(final String packageName) {
        Log.d(TAG, "앱 종료 시도.");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process suProcess = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
                    os.writeBytes("adb shell" + "\n");
                    os.flush();
                    os.writeBytes("am force-stop " + packageName + "\n");
                    os.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }




}
