package com.sec.android.app.sbrowser.pattern;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;

public class SwipePattern implements WorkPattern {

    private static final String TAG = SwipePattern.class.getSimpleName();

    private static final long preDelayMillis = 2000;
    private final TouchInjector _touchInjector;

    public WebView _webView = null;

    public SwipePattern(WebView webView) {
        _webView = webView;
        // 키보드 셋팅.
        _touchInjector = new TouchInjector(webView.getContext());
        _touchInjector.setSoftKeyboard(new SamsungKeyboard());

        _webView.addJavascriptInterface(new GetHtmlJavascriptInterface(), "FindView");

    }

    public void endPattern() {
        _webView.removeJavascriptInterface("FindView");
    }

    @Override
    public void run() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        workInThread();
                    }
                }).start();
            }
        }, preDelayMillis);
    }

    // 쓰레드 작업.
    private void workInThread() {
        Log.d(TAG, "아래로 스크롤");

        // 작업뷰를 찾는다.

        swipeDownInThread();

        checkSS();

        SystemClock.sleep(1500);

        checkSS();


//        // 1. 키워드 추출작업.
//        extractStrings(data.search);
//
//        // 4 ~ 7 초대기.
//        SystemClock.sleep(MathHelper.randomRange(4000, 7000));
//
//        Log.d(TAG, "키워드 입력시작");
//        // 2. 키워드 입력.
//        inputKeywordInThread();

        SystemClock.sleep(1500);

        swipeDownInThread();


        // 4 ~ 7 초대기.
        SystemClock.sleep(MathHelper.randomRange(4000, 7000));

        Log.d(TAG, "뷰 버튼 클릭");

//        touchSearchButtonInThread();
    }

    // 외부 쓰레드 내부에서 되어야 한다.
    private void swipeDownInThread() {
        _touchInjector.swipeScreen(true, 100);
    }

    private void checkSS() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.post(new Runnable() {
            @Override
            public void run() {
//                view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);"); //<html></html> 사이에 있는 모든 html을 넘겨준다.
                _webView.loadUrl("javascript:window.FindView.getPosition(document.querySelector('._list').querySelector('.api_more').getBoundingClientRect().top);");
                _webView.loadUrl("javascript:window.FindView.getPosition(document.querySelector('._list').querySelector('.api_more').getBoundingClientRect().top);");

//                _webView.loadUrl("javascript:window.FindView.getPosition(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().y);");
//                _webView.loadUrl("javascript:window.FindView.getPosition(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().top);");
//                _webView.loadUrl("javascript:window.FindView.getPosition(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().height);");
//                _webView.loadUrl("javascript:window.FindView.getHtml(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().y);");
//                _webView.loadUrl("javascript:window.FindView.getHtml(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().top);");
//                _webView.loadUrl("javascript:window.FindView.getHtml(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().height);");
//                _webView.loadUrl("javascript:window.FindView.getHtml(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].innerHTML);");
//                _webView.loadUrl("javascript:window.FindView.getHtml(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect());");
//                _webView.loadUrl("javascript:window.FindView.getHtml(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().toJSON());");
            }
        });
    }


    public class GetHtmlJavascriptInterface {
        @JavascriptInterface
        public void getPosition(float position) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
            System.out.println(position);
        }
        @JavascriptInterface
        public void getHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
            System.out.println(html);
        }
    }

}
