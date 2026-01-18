package com.sec.android.app.sbrowser.pattern.google.rank;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.pattern.action.RankResultAction;
import com.sec.android.app.sbrowser.pattern.google.GooglePatternMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class GoogleRankPatternMessage extends GooglePatternMessage {

    private static final String TAG = GoogleRankPatternMessage.class.getSimpleName();

    private static final int MAX_PAGE_COUNT = 7;

    private static final int GO_GOOGLE = 50;
    private static final int CHECK_RANK = GO_GOOGLE + 1;

    private final GoogleViewRankAction _viewRankAction;
    private final RankResultAction _resultPatternAction;

    private KeywordItemMoon _item;
    private String _parsedKeyword = "";
    private int _page = 0;

    public GoogleRankPatternMessage(WebViewManager manager, KeywordItemMoon item) {
        super(manager);

        _item = item;

        _viewRankAction = new GoogleViewRankAction(manager.getWebView());
        _resultPatternAction = new RankResultAction();
        _resultPatternAction.loginId = UserManager.getInstance().getLoginId(manager.getWebView().getContext());
        _resultPatternAction.item = item;
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 구글 순위 검사 작업 시작");
                _parsedKeyword = _item.keyword;

                try {
                    _parsedKeyword = URLEncoder.encode(_parsedKeyword, "UTF-8");
                } catch (UnsupportedEncodingException e) {

                }

                _handler.sendEmptyMessage(GO_HOME);
                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 구글 결과로 이동");
                webViewLoad(msg, "https://www.google.com/search?q=" + _parsedKeyword);
                break;
            }

            case CHECK_RANK: {
                Log.d(TAG, "# 구글 순위 검사");
                if (_viewRankAction.checkRank(_item.url)) {
                    if (_viewRankAction.getRank() > 0) {
                        Log.d(TAG, "# 구글 순위 검사 성공");
                        // 순위 업로드.
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                    } else {
                        if (_page < MAX_PAGE_COUNT) {
                            ++_page;

                            if (_viewRankAction.checkMoreButton()) {
                                Log.d(TAG, "# 순위를 못찾아서 더보기 터치.. " + _page);
                                _viewRankAction.clickMoreButton();
                                _handler.sendEmptyMessageDelayed(CHECK_RANK, MathHelper.randomRange(3000, 5000));
                            } else {
                                Log.d(TAG, "# 더보기 버튼 못찾아서 패턴종료.");
                                sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                            }

                        } else {
                            Log.d(TAG, "# 구글 순위 못찾아서 패턴종료.");
                            sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                        }
                    }
                } else {
                    Log.d(TAG, "# 구글 순위 검사 실패로 5초후 다시 시도..." + _retryCount);
                    if (!resendMessageDelayed(msg.what, 5000, 3)) {
                        Log.d(TAG, "# 구글 순위 검사 실패로 패턴종료.");
                        sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                    }
                }
                break;
            }

            case END_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# 순위 검사 패턴 종료");
                _webViewManager.goBlankPage();
                registerFinish();
                _viewRankAction.endPattern();
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
            case GO_HOME: {
                Log.d(TAG, "# 구글 이동 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_RANK, MathHelper.randomRange(5000, 6000));
                break;
            }
        }

        _lastMessage = -1;
    }

    protected void registerFinish() {
        _resultPatternAction.registerFinish(_viewRankAction.getRank(), 0);
    }
}
