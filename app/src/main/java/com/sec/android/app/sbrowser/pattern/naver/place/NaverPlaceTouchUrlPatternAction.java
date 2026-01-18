package com.sec.android.app.sbrowser.pattern.naver.place;

import static com.sec.android.app.sbrowser.pattern.BasePatternAction.getRandomName;

import android.os.SystemClock;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;


import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverBasePatternAction;
import com.sec.android.app.sbrowser.system.CoordinateHelper;

import java.util.Locale;

public class NaverPlaceTouchUrlPatternAction extends NaverBasePatternAction {

    private static final String TAG = NaverPlaceTouchUrlPatternAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zptu";

    private static final long MAX_MORE_RUN = 7;
//    private static final long MAX_MORE_RUN = 1;

    private WebView _webView = null;

    private final Object _mutex = new Object();

    private int _findCount = 0;
    private float _viewWindowHeight = 0.0f;
    private float _targetPositionY = 0.0f;
    private int _contentInside = 0;

    private int _selectedContentIndex = 0;

    private int _findTryCount = 0;
    private String _urlTitle = null;

    private boolean _workBreak = false;

    public KeywordItemMoon item = null;

    String Code;

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

    public boolean isWorkBreak() {
        return _workBreak;
    }

    public String getCodeTitle() {
        return _urlTitle;
    }


    public NaverPlaceTouchUrlPatternAction(WebView webView,String code) {
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

    public void endPattern() {
        _webView.post(new Runnable() {
            @Override
            public void run() {
                _webView.removeJavascriptInterface(JS_INTERFACE_NAME);
            }
        });
    }

    public void workInThread() {
        _findTryCount = 0;
        _workBreak = false;

        Log.d(TAG, "웹뷰 크기 얻기");
        getWindowHeight();
        threadWait();

        Log.d(TAG, "web height: " + _viewWindowHeight);

        mainProcess();
    }

    private boolean mainProcess() {
        Log.d(TAG, "등록된 code 찾기");
        _findCount = 0;
        findPlaceCode();
        threadWait();

        if (isFind()) {
            Log.d(TAG, "등록된 code 찾았음. 순위 등록 후, 해당 컨텐츠로 스크롤");
            // 찾았을때.
            if (_findCallback != null) {
                _findCallback.contentFind(true);
            }

            //getPlaceName();
            //threadWait();

            //Log.d(TAG, "등록된 상호: " + _urlTitle);

            // 있다면 스크롤하면서 해당 컨텐츠를 찾아 클릭.
            scrollToContentInThread();
        } else {
            Log.d(TAG, "등록된 code 못찾았음.");
            // 못찾았을때.
            if (!_isMoreView) {
                Log.d(TAG, "Place 더보기 버튼으로 스크롤");
                // 더보기 화면이 아닐때.
                // View 더보기 버튼까지 스크롤한다.
                scrollToMoreButtonInThread();
                //scrollToViewMoreButtonInThread();
            } else {
                Log.d(TAG, "더보기 버튼 찾기");
                _findCount = 0;
                findMoreButton();
                threadWait();

                if ((_findTryCount < MAX_MORE_RUN) &&
                        isFind()) {
                    Log.d(TAG, "더보기 버튼으로 스크롤");
                    // 더보기 화면일때.
                    // 하단으로 스크롤하여 더보기 버튼을 클릭한다.
                    _findTryCount++;
                    //scrollToMoreButtonInThread();

                    scrollToViewMoreButtonInThread();
                    SystemClock.sleep(MathHelper.randomRange(5000, 6000));

                    return mainProcess();
                } else {
                    if (_findTryCount >= MAX_MORE_RUN) {
                        Log.d(TAG, "더보기 시도회수 초과하여 중단.");
                        _findCount = 0;
                    }
                    _findTryCount = 0;

                    if (_findCallback != null) {
                        _findCallback.contentFind(false);
                    }

                    // 더보기 버튼이 없다면
                    // 아래로 스크롤 하다가
                    // 뒤로 이동.
                    //스크롤InThread();
                    //stayActionInThread();

                    return false;
                }
            }
        }

        return true;
    }

    private void 스크롤InThread() {
        SwipeThreadAction swipeThreadAction = new SwipeThreadAction(_touchInjector);
        for(int i = 0; i<3; i++){
            swipeThreadAction.swipeDown(5,10);
            SystemClock.sleep(MathHelper.randomRange(2000, 3000));
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
    }

    private void scrollToMoreButtonInThread() {
        scrollToContentInThread(2);
    }


    // 나중에 통합..
    private void scrollToContentInThread(int type) {
        SwipeThreadAction swipeThreadAction = new SwipeThreadAction(_touchInjector);
        _contentInside = 1;

        int swipeCount = 0;

        do {
            // 20번 이상 이벤트를 진행했다면 멈춰준다.
            // 이상이 생긴것이다.
            if (swipeCount > 20) {
                _workBreak = true;
                return;
            } else {
                ++swipeCount;
            }

            SystemClock.sleep(MathHelper.randomRange(0, 200));

            if (_contentInside > 0) {
                Log.d(TAG, "아래로 스크롤");
                swipeThreadAction.swipeDown();
            } else {
                Log.d(TAG, "위로 스크롤");
                // 플레이스는 위로 한번에 올라가버리면 지도가 보일수 있다.
                // 그렇게 되면 처리가 꼬일수 있으므로 미연에 살살 올라가도록 한다.
                int prevSpeed = swipeThreadAction.stayFastMin;
                swipeThreadAction.stayFastMin = 125;
                swipeThreadAction.swipeUp();
                swipeThreadAction.stayFastMin = prevSpeed;
            }

//            SystemClock.sleep(1500);
            SystemClock.sleep(MathHelper.randomRange(1000, 2000));

            _targetPositionY = 0.0f;

            switch (type) {
                case 1:
                    // Place 더보기 버튼이 화면안에 있는지 정보를 얻는다.
                    Log.d(TAG, "Place 더보기 y 좌표 얻기, 화면 안에 있는지 확인");
                    getPlaceMoreButtonPositionY();
                    break;

                case 2:
                    // 더보기 버튼이 화면안에 있는지 정보를 얻는다.
                    Log.d(TAG, "더보기 y 좌표 얻기, 화면 안에 있는지 확인");
                    getMoreButtonPositionY();
                    break;

                default:
                    // 컨텐츠가 있는지 정보를 얻는다.
                    Log.d(TAG, "컨텐츠 y 좌표 얻기, 화면 안에 있는지 확인");
                    getContentPositionY();
                    break;
            }

            Log.d(TAG, "좌표 대기중");
            threadWait();
        } while (_contentInside != 0);

        SystemClock.sleep(MathHelper.randomRange(2000, 4000));

        Log.d(TAG, "컨텐츠 클릭");
        int screenY = (int) CoordinateHelper.viewToScreenY(_webView, _viewWindowHeight, _targetPositionY);

        if (type == 0) {
            touchScreen(150, screenY + 15);
        } else {
            touchScreen(600, screenY + 50);
        }
    }

//    private class LoadWebViewDataTask extends AsyncTask<String, Void, Void> {
//
//        @Override
//        protected Void doInBackground(String... strings) {
//            return null;
//        }
//    }

    private String getScriptQuery(String funcName, String param) {
        return String.format(Locale.getDefault(), "javascript:window.%s.%s(%s);",
                JS_INTERFACE_NAME, funcName, param);
    }

    private String getSubScriptQuery(String funcName, String param) {
        return String.format(Locale.getDefault(), "window.%s.%s(%s);",
                JS_INTERFACE_NAME, funcName, param);
    }

    private String getCheckQuery(String nodeQuery) {
        String query = "javascript:(function() {"
                + "var rect = " + nodeQuery + ".getBoundingClientRect();"
                + "var area_min = 120;"  // 120 은 상단 검색창 및 정보바를 포함한 영역 크기이다.
                + "var inside = 0;"
                + "if (rect.top < area_min) { inside = -1; }"
                + "else if (rect.bottom > window.innerHeight) { inside = 1; }"
                + getSubScriptQuery("checkInside", "inside, rect.top")
                + "})();";
        return query;
    }


    private void getWindowHeight() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl(getScriptQuery("getWindowHeight", "window.innerHeight"));
            }
        },100);
    }

    private String getContentQuery() {

            String query = "document.querySelectorAll(\"a[href*='%s']\")";
            return String.format(Locale.getDefault(), query, Code+"?entry=");

//        return "document.querySelectorAll(\"._item:first-child a[href$='" + item.code + "']\")";
    }



//    private String getDetailContentQuery() {
//        String query = "document.querySelectorAll(\".list_item a[href*='%s']\")";
//        return String.format(Locale.getDefault(), query, item.code);
////        return "document.querySelectorAll(\"._item:first-child a[href$='" + item.code + "']\")";
//    }

    private String getViewMoreButtonQuery() {
        return "document.querySelector('._nx_place_list .api_more') != null ? document.querySelector('._nx_place_list .api_more'): document.querySelector('.sp_nmap .api_more')";
    }

    private String getMoreButtonQuery() {
        String query = "._35OzJ";

        if (item.url.contains("restaurant")) {             // 식당관련
            query = "._35OzJ";
        } else if (item.url.contains("accommodation")) {   // 예약관련
            query = "a[href*='accommodation']._35OzJ";
        } else if (item.url.contains("hospital")) {        // 병원관련
            query = "._35OzJ";
        } else if (item.url.contains("hairshop")) {        // 미용실관련
            query = "._35OzJ";
        }

        return "document.querySelectorAll(\"" + query + "\")";
//        return "document.querySelectorAll(\"a[href*='level=top&entry=']\")";
    }

    private void findPlaceCode() {
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
//                String query = _isMoreView ? getDetailContentQuery() : getContentQuery();
                _webView.loadUrl(getScriptQuery("getUrlCount", getContentQuery() + ".length"));
            }
        }, 100);
    }


    private void getContentPositionY() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                String contentQuery = getContentQuery() + "[" + _selectedContentIndex + "]";
//                String contentQuery = (_isMoreView ? getDetailContentQuery() : getContentQuery())
//                        + "[" + _selectedContentIndex + "]";
                _webView.loadUrl(getCheckQuery(contentQuery));


//                String contentQuery = _isMoreView ? getDetailContentQuery() : getContentQuery();
//                String query = "javascript:(function() {"
//                        + "var rect = " + contentQuery + "[" + _selectedContentIndex + "].getBoundingClientRect();"
//                        + "var area_min = 120;"  // 120 은 상단 검색창 및 정보바를 포함한 영역 크기이다.
//                        + "var inside = (rect.top >= area_min && rect.bottom < window.innerHeight);"
//                        + getScriptQuery2("checkInside", "inside")
//                        + "})()";
//                _webView.loadUrl(query);


//                String query = _isMoreView ? getDetailContentQuery() : getContentQuery();
//                String queryString = query + "[" + _selectedContentIndex + "].getBoundingClientRect().top";
//                _webView.loadUrl(getScriptQuery("getTargetPositionY", queryString));
//                String query = String.format("javascript:window.FindView.getTargetPositionY(%s.top);", queryString);
//                _webView.loadUrl(query);
            }
        }, 100);
    }

    private void getPlaceMoreButtonPositionY() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 플레이스 더보기는 특수하다..
                String query = "javascript:(function() {"
                        + "var node = document.querySelectorAll('._nx_place_list .api_more, .sp_nmap .api_more')[0];"
//                        + "var node = document.querySelector('._nx_place_list .api_more');"
//                        + "if (node == null) { node = document.querySelector('.sp_nmap .api_more'); }"
                        + "var rect = node.getBoundingClientRect();"
                        + "var area_min = 120;"  // 120 은 상단 검색창 및 정보바를 포함한 영역 크기이다.
                        + "var inside = 0;"
                        + "if (rect.top < area_min) { inside = -1; }"
                        + "else if (rect.bottom > window.innerHeight) { inside = 1; }"
                        + getSubScriptQuery("checkInside", "inside, rect.top")
                        + "})();";
                _webView.loadUrl(query);
//                _webView.loadUrl(getCheckQuery(getViewMoreButtonQuery()));
            }
        }, 100);
    }

    private void findMoreButton() {
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl(getScriptQuery("getUrlCount", getMoreButtonQuery() + ".length"));
            }
        }, 100);
    }

    private void getMoreButtonPositionY() {
        // View 더보기 버튼의 현재 화면의 위치를 얻는다.
        _webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                _webView.loadUrl(getCheckQuery(getMoreButtonQuery() + "[0]"));
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
            _findCount = count;

            synchronized (_mutex) {
                _mutex.notify();
            }
        }

        @JavascriptInterface
        public void getTargetPositionY(float y) {
            Log.d(TAG, "find target y:" + y);
            _targetPositionY = y;

            synchronized (_mutex) {
                Log.d(TAG, "락 해제");
                _mutex.notify();
            }
        }

        @JavascriptInterface
        public void checkInside(int inside, float targetY) {
            Log.d(TAG, "target inside:" + inside + ", target y:" + targetY);
            _contentInside = inside;
            _targetPositionY = targetY;

            synchronized (_mutex) {
                Log.d(TAG, "락 해제");
                _mutex.notify();
            }
        }

        @JavascriptInterface
        public void getUrlTitle(String text) {
            Log.d(TAG, "getUrlTitle:" + text);
            _urlTitle = text;

            synchronized (_mutex) {
                _mutex.notify();
            }
        }
    }
}
