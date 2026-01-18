package com.sec.android.app.sbrowser.pattern.naver.place_save;

import static com.sec.android.app.sbrowser.pattern.BasePatternAction.getRandomName;

import android.os.SystemClock;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverBasePatternAction;
import com.sec.android.app.sbrowser.system.CoordinateHelper;

public class NaverPlaceSaveTouchUrlPatternAction extends NaverBasePatternAction {

    private static final String TAG = NaverPlaceSaveTouchUrlPatternAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zpstu";

    private WebView _webView = null;
    private final Object _mutex = new Object();
    private int _findCount = 0;
    private int _findCount2 = 0;
    private float _viewWindowWidth = 0.0f;
    private float _viewWindowHeight = 0.0f;
    private float _targetPositionX = 0.0f;
    private float _targetPositionY = 0.0f;
    private int _selectedContentIndex = 0;
    // 더보기 버튼을 눌렀다면 이값을 true 로 해준다.
    private boolean _isShopView = false;

    // 찾을 페이지가 더 이상 없을 때
    private boolean _not_page = false;

    private boolean _pro2clk = false;

    String Code;

    public boolean isFind2() {
        return (_findCount2 > 0);
    }

    public boolean isFind() {
        return (_findCount > 0);
    }

    public boolean not_page() {
        return _not_page;
    }

    public boolean is_pro2clk() {
        return _pro2clk;
    }

    boolean scroll = false;

    public NaverPlaceSaveTouchUrlPatternAction(WebView webView, String code) {
        super(webView);

        _webView = webView;
        Code = code;
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.addJavascriptInterface(new GetHtmlJavascriptInterface(), JS_INTERFACE_NAME);
            }
        });
    }

    public boolean workInThread() {
        Log.d(TAG, "웹뷰 크기 얻기");
        getWindowSize();
        threadWait();
        Log.d(TAG, "web height: " + _viewWindowHeight);
        return mainProcess();
    }



    private boolean mainProcess() {
        threadWait();
        findUrl();
        if (isFind()) {
                scrollToContentInThread();
        }
        else {
            Log.d(TAG, "못찾음");

        }
        return true;
    }









    private void scrollToContentInThread() {
        SwipeThreadAction swipeThreadAction = new SwipeThreadAction(_touchInjector);
        int inside = 1;
        do {
            SystemClock.sleep(MathHelper.randomRange(0, 200));
            if (inside > 0) {
                Log.d(TAG, "아래로 스크롤");
                swipeThreadAction.swipeDown();
            } else {
                Log.d(TAG, "위로 스크롤");
                swipeThreadAction.swipeUp();
            }
//            SystemClock.sleep(1500);
            SystemClock.sleep(MathHelper.randomRange(1000, 2000));

            _targetPositionY = 0.0f;
            getProPositionY();
            Log.d(TAG, "좌표 대기중");
            threadWait();

            // 임시 예외처리
            if (_targetPositionY == 0.0f) {
                return;
            }
            Log.d(TAG, "대상이 안에 있는지 확인: " + _targetPositionY);
            inside = checkInView(_targetPositionY);

        } while (inside != 0);
        SystemClock.sleep(MathHelper.randomRange(2000, 4000));
        Log.d(TAG, "컨텐츠 클릭");
        int screenY = (int) CoordinateHelper.viewToScreenY(_webView, _viewWindowHeight, _targetPositionY);
        touchScreen(180, screenY + 10);
    }






    private float convertScreenToWebWindow(float value) {
        // 임시.
        float screenY = 1920.0f;

        return value * (_viewWindowHeight / screenY);
    }


    private int checkInView(float y) {
        float minY = convertScreenToWebWindow(370);
        float maxY = convertScreenToWebWindow(1500);
        if (y < minY) {
            return -1;
        } else if (y > maxY) {
            return 1;
        } else {
            return 0;
        }
    }

    private void getWindowSize() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window.FindView.getWindowSize(window.innerWidth, window.innerHeight);");
            }
        },100);
    }


    private void findUrl() {
        // 상품 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window.FindView.getUrlCount(document.evaluate(\"//a[contains(., '저장하기')]\", document, null, XPathResult.ANY_TYPE, null ).numberValue);");
            }
        }, 100);
    }




    private void getProPositionY() {
        // 상품 좌표 y
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                String queryString = "document.evaluate(\"//a[contains(., '저장하기')]\", document, null, XPathResult.ANY_TYPE, null )" + "iterateNext().getBoundingClientRect()";
                String query = String.format("javascript:window.FindView.getTargetPositionY(%s.top);", queryString);
                _webView.loadUrl(query);
            }
        }, 100);
    }


    private void threadWait() {
        synchronized (_mutex) {
            try {
                _mutex.wait(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public class GetHtmlJavascriptInterface {
        @JavascriptInterface
        public void getWindowSize(float width, float height) {
            Log.d(TAG, "window width:" + width + ", height:" + height);
            _viewWindowWidth = width;
            _viewWindowHeight = height;

            synchronized (_mutex) {
                _mutex.notify();
            }
        }
        @JavascriptInterface
        public void getWindowHeight(float height) {
            _viewWindowHeight = height;

            synchronized (_mutex) {
                _mutex.notify();
            }
        }

        @JavascriptInterface
        public void getUrlCount(int count) {
//            System.out.println(count);
            _findCount = count;

            synchronized (_mutex) {
                _mutex.notify();
            }
        }

        @JavascriptInterface
        public void getUrlCount2(int count) {
//            System.out.println(count);
            _findCount2 = count;

            synchronized (_mutex) {
                _mutex.notify();
            }
        }


        @JavascriptInterface
        public void getTargetPosition(float x, float y) {
            System.out.println("find target x: " + x + ", y: " + y);
            _targetPositionX = x;
            _targetPositionY = y;

            synchronized (_mutex) {
                Log.d(TAG, "락 해제");
                _mutex.notify();
            }
        }

        @JavascriptInterface
        public void getTargetPositionY(float y) {
            System.out.println("find target y:" + y);
            _targetPositionY = y;

            synchronized (_mutex) {
                Log.d(TAG, "락 해제");
                _mutex.notify();
            }
        }



        @JavascriptInterface
        public void getScrollY(float y) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
            System.out.println(y);
        }

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
