package com.sec.android.app.sbrowser.pattern.js;

import android.graphics.RectF;
import android.util.Log;
import android.util.Size;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HtmlJsInterface {

    private static final String TAG = HtmlJsInterface.class.getSimpleName();

    private String _html = null;
    private String _localStorageString = null;
    private String _localStorageValue = null;
    private JSONObject _jsonObject = null;
    private Size _windowSize = null;
    private String _innerText = null;
    private String _currentText = null;
    private InsideData _insideData = null;
    private int _nodeCount = 0;
    private String _randomValue = null;
    private String _value = null;
    private Map<String, String> _ajaxRequestContents = new HashMap<>();

    protected JsApi _jsApi = null;

    public HtmlJsInterface(JsApi jsApi) {
        _jsApi = jsApi;
    }

    public void reset() {
        _html = null;
        _localStorageString = null;
        _localStorageValue = null;
        _jsonObject = null;
        _windowSize = null;
        _innerText = null;
        _currentText = null;
        _insideData = null;
        _nodeCount = 0;
        _randomValue = null;
        _value = null;
        _ajaxRequestContents = new HashMap<>();
    }

    public String getHtml() {
        return _html;
    }

    public void resetHtml() {
        _html = null;
    }

    public String getLocalStorageString() {
        return _localStorageString;
    }

    public void resetLocalStorageString() {
        _localStorageString = null;
    }

    public String getLocalStorageValue() {
        return _localStorageValue;
    }

    public void resetLocalStorageValue() {
        _localStorageValue = null;
    }

    public JSONObject getJsonObject() {
        return _jsonObject;
    }

    public void resetJsonObject() {
        _jsonObject = null;
    }

    public Size getWindowSize() {
        return _windowSize;
    }

    public String getInnerText() {
        return _innerText;
    }

    public void resetInnerText() {
        _innerText = null;
    }

    public String getCurrentText() {
        return _currentText;
    }

    public void resetCurrentText() {
        _currentText = null;
    }

    public InsideData getInsideData() {
        return _insideData;
    }

    public void resetInsideData() {
        _insideData = null;
    }

    public int getNodeCount() {
        return _nodeCount;
    }

    public void resetRandomValue() {
        _randomValue = null;
    }

    public String getRandomValue() {
        return _randomValue;
    }

    public void resetValue() {
        _value = null;
    }

    public String getValue() {
        return _value;
    }

    public Map<String, String> getAjaxRequestContents() {
        return _ajaxRequestContents;
    }


    @JavascriptInterface
    public void printHtml(String html) {
        System.out.println(html);
        _jsApi.callbackOnSuccess(null);
    }

    @JavascriptInterface
    public void readHtml(String html) {
        Log.d(TAG, "readHtml: ");
        _html = html;
        _jsApi.callbackOnSuccess(_html);
    }

    @JavascriptInterface
    public void readLocalStorageString(String value) {
        Log.d(TAG, "readLocalStorageString: " + value);
        _localStorageString = value;
        _jsApi.callbackOnSuccess(_localStorageString);
    }

    @JavascriptInterface
    public void readLocalStorageValue(String value) {
        Log.d(TAG, "readLocalStorageValue: " + value);
        _localStorageValue = value;
        _jsApi.callbackOnSuccess(_localStorageValue);
    }

    @JavascriptInterface
    public void readJson(String value) {
//        Log.d(TAG, "readJson: " + value);
        try {
            _jsonObject = new JSONObject(value);
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
        _jsApi.callbackOnSuccess(value);
    }

    @JavascriptInterface
    public void undefinedNode(String selectors) {
        _jsApi.callbackOnFailed(JsApi.ERR_UNDEFINED, "undefinedNode: " + selectors);
    }

    @JavascriptInterface
    public void readWindowSize(int width, int height) {
        Log.d(TAG, "window width: " + width + ", height: " + height);
        _windowSize = new Size(width, height);
        _jsApi.callbackOnSuccess(_windowSize);
    }

    @JavascriptInterface
    public void readInnerText(String text) {
        Log.d(TAG, "readInnerText: " + text);
        _innerText = text;
        _jsApi.callbackOnSuccess(_innerText);
    }

    @JavascriptInterface
    public void readCurrentTextOnly(String text) {
        Log.d(TAG, "readCurrentTextOnly: " + text);
        _currentText = text;
        _jsApi.callbackOnSuccess(_currentText);
    }

    @JavascriptInterface
    public void checkInside(int inside, float left, float top, float right, float bottom) {
        Log.d(TAG, "target inside:" + inside + ","
                + " RectF(" + left + ", " + top + ", " + right + ", " + bottom + ")");
        _insideData = new InsideData();
        _insideData.inside = inside;
        _insideData.rect = new RectF(left, top, right, bottom);
        _jsApi.callbackOnSuccess(_insideData);
    }

    @JavascriptInterface
    public void readNodeCount(int count) {
        Log.d(TAG, "nodeCount: " + count);
        _nodeCount = count;
        _jsApi.callbackOnSuccess(count);
    }

    @JavascriptInterface
    public void readRandomValue(String value) {
        Log.d(TAG, "readRandomValue: " + value);
        _randomValue = value;
        _jsApi.callbackOnSuccess(value);
    }

    @JavascriptInterface
    public void readValue(String value) {
//        Log.d(TAG, "readValue: " + value);
        _value = value;
        _jsApi.callbackOnSuccess(value);
    }

    @JavascriptInterface
    public void writeValue() {
        _jsApi.callbackOnSuccess(null);
    }

    @JavascriptInterface
    public void clickUrl() {
        _jsApi.callbackOnSuccess(null);
    }

    @JavascriptInterface
    public void scrollToBottom() {
        _jsApi.callbackOnSuccess(null);
    }

    @JavascriptInterface
    public void fixedAj(final String id, final String body) {
        Log.d(TAG, "fixedAj[" + id + "]: " + body);
        _ajaxRequestContents.put(id, body);
        _jsApi.callbackOnSuccess(body);
    }
}
