package com.zero.traffic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebResourceRequest;
import android.webkit.WebViewClient;

import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.zero.traffic.captcha.CaptchaProxy;
import com.zero.traffic.engine.ScenarioManager;
import com.zero.traffic.engine.ScenarioRunner;
import com.zero.traffic.engine.ScriptEngine;
import com.zero.traffic.model.Scenario;
import com.zero.traffic.model.StepResult;
import com.zero.traffic.model.TaskInfo;
import com.zero.traffic.network.ChromeManager;
import com.zero.traffic.network.GroupManager;
import com.zero.traffic.network.NetworkUtils;
import com.zero.traffic.server.ApiClient;
import com.zero.traffic.server.TaskManager;
import com.zero.traffic.util.Logger;
import com.zero.traffic.util.RandomDelay;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    private static final String ACTION_TOGGLE_WEBVIEW = "com.zero.traffic.TOGGLE_WEBVIEW";

    private ExecutorService worker;
    private volatile boolean running = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile WebView webView;
    private volatile ApiClient api;
    private volatile TaskManager taskManager;
    private volatile ScenarioManager scenarioManager;
    private volatile ScriptEngine scriptEngine;
    private volatile CaptchaProxy captchaProxy;
    private volatile ScenarioRunner runner;
    private volatile GroupManager groupManager;
    private WindowManager windowManager;
    private WindowManager.LayoutParams overlayParams;
    private volatile boolean webViewVisible = false;

    private String deviceId;
    private String lastNotificationText = "대기 중...";
    private volatile com.zero.traffic.model.MobileHeaderConfig mobileHeaderConfig;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        try {
            deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            deviceId = Build.SERIAL != null ? Build.SERIAL : "unknown_" + System.currentTimeMillis();
            Logger.w("ANDROID_ID 접근 불가, fallback: " + deviceId);
        }
        worker = Executors.newSingleThreadExecutor();
        ToggleReceiver.serviceRef = this;
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
                stopSelf();
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

        // 3. Chrome 확인/설치 (WebView TLS 핑거프린트 최신화)
        ChromeManager chromeManager = new ChromeManager(this, api);
        if (chromeManager.ensureChromeReady()) {
            Logger.i("Chrome 준비 완료 (v" + chromeManager.getInstalledChromeVersion() + ")");
        } else {
            Logger.w("Chrome 미준비 — 기본 WebView로 진행");
        }

        // ★ Captive portal 검사 URL → 우리 서버로 변경 (204 반환 보장)
        // 기본 URL(connectivitycheck.gstatic.com)이 한국 통신사에서 차단/지연 → "인터넷 없음" 오판
        // 우리 서버가 /generate_204 → 204 No Content 반환 → Android가 네트워크 VALIDATED로 마킹
        try {
            String captiveUrl = serverUrl + "/zero/api/v1/generate_204";
            Settings.Global.putString(getContentResolver(), "captive_portal_http_url", captiveUrl);
            Settings.Global.putString(getContentResolver(), "captive_portal_https_url", captiveUrl);
            Settings.Global.putInt(getContentResolver(), "captive_portal_detection_enabled", 1); // 검사 활성화 (우리 서버로)
            Settings.Global.putInt(getContentResolver(), "captive_portal_mode", 0); // 캡티브 포탈 감지 시 무시
            Logger.i("★ Captive portal URL → " + captiveUrl);

            // 데이터 재연결 — 새 NetworkMonitor가 변경된 URL로 검사 → 204 수신 → VALIDATED
            Logger.i("★ 데이터 재연결 시작 (captive portal URL 적용)");
            Runtime.getRuntime().exec(new String[]{"svc", "data", "disable"}).waitFor();
            Thread.sleep(1500);
            Runtime.getRuntime().exec(new String[]{"svc", "data", "enable"}).waitFor();
            Thread.sleep(5000); // captive portal 검사 + 네트워크 안정화 대기
            Logger.i("★ 데이터 재연결 완료 — 인터넷 연결 상태 갱신됨");
        } catch (Exception e) {
            Logger.w("Captive portal 설정/재연결 실패: " + e.getMessage());
        }

        // 4a. 서버에서 동적 모바일 헤더 가져오기 (워커 스레드 — WebView 초기화 전)
        try {
            mobileHeaderConfig = api.fetchMobileHeaders();
            Logger.i("★ 서버 헤더 수신: UA=" + mobileHeaderConfig.getUserAgent().substring(
                    0, Math.min(80, mobileHeaderConfig.getUserAgent().length())));
        } catch (Exception e) {
            Logger.w("서버 헤더 실패 → 기본 UA 사용: " + e.getMessage());
            mobileHeaderConfig = null;
        }

        // 4b. WebView 초기화 (메인 스레드에서)
        if (!initWebView()) {
            throw new IllegalStateException("WebView 초기화 실패");
        }

        // 5. 매니저 초기화
        taskManager = new TaskManager(api, deviceId);
        scenarioManager = new ScenarioManager(this, api, deviceId);
        scriptEngine = new ScriptEngine(this, api);
        captchaProxy = new CaptchaProxy(api, deviceId);
        runner = new ScenarioRunner(this, webView, captchaProxy, scriptEngine);
        runner.setWebViewHideCallback(() -> setWebViewVisible(false));
        if (mobileHeaderConfig != null) {
            runner.setMobileHeaders(mobileHeaderConfig);
        }

        // 6. 서버 동기화
        scenarioManager.sync();
        scriptEngine.sync();

        // 7. 초기 워밍업 — 첫 작업 전 NNB/BUC 발급
        updateNotification("초기 워밍업 중...");
        warmupWebView();

        Logger.i("초기화 완료. 시나리오: " + scenarioManager.getCount() + "개");
        updateNotification(String.format("%s | %s | %d개 시나리오",
                groupManager.isLeader() ? "★대장" : "쫄병",
                groupManager.getGroupName(),
                scenarioManager.getCount()));
    }

    // ── Stealth JS — StealthConfig에서 공유 참조 (중복 방지) ────
    private static final String STEALTH_JS = com.zero.traffic.engine.StealthConfig.STEALTH_JS;

    /**
     * WebView 초기화 (메인 스레드) + WindowManager 오버레이 표시
     * ★ Stealth 모드: UA에서 "wv" 제거, X-Requested-With 제거, 봇 감지 JS 주입
     */
    private boolean initWebView() {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] created = {false};

        mainHandler.post(() -> {
            try {
                webView = new WebView(TrafficService.this);
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setDatabaseEnabled(true);
                settings.setAllowFileAccess(false);
                settings.setAllowContentAccess(false);
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    settings.setAllowFileAccessFromFileURLs(false);
                    settings.setAllowUniversalAccessFromFileURLs(false);
                }

                // ★ UA 설정: 서버 헤더가 있으면 동적 UA, 없으면 기본 스텔스 UA
                String appliedUA;
                if (mobileHeaderConfig != null && mobileHeaderConfig.isValid()) {
                    appliedUA = mobileHeaderConfig.getUserAgent();
                } else {
                    // 폴백: 기존 방식 (wv 제거 + 버전 보장)
                    String defaultUA = settings.getUserAgentString();
                    appliedUA = defaultUA
                            .replace("; wv)", ")")
                            .replace("Version/4.0 ", "");
                    try {
                        java.util.regex.Matcher m = java.util.regex.Pattern
                                .compile("Chrome/(\\d+)\\.").matcher(appliedUA);
                        if (m.find()) {
                            int ver = Integer.parseInt(m.group(1));
                            if (ver < 131) {
                                appliedUA = appliedUA.replaceFirst(
                                        "Chrome/\\d+\\.[\\d.]+", "Chrome/131.0.6778.200");
                                Logger.i("UA Chrome 버전 업데이트: " + ver + " → 131");
                            }
                        }
                    } catch (Exception e) {
                        Logger.w("UA 버전 파싱 실패: " + e.getMessage());
                    }
                }
                settings.setUserAgentString(appliedUA);
                Logger.i("Stealth UA: " + appliedUA);

                // ★ X-Requested-With 헤더 제거 (앱 패키지명 노출 방지)
                try {
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
                        WebSettingsCompat.setRequestedWithHeaderOriginAllowList(
                                settings, java.util.Collections.emptySet());
                        Logger.i("★ X-Requested-With 제거 완료");
                    } else {
                        Logger.w("X-Requested-With 제거 미지원");
                    }
                } catch (Exception e) {
                    Logger.w("X-Requested-With 제거 실패: " + e.getMessage());
                }

                // ★ 쿠키 설정 (서드파티 쿠키 허용 — 네이버 로그인/트래킹 필요)
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.setAcceptThirdPartyCookies(webView, true);

                // ★ Stealth WebViewClient — 봇 감지 우회 JS 주입 + HTTP 에러 로깅
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                        view.evaluateJavascript(STEALTH_JS, null);
                    }

                    @Override
                    public void onReceivedHttpError(WebView view, WebResourceRequest request,
                            android.webkit.WebResourceResponse errorResponse) {
                        if (request.isForMainFrame() && errorResponse != null) {
                            Logger.w("initWebView HTTP " + errorResponse.getStatusCode()
                                    + ": " + request.getUrl());
                        }
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        return false; // 모든 URL을 WebView 내부에서 처리
                    }
                });

                // ★ WebView 프록시 직접 연결 — 시스템 프록시 우회 (보안/추적 방지)
                if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
                    ProxyController.getInstance().setProxyOverride(
                            new ProxyConfig.Builder().addDirect().build(),
                            java.util.concurrent.Executors.newSingleThreadExecutor(),
                            () -> Logger.i("★ WebView 프록시: 직접 연결 설정 완료"));
                    Logger.i("★ WebView 프록시: PROXY_OVERRIDE 직접 연결 적용");
                } else {
                    Logger.w("★ WebView 프록시: PROXY_OVERRIDE 미지원");
                }

                // WindowManager 오버레이로 WebView를 화면에 붙이기
                windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                int overlayType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE;

                // 완전 투명 오버레이 (렌더링 정상, 사용자에게 안 보임)
                overlayParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        overlayType,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                );
                overlayParams.gravity = Gravity.TOP | Gravity.START;
                overlayParams.alpha = 0f;  // 완전 투명

                windowManager.addView(webView, overlayParams);
                Logger.i("WebView 오버레이 표시 완료 (Stealth 모드)");

                created[0] = true;
            } catch (Exception e) {
                Logger.e("WebView 초기화 실패: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                Logger.e("WebView 초기화 타임아웃");
                return false;
            }
            return created[0] && webView != null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.w("WebView 초기화 인터럽트");
            return false;
        }
    }

    /**
     * 메인 루프
     */
    // ★ IP 회전 주기 — N회 작업마다 1회 IP 변경 (캡챠 빈도 최소화)
    private static final int IP_ROTATE_INTERVAL = 5;
    private int taskCount = 0;   // 마지막 IP 회전 이후 완료된 작업 수

    private void mainLoop() {
        long lastSync = System.currentTimeMillis();

        while (running && !Thread.currentThread().isInterrupted()) {
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
                    taskManager.fail(task.getTrafficId(), task.getSlotId(), "No scenario available");
                    RandomDelay.sleepBetween(30000, 60000);
                    continue;
                }

                // 3. 실행 — WebView 화면 표시
                setWebViewVisible(true);
                StepResult result = runner.execute(scenario, task);

                // 4. 결과 보고
                if (result.isSuccess()) {
                    taskManager.complete(task.getTrafficId(), task.getSlotId());
                } else if (result.isSkip()) {
                    Logger.i("작업 스킵 (slot 페널티 없음): #" + task.getTrafficId() + " → " + result.getMessage());
                } else {
                    taskManager.fail(task.getTrafficId(), task.getSlotId(), result.getMessage());
                }

                // 5. WebView blank + 숨김
                mainHandler.post(() -> { if (webView != null) webView.loadUrl("about:blank"); });
                setWebViewVisible(false);

                taskCount++;
                boolean doRotate = (taskCount >= IP_ROTATE_INTERVAL);

                if (doRotate) {
                    // ★ 5회마다 IP 회전 + 전체 초기화 + 워밍업
                    Logger.i(String.format("★ IP 회전 (작업 %d회 완료)", taskCount));
                    taskCount = 0;
                    updateNotification("IP 변경 중...");
                    rotateIP();
                    updateNotification("브라우저 초기화...");
                    resetWebView();  // NNB/BUC 보존 + 나머지 클리어
                    updateNotification("워밍업 중...");
                    warmupWebView();
                } else {
                    // ★ 같은 IP 유지 — 캐시만 클리어 (쿠키 전체 보존)
                    Logger.i(String.format("★ IP 유지 (이번 IP 작업 %d/%d)", taskCount, IP_ROTATE_INTERVAL));
                    clearCacheOnly();
                }

                // 6. 다음 작업 전 대기
                RandomDelay.sleepBetween(3000, 6000);
                updateNotification("대기 중...");

            } catch (Exception e) {
                Logger.e("루프 오류: " + e.getMessage());
                if (Thread.currentThread().isInterrupted()) {
                    Logger.i("워커 인터럽트 감지 — 루프 종료");
                    break;
                }
                RandomDelay.sleepBetween(30000, 60000);
            }
        }
    }

    /**
     * 캐시/히스토리만 클리어 — 쿠키 전체 보존 (같은 IP 연속 작업 시)
     * NNB/BUC + 세션 쿠키 유지 → nfront 신뢰점수 누적
     */
    private void clearCacheOnly() {
        CountDownLatch latch = new CountDownLatch(1);
        mainHandler.post(() -> {
            try {
                if (webView != null) {
                    webView.stopLoading();
                    webView.clearCache(true);
                    webView.clearHistory();
                    webView.loadUrl("about:blank");
                    Logger.i("캐시 클리어 완료 (쿠키 보존)");
                }
            } catch (Exception e) {
                Logger.w("캐시 클리어 실패: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ── WebView 워밍업 (NNB/BUC 발급) ──────────────────

    /**
     * IP 교체 + 쿠키 클리어 후 네이버 메인 방문 → NNB/BUC 쿠키 발급 유도
     * nfront WAF는 NNB/BUC 없으면 신뢰점수 낮게 평가 → 영수증 캡챠 / 차단 발생
     * 워밍업으로 자연스러운 신규 방문자 신호 생성
     */
    private void warmupWebView() {
        Logger.i("★ 워밍업 시작: m.naver.com 방문 (NNB/BUC 발급)");

        CountDownLatch latch = new CountDownLatch(1);
        java.util.concurrent.atomic.AtomicBoolean done =
                new java.util.concurrent.atomic.AtomicBoolean(false);

        mainHandler.post(() -> {
            try {
                if (webView == null) { latch.countDown(); return; }
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String url, android.graphics.Bitmap fav) {
                        view.evaluateJavascript(STEALTH_JS, null);
                    }
                    @Override
                    public void onPageFinished(WebView view, String loadedUrl) {
                        view.evaluateJavascript(STEALTH_JS, null);
                        if (done.compareAndSet(false, true)) latch.countDown();
                    }
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                        return false;
                    }
                });
                webView.loadUrl("https://m.naver.com");
            } catch (Exception e) {
                Logger.w("워밍업 로드 실패: " + e.getMessage());
                latch.countDown();
            }
        });

        try {
            if (!latch.await(15, TimeUnit.SECONDS)) {
                Logger.w("워밍업: 페이지 로드 타임아웃 — 체류만 진행");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // ★ nlog.naver.com 트래킹 픽셀 로드 대기 → NNB 발급 (통상 2~3초)
        RandomDelay.sleepBetween(4000, 6000);

        // 자연스러운 스크롤 (실제 사용자처럼)
        int scrollCount = RandomDelay.between(2, 4);
        for (int i = 0; i < scrollCount; i++) {
            final int px = RandomDelay.between(250, 500);
            mainHandler.post(() -> {
                try {
                    if (webView != null) {
                        webView.evaluateJavascript(
                                "window.scrollBy({top:" + px + ",behavior:'smooth'})", null);
                    }
                } catch (Exception ignored) {}
            });
            RandomDelay.sleepBetween(1200, 2200);
        }

        // BUC 등 추가 쿠키 발급 대기
        RandomDelay.sleepBetween(4000, 7000);

        // 쿠키 확인 로그
        mainHandler.post(() -> {
            try {
                android.webkit.CookieManager cm = android.webkit.CookieManager.getInstance();
                String cookies = cm.getCookie("https://naver.com");
                boolean hasNnb = cookies != null && cookies.contains("NNB=");
                boolean hasBuc = cookies != null && cookies.contains("BUC=");
                Logger.i("★ 워밍업 완료: NNB=" + hasNnb + " BUC=" + hasBuc
                        + " | 쿠키=" + (cookies != null ? cookies.length() + "자" : "없음"));
            } catch (Exception ignored) {}
        });

        RandomDelay.sleepBetween(500, 1000);
    }

    // ── IP 변경 (비행기 모드 토글) ──────────────────────

    private void rotateIP() {
        // 방법 1: shell 명령어 (가장 확실)
        try {
            Logger.i("IP 변경: 모바일 데이터 OFF");
            Runtime.getRuntime().exec(new String[]{"svc", "data", "disable"}).waitFor();
            RandomDelay.sleepBetween(3000, 5000);

            // ★ 데이터 ON 직전 captive portal 비활성화 — Android가 연결 즉시 검사 시작하므로 선행 필수
            try {
                Settings.Global.putInt(getContentResolver(), "captive_portal_detection_enabled", 0);
                Settings.Global.putInt(getContentResolver(), "captive_portal_mode", 0);
            } catch (Exception ignored) {}

            Logger.i("IP 변경: 모바일 데이터 ON");
            Runtime.getRuntime().exec(new String[]{"svc", "data", "enable"}).waitFor();

            // 네트워크 복구 대기
            RandomDelay.sleepBetween(5000, 8000);

            // 네트워크 연결 확인 (최대 30초)
            for (int i = 0; i < 15; i++) {
                try {
                    java.net.InetAddress addr = java.net.InetAddress.getByName("m.naver.com");
                    if (addr != null) {
                        Logger.i("IP 변경 완료 — 네트워크 복구됨");
                        return;
                    }
                } catch (Exception ignored) {}
                RandomDelay.sleepBetween(2000, 2000);
            }
            Logger.w("IP 변경: 네트워크 복구 타임아웃 — 계속 진행");

        } catch (Exception e) {
            Logger.w("IP 변경 (svc) 실패: " + e.getMessage() + " — 비행기모드로 시도");
            // 방법 2: 비행기모드 Settings.Global (fallback)
            rotateIPviaAirplane();
        }
    }

    private void rotateIPviaAirplane() {
        try {
            Logger.i("IP 변경: 비행기 모드 ON");
            Settings.Global.putInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 1);
            RandomDelay.sleepBetween(3000, 5000);

            // ★ 비행기모드 OFF 직전 captive portal 비활성화
            try {
                Settings.Global.putInt(getContentResolver(), "captive_portal_detection_enabled", 0);
                Settings.Global.putInt(getContentResolver(), "captive_portal_mode", 0);
            } catch (Exception ignored) {}

            Logger.i("IP 변경: 비행기 모드 OFF");
            Settings.Global.putInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0);

            // 네트워크 복구 대기
            RandomDelay.sleepBetween(5000, 8000);

            for (int i = 0; i < 15; i++) {
                try {
                    java.net.InetAddress addr = java.net.InetAddress.getByName("m.naver.com");
                    if (addr != null) {
                        Logger.i("IP 변경 완료 — 네트워크 복구됨");
                        return;
                    }
                } catch (Exception ignored) {}
                RandomDelay.sleepBetween(2000, 2000);
            }
            Logger.w("IP 변경: 네트워크 복구 타임아웃 — 계속 진행");

        } catch (SecurityException e) {
            Logger.w("IP 변경 실패 (권한 없음): " + e.getMessage());
        } catch (Exception e) {
            Logger.w("IP 변경 실패: " + e.getMessage());
        }
    }

    // ── WebView 초기화 (쿠키/캐시 클리어, NNB/BUC 보존) ──

    private void resetWebView() {
        CountDownLatch latch = new CountDownLatch(1);
        mainHandler.post(() -> {
            try {
                if (webView != null) {
                    webView.stopLoading();
                    webView.clearCache(true);
                    webView.clearHistory();

                    // ★ NNB/BUC 보존 — 클리어 전 저장 (nfront 장기 신뢰 쿠키)
                    android.webkit.CookieManager cm = android.webkit.CookieManager.getInstance();
                    String naverCookies = cm.getCookie("https://naver.com");
                    String nnb = extractCookieValue(naverCookies, "NNB");
                    String buc = extractCookieValue(naverCookies, "BUC");

                    cm.removeAllCookies(null);
                    cm.flush();

                    // ★ NNB/BUC 복원 (새 IP에서도 기존 신뢰점수 유지)
                    if (nnb != null) {
                        cm.setCookie("https://naver.com", "NNB=" + nnb + "; domain=.naver.com; path=/");
                        cm.setCookie("https://m.naver.com", "NNB=" + nnb + "; domain=.naver.com; path=/");
                    }
                    if (buc != null) {
                        cm.setCookie("https://naver.com", "BUC=" + buc + "; domain=.naver.com; path=/");
                        cm.setCookie("https://m.naver.com", "BUC=" + buc + "; domain=.naver.com; path=/");
                    }
                    if (nnb != null || buc != null) cm.flush();

                    webView.loadUrl("about:blank");
                    Logger.i("WebView 초기화 완료 (NNB=" + (nnb != null ? "보존" : "없음")
                            + " BUC=" + (buc != null ? "보존" : "없음") + ")");
                }
            } catch (Exception e) {
                Logger.w("WebView 초기화 실패: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** 쿠키 문자열에서 특정 쿠키 값 추출 ("NNB=abc; BUC=xyz" → "abc") */
    private static String extractCookieValue(String cookieStr, String name) {
        if (cookieStr == null || cookieStr.isEmpty()) return null;
        for (String part : cookieStr.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(name + "=")) {
                return trimmed.substring(name.length() + 1).trim();
            }
        }
        return null;
    }

    // ── WebView 보이기/숨기기 ────────────────────────────

    /** mainLoop에서 호출 — 실행 중 보이기, 대기/IP변경 중 숨기기 */
    private void setWebViewVisible(boolean visible) {
        mainHandler.post(() -> {
            if (webView == null || windowManager == null || overlayParams == null) return;
            webViewVisible = visible;
            overlayParams.alpha = visible ? 1.0f : 0f;
            try {
                windowManager.updateViewLayout(webView, overlayParams);
            } catch (Exception e) {
                Logger.w("WebView 표시 변경 실패: " + e.getMessage());
            }
        });
    }

    /** ToggleReceiver에서 호출 (알림 버튼 토글) */
    public void onToggleWebView() {
        mainHandler.post(() -> {
            if (webView == null || windowManager == null || overlayParams == null) return;
            webViewVisible = !webViewVisible;
            overlayParams.alpha = webViewVisible ? 1.0f : 0f;
            try {
                windowManager.updateViewLayout(webView, overlayParams);
            } catch (Exception e) {
                Logger.w("WebView 토글 실패: " + e.getMessage());
            }
            Logger.i("WebView " + (webViewVisible ? "보이기" : "숨기기"));
            updateNotification(lastNotificationText);
        });
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

        // 보이기/숨기기 토글 액션 버튼
        Intent toggleIntent = new Intent(ACTION_TOGGLE_WEBVIEW);
        toggleIntent.setClass(this, ToggleReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent togglePending = PendingIntent.getBroadcast(this, 0, toggleIntent, flags);
        String toggleLabel = webViewVisible ? "\uD83D\uDC41 숨기기" : "\uD83D\uDC41 보이기";

        builder.setContentTitle("Zero Traffic")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .setStyle(new Notification.BigTextStyle().bigText(text + "\n\n[WebView: " + (webViewVisible ? "ON" : "OFF") + "]"))
                .addAction(android.R.drawable.ic_menu_view, toggleLabel, togglePending);

        return builder.build();
    }

    private void updateNotification(String text) {
        lastNotificationText = text;
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.notify(NOTIFICATION_ID, buildNotification(text));
        }
    }

    @Override
    public void onDestroy() {
        running = false;
        ToggleReceiver.serviceRef = null;
        if (worker != null) worker.shutdownNow();
        if (runner != null) runner.cancel();
        if (groupManager != null) groupManager.cleanup();
        WebView currentWebView = webView;
        if (currentWebView != null) {
            CountDownLatch destroyLatch = new CountDownLatch(1);
            mainHandler.post(() -> {
                try {
                    currentWebView.stopLoading();
                    currentWebView.loadUrl("about:blank");
                    currentWebView.clearHistory();
                    currentWebView.removeAllViews();
                    // WindowManager에서 제거
                    if (windowManager != null) {
                        windowManager.removeView(currentWebView);
                    }
                    currentWebView.destroy();
                } catch (Exception e) {
                    Logger.w("WebView 해제 실패: " + e.getMessage());
                } finally {
                    destroyLatch.countDown();
                }
            });
            try {
                destroyLatch.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            webView = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
