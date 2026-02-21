package com.zero.traffic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.zero.traffic.service.TrafficService;
import com.zero.traffic.util.Logger;

/**
 * 부팅 시 자동 시작
 * 서버 URL이 저장되어 있으면 자동으로 TrafficService 시작
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Logger.i("부팅 감지 → 자동 시작 체크");
            autoStartService(context);

        } else if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            // ADB 재설치 후 자동 실행
            Logger.i("패키지 업데이트 감지 → 자동 실행");
            launchMainActivity(context);
        }
    }

    /** 저장된 URL이 있으면 TrafficService 직접 시작 (부팅용) */
    private void autoStartService(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("ZeroSettings", Context.MODE_PRIVATE);
        String serverUrl = prefs.getString("server_url", "");

        if (!serverUrl.isEmpty()) {
            Logger.i("서버 URL 있음 → TrafficService 자동 시작");
            Intent serviceIntent = new Intent(context, TrafficService.class);
            serviceIntent.putExtra("server_url", serverUrl);
            context.startForegroundService(serviceIntent);
        }
    }

    /** MainActivity 실행 (설치 후 — 오버레이 권한 체크 포함) */
    private void launchMainActivity(Context context) {
        Intent launch = new Intent(context, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        launch.putExtra("auto_start", true);
        context.startActivity(launch);
    }
}
