package com.sec.android.app.sbrowser.pattern;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.ThreadMutex;
import com.sec.android.app.sbrowser.engine.Utility;
import com.sec.android.app.sbrowser.keyboard.MonkeyScript;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;
import com.sec.android.app.sbrowser.pattern.js.HtmlJsInterface;
import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.js.JsApi;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;
import com.sec.android.app.sbrowser.system.CoordinateHelper;

import org.json.JSONObject;

import java.util.Locale;

public class BasePatternAction {

    private static final String TAG = BasePatternAction.class.getSimpleName();

    private final ThreadMutex _mutex = new ThreadMutex();
    private final WebView _webView;
    private final MonkeyScript _monkeyScript;
    protected final TouchInjector _touchInjector;

    protected JsApi _jsApi = null;
    protected JsQuery _jsQuery = null;
    protected HtmlJsInterface _jsInterface = null;
    protected boolean _contentRight = false;

    public static String getRandomName(String postfix) {
        return Utility.getRandomStringStartAlpha(5,13, postfix);
    }

    public BasePatternAction(String jsInterfaceName, WebView webView) {
        _webView = webView;

        _monkeyScript = new MonkeyScript(webView.getContext());
        // 키보드 셋팅.
        _touchInjector = new TouchInjector(webView.getContext());
        _touchInjector.setSoftKeyboard(new SamsungKeyboard());

        _jsApi = new JsApi(jsInterfaceName, webView);
        _jsQuery = new JsQuery(jsInterfaceName);
        _jsInterface = new HtmlJsInterface(_jsApi);

        _jsApi.setCallback(new Callback());
    }

    public JsApi getJsApi() {
        return _jsApi;
    }

    public void endPattern() {
        _jsApi.unregister();
    }

    public void setContentRight(boolean contentRight) {
        _contentRight = contentRight;
    }

    public void copyToClipboard(Context context, String keyword) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", keyword);
        clipboard.setPrimaryClip(clip);
    }

    // N(24) 이상 전용.
    public void pasteClipboard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            _touchInjector.sendPasteClipboard();
        }
    }

    public void touchScreen(int x, int y) {
        _monkeyScript.runTouch(x, y);
    }

    public void touchScreenParsed(int x, int y) {
        _monkeyScript.runTouchParsed(x, y);
    }

    public void touchScreenLong(int x, int y) {
        _monkeyScript.runTouchLong(x, y);
    }

    public void touchScreenLong(Rect rc) {
        touchScreenLong(rc, 10);
    }

    public void touchScreenLong(Rect rc, int inset) {
        int x = (int) MathHelper.randomRange(rc.left + inset, rc.right - inset);
        int y = (int) MathHelper.randomRange(rc.top + inset, rc.bottom - inset);

        touchScreenLong(x, y);
    }

    public void touchScreen(Rect rc) {
        touchScreen(rc, 10);
    }

    public void touchScreen(Rect rc, int inset) {
        int x = (int) MathHelper.randomRange(rc.left + inset, rc.right - inset);
        int y = (int) MathHelper.randomRange(rc.top + inset, rc.bottom - inset);

        touchScreenParsed(x, y);
    }

    public void touchScreenAdb(Rect rc) {
        touchScreenAdb(rc, 10);
    }

    public void touchScreenAdb(Rect rc, int inset) {
        int x = (int) MathHelper.randomRange(rc.left + inset, rc.right - inset);
        int y = (int) MathHelper.randomRange(rc.top + inset, rc.bottom - inset);

        _touchInjector.touchScreen(_touchInjector.getParsedX(x), _touchInjector.getParsedX(y));
    }

    public void setInputValue(String selectors, String val) {
        String content = "nodeList[0].value = '" + val + "'";
        String query = _jsQuery.wrapJsFunction(_jsQuery.getValidateNodeQuery(selectors, content));
        _jsApi.postQuery(query);
    }

    public String getHtml() {
        Log.d(TAG, "- getHtml");
        _jsApi.postQuery(_jsQuery.getHtml());
        threadWait();

        return _jsInterface.getHtml();
    }

    public void printHtml() {
        _jsApi.postQuery(_jsQuery.printHtml());
        threadWait();
    }

    public void printLocalStorage() {
        Log.d(TAG, "- printLocalStorage");
        _jsInterface.resetLocalStorageString();
        _jsApi.postQuery(_jsQuery.getLocalStorageStringQuery());
//        _jsInterface.resetLocalStorageValue();
//        _jsApi.postQuery(_jsQuery.getLocalStorageValueQuery("ls"));
        threadWait();
    }

    public String getValueFromScript(String script) {
        Log.d(TAG, "- 스크립트로 가져오기: " + script);
        _jsInterface.resetValue();
        _jsApi.postQuery(_jsQuery.getValueFromScript(script));
        threadWait();

        return _jsInterface.getValue();
    }

    public String getSystemValue(String valuePath) {
//        Log.d(TAG, "- 시스템 값 가져오기: " + valuePath);
        _jsInterface.resetValue();
        _jsApi.postQuery(_jsQuery.getSystemValue(valuePath));
        threadWait();

        return _jsInterface.getValue();
    }

    public JSONObject getJsonSystemValue(String valuePath) {
//        Log.d(TAG, "- 시스템 값 json 으로 가져오기: " + valuePath);
        _jsInterface.resetJsonObject();
        _jsApi.postQuery(_jsQuery.getJsonSystemValue(valuePath));
        threadWait();

        return _jsInterface.getJsonObject();
    }

    public String getAjaxRequestBodyByID(String requestID) {
        String body = _jsInterface.getAjaxRequestContents().get(requestID);
        _jsInterface.getAjaxRequestContents().remove(requestID);
        return body;
    }

    public boolean getWebViewWindowSize() {
        return getWebViewWindowSize(false);
    }

    public boolean getWebViewWindowSize(boolean refresh) {
        if (!refresh && _jsInterface.getWindowSize() != null) {
            return true;
        }

        Log.d(TAG, "- 웹뷰 크기 얻기");
        _jsApi.postQuery(_jsQuery.getWindowSizeQuery());
        threadWait();

        return (_jsInterface.getWindowSize() != null);
    }

    protected String getInnerText(String selectors) {
        Log.d(TAG, "- getInnerText: " + selectors);
        _jsInterface.resetInnerText();
        _jsApi.postQuery(_jsQuery.getInnerTextQuery(selectors));
        threadWait();

        return _jsInterface.getInnerText();
    }

    protected String getInnerTextFromParent(String childSelectors, String selectors) {
        Log.d(TAG, "- getInnerTextFromParent: " + selectors);
        _jsInterface.resetInnerText();
        _jsApi.postQuery(_jsQuery.getInnerTextFromParentQuery(childSelectors, selectors));
        threadWait();

        return _jsInterface.getInnerText();
    }

    protected String getCurrentTextOnly(String selectors) {
        Log.d(TAG, "- getCurrentTextOnly: " + selectors);
        _jsInterface.resetCurrentText();
        _jsApi.postQuery(_jsQuery.getCurrentTextOnlyQuery(selectors));
        threadWait();

        return _jsInterface.getCurrentText();
    }

    protected String getCurrentTextOnlyFromParent(String childSelectors, String selectors) {
        Log.d(TAG, "- getCurrentTextOnlyFromParent: " + selectors);
        _jsInterface.resetCurrentText();
        _jsApi.postQuery(_jsQuery.getCurrentTextOnlyFromParentQuery(childSelectors, selectors));
        threadWait();

        return _jsInterface.getCurrentText();
    }

    protected boolean getCheckInside(String selectors) {
        Log.d(TAG, "- 화면 안에 보이는지 검사: " + selectors);
        _jsInterface.resetInsideData();
        _jsApi.postQuery(_jsQuery.getCheckInsideQuery(selectors));
        threadWait();

        return (_jsInterface.getInsideData() != null);
    }

    protected boolean getCheckInsideTopOffset(String selectors, int topOffset) {
        Log.d(TAG, "- 화면 안에 보이는지 검사: " + selectors);
        _jsInterface.resetInsideData();
        _jsApi.postQuery(_jsQuery.getCheckInsideQuery(selectors, topOffset, 0));
        threadWait();

        return (_jsInterface.getInsideData() != null);
    }

    protected boolean getCheckInsideOffset(String selectors, int topOffset, int bottomOffset) {
        Log.d(TAG, "- 화면 안에 보이는지 검사: " + selectors);
        _jsInterface.resetInsideData();
        _jsApi.postQuery(_jsQuery.getCheckInsideQuery(selectors, topOffset, bottomOffset));
        threadWait();

        return (_jsInterface.getInsideData() != null);
    }

    protected boolean getCheckInsideShop(String selectors) {
        Log.d(TAG, "- 샵 화면 안에 보이는지 검사: " + selectors);
        _jsInterface.resetInsideData();
        _jsApi.postQuery(_jsQuery.getCheckInsideQuery(selectors, 100, 0));
        threadWait();

        return (_jsInterface.getInsideData() != null);
    }

    protected InsideData getInsideDataShop(String selectors) {
        Log.d(TAG, "- 샵 화면 안에 보이는지 검사 후 정보 얻기: " + selectors);
        _jsInterface.resetInsideData();
        _jsApi.postQuery(_jsQuery.getCheckInsideQuery(selectors, 100, 0));
        threadWait();

        return _jsInterface.getInsideData();
    }

    protected InsideData getInsideData(String selectors) {
        Log.d(TAG, "- 화면 안에 보이는지 검사 후 정보 얻기: " + selectors);
        _jsInterface.resetInsideData();
        _jsApi.postQuery(_jsQuery.getCheckInsideQuery(selectors));
        threadWait();

        return _jsInterface.getInsideData();
    }

    protected InsideData getInsideDataTopOffset(String selectors, int topOffset) {
        Log.d(TAG, "- 화면 안에 보이는지 검사 후 정보 얻기: " + selectors);
        _jsInterface.resetInsideData();
        _jsApi.postQuery(_jsQuery.getCheckInsideQuery(selectors, topOffset, 0));
        threadWait();

        return _jsInterface.getInsideData();
    }

    protected InsideData getInsideDataOffset(String selectors, int topOffset, int bottomOffset) {
        Log.d(TAG, "- 화면 안에 보이는지 검사 후 정보 얻기: " + selectors);
        _jsInterface.resetInsideData();
        _jsApi.postQuery(_jsQuery.getCheckInsideQuery(selectors, topOffset, bottomOffset));
        threadWait();

        return _jsInterface.getInsideData();
    }

    protected InsideData getInsideDataOffset(String selectors, int topOffset, int bottomOffset, int index) {
        Log.d(TAG, "- 화면 안에 보이는지 검사 후 정보 얻기: " + selectors);
        _jsInterface.resetInsideData();
        _jsApi.postQuery(_jsQuery.getCheckInsideQuery(selectors, topOffset, bottomOffset, index));
        threadWait();

        return _jsInterface.getInsideData();
    }

    protected InsideData getInsideData(String selectors, int nodeIndex) {
        Log.d(TAG, "- 화면 안에 보이는지 검사 후 정보 얻기: " + selectors);
        _jsInterface.resetInsideData();
        _jsApi.postQuery(_jsQuery.getCheckInsideQuery(selectors, 0, 0, nodeIndex));
        threadWait();

        return _jsInterface.getInsideData();
    }

    protected int getNodeCount(String selectors) {
        Log.d(TAG, "- 노드 개수 검사: " + selectors);
        _jsApi.postQuery(_jsQuery.getNodeCount(selectors));
        threadWait();

        return _jsInterface.getNodeCount();
    }

    protected String getRandomValue(String selectors, String valueAttribute) {
        Log.d(TAG, "- 랜던 값 가져오기: " + selectors + " / " + valueAttribute);
        _jsInterface.resetRandomValue();
        _jsApi.postQuery(_jsQuery.getRandomValue(selectors, valueAttribute));
        threadWait();

        return _jsInterface.getRandomValue();
    }

    protected String getValue(String selector, String valueAttribute) {
        Log.d(TAG, "- 값 가져오기: " + selector + " / " + valueAttribute);
        _jsInterface.resetValue();
        _jsApi.postQuery(_jsQuery.getValue(selector, valueAttribute));
        threadWait();

        return _jsInterface.getValue();
    }

    protected void setValue(String selector, String valueAttribute, String value) {
        Log.d(TAG, "- 값 변경하기: " + selector + " / " + valueAttribute + ": " + value);
        _jsApi.postQuery(_jsQuery.setValue(selector, valueAttribute, value));
        threadWait();
    }

    protected boolean touchTarget() {
        return touchTarget(15, 15);
    }

    protected boolean touchTarget(int offset) {
        return touchTarget(offset, offset);
    }

    protected boolean touchTarget(int offsetX, int offsetY) {
        return touchTarget(offsetX, offsetY, offsetY, offsetX);
    }

    protected boolean touchTarget(int offsetLeft, int offsetTop, int offsetBottom, int offsetRight) {
        if ((_jsInterface.getWindowSize() == null) ||
                (_jsInterface.getInsideData() == null)) {
            return false;
        }

        if (!_jsInterface.getInsideData().isInside()) {
            return false;
        } else {
            int screenOffsetLeft = offsetLeft / 6;
            int screenOffsetTop = offsetTop / 6;
            int screenOffsetBottom = offsetBottom / 6;
            int screenOffsetRight = offsetRight / 6;

            int targetX = (int) MathHelper.randomRange((int) _jsInterface.getInsideData().rect.left + screenOffsetLeft, (int) _jsInterface.getInsideData().rect.right - screenOffsetRight);
            int targetY = (int) MathHelper.randomRange((int) _jsInterface.getInsideData().rect.top + screenOffsetTop, (int) _jsInterface.getInsideData().rect.bottom - screenOffsetBottom);

            if (_contentRight) {
                targetX = (int) _jsInterface.getInsideData().rect.right - screenOffsetRight;
            }

            int screenX = (int) CoordinateHelper.viewToScreenX(_webView, _jsInterface.getWindowSize().getWidth(), targetX);
            int screenY = (int) CoordinateHelper.viewToScreenY(_webView, _jsInterface.getWindowSize().getHeight(), targetY);

            Log.d(TAG, String.format(Locale.getDefault(), "btn: %s => %d, %d => %d, %d", _jsInterface.getInsideData().rect.toString(), targetX, targetY, screenX, screenY));

            touchScreen(screenX, screenY);
//            _touchInjector.touchScreenLong(screenX, screenY);
            // 오른쪽기준이면 빼주기 위해 -1을 곱한다.
//            _touchInjector.touchScreenLong(screenX + (offset * (_contentRight ? -1 : 1)), screenY + offset);

            return true;
        }
    }

    protected void touchWebLong(int topMargin) {
        touchWebLong(0, 0, 0, 0, topMargin);
    }

    protected void touchWebLong(int offset, int topMargin) {
        touchWebLong(offset, offset, offset, offset, topMargin);
    }

    protected void touchWebLong(int offsetLeft, int offsetTop, int offsetBottom, int offsetRight, int topMargin) {
        int screenOffsetLeft = offsetLeft / 6;
        int screenOffsetTop = offsetTop / 6;
        int screenOffsetBottom = offsetBottom / 6;
        int screenOffsetRight = offsetRight / 6;

        int targetX = (int) MathHelper.randomRange((int) _jsInterface.getInsideData().rect.left + screenOffsetLeft, (int) _jsInterface.getInsideData().rect.right - screenOffsetRight);
        int targetY = (int) MathHelper.randomRange((int) _jsInterface.getInsideData().rect.top + screenOffsetTop, (int) _jsInterface.getInsideData().rect.bottom - screenOffsetBottom);

        int screenX = (int) CoordinateHelper.viewToScreenX(_webView, _jsInterface.getWindowSize().getWidth(), targetX);
        int screenY = (int) CoordinateHelper.viewToScreenY(_webView, _jsInterface.getWindowSize().getHeight(), targetY) + topMargin;

        Log.d(TAG, String.format(Locale.getDefault(), "rect: %s => %d, %d", _jsInterface.getInsideData().rect.toString(), targetX, targetY));

        touchScreenLong(screenX, screenY);
    }


    protected boolean touchTarget2(int offset) {
        if ((_jsInterface.getWindowSize() == null) ||
                (_jsInterface.getInsideData() == null)) {
            return false;
        }

        if (!_jsInterface.getInsideData().isInside()) {
            return false;
        } else {
            int screenOffset = offset / 6;

            int targetX = (int) MathHelper.randomRange((int) _jsInterface.getInsideData().rect.left + screenOffset, (int) _jsInterface.getInsideData().rect.right - screenOffset);
            int targetY = (int) MathHelper.randomRange((int) _jsInterface.getInsideData().rect.top + screenOffset, (int) _jsInterface.getInsideData().rect.bottom - screenOffset);

            if (_contentRight) {
                targetX = (int) _jsInterface.getInsideData().rect.right - screenOffset;
            }

            int screenX = (int) CoordinateHelper.viewToScreenX(_webView, _jsInterface.getWindowSize().getWidth(), targetX);
            int screenY = (int) CoordinateHelper.viewToScreenY(_webView, _jsInterface.getWindowSize().getHeight(), targetY);

            Log.d(TAG, String.format(Locale.getDefault(), "btn: %s => %d, %d", _jsInterface.getInsideData().rect.toString(), targetX, targetY));

            _touchInjector.touchScreen2(screenX, screenY);
//            _touchInjector.touchScreenLong(screenX, screenY);
            // 오른쪽기준이면 빼주기 위해 -1을 곱한다.
//            _touchInjector.touchScreenLong(screenX + (offset * (_contentRight ? -1 : 1)), screenY + offset);

            return true;
        }
    }

    protected void threadWait() {
//        synchronized (_mutex) {
//            try {
//                _mutex.wait(30000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        _mutex.threadWait();
    }

    protected void threadWakeUp() {
//        synchronized (_mutex) {
//            _mutex.notify();
//        }
        _mutex.threadWakeUp();
    }


    public class Callback implements JsApi.Callback {
        @Override
        public <T> void onSuccess(T data) {
            threadWakeUp();
        }

        @Override
        public void onFailed(int code, String message) {
            threadWakeUp();
        }
    }
}
