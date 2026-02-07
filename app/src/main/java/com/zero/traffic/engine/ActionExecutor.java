package com.zero.traffic.engine;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.webkit.ValueCallback;
import android.webkit.WebView;

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
 */
public class ActionExecutor {
    private final WebView webView;
    private final Handler mainHandler;

    public ActionExecutor(WebView webView) {
        this.webView = webView;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // ── navigate ────────────────────────────────────────

    public StepResult navigate(Step step) {
        String url = step.getString("url");
        if (url.isEmpty()) return StepResult.fail("navigate: url empty");

        Logger.step(step.getId(), "navigate", url);
        CompletableFuture<Void> future = new CompletableFuture<>();

        mainHandler.post(() -> {
            webView.loadUrl(url);
            // WebViewClient.onPageFinished에서 future.complete() 호출 필요
            // 여기서는 간단히 타이머로 대기
            mainHandler.postDelayed(() -> future.complete(null), 1000);
        });

        try {
            long timeout = step.getLong("timeout", 30000);
            future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return StepResult.fail("navigate timeout: " + url);
        }

        // domcontentloaded 대기
        RandomDelay.sleepBetween(1000, 2000);
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
                long timeout = step.getLong("timeout", 20000);
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
        sb.append("var el=document.querySelector('").append(escapeJS(selector)).append("');");
        if (!fallback.isEmpty()) {
            sb.append("if(!el) el=document.querySelector('").append(escapeJS(fallback)).append("');");
        }
        if (!fallbackText.isEmpty()) {
            sb.append("if(!el){var links=document.querySelectorAll('a');");
            sb.append("for(var i=0;i<links.length;i++){");
            sb.append("if(links[i].textContent.trim().startsWith('").append(escapeJS(fallbackText)).append("')){el=links[i];break;}}}");
        }
        sb.append("if(!el) return 'null';");
        if (removeTarget) {
            sb.append("el.removeAttribute('target');");
        }
        sb.append("var r=el.getBoundingClientRect();");
        sb.append("return JSON.stringify({x:r.x+r.width/2,y:r.y+r.height/2});");
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
        String findJS = String.format(
                "(function(){var el=document.querySelector('%s');" +
                "if(!el && '%s') el=document.querySelector('%s');" +
                "if(!el) return 'not_found';" +
                "el.click();el.focus();return 'found';})()",
                escapeJS(selector), escapeJS(fallback), escapeJS(fallback));

        String found = evalJSSync(findJS, 5000);
        if (!"found".equals(found)) {
            return StepResult.fail("humanType: element not found: " + selector);
        }

        RandomDelay.sleepBetween(200, 400);

        // clearFirst
        if (clearFirst) {
            evalJSSync(String.format(
                    "(function(){var el=document.querySelector('%s');if(el){el.value='';el.dispatchEvent(new Event('input',{bubbles:true}));};})()",
                    escapeJS(selector)), 3000);
            RandomDelay.sleepBetween(100, 200);
        }

        // 한 글자씩 타이핑 (dispatchEvent로 실제 키 이벤트)
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String typeJS = String.format(
                    "(function(){var el=document.querySelector('%s');" +
                    "if(el){el.value+='%s';" +
                    "el.dispatchEvent(new Event('input',{bubbles:true}));" +
                    "el.dispatchEvent(new KeyboardEvent('keydown',{key:'%s',bubbles:true}));" +
                    "el.dispatchEvent(new KeyboardEvent('keyup',{key:'%s',bubbles:true}));};})()",
                    escapeJS(selector), escapeJSChar(c), escapeJSChar(c), escapeJSChar(c));

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

        String js = String.format(
                "document.dispatchEvent(new KeyboardEvent('keydown',{key:'%s',code:'%s',bubbles:true}));" +
                "document.dispatchEvent(new KeyboardEvent('keyup',{key:'%s',code:'%s',bubbles:true}));",
                escapeJS(key), escapeJS(key), escapeJS(key), escapeJS(key));

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

        String js = "(function(){" +
                "var t=document.body?document.body.innerText:'';" +
                "if(t.includes('비정상적인 접근')||t.includes('일시적으로 제한')||(t.includes('접근이 제한')&&t.includes('잠시 후'))) return 'blocked';" +
                "if(t.includes('보안 확인')||t.includes('자동입력방지')||t.includes('영수증')) return 'captcha';" +
                "return 'ok';})()";

        String status = evalJSSync(js, 5000);

        if ("blocked".equals(status)) {
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
        boolean useTouch = step.getBoolean("useTouchscreen", true);

        Logger.step(step.getId(), "clickProduct", "#" + index + " mid=" + mid);

        // N번째 상품 찾기
        String findJS = String.format(
                "(function(){var list=[];var seq=0;" +
                "document.querySelectorAll('a[data-shp-contents-id]').forEach(function(a){" +
                "if(/%s/.test(a.getAttribute('data-shp-inventory')||''))return;" +
                "seq++;list.push({mid:a.getAttribute('data-shp-contents-id')||'',i:seq});});" +
                "var t=list.find(function(p){return p.i===%d;});" +
                "if(!t)return 'null';" +
                "var el=document.querySelector('a[data-shp-contents-id=\"'+t.mid+'\"]');" +
                "if(!el)return 'null';" +
                "el.scrollIntoView({block:'center',behavior:'smooth'});" +
                "var r=el.getBoundingClientRect();" +
                "return JSON.stringify({x:r.x+r.width/2,y:r.y+r.height/2,mid:t.mid});})()",
                escapeJS(exclude), index);

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

            RandomDelay.sleepBetween(300, 700);
            simulateTouch(x, y);

            // 네비게이션 대기
            RandomDelay.sleepBetween(2000, 3500);
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

    // ═══════════════════════════════════════════════════
    // 내부 유틸
    // ═══════════════════════════════════════════════════

    /**
     * evaluateJavascript 동기 실행 (워커 스레드에서 호출)
     */
    private String evalJSSync(String js, long timeoutMs) {
        CompletableFuture<String> future = new CompletableFuture<>();

        mainHandler.post(() -> webView.evaluateJavascript(js, value -> {
            // value는 JSON 인코딩된 문자열 ("\"result\"" 형태)
            if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1)
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
            }
            future.complete(value);
        }));

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Logger.w("evalJS timeout");
            return null;
        }
    }

    /**
     * MotionEvent 터치 시뮬레이션 (메인 스레드에서 실행)
     */
    private void simulateTouch(float x, float y) {
        mainHandler.post(() -> {
            long now = SystemClock.uptimeMillis();

            // WebView 스케일 보정
            float scale = webView.getScale();
            float screenX = x * scale;
            float screenY = y * scale;

            MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, screenX, screenY, 0);
            MotionEvent up = MotionEvent.obtain(now, now + 80, MotionEvent.ACTION_UP, screenX, screenY, 0);

            webView.dispatchTouchEvent(down);
            webView.dispatchTouchEvent(up);

            down.recycle();
            up.recycle();
        });
        RandomDelay.sleepBetween(100, 200);
    }

    private String escapeJS(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n").replace("\r", "");
    }

    private String escapeJSChar(char c) {
        if (c == '\'') return "\\'";
        if (c == '\\') return "\\\\";
        return String.valueOf(c);
    }
}
