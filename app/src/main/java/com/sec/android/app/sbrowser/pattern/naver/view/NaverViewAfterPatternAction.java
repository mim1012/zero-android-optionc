package com.sec.android.app.sbrowser.pattern.naver.view;

import android.os.SystemClock;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverBasePatternAction;
import com.sec.android.app.sbrowser.system.CoordinateHelper;

public class NaverViewAfterPatternAction extends NaverBasePatternAction {

    private static final String TAG = NaverViewAfterPatternAction.class.getSimpleName();

    private WebView _webView = null;

    private final Object _mutex = new Object();

    private int _findCount = 0;
    private float _viewWindowHeight = 0.0f;
    private float _targetPositionY = 0.0f;

    private int _selectedContentIndex = 0;
    String url_;

    public boolean isFind() {
        return (_findCount > 0);
    }


    public NaverViewAfterPatternAction(WebView webView,String url) {
        super(webView);

        _webView = webView;
        url_ = url;
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.addJavascriptInterface(new GetHtmlJavascriptInterface(), "AfterView");
            }
        });
    }

    public void endPattern() {
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.removeJavascriptInterface("AfterView");
            }
        });
    }

    public void workInThread() {
        Log.d(TAG, "웹뷰 크기 얻기");
        getWindowHeight();

        threadWait();

        Log.d(TAG, "web height: " + _viewWindowHeight);
        Log.d(TAG, "더보기 버튼 찾기");
        _findCount = 0;
        findMoreButton();

        threadWait();

        if (isFind()) {
            Log.d(TAG, "더보기 버튼으로 스크롤");
            // 더보기 화면일때.
            // 하단으로 스크롤하여 더보기 버튼을 클릭한다.

            // 더보기 버튼이 보이면 자동 로드 처리로 변경되어 터치하지 않고, 스크롤 회수만 변경.
//            scrollToMoreButtonInThread();
            stayActionInThread((int)MathHelper.randomRange(4, 5));

            SystemClock.sleep(MathHelper.randomRange(5000, 6000));
        } else {
            // 더보기 버튼이 없다면
            // 아래로 스크롤 하다가
            // 뒤로 이동.
            stayActionInThread((int)MathHelper.randomRange(2, 3));
        }

        // 위 둘중하나가 끝나면 다시 기다린다.
        SystemClock.sleep(MathHelper.randomRange(3000, 5000));
    }

    // 일단 복붙.. 구조를 좀 개선하면 될듯함.
    // StayPattern 과 내용이 같다..
    private void stayActionInThread(int maxCount) {
        SwipeThreadAction swipeThreadAction = new SwipeThreadAction(_touchInjector);
        int runCount = 0;

        Log.d(TAG, maxCount + "번의 스크롤 행동이 설정됨");

        while (runCount < maxCount) {
            // 처음 1번은 무조건 아래로 간다.
            if (runCount < 1) {
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


    private void scrollToMoreButtonInThread() {
        scrollToContentInThread(2);
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

                case 2:
                    // 더보기 버튼이 화면안에 있는지 정보를 얻는다.
                    Log.d(TAG, "더보기 y 좌표 얻기");
                    getMoreButtonPositionY();
                    break;

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
                _webView.loadUrl("javascript:window.AfterView.getUrlCount(document.querySelectorAll(\"a[href^='https://m.blog.naver.com/nanumi_/221334841722']\").length);");
            }
        }, 100);
    }

    private void getWindowHeight() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window.AfterView.getWindowHeight(window.innerHeight);");
            }
        },100);
    }

    private String getContentQuery() {
        return "document.querySelectorAll(\"a[href^='" + url_ + "']\")";
    }

    private String getViewMoreButtonQuery() {
        return "document.querySelector('._list').querySelector('.api_more')";
    }

    private String getMoreButtonQuery() {
        return "document.getElementsByClassName('api_more_multi')";
    }

    private void findUrl() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window.AfterView.getUrlCount(" + getContentQuery() + ".length);");
            }
        }, 100);
    }

    private void getContentPositionY() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                String queryString = getContentQuery() + "[" + _selectedContentIndex + "].getBoundingClientRect()";
                String query = String.format("javascript:window.AfterView.getTargetPositionY(%s.top);", queryString);
                _webView.loadUrl(query);
            }
        }, 100);
    }

    private void getViewMoreButtonPositionY() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window.AfterView.getTargetPositionY(document.querySelector('._list').querySelector('.api_more').getBoundingClientRect().top);");
            }
        }, 100);
    }

    private void findMoreButton() {
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl("javascript:window.AfterView.getUrlCount(" + getMoreButtonQuery() + ".length);");
            }
        }, 100);
    }

    private void getMoreButtonPositionY() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 수정 필요.
                _webView.loadUrl("javascript:window.AfterView.getTargetPositionY(" + getMoreButtonQuery() + "[0].getBoundingClientRect().top);");
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
