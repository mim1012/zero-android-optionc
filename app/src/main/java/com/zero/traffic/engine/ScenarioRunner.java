package com.zero.traffic.engine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.CookieManager;
import android.webkit.WebView;

import com.zero.traffic.captcha.CaptchaProxy;
import com.zero.traffic.model.Scenario;
import com.zero.traffic.model.Step;
import com.zero.traffic.model.StepResult;
import com.zero.traffic.model.TaskInfo;
import com.zero.traffic.util.Logger;
import com.zero.traffic.util.RandomDelay;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 시나리오 실행기 — JSON DSL 파싱 → Action 순차 실행
 *
 * 워커 스레드에서 호출. WebView 조작은 ActionExecutor가 Handler로 처리.
 *
 * ★ BLOCKED(418/429/529) 감지 시 IP 회전 + WebView 초기화 후 시나리오 자동 재시도
 */
public class ScenarioRunner {
    private static final int MAX_CAPTCHA_RETRIES_PER_STEP = 3;
    private static final int MAX_BLOCKED_RETRIES = 2;

    private final Context context;
    private final WebView webView;
    private final ActionExecutor executor;
    private final CaptchaProxy captchaProxy;
    private final ScriptEngine scriptEngine;
    private final Handler mainHandler;

    private volatile boolean cancelled = false;

    public ScenarioRunner(Context context, WebView webView, CaptchaProxy captchaProxy, ScriptEngine scriptEngine) {
        this.context = context;
        this.webView = webView;
        this.executor = new ActionExecutor(context, webView);
        this.captchaProxy = captchaProxy;
        this.scriptEngine = scriptEngine;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /** Chrome 열릴 때 WebView 오버레이 숨기기 콜백 등록 */
    public void setWebViewHideCallback(Runnable callback) {
        executor.setOnChromeOpenCallback(callback);
    }

    /** 서버에서 가져온 모바일 헤더 설정 → ActionExecutor에 전달 */
    public void setMobileHeaders(com.zero.traffic.model.MobileHeaderConfig headers) {
        executor.setMobileHeaders(headers);
    }

    /**
     * 시나리오 실행 (BLOCKED 시 자동 재시도)
     *
     * @param scenario JSON DSL 시나리오
     * @param task     서버에서 받은 작업 정보
     * @return 최종 결과 (success/fail)
     */
    public StepResult execute(Scenario scenario, TaskInfo task) {
        for (int attempt = 0; attempt <= MAX_BLOCKED_RETRIES; attempt++) {
            if (cancelled) return StepResult.abort("Cancelled");

            if (attempt > 0) {
                Logger.w("═══ BLOCKED 재시도 " + attempt + "/" + MAX_BLOCKED_RETRIES
                        + " — IP 회전 + WebView 초기화 ═══");
                rotateIPAndReset();
            }

            StepResult result = executeSteps(scenario, task);

            if (!result.isBlocked()) {
                return result; // 성공, 실패, 캡챠, 중단 → 그대로 반환
            }

            // BLOCKED → 재시도 (마지막이면 반환)
            if (attempt == MAX_BLOCKED_RETRIES) {
                Logger.e("BLOCKED 최대 재시도 초과 (" + MAX_BLOCKED_RETRIES + "회)");
                return StepResult.fail("BLOCKED 재시도 초과 (418/429/529)");
            }
        }

        return StepResult.fail("BLOCKED 재시도 루프 종료");
    }

    /**
     * 시나리오 스텝 순차 실행
     */
    private StepResult executeSteps(Scenario scenario, TaskInfo task) {
        Logger.i("═══ 시나리오 시작: " + scenario.getName() + " ═══");
        Logger.i("  키워드: " + task.getKeyword());
        Logger.i("  MID: " + task.getNvMid());
        Logger.i("  스텝: " + scenario.getSteps().size() + "개");

        // 변수 치환기
        VariableResolver resolver = new VariableResolver(scenario, task);
        int captchaRetryCount = 0;

        for (int i = 0; i < scenario.getSteps().size(); i++) {
            if (cancelled) {
                return StepResult.abort("Cancelled");
            }

            Step rawStep = scenario.getSteps().get(i);
            Step step = resolver.resolve(rawStep);

            Logger.step(step.getId(), step.getAction());

            StepResult result = executeStep(step);

            // 결과 처리
            if (result.isSkip()) {
                Logger.i("스킵: " + step.getId() + " → " + result.getMessage() + " → 다음 작업으로");
                return result;
            }
            if (result.isFailed()) {
                if (result.isCaptcha()) {
                    // CAPTCHA 발견 → 서버 프록시로 해결 시도
                    String onCaptcha = step.getString("onCaptcha", "");
                    if ("solveCaptcha".equals(onCaptcha) || step.getAction().equals("checkStatus")) {
                        captchaRetryCount++;
                        if (captchaRetryCount > MAX_CAPTCHA_RETRIES_PER_STEP) {
                            Logger.e("CAPTCHA 최대 재시도 초과 (" + MAX_CAPTCHA_RETRIES_PER_STEP + "회) at " + step.getId());
                            return StepResult.fail("CAPTCHA 재시도 초과 at " + step.getId());
                        }
                        Logger.w("CAPTCHA 감지 → 해결 시도 (" + captchaRetryCount + "/" + MAX_CAPTCHA_RETRIES_PER_STEP + ")");
                        boolean solved = captchaProxy.solve(webView);
                        if (!solved) {
                            Logger.e("CAPTCHA 해결 실패");
                            return StepResult.fail("CAPTCHA 해결 실패 at " + step.getId());
                        }
                        Logger.i("CAPTCHA 해결 성공");
                        i--; // 현재 스텝을 다시 실행
                        continue;
                    }
                }
                if (result.isBlocked()) {
                    // ★ BLOCKED → execute()의 외부 루프에서 IP 회전 후 재시도
                    Logger.w("BLOCKED 감지 at " + step.getId() + " → 재시도 요청");
                    return result;
                }
                if (result.isAbort()) {
                    Logger.e("중단: " + result.getMessage());
                    return result;
                }
                // 일반 실패
                Logger.e("스텝 실패: " + step.getId() + " → " + result.getMessage());
                return result;
            }
            // 스텝 성공 시 CAPTCHA 카운터 리셋
            captchaRetryCount = 0;
        }

        Logger.i("═══ 시나리오 완료: " + scenario.getName() + " ═══");
        return StepResult.success();
    }

    /**
     * ★ IP 회전 + WebView 초기화 (BLOCKED 재시도용)
     * - svc data disable/enable로 IP 변경
     * - WebView 쿠키/캐시 클리어
     * - 새로운 세션으로 시작
     */
    private void rotateIPAndReset() {
        try {
            // 1. IP 회전 — 비행기모드 토글 (WRITE_SECURE_SETTINGS 필요)
            // svc data는 앱 UID에서 실행 불가 → Settings.Global 방식 사용
            Logger.i("★ BLOCKED 재시도: IP 회전 시작 (비행기모드 토글)");
            android.content.ContentResolver cr = context.getContentResolver();

            android.provider.Settings.Global.putInt(cr,
                    android.provider.Settings.Global.AIRPLANE_MODE_ON, 1);
            android.content.Intent airOn = new android.content.Intent(
                    android.content.Intent.ACTION_AIRPLANE_MODE_CHANGED);
            airOn.putExtra("state", true);
            context.sendBroadcast(airOn);
            Thread.sleep(4000);

            android.provider.Settings.Global.putInt(cr,
                    android.provider.Settings.Global.AIRPLANE_MODE_ON, 0);
            android.content.Intent airOff = new android.content.Intent(
                    android.content.Intent.ACTION_AIRPLANE_MODE_CHANGED);
            airOff.putExtra("state", false);
            context.sendBroadcast(airOff);

            // 네트워크 복구 대기 (최대 15초)
            for (int i = 0; i < 15; i++) {
                Thread.sleep(1000);
                try {
                    java.net.InetAddress addr = java.net.InetAddress.getByName("m.naver.com");
                    if (addr != null) {
                        Logger.i("★ IP 회전 완료 (" + (i + 1) + "초)");
                        break;
                    }
                } catch (Exception ignored) {}
            }

            // 2. WebView 쿠키/캐시 클리어 (메인 스레드)
            CountDownLatch latch = new CountDownLatch(1);
            mainHandler.post(() -> {
                try {
                    if (webView != null) {
                        webView.stopLoading();
                        webView.clearCache(true);
                        webView.clearHistory();
                        CookieManager.getInstance().removeAllCookies(null);
                        CookieManager.getInstance().flush();
                        webView.loadUrl("about:blank");
                        Logger.i("★ WebView 초기화 완료 (쿠키/캐시 클리어)");
                    }
                } catch (Exception e) {
                    Logger.w("WebView 리셋 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
            latch.await(5, TimeUnit.SECONDS);

            // 3. ActionExecutor 상태 초기화 (stale lastHttpStatus 방지)
            executor.resetState();

            // 4. 안정화 대기 (너무 빠른 재접속 방지)
            RandomDelay.sleepBetween(3000, 5000);

        } catch (Exception e) {
            Logger.w("IP 회전/리셋 실패: " + e.getMessage());
        }
    }

    /**
     * 개별 스텝 실행 (action별 분기)
     */
    private StepResult executeStep(Step step) {
        switch (step.getAction()) {
            case "navigate":
                return executor.navigate(step);
            case "delay":
                return executor.delay(step);
            case "tap":
                return executor.tap(step);
            case "humanType":
                return executor.humanType(step);
            case "press":
                return executor.press(step);
            case "scroll":
                return executor.scroll(step);
            case "scrollTo":
                return executor.scrollTo(step);
            case "checkStatus":
                return executor.checkStatus(step);
            case "clickProduct":
                return executor.clickProduct(step);
            case "dwell":
                return executor.dwell(step);
            case "report":
                return executor.report(step);
            case "log":
                return executor.log(step);
            case "evalJS":
                return executor.evalJS(step);
            case "findMid":
                return executor.findMid(step);
            case "runScript":
                String name = step.getString("scriptName", "");
                String content = scriptEngine.getScript(name);
                if (content == null) return StepResult.fail("Script not found: " + name);
                return executor.runScript(step, content);
            default:
                Logger.w("Unknown action: " + step.getAction());
                return StepResult.fail("Unknown action: " + step.getAction());
        }
    }

    /**
     * 실행 취소 + WebView destroy 마킹
     */
    public void cancel() {
        cancelled = true;
        executor.markDestroyed();
    }
}
