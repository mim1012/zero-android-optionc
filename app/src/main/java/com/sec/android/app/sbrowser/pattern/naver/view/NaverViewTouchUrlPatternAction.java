package com.sec.android.app.sbrowser.pattern.naver.view;

import static com.sec.android.app.sbrowser.pattern.BasePatternAction.getRandomName;

import android.os.SystemClock;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverBasePatternAction;
import com.sec.android.app.sbrowser.system.CoordinateHelper;

public class NaverViewTouchUrlPatternAction extends NaverBasePatternAction {

    private static final String TAG = NaverViewTouchUrlPatternAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "FindView";

    private static final long MAX_MORE_RUN = 3;
//    private static final long MAX_MORE_RUN = 1;

    private WebView _webView = null;

    private final Object _mutex = new Object();

    private int _findCount = 0;
    private float _viewWindowWidth = 0.0f;
    private float _viewWindowHeight = 0.0f;
    private float _targetPositionX = 0.0f;
    private float _targetPositionY = 0.0f;

    private float _prevTargetOffsetY = 0.0f;
    private float _targetOffsetY = 0.0f;

    private int _selectedContentIndex = 0;

    private int _findTryCount = 0;

    // 더보기 버튼을 눌렀다면 이값을 true 로 해준다.
    private boolean _isMoreView = false;


    // 임시로 찾기 콜백 도입.
    private FindCallback _findCallback = null;

    public void setFindCallback(FindCallback callback) {
        _findCallback = callback;
    }

    public interface FindCallback {
        void contentFind(boolean find);
    }


    public void setMoreView(boolean moreView) {
        _isMoreView = moreView;
    }

    public boolean isFind() {
        return (_findCount > 0);
    }
    String url_;

    public NaverViewTouchUrlPatternAction(WebView webView,String url) {
        super(webView);

        _webView = webView;
        url_ = url;
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.addJavascriptInterface(new GetHtmlJavascriptInterface(), JS_INTERFACE_NAME);
            }
        });
    }

    public void endPattern() {
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.removeJavascriptInterface(JS_INTERFACE_NAME);
            }
        });
    }

    public boolean workInThread() {
        _findTryCount = 0;

        Log.d(TAG, "웹뷰 크기 얻기");
        getWindowSize();
        threadWait();

        Log.d(TAG, "web height: " + _viewWindowHeight);

        return mainProcess();
    }

    private boolean mainProcess() {
        Log.d(TAG, "등록된 url 찾기");
        _findCount = 0;
        findUrl();
        threadWait();

        if (isFind()) {
            Log.d(TAG, "등록된 url 찾았음. 순위 등록 후, 해당 컨텐츠로 스크롤");
            // 찾았을때.
            if (_findCallback != null) {
                _findCallback.contentFind(true);
            }

            // 있다면 스크롤하면서 해당 컨텐츠를 찾아 클릭.
            scrollToContentInThread();
        } else {
            Log.d(TAG, "등록된 url 못찾았음.");
            // 못찾았을때.
            if (!_isMoreView) {
                // 더보기 화면이 아닐때는 더보기 버튼 혹은 view 탭을 직접 클릭한다.
                _findCount = 0;
//                findViewMoreButton();
//                threadWait();
//
//                if (isFind()) {
//                    Log.d(TAG, "View 더보기 버튼으로 스크롤");
//                    // View 더보기 버튼까지 스크롤한다.
//                    scrollToViewMoreButtonInThread();
//                } else {
                    Log.d(TAG, "View 탭을 바로 터치한다.");
                    // View 더보기 버튼까지 스크롤한다.
                    touchViewTabButton();
//                }

                // 강제 초기화 필요. 외부에서 다시 사용한다. 원래 여기는 못찾으면 들어오는 것이므로 0으로 초기화.
                _findCount = 0;
            } else {
                Log.d(TAG, "컨텐츠 로드 위하여 아래로..");
//                _findCount = 0;

                checkPageBottom();
                Log.d(TAG, "화면 하단체크");
                threadWait();

                Log.d(TAG, "대상이 안에 있는지 확인: " + _targetPositionY);
                int inside = checkInView(_targetPositionY);

                if (inside != 0) {
                    SwipeThreadAction swipeThreadAction = new SwipeThreadAction(_touchInjector);

                    Log.d(TAG, "아래로 스크롤: " + _findTryCount);
//                    swipeThreadAction.swipeDownFast(130, 250);
                    swipeThreadAction.swipeDownFast(50, 55);
                    SystemClock.sleep(MathHelper.randomRange(1000, 2000));

                    SystemClock.sleep(2000);
                } else {
                    Log.d(TAG, "화면 하단이어서 중단.");
                    _findCount = 0;
                    return false;
                }

                if (_findTryCount >= (MAX_MORE_RUN * 10)) {
                    Log.d(TAG, "더보기 시도회수 초과하여 중단.");
                    _findCount = 0;
                    return false;
                } else {
                    ++_findTryCount;
                    return mainProcess();
                }

//                findMoreButton();
//                threadWait();
//
//                if ((_findTryCount < MAX_MORE_RUN) &&
//                        isFind()) {
//                    Log.d(TAG, "더보기 버튼으로 스크롤:" + _findTryCount);
//                    // 더보기 화면일때.
//                    // 하단으로 스크롤하여 더보기 버튼을 클릭한다.
//                    _findTryCount++;
//                    scrollToMoreButtonInThread();
//
//                    SystemClock.sleep(MathHelper.randomRange(5000, 6000));
//
//                    return mainProcess();
//                } else {
//                    if (_findTryCount >= MAX_MORE_RUN) {
//                        Log.d(TAG, "더보기 시도회수 초과하여 중단.");
//                        _findCount = 0;
//                    }
//                    _findTryCount = 0;
//
//                    if (_findCallback != null) {
//                        _findCallback.contentFind(false);
//                    }
//
//                    // 더보기 버튼이 없다면
//                    // 아래로 스크롤..
//                    stayActionInThread();
//
//                    return false;
//                }
            }
        }

        return true;
    }

    private void scrollBottomInThread() {
        SwipeThreadAction swipeThreadAction = new SwipeThreadAction(_touchInjector);
        int maxCount = (int)MathHelper.randomRange(4, 5);
        int runCount = 0;

        Log.d(TAG, maxCount + "번의 스크롤 행동이 설정됨");

        while (runCount < maxCount) {
            Log.d(TAG, "아래로 스크롤");
            swipeThreadAction.swipeDown();
            SystemClock.sleep(MathHelper.randomRange(1300, 2500));
            ++runCount;
        }
    }

    // StayPattern 과 내용이 같다..
    private void stayActionInThread() {
        SwipeThreadAction swipeThreadAction = new SwipeThreadAction(_touchInjector);
        int maxCount = (int)MathHelper.randomRange(3, 10);
        int runCount = 0;

        Log.d(TAG, maxCount + "번의 스크롤 행동이 설정됨");

        while (runCount < maxCount) {
            // 처음 3번은 무조건 아래로 간다.
            if (runCount < 3) {
                Log.d(TAG, "아래로 스크롤");
                swipeThreadAction.swipeDown();
            } else {
                int isUp = (int)MathHelper.randomRange(0, 1);

                if (isUp == 0) {
                    Log.d(TAG, "아래로 스크롤");
                    swipeThreadAction.swipeDown();
                } else {
                    Log.d(TAG, "위로 스크롤");
                    swipeThreadAction.swipeUp();
                }
            }

            SystemClock.sleep(MathHelper.randomRange(1300, 2500));

            ++runCount;
        }
    }


    // 외부 쓰레드 내부에서 되어야 한다.
    private void scrollToContentInThread() {
        _selectedContentIndex = (int)MathHelper.randomRange(0, _findCount - 1);

        Log.d(TAG, "컨텐츠 링크 " + _findCount + "개 중 " + _selectedContentIndex + "번 선택");

//        _touchInjector.swipeScreen(true);
        scrollToContentInThread(0);
    }

    private void scrollToViewMoreButtonInThread() {
        scrollToContentInThread(1);
//        int inside = 1;
//
//        do {
//            SystemClock.sleep(MathHelper.randomRange(0, 200));
//
//            if (inside > 0) {
//                Log.d(TAG, "아래로 스크롤");
//                swipeDownInThread();
//            } else {
//                Log.d(TAG, "위로 스크롤");
//                swipeUpInThread();
//            }
//
//            SystemClock.sleep(1500);
//
//            // 버튼이 화면안에 있는지 정보를 얻는다.
//            getViewMoreButtonPositionY();
//
//            threadWait();
//
//            Log.d(TAG, "뷰 버튼 있는지 확인: " + _targetPositionY);
//            inside = checkInView(_targetPositionY);
//        } while (inside != 0);
//
//        SystemClock.sleep(MathHelper.randomRange(2000, 4000));
//
//        Log.d(TAG, "뷰 버튼 클릭");
//        _touchInjector.touchScreen(600, (int)convertWebWindowToScreen(_targetPositionY) + 60);
    }

    private void scrollToMoreButtonInThread() {
        scrollToContentInThread(2);
    }

    private void touchViewTabButton() {
        _targetPositionX = 0.0f;
        _targetPositionY = 0.0f;
        Log.d(TAG, "탭 버튼 x, y 좌표 얻기");
        getViewTabButtonPosition();

        Log.d(TAG, "좌표 대기중");
        threadWait();

        // 임시 예외처리
        if (_targetPositionY == 0.0f) {
            return;
        }

        SystemClock.sleep(MathHelper.randomRange(2000, 4000));

        Log.d(TAG, "탭 버튼 클릭: " + _targetPositionX + ", " + _targetPositionY);
        int screenX = (int) CoordinateHelper.viewToScreenX(_webView, _viewWindowWidth, _targetPositionX);
        int screenY = (int) CoordinateHelper.viewToScreenY(_webView, _viewWindowHeight, _targetPositionY);

        touchScreen(screenX + 30, screenY + 15);
    }


    // 나중에 통합..
    private void scrollToContentInThread(int type) {
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

            switch (type) {
                case 1:
                    // View 더보기 버튼이 화면안에 있는지 정보를 얻는다.
                    Log.d(TAG, "View 더보기 y 좌표 얻기");
                    getViewMoreButtonPositionY();
                    break;

                case 2: {
                    int prevCount = _findCount;
                    _findCount = 0;
                    findMoreButton();
                    threadWait();

                    if (!isFind()) {
                        Log.d(TAG, "더보기 버튼이 없어서 중단.");
                        _findCount = prevCount;
                        return;
                    }

                    _findCount = prevCount;
                    _prevTargetOffsetY = _targetOffsetY;
                    _targetOffsetY = 0.0f;

                    // 더보기 버튼이 화면안에 있는지 정보를 얻는다.
                    Log.d(TAG, "더보기 y 좌표 얻기");
                    getMoreButtonPositionY();
                    break;
                }

                default:
                    // 컨텐츠가 있는지 정보를 얻는다.
                    Log.d(TAG, "컨텐츠 y 좌표 얻기");
                    getContentPositionY();
                    break;
            }

            Log.d(TAG, "좌표 대기중");
            threadWait();

            // 임시 예외처리
            if (_targetPositionY == 0.0f) {
                return;
            }

            Log.d(TAG, "대상이 안에 있는지 확인: " + _targetPositionY);
            inside = checkInView(_targetPositionY);

            if (type == 2) {
                // 더보기 버튼이 보이면 자동 로드 처리로 변경되어 좌표값 비교로 로드여부 판단함.
                if ((_prevTargetOffsetY > 0) &&  (_targetOffsetY != _prevTargetOffsetY)) {
                    Log.d(TAG, "더보기 버튼 로딩으로 중단");
                    return;
                }
            }
        } while (inside != 0);

        SystemClock.sleep(MathHelper.randomRange(2000, 4000));

        Log.d(TAG, "컨텐츠 클릭");
        int screenY = (int) CoordinateHelper.viewToScreenY(_webView, _viewWindowHeight, _targetPositionY);

        if (type == 0) {
            touchScreen(150, screenY + 20);
        } else {
            touchScreen(600, screenY + 50);
        }
    }


    private float convertScreenToWebWindow(float value) {
        // 임시.
        float screenY = 1920.0f;

        return value * (_viewWindowHeight / screenY);
    }

    private float convertWebWindowToScreen(float value) {
        // 임시.
        float screenY = 1920.0f;

        return value * (screenY / _viewWindowHeight);
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

//    private class LoadWebViewDataTask extends AsyncTask<String, Void, Void> {
//
//        @Override
//        protected Void doInBackground(String... strings) {
//            return null;
//        }
//    }

    private void findUrl2() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getUrlCount(document.querySelectorAll(\"a[href^='https://m.blog.naver.com/nanumi_/221334841722']\").length);");
            }
        }, 100);
    }

    private void getWindowSize() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getWindowSize(window.innerWidth, window.innerHeight);");
            }
        },100);
    }

    private String getContentQuery() {
        return "document.querySelectorAll(\"a[href*='" + url_ + "'].t0ZSeRhLDI88qOA3Nvk6\")";
    }

    private String getViewMoreButtonQuery() {
        return "document.querySelectorAll('.sp_nreview .group_more')";
//        return "document.querySelectorAll('._panel .api_more')";
    }

    private String getMoreButtonQuery() {
        return "document.querySelectorAll('.api_more_multi')";
    }

    private String getViewTabButtonQuery() {
        return "document.querySelectorAll(\".flick_bx a[href*='m_blog']\")";
    }

    private void checkPageBottom() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                String queryString = "document.querySelectorAll('.footer_etc')[0].getBoundingClientRect()";
                String query = String.format("javascript:window." + JS_INTERFACE_NAME + ".getTargetPositionY(%s.top);", queryString);
                _webView.loadUrl(query);
            }
        }, 100);
    }

    private void findUrl() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getUrlCount(" + getContentQuery() + ".length);");
            }
        }, 100);
    }

    private void findViewMoreButton() {
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getUrlCount(" + getViewMoreButtonQuery() + ".length);");
            }
        }, 100);
    }

    private void findMoreButton() {
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getUrlCount(" + getMoreButtonQuery() + ".length);");
            }
        }, 100);
    }

    private void findViewTabButton() {
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getUrlCount(" + getViewTabButtonQuery() + ".length);");
            }
        }, 100);
    }

    private void getContentPositionY() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                String queryString = getContentQuery() + "[" + _selectedContentIndex + "].getBoundingClientRect()";
                String query = String.format("javascript:window." + JS_INTERFACE_NAME + ".getTargetPositionY(%s.top);", queryString);
                _webView.loadUrl(query);
            }
        }, 100);
    }

    private void getViewMoreButtonPositionY() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getTargetPositionY(" + getViewMoreButtonQuery() + "[0].getBoundingClientRect().top);");
            }
        }, 100);
    }

    private void getMoreButtonPositionY() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 수정 필요.
                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getTargetPositionWithOffsetY(" + getMoreButtonQuery() + "[0].getBoundingClientRect().top, " + getMoreButtonQuery() + "[0].offsetTop);");
            }
        }, 100);
    }

    private void getViewTabButtonPosition() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getTargetPosition(" + getViewTabButtonQuery() + "[0].getBoundingClientRect().left, " + getViewTabButtonQuery() + "[0].getBoundingClientRect().top);");
            }
        }, 100);
    }


    private void getScrollY() {
        // 현재 화면의 스크롤 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getScrollY(document.documentElement.scrollTop);");
            }
        }, 100);
    }


    private void checkSS() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
//                view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);"); //<html></html> 사이에 있는 모든 html을 넘겨준다.
                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getPosition(document.querySelector('._list').querySelector('.api_more').getBoundingClientRect().top);");
//                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getHtml(document.querySelector('._list').querySelector('.api_more').getBoundingClientRect().top);");

//                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getPosition(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().y);");
//                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getPosition(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().top);");
//                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getPosition(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().height);");
//                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getHtml(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().y);"); // not
//                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getHtml(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().top);");
//                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getHtml(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].getBoundingClientRect().height);");
//                _webView.loadUrl("javascript:window." + JS_INTERFACE_NAME + ".getHtml(document.getElementsByClassName('_list')[0].getElementsByClassName('api_more')[0].innerHTML);");
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
        public void getTargetPositionWithOffsetY(float posY, float offsetY) {
            System.out.println("find target posY:" + posY + ", offsetY:" + offsetY);
            _targetPositionY = posY;
            _targetOffsetY = offsetY;

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
