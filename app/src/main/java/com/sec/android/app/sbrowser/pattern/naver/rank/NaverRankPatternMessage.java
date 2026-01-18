package com.sec.android.app.sbrowser.pattern.naver.rank;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.Config;
import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpCookieController;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpEngine;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.keyboard.SamsungKeyboard;
import com.sec.android.app.sbrowser.keyboard.TouchInjector;
import com.sec.android.app.sbrowser.library.naver.retrofit.models.ShoppingSearchData;
import com.sec.android.app.sbrowser.models.KeywordItem;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.models.NnbData;
import com.sec.android.app.sbrowser.pattern.action.NnbAction;
import com.sec.android.app.sbrowser.pattern.action.RankResultAction;
import com.sec.android.app.sbrowser.pattern.action.SwipeThreadAction;
import com.sec.android.app.sbrowser.pattern.action.UaAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.shop.NaverShopPageAction;
import com.sec.android.app.sbrowser.pattern.naver.shop.NaverShopSearchBarAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringTokenizer;

public class NaverRankPatternMessage extends NaverPatternMessage {

    private static final String TAG = NaverRankPatternMessage.class.getSimpleName();

    private static final int MAX_PAGE_COUNT = 7;
    private static final int MAX_SHOP_PAGE_COUNT = 7;
//    private static final int MAX_SHOP_PAGE_API_COUNT = 13;
    private static final int MAX_SHOP_PAGE_API_COUNT = 25;
    private static final int MAX_SHOP_MID2_PAGE_COUNT = 10;
    private static final int MAX_SCROLL_COUNT = 7;

    private static final int GO_COOKIE_PAGE = 40;
    private static final int GO_ACTION = 41;
    private static final int PAGE_RELOAD = 42;
    private static final int GET_SHOP_PRODUCTS = 45;
    private static final int CHECK_SHOP_ERROR_PAGE = 49;
    private static final int GO_SHOP = 50;
    private static final int CHECK_SHOP_RANK_MID1 = GO_SHOP + 1;
    private static final int SCROLL_SHOP_BOTTOM = GO_SHOP + 2;
    private static final int CHECK_ITEM_LOADED = GO_SHOP + 3;
    private static final int CLICK_SHOP_NEXT_BUTTON = CHECK_ITEM_LOADED + 1;
    private static final int CLICK_SHOP_CONTENT = CHECK_ITEM_LOADED + 2;
    private static final int GET_SHOP_PRODUCT_INFO = CHECK_ITEM_LOADED + 3;
    private static final int GET_SHOP_PRODUCT_ID = CHECK_ITEM_LOADED + 4;
    private static final int GO_SHOP_PRE_MID2 = CHECK_ITEM_LOADED + 5;
    private static final int CHECK_SHOP_DETAIL_URL = CHECK_ITEM_LOADED + 6;
    private static final int GET_MID2_API = CHECK_ITEM_LOADED + 7;
    private static final int GO_SHOP_MID2 = CHECK_ITEM_LOADED + 8;
    private static final int CHECK_SHOP_RANK_MID2 = GO_SHOP_MID2 + 1;
    private static final int SCROLL_SHOP_BOTTOM_MID2 = GO_SHOP_MID2 + 2;
    private static final int CLICK_SHOP_CONTENT_MID2 = GO_SHOP_MID2 + 3;
    private static final int GET_SHOP_SMART_STORE_INFO = GO_SHOP_MID2 + 4;
    private static final int UPLOAD_SHOP_STORE_INFO = GO_SHOP_MID2 + 5;

    private static final int CREATE_SHOP_KEYWORD = UPLOAD_SHOP_STORE_INFO + 1;
    private static final int CHECK_SHOP_KEYWORD = CREATE_SHOP_KEYWORD + 1;

    private static final int GO_SHOP_HOME = 80;
    private static final int TOUCH_SHOP_HOME_SEARCH_BUTTON = 81;
    private static final int TOUCH_SHOP_CONTENT = TOUCH_SHOP_HOME_SEARCH_BUTTON + 1;

    private static final int GET_SHOP_PACKET = 90;
//    private static final int GET_SHOP_PACKET = 90;
    private static final int GO_PLACE = 100;
    private static final int CLICK_PLACE_OPEN_MORE_BUTTON = GO_PLACE + 1;
    private static final int CLICK_PLACE_MORE_BUTTON = GO_PLACE + 2;
    private static final int CHECK_PLACE_RANK = GO_PLACE + 3;
    private static final int SCROLL_PLACE_BOTTOM = GO_PLACE + 4;
    private static final int GET_PLACE_INFO = GO_PLACE + 5;
    private static final int UPLOAD_PLACE_INFO = GO_PLACE + 6;

    private static final int GO_SITE = 150;
    private static final int CHECK_SITE_RANK = GO_SITE + 1;

    private static final int GO_VIEW = 200;
    private static final int CHECK_VIEW_RANK = GO_VIEW + 1;
    private static final int SCROLL_VIEW_BOTTOM = GO_VIEW + 2;

    private final NaverShopPageAction _shopPageAction;
    private final NaverShopProductsAction _shopProductsAction;
    private final NaverShopRankAction _shopRankAction;
    private final NaverPlaceRankAction _placeRankAction;
    private final NaverSiteRankAction _siteRankAction;
    private final NaverViewRankAction _viewRankAction;

    protected final NaverShopSearchBarAction _shopSearchBarAction;
    protected final NaverProductInfoUpdateAction _productInfoUpdateAction;
    protected final RankResultAction _resultPatternAction;

    private final SwipeThreadAction _swipeAction;

    private HttpCookieController _cookieController;
    private HttpEngine _httpEngine;

    private KeywordItemMoon _item;
    private String _parsedKeyword = "";
    private int _page = 0;
    private int _page2 = 0;
    private int _scrollCount = 0;
    private int _nextMessage = 0;
    private int _reloadMessage = 0;
    private boolean _success = false;

    private String _productName = null;
    private String _storeName = null;
    private String _mallId = null;
    private String _catId = null;
    private String _productUrl = null;
    private String _sourceType = null;
    private String _sourceUrl = null;
    private String _generatedKeyword = null;
    private int _step = 0;
    private boolean _isMore = false;
    private int _prevNodeCount = 0;
    private int _itemCheckCount = 0;
    private int _shopProductRank = 0;

    private int _findBarCount = 0;
    private int _waitCount = 0;

    public NaverRankPatternMessage(WebViewManager manager, KeywordItemMoon item) {
        super(manager);

        _item = item;

        _shopPageAction = new NaverShopPageAction(manager.getWebView());
        _shopProductsAction = new NaverShopProductsAction();
        _shopRankAction = new NaverShopRankAction(manager.getWebView());
        _placeRankAction = new NaverPlaceRankAction(manager.getWebView());
        _siteRankAction = new NaverSiteRankAction(manager.getWebView());
        _viewRankAction = new NaverViewRankAction(manager.getWebView());

        TouchInjector touchInjector = new TouchInjector(manager.getWebView().getContext());
        touchInjector.setSoftKeyboard(new SamsungKeyboard());
        _swipeAction = new SwipeThreadAction(touchInjector);

        _shopSearchBarAction = new NaverShopSearchBarAction(manager.getWebView());

        _productInfoUpdateAction = new NaverProductInfoUpdateAction();
        _productInfoUpdateAction.loginId = UserManager.getInstance().getLoginId(manager.getWebView().getContext());
        _productInfoUpdateAction.item = item;

        _resultPatternAction = new RankResultAction();
        _resultPatternAction.loginId = UserManager.getInstance().getLoginId(manager.getWebView().getContext());
        _resultPatternAction.item = item;

        _cookieController = new HttpCookieController();
        _httpEngine = new HttpEngine(_webViewManager.getWebView().getContext());
        _httpEngine.setCookieController(_cookieController);
        _httpEngine.setUa(UserManager.getInstance().ua);
        _httpEngine.setChromeVersion(UserManager.getInstance().chromeVersion);
        _httpEngine.setBrowserVersion(UserManager.getInstance().browserVersion);
        _httpEngine.setNnb(UserManager.getInstance().nnb);

        _shopProductsAction.setHttpEngine(_httpEngine);
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 순위 검사 작업 시작");

                if (_item.category.equals("nshop") || _item.category.equals("nshop_pc") || _item.category.equals("nshop_cr")) {
                    _parsedKeyword = WebViewManager.keywordEncodeForNaverInclPlus(_item.keyword);
                } else if (_item.category.equals("nplace")) {
                    _parsedKeyword = WebViewManager.keywordEncodeForNaverInclPlus(_item.keyword);
                } else {
//                    if (_item.category.equals("nfplace") || _item.category.equals("nplace")) {
//                        _parsedKeyword = _item.keyword.replaceAll("\\s+","");
////                        _parsedKeyword = _item.keyword.replace(" ", "");
////                        _parsedKeyword = removeAllWhiteSpace(_item.keyword);
//                    } else {
                        _parsedKeyword = _item.keyword;
//                    }

                    try {
                        _parsedKeyword = URLEncoder.encode(_parsedKeyword, "UTF-8");
                    } catch (UnsupportedEncodingException e) {

                    }
                }

                _handler.sendEmptyMessage(GO_COOKIE_PAGE);
//                _handler.sendEmptyMessage(GO_ACTION);
                break;
            }

            case GO_COOKIE_PAGE: {
                Log.d(TAG, "# 쿠키 페이지 이동");
//                if (_item.category.equals("nfplace")) {
//                    // 플레이스 [많이찾는]
//                    _handler.sendEmptyMessage(GO_PLACE);
//                } else if (_item.category.equals("nplace")) {
//                    _handler.sendEmptyMessage(GO_PLACE);
//                } else if (_item.category.equals("nshop")) {
                if (_item.category.equals("nshop") || _item.category.equals("nshop_cr")) {
                    boolean runApi = false;

                    if (!hasCookie()) {
                        if (!getCookie()) {
                            // 일단은. 서버에서 쿠키 못가져와도 고정값으로 처리되게 변경.
//                            runApi = false;
                            UserManager.getInstance().nnb = "EA3DMJJWQS7GK";
                        }
                    }

                    if (runApi) {
                        setCookie();
                        _httpEngine.resetClient();
                        _handler.sendEmptyMessage(GET_SHOP_PRODUCTS);
                    } else {
//                        int type = (int) MathHelper.randomRange(0, 1);
                        int type = 0;

                        switch (type) {
                            case 1:
                                webViewLoad(msg, "https://search.shopping.naver.com/home");
//                                webViewLoad(msg, "https://shopping.naver.com/"); // 플러스스토어
                                break;
    //                        case 2:
    //                            webViewLoad(msg, "https://m.naver.com/");
    //                            break;
                            default:
                                webViewLoad(msg, "https://m.naver.com/");
                                break;
                        }

//                    webViewLoad(msg, "https://shopping.naver.com/");
//                    _handler.sendEmptyMessage(GO_ACTION);
                    }

                } else if (_item.category.equals("nshop_pc")) {
//                    webViewLoad(msg, "https://shopping.naver.com/home");
                    _handler.sendEmptyMessage(GO_ACTION);
//                } else if (_item.category.equals("site")) {
//                    _page = 1;
//                    _handler.sendEmptyMessage(GO_SITE);
//                } else if (_item.category.equals("nview")) {
//                    _page = 1;
//                    _handler.sendEmptyMessage(GO_VIEW);
                } else {
                    _webViewManager.setLoadsImagesAutomatically(true);
                    webViewLoad(msg, Config.NAVER_LOGIN_URL);
                }
                break;
            }

            case GO_ACTION: {
                Log.d(TAG, "# 액션 시작");
                _webViewManager.setLoadsImagesAutomatically(false);

                if (_item.category.equals("nfplace")) {
                    // 플레이스 [많이찾는]
                    _handler.sendEmptyMessage(GO_PLACE);
                } else if (_item.category.equals("nplace")) {
                    _handler.sendEmptyMessage(GO_PLACE);
                } else if (_item.category.equals("nshop") || _item.category.equals("nshop_cr")) {
                    _page = 1;
                    _page2 = 1;
                    _handler.sendEmptyMessage(GO_SHOP);
//                    _handler.sendEmptyMessage(GO_SHOP_HOME);
                } else if (_item.category.equals("nshop_pc")) {
                    _page = 1;
                    _page2 = 1;
                    _handler.sendEmptyMessage(GO_SHOP);
//                    _handler.sendEmptyMessage(GO_SHOP_HOME);
                } else if (_item.category.equals("site")) {
                    _page = 1;
                    _handler.sendEmptyMessage(GO_SITE);
                } else if (_item.category.equals("nview")) {
                    _page = 1;
                    _handler.sendEmptyMessage(GO_VIEW);
                } else {
                    Log.d(TAG, "# 알수 없는 타입 패턴종료.");
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 5000);
                }

//                _handler.sendEmptyMessage(GO_HOME);

                break;
            }

            case PAGE_RELOAD: {
                Log.d(TAG, "# 페이지 다시 이동");
                _handler.sendEmptyMessage(_reloadMessage);
                break;
            }

            // ### shop
            case GET_SHOP_PRODUCTS: {
                Log.d(TAG, "# 쇼핑 상품 가져오기 Page" + _page);
//                String sbth = getSbth();
                _httpEngine.setUseGenHeader(false);
//                int result = _shopProductsAction.requestProducts(sbth, _parsedKeyword, _page + 1);
                int result = _shopProductsAction.requestProducts(null, _parsedKeyword, _page + 1);
                _httpEngine.setUseGenHeader(true);

                if (result == 1) {
                    int maxPage = MAX_SHOP_PAGE_API_COUNT;
                    int rank;

                    if (!TextUtils.isEmpty(_item.url)) {
                        rank = _shopProductsAction.getProductRankForPid(_item.url);
                    } else {
                        rank = _shopProductsAction.getProductRank(_item.mid1);
                    }

                    if (rank > 0) {
                        Log.e(TAG, "# 쇼핑 MID1 순위 검사 성공: " + rank);
                        _shopRankAction.setRank(rank);
                        ShoppingSearchData.Product product;

                        if (!TextUtils.isEmpty(_item.url)) {
                            product = _shopProductsAction.getProductForPid(_item.url);
                        } else {
                            product = _shopProductsAction.getProduct(_item.mid1);
                        }

                        if (product != null) {
                            String mallId = product.mallNo;
                            if (!TextUtils.isEmpty(mallId) && !mallId.equals("0")) {
                                _mallId = mallId;
                            }

                            // catId 얻기.
                            _catId = product.category4Id;

                            if (_item.mid2.length() > 0 && !_item.mid2.equals(".")) {
                                if (TextUtils.isEmpty(_item.item.code3)) {
                                    webViewLoad(msg, product.crUrl);
                                } else {
                                    _handler.sendEmptyMessageDelayed(GO_SHOP_MID2, 100);
                                }
//                                _handler.sendEmptyMessageDelayed(GO_SHOP_PRE_MID2, MathHelper.randomRange(2500, 3500));
                            } else {
                                _productName = product.productTitle;
                                _storeName = product.mallName;
                                _sourceType = "0";
                                _sourceUrl = product.imageUrl;

                                _success = true;
                                _handler.sendEmptyMessageDelayed(UPLOAD_SHOP_STORE_INFO, 100);
                            }
                        } else {
                            Log.d(TAG, "# 쇼핑 상품 정보 가져오기 실패로 패턴 종료...");
                            sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                        }
                    } else if (_page < (maxPage - 1)) {
                        int productCount = _shopProductsAction.getProductCount();

                        if (productCount == 40) {
                            ++_page;
                            Log.d(TAG, "# 순위를 못찾아서 다음으로.. " + _page);
                            sendMessageDelayed(GET_SHOP_PRODUCTS, MathHelper.randomRange(200, 1000));
                        } else {
                            Log.d(TAG, "# 상품 개수가 적어서 패턴 종료..." + productCount + "개");
                            sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                        }
                    } else {
                        Log.d(TAG, "# " + maxPage + "페이지 초과로 패턴종료.");
                        _success = true;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    }
                } else {
                    int response = _httpEngine.getResponseCode();
                    int nextMessage = msg.what;
                    Log.e(TAG, "# 쇼핑 상품 순위 가져오기 실패(" + response + ")로 2초후 다시 시도..." + _retryCount);

                    if (response == 418) {
                        nextMessage = GO_COOKIE_PAGE;
                    }

                    if (!resendMessageDelayed(nextMessage, 1000, 30)) {
                        Log.d(TAG, "# 쇼핑 상품 가져오기 실패로 패턴 종료...");
                        sendMessageDelayed(END_PATTERN, 500);
                    }
                }
                break;
            }

            case CHECK_SHOP_ERROR_PAGE: {
                Log.d(TAG, "# 에러 페이지 검사");
                if (!_shopRankAction.checkErrorPage()) {
                    _handler.sendEmptyMessage(_nextMessage);
                } else {
                    Log.d(TAG, "# 에러 페이지여서 15초후 다시 시도..." + _retryCount);
                    if (!resendMessageDelayed(PAGE_RELOAD, 15000, 3)) {
                        Log.d(TAG, "# 에러 페이지여서 패턴종료.");
                        sendMessageDelayed(END_PATTERN, 500);
                    }
                }
                break;
            }

            case GO_SHOP: {
                Log.d(TAG, "# 쇼핑 페이지로 이동");
                if (_page < 2) {
                    webViewLoad(msg, "https://msearch.shopping.naver.com/search/all?query=" + _parsedKeyword);
                } else {
                    webViewLoad(msg, "https://msearch.shopping.naver.com/search/all?query=" + _parsedKeyword + "&pagingIndex=" + _page);
                }
                break;
            }

            case CHECK_SHOP_RANK_MID1: {
                Log.d(TAG, "# 쇼핑 MID1 순위 검사");
                if (_shopRankAction.isNoResult()) {
                    Log.d(TAG, "# 결과가 없어서 패턴종료.");
                    _success = true;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    if (_shopRankAction.checkRank(_item.mid1, _page)) {
                        if (_shopRankAction.getRank() > 0) {
                            Log.d(TAG, "# 쇼핑 MID1 순위 검사 성공");
                            String mallId = _shopRankAction.getMallId(_item.mid1);
                            if (!TextUtils.isEmpty(mallId) && !mallId.equals("0")) {
                                _mallId = mallId;
                            }

                            // catId 얻기.
                            String href = _shopRankAction.getHrefString(_item.mid1);
                            String[] parts = href.split("&");

                            for (String part : parts) {
                                if (part.startsWith("catId=")) {
                                    _catId = part.replace("catId=", "").trim();
                                }
                            }

                            if (_item.mid2.length() > 0 && !_item.mid2.equals(".")) {
                                _shopProductRank = 0;
                                _handler.sendEmptyMessageDelayed(GET_MID2_API, 1000);

//                                if (TextUtils.isEmpty(_item.item.code3)) {
//                                    _handler.sendEmptyMessageDelayed(CLICK_SHOP_CONTENT, 100);
//                                } else {
//                                    _handler.sendEmptyMessageDelayed(GO_SHOP_MID2, 100);
//                                }
//                                _handler.sendEmptyMessageDelayed(GO_SHOP_PRE_MID2, MathHelper.randomRange(2500, 3500));
                            } else {
                                _handler.sendEmptyMessageDelayed(GET_SHOP_PRODUCT_INFO, 100);
                            }
                        } else {
                            if (_shopRankAction.hasPageBottom()) {
                                if (!_shopRankAction.checkPageBottom()) {
                                    _prevNodeCount = _shopRankAction.getNodeCount();
                                    _itemCheckCount = 0;
                                    // 페이지 하단이 아니라면 아래로 스크롤한다.
                                    _handler.sendEmptyMessageDelayed(SCROLL_SHOP_BOTTOM, MathHelper.randomRange(2000, 3000));

//                                    Log.d(TAG, "# 노드 로딩을 위해 아래로 스크롤");
////                                    _swipeAction.swipeDownFast(45, 50);
//                                    _swipeAction.swipeDownFast(110, 115);
//                                    _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(3000, 4000));
                                } else {
                                    if (_page < MAX_SHOP_PAGE_COUNT) {
                                        ++_page;
                                        Log.d(TAG, "# 순위를 못찾아서 다음으로.. " + _page);
                                        if (_shopRankAction.checkNextButton()) {
//                                            _handler.sendEmptyMessageDelayed(GO_SHOP, MathHelper.randomRange(6000, 8000));
                                            _handler.sendEmptyMessageDelayed(CLICK_SHOP_NEXT_BUTTON, MathHelper.randomRange(1000, 2000));
                                        } else {
                                            Log.d(TAG, "# 다음 버튼 못찾아서 패턴종료.");
                                            _success = true;
                                            _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                                        }
                                    } else {
                                        // 순위 업로드.
                                        _success = true;
                                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                                    }
                                }
                            } else {
                                Log.d(TAG, "# 페이지 하단을 못찾아서 아니라서 새로고침.");
                                _webViewManager.reload();
                                webViewLoading(msg);
                            }
                        }
                    } else {
                        Log.d(TAG, "# 쇼핑 MID1 순위 검사 실패로 20초후 다시 시도..." + _retryCount);
                        if (!resendMessageDelayed(GO_SHOP, 20000, 3)) {
                            Log.d(TAG, "# 쇼핑 MID1 순위 검사에 실패로 패턴종료.");
                            sendMessageDelayed(END_PATTERN, 500);
                        }
                    }
                }
                break;
            }

            case SCROLL_SHOP_BOTTOM: {
                Log.d(TAG, "# 쇼핑 MID1 하단으로 이동");
                _shopRankAction.scrollToBottom();
//                _swipeAction.swipeDownFast(45, 50);
                _handler.sendEmptyMessageDelayed(CHECK_SHOP_RANK_MID1, MathHelper.randomRange(3000, 4000));
//                _handler.sendEmptyMessageDelayed(CHECK_ITEM_LOADED, MathHelper.randomRange(500, 1200));
                break;
            }

            case CHECK_ITEM_LOADED: {
                Log.d(TAG, "# 노드 로딩 체크");
                if (_prevNodeCount < _shopRankAction.getContentCount()) {
                    _handler.sendEmptyMessageDelayed(CHECK_SHOP_RANK_MID1, MathHelper.randomRange(3000, 4000));
                } else {
                    Log.d(TAG, "# 노드 개수가 같아서 다시 시도..." + _retryCount);
                    if (!resendMessageDelayed(CHECK_ITEM_LOADED, 1000, 5)) {
                        Log.d(TAG, "# 노드 개수가 변동되지 않아서 패턴종료.");
                        sendMessageDelayed(END_PATTERN, 500);
                    }
                }
                break;
            }

            // 미사용
            case CLICK_SHOP_NEXT_BUTTON: {
                Log.d(TAG, "# 쇼핑 MID 다음페이지 클릭");
                _shopRankAction.clickNextButton();
                webViewLoading(msg);
                break;
            }

            case GET_SHOP_PRODUCT_INFO: {
                Log.d(TAG, "# 쇼핑 상품 정보 가져오기");
                _productName = _shopRankAction.getProductName(_item.mid1);
                _storeName = _shopRankAction.getSellerName(_item.mid1);

                if (_item.item.getDetail == 0 || !TextUtils.isEmpty(_item.item.productUrl)) {
                    _success = true;
                    _handler.sendEmptyMessageDelayed(UPLOAD_SHOP_STORE_INFO, 100);
                } else {
                    _handler.sendEmptyMessageDelayed(CLICK_SHOP_CONTENT, 100);
                }
                break;
            }

            case GET_SHOP_PRODUCT_ID: {
                Log.d(TAG, "# 쇼핑 상품 ID 가져오기");
                _webViewManager.getWebView().post(() -> {
                    if (_webViewManager.getWebView().getUrl().contains("smartstore.naver.com/")) {
                        Log.d(TAG, "# 스마트 스토어 확인");
                        String url = _webViewManager.getWebView().getUrl();
                        String[] urls = url.split("\\?");
//                        String[] urlParts = urls[0].split("/");
//                        String code = urlParts[urlParts.length - 1];
                        _productUrl = urls[0];
                    } else {
                        _productUrl = "-1";
                    }
                    Log.d(TAG, "productUrl: " + _productUrl);
                });

                _success = true;
                _handler.sendEmptyMessageDelayed(UPLOAD_SHOP_STORE_INFO, 100);
                break;
            }

            case CLICK_SHOP_CONTENT: {
                Log.d(TAG, "# 쇼핑 MID 상세페이지 클릭");
                _webViewManager.setInterceptType(1);
                _shopRankAction.clickContent(_item.mid1);
                webViewLoading(msg);
                break;
            }

            case GO_SHOP_PRE_MID2: {
                Log.d(TAG, "# 쇼핑 MID 상세페이지로 이동");
                webViewLoad(msg, "https://msearch.shopping.naver.com/catalog/" + _item.mid1);
                break;
            }

            case CHECK_SHOP_DETAIL_URL: {
                Log.d(TAG, "# 쇼핑 MID 상세페이지 검사");
                if (_shopRankAction.checkCatalogPage()) {
                    Log.d(TAG, "# 쇼핑 MID 상세페이지 URL 검사");
                    _shopRankAction.getPurchaseConditionSequence();
                    Log.d(TAG, "# 쇼핑 MID 상세페이지 URL 검사 결과: " + _shopRankAction.getSeq());

                    _webViewManager.getWebView().post(new Runnable() {
                        @Override
                        public void run() {
                            String url = _webViewManager.getWebView().getUrl();
                            Log.d(TAG, "# 현재 URL: " + url);
                            _httpEngine.setCurrentUrl(url);
                            _handler.sendEmptyMessageDelayed(GET_MID2_API, 100);
                        }
                    });

//                    _handler.sendEmptyMessageDelayed(GO_SHOP_MID2, 100);
                } else {
                    _webViewManager.clearCookie();
                    Log.d(TAG, "# 쇼핑 MID2 상세페이지 검사 실패로 20초후 다시 시도..." + _retryCount);
                    if (!resendMessageDelayed(GO_SHOP_PRE_MID2, 20000, 5)) {
                        Log.d(TAG, "# 쇼핑 MID2 상세페이지 검사에 실패로 패턴종료.");
                        sendMessageDelayed(END_PATTERN, 500);
                    }
                }
                break;
            }

            case GET_MID2_API: {
                Log.d(TAG, "# 쇼핑 MID2 순위 가져오기");
                NnbData prevNnbData = UserManager.getInstance().nnbData;
                String prevUa = UserManager.getInstance().ua;
                String prevChromeVersion = UserManager.getInstance().chromeVersion;
                String prevBrowserVersion = UserManager.getInstance().browserVersion;

                if (TextUtils.isEmpty(UserManager.getInstance().chromeVersion)) {
                    Log.d(TAG, "## API용 ua 가져오기.");
                    UaAction action = new UaAction();
                    action.loginId = UserManager.getInstance().getLoginId(_webViewManager.getWebView().getContext());
                    int result = action.requestUa();

                    if (result == 1) {
                        NnbData nnbData = action.getNnbData();
                        String ua = action.getUserAgent();
                        UserManager.getInstance().nnbData = nnbData;

                        if ((ua != null) && (ua.length() > 0)) {
                            Log.d(TAG, "## 가져온 UA: " + _webViewManager.getUserAgentString());
                            UserManager.getInstance().ua = ua;

                            if (nnbData != null) {
                                UserManager.getInstance().chromeVersion = nnbData.chromeVersion;
                                UserManager.getInstance().browserVersion = nnbData.browserVersion;
                            }
                        }
                    } else {
                        Log.d(TAG, "# UA 가져오기 실패로 수동 설정");
                        NnbData nnbData = new NnbData();
                        nnbData.model = "SM-G975N";
                        nnbData.platformVersion = "SM-G975N";
                        UserManager.getInstance().nnbData = nnbData;
                        UserManager.getInstance().ua = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Mobile Safari/537.36";
                        UserManager.getInstance().chromeVersion = "138.0.7204.179";
                    }

                    _httpEngine.setUa(UserManager.getInstance().ua);
                    _httpEngine.setChromeVersion(UserManager.getInstance().chromeVersion);
                }

                _httpEngine.setCurrentUrl("https://msearch.shopping.naver.com/");
                _httpEngine.setAddedHeader("x-wtm-graphql", "eyJvcCI6IkNhdGFsb2dQcm9kdWN0cyJ9");
                int pageSize = 20;
                String jsonData = _httpEngine.requestNaverMobileCompanyContentFromGraphql(getApiBodyString());
                Log.d(TAG, "result: " + jsonData);
                JSONObject product = null;
                boolean notFound = false;

                // rollback
                UserManager.getInstance().nnbData = prevNnbData;
                UserManager.getInstance().ua = prevUa;
                UserManager.getInstance().chromeVersion = prevChromeVersion;
                UserManager.getInstance().browserVersion = prevBrowserVersion;
                _httpEngine.setUa(prevUa);
                _httpEngine.setChromeVersion(prevChromeVersion);
                _httpEngine.setBrowserVersion(prevBrowserVersion);

                if (TextUtils.isEmpty(jsonData)) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONObject dataVariables = jsonObject.getJSONObject("data");
                        JSONObject catalogProductsVariables = dataVariables.getJSONObject("catalog_Products");
                        JSONArray productsVariables = catalogProductsVariables.getJSONArray("products");

                        if (productsVariables.length() == 0) {
                            notFound = true;
                        } else {
                            for (int i = 0; i < productsVariables.length(); ++i) {
                                ++_shopProductRank;
                                JSONObject object = productsVariables.getJSONObject(i);
                                Log.d(TAG, "mid2: " + _item.mid2 + ", nvMid: " + object.getString("nvMid"));
                                if (object.getString("nvMid").equals(_item.mid2)) {
                                    product = object;
                                    break;
                                }
                            }

                            if (product == null && productsVariables.length() < pageSize) {
                                notFound = true;
                            }
                        }

                        if (notFound) {
                            Log.d(TAG, "# 가격비교 상품을 찾을 수 없어서 패턴종료.");
                            // 순위 업로드.
                            // mid1 순위는 가져왔기 때문에 업로드 해준다.
                            _success = true;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                        } else {
                            if (product != null) {
                                Log.d(TAG, "# 쇼핑 MID2 순위 검사 성공");
                                _shopRankAction.setProductRank(_shopProductRank);

                                try {
                                    _storeName = product.getString("mallName");
                                    _productName = product.getString("productName");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.d(TAG, "# 기본정보 가져오기 실패");
                                }

                                // 순위 업로드.
                                _success = true;
                                _handler.sendEmptyMessageDelayed(UPLOAD_SHOP_STORE_INFO, 100);
                            } else {
                                if (_page2 <= MAX_SHOP_MID2_PAGE_COUNT) {
                                    ++_page2;
                                    _handler.sendEmptyMessageDelayed(GET_MID2_API, MathHelper.randomRange(1000, 2000));
                                } else {
                                    Log.d(TAG, "# 가격비교 상품을 찾을 수 없어서 패턴종료.");
                                    // 순위 업로드.
                                    // mid1 순위는 가져왔기 때문에 업로드 해준다.
                                    _success = true;
                                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(TAG, "# 가격비교 API 호출에 실패해서 패턴종료.");
                        // 순위 업로드.
                        // mid1 순위는 가져왔기 때문에 업로드 해준다.
                        _success = true;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    }
                }
                break;
            }

            case GO_SHOP_MID2: {
                Log.d(TAG, "# 쇼핑 MID2 페이지로 이동");
                if (TextUtils.isEmpty(_item.item.code3)) {
                    _webViewManager.getWebView().post(new Runnable() {
                        @Override
                        public void run() {
                            String url = _webViewManager.getWebView().getUrl();
                            Log.d(TAG, "# 현재 URL: " + url);
                            String url2 = url.replace("?", "/products?fromWhere=CATALOG&");
                            Log.d(TAG, "# 변경 URL: " + url2);
                            _webViewManager.getWebView().loadUrl(url2);
                        }
                    });

                    webViewLoading(msg);
                } else {
                    String url = "https://msearch.shopping.naver.com/catalog/" + _item.mid1 + "/products?fromWhere=CATALOG";
//                String url = "https://msearch.shopping.naver.com/catalog/" + _item.mid1 + "/products?page=9&fromWhere=CATALOG";
                    url += "&purchaseConditionSequence=" + _item.item.code3;

//                    if (_shopRankAction.getSeq() > 0) {
//                        url += "&purchaseConditionSequence=" + _shopRankAction.getSeq();
//                    }

                    webViewLoad(msg, url);
//                webViewLoad(msg, "https://msearch.shopping.naver.com/catalog/" + _item.mid1 + "/products?page=5");
                }
                break;
            }

            case CHECK_SHOP_RANK_MID2: {
                Log.d(TAG, "# 쇼핑 MID2 순위 검사");
                if (_shopRankAction.checkShopRank(_item.mid2)) {
                    if (_shopRankAction.getProductRank() > 0) {
                        Log.d(TAG, "# 쇼핑 MID2 순위 검사 성공");
                        // 순위 업로드.
                        _success = true;
                        _storeName = _shopRankAction.getShopSellerName(_item.mid2);
                        _productName = _shopRankAction.getShopProductName(_item.mid2);

                        if (_item.item.getDetail == 0 || !TextUtils.isEmpty(_item.item.productUrl)) {
                            _handler.sendEmptyMessageDelayed(UPLOAD_SHOP_STORE_INFO, 100);
                        } else {
                            _handler.sendEmptyMessageDelayed(CLICK_SHOP_CONTENT_MID2, 500);
                        }

//                        _handler.sendEmptyMessageDelayed(UPLOAD_SHOP_STORE_INFO, 100);
//                        _handler.sendEmptyMessageDelayed(CLICK_SHOP_CONTENT_MID2, 500);
//                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    } else {
                        if (_page2 <= MAX_SHOP_MID2_PAGE_COUNT) {
                            Log.d(TAG, "# 쇼핑 MID2 순위 하단 검사... " + _page2);
                            ++_page2;

                            if (!_shopRankAction.checkPageBottom()) {
                                // 페이지 하단이 아니라면 아래로 스크롤한다.
                                _handler.sendEmptyMessageDelayed(SCROLL_SHOP_BOTTOM_MID2, MathHelper.randomRange(1000, 2000));
                            } else {
                                _success = true;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                            }
                        } else {
                            Log.d(TAG, "# 하단 이동 시도 초과로 패턴종료.. " + _page2);
                            _success = true;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                        }
                    }

                } else {
                    Log.d(TAG, "# 쇼핑 MID2 순위 검사 실패로 20초후 다시 시도..." + _retryCount);
                    if (!resendMessageDelayed(GO_SHOP_MID2, 20000, 3)) {
                        Log.d(TAG, "# 쇼핑 MID2 순위 검사에 실패로 패턴종료.");
                        sendMessageDelayed(END_PATTERN, 500);
                    }
                }
                break;
            }

            case SCROLL_SHOP_BOTTOM_MID2: {
                Log.d(TAG, "# 쇼핑 MID2 하단으로 이동");
                _shopRankAction.scrollToBottom(600);
//                _swipeAction.swipeDownFast(45, 50);
                _handler.sendEmptyMessageDelayed(CHECK_SHOP_RANK_MID2, MathHelper.randomRange(1000, 2000));
                break;
            }

            case CLICK_SHOP_CONTENT_MID2: {
                Log.d(TAG, "# 쇼핑 MID2 상세페이지 클릭");
                _shopRankAction.clickContent2(_item.mid2);
                webViewLoading(msg);
                break;
            }

            case GET_SHOP_SMART_STORE_INFO: {
                Log.d(TAG, "# 스마트 스토어 정보 가져오기");
//                _productName = _shopRankAction.getSmartStoreProductName();
//                _storeName = _shopRankAction.getSmartStoreName();
                Log.d(TAG, "스마트 스토어 상품명/판매자명: " + _productName + "/" + _storeName);

                _webViewManager.getWebView().post(() -> {
                    String url = _webViewManager.getWebView().getUrl();
                    String[] urls = url.split("\\?");
//                    String[] urlParts = urls[0].split("/");
//                    _productUrl = urlParts[urlParts.length - 1];
                    _productUrl = urls[0];
                    Log.d(TAG, "productUrl: " + _productUrl);
                });

                _success = true;
                _handler.sendEmptyMessageDelayed(UPLOAD_SHOP_STORE_INFO, 100);
                break;
            }

            case UPLOAD_SHOP_STORE_INFO: {
                Log.d(TAG, "# 상점 정보 등록 - 상품명/판매자명: " + _productName + "/" + _storeName + ", mallId: " + _mallId + ", catId: " + _catId + ", productUrl: " + _productUrl);
                _productInfoUpdateAction.registerInfo(_productName, _storeName, _mallId, _catId, _productUrl, _sourceType, _sourceUrl);
                Log.d(TAG, "# 샵 스토어 정보 등록 완료로 패턴종료.");
                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                break;
            }

            case CREATE_SHOP_KEYWORD: {
                Log.d(TAG, "# 쇼핑 검색 키워드 생성");
                if (_item.mid2.length() > 0 && !_item.mid2.equals(".")) {
                    // 가격비교
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 100);
                } else {
                    // 단일상품
                    switch (_step) {
                        case 0:
                            _generatedKeyword = _item.keyword;
                            break;

                        case 1:
                            break;

                        case 2:
                            break;

                        case 3:
                            break;

                        default:
                            break;
                    }

                    if (_generatedKeyword != null) {
                        webViewLoad(msg, "https://msearch.shopping.naver.com/search/all?query=" + _parsedKeyword);
                    }
//
//                    _success = true;
//                    _handler.sendEmptyMessageDelayed(GET_SHOP_PRODUCT_INFO, 100);
                }
//                _productInfoUpdateAction.registerInfo(_productName, _storeName);
//                Log.d(TAG, "# 샵 스토어 정보 등록 완료로 패턴종료.");
//                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                break;
            }

//            case CHECK_SHOP_KEYWORD: {
//                Log.d(TAG, "# 쇼핑 MID1 순위 검사");
//                if (_shopRankAction.isNoResult()) {
//                    Log.d(TAG, "# 결과가 없어서 패턴종료.");
//                    _success = true;
//                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
//                } else {
//                    if (_shopRankAction.checkRank(_item.mid1, _page)) {
//                        if (_shopRankAction.getRank() > 0) {
//                            Log.d(TAG, "# 쇼핑 MID1 순위 검사 성공");
//                            if (_item.mid2.length() > 0 && !_item.mid2.equals(".")) {
//                                _handler.sendEmptyMessageDelayed(CLICK_SHOP_CONTENT, 100);
////                                _handler.sendEmptyMessageDelayed(GO_SHOP_PRE_MID2, MathHelper.randomRange(2500, 3500));
//                            } else {
//                                // 순위 업로드.
//                                _success = true;
//                                _handler.sendEmptyMessageDelayed(GET_SHOP_PRODUCT_INFO, 100);
//                            }
//                        } else {
//                            if (!_shopRankAction.checkPageBottom()) {
//                                // 페이지 하단이 아니라면 아래로 스크롤한다.
//                                _handler.sendEmptyMessageDelayed(SCROLL_SHOP_BOTTOM, MathHelper.randomRange(1000, 2000));
//                            } else {
//                                if (_page < MAX_SHOP_PAGE_COUNT) {
//                                    ++_page;
//                                    Log.d(TAG, "# 순위를 못찾아서 다음으로.. " + _page);
//                                    if (_shopRankAction.checkNextButton()) {
//                                        _handler.sendEmptyMessageDelayed(GO_SHOP, MathHelper.randomRange(1000, 2000));
//                                    } else {
//                                        Log.d(TAG, "# 다음 버튼 못찾아서 패턴종료.");
//                                        _success = true;
//                                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
//                                    }
//                                } else {
//                                    // 순위 업로드.
//                                    _success = true;
//                                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
//                                }
//                            }
//                        }
//                    } else {
//                        Log.d(TAG, "# 쇼핑 MID1 순위 검사 실패로 10초후 다시 시도..." + _retryCount);
//                        _nextMessage = GO_SHOP;
//                        if (!resendMessageDelayed(PAGE_RELOAD, 10000, 3)) {
//                            Log.d(TAG, "# 쇼핑 MID1 순위 검사에 실패로 패턴종료.");
//                            sendMessageDelayed(END_PATTERN, 500);
//                        }
//                    }
//                }
//                break;
//            }

            case GO_SHOP_HOME: {
                Log.d(TAG, "# 네이버 쇼핑홈으로 이동");
                webViewLoad(msg, "https://shopping.naver.com/home");
                break;
            }

            case TOUCH_SHOP_HOME_SEARCH_BUTTON: {
                Log.d(TAG, "# 쇼핑홈 검색버튼 터치");
                if (_shopPageAction.touchHomeSearchButton()) {
                    _page = 2;   // 실제 페이지가 아니라 단순 구분용이므로. 메인이 아닌것으로 설정.
//                    _waitCount = 0;
                    _handler.sendEmptyMessageDelayed(INPUT_KEYWORD, MathHelper.randomRange(4000, 5000));
                } else {
                    Log.d(TAG, "# 쇼핑홈 검색버튼 터치에 실패해서 패턴종료.");
                    _workCode = 110006;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                }
                break;
            }

            case INPUT_KEYWORD: {
                Log.d(TAG, "# 검색창 검사");
                if (!_shopPageAction.searchBarShown()) {
                    if (_findBarCount > 15) {
                        Log.d(TAG, "# 로딩에러로 처리 중단.");
                        _workCode = 110012;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    } else {
                        if (_waitCount < 5) {
//                            Log.d(TAG, "# 검색창이 떠있지 않아서 3초 후 다시 시도..." + _waitCount);
                            Log.d(TAG, "# 검색창이 떠있지 않아서 3초 후 다시 터치..." + _waitCount);
                            ++_waitCount;
//                            _handler.sendEmptyMessageDelayed(msg.what, 3000);
                            _handler.sendEmptyMessageDelayed(TOUCH_SHOP_HOME_SEARCH_BUTTON, 3000);
                        } else {
                            Log.d(TAG, "# 검색창이 떠있지 않아서 새로고침");
                            SystemClock.sleep(MathHelper.randomRange(3500, 5000));
                            ++_findBarCount;
                            _webViewManager.reload();
                            webViewLoading(msg);
//                            Log.d(TAG, "# 검색창이 떠있지 않아서 다시 터치");
//                            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BAR, 5000);
                        }
                    }
                } else {
                    inputKeyword(_item.keyword);
                }
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치");
                _step = 0;
                _page = 2;   // 실제 페이지가 아니라 단순 구분용이므로. 메인이 아닌것으로 설정.
//                _action.touchSearchButton();
//                webViewLoading(msg);
                break;
            }

            /*
            case TOUCH_SHOP_CONTENT: {
                Log.d(TAG, "# 네이버 쇼핑 컨텐츠 검사");

                if (_shopPageAction.searchBarShown()) {
                    Log.d(TAG, "# 쇼핑 검색창이 떠있어서 검색버튼 클릭.");
                    _shopSearchBarAction.submitSearchButton();
                    msg.what = TOUCH_SEARCH_BUTTON;
                    webViewLoading(msg);
                    break;
                }

                if ((_item.item.inclEmpty == 0) && _shopPageAction.hasEmptyResult()) {
                    Log.d(TAG, "# 검색결과 없는 연관 상품 노출로 패턴종료.");
                    _workCode = 110022;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 1000);
                    break;
                }

                if (_shopPageAction.hasShopEmptyResult()) {
                    Log.d(TAG, "# 쇼핑 검색결과 없어서 패턴종료.");
                    _workCode = 110023;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 1000);
                    break;
                }

                InsideData insideData = _shopPageAction.getContentMidInsideData(_mid, (_currentPage <= 1));
                if (insideData != null) {
                    if (insideData.isInside()) {
                        Log.d(TAG, "# 네이버 쇼핑 컨텐츠 터치");
                        if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_URL_CHANGE) {
                            _handler.sendEmptyMessageDelayed(GO_SHOP_CONTENT_URL, MathHelper.randomRange(2000, 4000));
                        } else {
                            String currentUrl = _webViewManager.getCurrentUrl();

                            if (_shopPageAction.touchContentMid(_mid, (_currentPage <= 1))) {
                                // 쇼검만해당.
                                if (_currentPage > 1) {
                                    String url = _shopPageAction.getNClickUrl(_mid, currentUrl);

                                    if (!TextUtils.isEmpty(url)) {
                                        // 네이버 신규 버전 처리용.
                                        HttpEngine _httpEngine;
                                        _httpEngine = new HttpEngine(_webViewManager.getWebView().getContext());
                                        _httpEngine.setUa(UserManager.getInstance().ua);
                                        _httpEngine.setChromeVersion(UserManager.getInstance().chromeVersion);
                                        _httpEngine.setBrowserVersion(UserManager.getInstance().browserVersion);
                                        _httpEngine.setNnb(UserManager.getInstance().nnb);
                                        _httpEngine.setReferer(currentUrl);
                                        _httpEngine.requestUrlWithOkHttpClientImage(url);
                                    }
                                }

                                webViewLoading(msg);
                            } else {
                                Log.d(TAG, "# 네이버 쇼핑 컨텐츠 터치 실패로 패턴종료.");
                                _workCode = 110021;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                            }
                        }
                    } else if (insideData.getHeight() > 450) {
                        Log.d(TAG, "# 컨텐츠가 제대로 로드되지 않아서 새로고침.");
                        _webViewManager.reload();
                        msg.what = TOUCH_SEARCH_BUTTON;
                        webViewLoading(msg);
                    } else if (insideData.inside > 0) {
                        Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                        if (insideData.rect.top > 1500) {
                            _swipeAction.swipeDownFast(110, 200);
                        } else {
                            _swipeAction.swipeDown();
                        }
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    } else {
                        Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                        _swipeAction.swipeUp();
                        _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                    }
                } else {
                    Log.d(TAG, "# 네이버 쇼핑 컨텐츠 못찾아서 다음으로...");
                    if (_currentPage <= 1) {    // 모통홈 로직
                        if (_item.item.workType == 1) {
                            _nextMessage = TOUCH_TOP_SHOPPING_BUTTON;
                            sendMessageDelayed(TOUCH_TOP_DOTS_BUTTON, 100);
                        } else {
                            sendMessageDelayed(TOUCH_MORE2_BUTTON, 100);
                        }
                    } else {
                        if (_workMore) {
                            Log.d(TAG, "# 페이지 하단 검사");
                            if ((_scrollCount1 < 10) && !_shopPageAction.checkPageBottom()) {
                                ++_scrollCount1;
                                // 페이지 하단이 아니라면 아래로 스크롤한다.
                                _nextMessage = TOUCH_SHOP_CONTENT;
                                _scrollType = 0;
                                _handler.sendEmptyMessageDelayed(SCROLL_BOTTOM, 100);
                            } else {
                                sendMessageDelayed(TOUCH_NEXT_BUTTON, 100);
                            }
                        } else {
                            Log.d(TAG, "# 더보기 처리 중단으로 패턴종료.");
                            _workCode = 110045;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                        }
                    }
                }
                break;
            }

             */


            case GET_SHOP_PACKET: {

                String sbth = getSbth();
//                String htmlString2 = _httpEngine.requestNaverApiSbth();
//
//                if (!TextUtils.isEmpty(htmlString2)) {
//                    Log.d(TAG, "requestNaverLcsPidFromUrl: " + htmlString2);
//                    sbth = htmlString2.trim();
//                }



                break;
            }



            // ### place
            case GO_PLACE: {
                Log.d(TAG, "# 플레이스 페이지로 이동");
                _scrollCount = 0;
                _isMore = false;
                webViewLoad(msg, "https://m.search.naver.com/search.naver?sm=mtb_hty.top&where=m&query=" + _parsedKeyword);
//                webViewLoad(msg, "https://m.place.naver.com/" + getPlaceType() +"/list?query=" + _parsedKeyword + "&x=0&y=0&level=top&entry=" + getEntryType());
                break;
            }

            case CLICK_PLACE_OPEN_MORE_BUTTON: {
                Log.d(TAG, "# 플레이스 펼쳐서 더보기 버튼 검사");
                if (_placeRankAction.hasOpenMoreButton(_item.url)) {
                    _placeRankAction.clickOpenMoreButton(_item.url);
//                    _handler.sendEmptyMessageDelayed(CLICK_PLACE_MORE_BUTTON, 3000);
                    _handler.sendEmptyMessageDelayed(CHECK_PLACE_RANK, 3000);
                } else {
                    Log.d(TAG, "# 플레이스 펼쳐서 더보기 버튼 못찾아서 다음으로...");
//                    _handler.sendEmptyMessageDelayed(CLICK_PLACE_MORE_BUTTON, 100);
                    _handler.sendEmptyMessageDelayed(CHECK_PLACE_RANK, 100);
                }
                break;
            }

            case CLICK_PLACE_MORE_BUTTON: {
                Log.d(TAG, "# 플레이스 더보기 버튼 검사");
                if (_placeRankAction.checkMoreButton(_item.url)) {
                    _placeRankAction.clickMoreButton(_item.url);
                    _isMore = true;
                    // 로딩이 두번되는 문제로 첫로딩 건너뛰기 위해 .5초 대기
                    SystemClock.sleep(500);
                    webViewLoading(msg);
                } else {
                    Log.d(TAG, "# 플레이스 더보기 버튼 못찾아서 패턴종료.");
                    _success = true;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);

//                    Log.d(TAG, "# 플레이스 더보기 버튼 못찾아서 다음으로...");
//                    _handler.sendEmptyMessageDelayed(CHECK_PLACE_RANK, 100);
                }
                break;
            }

            case CHECK_PLACE_RANK: {
                Log.d(TAG, "# 플레이스 순위 검사");
                if (_placeRankAction.checkRank(_item.code)) {
                    Log.d(TAG, "# 플레이스 순위 검사 성공");
                    if (!_isMore) {
                        if (_placeRankAction.getRank() > 0) {
                            // 순위 업로드.
                            _success = true;
                            _handler.sendEmptyMessageDelayed(GET_PLACE_INFO, 100);
                        } else {
                            Log.d(TAG, "# 플레이스 순위 검사 실패로 다음으로...");
                            _handler.sendEmptyMessageDelayed(CLICK_PLACE_MORE_BUTTON, 100);
                        }
                    } else {
                        if (_placeRankAction.getRank() > 0) {
                            InsideData insideData = _placeRankAction.getContentCodeInsideData(_item.code);
                            if (insideData != null) {
                                if (insideData.isInside()) {
                                    // 순위 업로드.
                                    _success = true;
                                    _handler.sendEmptyMessageDelayed(GET_PLACE_INFO, 100);
                                } else if (insideData.inside > 0) {
                                    Log.d(TAG, "# 화면에 안보여서 아래로 스크롤");
                                    if (insideData.rect.top > 1500) {
                                        _swipeAction.swipeDownFast(110, 200);
                                    } else {
                                        _swipeAction.swipeDown();
                                    }
                                    _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                                } else {
                                    Log.d(TAG, "# 화면에 안보여서 위로 스크롤");
                                    _swipeAction.swipeUp();
                                    _handler.sendEmptyMessageDelayed(msg.what, MathHelper.randomRange(1000, 2000));
                                }
                            } else {
                                Log.d(TAG, "# 플레이스 화면안에 못찾아서 순위만 업로드");
                                // 순위 업로드.
                                _success = true;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                            }
                        } else {
                            Log.d(TAG, "# 노드개수: " + _placeRankAction.getNodeCount());
                            if (_placeRankAction.getNodeCount() < 100) {
                                if (_scrollCount < 5) {
                                    Log.d(TAG, "# 순위를 못찾아서 다음으로 스크롤.. " + _scrollCount);
                                    ++_scrollCount;
                                    _handler.sendEmptyMessageDelayed(SCROLL_PLACE_BOTTOM, MathHelper.randomRange(1000, 2000));
                                } else {
                                    Log.d(TAG, "# 플레이스 " + _placeRankAction.getNodeCount() + "위 내에 없어서 패턴종료.");
                                    _success = true;
                                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                                }
                            } else {
                                Log.d(TAG, "# 플레이스 " + _placeRankAction.getNodeCount() + "위 내에 없어서 패턴종료.");
                                _success = true;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "# 플레이스 순위 검사 실패로 3초후 다시 시도..." + _retryCount);
                    if (!resendMessageDelayed(msg.what, 3000, 3)) {
                        if (!_isMore) {
                            Log.d(TAG, "# 플레이스 순위 검사 실패로 다음으로...");
                            _handler.sendEmptyMessageDelayed(CLICK_PLACE_MORE_BUTTON, 100);
                        } else {
                            Log.d(TAG, "# 플레이스 순위 검사 실패로 패턴종료.");
                            sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                        }
                    }
                }
                break;
            }

            case SCROLL_PLACE_BOTTOM: {
                Log.d(TAG, "# 플레이스 하단으로 이동");
                _swipeAction.swipeDownFast(45, 50);
                _handler.sendEmptyMessageDelayed(CHECK_PLACE_RANK, MathHelper.randomRange(3000, 4000));
                break;
            }

            case GET_PLACE_INFO: {
                Log.d(TAG, "# 플레이스 정보 가져오기");
                if (_placeRankAction.checkSource(_item.code)) {
                    _sourceType = Integer.toString(_placeRankAction.getSourceType());
                    _sourceUrl = _placeRankAction.getSourceUrl();
                    _handler.sendEmptyMessageDelayed(UPLOAD_PLACE_INFO, 100);
                } else {
                    if (!_isMore) {
                        _isMore = true;
                        Log.d(TAG, "# 플레이스 정보 검사 실패로 다시 검사");
                        sendMessageDelayed(CHECK_PLACE_RANK, 100);
                    } else {
                        Log.d(TAG, "# 플레이스 정보 검사 실패로 패턴종료.");
                        sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                    }
                }
                break;
            }

            case UPLOAD_PLACE_INFO: {
                Log.d(TAG, "# 플레이스 정보 등록 - sourceType: " + _sourceType + ", sourceUrl: " + _sourceUrl);
                _productInfoUpdateAction.registerInfo(_productName, _storeName, _mallId, _catId, _productUrl, _sourceType, _sourceUrl);
                Log.d(TAG, "# 플레이스 정보 등록 완료로 패턴종료.");
                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                break;
            }



            // ### site
            case GO_SITE: {
                Log.d(TAG, "# 사이트 페이지로 이동");
                _scrollCount = 0;
                webViewLoad(msg, "https://m.search.naver.com/search.naver?page=2&query=" + _parsedKeyword + "&sm=mtb_pge&start=1&where=m_web");
                break;
            }

            case CHECK_SITE_RANK: {
                Log.d(TAG, "# 사이트 순위 검사");
                if (_siteRankAction.checkRank(_item.url, _page)) {
                    if (_siteRankAction.getRank() > 0) {
                        Log.d(TAG, "# 사이트 순위 검사 성공");
                        // 순위 업로드.
                        _success = true;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    } else {
                        if (_page < MAX_PAGE_COUNT) {
                            ++_page;

                            if (_siteRankAction.checkNextButton()) {
                                Log.d(TAG, "# 순위를 못찾아서 다음 버튼 터치.. " + _page);
                                _siteRankAction.clickNextButton();
                                // 로딩이 두번되는 문제로 첫로딩 건너뛰기 위해 .5초 대기
                                SystemClock.sleep(500);
                                webViewLoading(msg);
                            } else {
                                Log.d(TAG, "# 다음 버튼 못찾아서 패턴종료.");
                                sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                            }

                        } else {
                            Log.d(TAG, "# 사이트 순위 못찾아서 패턴종료.");
                            _success = true;
                            sendMessageDelayed(END_PATTERN, 500);
                        }
                    }
                } else {
                    Log.d(TAG, "# 사이트 순위 검사 실패로 5초후 다시 시도..." + _retryCount);
                    if (!resendMessageDelayed(GO_SITE, 5000, 3)) {
                        Log.d(TAG, "# 사이트 순위 검사 실패로 패턴종료.");
                        sendMessageDelayed(END_PATTERN, MathHelper.randomRange(3000, 5000));
                    }
                }
                break;
            }



            // ### view
            case GO_VIEW: {
                Log.d(TAG, "# 뷰 페이지로 이동");
                _scrollCount = 0;
//                webViewLoad(msg, "https://m.search.naver.com/search.naver?where=m_view&sm=mtb_jum&query=" + _parsedKeyword);
                webViewLoad(msg, "https://m.search.naver.com/search.naver?ssc=tab.m_blog.all&sm=mtb_jum&query=" + _parsedKeyword);
                break;
            }

            case CHECK_VIEW_RANK: {
                Log.d(TAG, "# 뷰 순위 검사");
                if (_viewRankAction.checkRank(_item.url)) {
                    Log.d(TAG, "# 뷰 순위 검사 성공");
                    if (_viewRankAction.getRank() > 0) {
                        // 순위 업로드.
                        _success = true;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    } else {
                        if (_viewRankAction.getNodeCount() < 50) {
                            if (_scrollCount < 5) {
                                Log.d(TAG, "# 뷰 못찾아서 다음으로 스크롤.. " + _scrollCount);
                                ++_scrollCount;
                                _handler.sendEmptyMessageDelayed(SCROLL_VIEW_BOTTOM, MathHelper.randomRange(1000, 2000));
                            } else {
                                Log.d(TAG, "# 뷰 " + _viewRankAction.getNodeCount() + "위 내에 없어서 패턴종료.");
                                _success = true;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                            }
                        } else {
                            Log.d(TAG, "# 뷰 " + _viewRankAction.getNodeCount() + "위 내에 없어서 패턴종료.");
                            _success = true;
                            _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                        }
                    }
                } else {
                    Log.d(TAG, "# 뷰 순위 검사 실패로 5초후 다시 시도..." + _retryCount);
                    _nextMessage = msg.what;
                    if (!resendMessageDelayed(GO_VIEW, 5000, 3)) {
                        Log.d(TAG, "# 뷰 순위 검사에 실패로 패턴종료.");
                        sendMessageDelayed(END_PATTERN, 500);
                    }
                }
                break;
            }

            case SCROLL_VIEW_BOTTOM: {
                Log.d(TAG, "# 뷰 하단으로 이동");
                _viewRankAction.scrollToBottom();
//                _swipeAction.swipeDownFast(45, 50);
                _handler.sendEmptyMessageDelayed(CHECK_VIEW_RANK, MathHelper.randomRange(3000, 4000));
                break;
            }


            case END_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# 순위 검사 패턴 종료");
                _webViewManager.goBlankPage();
                registerFinish();
                _shopSearchBarAction.endPattern();
                _placeRankAction.endPattern();
                _shopRankAction.endPattern();
                _siteRankAction.endPattern();
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
            case GO_COOKIE_PAGE: {
                Log.d(TAG, "# 쿠키 페이지 이동 후 동작");
                sendMessageDelayed(GO_ACTION, MathHelper.randomRange(8000, 10000));
                break;
            }

//            case PAGE_RELOAD: {
//                Log.d(TAG, "# 페이지 새로고침 후 동작");
//                _handler.sendEmptyMessageDelayed(_nextMessage, MathHelper.randomRange(3000, 4000));
//                break;
//            }

            case GET_SHOP_PRODUCTS: {
                Log.d(TAG, "# 상품 페이지 이동 후 동작");
                if (!TextUtils.isEmpty(_item.mid2) && !_item.mid2.equals(".")) {
                    _reloadMessage = _lastMessage;
                    _nextMessage = CHECK_SHOP_DETAIL_URL;
                    _handler.sendEmptyMessageDelayed(CHECK_SHOP_ERROR_PAGE, MathHelper.randomRange(5000, 7000));
                } else {
                    _handler.sendEmptyMessageDelayed(GET_SHOP_PRODUCT_ID, MathHelper.randomRange(5000, 7000));
                }
                break;
            }

            case GO_SHOP: {
                Log.d(TAG, "# 쇼핑 이동 후 동작");
                _reloadMessage = _lastMessage;
                _nextMessage = CHECK_SHOP_RANK_MID1;
                _handler.sendEmptyMessageDelayed(CHECK_SHOP_ERROR_PAGE, MathHelper.randomRange(5000, 7000));
                break;
            }

            case CHECK_SHOP_RANK_MID1: {
                Log.d(TAG, "# 페이지 하단을 못찾아서 아니라서 새로고침 후 동작");
                _reloadMessage = _lastMessage;
                _nextMessage = CHECK_SHOP_RANK_MID1;
                _handler.sendEmptyMessageDelayed(CHECK_SHOP_ERROR_PAGE, MathHelper.randomRange(5000, 7000));
                break;
            }

            case CLICK_SHOP_NEXT_BUTTON: {
                Log.d(TAG, "# 쇼핑 MID 다음페이지 버튼 클릭 후 동작");
                _reloadMessage = _lastMessage;
                _nextMessage = CHECK_SHOP_RANK_MID1;
                _handler.sendEmptyMessageDelayed(CHECK_SHOP_ERROR_PAGE, MathHelper.randomRange(5000, 7000));
                break;
            }

            case CLICK_SHOP_CONTENT: {
                Log.d(TAG, "# 쇼핑 MID 상세페이지 클릭 후 동작");
                if (!TextUtils.isEmpty(_item.mid2) && !_item.mid2.equals(".")) {
                    _reloadMessage = _lastMessage;
                    _nextMessage = CHECK_SHOP_DETAIL_URL;
                    _handler.sendEmptyMessageDelayed(CHECK_SHOP_ERROR_PAGE, MathHelper.randomRange(5000, 7000));
                } else {
                    _handler.sendEmptyMessageDelayed(GET_SHOP_PRODUCT_ID, MathHelper.randomRange(5000, 7000));
                }
                break;
            }

            case GO_SHOP_PRE_MID2: {
                Log.d(TAG, "# 쇼핑 MID 상세페이지 이동 후 동작");
                _reloadMessage = _lastMessage;
                _nextMessage = CHECK_SHOP_DETAIL_URL;
                _handler.sendEmptyMessageDelayed(CHECK_SHOP_ERROR_PAGE, MathHelper.randomRange(5000, 7000));
                break;
            }

            case GO_SHOP_MID2: {
                Log.d(TAG, "# 쇼핑 MID2 이동 후 동작");
                _reloadMessage = _lastMessage;
                _nextMessage = CHECK_SHOP_RANK_MID2;
                _handler.sendEmptyMessageDelayed(CHECK_SHOP_ERROR_PAGE, MathHelper.randomRange(5000, 7000));
                break;
            }

            case CLICK_SHOP_CONTENT_MID2: {
                Log.d(TAG, "# 쇼핑 MID2 상세페이지 클릭 후 동작");
                _webViewManager.getWebView().post(new Runnable() {
                    @Override
                    public void run() {
                        if (_webViewManager.getWebView().getUrl().contains("smartstore.naver.com/")) {
                            Log.d(TAG, "# 스마트 스토어 확인");
                            _reloadMessage = _lastMessage;
                            _nextMessage = GET_SHOP_SMART_STORE_INFO;
                            _handler.sendEmptyMessageDelayed(CHECK_SHOP_ERROR_PAGE, MathHelper.randomRange(5000, 7000));
                        } else {
                            Log.d(TAG, "# 스마트 스토어가 아니어서 상점명만 업로드");
                            _productUrl = "-1";
                            _handler.sendEmptyMessageDelayed(UPLOAD_SHOP_STORE_INFO, 500);
                        }
                    }
                });
                break;
            }

            case GO_SHOP_HOME: {
                Log.d(TAG, "# 쇼핑홈 이동 후 동작");
                _findBarCount = 0;
//                SystemClock.sleep(5000);
//                _shopPageAction.printLocalStorage();
                _handler.sendEmptyMessageDelayed(TOUCH_SHOP_HOME_SEARCH_BUTTON, MathHelper.randomRange(5000, 6000));
                break;
            }

            case TOUCH_SEARCH_BUTTON: {
                Log.d(TAG, "# 검색버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_SHOP_RANK_MID1, MathHelper.randomRange(1000, 2000));
                break;
            }


            case GO_PLACE: {
                Log.d(TAG, "# 플레이스 이동 후 동작");
                _handler.sendEmptyMessageDelayed(CLICK_PLACE_OPEN_MORE_BUTTON, MathHelper.randomRange(4000, 5000));
                break;
            }

            case CLICK_PLACE_MORE_BUTTON: {
                Log.d(TAG, "# 플레이스 더보기 버튼 클릭 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_PLACE_RANK, MathHelper.randomRange(4000, 5000));
                break;
            }

            case GO_SITE: {
                Log.d(TAG, "# 사이트 이동 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_SITE_RANK, MathHelper.randomRange(4000, 5000));
                break;
            }

            case CHECK_SITE_RANK: {
                Log.d(TAG, "# 사이트 다음 버튼 터치 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_SITE_RANK, MathHelper.randomRange(4000, 5000));
                break;
            }

            case GO_VIEW: {
                Log.d(TAG, "# 뷰 이동 후 동작");
                _handler.sendEmptyMessageDelayed(CHECK_VIEW_RANK, MathHelper.randomRange(3000, 4000));
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

        if (_item.category.equals("nfplace")) {
            // 플레이스 [많이찾는]
            _resultPatternAction.registerFinish(_placeRankAction.getRank());
        } else if (_item.category.equals("nplace")) {
            _resultPatternAction.registerFinish(_placeRankAction.getRank());
        } else if (_item.category.equals("nshop")) {
            _resultPatternAction.registerFinish(_shopRankAction.getRank(), _shopRankAction.getProductRank());
        } else if (_item.category.equals("nshop_pc")) {
            _resultPatternAction.registerFinish(_shopRankAction.getRank(), _shopRankAction.getProductRank());
        } else if (_item.category.equals("nshop_cr")) {
            _resultPatternAction.registerFinish(_shopRankAction.getRank(), _shopRankAction.getProductRank());
        } else if (_item.category.equals("site")) {
            _resultPatternAction.registerFinish(_siteRankAction.getRank());
        } else if (_item.category.equals("nview")) {
            _resultPatternAction.registerFinish(_viewRankAction.getRank());
        }
    }

    protected boolean hasCookie() {
        return UserManager.getInstance().nnb != null;
    }

    protected boolean getCookie() {
        Log.d(TAG, "# 쿠기 가져오기.");
        NnbAction action = new NnbAction();
        action.loginId = UserManager.getInstance().getLoginId(_webViewManager.getWebView().getContext());
        int result;

        result = action.requestNnb();

        if (result == 1) {
            NnbData _nnbData = action.getNnbData();
            String _getNnb = action.getNnb();
            String ua = action.getUserAgent();
//            _getNnb = "JJCPIG2XJWKGK";
//            _getNnb = "EA3DMJJWQS7GK";

            if (UserManager.getInstance().nnbData == null) {
                UserManager.getInstance().nnbData = _nnbData;
            } else {
                UserManager.getInstance().nnbData.nnb = _getNnb;

                if (_nnbData != null) {
                    UserManager.getInstance().nnbData.loginCookieId = _nnbData.loginCookieId;
                    UserManager.getInstance().nnbData.naverCookieId = _nnbData.naverCookieId;
//                    UserManager.getInstance().nnbData.nnb = _getNnb;
                    UserManager.getInstance().nnbData.nidInf = _nnbData.nidInf;
                    UserManager.getInstance().nnbData.nidAut = _nnbData.nidAut;
                    UserManager.getInstance().nnbData.nidJkl = _nnbData.nidJkl;
                    UserManager.getInstance().nnbData.nidSes = _nnbData.nidSes;
                    UserManager.getInstance().nnbData.naverCookieOther = _nnbData.naverCookieOther;
                }
            }

            UserManager.getInstance().nnb = _getNnb;

            if ((ua != null) && (ua.length() > 0)) {
                Log.d(TAG, "# UA 변경 시도: " + _webViewManager.getUserAgentString());
                _webViewManager.setUserAgentString(ua);
                UserManager.getInstance().ua = ua;
                UserManager.getInstance().chromeVersion = action.getChromeVersion();
                UserManager.getInstance().browserVersion = action.getBrowserVersion();
                Log.d(TAG, "# UA 변경 완료: " + _webViewManager.getUserAgentString());
            }

            if (_getNnb == null) {
                Log.d(TAG, "# 쿠키 없음.");
            } else {
                return true;
            }
        } else {
            Log.d(TAG, "# 쿠키 가져오기 실패...");
        }

        return false;
    }

    protected boolean setCookie() {
        String nnb = UserManager.getInstance().nnb;

        if (nnb != null) {
            _webViewManager.setCookieString("https://naver.com", "NNB" + "=" + nnb + "; expires=Sat, 01 Jan 2050 09:00:00 GMT; path=/; domain=.naver.com; SameSite=None; Secure");
//                _cookieController.setCookie(".naver.com", "NNB=" + _getNnb);

            return true;
        }

        return false;
    }

    protected String getSbth() {
        String htmlString = _httpEngine.requestNaverApiSbth();
        String sbth = null;

        if (!TextUtils.isEmpty(htmlString)) {
            Log.d(TAG, "requestNaverApiSbth: " + htmlString);
            sbth = htmlString.trim();
        }

        return sbth;
    }

    public String getApiBodyString() {
//        String urlString = _httpEngine.getCurrentUrl();
//        Log.d(TAG, "base url: " + urlString);
//        Map<String, String> queries = HttpEngine.query2MapFromUrlString(urlString);
//        String naPm = queries.get("NaPm");
//        String query = queries.get("query");
//
//        try {
//            naPm = URLDecoder.decode(naPm, "UTF-8");
//            query = URLDecoder.decode(query, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

//        return HttpEngine.makeCompanyListApiBody(_item.mid1, "", _item.item.code3, 0, _page2, query, naPm, false);
        return HttpEngine.makeCompanyListApiBody(_item.mid1, "", _item.item.code3, 0, _page2, _item.keyword, null, false);
    }

    public String removeAllWhiteSpace(String str) {
        StringTokenizer st = new StringTokenizer(str.trim());
        StringBuilder sb = new StringBuilder();

        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
        }

        return sb.toString();
    }

    private String getPlaceType() {
        String type = "place";

        if (_item.url.contains("restaurant")) {             // 식당관련
            type = "restaurant";
        } else if (_item.url.contains("accommodation")) {   // 예약관련
            type = "accommodation";
        } else if (_item.url.contains("hospital")) {        // 병원관련
            type = "hospital";
        } else if (_item.url.contains("hairshop")) {        // 미용실관련
            type = "hairshop";
        } else if (_item.url.contains("nailshop")) {        // 네일샵관련
            type = "nailshop";
        }

        return type;
    }

    private String getEntryType() {
        String type = "pll";

        if (_item.url.contains("restaurant")) {             // 식당관련
//            type = "restaurant";
        } else if (_item.url.contains("accommodation")) {   // 예약관련
            type = "pbl";
        } else if (_item.url.contains("hospital")) {        // 병원관련
//            type = "hospital";
        } else if (_item.url.contains("hairshop")) {        // 미용실관련
//            type = "hairshop";
        } else if (_item.url.contains("nailshop")) {        // 네일샵관련
//            type = "nailshop";
        }

        return type;
    }


    public void inputKeyword(String keyword) {
        if (_item.item.workType == KeywordItem.WORK_TYPE_INPUT) {
            Log.d(TAG, "# 검색어 삽입: " + keyword);
            // 인풋태그에 값 넣기
            _shopPageAction.inputSearchBar(keyword);
            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1500, 3000));
        } else if (_item.item.workType == KeywordItem.WORK_TYPE_CLIPBOARD) {
            Log.d(TAG, "# 검색어 클립보드 복사: " + keyword);
            _action.copyToClipboard(_webViewManager.getWebView().getContext(), keyword);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SystemClock.sleep(MathHelper.randomRange(1000, 1500));
                Log.d(TAG, "# 검색어 붙여넣기");
                _action.pasteClipboard();
            } else {
                Log.d(TAG, "# 검색창 롱터치");
                if (!_shopPageAction.touchSearchBarLong()) {
                    Log.d(TAG, "# 검색창 롱터치에 실패해서 패턴종료.");
                    _workCode = 110013;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 3000);
                    return;
                }

                SystemClock.sleep(MathHelper.randomRange(1000, 1500));
                Log.d(TAG, "# 검색어 붙여넣기");
                _shopPageAction.touchPasteButton();
            }

            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1500, 3000));
        } else if (_item.item.workType == 5) {
            // 추가 가능한 기능있으면 추가 예정..
        } else {
            Log.d(TAG, "# 검색어 입력: " + keyword);
            _action.inputKeywordForTyping(keyword);
            _handler.sendEmptyMessageDelayed(TOUCH_SEARCH_BUTTON, MathHelper.randomRange(1000, 3000));
        }
    }
}
