package com.sec.android.app.sbrowser.pattern.naver.shop;

import static com.sec.android.app.sbrowser.pattern.BasePatternAction.getRandomName;

import android.os.SystemClock;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverBasePatternAction;
import com.sec.android.app.sbrowser.system.CoordinateHelper;

public class NaverShopTouchUrlPatternAction extends NaverBasePatternAction {

    private static final String TAG = NaverShopTouchUrlPatternAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "FindView";

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

    String MID;
    String MID2;

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

    public NaverShopTouchUrlPatternAction(WebView webView, String mid,String mid2) {
        super(webView);

        _webView = webView;
        MID = mid;
        MID2 = mid2;
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

    public boolean workInThread2(Boolean chk) {
        Log.d(TAG, "웹뷰 크기 얻기");
        getWindowSize();
        threadWait();
        Log.d(TAG, "web height: " + _viewWindowHeight);
        return mainProcess2(chk);
    }

    private boolean mainProcess() {
        /*
        if(!_isShopView) {
            touchMoreTabButton();
            touchShopTabButton();
            threadWait();
        }else {
            scrollToENDInThread();
            threadWait();
        }
         */
        //스크롤InThread();
        //scrollToENDInThread();
        //threadWait();

        if (!_isShopView) {
            Log.d(TAG, "Shop 더보기 버튼으로 스크롤");
            scrollToMoreButtonInThread();
            SystemClock.sleep(MathHelper.randomRange(5000, 7000));
        }

        Log.d(TAG, "상품찾기");
        findUrl(MID);
        threadWait();
        if (isFind()) {
            Log.d(TAG, "찾음");
            if (scroll) {
                scrollToContentInThread(MID);
            } else {
                proScroll(MID);
                scrollToContentInThread(MID);
            }

        } else {
            Log.d(TAG, "못찾음");
            /*
            if(scroll) {
                scrollToNextInThread();
            }else{
                nextButtonScroll();
                scrollToNextInThread();
            }
*/
        }
        return true;
    }

    private boolean mainProcess2(Boolean chk) {
        if(!chk) {
            findUrl2();
            threadWait();
        }

        if(!chk) {
            if (isFind2()) {
                더보기스크롤InThread();
                threadWait();
                findUrl(MID2);
                threadWait();
                if (isFind()) {
                    Log.d(TAG, "찾음");
                    scrollToContentInThread(MID2);
                    _pro2clk = true;
                }else{
                    Log.d(TAG, "못찾음");
                    스크롤InThread();
                }
            } else {

                findUrl(MID2);
                threadWait();
                if (isFind()) {
                    Log.d(TAG, "찾음");
                    scrollToContentInThread(MID2);
                    _pro2clk = true;
                }

            }
        }
        else{
            findUrl(MID2);
            threadWait();
            if (isFind()) {
                Log.d(TAG, "찾음");
                scrollToContentInThread(MID2);
                _pro2clk = true;
            }else{
                Log.d(TAG, "못찾음");
                스크롤InThread();
            }
        }
        return true;
    }


    private void touchMoreTabButton() {
        _targetPositionX = 0.0f;
        _targetPositionY = 0.0f;
        Log.d(TAG, "더보기 탭 버튼 x, y 좌표 얻기");
        getMoreTabButtonPosition();
        Log.d(TAG, "좌표 대기중");
        threadWait();
        // 임시 예외처리
        if (_targetPositionY == 0.0f) {
            return;
        }
        SystemClock.sleep(MathHelper.randomRange(2000, 4000));
        Log.d(TAG, "더보기 탭 버튼 클릭");
        int screenX = (int) CoordinateHelper.viewToScreenX(_webView, _viewWindowWidth, _targetPositionX);
        int screenY = (int) CoordinateHelper.viewToScreenY(_webView, _viewWindowHeight, _targetPositionY);
        touchScreen(screenX+45, screenY+15);
    }



    private void touchShopTabButton() {
        _targetPositionX = 0.0f;
        _targetPositionY = 0.0f;
        Log.d(TAG, "쇼핑 탭 버튼 x, y 좌표 얻기");
        getShopTabButtonPosition();
        Log.d(TAG, "좌표 대기중");
        threadWait();
        // 임시 예외처리
        if (_targetPositionY == 0.0f) {
            return;
        }
        SystemClock.sleep(MathHelper.randomRange(2000, 4000));
        Log.d(TAG, "쇼핑 탭 버튼 클릭");
        int screenX = (int) CoordinateHelper.viewToScreenX(_webView, _viewWindowWidth, _targetPositionX);
        int screenY = (int) CoordinateHelper.viewToScreenY(_webView, _viewWindowHeight, _targetPositionY);
        touchScreen(screenX + 15, screenY + 15);
    }


    private void scrollToMoreButtonInThread() {
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
            getShoppingMoreButtonPositionY();
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
        _isShopView = true;
    }

    private void scrollToContentInThread(String _MID) {
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
            getProPositionY(_MID);
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

    private void scrollToNextInThread() {
        SwipeThreadAction swipeThreadAction = new SwipeThreadAction(_touchInjector);
        int inside = 1;
        do {
            SystemClock.sleep(MathHelper.randomRange(0, 200));
            if (inside > 0) {
                Log.d(TAG, "아래로 스크롤");
                swipeThreadAction.swipeDown(70,100);
            } else {
                Log.d(TAG, "위로 스크롤");
                swipeThreadAction.swipeUp(70,100);
            }
//            SystemClock.sleep(1500);
            SystemClock.sleep(MathHelper.randomRange(1000, 2000));

            _targetPositionY = 0.0f;
            getNextButtonPosition();
            Log.d(TAG, "좌표 대기중");
            threadWait();
            // 임시 예외처리
            if (_targetPositionY == 0.0f) {
                Log.d(TAG, "더 이상 찾을 페이지 없음....");
                _not_page=true;
                return;
            }
            Log.d(TAG, "대상이 안에 있는지 확인: " + _targetPositionY);
            inside = checkInView(_targetPositionY);

        } while (inside != 0);
        SystemClock.sleep(MathHelper.randomRange(2000, 4000));
        Log.d(TAG, "다음 페이지 클릭");
        int screenX = (int) CoordinateHelper.viewToScreenX(_webView, _viewWindowWidth, _targetPositionX);
        int screenY = (int) CoordinateHelper.viewToScreenY(_webView, _viewWindowHeight, _targetPositionY);
        touchScreen(screenX+15, screenY + 15);
    }

    //페이지 맨 밑까지 스크롤
    private void scrollToENDInThread() {
        SwipeThreadAction swipeThreadAction = new SwipeThreadAction(_touchInjector);
        int inside = 1;
        do {
            SystemClock.sleep(MathHelper.randomRange(0, 200));
            if (inside > 0) {
                Log.d(TAG, "아래로 스크롤");
                swipeThreadAction.swipeDown(5,10);
            } else {
                Log.d(TAG, "위로 스크롤");
                swipeThreadAction.swipeUp(200,300);
            }
//            SystemClock.sleep(1500);
            SystemClock.sleep(MathHelper.randomRange(1000, 2000));

            _targetPositionY = 0.0f;
            getNextButtonPosition();
            Log.d(TAG, "좌표 대기중");
            threadWait();
            // 임시 예외처리
            if (_targetPositionY == 0.0f) {
                return;
            }
            Log.d(TAG, "대상이 안에 있는지 확인: " + _targetPositionY);
            inside = checkInView(_targetPositionY);
        } while (inside != 0);

    }

    private void 스크롤InThread() {
        SwipeThreadAction swipeThreadAction = new SwipeThreadAction(_touchInjector);
      for(int i = 0; i<10; i++){
          swipeThreadAction.swipeDown(5,10);
          SystemClock.sleep(MathHelper.randomRange(2000, 3000));
        }

    }


    private void 더보기스크롤InThread() {
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
            getMoreProButtonPositionY();
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
        Log.d(TAG, "가격비교 더보기 클릭");
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
    private String getMidQuery(String _MID) {
        return "document.querySelectorAll(\"a[data-i^='" + _MID + "']\")";
    }
    private String getNextQuery() {
        return "document.querySelectorAll('._1nBYwhu2M3')";
    }


    private String getShopMoreButtonQuery() {
        return "document.querySelectorAll(\".sp_nshop .api_more_wrap a\")";
    }

    private String getMoreTabButtonQuery() {
        return "document.querySelectorAll('a.btn_tab_more._unfold')";
    }

    private String getShopTabButtonQuery() {
        return "document.querySelectorAll(\".bx a[href*='shopping']\")";
    }

    private String getMoreProButtonQuery() {
        return "document.querySelectorAll('.link_more.linkAnchor')";
    }


    private void findUrl(final String _MID) {
        // 상품 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window.FindView.getUrlCount(" + getMidQuery(_MID) + ".length);");
            }
        }, 100);
    }

    private void findUrl2() {
        // 상품 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window.FindView.getUrlCount2(" + getMoreProButtonQuery() + ".length);");
            }
        }, 100);
    }

    private void proScroll(final String _MID) {
        // 상품 까지 스크롤
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                String queryString = "javascript:"+getMidQuery(_MID) + "[0].scrollIntoView();";
                _webView.loadUrl(queryString);
            }
        }, 100);
        SystemClock.sleep(MathHelper.randomRange(2000, 4000));
      //  Log.d(TAG, "컨텐츠 클릭");
     //   int screenY = (int) CoordinateHelper.viewToScreenY(_webView, _viewWindowHeight, _targetPositionY);
        //touchScreen(180, screenY+10);
    }

    private void nextButtonScroll() {
        // 다음 페이지 버튼 까지 스크롤
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                String queryString = "javascript:"+getNextQuery() + "[0].scrollIntoView();";
                _webView.loadUrl(queryString);
            }
        }, 100);
    }


    private void getShoppingMoreButtonPositionY() {
        // 쇼핑 더보기 버튼
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                String queryString = getShopMoreButtonQuery() + "[" + _selectedContentIndex + "].getBoundingClientRect()";
                String query = String.format("javascript:window.FindView.getTargetPositionY(%s.top);", queryString);
                _webView.loadUrl(query);
            }
        }, 100);
    }

    private void getProPositionY(final String _MID) {
        // 상품 좌표 y
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                String queryString = getMidQuery(_MID) + "[0].getBoundingClientRect()";
                String query = String.format("javascript:window.FindView.getTargetPositionY(%s.top);", queryString);
                _webView.loadUrl(query);
            }
        }, 100);
    }



      private void getMoreProButtonPositionY() {
        // 가격비교 더보기 버튼
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                String queryString = getMoreProButtonQuery() + "[" + _selectedContentIndex + "].getBoundingClientRect()";
                String query = String.format("javascript:window.FindView.getTargetPositionY(%s.top);", queryString);
                _webView.loadUrl(query);
            }
        }, 100);
    }



    private void getNextButtonPosition() {
        // 상품 다음페이지 좌표
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window.FindView.getTargetPosition(" + getNextQuery() + "[0].getBoundingClientRect().left, " + getNextQuery() + "[0].getBoundingClientRect().top);");
            }
        }, 100);
    }



    private void getMoreTabButtonPosition() {
        // 탭 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window.FindView.getTargetPosition(" + getMoreTabButtonQuery() + "[0].getBoundingClientRect().left, " + getMoreTabButtonQuery() + "[0].getBoundingClientRect().top);");
            }
        }, 100);
    }

    private void getShopTabButtonPosition() {
        // 쇼핑 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window.FindView.getTargetPosition(" + getShopTabButtonQuery() + "[0].getBoundingClientRect().left, " + getShopTabButtonQuery() + "[0].getBoundingClientRect().top);");
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
