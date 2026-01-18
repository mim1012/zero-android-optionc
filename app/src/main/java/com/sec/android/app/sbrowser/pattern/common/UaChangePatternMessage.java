package com.sec.android.app.sbrowser.pattern.common;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.models.NnbData;
import com.sec.android.app.sbrowser.pattern.action.UaAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;

public class UaChangePatternMessage extends NaverPatternMessage {

    private static final String TAG = UaChangePatternMessage.class.getSimpleName();

    public static final int GET_UA = 5200;

    public boolean isPc = false;

    private int _retryCount = 0;
    private NnbData _nnbData = null;

    public UaChangePatternMessage(WebViewManager manager) {
        super(manager);
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# UA 설정 패턴 시작");
//                sendMessageDelayed(TOUCH_URL_BAR, 5000);
                _handler.sendEmptyMessage(GET_UA);
                break;
            }

            case GET_UA: {
                Log.d(TAG, "# ua 가져오기.");
                UaAction action = new UaAction();
                action.loginId = UserManager.getInstance().getLoginId(_webViewManager.getWebView().getContext());
                int result;

                if (isPc) {
                    result = action.requestUaPc();
                } else {
                    result = action.requestUa();
                }

                if (result == 1) {
                    _nnbData = action.getNnbData();
                    String ua = action.getUserAgent();
                    UserManager.getInstance().nnbData = _nnbData;

                    if ((ua != null) && (ua.length() > 0)) {
                        Log.d(TAG, "# UA 변경 시도: " + _webViewManager.getUserAgentString());
//                        _webViewManager.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
                        _webViewManager.setUserAgentString(ua);
                        UserManager.getInstance().ua = ua;

                        if (_nnbData != null) {
                            UserManager.getInstance().chromeVersion = _nnbData.chromeVersion;
                            UserManager.getInstance().browserVersion = _nnbData.browserVersion;
                        }

                        Log.d(TAG, "# UA 변경 완료: " + _webViewManager.getUserAgentString());
                    }

                    sendMessageDelayed(END_PATTERN, 100);
                } else {
                    Log.d(TAG, "# UA 가져오기 실패로 패턴 종료...");
                    sendMessageDelayed(END_PATTERN, 500);
                }
                break;
            }

            case END_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# UA 설정 패턴 종료");
                sendEndPatternMessage();
                break;
            }

            case PAUSE_PATTERN: {
                Log.d(TAG, "# 패턴 중단");
                break;
            }
        }
    }

    @Override
    public void onPageLoaded(String url) {
        super.onPageLoaded(url);

        switch (_lastMessage) {
            default:
                break;
        }

        _lastMessage = -1;
    }

    protected void sendMessageDelayed(int what, long delayMillis) {
        _retryCount = 0;
        _handler.sendEmptyMessageDelayed(what, delayMillis);
    }

    protected boolean resendMessageDelayed(int what, long delayMillis, int maxRetryCount) {
        if (_retryCount < maxRetryCount) {
            ++_retryCount;
            _handler.sendEmptyMessageDelayed(what, delayMillis);
            return true;
        }

        return false;
    }
}
