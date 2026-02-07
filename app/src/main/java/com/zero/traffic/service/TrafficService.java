package com.zero.traffic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zero.traffic.captcha.CaptchaProxy;
import com.zero.traffic.engine.ScenarioManager;
import com.zero.traffic.engine.ScenarioRunner;
import com.zero.traffic.engine.ScriptEngine;
import com.zero.traffic.model.Scenario;
import com.zero.traffic.model.StepResult;
import com.zero.traffic.model.TaskInfo;
import com.zero.traffic.network.GroupManager;
import com.zero.traffic.network.NetworkUtils;
import com.zero.traffic.server.ApiClient;
import com.zero.traffic.server.TaskManager;
import com.zero.traffic.util.Logger;
import com.zero.traffic.util.RandomDelay;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 트래픽 포그라운드 서비스 — 메인 루프
 *
 * 1. 초기화 (API 클라이언트, 시나리오, 스크립트)
 * 2. 루프: 작업 받기 → 시나리오 선택 → 실행 → 결과 보고
 * 3. 자동 업데이트 (1시간마다)
 */
public class TrafficService extends Service {
    private static final String CHANNEL_ID = "zero_traffic";
    private static final int NOTIFICATION_ID = 1;
    private static final String EXTRA_SERVER_URL = "server_url";

    private ExecutorService worker;
    private volatile boolean running = false;

    private WebView webView;
    private ApiClient api;
    private TaskManager taskManager;
    private ScenarioManager scenarioManager;
    private ScriptEngine scriptEngine;
    private CaptchaProxy captchaProxy;
    private ScenarioRunner runner;
    private GroupManager groupManager;

    private String deviceId;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        worker = Executors.newSingleThreadExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String serverUrl = intent != null
                ? intent.getStringExtra(EXTRA_SERVER_URL)
                : null;

        if (serverUrl == null || serverUrl.isEmpty()) {
            Logger.e("서버 URL 없음");
            stopSelf();
            return START_NOT_STICKY;
        }

        // 포그라운드 알림
        Notification notification = buildNotification("초기화 중...");
        startForeground(NOTIFICATION_ID, notification);

        // 초기화 + 메인 루프 시작
        running = true;
        worker.execute(() -> {
            try {
                init(serverUrl);
                mainLoop();
            } catch (Exception e) {
                Logger.e("서비스 오류", e);
            }
        });

        return START_STICKY;
    }

    /**
     * 초기화
     */
    private void init(String serverUrl) {
        Logger.i("══════════════════════════════════════");
        Logger.i("  Zero Traffic v2 시작");
        Logger.i("  서버: " + serverUrl);
        Logger.i("  기기: " + deviceId);
        Logger.i("══════════════════════════════════════");

        // API 클라이언트
        api = new ApiClient(serverUrl + "/zero/api/v1");

        // 1. 그룹 등록 (대장봇/쫄병봇 역할 할당)
        groupManager = new GroupManager(this, api, deviceId);
        String currentIp = NetworkUtils.getLocalIpAddress();
        groupManager.register(currentIp);

        if (groupManager.isRegistered()) {
            Logger.i(String.format("그룹: %s | 역할: %s",
                    groupManager.getGroupName(),
                    groupManager.isLeader() ? "대장봇 ★" : "쫄병봇"));

            // 2. 네트워크 설정 (대장봇=핫스팟, 쫄병봇=WiFi 연결)
            groupManager.setupNetwork();
            updateNotification(groupManager.isLeader() ? "대장봇 — 핫스팟 ON" : "쫄병봇 — WiFi 연결");
            RandomDelay.sleepBetween(3000, 5000); // 네트워크 안정화 대기
        } else {
            Logger.w("그룹 미등록 — 독립 모드로 실행");
        }

        // 3. WebView 초기화 (메인 스레드에서)
        initWebView();

        // 4. 매니저 초기화
        taskManager = new TaskManager(api, deviceId);
        scenarioManager = new ScenarioManager(this, api, deviceId);
        scriptEngine = new ScriptEngine(this, api);
        captchaProxy = new CaptchaProxy(api, deviceId);
        runner = new ScenarioRunner(webView, captchaProxy, scriptEngine);

        // 5. 서버 동기화
        scenarioManager.sync();
        scriptEngine.sync();

        Logger.i("초기화 완료. 시나리오: " + scenarioManager.getCount() + "개");
        updateNotification(String.format("%s | %s | %d개 시나리오",
                groupManager.isLeader() ? "★대장" : "쫄병",
                groupManager.getGroupName(),
                scenarioManager.getCount()));
    }

    /**
     * WebView 초기화 (메인 스레드)
     */
    private void initWebView() {
        // 메인 스레드에서 WebView 생성 필요
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

        mainHandler.post(() -> {
            webView = new WebView(TrafficService.this);
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            settings.setUserAgentString(
                "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL +
                ") AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
            );
            webView.setWebViewClient(new WebViewClient());
            latch.countDown();
        });

        try { latch.await(); } catch (InterruptedException ignored) {}
    }

    /**
     * 메인 루프
     */
    private void mainLoop() {
        long lastSync = System.currentTimeMillis();

        while (running) {
            try {
                // 자동 업데이트 (1시간마다)
                if (System.currentTimeMillis() - lastSync > 3600_000) {
                    Logger.i("자동 업데이트 중...");
                    scenarioManager.sync();
                    scriptEngine.sync();
                    lastSync = System.currentTimeMillis();
                }

                // 1. 작업 받기
                TaskInfo task = taskManager.claimWork();
                if (task == null) {
                    Logger.i("작업 없음 — 60초 대기");
                    RandomDelay.sleepBetween(50000, 70000);
                    continue;
                }

                Logger.i("작업 수신: #" + task.getTrafficId() + " " + task.getKeyword());
                updateNotification("실행 중: " + task.getKeyword());

                // 2. 시나리오 선택 (가중치 기반)
                Scenario scenario = scenarioManager.selectScenario();
                if (scenario == null) {
                    Logger.e("시나리오 없음");
                    taskManager.fail(task.getTrafficId(), "No scenario available");
                    RandomDelay.sleepBetween(30000, 60000);
                    continue;
                }

                // 3. 실행
                StepResult result = runner.execute(scenario, task);

                // 4. 결과 보고
                if (result.isSuccess()) {
                    taskManager.complete(task.getTrafficId());
                } else {
                    taskManager.fail(task.getTrafficId(), result.getMessage());
                }

                // 5. 다음 작업 전 대기
                RandomDelay.sleepBetween(30000, 60000);
                updateNotification("대기 중...");

            } catch (Exception e) {
                Logger.e("루프 오류: " + e.getMessage());
                RandomDelay.sleepBetween(30000, 60000);
            }
        }
    }

    // ── 알림 ───────────────────────────────────────────

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Zero Traffic", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    private Notification buildNotification(String text) {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        return builder
                .setContentTitle("Zero Traffic")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String text) {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, buildNotification(text));
        }
    }

    @Override
    public void onDestroy() {
        running = false;
        if (worker != null) worker.shutdownNow();
        if (runner != null) runner.cancel();
        if (groupManager != null) groupManager.cleanup();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
