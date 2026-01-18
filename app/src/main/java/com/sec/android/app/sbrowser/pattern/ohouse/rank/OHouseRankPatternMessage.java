package com.sec.android.app.sbrowser.pattern.ohouse.rank;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.library.ohouse.retrofit.models.FeedData;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.pattern.action.RankResultAction;
import com.sec.android.app.sbrowser.pattern.coupang.CoupangPatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.rank.NaverProductInfoUpdateAction;

public class OHouseRankPatternMessage extends CoupangPatternMessage {

    private static final String TAG = OHouseRankPatternMessage.class.getSimpleName();

    private static final int MAX_PAGE_COUNT = 7;
    private static final int MAX_PAGE_PC_COUNT = 5;

    private static final int GET_FEEDS = 50;
    private static final int UPLOAD_SHOP_STORE_INFO = GET_FEEDS + 1;

    private final OHouseFeedAction _feedAction;
    protected final NaverProductInfoUpdateAction _productInfoUpdateAction;
    private final RankResultAction _resultPatternAction;

    private KeywordItemMoon _item;
    private String _parsedKeyword = "";
    private int _page = 0;
    private int _nextPopupMessage = 0;
    private int _prevCount = 0;
    private int _rank = -1;
    private boolean _success = false;

    public OHouseRankPatternMessage(WebViewManager manager, KeywordItemMoon item) {
        super(manager);

        _item = item;

        _feedAction = new OHouseFeedAction();

        _productInfoUpdateAction = new NaverProductInfoUpdateAction();
        _productInfoUpdateAction.loginId = UserManager.getInstance().getLoginId(manager.getWebView().getContext());
        _productInfoUpdateAction.item = item;

        _resultPatternAction = new RankResultAction();
        _resultPatternAction.loginId = UserManager.getInstance().getLoginId(manager.getWebView().getContext());
        _resultPatternAction.item = item;
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 오늘의집 순위 검사 작업 시작");
                _parsedKeyword = _item.keyword;

//                try {
//                    _parsedKeyword = URLEncoder.encode(_parsedKeyword, "UTF-8");
//                } catch (UnsupportedEncodingException e) {
//
//                }

                if (_item.category.equals("ohouse")) {
                    _page = 1;
                    _prevCount = 0;
                    _handler.sendEmptyMessage(GET_FEEDS);
//                } else if (_item.category.equals("ohouse_pc")) {
//                    _handler.sendEmptyMessage(GO_HOME_PC);
                } else {
                    Log.d(TAG, "# 알수 없는 타입 패턴종료.");
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                }
                break;
            }

            case GET_FEEDS: {
                Log.d(TAG, "# 오늘의집 글 가져오기 Page" + _page);
                int result = _feedAction.requestFeed(_parsedKeyword, _page);

                if (result == 1) {
                    int rank = _feedAction.getProductionRank(_item.item.code);

                    if (rank > 0) {
                        Log.d(TAG, "# 오늘의집 순위 검사 성공: " + rank + "위");
                        _rank = _prevCount + rank;
                        // 순위 업로드.
                        _success = true;
                        _handler.sendEmptyMessageDelayed(UPLOAD_SHOP_STORE_INFO, 500);
                    } else if (_page > MAX_PAGE_COUNT) {
                        Log.d(TAG, "# " + MAX_PAGE_COUNT + "페이지 초과로 패턴종료.");
                        _success = true;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    } else {
                        _prevCount += _feedAction.getProductionCount();
                        ++_page;
                        Log.d(TAG, "# 순위를 못찾아서 다음 페이지로... " + _page + ": 누적 " + _prevCount + "개");

                        sendMessageDelayed(GET_FEEDS, MathHelper.randomRange(1500, 3000));
                    }
                } else {
                    Log.d(TAG, "# 글 가져오기 실패로 패턴 종료...");
                    sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                }
                break;
            }

            case UPLOAD_SHOP_STORE_INFO: {
                FeedData.Production production = _feedAction.getProduction(_item.item.code);
                String productName = production.name;
                String storeName = production.brandName;
                Log.d(TAG, "# 상점 정보 등록 - 상품명/판매자명: " + productName + "/" + storeName);
                _productInfoUpdateAction.registerInfo(productName, storeName, null, null, null, null, null);
                Log.d(TAG, "# 샵 스토어 정보 등록 완료로 패턴종료.");
                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                break;
            }

            case END_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# 오늘의집 검사 패턴 종료");
                _webViewManager.goBlankPage();
                registerFinish();
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
//                Log.d(TAG, "# 오늘의집 이동 후 동작");
//                _nextPopupMessage = CHECK_RANK;
//                _handler.sendEmptyMessageDelayed(TOUCH_MOBILE_WEB_POPUP, MathHelper.randomRange(5000, 6000));
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

        if (_item.category.equals("ohouse")) {
            _resultPatternAction.registerFinish(_rank, 0);
        } else if (_item.category.equals("ohouse_pc")) {
            _resultPatternAction.registerFinish(_rank, 0);
        }
    }
}
