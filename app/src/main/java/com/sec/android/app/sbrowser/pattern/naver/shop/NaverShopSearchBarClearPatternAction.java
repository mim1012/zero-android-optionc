package com.sec.android.app.sbrowser.pattern.naver.shop;

import android.os.SystemClock;
import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.pattern.BasePatternAction;

public class NaverShopSearchBarClearPatternAction extends BasePatternAction {

    private static final String TAG = NaverShopSearchBarClearPatternAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zssbcl";

    public String keyword = null;

    public NaverShopSearchBarClearPatternAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsApi.register(_jsInterface);
    }

    public boolean clearSearchBar() {
        int pattern = (int) MathHelper.randomRange(0, 2);
        pattern = 0;

        boolean result = false;

        switch (pattern) {
            case 0:
                result = work1();
                break;

            case 1:
                work2();
                break;

            case 2:
                work3();
                break;
        }

        return result;
    }

    private boolean work1() {
        Log.d(TAG, "쇼핑 검색창 엑스버튼 바로 터치");

        if (!getWebViewWindowSize(true)) {
            return false;
        }
        String selector = getShopSearchBarClearButtonSelector();
        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget(30);
    }

    private void work2() {
        Log.d(TAG, "검색창 터치");
        touchSearchBarInThread();

        // 4 ~ 6 초대기.
        SystemClock.sleep(MathHelper.randomRange(4000, 6000));

        Log.d(TAG, "검색창 엑스버튼 터치2");
        touchClearButton2InThread();
    }

    private void work3() {
        Log.d(TAG, "검색창 터치");
        touchSearchBarInThread();

        // 4 ~ 6 초대기.
        SystemClock.sleep(MathHelper.randomRange(4000, 6000));

        int count = keyword.length() / 6 + 1;

        while (count-- > 0) {
            Log.d(TAG, "검색창을 다시 터치");
            touchSearchBar2InThread();
            SystemClock.sleep(MathHelper.randomRange(2000, 3000));

            int isLong = (int)MathHelper.randomRange(0, 1);
            if (isLong == 0) {
                Log.d(TAG, "백스페이스 연타");
                touchBackFastPress();
            } else {
                Log.d(TAG, "백스페이스 롱프레스");
                touchBackLongPress();
            }

            SystemClock.sleep(MathHelper.randomRange(2000, 3000));
        }
    }

    private String getShopSearchBarClearButtonSelector() {
        return ".searchInput_btn_delete__kX_du";
    }



    // 구소스 잔재.

    // 외부 쓰레드 내부에서 되어야 한다.
    private void touchSearchBarInThread() {
        touchScreen(660, 150);
    }

    private void touchSearchBar2InThread() {
        touchScreen(760, 150);
    }

    // 키보드 올라오기 전 네이버 로고가 있는 검색창의 x 버튼 좌표
    private void touchClearButton1InThread() {
        touchScreen(875, 160);
    }

    // 검색창 내부의 x 버튼 클릭.
    private void touchClearButton2InThread() {
        touchScreen(875, 160);
    }

    private void touchBackFastPress() {
        int maxTap = (int)MathHelper.randomRange(6, 9);

        for (int i = 0; i < maxTap; ++i) {
            touchBackButton();
            SystemClock.sleep(MathHelper.randomRange(250, 700));
        }
    }

    private void touchBackLongPress() {
        _touchInjector.touchKeyboardLong(0x0008, MathHelper.randomRange(1500, 2500));
    }

    private void touchBackButton() {
        _touchInjector.touchKeyboard(0x0008);
    }
}
