package com.sec.android.app.sbrowser.pattern.coupang.rank;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.pattern.action.RankResultAction;
import com.sec.android.app.sbrowser.pattern.coupang.CoupangPatternMessage;
import com.sec.android.app.sbrowser.pattern.coupang.action.CoupangHomeAction;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CoupangRankPatternMessage extends CoupangPatternMessage {

    private static final String TAG = CoupangRankPatternMessage.class.getSimpleName();

    private static final int MAX_PAGE_COUNT = 10;
    private static final int MAX_PAGE_PC_COUNT = 5;

    private static final int GO_HOME_PC_REDIRECT = 49;
    private static final int GO_HOME_PC = 50;
    private static final int CHECK_RANK = GO_HOME_PC + 1;
    private static final int CHECK_RANK_PC = GO_HOME_PC + 2;

    private final CoupangHomeAction _homeAction;
    private final CoupangViewRankAction _viewRankAction;
    private final CoupangPcRankAction _pcRankAction;
    private final RankResultAction _resultPatternAction;

    private KeywordItemMoon _item;
    private String _parsedKeyword = "";
    private int _page = 0;
    private int _nextPopupMessage = 0;
    private boolean _success = false;

    public CoupangRankPatternMessage(WebViewManager manager, KeywordItemMoon item) {
        super(manager);

        _item = item;

        _homeAction = new CoupangHomeAction(manager.getWebView());
        _viewRankAction = new CoupangViewRankAction(manager.getWebView());
        _pcRankAction = new CoupangPcRankAction(manager.getWebView());
        _resultPatternAction = new RankResultAction();
        _resultPatternAction.loginId = UserManager.getInstance().getLoginId(manager.getWebView().getContext());
        _resultPatternAction.item = item;
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 쿠팡 순위 검사 작업 시작");
                _parsedKeyword = _item.keyword;

                try {
                    _parsedKeyword = URLEncoder.encode(_parsedKeyword, "UTF-8");
                } catch (UnsupportedEncodingException e) {

                }

                if (_item.category.equals("coupang")) {
                    _handler.sendEmptyMessage(GO_HOME);
                } else if (_item.category.equals("coupang_pc")) {
                    _handler.sendEmptyMessage(GO_HOME_PC);
//                    _handler.sendEmptyMessage(GO_HOME_PC_REDIRECT);
                } else {
                    Log.d(TAG, "# 알수 없는 타입 패턴종료.");
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                }

                break;
            }

            case GO_HOME: {
                Log.d(TAG, "# 쿠팡 결과로 이동");
                _page = 1;
                webViewLoad(msg, "https://m.coupang.com/nm/search?q=" + _parsedKeyword);
                break;
            }

            case TOUCH_MOBILE_WEB_POPUP: {
                Log.d(TAG, "# 모바일웹 보기 팝업 검사");

                if (_homeAction.checkFullBanner()) {
                    if (_homeAction.touchButton(CoupangHomeAction.BUTTON_MOBILE_WEB)) {
                        _handler.sendEmptyMessageDelayed(_nextPopupMessage, MathHelper.randomRange(2500, 3500));
                    } else {
                        _handler.sendEmptyMessageDelayed(_nextPopupMessage, 1000);
                    }
                } else {
                    _handler.sendEmptyMessageDelayed(_nextPopupMessage, 100);
                }
                break;
            }

            case CHECK_RANK: {
                Log.d(TAG, "# 쿠팡 순위 검사");
                String currentPage =  _viewRankAction.getCurrentPage();
                if (currentPage == null) {
                    Log.d(TAG, "# 현재 페이지를 못찾아서 패턴종료.");
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                } else if (Integer.parseInt(currentPage) > MAX_PAGE_COUNT) {
                    Log.d(TAG, "# " + MAX_PAGE_COUNT + "페이지 초과로 패턴종료.");
                    _success = true;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    int page = Integer.parseInt(currentPage);
                    if (_viewRankAction.checkRank(_item.code, page)) {
                        if (_viewRankAction.getRank() > 0) {
                            Log.d(TAG, "# 쿠팡 순위 검사 성공");
                            // 순위 업로드.
                            _success = true;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                        } else {
                            if (_viewRankAction.checkNextButton()) {
                                Log.d(TAG, "# 순위를 못찾아서 다음 버튼 터치.. " + _page);
                                _viewRankAction.clickNextButton();
                                webViewLoading(msg);
                            } else {
                                Log.d(TAG, "# 다음 버튼 못찾아서 패턴종료.");
                                sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                            }
                        }
                    } else {
                        Log.d(TAG, "# 쿠팡 순위 검사 실패로 5초후 다시 시도..." + _retryCount);
                        if (!resendMessageDelayed(msg.what, 5000, 3)) {
                            Log.d(TAG, "# 쿠팡 순위 검사 실패로 패턴종료.");
                            sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                        }
                    }
                }
                break;
            }

            case GO_HOME_PC_REDIRECT: {
                Log.d(TAG, "# 쿠팡PC PC모드로 이동");
                _page = 1;
//                https://www.coupang.com/np/search?component=&q= &channel=user&&listSize=72
//                https://www.coupang.com/np/search?q= &brand=&offerCondition=&filter=&availableDeliveryFilter=&filterType=&isPriceRange=false&priceRange=&minPrice=&maxPrice=&page=1&trcid=&traid=&filterSetByUser=true&channel=user&backgroundColor=&searchProductCount=263455&component=&rating=0&sorter=scoreDesc&listSize=72

//                webViewLoad(msg, "https://www.coupang.com/np/search?component=&q=" + _parsedKeyword + "&channel=user&listSize=72");
                webViewLoad(msg, "https://m.coupang.com/nm/redirect/pcHome");
                break;
            }

            case GO_HOME_PC: {
                Log.d(TAG, "# 쿠팡PC 결과로 이동");
                _page = 1;
//                https://www.coupang.com/np/search?component=&q= &channel=user&&listSize=72
//                https://www.coupang.com/np/search?q= &brand=&offerCondition=&filter=&availableDeliveryFilter=&filterType=&isPriceRange=false&priceRange=&minPrice=&maxPrice=&page=1&trcid=&traid=&filterSetByUser=true&channel=user&backgroundColor=&searchProductCount=263455&component=&rating=0&sorter=scoreDesc&listSize=72

                webViewLoad(msg, "https://www.coupang.com/np/search?component=&q=" + _parsedKeyword + "&channel=user&listSize=72");
                break;
            }

            case CHECK_RANK_PC: {
                Log.d(TAG, "# 쿠팡PC 순위 검사");
                String currentPage =  _pcRankAction.getCurrentPage();

                if (!_pcRankAction.hasPagination()) {
                    currentPage = "1";
                }

                if (currentPage == null) {
                    Log.d(TAG, "# 현재 페이지를 못찾아서 패턴종료.");
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                } else if (Integer.parseInt(currentPage) > MAX_PAGE_PC_COUNT) {
                    Log.d(TAG, "# " + MAX_PAGE_PC_COUNT + "페이지 초과로 패턴종료.");
                    _success = true;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    int page = Integer.parseInt(currentPage);
                    if (_pcRankAction.checkRank(_item.code, page)) {
                        if (_pcRankAction.getRank() > 0) {
                            Log.d(TAG, "# 쿠팡PC 순위 검사 성공");
                            // 순위 업로드.
                            _success = true;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                        } else {
                            if (_pcRankAction.checkNextButton()) {
                                Log.d(TAG, "# 순위를 못찾아서 다음 버튼 터치.. " + _page);
                                _pcRankAction.clickNextButton();
                                webViewLoading(msg);
                            } else {
                                Log.d(TAG, "# 다음 버튼 못찾아서 패턴종료.");
                                sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                            }
                        }
                    } else {
                        Log.d(TAG, "# 쿠팡PC 순위 검사 실패로 5초후 다시 시도..." + _retryCount);
                        if (!resendMessageDelayed(msg.what, 5000, 3)) {
                            Log.d(TAG, "# 쿠팡PC 순위 검사 실패로 패턴종료.");
                            sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                        }
                    }
                }
                break;
            }

            case END_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# 쿠팡 검사 패턴 종료");
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
                Log.d(TAG, "# 쿠팡 이동 후 동작");
                _nextPopupMessage = CHECK_RANK;
                _handler.sendEmptyMessageDelayed(TOUCH_MOBILE_WEB_POPUP, MathHelper.randomRange(5000, 6000));
                break;
            }

            case GO_HOME_PC_REDIRECT: {
                Log.d(TAG, "# 쿠팡PC PC모드로 이동 후 동작");
                _handler.sendEmptyMessageDelayed(GO_HOME_PC, MathHelper.randomRange(10000, 11000));
                break;
            }

            case GO_HOME_PC: {
                Log.d(TAG, "# 쿠팡PC 이동 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_RANK_PC, MathHelper.randomRange(7000, 8000));
                break;
            }

            case CHECK_RANK: {
                Log.d(TAG, "# 다음 버튼 터치 후 동작");
                _nextPopupMessage = CHECK_RANK;
                _handler.sendEmptyMessageDelayed(TOUCH_MOBILE_WEB_POPUP, MathHelper.randomRange(5000, 6000));
//                _handler.sendEmptyMessageDelayed(CHECK_RANK, MathHelper.randomRange(5000, 6000));
                break;
            }

            case CHECK_RANK_PC: {
                Log.d(TAG, "# 쿠팡PC 다음 버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_RANK_PC, MathHelper.randomRange(7000, 8000));
//                _handler.sendEmptyMessageDelayed(CHECK_RANK, MathHelper.randomRange(5000, 6000));
                break;
            }
        }

        _lastMessage = -1;
    }

    protected void registerFinish() {
        if (!_success) {
            Log.d(TAG, "# 순위 검사 실패로 서버에 등록하지 않고 패스.");
            return;
        }

        if (_item.category.equals("coupang")) {
            _resultPatternAction.registerFinish(_viewRankAction.getRank(), 0);
        } else if (_item.category.equals("coupang_pc")) {
            _resultPatternAction.registerFinish(_pcRankAction.getRank(), 0);
        }
    }
}
