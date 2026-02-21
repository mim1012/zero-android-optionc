package com.zero.traffic.engine;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.browser.customtabs.CustomTabsIntent;

import com.zero.traffic.model.Step;
import com.zero.traffic.model.StepResult;
import com.zero.traffic.util.Logger;
import com.zero.traffic.util.RandomDelay;

import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 개별 Action 실행기 — WebView 제어
 *
 * 모든 메서드는 워커 스레드에서 호출.
 * WebView 조작은 Handler를 통해 메인 스레드에서 실행.
 *
 * ★ Stealth: 모든 페이지 전환에서 봇 감지 우회 JS 자동 주입
 */
public class ActionExecutor {
    private final Context context;
    private final WebView webView;
    private final Handler mainHandler;

    // ★ WebView destroy 감지 플래그 (final 필드는 null 체크 불가)
    private volatile boolean destroyed = false;

    // Chrome 폴백 사용 시 true — dwell 스텝에서 WebView 스크롤 대신 단순 대기
    private volatile boolean chromeFallbackUsed = false;

    // Chrome 열릴 때 WebView를 숨기기 위한 콜백 (TrafficService에서 주입)
    private Runnable onChromeOpenCallback;

    // 서버에서 가져온 헤더 설정 (navigate 시 적용)
    private com.zero.traffic.model.MobileHeaderConfig mobileHeaders;

    public void setMobileHeaders(com.zero.traffic.model.MobileHeaderConfig headers) {
        this.mobileHeaders = headers;
    }

    // 현재 탐색 중인 nv_mid — shouldOverrideUrlLoading에서 smartstore 우회 시 사용
    private volatile String currentMid = "";

    // ★ smartstore/brand 인터셉트 시 저장한 실제 목적지 URL (Chrome 오픈에 사용)
    private volatile String interceptedProductUrl = "";

    // ★ HTTP 상태 코드 추적 (onReceivedHttpError에서 설정)
    private volatile int lastHttpStatus = 200;

    // ★ Stealth JS — StealthConfig에서 공유 참조 (중복 방지)
    private static final String STEALTH_JS = StealthConfig.STEALTH_JS;

    public ActionExecutor(Context context, WebView webView) {
        this.context = context;
        this.webView = webView;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * ★ 상태 초기화 (BLOCKED 재시도 전 ScenarioRunner에서 호출)
     * - lastHttpStatus, chromeFallbackUsed 리셋
     */
    public void resetState() {
        lastHttpStatus = 200;
        chromeFallbackUsed = false;
        currentMid = "";
        interceptedProductUrl = "";
    }

    // ── navigate ────────────────────────────────────────

    public StepResult navigate(Step step) {
        String url = step.getString("url");
        if (url.isEmpty()) return StepResult.fail("navigate: url empty");
        Uri parsed = Uri.parse(url);
        String scheme = parsed.getScheme();
        if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            return StepResult.fail("navigate: unsupported scheme: " + url);
        }

        Logger.step(step.getId(), "navigate", url);
        chromeFallbackUsed = false; // 새 네비게이션 시 리셋
        interceptedProductUrl = "";
        lastHttpStatus = 200; // HTTP 상태 리셋

        // ★ 인간적 pre-navigation 딜레이 (즉시 연속 요청 방지)
        RandomDelay.sleepBetween(200, 600);

        CompletableFuture<Void> future = new CompletableFuture<>();
        long timeout = step.getLong("timeout", 30000);

        mainHandler.post(() -> {
            if (isWebViewDestroyed()) {
                future.completeExceptionally(new IllegalStateException("webview destroyed"));
                return;
            }

            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                    view.evaluateJavascript(getActiveStealthJS(), null);
                }

                @Override
                public void onPageFinished(WebView view, String loadedUrl) {
                    view.evaluateJavascript(getActiveStealthJS(), null);
                    if (!future.isDone()) {
                        future.complete(null);
                    }
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    if (request != null && request.isForMainFrame() && !future.isDone()) {
                        CharSequence description = error != null ? error.getDescription() : null;
                        future.completeExceptionally(
                                new IllegalStateException(description != null ? description.toString() : "load error"));
                    }
                }

                // ★ HTTP 418/529 상태 코드 캡처 (API 23+)
                @Override
                public void onReceivedHttpError(WebView view, WebResourceRequest request,
                        android.webkit.WebResourceResponse errorResponse) {
                    if (request.isForMainFrame() && errorResponse != null) {
                        int code = errorResponse.getStatusCode();
                        lastHttpStatus = code;
                        Logger.w("HTTP " + code + " 감지: " + request.getUrl().toString()
                                .substring(0, Math.min(80, request.getUrl().toString().length())));
                    }
                }

                // ★ smartstore/brand → HTTP 요청 자체를 차단 (nfront 429 완전 방지)
                // shouldOverrideUrlLoading은 서버사이드 302 리다이렉트에 미발동 →
                // shouldInterceptRequest는 모든 요청(302 포함)에 발동하므로 확실히 차단
                @Override
                public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    if (request.isForMainFrame() &&
                        (url.contains("smartstore.naver.com") || url.contains("brand.naver.com"))) {
                        Logger.i("★ smartstore 요청 차단 (HTTP 전): " + url.substring(0, Math.min(80, url.length())));
                        chromeFallbackUsed = true;
                        interceptedProductUrl = url;
                        return new android.webkit.WebResourceResponse("text/html", "UTF-8",
                            new java.io.ByteArrayInputStream("".getBytes()));
                    }
                    return null;
                }

                // ★ smartstore/brand → Chrome Custom Tabs 전환 (클라이언트 네비게이션용 백업)
                // nfront WAF는 WebView HTTP/2 핑거프린트를 감지하여 차단
                // bridge URL(cr*.shopping.naver.com)은 클릭 추적용 → WebView에서 정상 처리
                // 최종 목적지(smartstore/brand)에 도달할 때만 Chrome으로 전환
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return interceptToChrome(request.getUrl().toString());
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return interceptToChrome(url);
                }

                private boolean interceptToChrome(String url) {
                    if (url == null) return false;

                    // Bridge URL (cr*.shopping.naver.com) → WebView에서 정상 처리
                    // 네이버 클릭 추적이 정상 동작하도록 WebView가 bridge를 따라감
                    if (url.contains("cr.shopping.naver.com") || url.contains("cr2.shopping.naver.com")
                            || url.contains("cr3.shopping.naver.com")) {
                        Logger.i("Bridge 통과 (WebView): " + url.substring(0, Math.min(80, url.length())));
                        return false; // WebView에서 처리
                    }

                    // ★ smartstore/brand URL → 플래그만 설정 (Chrome은 IP 회전 후 열기)
                    // nfront는 IP 평판 기반 차단 → 같은 IP로 Chrome 열면 418 에러
                    // findMid/dwell에서 IP 회전 후 Chrome으로 상품페이지 열기
                    if (url.contains("smartstore.naver.com") || url.contains("brand.naver.com")) {
                        Logger.i("smartstore 감지 → Chrome 전환 예약 (IP 회전 후 열기)");
                        chromeFallbackUsed = true;
                        interceptedProductUrl = url;
                        return true; // WebView 네비게이션 취소
                    }
                    return false;
                }
            });

            webView.loadUrl(url, buildNavHeaders(url));
        });

        try {
            future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return StepResult.fail("navigate failed: " + e.getMessage());
        }

        // ★ HTTP 418/529 감지 → blocked 반환 (ScenarioRunner에서 재시도)
        if (lastHttpStatus == 418 || lastHttpStatus == 529) {
            Logger.w("navigate: HTTP " + lastHttpStatus + " → blocked 반환");
            return StepResult.blocked();
        }

        // domcontentloaded 대기
        RandomDelay.sleepBetween(300, 700);
        return StepResult.success();
    }

    // ── delay ───────────────────────────────────────────

    public StepResult delay(Step step) {
        int[] ms = step.getIntRange("ms", new int[]{1000, 2000});
        int wait = RandomDelay.between(ms[0], ms[1]);
        Logger.step(step.getId(), "delay", wait + "ms");
        RandomDelay.sleep(wait);
        return StepResult.success();
    }

    // ── tap ──────────────────────────────────────────────

    public StepResult tap(Step step) {
        String selector = step.getString("selector");
        String fallback = step.getString("fallback", "");
        String fallbackText = step.getString("fallbackText", "");
        boolean removeTarget = step.getBoolean("removeTarget", false);
        boolean waitNav = step.getBoolean("waitNav", false);

        Logger.step(step.getId(), "tap", selector);

        // JS로 요소 좌표 가져오기
        String js = buildTapJS(selector, fallback, fallbackText, removeTarget);
        String result = evalJSSync(js, step.getLong("timeout", 10000));

        if (result == null || result.equals("null") || result.isEmpty()) {
            return StepResult.fail("tap: element not found: " + selector);
        }

        try {
            JSONObject rect = new JSONObject(result);
            float x = (float) rect.getDouble("x");
            float y = (float) rect.getDouble("y");

            // MotionEvent 터치 시뮬레이션
            simulateTouch(x, y);

            if (waitNav) {
                RandomDelay.sleepBetween(2000, 3000);
            }
            return StepResult.success();

        } catch (Exception e) {
            return StepResult.fail("tap parse error: " + e.getMessage());
        }
    }

    private String buildTapJS(String selector, String fallback, String fallbackText, boolean removeTarget) {
        StringBuilder sb = new StringBuilder();
        sb.append("(function(){");
        sb.append("var el=document.querySelector(").append(jsQuote(selector)).append(");");
        if (!fallback.isEmpty()) {
            sb.append("if(!el) el=document.querySelector(").append(jsQuote(fallback)).append(");");
        }
        if (!fallbackText.isEmpty()) {
            sb.append("if(!el){var links=document.querySelectorAll('a');");
            sb.append("for(var i=0;i<links.length;i++){");
            sb.append("if(links[i].textContent.trim().startsWith(").append(jsQuote(fallbackText)).append(")){el=links[i];break;}}}");
        }
        sb.append("if(!el) return 'null';");
        if (removeTarget) {
            sb.append("el.removeAttribute('target');");
        }
        sb.append("var r=el.getBoundingClientRect();");
        sb.append("var dpr=window.devicePixelRatio||1;");
        sb.append("return JSON.stringify({x:(r.x+r.width/2)*dpr,y:(r.y+r.height/2)*dpr});");
        sb.append("})()");
        return sb.toString();
    }

    // ── humanType ───────────────────────────────────────

    public StepResult humanType(Step step) {
        String selector = step.getString("selector");
        String fallback = step.getString("fallback", "");
        String text = step.getString("text");
        boolean clearFirst = step.getBoolean("clearFirst", false);
        int[] charDelay = step.getIntRange("charDelay", new int[]{60, 140});
        int[] gapDelay = step.getIntRange("gapDelay", new int[]{20, 70});

        Logger.step(step.getId(), "humanType", text.length() > 30 ? text.substring(0, 30) + "..." : text);

        // 요소 찾기 + 포커스
        String selectorLiteral = jsQuote(selector);
        String fallbackLiteral = jsQuote(fallback);
        String findJS = "(function(){var el=document.querySelector(" + selectorLiteral + ");" +
                "if(!el && " + fallbackLiteral + ") el=document.querySelector(" + fallbackLiteral + ");" +
                "if(!el) return 'not_found';" +
                "el.click();el.focus();return 'found';})()";

        String found = evalJSSync(findJS, 5000);
        if (!"found".equals(found)) {
            return StepResult.fail("humanType: element not found: " + selector);
        }

        RandomDelay.sleepBetween(200, 400);

        // clearFirst
        if (clearFirst) {
            evalJSSync(
                    "(function(){var el=document.querySelector(" + selectorLiteral + ");" +
                    "if(el){el.value='';el.dispatchEvent(new Event('input',{bubbles:true}));}})()",
                    3000);
            RandomDelay.sleepBetween(100, 200);
        }

        // 한 글자씩 타이핑 (dispatchEvent로 실제 키 이벤트)
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String cLiteral = jsQuote(String.valueOf(c));
            String typeJS = "(function(){var el=document.querySelector(" + selectorLiteral + ");" +
                    "if(el){el.value+=" + cLiteral + ";" +
                    "el.dispatchEvent(new Event('input',{bubbles:true}));" +
                    "el.dispatchEvent(new KeyboardEvent('keydown',{key:" + cLiteral + ",bubbles:true}));" +
                    "el.dispatchEvent(new KeyboardEvent('keyup',{key:" + cLiteral + ",bubbles:true}));}})()";

            evalJSSync(typeJS, 2000);
            RandomDelay.sleep(RandomDelay.between(charDelay[0], charDelay[1]));
            if (i < text.length() - 1) {
                RandomDelay.sleep(RandomDelay.between(gapDelay[0], gapDelay[1]));
            }
        }

        return StepResult.success();
    }

    // ── press ───────────────────────────────────────────

    public StepResult press(Step step) {
        String key = step.getString("key");
        boolean waitNav = step.getBoolean("waitNav", false);
        Logger.step(step.getId(), "press", key);

        String keyLiteral = jsQuote(key);
        String js = "document.dispatchEvent(new KeyboardEvent('keydown',{key:" + keyLiteral +
                ",code:" + keyLiteral + ",bubbles:true}));" +
                "document.dispatchEvent(new KeyboardEvent('keyup',{key:" + keyLiteral +
                ",code:" + keyLiteral + ",bubbles:true}));";

        // Enter는 form submit 시뮬레이션
        if ("Enter".equals(key)) {
            js = "(function(){" +
                 "var active=document.activeElement;" +
                 "if(active&&active.form){active.form.submit();}" +
                 "else{document.dispatchEvent(new KeyboardEvent('keydown',{key:'Enter',code:'Enter',keyCode:13,bubbles:true}));}" +
                 "})()";
        }

        evalJSSync(js, 3000);

        if (waitNav) {
            RandomDelay.sleepBetween(2000, 3500);
        }
        return StepResult.success();
    }

    // ── scroll ──────────────────────────────────────────

    public StepResult scroll(Step step) {
        int distance = step.getInt("distance", 2000);
        int[] stepRange = step.getIntRange("stepRange", new int[]{150, 350});
        int[] stepDelay = step.getIntRange("stepDelay", new int[]{200, 400});

        Logger.step(step.getId(), "scroll", distance + "px");

        int scrolled = 0;
        while (scrolled < distance) {
            int px = RandomDelay.between(stepRange[0], stepRange[1]);
            evalJSSync(String.format("window.scrollBy({top:%d,behavior:'smooth'})", px), 2000);
            scrolled += px;
            RandomDelay.sleep(RandomDelay.between(stepDelay[0], stepDelay[1]));
        }
        return StepResult.success();
    }

    // ── scrollTo ────────────────────────────────────────

    public StepResult scrollTo(Step step) {
        int y = step.getInt("y", 0);
        Logger.step(step.getId(), "scrollTo", "y=" + y);
        evalJSSync(String.format("window.scrollTo(0,%d)", y), 2000);
        RandomDelay.sleepBetween(500, 800);
        return StepResult.success();
    }

    // ── checkStatus ─────────────────────────────────────

    public StepResult checkStatus(Step step) {
        Logger.step(step.getId(), "checkStatus");

        // Chrome 폴백 사용한 경우 → 이미 성공
        if (chromeFallbackUsed) {
            Logger.i("checkStatus: Chrome 폴백 사용됨 → OK");
            return StepResult.success();
        }

        // ★ HTTP 상태 코드 기반 차단 감지 (418/429/529)
        if (lastHttpStatus == 418 || lastHttpStatus == 429 || lastHttpStatus == 529) {
            Logger.w("checkStatus: HTTP " + lastHttpStatus + " 감지 → blocked");
            String onBlocked = step.getString("onBlocked", "");
            if ("abort".equals(onBlocked)) return StepResult.abort("HTTP " + lastHttpStatus);
            return StepResult.blocked();
        }

        // ★ nfront 에러페이지 + 차단/캡챠 감지 (콘텐츠 기반)
        String js = "(function(){" +
                "var title=document.title||'';" +
                "if(title.includes('에러페이지')) return 'blocked_error';" +
                "var t=document.body?document.body.innerText:'';" +
                // 차단 패턴 (418/429/529 공통)
                "if(t.includes('비정상적인 접근')||t.includes('일시적으로 제한')" +
                "||t.includes('자동화된 접근')||t.includes('접근이 제한')" +
                "||t.includes('잠시 후 다시')||t.includes('이용이 제한')" +
                "||t.includes('서버 오류')||t.includes('과부하')" +
                "||t.includes('Too Many Requests')||t.includes('I\\'m a Teapot')" +
                "||t.includes('Service Unavailable')) return 'blocked';" +
                // 영수증 캡챠 패턴
                "if(t.includes('자동입력방지')" +
                "||(t.includes('보안 확인')&&(t.includes('영수증')||t.includes('무엇입니까')))" +
                "||t.includes('보안문자를 입력')) return 'captcha';" +
                "return 'ok';})()";

        String status = evalJSSync(js, 5000);
        Logger.i("checkStatus result: " + status + " (HTTP " + lastHttpStatus + ")");

        if ("blocked_error".equals(status) || "blocked".equals(status)) {
            String onBlocked = step.getString("onBlocked", "");
            if ("abort".equals(onBlocked)) return StepResult.abort("Page blocked");
            return StepResult.blocked();
        }
        if ("captcha".equals(status)) {
            return StepResult.captcha();
        }
        return StepResult.success();
    }

    // ── clickProduct ────────────────────────────────────

    public StepResult clickProduct(Step step) {
        int index = step.getInt("index", 2);
        String mid = step.getString("mid", "");
        String exclude = step.getString("excludePattern", "lst*(A|P|D)");

        Logger.step(step.getId(), "clickProduct", "#" + index + " mid=" + mid);

        // N번째 상품 찾기 (+ href 추출)
        String findJS = String.format(
                "(function(){var list=[];var seq=0;" +
                "var re=new RegExp(%s);" +
                "document.querySelectorAll('a[data-shp-contents-id]').forEach(function(a){" +
                "if(re.test(a.getAttribute('data-shp-inventory')||''))return;" +
                "seq++;list.push({mid:a.getAttribute('data-shp-contents-id')||'',i:seq});});" +
                "var t=list.find(function(p){return p.i===%d;});" +
                "if(!t)return 'null';" +
                "var el=document.querySelector('a[data-shp-contents-id=\"'+t.mid+'\"]');" +
                "if(!el)return 'null';" +
                "el.removeAttribute('target');" +
                "el.scrollIntoView({block:'center',behavior:'smooth'});" +
                "var r=el.getBoundingClientRect();" +
                "var dpr=window.devicePixelRatio||1;" +
                "return JSON.stringify({x:(r.x+r.width/2)*dpr,y:(r.y+r.height/2)*dpr,mid:t.mid,href:el.href||''});})()",
                jsQuote(exclude), index);

        // 스크롤 후 대기
        RandomDelay.sleepBetween(500, 1000);

        String result = evalJSSync(findJS, 10000);
        if (result == null || result.equals("null") || result.isEmpty()) {
            return StepResult.fail("clickProduct: product #" + index + " not found");
        }

        try {
            JSONObject pos = new JSONObject(result);
            float x = (float) pos.getDouble("x");
            float y = (float) pos.getDouble("y");
            String href = pos.optString("href", "");

            RandomDelay.sleepBetween(300, 700);
            simulateTouch(x, y);

            // 네비게이션 대기
            RandomDelay.sleepBetween(2500, 4000);

            // ★ 418/429/529 감지 + IP 회전 후 Chrome 폴백
            String pageState = detectPageState();
            if (isBlockedState(pageState)) {
                Logger.w("clickProduct: " + pageState + " 감지 → IP 회전 후 Chrome 폴백");
                String targetUrl;
                if (!interceptedProductUrl.isEmpty()) {
                    targetUrl = interceptedProductUrl;
                } else if (!href.isEmpty()) {
                    targetUrl = href;
                } else if (!mid.isEmpty()) {
                    targetUrl = "https://msearch.shopping.naver.com/catalog/" + mid;
                } else {
                    return StepResult.blocked();
                }
                quickRotateIP();
                openInChrome(targetUrl);
                chromeFallbackUsed = true;
                return StepResult.success();
            }

            return StepResult.success();

        } catch (Exception e) {
            return StepResult.fail("clickProduct error: " + e.getMessage());
        }
    }

    // ── dwell (체류) ────────────────────────────────────

    public StepResult dwell(Step step) {
        int[] ms = step.getIntRange("ms", new int[]{3000, 5000});
        int scrollDist = step.getInt("scrollDist", 1500);
        int[] scrollCount = step.getIntRange("scrollCount", new int[]{1, 2});

        int dwellTime = RandomDelay.between(ms[0], ms[1]);
        Logger.step(step.getId(), "dwell", dwellTime + "ms");

        // ★ Chrome 폴백 사용 시 → Chrome에서 체류 중이므로 대기 후 HOME
        // Chrome Custom Tabs는 bridge URL 리다이렉트(2~3초) + 페이지 로드(1~2초) 필요
        // 최소 8초 대기: 리다이렉트 3초 + 로드 2초 + 실제 체류 3초
        if (chromeFallbackUsed) {
            int chromeMinDwell = 8000; // Chrome 최소 체류 시간
            int chromeDwell = Math.max(dwellTime, chromeMinDwell);
            // 약간의 랜덤 추가 (8~12초)
            chromeDwell = RandomDelay.between(chromeDwell, chromeDwell + 4000);
            Logger.i("dwell: Chrome Custom Tabs에서 체류 (" + chromeDwell + "ms, 원래=" + dwellTime + "ms)");
            RandomDelay.sleep(chromeDwell);
            // HOME 버튼으로 Chrome 닫기
            pressHome();
            RandomDelay.sleepBetween(500, 1000);
            chromeFallbackUsed = false;
            interceptedProductUrl = "";
            return StepResult.success();
        }

        // 디버그: 체류 시작 시 URL 확인
        String dwellUrl = evalJSSync("(function(){return window.location.href;})()", 3000);
        Logger.i("dwell URL: " + dwellUrl);

        long start = System.currentTimeMillis();

        // 스크롤
        int count = RandomDelay.between(scrollCount[0], scrollCount[1]);
        int perScroll = scrollDist / Math.max(count, 1);
        for (int i = 0; i < count; i++) {
            int px = RandomDelay.between(perScroll - 50, perScroll + 50);
            evalJSSync(String.format("window.scrollBy({top:%d,behavior:'smooth'})", px), 2000);
            RandomDelay.sleepBetween(800, 1500);
        }

        // 남은 시간 대기
        long elapsed = System.currentTimeMillis() - start;
        long remaining = dwellTime - elapsed;
        if (remaining > 0) {
            RandomDelay.sleep((int) remaining);
        }

        return StepResult.success();
    }

    // ── report ──────────────────────────────────────────

    public StepResult report(Step step) {
        String status = step.getString("status", "completed");
        Logger.step(step.getId(), "report", status);
        // TaskManager에서 처리 — 여기서는 status 전달만
        return StepResult.success();
    }

    // ── log ─────────────────────────────────────────────

    public StepResult log(Step step) {
        String message = step.getString("message", "");
        Logger.i("[LOG] " + message);
        return StepResult.success();
    }

    // ── runScript (JS 주입) ─────────────────────────────

    public StepResult runScript(Step step, String scriptContent) {
        Logger.step(step.getId(), "runScript", step.getString("scriptName"));
        String result = evalJSSync(scriptContent, step.getLong("timeout", 30000));
        if (result != null && result.startsWith("ERROR:")) {
            return StepResult.fail(result);
        }
        return StepResult.success();
    }

    // ── evalJS (인라인 JS + 변수 주입) ───────────────────

    public StepResult evalJS(Step step) {
        String script = step.getString("script", "");
        if (script.isEmpty()) return StepResult.fail("evalJS: empty script");

        Logger.step(step.getId(), "evalJS");

        // vars 필드가 있으면 JSON으로 직렬화하여 __V 변수로 주입
        JSONObject varsObj = step.getRaw().optJSONObject("vars");
        if (varsObj != null) {
            // JSONObject.toString()은 올바르게 이스케이프된 JSON 출력
            script = "var __V=" + varsObj.toString() + ";" + script;
        }

        String result = evalJSSync(script, step.getLong("timeout", 30000));
        if (result != null && result.startsWith("ERROR:")) {
            return StepResult.fail(result);
        }
        return StepResult.success();
    }

    // ── findMid (3전략 MID 탐색 + 쇼핑 컴포넌트 페이지네이션 + 클릭) ────

    public StepResult findMid(Step step) {
        String mid = step.getString("mid", "");
        int maxScroll = step.getInt("maxScroll", 10);
        int maxPages = step.getInt("maxPages", 5);
        if (mid.isEmpty()) return StepResult.fail("findMid: mid empty");

        String keyword = step.getString("keyword", ""); // 쇼핑 검색 폴백용

        Logger.step(step.getId(), "findMid", "mid=" + mid);
        chromeFallbackUsed = false;
        interceptedProductUrl = "";
        currentMid = mid;

        // ★ 진단: 현재 URL + 페이지 제목
        String diagUrl = evalJSSync("window.location.href", 3000);
        String diagTitle = evalJSSync("document.title", 3000);
        Logger.i("findMid 시작: url=" + diagUrl + " title=" + diagTitle);

        // ★ 일반 네이버 검색 페이지 → 쇼핑 검색으로 이동 (keyword 있을 때)
        // 랜딩 → m.search.naver.com 으로 리다이렉트되면 쇼핑 컴포넌트 없을 수 있음
        if (!keyword.isEmpty() && diagUrl != null && diagUrl.contains("m.search.naver.com")) {
            String shoppingSearchUrl = "https://msearch.shopping.naver.com/search/all?query="
                    + android.net.Uri.encode(keyword);
            Logger.i("findMid: 일반 검색 감지 → 쇼핑 검색 이동: " + keyword);
            CompletableFuture<Void> shopNav = new CompletableFuture<>();
            mainHandler.post(() -> {
                if (isWebViewDestroyed()) { shopNav.complete(null); return; }
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String u, android.graphics.Bitmap fav) {
                        view.evaluateJavascript(getActiveStealthJS(), null);
                    }
                    @Override
                    public void onPageFinished(WebView view, String loadedUrl) {
                        view.evaluateJavascript(getActiveStealthJS(), null);
                        if (!shopNav.isDone()) shopNav.complete(null);
                    }
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                        String u = req.getUrl().toString();
                        if (req.isForMainFrame() &&
                            (u.contains("smartstore.naver.com") || u.contains("brand.naver.com"))) {
                            chromeFallbackUsed = true;
                            interceptedProductUrl = u;
                            return new WebResourceResponse("text/html", "UTF-8",
                                new java.io.ByteArrayInputStream("".getBytes()));
                        }
                        return null;
                    }
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                        String u = req.getUrl().toString();
                        if (u.contains("smartstore.naver.com") || u.contains("brand.naver.com")) {
                            chromeFallbackUsed = true;
                            interceptedProductUrl = u;
                            return true;
                        }
                        return false;
                    }
                });
                webView.loadUrl(shoppingSearchUrl);
            });
            try { shopNav.get(15000, TimeUnit.MILLISECONDS); } catch (Exception ignored) {}
            RandomDelay.sleepBetween(1000, 1500);
            diagUrl = evalJSSync("window.location.href", 3000);
            Logger.i("findMid: 쇼핑 검색 이동 완료: " + diagUrl);
        }

        // ★ 검색 자동완성/추천 오버레이 제거
        evalJSSync(
            "(function(){" +
            "if(document.activeElement)document.activeElement.blur();" +
            "var ov=document.querySelectorAll('[class*=\"suggest\"],[class*=\"autocomplete\"],[class*=\"recommend\"]');" +
            "for(var i=0;i<ov.length;i++){ov[i].style.display='none';}" +
            "})()", 3000);
        RandomDelay.sleepBetween(300, 500);

        // ★ MID 링크 탐색 + el.click()
        String midLiteral = mid.replace("'", "\\'");
        String findAndClickJS =
            "(function(){var mid='" + midLiteral + "';" +
            "var el=document.querySelector('a[href*=\"nv_mid='+mid+'\"]');" +
            "var s=1;" +
            "if(!el){el=document.querySelector('a[href*=\"/products/'+mid+'\"]');s=2;}" +
            "if(!el){var c=document.querySelector('[id=\"nstore_productId_'+mid+'\"]');" +
            "if(c){el=c.previousElementSibling;" +
            "while(el&&el.tagName!=='A')el=el.previousElementSibling;" +
            "if(!el)el=c.closest('a');s=3;}}" +
            // 전략 4: data-shp-contents-id (통합검색 쇼핑 섹션)
            "if(!el){el=document.querySelector('a[data-shp-contents-id=\"'+mid+'\"]');s=4;}" +
            // 전략 5: /catalog/<mid> URL (msearch.shopping.naver.com/catalog/)
            "if(!el){el=document.querySelector('a[href*=\"/catalog/'+mid+'\"]');s=5;}" +
            "if(!el)return 'not_found';" +
            "el.removeAttribute('target');" +
            "el.scrollIntoView({block:'center',behavior:'smooth'});" +
            "var href=el.href||'';" +
            "el.click();" +
            "return JSON.stringify({s:s,href:href,clicked:true});})()";

        // ★ 쇼핑 컴포넌트 페이지네이션 클릭 JS (숫자 페이지 버튼 + 다음 버튼)
        // 네이버 가격비교 컴포넌트의 페이지 버튼을 찾아 el.click()으로 AJAX 전환
        // targetPage를 String.format으로 주입
        String clickPageJSTemplate =
            "(function(){" +
            "var target=%d;" +
            // 진단: 쇼핑 영역의 페이지네이션 정보 수집
            "var items=document.querySelectorAll('[data-shp-contents-id]');" +
            "var itemCount=items.length;" +
            // ★ 쇼핑 컴포넌트가 없으면 즉시 종료 (웹검색 탭 페이지네이션 오클릭 방지)
            "if(itemCount===0)return JSON.stringify({type:'not_found',items:0,numBtns:''});" +
            // 방법 1: 쇼핑 영역 근처의 숫자 페이지 버튼 찾기
            // 페이지 버튼은 보통 작은 크기(width<80)이고 숫자만 표시
            "var allEls=document.querySelectorAll('a,button');" +
            "var candidates=[];" +
            "for(var i=0;i<allEls.length;i++){" +
            "  var t=allEls[i].textContent.trim();" +
            "  if(t===String(target)){" +
            // ★ 웹검색 탭 페이지네이션 링크 제외 (where=m_web, sm=mtb_pge 등)
            "    if(allEls[i].tagName==='A'){" +
            "      var h=allEls[i].getAttribute('href')||'';" +
            "      if(h.indexOf('where=')>=0||h.indexOf('sm=mtb_pge')>=0||h.indexOf('page=')>=0)continue;" +
            "    }" +
            "    var r=allEls[i].getBoundingClientRect();" +
            "    if(r.width>0&&r.width<80&&r.height>0&&r.height<80){" +
            "      candidates.push({el:allEls[i],y:r.top});" +
            "    }" +
            "  }" +
            "}" +
            // ★ 후보 중 가장 위쪽(쇼핑 컴포넌트 = 페이지 중간)의 것을 선택
            "if(candidates.length>0){" +
            "  candidates.sort(function(a,b){return a.y-b.y;});" +
            "  var btn=candidates[0].el;" +
            "  btn.scrollIntoView({block:'center',behavior:'smooth'});" +
            "  btn.click();" +
            "  return JSON.stringify({type:'num',page:target,items:itemCount,cands:candidates.length});" +
            "}" +
            // 방법 2: "다음" 또는 "다음 페이지" 버튼
            "for(var i=0;i<allEls.length;i++){" +
            "  var t=allEls[i].textContent.trim();" +
            "  if((t==='다음'||t==='다음 페이지'||t==='다음페이지')&&!allEls[i].disabled){" +
            // ★ 웹검색 "다음" 링크 제외 (href에 where= 또는 page= 포함)
            "    if(allEls[i].tagName==='A'){" +
            "      var h2=allEls[i].getAttribute('href')||'';" +
            "      if(h2.indexOf('where=')>=0||h2.indexOf('sm=mtb_pge')>=0||h2.indexOf('page=')>=0)continue;" +
            "    }" +
            "    var r=allEls[i].getBoundingClientRect();" +
            "    if(r.width>0){" +
            "      allEls[i].scrollIntoView({block:'center',behavior:'smooth'});" +
            "      allEls[i].click();" +
            "      return JSON.stringify({type:'next',page:target,items:itemCount});" +
            "    }" +
            "  }" +
            "}" +
            // 방법 3: role=button + aria-label에 페이지 번호
            "var ariaBtns=document.querySelectorAll('[role=\"button\"],[aria-label*=\"페이지\"]');" +
            "for(var i=0;i<ariaBtns.length;i++){" +
            "  var label=ariaBtns[i].getAttribute('aria-label')||'';" +
            "  var t=ariaBtns[i].textContent.trim();" +
            "  if(t===String(target)||(label&&label.indexOf(target+'페이지')>=0)){" +
            "    ariaBtns[i].scrollIntoView({block:'center',behavior:'smooth'});" +
            "    ariaBtns[i].click();" +
            "    return JSON.stringify({type:'aria',page:target,items:itemCount});" +
            "  }" +
            "}" +
            // 진단: 페이지네이션 후보 덤프
            "var diag=[];" +
            "for(var i=0;i<allEls.length;i++){" +
            "  var t=allEls[i].textContent.trim();" +
            "  if(t.length<=3&&/^[0-9]+$/.test(t)){" +
            "    var r=allEls[i].getBoundingClientRect();" +
            "    diag.push(t+'('+Math.round(r.width)+'x'+Math.round(r.height)+'@'+Math.round(r.top)+')');" +
            "  }" +
            "  if(diag.length>=10)break;" +
            "}" +
            "return JSON.stringify({type:'not_found',items:itemCount,numBtns:diag.join(',')});})()";

        // 페이지 순회
        for (int page = 1; page <= maxPages; page++) {
            if (page > 1) {
                // ★ 쇼핑 컴포넌트 페이지 버튼 클릭 (AJAX 전환)
                String clickPageJS = String.format(clickPageJSTemplate, page);
                String clickResult = evalJSSync(clickPageJS, 5000);
                Logger.i("findMid: 페이지 " + page + " 전환: " + clickResult);

                if (clickResult == null || clickResult.equals("null")) {
                    Logger.i("findMid: 페이지네이션 실패 — 탐색 종료");
                    break;
                }

                try {
                    JSONObject cr = new JSONObject(clickResult);
                    String type = cr.optString("type", "");
                    if ("not_found".equals(type)) {
                        String numBtns = cr.optString("numBtns", "");
                        int pgItems = cr.optInt("items", -1);
                        // ★ 쇼핑 컴포넌트 자체가 없으면 (items=0) 더보기 시도 생략 → 즉시 종료
                        if (pgItems == 0) {
                            Logger.i("findMid: 쇼핑 컴포넌트 없음 (items=0) — 탐색 종료");
                            break;
                        }
                        // ★ Phase 4: 더보기 버튼 폴백 (가격비교 컴포넌트 추가 로드)
                        String moreResult = evalJSSync(
                            "(function(){" +
                            "var kw=['더보기','더 보기','전체보기'];" +
                            "var btns=document.querySelectorAll('button,a,[role=\"button\"]');" +
                            "for(var i=0;i<btns.length;i++){" +
                            "  var t=(btns[i].textContent||'').trim();" +
                            "  for(var k=0;k<kw.length;k++){" +
                            "    if(t.indexOf(kw[k])>=0){" +
                            "      var r=btns[i].getBoundingClientRect();" +
                            "      if(r.width>0&&r.height>0){" +
                            "        btns[i].scrollIntoView({block:'center',behavior:'smooth'});" +
                            "        btns[i].click();" +
                            "        return 'clicked:'+t;" +
                            "      }" +
                            "    }" +
                            "  }" +
                            "}" +
                            "return 'none';" +
                            "})()", 3000);
                        Logger.i("findMid: 더보기 결과: " + moreResult);
                        if (moreResult != null && moreResult.startsWith("clicked:")) {
                            Logger.i("findMid: 더보기 클릭 → 추가 항목 대기 후 재탐색");
                            RandomDelay.sleepBetween(1500, 2000);
                            // break 생략 → scan loop 진행
                        } else {
                            Logger.i("findMid: 더보기 없음 (숫자버튼: " + numBtns + ") — 탐색 종료");
                            break;
                        }
                    }
                    Logger.i("findMid: 페이지 " + page + " 클릭 완료 (방식: " + type + ")");
                } catch (Exception e) {
                    Logger.w("findMid: 페이지 전환 파싱 오류: " + e.getMessage());
                    break;
                }

                // AJAX 콘텐츠 업데이트 대기
                RandomDelay.sleepBetween(1500, 2500);
            }

            // 현재 페이지에서 스크롤하며 MID 탐색
            for (int i = 0; i < maxScroll; i++) {
                if (i > 0) {
                    Logger.i("findMid: scroll " + (i + 1) + "/" + maxScroll + " (p" + page + ")");
                }

                RandomDelay.sleepBetween(300, 500);

                String result = evalJSSync(findAndClickJS, 5000);
                if (result != null && !result.equals("not_found") && !result.equals("null") && !result.isEmpty()) {
                    try {
                        JSONObject pos = new JSONObject(result);
                        int strategy = pos.optInt("s", 0);
                        String href = pos.optString("href", "");
                        Logger.i("findMid: MID 발견+클릭 (전략 " + strategy + ", p" + page + ", href=" +
                                href.substring(0, Math.min(80, href.length())) + ")");

                        // ★ shouldInterceptRequest/shouldOverrideUrlLoading에서 smartstore 감지
                        if (chromeFallbackUsed) {
                            String targetUrl = !interceptedProductUrl.isEmpty()
                                ? interceptedProductUrl
                                : "https://msearch.shopping.naver.com/catalog/" + mid;
                            Logger.i("findMid: smartstore 감지 → Chrome 직접 열기: " + targetUrl.substring(0, Math.min(80, targetUrl.length())));
                            openInChrome(targetUrl);
                            return StepResult.success();
                        }

                        // Bridge 리다이렉트 대기 (shouldInterceptRequest가 smartstore 감지까지)
                        for (int w = 0; w < 8; w++) {
                            if (chromeFallbackUsed) break;
                            RandomDelay.sleep(300);
                        }

                        // ★ 리다이렉트 후 smartstore 감지 확인
                        if (chromeFallbackUsed) {
                            String targetUrl = !interceptedProductUrl.isEmpty()
                                ? interceptedProductUrl
                                : "https://msearch.shopping.naver.com/catalog/" + mid;
                            Logger.i("findMid: 리다이렉트 후 smartstore 감지 → Chrome: " + targetUrl.substring(0, Math.min(80, targetUrl.length())));
                            openInChrome(targetUrl);
                            return StepResult.success();
                        }

                        // 페이지 상태 확인
                        String pageState = detectPageState();
                        Logger.i("findMid: 페이지 상태: " + pageState);

                        if ("product".equals(pageState)) {
                            // ★ Phase 3: DOM 실제 로드 검증 + 실제 URL 획득
                            String afterUrl = evalJSSync("(function(){return window.location.href;})()", 3000);
                            String domCheck = evalJSSync(
                                "(function(){" +
                                "var s='[class*=\"product_title\"],[class*=\"prd_name\"],[class*=\"price_area\"]," +
                                "[class*=\"productInfo\"],[class*=\"product_price\"],[class*=\"prdName\"]," +
                                "[class*=\"prod_name\"],[class*=\"prod_price\"],[id*=\"PRODUCT\"]';" +
                                "return document.querySelector(s)?'ok':'no_dom';" +
                                "})()", 3000);
                            Logger.i("findMid: 상품 페이지 DOM=" + domCheck + " url=" + afterUrl);

                            // ★ 상품 페이지 항상 Chrome으로 열기 (WebView alpha=0 투명 → 사용자에게 보이게)
                            // afterUrl = WebView가 실제 로드한 URL (smartstore/catalog 등)
                            // 없으면 mid 기반 catalog URL로 폴백
                            String chromeUrl = (afterUrl != null && !afterUrl.isEmpty()
                                    && !afterUrl.equals("null")
                                    && !afterUrl.startsWith("about:")
                                    && !afterUrl.contains("search.naver.com"))
                                ? afterUrl
                                : "https://msearch.shopping.naver.com/catalog/" + mid;
                            openInChrome(chromeUrl);
                            chromeFallbackUsed = true;
                            return StepResult.success();
                        }

                        if (isBlockedState(pageState)) {
                            Logger.w("findMid: " + pageState + " 감지 → IP 회전 후 Chrome 폴백");
                            quickRotateIP();
                            openInChrome("https://msearch.shopping.naver.com/product/" + mid);
                            chromeFallbackUsed = true;
                            return StepResult.success();
                        }

                        if ("captcha".equals(pageState)) {
                            Logger.w("findMid: 캡챠 감지 → IP 회전 후 Chrome 폴백");
                            quickRotateIP();
                            openInChrome("https://msearch.shopping.naver.com/product/" + mid);
                            chromeFallbackUsed = true;
                            return StepResult.success();
                        }

                        // unknown/search — Chrome으로 상품페이지 보장
                        String afterUrl = evalJSSync("(function(){return window.location.href;})()", 3000);
                        Logger.i("findMid: 클릭 후 URL: " + afterUrl);
                        // WebView가 에러 페이지이거나 상품 페이지가 아니면 Chrome 폴백
                        if (afterUrl != null && (afterUrl.contains("chrome-error")
                                || afterUrl.contains("about:blank")
                                || afterUrl.contains("search.naver.com"))) {
                            Logger.i("findMid: WebView 상품미로드 → Chrome 직접 폴백");
                            openInChrome("https://msearch.shopping.naver.com/product/" + mid);
                            chromeFallbackUsed = true;
                        }
                        return StepResult.success();

                    } catch (Exception e) {
                        Logger.w("findMid: parse error: " + e.getMessage());
                    }
                }

                // ★ 인간적 스크롤 (가변 속도 + 간헐적 멈춤)
                int scrollPx = RandomDelay.between(300, 700);
                evalJSSync(String.format("window.scrollBy({top:%d,behavior:'smooth'})", scrollPx), 2000);
                RandomDelay.sleepBetween(400, 900);
                // 20% 확률로 짧은 멈춤 (콘텐츠 읽는 시뮬레이션)
                if (Math.random() < 0.2) {
                    RandomDelay.sleepBetween(800, 1500);
                }

                // 스크롤 끝 감지
                if (i > 3) {
                    String heightCheck = evalJSSync(
                        "(function(){var h=document.body.scrollHeight;var y=window.scrollY+window.innerHeight;return (y>=h-50)?'end':'more';})()",
                        2000);
                    if ("end".equals(heightCheck)) {
                        Logger.i("findMid: scroll 끝 (p" + page + ")");
                        break;
                    }
                }
            }
        }

        // ★ 실패 진단: 페이지에 실제로 어떤 nv_mid 링크가 있는지 덤프
        String diagLinks = evalJSSync(
            "(function(){" +
            "var url=window.location.href;" +
            "var links=document.querySelectorAll('a[href]');" +
            "var nv=[];" +
            "for(var i=0;i<links.length;i++){" +
            "  var h=links[i].href||'';" +
            "  if(h.includes('nv_mid')||h.includes('/products/')||h.includes('shopping.naver')){" +
            "    nv.push(h.substring(0,120));" +
            "    if(nv.length>=5)break;" +
            "  }" +
            "}" +
            "return JSON.stringify({url:url,total:links.length,nv:nv});})();",
            5000);
        Logger.w("findMid 스킵: MID not found after " + maxPages + " pages → 다음 작업으로");
        return StepResult.skip("findMid: MID not found");
    }

    // ═══════════════════════════════════════════════════
    // 418/429/529 감지 + Chrome 폴백
    // ═══════════════════════════════════════════════════

    /**
     * 차단 상태인지 판별 (418/429/529/blocked)
     */
    private boolean isBlockedState(String state) {
        return "418".equals(state) || "429".equals(state) || "529".equals(state) || "blocked".equals(state);
    }

    /**
     * 현재 페이지 상태 감지
     * @return "product" | "418" | "429" | "529" | "blocked" | "captcha" | "search" | "unknown"
     */
    private String detectPageState() {
        // ★ HTTP 상태 코드 기반 빠른 감지 (onReceivedHttpError에서 캡처)
        if (lastHttpStatus == 418) {
            Logger.i("detectPageState: HTTP 418 (nfront WAF)");
            return "418";
        }
        if (lastHttpStatus == 529) {
            Logger.i("detectPageState: HTTP 529 (서버 과부하)");
            return "529";
        }
        if (lastHttpStatus == 429) {
            Logger.i("detectPageState: HTTP 429 (Rate Limit)");
            return "429";
        }

        String js = "(function(){" +
                "var title=document.title||'';" +
                "var url=window.location.href;" +
                "var t=document.body?document.body.innerText.substring(0,500):'';" +
                // nfront 에러페이지 (title: "[에러페이지] 에러페이지")
                "if(title.includes('에러페이지')) return 'STATE:429|URL:'+url+'|T:'+title;" +
                // 차단 감지 — 상세 매칭 정보 포함
                "var blocked='';" +
                "if(t.includes('비정상적인 접근'))blocked='비정상적인 접근';" +
                "else if(t.includes('일시적으로 제한'))blocked='일시적으로 제한';" +
                "else if(t.includes('자동화된 접근'))blocked='자동화된 접근';" +
                "else if(t.includes('접근이 제한'))blocked='접근이 제한';" +
                "else if(t.includes('잠시 후 다시'))blocked='잠시 후 다시';" +
                "else if(t.includes('이용이 제한'))blocked='이용이 제한';" +
                "if(blocked)return 'STATE:blocked|URL:'+url+'|MATCH:'+blocked+'|T:'+title;" +
                // 캡챠 감지
                "if(t.includes('자동입력방지')" +
                "||(t.includes('보안 확인')&&(t.includes('영수증')||t.includes('무엇입니까'))))" +
                "return 'STATE:captcha|URL:'+url+'|T:'+title;" +
                // 상품 페이지 감지 (smartstore / brand / msearch product / catalog)
                "if(url.includes('smartstore.naver.com')||url.includes('brand.naver.com')" +
                "||url.includes('/products/')||url.includes('msearch.shopping.naver.com/product/')" +
                "||url.includes('msearch.shopping.naver.com/catalog/')) return 'product';" +
                // 검색 결과 페이지
                "if(url.includes('search.shopping.naver.com')||url.includes('msearch.shopping.naver.com')) return 'search';" +
                "return 'STATE:unknown|URL:'+url+'|T:'+title;})()";

        String state = evalJSSync(js, 5000);
        if (state == null) return "unknown";

        // 디버그: 상세 정보 로그
        if (state.startsWith("STATE:")) {
            Logger.i("detectPageState 상세: " + state.substring(0, Math.min(200, state.length())));
            // 실제 상태값 추출
            if (state.startsWith("STATE:429")) return "429";
            if (state.startsWith("STATE:blocked")) return "blocked";
            if (state.startsWith("STATE:captcha")) return "captcha";
            return "unknown";
        }
        return state;
    }

    /**
     * Chrome으로 URL 열기 (nfront 차단 우회용)
     * Service 컨텍스트에서는 직접 Intent가 Custom Tabs보다 신뢰성 높음
     */
    private void openInChrome(String url) {
        // ★ WebView 오버레이 숨기기 — Chrome이 화면에 보이도록
        if (onChromeOpenCallback != null) {
            mainHandler.post(onChromeOpenCallback);
        }

        try {
            // ★ 직접 Chrome Intent (Service에서 Custom Tabs보다 신뢰성 높음)
            // FLAG_ACTIVITY_CLEAR_TOP: 기존 Chrome 창에 새 URL 로드 보장
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.android.chrome");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            Logger.i("Chrome 열림: " + url.substring(0, Math.min(80, url.length())));
        } catch (Exception e) {
            Logger.w("Chrome 직접 실행 실패 → Custom Tabs 폴백: " + e.getMessage());
            try {
                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .build();
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                customTabsIntent.launchUrl(context, Uri.parse(url));
            } catch (Exception e2) {
                // 최종 폴백: 기본 브라우저
                Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fallback);
            }
        }
    }

    /**
     * Chrome 열기 전 빠른 IP 회전 — nfront IP 평판 차단 우회
     * WRITE_SECURE_SETTINGS로 비행기모드 토글 → 새 LTE IP 취득
     * (svc data는 앱 UID에서 실행 불가 → Settings.Global 방식 사용)
     */
    private void quickRotateIP() {
        try {
            Logger.i("★ Chrome 전 IP 회전 시작 (비행기모드 토글)");
            android.content.ContentResolver cr = context.getContentResolver();

            // 비행기모드 ON
            android.provider.Settings.Global.putInt(cr,
                    android.provider.Settings.Global.AIRPLANE_MODE_ON, 1);
            Intent airOn = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            airOn.putExtra("state", true);
            context.sendBroadcast(airOn);
            Thread.sleep(3000);

            // 비행기모드 OFF
            android.provider.Settings.Global.putInt(cr,
                    android.provider.Settings.Global.AIRPLANE_MODE_ON, 0);
            Intent airOff = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            airOff.putExtra("state", false);
            context.sendBroadcast(airOff);

            // 네트워크 복구 대기
            for (int i = 0; i < 10; i++) {
                Thread.sleep(1000);
                try {
                    java.net.InetAddress addr = java.net.InetAddress.getByName("m.naver.com");
                    if (addr != null) {
                        Logger.i("★ Chrome 전 IP 회전 완료 (" + (i + 1) + "초)");
                        return;
                    }
                } catch (Exception ignored) {}
            }
            Logger.w("★ Chrome 전 IP 회전: 네트워크 복구 타임아웃 (진행)");
        } catch (Exception e) {
            Logger.w("★ Chrome 전 IP 회전 실패: " + e.getMessage());
        }
    }

    /**
     * HOME 버튼 눌러 Chrome Custom Tabs 닫기
     */
    private void pressHome() {
        try {
            Runtime.getRuntime().exec(new String[]{"input", "keyevent", "3"});
            Logger.i("HOME 키 → 앱 복귀");
        } catch (Exception e) {
            Logger.w("HOME 키 실패: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════
    // 내부 유틸
    // ═══════════════════════════════════════════════════

    /**
     * evaluateJavascript 동기 실행 (워커 스레드에서 호출)
     */
    private String evalJSSync(String js, long timeoutMs) {
        CompletableFuture<String> future = new CompletableFuture<>();

        mainHandler.post(() -> {
            if (isWebViewDestroyed()) {
                future.complete(null);
                return;
            }
            try {
                webView.evaluateJavascript(js, value -> {
                    // value는 JSON 인코딩된 문자열 ("\"result\"" 형태)
                    if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1)
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\");
                    }
                    future.complete(value);
                });
            } catch (Exception e) {
                future.complete(null);
            }
        });

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Logger.w("evalJS timeout");
            return null;
        }
    }

    /**
     * ★ 인간적 터치 시뮬레이션 (좌표 지터 + 가변 프레스 + 선행 무브)
     * - 좌표에 ±3px 지터 추가 (완벽한 중앙 클릭 방지)
     * - 프레스 지속시간 50~150ms 랜덤 (기계적 80ms 고정 방지)
     * - 터치 전 마우스 무브 이벤트로 hover 시뮬레이션
     */
    private void simulateTouch(float x, float y) {
        // 좌표 지터 (±3px)
        float jitterX = x + RandomDelay.between(-3, 3);
        float jitterY = y + RandomDelay.between(-3, 3);
        // 프레스 지속시간 (50~150ms)
        int pressDuration = RandomDelay.between(50, 150);

        mainHandler.post(() -> {
            long now = SystemClock.uptimeMillis();

            // 선행 MOVE 이벤트 (hover 시뮬레이션 — 봇 탐지 우회)
            float moveX = jitterX + RandomDelay.between(-20, 20);
            float moveY = jitterY + RandomDelay.between(-20, 20);
            MotionEvent move = MotionEvent.obtain(now - 100, now - 100,
                    MotionEvent.ACTION_MOVE, moveX, moveY, 0);
            webView.dispatchTouchEvent(move);
            move.recycle();

            // DOWN → UP
            MotionEvent down = MotionEvent.obtain(now, now,
                    MotionEvent.ACTION_DOWN, jitterX, jitterY, 0);
            MotionEvent up = MotionEvent.obtain(now, now + pressDuration,
                    MotionEvent.ACTION_UP, jitterX, jitterY, 0);

            webView.dispatchTouchEvent(down);
            webView.dispatchTouchEvent(up);

            down.recycle();
            up.recycle();
        });
        RandomDelay.sleepBetween(100, 300);
    }

    private boolean isWebViewDestroyed() {
        return destroyed || webView == null;
    }

    /**
     * WebView destroy 알림 (TrafficService.onDestroy에서 호출)
     * evalJSSync가 destroyed WebView에 접근하는 것을 방지
     */
    public void markDestroyed() {
        destroyed = true;
    }

    /** Chrome 열릴 때 WebView 숨기기 콜백 등록 (TrafficService에서 호출) */
    public void setOnChromeOpenCallback(Runnable callback) {
        this.onChromeOpenCallback = callback;
    }

    /** 현재 헤더 설정 기반 Stealth JS 반환 (없으면 기본값) */
    private String getActiveStealthJS() {
        if (mobileHeaders != null && mobileHeaders.isValid()) {
            return StealthConfig.buildStealthJS(
                mobileHeaders.getChromeVersion(),
                mobileHeaders.getChromeFullVersion(),
                mobileHeaders.getDeviceModel()
            );
        }
        return StealthConfig.STEALTH_JS;
    }

    /** navigate용 추가 HTTP 헤더 빌드 */
    private java.util.Map<String, String> buildNavHeaders(String url) {
        java.util.Map<String, String> h = new java.util.LinkedHashMap<>();
        h.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
        h.put("Accept-Language", mobileHeaders != null ? mobileHeaders.getAcceptLanguage()
                : "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
        h.put("Sec-Fetch-Mode", "navigate");
        h.put("Sec-Fetch-Dest", "document");
        h.put("Upgrade-Insecure-Requests", "1");
        // Sec-Fetch-Site: 첫 진입은 none (직접 입력), 랜딩→네이버는 cross-site
        if (url.contains("naver.com")) {
            h.put("Sec-Fetch-Site", "cross-site");
            h.put("Sec-Fetch-User", "?1");
        } else {
            h.put("Sec-Fetch-Site", "none");
            h.put("Sec-Fetch-User", "?1");
        }
        return h;
    }

    private String jsQuote(String s) {
        return JSONObject.quote(s == null ? "" : s);
    }
}
