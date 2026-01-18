package com.sec.android.app.sbrowser.pattern.naver.shop;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.Utility;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpCookieController;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpEngine;
import com.sec.android.app.sbrowser.engine.WebEngine.HttpHeader;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.models.KeywordItem;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.models.NnbData;
import com.sec.android.app.sbrowser.pattern.action.NaverCookieOtherAction;
import com.sec.android.app.sbrowser.pattern.action.NaverCookieStatusAction;
import com.sec.android.app.sbrowser.pattern.action.NnbAction;
import com.sec.android.app.sbrowser.pattern.action.ResultAction;
import com.sec.android.app.sbrowser.pattern.naver.NaverPatternMessage;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NaverShopPacketPatternMessage extends NaverPatternMessage {

    private static final String TAG = NaverShopPacketPatternMessage.class.getSimpleName();

    protected static final int GET_COOKIE = 43;
    protected static final int RUN_PACKET = 44;
    protected static final int RUN_PACKET_INPUT_CC = RUN_PACKET + 1;
    protected static final int RUN_PACKET_INPUT = RUN_PACKET + 2;
    protected static final int RUN_PACKET_SEARCH = RUN_PACKET + 3;
    protected static final int RUN_PACKET_SECOND = RUN_PACKET + 4;
    protected static final int RUN_PACKET_SHOPPING_TAB = RUN_PACKET + 5;
    protected static final int RUN_PACKET_CONTENT = RUN_PACKET + 6;
    protected static final int RUN_PACKET_CONTENT_COMPANY = RUN_PACKET + 7;
    protected static final int RUN_PACKET_CONTENT_COMPANY_LIST = RUN_PACKET + 8;
    protected static final int RUN_PACKET_VOLT = RUN_PACKET + 9;
    protected static final int RUN_PACKET_SIMPLE_URL = RUN_PACKET + 10;
    protected static final int RUN_PACKET_AFTER_PRODUCT_DETAIL = RUN_PACKET_SIMPLE_URL + 1;
    protected static final int RUN_PACKET_AFTER_POPULAR = RUN_PACKET_SIMPLE_URL + 2;
    protected static final int RUN_PACKET_AFTER_REVIEW = RUN_PACKET_SIMPLE_URL + 3;
    protected static final int RUN_PACKET_EXPOSE = RUN_PACKET_AFTER_REVIEW + 1;

    protected final NaverCookieStatusAction _cookieStatusAction;
    protected final NaverCookieOtherAction _cookieOtherAction;

    private String _keyword;
    private String _mid;
    private String _mid2;
    private NnbData _nnbData = null;
    private String _ua = null;
    private String _chromeVersion = null;
    private String _browserVersion = null;
    private String _nnb = null;

    private int _startHomeMode = KeywordItem.SHOP_HOME_MOBILE;
    private int _firstPattern = RUN_PACKET_SEARCH;

    private HttpCookieController _cookieController;
    private HttpEngine _httpEngine;

    private String _htmlString = null;
    private String _searchHtmlString = null;
    private String _categoryProductsHtmlString = null;
    private JSONObject _baseJsonObject = null;

    private boolean _isWifi = false;
    private String _jqueryFuncName = null;

    private String _contentUrl = null;
    private String _catalogUrl = null;
    private int _delayType = 0;
    private int _stayDelayType = 0;
    private int _packetPatternType = 0;
    private int _step = 0;
    private boolean _isLoginCookieExpired = false;
    private boolean _isPacketShopHome = false;
    private boolean _mainReferer = false;
    private boolean _clickReferer = false;
    private String _clickRefererUrl = null;
    private String _wcsRefererUrl = null;
    private boolean _mainCookie = false;
    private boolean _runShoppingTab = false;
    private boolean _runSimpleUrl = false;
    private boolean _useMobileAc = false;
    private boolean _useNewPacket = false;
    private boolean _useNewAfter = false;
    private boolean _useLcsGet = false;
    private boolean _useRd0 = false;
    private boolean _useRunPRd = false;
    private boolean _useProductLog = false;
    private boolean _useVoltPv = false;
    private boolean _useFetchStoreProducts = false;
    private boolean _useRandomNid = false;
    private boolean _useLcsPost = false;
    private boolean _useSlcPost = false;
    private boolean _useWcsLog = false;
    private boolean _useAmbulancePost = false;
    private boolean _useCcPost = false;
    private boolean _runExposePost = true;

    private String _mainPid = null;
    private String _pid = null;
    private String _performanceString = null;
    private long _performanceTime = 0;

    private Map<String, String> _queries = null;
    private int _price = 0;
    private boolean _hasOfficial = false;
    private String _body = null;
    private String _homePath = null;
    private int _companyPage = 1;

    public NaverShopPacketPatternMessage(WebViewManager manager, KeywordItemMoon item) {
        super(manager, false);

        _cookieStatusAction = new NaverCookieStatusAction(manager.getWebView().getContext());
        _cookieOtherAction = new NaverCookieOtherAction(manager.getWebView().getContext());

        _item = item;
        _keyword = item.keyword;
        _mid = item.mid1;
        _mid2 = item.mid2;
        _startHomeMode = item.shopHome;

        _cookieController = new HttpCookieController();
        _httpEngine = new HttpEngine(_webViewManager.getWebView().getContext());
        _httpEngine.setCookieController(_cookieController);

        _isPacketShopHome = false;
        _mainReferer = false;
        _clickReferer = false;
        _mainCookie = false;
        _useLcsPost = _item.item.useLcsPost == 1;
        _useSlcPost = _item.item.useSlcPost == 1;
        _useFetchStoreProducts = _item.item.useFetchStoreProducts == 1;
        _useRandomNid = _item.item.useRandomNid == 1;
        _stayDelayType = _item.item.stayDelayType;
//        _packetPatternType = _item.item.packetPatternType;

        _isWifi = ((int) MathHelper.randomRange(0, 1) == 1);
        String genName = "2.2.4" + Math.random();
        _jqueryFuncName = "jQuery" + genName.replaceAll("\\D", "");

        // 임시로 강제한다. -> 서버와 동기화 전까지.
        _startHomeMode = KeywordItem.SHOP_HOME_SEARCH_DI;

        _useVoltPv = true;

        if (_item.item.workType == 101) {
            // 통검, 검색 레퍼럴 미포함/none, 클릭 레퍼럴 포함
            _mainReferer = false;
            _clickReferer = true;
        } else if (_item.item.workType == 102) {
            // 통검, 검색 레퍼럴 포함/same-site, 클릭 레퍼럴 포함
            _mainReferer = true;
            _clickReferer = true;
        } else if (_item.item.workType == 103) {
            _startHomeMode = KeywordItem.SHOP_HOME_MOBILE;
            _mainReferer = false;
            _clickReferer = false;
            _mainCookie = true;
            _useMobileAc = true;
            _useLcsGet = true;
//            _useRd0 = true;
//            _useRunPRd = true;
//            _useProductLog = true;
            _useWcsLog = true;
            _useAmbulancePost = true;
            _useCcPost = true;
            _delayType = 1;
            _useNewPacket = true;
        } else if (_item.item.workType == 104) {
            _mainReferer = true;
            _clickReferer = true;
            _mainCookie = true;
            _useLcsGet = true;
            _useWcsLog = true;
            _useAmbulancePost = true;
            _useCcPost = true;
            _delayType = 1;
            _useNewPacket = true;
        } else if (_item.item.workType == 105) {
            _startHomeMode = KeywordItem.SHOP_HOME_MOBILE;
            _mainReferer = false;
            _clickReferer = true;
            _mainCookie = true;
            _useLcsGet = true;
            _useWcsLog = true;
            _useAmbulancePost = true;
            _useCcPost = true;
            _delayType = 1;
        } else if (_item.item.workType == 106) {
            // 통검, 검색 레퍼럴 미포함/추가쿠키, 클릭 레퍼럴 미포함
            _startHomeMode = KeywordItem.SHOP_HOME_MOBILE;
            _mainReferer = false;
            _clickReferer = false;
            _mainCookie = true;
            _useRunPRd = true;
//            _runSimpleUrl = true;
        } else if (_item.item.workType == 107) {
            // 통검, 검색 레퍼럴 포함/추가쿠키, 클릭 레퍼럴 포함
            _startHomeMode = KeywordItem.SHOP_HOME_MOBILE;
            _mainReferer = true;
            _clickReferer = true;
            _mainCookie = true;
            _useRunPRd = true;
        } else if (_item.item.workType == 108) {
            // 통검, 검색 레퍼럴 포함/추가쿠키, 클릭 레퍼럴 미포함
            _mainReferer = true;
            _clickReferer = false;
            _mainCookie = true;
            _useRunPRd = true;
        } else if (_item.item.workType == 109) {
            // 통검, 검색 레퍼럴 미포함/추가쿠키, 클릭 레퍼럴 포함
            _startHomeMode = KeywordItem.SHOP_HOME_MOBILE;
            _mainReferer = false;
            _clickReferer = true;
            _mainCookie = true;
        } else if (_item.item.workType == 110) {
            // 통검, 검색 레퍼럴 포함/추가쿠키, 클릭 레퍼럴 포함
            _startHomeMode = KeywordItem.SHOP_HOME_MOBILE;
//            _packetPatternType = KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_VIEW;
            _mainReferer = true;
            _clickReferer = true;
            _mainCookie = true;
            _useMobileAc = true;
            _useLcsGet = true;
            _useRunPRd = true;
            _useProductLog = true;
            _useWcsLog = true;
            _useAmbulancePost = true;
            _useCcPost = true;

            _useNewPacket = true;
        } else if (_item.item.workType == 111) {
            // 통검, 검색 레퍼럴 포함/추가쿠키, 클릭 레퍼럴 포함
            _startHomeMode = KeywordItem.SHOP_HOME_MOBILE;
//            _packetPatternType = KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_VIEW;
            _mainReferer = true;
            _clickReferer = true;
            _mainCookie = true;
            _useMobileAc = true;
            _useRd0 = true;
            _useRunPRd = true;
            _useProductLog = true;
            _useWcsLog = true;
            _useAmbulancePost = true;
            _useCcPost = true;
        } else if (_item.item.workType == 112) {
            _mainReferer = false;
            _clickReferer = true;
            _mainCookie = true;
            _runShoppingTab = true;
            _runExposePost = false;
        } else if (_item.item.workType == 113) {
            _mainReferer = false;
            _clickReferer = true;
            _mainCookie = true;
            _runShoppingTab = true;
            _useWcsLog = true;
            _runExposePost = false;
        } else if (_item.item.workType == 121) {
            // 통검, 검색 레퍼럴 미포함/none, 클릭 레퍼럴 미포함, naver.com 시작.
            _startHomeMode = KeywordItem.SHOP_HOME_MOBILE;
//            _packetPatternType = KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_VIEW;
            _mainReferer = false;
            _clickReferer = false;
            _mainCookie = true;
            _useMobileAc = true;

            _useNewPacket = true;
        } else if (_item.item.workType == 122) {
            // 통검, 검색 레퍼럴 미포함/none, 클릭 레퍼럴 미포함, naver.com 시작.
            _startHomeMode = KeywordItem.SHOP_HOME_MOBILE;
            _mainReferer = false;
            _clickReferer = true;
            _mainCookie = true;
            _useMobileAc = true;
        } else if (_item.item.workType == 123) {
            // 빈검, 검색 레퍼럴 미포함/none, 클릭 레퍼럴 포함, 빈검색 시작.
            _startHomeMode = KeywordItem.SHOP_HOME_SEARCH_EMPTY;
            _mainReferer = false;
            _clickReferer = false;
            _mainCookie = true;
            _useMobileAc = true;
            _useLcsGet = true;
            _useRunPRd = true;
            _useProductLog = true;
            _useWcsLog = true;
            _useAmbulancePost = true;
            _useCcPost = true;

            _useNewPacket = true;
        } else if (_item.item.workType == 124) {
            // 통검, 검색 레퍼럴 미포함/none, 클릭 레퍼럴 미포함, naver.com 시작.
            _startHomeMode = KeywordItem.SHOP_HOME_MOBILE;
//            _packetPatternType = KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_OTHER;
            _mainReferer = false;
            _clickReferer = false;
            _mainCookie = true;
            _useMobileAc = true;
            _useLcsGet = true;
            _useRunPRd = true;
            _useProductLog = true;
            _useWcsLog = true;
            _useAmbulancePost = true;
            _useCcPost = true;

            _useNewPacket = true;
        } else if (_item.item.workType == 125) {
            // 통검, 검색 레퍼럴 미포함/none, 클릭 레퍼럴 미포함, naver.com 시작.
            _startHomeMode = KeywordItem.SHOP_HOME_MOBILE;
//            _packetPatternType = KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_B_CLICK;
            _mainReferer = false;
            _clickReferer = false;
            _mainCookie = true;
            _useMobileAc = true;
            _useLcsGet = true;
            _useRunPRd = true;
            _useProductLog = true;
            _useWcsLog = true;
            _useAmbulancePost = true;
            _useCcPost = true;

            _useNewPacket = true;
        } else if (_item.item.workType == 126) {
            // 빈검, 검색 레퍼럴 미포함/none, 클릭 레퍼럴 포함, 빈검색 시작.
            _startHomeMode = KeywordItem.SHOP_HOME_SEARCH_EMPTY;
            _mainReferer = false;
            _clickReferer = true;
            _useMobileAc = true;
        } else if (_item.item.workType == 131) {
            _packetPatternType = KeywordItem.PACKET_PATTERN_TYPE_MAIN;
            _mainReferer = false;
            _clickReferer = false;
        } else if (_item.item.workType == 132) {
            _packetPatternType = KeywordItem.PACKET_PATTERN_TYPE_MAIN;
            _mainReferer = true;
            _clickReferer = true;
        } else if (_item.item.workType == 133) {
            _packetPatternType = KeywordItem.PACKET_PATTERN_TYPE_MAIN;
            _mainReferer = false;
            _clickReferer = true;
        } else if (_item.item.workType == 151) {
            // 쇼검, 검색 레퍼럴 미포함/none, 클릭 레퍼럴 미포함
            _isPacketShopHome = true;
            _mainReferer = false;
            _clickReferer = false;
        } else if (_item.item.workType == 152) {
            // 쇼검, 검색 레퍼럴 포함/same-site, 클릭 레퍼럴 포함/추가쿠키
            _isPacketShopHome = true;
            _mainReferer = true;
            _clickReferer = true;
        } else if (_item.item.workType == 153) {
            // 쇼검, 검색 레퍼럴 미포함/none, 클릭 레퍼럴 포함/추가쿠키
            _isPacketShopHome = true;
            _mainReferer = false;
            _clickReferer = true;
        } else if (_item.item.workType == 200) {
            _mainReferer = true;
            _clickReferer = true;
            _mainCookie = true;
            _runExposePost = false;
        } else if (_item.item.workType == 201) {
            _mainReferer = true;
            _clickReferer = true;
            _mainCookie = true;
            _useWcsLog = true;
            _runExposePost = true;
        } else if (_item.item.workType == 202) {
            _mainReferer = true;
            _clickReferer = true;
            _runExposePost = false;
        } else if (_item.item.workType == 203) {
            _mainReferer = true;
            _clickReferer = true;
            _mainCookie = true;
            _runExposePost = false;
        } else {
            // 통검, 검색 레퍼럴 미포함/none, 클릭 레퍼럴 미포함
            _mainReferer = false;
            _clickReferer = false;
        }

        // 이것 작동 보장되지 않음.
        if (item.item.searchWork.length() > 0) {
            _keyword = item.item.searchWork;

            if (!_mid2.equals(".")) {
                _mid = _mid2;
                _mid2 = ".";
            }
        } else if (item.item.productName.length() > 0) {
            String str = item.item.productName;
            String storeName = item.item.storeName;

            if (storeName.length() > 0) {
                str = str.replace(item.item.storeName, "");
                storeName += " ";
            }

//            String[] strArray = str.trim().split("\\s+");
//            int length = strArray.length;
//            StringBuilder sb = new StringBuilder();
//            String productName = "";
//
//            if (length > 3) {
//                length = 3;
//            }
//
//            sb.append(strArray[0]);
//
//            for (int i = 1; i < length; i++) {
//                sb.append(" " + strArray[i]);
//            }
//
//            productName = sb.toString();
//            _keyword = storeName + productName;
            _keyword = storeName + str;

            if (!_mid2.equals(".")) {
                _mid = _mid2;
                _mid2 = ".";
            }
        }

        if (!TextUtils.isEmpty(_item.item.search1)) {
            _keyword = item.item.search1;
        }

        if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_PACKET_BOOST) {
            Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_SHOP_PACKET_BOOST " + _item.item.workType);
        } else {
            Log.d(TAG, "# 패턴 모드: PATTERN_TYPE_SHOP_PACKET " + _item.item.workType);
        }

        if (_packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_VIEW) {
            Log.d(TAG, "# 패킷 패턴 모드: PACKET_PATTERN_TYPE_A_CLICK_VIEW");
        } else if (_packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_OTHER) {
            Log.d(TAG, "# 패킷 패턴 모드: PACKET_PATTERN_TYPE_A_CLICK_OTHER");
        } else if (_packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_B_CLICK) {
            Log.d(TAG, "# 패킷 패턴 모드: PACKET_PATTERN_TYPE_A_CLICK_B_CLICK");
        } else if (_packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_B_CLICK_C_CLICK) {
            Log.d(TAG, "# 패킷 패턴 모드: PACKET_PATTERN_TYPE_A_CLICK_B_CLICK_C_CLICK");
        } else if (_packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_MAIN) {
            Log.d(TAG, "# 패킷 패턴 모드: PACKET_PATTERN_TYPE_MAIN");
            if (!TextUtils.isEmpty(_item.item.searchMain)) {
                _keyword = _item.item.searchMain;
            }
        } else {
            Log.d(TAG, "# 패킷 패턴 모드: PACKET_PATTERN_TYPE_DEFAULT");
        }

        if (_item.item.afterType == KeywordItem.AFTER_TYPE_POPULAR) {
            Log.d(TAG, "# 후처리 모드: AFTER_TYPE_POPULAR");
        } else if (_item.item.afterType == KeywordItem.AFTER_TYPE_REVIEW) {
            Log.d(TAG, "# 후처리 모드: AFTER_TYPE_REVIEW");
        } else {
            Log.d(TAG, "# 후처리 모드: AFTER_TYPE_NONE");
        }

        switch (_startHomeMode) {
            case KeywordItem.SHOP_HOME_SEARCH_DI:
            case KeywordItem.SHOP_HOME_MOBILE_SHOP_DI:
                _firstPattern = RUN_PACKET_SEARCH;
                break;
            default:
                _firstPattern = RUN_PACKET;
        }

        getResultAction().item = item;
    }

    @Override
    public void onHandleMessage(Handler handler, Message msg) {
        super.onHandleMessage(handler, msg);

        switch (msg.what) {
            case START_PATTERN: {
                Log.d(TAG, "# 네이버 쇼핑 패킷 작업 시작");
                _ua = UserManager.getInstance().ua;

                if (_item.item.patternType == KeywordItem.PATTERN_TYPE_SHOP_PACKET_BOOST) {
                    HttpEngine.useCookieManager = false;
                    _handler.sendEmptyMessage(GET_COOKIE);
                } else {
                    _nnbData = UserManager.getInstance().nnbData;
                    _chromeVersion = UserManager.getInstance().chromeVersion;
                    _browserVersion = UserManager.getInstance().browserVersion;
                    _nnb = UserManager.getInstance().nnb;
                    _httpEngine.setUa(_ua);
                    _httpEngine.setChromeVersion(_chromeVersion);
                    _httpEngine.setBrowserVersion(_browserVersion);
                    _httpEngine.setNnb(_nnb);
                    setBaseCookie();
                    _handler.sendEmptyMessage(_firstPattern);
                }
                break;
            }

            case GET_COOKIE: {
                Log.d(TAG, "# 쿠기 가져오기.");
                NnbAction action = new NnbAction();
                action.loginId = UserManager.getInstance().getLoginId(_webViewManager.getWebView().getContext());

                if (action.requestNnb() == 1) {
                    _nnbData = action.getNnbData();

                    if (!TextUtils.isEmpty(action.getUserAgent())) {
                        _ua = action.getUserAgent();
                    }

                    _chromeVersion = action.getChromeVersion();
                    _browserVersion = action.getBrowserVersion();
                    _nnb = action.getNnb();
                    _httpEngine.setUa(_ua);
                    _httpEngine.setChromeVersion(_chromeVersion);
                    _httpEngine.setBrowserVersion(_browserVersion);
                    _httpEngine.setNnb(_nnb);
                    // CookieManager 는 쿠키 굽는 웹뷰와 데이터상 오류가 발생하므로 사용하지 않는다.
//                    CookieManager cookieManager = CookieManager.getInstance();
//                    cookieManager.clear();

                    setBaseCookie();
                    sendMessageDelayed(_firstPattern, (_item.item.lowDelay > 0) ? 50 : 500);
                } else {
                    Log.d(TAG, "# 쿠키 가져오기 실패로 패턴 종료...");
                    sendMessageDelayed(END_PATTERN, (_item.item.lowDelay > 0) ? 50 : 500);
                }
                break;
            }

            case RUN_PACKET: {
                Log.d(TAG, "# 패킷 홈 이동");
                String homeUrl = "https://m.naver.com/";

                if (_isPacketShopHome) {
                    // 쇼검은 일단 패스.
                    _htmlString = _httpEngine.requestUrlWithOkHttpClient("https://m.shopping.naver.com/");
                } else {
                    if (_startHomeMode == KeywordItem.SHOP_HOME_SEARCH_EMPTY) {
//                        homeUrl = "https://m.search.naver.com/search.naver?sm=mtp_hty.top&where=m&query=";
                    }

                    _htmlString = _httpEngine.requestUrlWithOkHttpClient("https://m.naver.com/");
                }

                Log.d(TAG, "home result: " + _htmlString);

                if (TextUtils.isEmpty(_htmlString)) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112001;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    // 각종 관련 패킷 처리후 다음으로.
                    if (_isPacketShopHome) {
//                        _contentUrl = targetUrl;
                    } else {
                        String currentUrl = _httpEngine.getCurrentUrl();
                        _httpEngine.setReferer(currentUrl);

                        //https://tivan.naver.com/sc2/1/
                        //https://siape.veta.naver.com/openrtb/nurl?eu=EU10042822&calp=-&oj=dZ2qDmtmLJiMkEPOKu5OOq6beURlrgHvQxzW3BiW8TSLhSqWppOU%2B9DrjSPVdafO96o30MX8S%2Bn3nvvIR%2B84j3YUceogmmVMYXgfoAQz5rh0Awll8fPQgoOmQXdnTUW%2FDffpikPz2%2B4%2BBciBnJc6I56Yv9D79jsNAYiNNd3n%2FpnkHOJ%2F1JQS4bbBNbxbhGx4RfF06pMBmao3jr4axiQq%2FRvwkG6KJVxRvSf%2F3KRVSqKg%2BC2U6vUkVIHxSMryXkNS&ac=8732336&src=6368074&evtcd=BID_WIN&x_ti=1441&rk=077ecc7ed2fe5a85b65e1f37f391a8c1&eltts=i6EHOWLQCMWOg4LIM6EzoA%3D%3D&brs=Y&&dummy=1678153893699
                        //https://tivan.naver.com/sc2/11/
                        //https://m.search.naver.com/remote_frame
                        //https://m.naver.com/preview/index.json?bizTalk=yes&uid=1529254
                        //https://lcs.naver.com/m
                        //https://inspector-collector.m.naver.com/collect-post
                        //https://m.naver.com/panels/NEWS-CHANNEL.shtml
                        //https://m.naver.com/panels/SHOP-TREND.shtml
                        //https://l.m.naver.com/l?SOU&act=WEB.menu&menuOnList=NEWS%3BENT%3BSPORTS%3BSHOPPING%3BMYFEED%3BLECTURE%3BDATA%3BPLACE%3BBBOOM%3BCARGAME&menuList=NEWS%3BENT%3BSPORTS%3BSHOPPING%3BMYFEED%3BLECTURE%3BDATA%3BPLACE%3BBBOOM%3BCARGAME&oldMenuList=&menuOffList=BOOM&ni=GW7ESIE2TADGI&menuSetting=OFF&westMenuOnList=SHOP_VOGUE%3BSHOP-LIVE%3BARRIVAL-GUARANTEE%3BGIFT-SHOP%3BBEAUTY%3BFOOD-MARKET%3BLIVING%3BLIVINGHOME%3BPLUS-DEAL%3BCULTURE%3BSHOP-TODO&westMenuOffList=&westMenuSetting=OFF&ts=1678153894662&EOU
                        //https://l.m.naver.com/l?SOU&act=MSDT.lcs&sti=m_main_home&pid=GW7ESIE2TADGI-SEARCH-20230307105131-2635034&ugr=newmain&pmd=home&orientation=0&tz=%ED%95%9C%EA%B5%AD%20%ED%91%9C%EC%A4%80%EC%8B%9C&menuOnList=NEWS%3BENT%3BSPORTS%3BSHOPPING%3BMYFEED%3BLECTURE%3BDATA%3BPLACE%3BBBOOM%3BCARGAME&menuSetting=OFF&westMenuOnList=SHOP_VOGUE%3BSHOP-LIVE%3BARRIVAL-GUARANTEE%3BGIFT-SHOP%3BBEAUTY%3BFOOD-MARKET%3BLIVING%3BLIVINGHOME%3BPLUS-DEAL%3BCULTURE%3BSHOP-TODO&westMenuSetting=OFF&p60=&p90=&u60=&u90=&menu=SEARCH&subMenu=SEARCH&font=2&ts=1678153894785&EOU
                        //https://inspector-collector.m.naver.com/collect-post
                        //https://tivan.naver.com/sc2/12/
                        //https://siape.veta.naver.com/fxview?eu=EU10042822&calp=-&oj=dZ2qDmtmLJiMkEPOKu5OOq6beURlrgHvQxzW3BiW8TSLhSqWppOU%2B9DrjSPVdafO96o30MX8S%2Bn3nvvIR%2B84j3YUceogmmVMYXgfoAQz5rh0Awll8fPQgoOmQXdnTUW%2FDffpikPz2%2B4%2BBciBnJc6I56Yv9D79jsNAYiNNd3n%2FpnkHOJ%2F1JQS4bbBNbxbhGx4RfF06pMBmao3jr4axiQq%2FRvwkG6KJVxRvSf%2F3KRVSqKg%2BC2U6vUkVIHxSMryXkNS&ac=8732336&src=6368074&evtcd=V900&x_ti=1441&tb=&oid=&sid1=&sid2=&rk=077ecc7ed2fe5a85b65e1f37f391a8c1&eltts=i6EHOWLQCMWOg4LIM6EzoA%3D%3D&brs=Y&&eid=V900&tb=&dummy=0.6647478175046366
                        //https://tivan.naver.com/sc2/1/

                        //https://cc.naver.com/cc

                        if (_useNewPacket) {
                            String url = "https://ssl.pstatic.net/tveta/libs/glad/prod/u.html";
                            Log.d(TAG, "### tveta Url: " + url);
                            String htmlString3 = _httpEngine.requestUrlWithOkHttpClient(url, null, null, "navigate", "iframe");
                            Log.d(TAG, "tveta result: " + htmlString3);

                            if (htmlString3 == null) {
                                Log.d(TAG, "# 통신 오류로 패턴종료.");
                                _workCode = 112002;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                                break;
                            }
                        }

                        if (_useNewPacket) {
                            String url = "https://m.search.naver.com/remote_frame";
                            Log.d(TAG, "### remote_frame Url: " + url);
                            String htmlString3 = _httpEngine.requestUrlWithOkHttpClient(url, null, null, "navigate", "iframe");
                            Log.d(TAG, "remote_frame result: " + htmlString3);

                            if (htmlString3 == null) {
                                Log.d(TAG, "# 통신 오류로 패턴종료.");
                                _workCode = 112003;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                                break;
                            }
                        }

                        if (_useNewPacket) {
                            String genUrl = getLcsPostUrl();
                            Log.d(TAG, "### lcsPost genUrl: " + genUrl);

                            if (!TextUtils.isEmpty(genUrl)) {
                                Log.d(TAG, "lcsPost u,pidUrl: " + _httpEngine.getCurrentUrl());
                                String jsonBody = getParams(_htmlString, _httpEngine.getCurrentUrl(), "m_main_home", true, false);
                                Log.d(TAG, "lcsPost getParams: " + jsonBody);

                                _httpEngine.setOrigin("https://m.naver.com");
                                _httpEngine.setReferer("https://m.naver.com/");
                                String htmlString3 = _httpEngine.requestUrlPostWithOkHttpClientText(genUrl, jsonBody);
                                Log.d(TAG, "lcsPost result: " + htmlString3);
                            }
                        }


//                        if (_useLcsGet) {
//                            String url = getLcsUrl(htmlString, "m_smartstore_products");
//                            Log.d(TAG, "### lcs Url: " + url);
//                            String htmlString3 = _httpEngine.requestNaverMobileContentFromLcs(url);
//
//                            if (htmlString3 == null) {
//                                Log.d(TAG, "# 통신 오류로 패턴종료.");
//                                _workCode = 112012;
//                                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
//                                break;
//                            }
//                        }
//
//                        if (_useRd0) {
//                            String url = getRd0Url(htmlString);
//                            Log.d(TAG, "### rd0 Url: " + url);
//                            //https://m.search.naver.com/p/crd/rd?m=0&px=0&py=0&sx=-1&sy=-1&p=h%2FZT1wqVbxVss75kniCssssstAC-080982&q=%EC%9E%90%EC%A0%84%EA%B1%B0+%EB%A7%A4%ED%8A%B8&ie=utf8&rev=1&ssc=tab.m.all&f=m&w=m&s=t0uTZ6CpYarVrH0zo1AMHA%3D%3D&time=1676744408642&abt=%5B%7B%22eid%22%3A%22FBL-MAXCOLL%22%2C%22vid%22%3A%2224%22%7D%2C%7B%22eid%22%3A%22SBR1%22%2C%22vid%22%3A%22754%22%7D%5D&u=javascript&r=&i=&a=shf_tli.rkey
//                            //https://m.search.naver.com/p/crd/rd?m=0&px=0&py=0&sx=-1&sy=-1&p=h%2FZCZdqVWussscbO8Glssssssww-126787&q=%EB%8B%AD%EA%B0%88%EB%B9%84%EC%96%91%EB%85%90%&ie=utf8&rev=1&ssc=tab.m.all&f=m&w=m&s=u3LI6nvcGiEI2KK0i0AB9Q%3D%3D&time=1676747794673&abt=%5B%7B%22eid%22%3A%22SBR1%22%2C%22vid%22%3A%22761%22%7D%5D&u=javascript&r=&i=&a=shf_tli.rkey
//
//                            String htmlString2 = _httpEngine.requestNaverMobileContentFromRd0(url);
//                            Log.d(TAG, "rd0 결과: " + htmlString2);
//
//                            if (htmlString2 == null) {
//                                Log.d(TAG, "# 통신 오류로 패턴종료.");
//                                _workCode = 112013;
//                                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
//                                break;
//                            }
//                        }
//
//                        if (_useRunPRd) {
//                            String genUrl = getPRdUrl(htmlString, doc, elTagA, targetUrl);
//                            Log.d(TAG, "# Next URL(Parsed): " + genUrl);
//                            _contentUrl = genUrl;
//                        } else {
//                            Log.d(TAG, "# Next URL: " + targetUrl);
//                            _contentUrl = targetUrl;
//                        }

                        if (_useMobileAc) {
                            if (_useCcPost) {
                                _handler.sendEmptyMessageDelayed(RUN_PACKET_INPUT_CC, MathHelper.randomRange(500, 1500));
                            } else {
                                _handler.sendEmptyMessageDelayed(RUN_PACKET_INPUT, MathHelper.randomRange(500, 1500));
                            }
                            break;
                        }
                    }

                    _handler.sendEmptyMessageDelayed(RUN_PACKET_SEARCH, MathHelper.randomRange(500, 1500));
                }
                break;
            }

            case RUN_PACKET_INPUT_CC: {
                Log.d(TAG, "# 패킷 검색창 클릭 cc");

                if (_isPacketShopHome) {
                    // 쇼검은 일단 패스.
//                    aTagSel = getShopHomeMidSelector(_mid);
//                    htmlString = _httpEngine.requestUrlWithOkHttpClient("https://m.shopping.naver.com/");
                } else {
                    runSearchCcPost(_htmlString, true);
                }

                _handler.sendEmptyMessageDelayed(RUN_PACKET_INPUT, MathHelper.randomRange(500, 1500));
                break;
            }

            case RUN_PACKET_INPUT: {
                Log.d(TAG, "# 패킷 키워드 붙여넣기: " + _keyword);

                if (_isPacketShopHome) {
                    // 쇼검은 일단 패스.
//                    aTagSel = getShopHomeMidSelector(_mid);
//                    htmlString = _httpEngine.requestUrlWithOkHttpClient("https://m.shopping.naver.com/");
                } else {
                    //붙여넣기 패킷
                    //https://mac.search.naver.com/mobile/ac?_callback=_jsonp_0&q=%EB%B2%A0%EC%9D%B4%EB%B9%84%20%ED%81%AC%EB%A6%AC%EC%B8%A0%20%EC%A0%84%EC%9E%90%EB%8F%99%203%EC%BD%94%EC%9D%BC%20%EA%B3%A0%EC%86%8D%EB%AC%B4%EC%84%A0%EC%B6%A9%EC%A0%84&con=0&q_enc=UTF-8&st=1&frm=mobile_nv&r_format=json&r_enc=UTF-8&r_unicode=0&t_koreng=1&ans=2&run=2&rev=4
                    String parsed = WebViewManager.keywordEncodeForNaverInclPlus(_keyword);
                    String url = "https://mac.search.naver.com/mobile/ac?_callback=_jsonp_0&q=" + parsed + "&con=0&q_enc=UTF-8&st=1&frm=mobile_nv&r_format=json&r_enc=UTF-8&r_unicode=0&t_koreng=1&ans=2&run=2&rev=4";
                    Log.d(TAG, "### mobileAc Url: " + url);
                    _httpEngine.setOrigin(null);
                    String htmlString3 = _httpEngine.requestUrlWithOkHttpClient(url, "*/*", null, "no-cors", "script");
                    Log.d(TAG, "mobileAc result: " + htmlString3);

                    if (htmlString3 == null) {
                        Log.d(TAG, "# 통신 오류로 패턴종료.");
                        _workCode = 112011;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                        break;
                    }
                }

                _handler.sendEmptyMessageDelayed(RUN_PACKET_SEARCH, MathHelper.randomRange(500, 1500));
                break;
            }

            case RUN_PACKET_SEARCH: {
                Log.d(TAG, "# 패킷 검색: " + _keyword);

                if (_useCcPost) {
                    runSearchCcPost(_htmlString, false);

                    if (_step == 0 && _packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_OTHER) {
                    } else if (_step == 0 && _packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_VIEW) {
                    } else if (_step == 0 && _packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_B_CLICK) {
                    } else {
                    }
                }

                //https://cc.naver.com/cc
                //a=home.search&r=&i=&m=0&ssc=mtop.v5&p=0.-SEARCH-20230307112453-7926767&lcsurl=https%3A%2F%2Fm.naver.com%2F&lcssti=m_main_home&ua_mobile=true&ua_brand_0=Chromium&ua_version_0=110&ua_brand_1=Not%20A(Brand&ua_version_1=24&ua_brand_2=Google%20Chrome&ua_version_2=110&ua_brands=%5Bobject%20Object%5D%2C%5Bobject%20Object%5D%2C%5Bobject%20Object%5D&ua_model=Nexus%205&ua_platform=Android&ua_platformVersion=6.0&ua_uaFullVersion=110.0.5481.177&u=about%3Ablank
                //a=home.search&r=&i=&m=0&ssc=mtop.v5&p=X74MASVCVUDGI-SEARCH-20230307122121-8218844&lcsurl=https%3A%2F%2Fm.naver.com%2F&lcssti=m_main_home&u=about%3Ablank
//                    aTagSel = getHomeMidSelector(_mid);
//                    htmlString = _httpEngine.requestUrlWithOkHttpClient(Config.NAVER_HOME_MOBILE_URL);


                _performanceString = null;
                _htmlString = null;
                _httpEngine.setUa(_ua);
                _httpEngine.setChromeVersion(_chromeVersion);
                _httpEngine.setBrowserVersion(_browserVersion);
                _httpEngine.setNnb(_nnb);
                _httpEngine.setOrigin(null);

                if (_item.item.workType < 200) {
                    if (_isPacketShopHome) {
                        if (_mainReferer) {
                            _httpEngine.setReferer("https://m.shopping.naver.com/home");
                            _httpEngine.setUseDetailChUa(true);
                            _htmlString = _httpEngine.requestNaverShopMobileSearch(_keyword);
                        } else {
                            _httpEngine.setReferer(null);
                            _htmlString = _httpEngine.requestNaverShopMobileSearchFirst(_keyword);
                        }
                    } else {
                        if (_mainReferer) {
                            _httpEngine.setReferer("https://m.naver.com/");
                            _httpEngine.setUseDetailChUa(true);
                            _htmlString = _httpEngine.requestNaverMobileSearch(_keyword);
                        } else {
                            _httpEngine.setReferer(null);
                            _htmlString = _httpEngine.requestNaverMobileSearchFirst(_keyword);
                        }
                    }
                } else {
                    if (!_mid2.equals(".")) {
                        _mid = _mid2;
                        _mid2 = ".";
                    }

                    if (_item.item.workType == 202) {
                        _htmlString = _httpEngine.requestNaverMobileCatalogSearchFromShop(_keyword, _mid);
                    } else if (_item.item.workType == 203) {
                        _htmlString = _httpEngine.requestNaverMobileCatalogSearchEx(_keyword, _mid);
                    } else {
//                        URL url2 = new URL("ss");
//                        url2.getQuery();
                        _htmlString = _httpEngine.requestNaverShopMobileGoBuy(_mid);
                    }
                }

                if (TextUtils.isEmpty(_htmlString)) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112021;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    if (TextUtils.isEmpty(_item.item.search1)) {
                        if (_nnbData != null && !TextUtils.isEmpty(_nnbData.nidSes)) {
                            String currentUrl = _httpEngine.getCurrentUrl();
                            String cookies = _cookieController.getCookie(currentUrl);

                            if (!cookies.contains("NID_AUT") && !cookies.contains("NID_SES")) {
                                Log.d(TAG, "# 로그인 쿠키가 유요하지 않아 상태 업로드");
                                _cookieStatusAction.registerNaverCookieStatus(_nnbData.loginCookieId, 2);
                                _isLoginCookieExpired = true;
                            }
                        }

                        runSearchPage(_htmlString, getNextDelay());
                    } else {
                        // A -> B 클릭 방식.
                        String prev = _keyword;
                        _keyword = _item.keyword;
                        String tqi = _httpEngine.getValueFromHtml(_htmlString, "var g_puid");
                        try {
                            tqi = URLEncoder.encode(tqi, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        _contentUrl = _httpEngine.getNaverSearchSecondUrl(_keyword, prev, tqi);
                        _handler.sendEmptyMessageDelayed(RUN_PACKET_SECOND, getNextDelay());
                    }
                }
                break;
            }

            case RUN_PACKET_SECOND: {
                Log.d(TAG, "# 패킷 두번쨰 검색: " + _keyword);
                String htmlString = null;

                _performanceString = null;
                _httpEngine.setOrigin(null);

                if (_item.item.workType < 200) {
                    if (_isPacketShopHome) {
                        // 쇼검 방식 미완성.
                        if (_mainReferer) {
                            _httpEngine.setReferer("https://m.shopping.naver.com/home");
                        }

                        htmlString = _httpEngine.requestUrlWithOkHttpClient(_contentUrl);
                    } else {
                        if (_mainReferer) {
                            _httpEngine.setReferer(_httpEngine.getCurrentUrl());
                        }

                        htmlString = _httpEngine.requestUrlWithOkHttpClient(_contentUrl);
                    }
                } else {
                    if (_item.item.workType == 202) {
                        htmlString = _httpEngine.requestNaverMobileCatalogSearchFromShop(_keyword, _mid2);
                    } else {
//                        URL url2 = new URL("ss");
//                        url2.getQuery();
                        htmlString = _httpEngine.requestNaverMobileCatalogSearchEx(_keyword, _mid2);
                    }
                }

                if (TextUtils.isEmpty(htmlString)) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112031;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    runSearchPage(_htmlString, getNextDelay());
                }
                break;
            }

            case RUN_PACKET_SHOPPING_TAB: {
                Log.d(TAG, "# 쇼핑탭 클릭 패킷 전송");
                String url = _httpEngine.getCurrentUrl();
                _clickRefererUrl = url;
                _httpEngine.setReferer(url);
                _httpEngine.setOrigin(null);
                String htmlString = _httpEngine.requestNaverShopMobileSearchShoppingTab(_keyword);

                if (TextUtils.isEmpty(htmlString)) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112054;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    String url2 = _httpEngine.getCurrentUrl();
                    _clickRefererUrl = url2;
                    _httpEngine.setReferer(url2);

                    _runShoppingTab = false;
                    _isPacketShopHome = true;
                    _htmlString = htmlString;
                    runSearchPage(_htmlString, getNextDelay());
                }
                break;
            }

            case RUN_PACKET_CONTENT: {
                Log.d(TAG, "# 패킷(상품) 이동");
                String url = _httpEngine.getCurrentUrl();

                if (_clickReferer) {
                    _clickRefererUrl = url;
                } else {
                    _clickRefererUrl = "";
                }

                if (_clickReferer) {
                    if (_isPacketShopHome) {
//                    cookieManager.setCookie(".shopping.naver.com", "listOffset=2");
//                        _httpEngine.setReferer(url);
                    } else {
                        _httpEngine.setReferer(url);
                    }
                } else {
                    _httpEngine = new HttpEngine(_webViewManager.getWebView().getContext());
                    _httpEngine.setCookieController(_cookieController);
                    _httpEngine.setUa(_ua);
                    _httpEngine.setChromeVersion(_chromeVersion);
                    _httpEngine.setBrowserVersion(_browserVersion);
                    _httpEngine.setNnb(_nnb);
                    _httpEngine.setUrl(url);
                }

                String htmlString = _httpEngine.requestNaverMobileContentFromRd(_contentUrl);

                if (TextUtils.isEmpty(htmlString)) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112041;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    if (_mid2.equals(".")) {
                        if (_step == 0 && _packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_VIEW) {
                            ++_step;
                            _mid = _item.mid1;
                            Log.d(TAG, "# 뷰 패킷 클릭 다음: " + _mid);
                            runSearchPage(_htmlString, MathHelper.randomRange(1000, 2000));
                        } else if (_step == 0 && _packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_OTHER) {
                            if (!runPageCall(htmlString, "m_smartstore_products")) {
                                break;
                            }

                            ++_step;
                            _mid = _item.mid1;
                            Log.d(TAG, "# 패킷 클릭: " + _mid);
                            runSearchPage(_htmlString, MathHelper.randomRange(1000, 2000));
                        } else if (_step == 0 && _packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_B_CLICK) {
                            if (!runPageCall(htmlString, "m_smartstore_products")) {
                                break;
                            }

                            ++_step;

                            String[] keywords = _item.keyword.split(" ");

                            String prev = _keyword;
                            _keyword = _item.keyword;
                            String tqi = _httpEngine.getValueFromHtml(_htmlString, "var g_puid");
                            try {
                                tqi = URLEncoder.encode(tqi, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            _contentUrl = _httpEngine.getNaverSearchSecondUrl(_keyword, prev, tqi);
                            _handler.sendEmptyMessageDelayed(RUN_PACKET_SECOND, getNextDelay());
                        } else if (_step == 1 && _packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_B_CLICK) {
                            if (!runPageCall(htmlString, "m_smartstore_products")) {
                                break;
                            }

                            ++_step;

                            String prev = _keyword;
                            _keyword = _item.keyword;
                            String tqi = _httpEngine.getValueFromHtml(_htmlString, "var g_puid");
                            try {
                                tqi = URLEncoder.encode(tqi, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            _contentUrl = _httpEngine.getNaverSearchSecondUrl(_keyword, prev, tqi);
                            _handler.sendEmptyMessageDelayed(RUN_PACKET_SECOND, getNextDelay());
                        } else {
                            if (_runSimpleUrl) {
                                String urlString = _httpEngine.getCurrentUrl();
                                String newURL = urlString;
                                int index = urlString.indexOf('?');

                                if (index > -1) {
                                    newURL = urlString.substring(0, index);
                                }

                                _contentUrl = newURL;
                                _handler.sendEmptyMessageDelayed(RUN_PACKET_SIMPLE_URL, getSmallDelay());
                            } else {
                                if (!runPageCall(htmlString, "m_smartstore_products")) {
                                    break;
                                }

                                _htmlString = htmlString;
                                lastCcProcess(htmlString);
                            }
                        }
                    } else {
                        if (_item.item.workType >= 200) {
//                            _result = ResultAction.SUCCESS;
//                            _workCode = 112961;
//                            _handler.sendEmptyMessageDelayed(END_PATTERN, getSmallDelay());

                            _htmlString = htmlString;
                            lastCcProcess(htmlString);
                        } else {
//                            _workCode = 112910;
                            String aTagSel = "a.productContent_link_seller__p3N_C[data-i=" + _mid2 + "], a.productPerMall_link_seller__K8_B_[data-i=" + _mid2 + "], a.buyButton_link_buy__a_Zkc[data-i=" + _mid2 + "], a.officialSeller_link_seller__3_n22[data-i=" + _mid2 + "]";
                            Document doc = Jsoup.parse(htmlString);
                            Elements elTagA = doc.select(aTagSel);
                            String targetUrl = elTagA.attr("href");
//                    Log.d(TAG, "URL: " + targetUrl);

                            _catalogUrl = getCatalogAllUrl(doc);
                            Log.d(TAG, "gen url: " + _catalogUrl);

                            if (!TextUtils.isEmpty(targetUrl)) {
                                Log.d(TAG, "# Next URL: " + targetUrl);
                                _contentUrl = targetUrl;
                                _handler.sendEmptyMessageDelayed(RUN_PACKET_CONTENT_COMPANY, getSmallDelay());
                            } else {
                                Log.d(TAG, "# 가격비교 상품을 찾을 수 없어서 다음으로.");
                                // 공식 여부 판별.
                                Elements elTagOfficialA = doc.select(".officialSeller_link_seller__UC4IZ");
                                _hasOfficial = !elTagOfficialA.isEmpty();

                                // 최저가 찾기.
                                String price = _httpEngine.getValueFromHtml(htmlString, "\"lowestPrice\"", ":", "}");
                                if (!TextUtils.isEmpty(price)) {
                                    _price = Integer.parseInt(price);
                                    Log.d(TAG, "html price: " + _price);
                                }

//                                Elements elTagText = doc.select("em[data-testid=CATALOG_TOP_LOWEST_PRICE]");
//                                Element el = elTagText.first();
//
//                                if (el != null && !TextUtils.isEmpty(el.text())) {
//                                    _price = Integer.parseInt(el.text().replace(",", ""));
//                                    Log.d(TAG, "TAG price: " + el.text());
//                                }

                                _companyPage = 1;
                                _body = getBodyString();
                                _handler.sendEmptyMessageDelayed(RUN_PACKET_CONTENT_COMPANY_LIST, MathHelper.randomRange(300, 800));
                            }
                        }
                    }
                }
                break;
            }

            case RUN_PACKET_VOLT: {
                Log.d(TAG, "# 노출수 패킷 전송: " + _contentUrl);
                String body = HttpEngine.makeCeApiBody(_contentUrl);
                Log.d(TAG, "ce body: " + body);
                _httpEngine.setOrigin("https://msearch.shopping.naver.com");
                String htmlString = _httpEngine.requestNaverMobileCompanyContentFromCe(body);
                _httpEngine.setOrigin(null);
                Log.d(TAG, "ce result: " + htmlString);

                if (TextUtils.isEmpty(htmlString)) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112053;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    _handler.sendEmptyMessageDelayed(RUN_PACKET_CONTENT, getNextDelay());
                }
                break;
            }

            case RUN_PACKET_SIMPLE_URL: {
                Log.d(TAG, "# 상품페이지만 따로보기: " + _contentUrl);
                String htmlString = _httpEngine.requestNaverMobileContentFromRd(_contentUrl);

                if (TextUtils.isEmpty(htmlString)) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112042;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    _htmlString = htmlString;
                    lastCcProcess(htmlString);
                }
                break;
            }

            case RUN_PACKET_CONTENT_COMPANY: {
                Log.d(TAG, "# 패킷(전체 판매처) 이동");
                if (_clickReferer) {
                    if (_isPacketShopHome) {
//                        _cookieController.setCookie(".shopping.naver.com", "spage_uid=");
//                        _cookieController.setCookie(".shopping.naver.com", "listOffset=2");
                    } else {
//                        NFS=2; MM_PF=SEARCH; NNB=VL5YGRFE2DWWG; page_uid=h+3dpwpr4KGsseghhs8ssssstAo-344072; _naver_usersession_=0Sm03hYx96HuGitEfEdpYw==; sus_val=0c0APx2DQgeAM6MvpuLDpo6/; ncpa=95694|le6qkr2o|5ef27b8236773859ddba1a0b8bfb1047cf56ea72|95694|45fc9732564db834c816f78255a1f0060122c7a9; SHP_BID=9; spage_uid=h%2B3dpwpr4KGsseghhs8ssssstAo-344072
                    }

                    _httpEngine.setOrigin("https://msearch.shopping.naver.com");
                    _httpEngine.setReferer(_catalogUrl);
                } else {
                    _httpEngine = new HttpEngine(_webViewManager.getWebView().getContext());
                    _httpEngine.setCookieController(_cookieController);
                    _httpEngine.setUa(_ua);
                    _httpEngine.setChromeVersion(_chromeVersion);
                    _httpEngine.setBrowserVersion(_browserVersion);
                    _httpEngine.setNnb(_nnb);
                }

                String body = HttpEngine.makeCeApiBody(_contentUrl);
                Log.d(TAG, "ce body: " + body);
                String htmlString2 = _httpEngine.requestNaverMobileCompanyContentFromCe(body);
                Log.d(TAG, "ce result: " + htmlString2);

                if (TextUtils.isEmpty(htmlString2)) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112051;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    _httpEngine.setOrigin(null);
                    String htmlString = _httpEngine.requestNaverMobileContentFromRd(_contentUrl);

                    if (TextUtils.isEmpty(htmlString)) {
                        Log.d(TAG, "# 통신 오류로 패턴종료.");
                        _workCode = 112052;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    } else {
                        if (!runPageCall(htmlString, "m_smartstore_products")) {
                            break;
                        }

                        _htmlString = htmlString;
                        lastCcProcess(htmlString);
                    }
                }
                break;
            }

            case RUN_PACKET_CONTENT_COMPANY_LIST: {
                Log.d(TAG, "# 패킷(전체 판매처) 페이지 로드");
                if (_clickReferer) {
                    if (_isPacketShopHome) {
//                        _cookieController.setCookie(".shopping.naver.com", "spage_uid=");
//                        _cookieController.setCookie(".shopping.naver.com", "listOffset=2");
                    } else {
//                        NFS=2; MM_PF=SEARCH; NNB=VL5YGRFE2DWWG; page_uid=h+3dpwpr4KGsseghhs8ssssstAo-344072; _naver_usersession_=0Sm03hYx96HuGitEfEdpYw==; sus_val=0c0APx2DQgeAM6MvpuLDpo6/; ncpa=95694|le6qkr2o|5ef27b8236773859ddba1a0b8bfb1047cf56ea72|95694|45fc9732564db834c816f78255a1f0060122c7a9; SHP_BID=9; spage_uid=h%2B3dpwpr4KGsseghhs8ssssstAo-344072
                    }

                    _httpEngine.setOrigin("https://msearch.shopping.naver.com");
                    _httpEngine.setReferer(_catalogUrl);
                } else {
                    _httpEngine = new HttpEngine(_webViewManager.getWebView().getContext());
                    _httpEngine.setCookieController(_cookieController);
                    _httpEngine.setUa(_ua);
                    _httpEngine.setChromeVersion(_chromeVersion);
                    _httpEngine.setBrowserVersion(_browserVersion);
                    _httpEngine.setNnb(_nnb);
                }

                _httpEngine.setAddedHeader("sbth", "fdc9e2ae1fec629f0839960c04d0c33a79b1d0eb3ebc306cab08b66aaaa4c085a94cbdefb2f48124b42089200a7b7887");
                String jsonData = _httpEngine.requestNaverMobileCompanyContentFromGraphql(_body);
                _httpEngine.clearAddedHeader();
                String targetUrl = null;
                boolean notFound = false;

                if (TextUtils.isEmpty(jsonData)) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112061;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    try {
//                        Log.d("srgsg", jsonData);
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONObject dataVariables = jsonObject.getJSONObject("data");
                        JSONObject catalogProductsVariables = dataVariables.getJSONObject("catalog_Products");
                        JSONArray productsVariables = catalogProductsVariables.getJSONArray("products");

                        if (productsVariables.length() == 0) {
                            notFound = true;
                        } else {
                            for (int i = 0; i < productsVariables.length(); ++i) {
                                JSONObject object = productsVariables.getJSONObject(i);
                                Log.d("srgsg", "mid: " + _mid2 + ", nvMid: " + object.getString("nvMid"));
                                if (object.getString("nvMid").equals(_mid2)) {
                                    targetUrl = object.getString("mobileProductUrl");
                                    break;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(TAG, "# 가격비교 API 호출에 실패해서 패턴종료.");
                        _workCode = 112062;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    }

                    if (notFound) {
                        Log.d(TAG, "# 가격비교 상품을 찾을 수 없어서 패턴종료.");
                        _workCode = 112063;
                        _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    } else {
                        if (!TextUtils.isEmpty(targetUrl)) {
                            Log.d(TAG, "# Next URL: " + targetUrl);
                            _contentUrl = targetUrl;
                            _handler.sendEmptyMessageDelayed(RUN_PACKET_CONTENT_COMPANY, getSmallDelay());
                        } else {
                            // 200위 까지
                            if (_companyPage < 10) {
                                ++_companyPage;
                                _body = getBodyString();
                                _handler.sendEmptyMessageDelayed(RUN_PACKET_CONTENT_COMPANY_LIST, MathHelper.randomRange(300, 800));
                            } else {
                                Log.d(TAG, "# 가격비교 상품을 찾을 수 없어서 패턴종료.");
                                _workCode = 112064;
                                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                            }
                        }
                    }
                }
                break;
            }

            case RUN_PACKET_AFTER_PRODUCT_DETAIL: {
                Log.d(TAG, "# 패킷 상세정보 펼쳐보기 클릭");
                runProductDetailCcCall(_htmlString, "m_smartstore_products");
                _handler.sendEmptyMessageDelayed(RUN_PACKET_EXPOSE, MathHelper.randomRange(2000, 5000));
                break;
            }

            case RUN_PACKET_AFTER_POPULAR: {
                Log.d(TAG, "# 패킷 인기상품 전체보기 클릭");
                if (_useCcPost) {
                    runCcCall(_htmlString, "m_smartstore_products");
                }

                _httpEngine.setOrigin(null);
                _httpEngine.setReferer(_httpEngine.getCurrentUrl());
                String htmlString = _httpEngine.requestNaverMobileContentFromRd(_contentUrl);

                if (TextUtils.isEmpty(htmlString)) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112071;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    String currentUrl = _httpEngine.getCurrentUrl();
                    String sti = "m_smartstore_sub";
                    String referer = _httpEngine.getReferer();

                    if (currentUrl.contains("/category/")) {
                        sti = "m_smartstore_category";
                        _wcsRefererUrl = referer;
                        referer = _clickRefererUrl;
                    } else if (currentUrl.contains("/search")) {
                        sti = "m_smartstore_search";
                    } else if (currentUrl.contains("/bundle/")) {
                        sti = "m_smartstore_bundle";
                    } else if (currentUrl.endsWith(_homePath)) {
                        sti = "m_smartstore_home";
                        // 이건 주소 바꾸는게 아니라 데이터 교체 방식일때.
//                        _wcsRefererUrl = referer;
//                        referer = _clickRefererUrl;
                    }

                    _clickRefererUrl = referer;

                    if (!runPageCall(htmlString, sti)) {
                        break;
                    }

                    successPattern();
                }
                break;
            }

            case RUN_PACKET_AFTER_REVIEW: {
                Log.d(TAG, "# 패킷 리뷰 클릭");
                _useNewAfter = true;    // 임시로 강제 활성화.

                if (!fetchReviewsEvaluations(_htmlString)) {
                }

                if (!fetchReviewsAttaches(_htmlString)) {
                }

                successPattern();
                break;
            }

            case RUN_PACKET_EXPOSE: {
                Log.d(TAG, "# 상세 하단 이동후 패킷 전송");
                runExposesPost(_htmlString);
                lastProcess(_htmlString);
                break;
            }

            case END_PATTERN: {
                // 작업종료.
                Log.d(TAG, "# 네이버 쇼핑 패킷 패턴 종료");

                // 쿠키 정보 있으면 업로드.
                if (_nnbData != null && _nnbData.naverCookieId > 0) {
                    // 일단 패킷버전 정보 중지.
//                    String others = _httpEngine.getAllSetCookieString();
//                    _cookieOtherAction.registerNaverCookieOthers(_nnbData.naverCookieId, others);
                }

                registerResultFinish();
                sendEndPatternMessage();
                break;
            }

            case PAUSE_PATTERN: {
                Log.d(TAG, "# 패턴 중단");
                break;
            }
        }
    }

    public void setBaseCookie() {
        // 기본 쿠키 설정.
        if (_isPacketShopHome) {
        } else {
            if (_mainCookie) {
                if (_item.item.workType < 200) {
//                    _cookieController.setCookie(".naver.com", "NFS=2");
                    _cookieController.setCookie(".naver.com", "MM_PF=SEARCH");

                    if (_nnbData != null && !TextUtils.isEmpty(_nnbData.nidSes)) {
                        _cookieController.setCookie("m.naver.com", "MM_LAST_REFERER=https://nid.naver.com/");
                    }
                } else {
                    _cookieController.setCookie(".naver.com", "MM_PF=SEARCH");
                    _cookieController.setCookie(".shopping.naver.com", "spage_uid=");
                }
            }
        }

        _cookieController.setCookie(".naver.com", "NNB=" + _nnb);

        if (_nnbData != null && !TextUtils.isEmpty(_nnbData.nidSes)) {
            Log.d(TAG, "#### nid_inf: " + _nnbData.nidInf);
            Log.d(TAG, "#### NID_AUT: " + _nnbData.nidAut);
            Log.d(TAG, "#### NID_SES: " + _nnbData.nidSes);
            Log.d(TAG, "#### NID_JKL: " + _nnbData.nidJkl);

            _cookieController.setCookie(".naver.com", "nid_inf=" + _nnbData.nidInf);
            _cookieController.setCookie(".naver.com", "NID_AUT=" + _nnbData.nidAut);
            _cookieController.setCookie(".naver.com", "NID_SES=" + _nnbData.nidSes);
            _cookieController.setCookie(".naver.com", "NID_JKL=" + _nnbData.nidJkl);

            if (!TextUtils.isEmpty(_nnbData.naverCookieOther)) {
                String[] cookieStrings = _nnbData.naverCookieOther.split("\n");

                for (String cookieString : cookieStrings) {
                    List<HttpCookie> cookieList = HttpCookie.parse(cookieString);

                    for (HttpCookie cookie : cookieList) {
                        if (!TextUtils.isEmpty(cookie.getDomain()) && !cookie.hasExpired()) {
                            String[] values = cookieString.split(";");
                            _cookieController.setCookie(cookie.getDomain(), values[0].trim());
                            Log.d(TAG, "Set cookie: " + cookieString);
                        }
                    }
                }
            }
        }

        if (_useRandomNid) {
            String nidInf = MathHelper.randomRange(1100000000, 1300000000) + "";
            String nidAut = RandomStringUtils.randomAlphabetic(1) + Utility.getRandomStringNew(63);
            String nidSes = "AAAB" + Utility.getRandomStringNew(535) + "=";
            String nidJkl = RandomStringUtils.randomAlphabetic(1) + Utility.getRandomStringNew(42) + "=";
//                            String nidInf = "1281341740";
//                            String nidAut = "zYmCffR8oNsx0PBTCrbzQ01IR1/S/YdPwFfuPc8qqYufU6v+PZXz2sxLExtgI2JE";
//                            String nidSes = "AAABkPcKZ5dLmazpPU4fvvpV+s01GCa2ttqua9WMiabeIafeTLf6ZuMf3hl4Nx0PbxxdsPqq7sliBlHOpuB5Wg5I90fa7yq36V4sCVN4FGcV4PqRzpb12QNf/x4vKy1qA00RqukUoN03Q5jp35CA0EtGzRN1h0IkDzNWquovnc1tkGJeWVL/NQErAjvKMQ7R7rIMU11r8JJcQtHc6003Bf5nF9BqILQ+2yrJXm8V5i0+TyChJs9EG90PYN8T5rjVGY0RPFjSW5C1KyoJsQmFnRswIzdwvks5A7cetRmtURtGhTaqLpFeSJqHjcFJoXalTOmfZb5kc1aUv9i06jGNyar0WejhaBfk4/rSqGaVzAlz+87BwJpNV+XLFcMLKwnjNTocummP+H1e8Pjy0y7AIY2jvf8TgDPwhYfDzF56HippnNEHa3g3fNCRh8jQEv33D8vzEbICZrimIxPUMlF9ZIw/x6tsoW5ESiUnnTIk0xc3NvHbopVR3ouHCia9+w6hanOecUnQjaBDGOjrg4FoSJ4hHQo=";
//                            String nidJkl = "qjtw9djGGjn6qzTmYsOHHGmLGt1GYqgwjWtbfTLtfqw=";
//                            String nipd = "qjtw9djGGjn6qzTmYsOHHGmLGt1GYqgwjWtbfTLtfqw=";

            Log.d(TAG, "#### nid_inf: " + nidInf);
            Log.d(TAG, "#### NID_AUT: " + nidAut);
            Log.d(TAG, "#### NID_SES: " + nidSes);
            Log.d(TAG, "#### NID_JKL: " + nidJkl);

            _cookieController.setCookie(".naver.com", "nid_inf=" + nidInf);
            _cookieController.setCookie(".naver.com", "NID_AUT=" + nidAut);
            _cookieController.setCookie(".naver.com", "NID_SES=" + nidSes);
            _cookieController.setCookie(".naver.com", "NID_JKL=" + nidJkl);
            // 로그아웃 할때만 확인된다.
//                            _cookieController.setCookie(".naver.com", "NIPD=" + nipd);
        }
    }

    public Element getElement(Document doc) {
        String aTagSel = null;

        if (_step == 0 && _packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_OTHER) {
            // A -> 다른 mid 랜덤클릭 -> mid 클릭
            if (_item.item.workType < 200) {
                if (_isPacketShopHome) {
                    aTagSel = getShopHomeNotMidSelector(_mid);
                } else {
                    aTagSel = getHomeNotMidSelector(_mid);
                }
            } else {
                // 여긴 정민이 출처 심플버전용.
                aTagSel = ".product_btn_link__XRWYu[href*=Mid=" + _mid2 + "]";
            }
        } else if (_step == 0 && _packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_VIEW) {
            // A -> view 랜덤클릭 -> mid 클릭
            aTagSel = "a.total_tit";
        } else if (_step == 0 && _packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_MAIN) {
            if (_item.item.workType < 200) {
                if (_isPacketShopHome) {
//                    aTagSel = getShopHomeNotMidSelector(_mid);
                } else {
                    aTagSel = getHomeMidsSelector();
                }
            } else {
                // 여긴 정민이 출처 심플버전용.
//                aTagSel = ".product_btn_link__XRWYu[href*=Mid=" + _mid2 + "]";
            }
        } else {
            // A mid 클릭 방식.
            if (_item.item.workType < 200) {
                if (_isPacketShopHome) {
                    aTagSel = getShopHomeMidSelector(_mid);
                } else {
                    aTagSel = getHomeMidSelector(_mid);
                }
            } else {
                // 여긴 정민이 출처 심플버전용.
                aTagSel = ".product_btn_link__XRWYu[href*=Mid=" + _mid + "]";
            }
        }

        Elements elTagAs = doc.select(aTagSel);
        Element elTagA = null;

        if (elTagAs.size() > 1) {
            elTagA = elTagAs.get((int) MathHelper.randomRange(0, elTagAs.size() - 1));

            if (_step == 0 && _packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_VIEW) {
                Log.d(TAG, "# 대상 url: " + elTagA.attr("href") + " / " + elTagA.text());
            } else if (_step == 0 && _packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_A_CLICK_OTHER) {
                _mid = elTagA.parent().dataset().get("nvmid");
                Log.d(TAG, "# 대상 mid: " + _mid + " / " + elTagA.select(".title"));
            } else {
                _mid = elTagA.parent().dataset().get("nvmid");
                Log.d(TAG, "# 대상 mid: " + _mid + " / " + elTagA.select(".title"));
            }
        } else {
            if (_item.item.workType < 200) {
                if (_isPacketShopHome) {
                    aTagSel = getShopHomeMidSelector(_mid);
                } else {
                    aTagSel = getHomeMidSelector(_mid);
                }
            } else {
                // 여긴 정민이 출처 심플버전용.
                aTagSel = ".product_btn_link__XRWYu[href*=Mid=" + _mid + "]";
            }

            elTagAs = doc.select(aTagSel);
            elTagA = elTagAs.first();
        }

        return elTagA;
    }

    public boolean runPageCall(String htmlString, String sti) {
        _performanceString = null;
        _baseJsonObject = null;

        if (!runProductLog(htmlString)) {
            return false;
        }

        if (!runLcsPost(htmlString, sti)) {
            return false;
        }

        if (!runSlcPost(htmlString, sti)) {
            return false;
        }

        if (sti.equals("m_smartstore_products")) {
            if (!runProductsCall(htmlString)) {
                return false;
            }
        }

//        if (!runInflowBenefits(htmlString, sti)) {
//            return false;
//        }

        if (!runAmbulancePost(htmlString, sti)) {
            return false;
        }

        if (!runWcsLog(htmlString, sti)) {
            return false;
        }

        return true;
    }

    public boolean runCcCall(String htmlString, String sti) {
        String py = (int) MathHelper.randomRange(6000, 9600) + "";

//        if (order == 1) {
//            py = (int) MathHelper.randomRange(250, 380) + "";
//        }

        if (!runCcPost(htmlString, sti, py)) {
            return false;
        }

        if (!runSlcCcPost(htmlString, sti, py)) {
            return false;
        }

        return true;
    }

    public boolean runProductDetailCcCall(String htmlString, String sti) {
        String py = (int) MathHelper.randomRange(2300, 3500) + "";

//        if (order == 1) {
//            py = (int) MathHelper.randomRange(250, 380) + "";
//        }

        if (!runProductDetailCcPost(htmlString, sti, py)) {
            return false;
        }

        if (!runProductDetailSlcCcPost(htmlString, sti, py)) {
            return false;
        }

        return true;
    }

    public boolean runProductsCall(String htmlString) {
        if (!fetchStoreProducts(htmlString)) {
            return false;
        }

        if (!fetchBenefits(htmlString)) {
            return false;
        }

        if (!fetchPrediction(htmlString)) {
            return false;
        }

        if (!fetchStoreContents(htmlString)) {
            return false;
        }

        return true;
    }

    public boolean runSearchPage(String htmlString, long delay) {
        Document doc = Jsoup.parse(htmlString);
        Element elTagA = getElement(doc);
        String targetUrl = null;

        if (elTagA != null) {
            targetUrl = elTagA.attr("href");
        }

        if (TextUtils.isEmpty(targetUrl)) {
            Log.d(TAG, "# 상품을 찾을 수 없어서 패턴종료.");
            _workCode = 112022;
            _handler.sendEmptyMessageDelayed(END_PATTERN, 500);

            return false;
        } else if (_runShoppingTab) {
            _handler.sendEmptyMessageDelayed(RUN_PACKET_SHOPPING_TAB, delay);
        } else {
            Log.d(TAG, "Next URL: " + targetUrl);
            if (_isPacketShopHome) {
                _contentUrl = targetUrl;
            } else {
                if (_packetPatternType == KeywordItem.PACKET_PATTERN_TYPE_MAIN) {
                    targetUrl = _webViewManager.changeQueryValue(targetUrl, "nv_mid", _item.mid1);

                    if (!TextUtils.isEmpty(_item.item.catId)) {
                        targetUrl = _webViewManager.changeQueryValue(targetUrl, "cat_id", _item.item.catId);
                    }

                    Log.d(TAG, "Next URL(Fixed): " + targetUrl);
                }

                if (!runSearchPageProcess(targetUrl, htmlString, doc, elTagA)) {
                    return false;
                }
            }

            if (!_isPacketShopHome && _item.item.workType < 200) {
                _handler.sendEmptyMessageDelayed(RUN_PACKET_CONTENT, delay);
            } else {
                if (!runVoltPv()) {
                    return false;
                }

                // 이 페이지는 무조건 5초는 기달려야 한다.
                _handler.sendEmptyMessageDelayed(RUN_PACKET_VOLT, 5000);
            }
        }

        return true;
    }

    public boolean runSearchPageProcess(String targetUrl, String htmlString, Document doc, Element elTagA) {
        _clickRefererUrl = _httpEngine.getReferer();
        String currentUrl = _httpEngine.getCurrentUrl();
        String mainKeyword = _item.item.searchMain;

        if (TextUtils.isEmpty(_clickRefererUrl)) {
            _clickRefererUrl = "";
        }

        if (!TextUtils.isEmpty(mainKeyword)) {
            String parsed = WebViewManager.keywordEncodeForNaver(mainKeyword);
            currentUrl = _webViewManager.changeQueryValue(currentUrl, "query", parsed);
            targetUrl = _webViewManager.changeQueryValue(targetUrl, "query", parsed);
            Log.d(TAG, "키워드: " + _keyword + " -> " + mainKeyword);
            _keyword = mainKeyword;
            _httpEngine.setCurrentUrl(currentUrl);
        }

        _httpEngine.setReferer(currentUrl);

        if (_useNewPacket) {
            String url = "https://ssl.pstatic.net/sstatic/sdyn.js?f=/fe/sfe/nx/mobile/nx_221027.js+/au/m/_common/nhn.common_210105.js+/au/module/requirejs/require-2.3.5.js+/fe/sfe/post-requirejs/mobile/app_221215.js+/fe/meerkat/logger/sfe/naver.common.meerkat.logger.sfe_221027.js+/fe/sfe/web-vitals/web-vitals_230223.js+/fe/sfe/scrollLog/Controller_220715.js+/fe/sfe/sponsor/Controller_221114.js&o=m.search";
            Log.d(TAG, "### sdyn Url: " + url);
            _httpEngine.setOrigin("https://m.search.naver.com");
            String htmlString3 = _httpEngine.requestUrlWithOkHttpClient(url, "*/*", null, "cors", "script");
            _httpEngine.setOrigin(null);
            Log.d(TAG, "sdyn result: " + htmlString3);

            if (htmlString3 == null) {
                Log.d(TAG, "# 통신 오류로 패턴종료.");
                _workCode = 112023;
                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                return false;
            }
        }

        if (_useLcsGet) {
            String url = getLcsUrl(htmlString, "m_smartstore_products");
            Log.d(TAG, "### lcsGet Url: " + url);
            String htmlString3 = _httpEngine.requestNaverMobileContentFromLcs(url);

            if (htmlString3 == null) {
                Log.d(TAG, "# 통신 오류로 패턴종료.");
                _workCode = 112024;
                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                return false;
            }
        }

        if (_useNewPacket) {
            String url = getErUrl(htmlString);
            Log.d(TAG, "### getEr Url: " + url);

            if (!TextUtils.isEmpty(url)) {
                String htmlString3 = _httpEngine.requestUrlWithOkHttpClientImage(url);
                Log.d(TAG, "getEr result: " + htmlString3);

                if (htmlString3 == null) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112029;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    return false;
                }
            }
        }

        if (_useNewPacket) {
            String url = getShoppingSearchUrl1(htmlString);
            Log.d(TAG, "### shoppingSearch1 Url: " + url);

            if (!TextUtils.isEmpty(url)) {
                String htmlString3 = _httpEngine.requestUrlWithOkHttpClient(url, "*/*", null, "no-cors", "script");
                Log.d(TAG, "shoppingSearch1 result: " + htmlString3);

                if (htmlString3 == null) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112025;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    return false;
                }
            }
        }

        if (_useNewPacket) {
            String url = getShoppingSearchUrl2(htmlString);
            Log.d(TAG, "### shoppingSearch2 Url: " + url);

            if (!TextUtils.isEmpty(url)) {
                String htmlString3 = _httpEngine.requestUrlWithOkHttpClient(url, "*/*", null, "no-cors", "script");
                Log.d(TAG, "shoppingSearch2 result: " + htmlString3);

                if (htmlString3 == null) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112026;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    return false;
                }
            }
        }

        if (_useNewPacket) {
            String url = getShoppingSearchUrl3(htmlString);
            Log.d(TAG, "### shoppingSearch3 Url: " + url);

            if (!TextUtils.isEmpty(url)) {
                String htmlString3 = _httpEngine.requestUrlWithOkHttpClient(url, "*/*", null, "no-cors", "script");
                Log.d(TAG, "shoppingSearch3 result: " + htmlString3);

                if (htmlString3 == null) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112027;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    return false;
                }
            }
        }

        if (_useRd0) {
            String url = getRd0Url(htmlString);
            Log.d(TAG, "### rd0 Url: " + url);
            //https://m.search.naver.com/p/crd/rd?m=0&px=0&py=0&sx=-1&sy=-1&p=h%2FZT1wqVbxVss75kniCssssstAC-080982&q=%EC%9E%90%EC%A0%84%EA%B1%B0+%EB%A7%A4%ED%8A%B8&ie=utf8&rev=1&ssc=tab.m.all&f=m&w=m&s=t0uTZ6CpYarVrH0zo1AMHA%3D%3D&time=1676744408642&abt=%5B%7B%22eid%22%3A%22FBL-MAXCOLL%22%2C%22vid%22%3A%2224%22%7D%2C%7B%22eid%22%3A%22SBR1%22%2C%22vid%22%3A%22754%22%7D%5D&u=javascript&r=&i=&a=shf_tli.rkey
            //https://m.search.naver.com/p/crd/rd?m=0&px=0&py=0&sx=-1&sy=-1&p=h%2FZCZdqVWussscbO8Glssssssww-126787&q=%EB%8B%AD%EA%B0%88%EB%B9%84%EC%96%91%EB%85%90%&ie=utf8&rev=1&ssc=tab.m.all&f=m&w=m&s=u3LI6nvcGiEI2KK0i0AB9Q%3D%3D&time=1676747794673&abt=%5B%7B%22eid%22%3A%22SBR1%22%2C%22vid%22%3A%22761%22%7D%5D&u=javascript&r=&i=&a=shf_tli.rkey

            String htmlString2 = _httpEngine.requestNaverMobileContentFromRd0(url);
            Log.d(TAG, "rd0 결과: " + htmlString2);

            if (htmlString2 == null) {
                Log.d(TAG, "# 통신 오류로 패턴종료.");
                _workCode = 112028;
                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                return false;
            }
        }

        if (_useRunPRd) {
            String genUrl = getPRdUrl(htmlString, doc, elTagA, targetUrl);
            Log.d(TAG, "# Next URL(Parsed): " + genUrl);
            _contentUrl = genUrl;
            _httpEngine.setUseDetailChUa(true);
        } else {
            Log.d(TAG, "# Next URL: " + targetUrl);
            _contentUrl = targetUrl;
        }

        return true;
    }

    public void lastCcProcess(String htmlString) {
        boolean runDetailButtonClick = false;
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "lastCcProcess currentUrl: " + urlString);

        if (_item.item.afterType > 0 && urlString.contains("naver.com")) {
            runDetailButtonClick = true;
        }

        fetchCategoryProducts(htmlString);

        if (runDetailButtonClick) {
            long delay = getStayDelay();
            Log.d(TAG, "# 후처리: " + (delay / 1000.0) + "초 후");
            _htmlString = htmlString;
            _handler.sendEmptyMessageDelayed(RUN_PACKET_AFTER_PRODUCT_DETAIL, delay);
        } else {
            _htmlString = htmlString;
            _handler.sendEmptyMessageDelayed(RUN_PACKET_EXPOSE, MathHelper.randomRange(2000, 5000));
        }
    }

    public void lastProcess(String htmlString) {
        boolean runLastMove = false;
        boolean runLastAction = false;
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "lastProcess currentUrl: " + urlString);

        if (_item.item.afterType > 0 && urlString.contains("naver.com")) {
            if (_item.item.afterType == KeywordItem.AFTER_TYPE_POPULAR) {
                runLastMove = true;
            } else if (_item.item.afterType == KeywordItem.AFTER_TYPE_REVIEW) {
                runLastAction = true;
            } else if (_item.item.afterType == KeywordItem.AFTER_TYPE_QA) {
                runLastAction = true;
            }
        }

        if (runLastMove) {
            Document doc = Jsoup.parse(htmlString);
            String aTagSel = null;
            Elements elTagA = null;

            if (_item.item.afterType == KeywordItem.AFTER_TYPE_POPULAR) {
                // 인기상품 전체보기
                aTagSel = "a._1iZ-3SrGbc";
                elTagA = doc.select(aTagSel);

                if (elTagA.isEmpty()) {
                    aTagSel = null;
                }
            }

            if (TextUtils.isEmpty(aTagSel)) {
                aTagSel = "a._1WTXU7spDS" +         // 스토어 홈
//                        ", a._2CEl2aEeO7" +         // 배송비 절약상품(상단 배송정보)
//                        ", a._3DlaPsr4xg" +         // 태그 링크
//                        ", a._237F4kum82" +         // 숏클립
//                        ", a._4wVjvZyxXq" +         // 배송비 절약상품
//                        ", a[href$=additional]" +   // 구매 추가정보
//                        ", a[href$=contact]" +      // 상품정보 제공고시
//                        ", a[href$=exchange]" +     // 반품/교환안내
//                        ", a[href$=seller]" +       // 판매자정보
//                        ", a[href$=caution]" +      // 주의사항
                "";
            }

            Elements elHomeA = doc.select("a._1WTXU7spDS");
            _homePath = elHomeA.attr("href");
            Log.d(TAG, "home path: " + _homePath);

            elTagA = doc.select(aTagSel);
            Log.d(TAG, "link Count: " + elTagA.size());
//            for (int i = 0; i < elTagA.size(); ++i) {
//                Log.d(TAG, "class: " + elTagA.get(i).className() + " /URL: " + elTagA.get(i).attr("href"));
//            }

            if (elTagA.size() > 0) {
                String targetUrl = elTagA.get((int) MathHelper.randomRange(0, elTagA.size() - 1)).attr("href");
                Log.d(TAG, "URL: " + targetUrl);

                if (TextUtils.isEmpty(targetUrl)) {
                    Log.d(TAG, "# 후처리 링크를 찾을 수 없어서 패턴종료.");
                    _workCode = 112081;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                } else {
                    if (!targetUrl.startsWith("https")) {
                        _contentUrl = "https://m.smartstore.naver.com" + targetUrl;
                    } else {
                        _contentUrl = targetUrl;
                    }

                    long delay = getStayDelay();
                    Log.d(TAG, "# 후처리: " + (delay / 1000.0) + "초 후");
                    _htmlString = htmlString;
                    _handler.sendEmptyMessageDelayed(RUN_PACKET_AFTER_POPULAR, delay);
                }
            } else {
                Log.d(TAG, "# 후처리 링크를 찾을 수 없어서 패턴종료.");
                _workCode = 112082;
                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
            }
        } else if (runLastAction) {
            if (_item.item.afterType == KeywordItem.AFTER_TYPE_REVIEW) {
                long delay = getStayDelay();
                Log.d(TAG, "# 후처리: " + (delay / 1000.0) + "초 후");
                _htmlString = htmlString;
                _handler.sendEmptyMessageDelayed(RUN_PACKET_AFTER_REVIEW, delay);
            } else if (_item.item.afterType == KeywordItem.AFTER_TYPE_QA) {
                long delay = getStayDelay();
                Log.d(TAG, "# 후처리: " + (delay / 1000.0) + "초 후");
                _htmlString = htmlString;
                _handler.sendEmptyMessageDelayed(RUN_PACKET_AFTER_POPULAR, delay);
            }
        } else {
            successPattern();
        }
    }

    public boolean successPattern() {
        _result = ResultAction.SUCCESS;

        if (_item.item.workType == 101) {
            _workCode = 112931;
        } else if (_item.item.workType == 102) {
            _workCode = 112932;
        } else if (_item.item.workType == 103) {
            _workCode = 112933;
        } else if (_item.item.workType == 104) {
            _workCode = 112934;
        } else if (_item.item.workType == 105) {
            _workCode = 112935;
        } else if (_item.item.workType == 106) {
            _workCode = 112936;
        } else if (_item.item.workType == 107) {
            _workCode = 112937;
        } else if (_item.item.workType == 108) {
            _workCode = 112938;
        } else if (_item.item.workType == 109) {
            _workCode = 112939;
        } else if (_item.item.workType == 110) {
            _workCode = 112940;
        } else if (_item.item.workType == 111) {
            _workCode = 112941;
        } else if (_item.item.workType == 112) {
            _workCode = 112942;
        } else if (_item.item.workType == 113) {
            _workCode = 112943;
        } else if (_item.item.workType == 114) {
            _workCode = 112944;
        } else if (_item.item.workType == 115) {
            _workCode = 112945;
        } else if (_item.item.workType == 121) {
            _workCode = 112951;
        } else if (_item.item.workType == 122) {
            _workCode = 112952;
        } else if (_item.item.workType == 123) {
            _workCode = 112953;
        } else if (_item.item.workType == 124) {
            _workCode = 112954;
        } else if (_item.item.workType == 125) {
            _workCode = 112955;
        } else if (_item.item.workType == 126) {
            _workCode = 112956;
        } else if (_item.item.workType == 127) {
            _workCode = 112957;
        } else if (_item.item.workType == 128) {
            _workCode = 112958;
        } else if (_item.item.workType == 129) {
            _workCode = 112959;
        } else if (_item.item.workType == 130) {
            _workCode = 112960;
        } else if (_item.item.workType == 131) {
            _workCode = 112961;
        } else if (_item.item.workType == 132) {
            _workCode = 112962;
        } else if (_item.item.workType == 133) {
            _workCode = 112963;
        } else if (_item.item.workType == 151) {
            _workCode = 112551;
        } else if (_item.item.workType == 152) {
            _workCode = 112552;
        } else if (_item.item.workType == 153) {
            _workCode = 112553;
        } else if (_item.item.workType == 200) {
            _workCode = 112500;
        } else if (_item.item.workType == 201) {
            _workCode = 112501;
        } else if (_item.item.workType == 202) {
            _workCode = 112502;
        } else if (_item.item.workType == 203) {
            _workCode = 112503;
        } else {
            _workCode = 112930;
        }

        // 가격비교
        if (!_mid2.equals(".")) {
            _workCode -= 100;
        }

        if (!TextUtils.isEmpty(_item.item.searchMain)) {
            _workCode -= 200;
        }

        // _workCode = 114000 번대로 사용.
        if (_isLoginCookieExpired) {
            _workCode += 2000;
        }

        // 단일: 1129??/ 가비:1128??, 메인 단일: 1127??/ 메인 가비: 1126??

        _handler.sendEmptyMessageDelayed(END_PATTERN, MathHelper.randomRange(1000, 2000));

        return true;
    }

    public long getNextDelay() {
        switch (_delayType) {
            case 1:
                return MathHelper.randomRange(4000, 5000);
            case 0:
            default:
                return MathHelper.randomRange(2500, 4000);
        }
    }

    public long getSmallDelay() {
        switch (_delayType) {
            case 1:
                return MathHelper.randomRange(3000, 4000);
            case 0:
            default:
                return MathHelper.randomRange(1000, 2000);
        }
    }

    public long getStayDelay() {
        switch (_stayDelayType) {
            case 4:
                return MathHelper.randomRange(28000, 32000);
            case 3:
                return MathHelper.randomRange(18000, 22000);
            case 2:
                return MathHelper.randomRange(8000, 12000);
            case 1:
                return MathHelper.randomRange(4000, 7000);
            case 0:
            default:
                return MathHelper.randomRange(1000, 2000);
        }
    }

    public boolean isShopHome() {
        return _startHomeMode == KeywordItem.SHOP_HOME_MOBILE_SHOP || _startHomeMode == KeywordItem.SHOP_HOME_MOBILE_SHOP_DI;
    }

    private String getHomeMidsSelector() {
        return "._item li:not(._ad) a.product," +
                " .square_bx:not(._ad) a," +
                " .prod_info_basic .prod_explain";
    }

    private String getHomeMidSelector(String mid) {
        return "._item li:not(._ad) a[href*=mid=" + mid + "].product," +
                " .square_bx:not(._ad) a[href*=mid=" + mid + "]," +
                " .prod_info_basic .prod_explain[href*=mid=" + mid + "]";
    }

    private String getHomeNotMidSelector(String mid) {
        return "._item li:not(._ad) a:not([href*=mid=" + mid + "]).product," +
                " .square_bx:not(._ad) a:not([href*=mid=" + mid + "])," +
                " .prod_info_basic .prod_explain:not([href*=mid=" + mid + "])";
    }

    private String getShopHomeMidSelector(String mid) {
        return "a.product_btn_link__ArGCa[data-i=" + mid + "]";
    }

    private String getShopHomeNotMidSelector(String mid) {
        return "a.product_btn_link__ArGCa[data-i=" + mid + "]";
    }

    private JSONObject loadJson(String htmlString) {
        if (_baseJsonObject == null) {
            String baseJsonString = _httpEngine.getValueFromHtml(htmlString, "<script>window.__PRELOADED_STATE__", "=", "</script>", false);
//                    baseJsonString = StringEscapeUtils.unescapeJava(baseJsonString);
            Log.d(TAG, "base json: " + baseJsonString);

            if (baseJsonString.length() > 0) {
                try {
                    _baseJsonObject = new JSONObject(baseJsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return _baseJsonObject;
    }

    private String getPerformanceString(String sti) {
        if (TextUtils.isEmpty(_performanceString)) {
            StringBuilder performanceString = new StringBuilder();
            Date mDate = new Date();
            long time = mDate.getTime();

            if (_isWifi) {
                performanceString.append("&ct=").append("wifi");
            }

            performanceString.append("&ect=").append("4g");

            performanceString.append("&navigationStart=").append(time);

            if (!sti.equals("m_main_home") && !sti.equals("m_smartstore_products")) {
                long newTime = time + MathHelper.randomRange(200, 400);
                performanceString.append("&unloadEventStart=").append(newTime);
                performanceString.append("&unloadEventEnd=").append(newTime);
            }

            time += MathHelper.randomRange(7, 17);
            performanceString.append("&fetchStart=").append(time);
            performanceString.append("&domainLookupStart=").append(time);
            performanceString.append("&domainLookupEnd=").append(time);
            performanceString.append("&connectStart=").append(time);
            performanceString.append("&connectEnd=").append(time);
//            performanceString.append("&secureConnectionStart=").append(time);

            time += MathHelper.randomRange(10, 60);
            performanceString.append("&requestStart=").append(time);

            time += MathHelper.randomRange(10, 25);
            performanceString.append("&responseStart=").append(time);

            time += MathHelper.randomRange(300, 350);
            performanceString.append("&responseEnd=").append(time);

            time += MathHelper.randomRange(40, 60);
            performanceString.append("&domLoading=").append(time);

            time += MathHelper.randomRange(700, 1800);
            performanceString.append("&domInteractive=").append(time);
            performanceString.append("&domContentLoadedEventStart=").append(time);

            time += MathHelper.randomRange(3, 15);
            performanceString.append("&domContentLoadedEventEnd=").append(time);

            time += MathHelper.randomRange(1, 2);
            performanceString.append("&domComplete=").append(time);
            performanceString.append("&loadEventStart=").append(time);

            time += MathHelper.randomRange(7, 20);
            performanceString.append("&loadEventEnd=").append(time);

            String paint = paintValue(0);
            String[] paintStrings = paint.split("\\.");
            long minValue = 0;

            if (paintStrings.length > 0) {
                minValue = Long.parseLong(paintStrings[0]);
            }
            performanceString.append("&first-paint=").append(paint);
            performanceString.append("&first-contentful-paint=").append(paintValue(minValue));

            time += MathHelper.randomRange(20, 60);
            _performanceTime = time;
            _performanceString = performanceString.toString();
        } else {
            _performanceTime += MathHelper.randomRange(1, 4);
        }

        return _performanceString;
    }

    private String getPid(String htmlString) {
        if (TextUtils.isEmpty(_mainPid)) {
            StringBuilder pid = new StringBuilder();

            if (!TextUtils.isEmpty(_nnb)) {
                pid.append(_nnb);
            } else {
                pid.append("0.");
                // Math.random().toString(16).substring(0, 2).toUpperCase()
            }

            pid.append("-").append("SEARCH");
            String svt = _httpEngine.getValueFromHtml(htmlString, "window.svt", "=", ",");
            pid.append("-").append(svt);
            // (1e7 * Math.random()).toFixed()
            pid.append("-").append((int) Math.round(1e7 * Math.random()));

            _mainPid = pid.toString();
        }

        return _mainPid;
    }

    private String getUaParam(boolean hasModel, boolean hasBrands) throws UnsupportedEncodingException {
        Map<String, String> chUa = HttpHeader.getSecChUa(_ua);
        String secUa = null;
        StringBuilder paramString = new StringBuilder();

        for (Map.Entry<String, String> entry : chUa.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("sec-ch-ua")) {
                secUa = entry.getValue();
                break;
            }
        }

        if (!TextUtils.isEmpty(secUa)) {
            paramString.append("&ua_mobile=").append("true");

            String[] secUas = secUa.split(",");

            for (int i = 0; i < secUas.length; ++i) {
                String partUa = secUas[i].trim().replaceAll("\"", "");
                String[] keyVal = partUa.split(";v=");

                String val0 = URLEncoder.encode(keyVal[0], "UTF-8");
                val0 = val0.replaceAll("\\+", "%20");
                val0 = val0.replaceAll("%28", "(");
                val0 = val0.replaceAll("%29", ")");
                paramString.append("&ua_brand_").append(i).append("=").append(val0);

                if (keyVal.length > 1) {
                    paramString.append("&ua_version_").append(i).append("=").append(URLEncoder.encode(keyVal[1], "UTF-8"));
                }
            }

            if (hasBrands) {
                // &ua_brands=%5Bobject%20Object%5D%2C%5Bobject%20Object%5D%2C%5Bobject%20Object%5D
                String value = "[object Object],[object Object],[object Object]";
                value = URLEncoder.encode(value, "UTF-8");
                value = value.replaceAll("\\+", "%20");
                paramString.append("&ua_brands=").append(value);
            }

            if (hasModel && !TextUtils.isEmpty(_chromeVersion)) {
                String[] uaParts = _ua.split("\\)", 2);
                int begin = uaParts[0].indexOf("(");
                String deviceInfo = uaParts[0];

                if (begin > -1) {
                    deviceInfo = uaParts[0].substring(begin + 1);
                }

                //Linux; Android 6.0; Nexus 5 Build/MRA58N
                String[] infoParts = deviceInfo.split(";");
                String model = "";
                String platform = "";
                String platformVersion = "";

                if (infoParts.length > 2) {
                    model = infoParts[2].trim();

                    if (model.contains("Build")) {
                        model = model.substring(0, model.indexOf("Build"));
                    }

                    if (model.contains("/")) {
                        model = model.substring(0, model.indexOf("/"));
                    }

                    model = model.trim();
                }

                if (infoParts.length > 1) {
                    String platformInfo = infoParts[1].trim();
                    String[] parts = platformInfo.split(" ");
                    platform = parts[0].trim();

                    if (parts.length > 1) {
                        platformVersion = parts[1].trim();
                    }
                }

                paramString.append("&ua_model=").append(model);
                paramString.append("&ua_platform=").append(platform);
                paramString.append("&ua_platformVersion=").append(platformVersion);
                paramString.append("&ua_uaFullVersion=").append(_chromeVersion);
            }
        }

        return paramString.toString();
    }

    public boolean runSearchCcPost(String htmlString, boolean isBox) {
        String genUrl = getCcPostUrl();
        Log.d(TAG, "### searchCcPost genUrl: " + genUrl);

        if (!TextUtils.isEmpty(genUrl)) {
            String jsonBody = getSearchCcBody(htmlString, isBox);
            Log.d(TAG, "searchCcPost getSearchCcBody: " + jsonBody);

            _httpEngine.setOrigin("https://m.naver.com");
            _httpEngine.setReferer("https://m.naver.com/");
            String htmlString3 = _httpEngine.requestUrlPostWithOkHttpClientText(genUrl, jsonBody);
            Log.d(TAG, "searchCcPost result: " + htmlString3);
        }

        return true;
    }

    public String getSearchCcBody(String htmlString, boolean isBox) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        StringBuilder paramString = new StringBuilder();

        try {
            String aType = "home.search";

            if (isBox) {
                aType = "home.box";
            }

            paramString.append("a=").append(aType);
            paramString.append("&r=");
            paramString.append("&i=");
            paramString.append("&m=0");
            paramString.append("&ssc=").append("mtop.v5");
            paramString.append("&p=").append(getPid(htmlString));
            paramString.append("&lcsurl=").append(URLEncoder.encode(urlString, "UTF-8"));
            paramString.append("&lcssti=").append("m_main_home");

            String uaParam = getUaParam(true, true);

            if (!TextUtils.isEmpty(uaParam)) {
                paramString.append(uaParam);
            }

            paramString.append("&u=").append(URLEncoder.encode("about:blank", "UTF-8"));

            //a=home.search
            // &r=
            // &i=
            // &m=0
            // &ssc=mtop.v5
            // &p=NRCTMRI3EUFGI-SEARCH-20230313012947-9281080
            // &lcsurl=https%3A%2F%2Fm.naver.com%2F
            // &lcssti=m_main_home
            // &ua_mobile=true
            // &ua_brand_0=Google%20Chrome
            // &ua_version_0=111
            // &ua_brand_1=Not(A%3ABrand
            // &ua_version_1=8
            // &ua_brand_2=Chromium
            // &ua_version_2=111
            // &ua_brands=%5Bobject%20Object%5D%2C%5Bobject%20Object%5D%2C%5Bobject%20Object%5D
            // &ua_model=SM-G981B
            // &ua_platform=Android
            // &ua_platformVersion=10
            // &ua_uaFullVersion=111.0.5563.64
            // &u=about%3Ablank

            //a=home.search&r=&i=&m=0&ssc=mtop.v5&p=0.-SEARCH-20230307112453-7926767&lcsurl=https%3A%2F%2Fm.naver.com%2F&lcssti=m_main_home&ua_mobile=true&ua_brand_0=Chromium&ua_version_0=110&ua_brand_1=Not%20A(Brand&ua_version_1=24&ua_brand_2=Google%20Chrome&ua_version_2=110&ua_brands=%5Bobject%20Object%5D%2C%5Bobject%20Object%5D%2C%5Bobject%20Object%5D&ua_model=Nexus%205&ua_platform=Android&ua_platformVersion=6.0&ua_uaFullVersion=110.0.5481.177&u=about%3Ablank
            //a=home.search&r=&i=&m=0&ssc=mtop.v5&p=X74MASVCVUDGI-SEARCH-20230307122121-8218844&lcsurl=https%3A%2F%2Fm.naver.com%2F&lcssti=m_main_home&u=about%3Ablank
        } catch (Exception e) {
            e.printStackTrace();
        }

        return paramString.toString();
    }

    public boolean runCcPost(String htmlString, String sti, String py) {
        String genUrl = getCcPostUrl();
        Log.d(TAG, "### ccPost genUrl: " + genUrl);

        if (!TextUtils.isEmpty(genUrl)) {
            String jsonBody = getCcBody(htmlString, sti, py);
            Log.d(TAG, "ccPost getCcBody: " + jsonBody);

            _httpEngine.setOrigin("https://m.smartstore.naver.com");
            _httpEngine.setReferer("https://m.smartstore.naver.com/");
            String htmlString3 = _httpEngine.requestUrlPostWithOkHttpClientText(genUrl, jsonBody);
            Log.d(TAG, "ccPost result: " + htmlString3);
        }

        return true;
    }

    public boolean runProductDetailCcPost(String htmlString, String sti, String py) {
        String genUrl = getCcPostUrl();
        Log.d(TAG, "### productDetailCcPost genUrl: " + genUrl);

        if (!TextUtils.isEmpty(genUrl)) {
            String jsonBody = getProductDetailCcBody(htmlString, sti, py);
            Log.d(TAG, "getProductDetailCcBody: " + jsonBody);

            _httpEngine.setOrigin("https://m.smartstore.naver.com");
            _httpEngine.setReferer("https://m.smartstore.naver.com/");
            String htmlString3 = _httpEngine.requestUrlPostWithOkHttpClientText(genUrl, jsonBody);
            Log.d(TAG, "productDetailCcPost getProductDetailCcBody: " + htmlString3);
        }

        return true;
    }

    public String getCcPostUrl() {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        if (TextUtils.isEmpty(urlString) || !urlString.contains("naver.com")) {
            return null;
        }

        return "https://cc.naver.com/cc";
    }

    public String getCcBody(String htmlString, String sti, String py) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        StringBuilder paramString = new StringBuilder();

        try {
            String aType = "shi.home";
            if (sti.equals("m_smartstore_category")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_search")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_bundle")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_sub")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_home")) {
//                aType = "HOME";
            }

            aType = "GNB.storename";
            py = "0";

            paramString.append("a=").append(aType);
            paramString.append("&r=null");
            paramString.append("&i=null");
            paramString.append("&m=0");
            paramString.append("&ssc=").append("Msmartstore.end");

            if (TextUtils.isEmpty(_pid)) {
                // 여기서는 url무시한다.
                String currentUrl = _httpEngine.getCurrentUrl();
                String htmlString2 = _httpEngine.requestNaverLcsPidFromUrl(_nnb, URLEncoder.encode(urlString, "UTF-8"), _performanceTime);
                _httpEngine.setCurrentUrl(currentUrl);

                if (!TextUtils.isEmpty(htmlString2)) {
                    Log.d(TAG, "requestNaverLcsPidFromUrl: " + htmlString2);
                    _pid = htmlString2.trim();
                }
            }

            paramString.append("&p=").append(_pid);

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("sti", sti);
                String json = jsonObject.toString().replaceAll("\\\\" ,"");
                paramString.append("&g=").append(URLEncoder.encode(json, "UTF-8"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            paramString.append("&lcsurl=").append(URLEncoder.encode(urlString, "UTF-8"));
            paramString.append("&lcssti=").append(sti);
            paramString.append("&px=").append("0");
            paramString.append("&py=").append(py);

            String uaParam = getUaParam(false, false);

            if (!TextUtils.isEmpty(uaParam)) {
                paramString.append(uaParam);
            }

            paramString.append("&u=").append("about:blank");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return paramString.toString();
    }

    public String getProductDetailCcBody(String htmlString, String sti, String py) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        StringBuilder paramString = new StringBuilder();

        try {
            String aType = "shi.home";
            if (sti.equals("m_smartstore_category")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_search")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_bundle")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_sub")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_home")) {
//                aType = "HOME";
            }

            aType = "itm.dclose";

            paramString.append("a=").append(aType);
            paramString.append("&r=null");
            paramString.append("&i=null");
            paramString.append("&m=0");
            paramString.append("&ssc=").append("Msmartstore.end");

            if (TextUtils.isEmpty(_pid)) {
                // 여기서는 url무시한다.
                String currentUrl = _httpEngine.getCurrentUrl();
                String htmlString2 = _httpEngine.requestNaverLcsPidFromUrl(_nnb, URLEncoder.encode(urlString, "UTF-8"), _performanceTime);
                _httpEngine.setCurrentUrl(currentUrl);

                if (!TextUtils.isEmpty(htmlString2)) {
                    Log.d(TAG, "requestNaverLcsPidFromUrl: " + htmlString2);
                    _pid = htmlString2.trim();
                }
            }

            paramString.append("&p=").append(_pid);

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("sti", sti);
                String json = jsonObject.toString().replaceAll("\\\\" ,"");
                paramString.append("&g=").append(URLEncoder.encode(json, "UTF-8"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            paramString.append("&lcsurl=").append(URLEncoder.encode(urlString, "UTF-8"));
            paramString.append("&lcssti=").append(sti);

            String x = (int) MathHelper.randomRange(30, 340) + "";
            String y = (int) MathHelper.randomRange(300, 640) + "";

            paramString.append("&px=").append(x);
            paramString.append("&py=").append(py);
            paramString.append("&sx=").append(x);
            paramString.append("&sy=").append(y);

            String uaParam = getUaParam(false, false);

            if (!TextUtils.isEmpty(uaParam)) {
                paramString.append(uaParam);
            }

            paramString.append("&u=").append("about:blank");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return paramString.toString();
    }

    public boolean runSlcCcPost(String htmlString, String sti, String py) {
        String genUrl = getSlcCcPostUrl();
        Log.d(TAG, "### slcCcPos genUrl: " + genUrl);

        if (!TextUtils.isEmpty(genUrl)) {
            String jsonBody = getSlcCcBody(htmlString, sti, py);
            Log.d(TAG, "slcCcPos getSlcCcBody: " + jsonBody);

            _httpEngine.setOrigin("https://m.smartstore.naver.com");
            _httpEngine.setReferer("https://m.smartstore.naver.com/");
            String htmlString3 = _httpEngine.requestUrlPostWithOkHttpClientText(genUrl, jsonBody);
            Log.d(TAG, "slcCcPos result: " + htmlString3);
        }

        return true;
    }

    public boolean runProductDetailSlcCcPost(String htmlString, String sti, String py) {
        String genUrl = getSlcCcPostUrl();
        Log.d(TAG, "### productDetailSlcCcPos genUrl: " + genUrl);

        if (!TextUtils.isEmpty(genUrl)) {
            String jsonBody = getProductDetailSlcCcBody(htmlString, sti, py);
            Log.d(TAG, "getProductDetailSlcCcBody: " + jsonBody);

            _httpEngine.setOrigin("https://m.smartstore.naver.com");
            _httpEngine.setReferer("https://m.smartstore.naver.com/");
            String htmlString3 = _httpEngine.requestUrlPostWithOkHttpClientText(genUrl, jsonBody);
            Log.d(TAG, "productDetailSlcCcPos result: " + htmlString3);
        }

        return true;
    }

    public String getSlcCcPostUrl() {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        if (!urlString.contains("naver.com")) {
            return null;
        }

        return "https://slc.commerce.naver.com/cc";
    }

    public String getSlcCcBody(String htmlString, String sti, String py) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        StringBuilder paramString = new StringBuilder();

        try {
            String aType = "shi.home";
            if (sti.equals("m_smartstore_category")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_search")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_bundle")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_sub")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_home")) {
//                aType = "HOME";
            }

            aType = "GNB.storename";
            py = "0";

            paramString.append("a=").append(aType);
            paramString.append("&r=null");
            paramString.append("&i=null");
            paramString.append("&m=0");
            paramString.append("&ssc=").append("Msmartstore.end");

            if (TextUtils.isEmpty(_pid)) {
                // 여기서는 url무시한다.
                String currentUrl = _httpEngine.getCurrentUrl();
                String htmlString2 = _httpEngine.requestNaverLcsPidFromUrl(_nnb, URLEncoder.encode(urlString, "UTF-8"), _performanceTime);
                _httpEngine.setCurrentUrl(currentUrl);

                if (!TextUtils.isEmpty(htmlString2)) {
                    Log.d(TAG, "requestNaverLcsPidFromUrl: " + htmlString2);
                    _pid = htmlString2.trim();
                }
            }

            paramString.append("&p=").append(_pid);

            try {
                JSONObject jsonObject = new JSONObject();

                //채널번호
                String sid = _httpEngine.getValueFromHtml(htmlString, "\"channelNo\"", ":", ",");

                JSONObject pageJsonObject = new JSONObject();
                pageJsonObject.put("sti", sti);
                pageJsonObject.put("ctp", "chnl_prod");
                pageJsonObject.put("sid", sid);
                //카테고리
                pageJsonObject.put("ctg", _httpEngine.getValueFromHtml(htmlString, "\"categoryId\"", ":", ","));
                //상품코드
                pageJsonObject.put("cpn", _httpEngine.getValueFromHtml(htmlString, "\"productID\"", ":", ","));

                JSONObject clickJsonObject = new JSONObject();
                clickJsonObject.put("sid", sid);

                jsonObject.put("sti", sti);
                jsonObject.put("page", pageJsonObject);
                jsonObject.put("click", clickJsonObject);
                jsonObject.put("ts", (new Date()).getTime());

                String json = jsonObject.toString().replaceAll("\\\\" ,"");
                paramString.append("&g=").append(URLEncoder.encode(json, "UTF-8"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            paramString.append("&lcsurl=").append(URLEncoder.encode(urlString, "UTF-8"));
            paramString.append("&lcssti=").append(sti);
            paramString.append("&px=").append("0");
            paramString.append("&py=").append(py);

            String uaParam = getUaParam(false, false);

            if (!TextUtils.isEmpty(uaParam)) {
                paramString.append(uaParam);
            }

            paramString.append("&u=").append("about:blank");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return paramString.toString();
    }

    public String getProductDetailSlcCcBody(String htmlString, String sti, String py) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        StringBuilder paramString = new StringBuilder();

        try {
            String aType = "shi.home";
            if (sti.equals("m_smartstore_category")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_search")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_bundle")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_sub")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_home")) {
//                aType = "HOME";
            }

            aType = "itm.dclose";

            paramString.append("a=").append(aType);
            paramString.append("&r=null");
            paramString.append("&i=null");
            paramString.append("&m=0");
            paramString.append("&ssc=").append("Msmartstore.end");

            if (TextUtils.isEmpty(_pid)) {
                // 여기서는 url무시한다.
                String currentUrl = _httpEngine.getCurrentUrl();
                String htmlString2 = _httpEngine.requestNaverLcsPidFromUrl(_nnb, URLEncoder.encode(urlString, "UTF-8"), _performanceTime);
                _httpEngine.setCurrentUrl(currentUrl);

                if (!TextUtils.isEmpty(htmlString2)) {
                    Log.d(TAG, "requestNaverLcsPidFromUrl: " + htmlString2);
                    _pid = htmlString2.trim();
                }
            }

            paramString.append("&p=").append(_pid);

            try {
                JSONObject jsonObject = new JSONObject();

                //채널번호
                String sid = _httpEngine.getValueFromHtml(htmlString, "\"channelNo\"", ":", ",");

                JSONObject pageJsonObject = new JSONObject();
                pageJsonObject.put("sti", sti);
                pageJsonObject.put("ctp", "chnl_prod");
                pageJsonObject.put("sid", sid);
                //카테고리
                pageJsonObject.put("ctg", _httpEngine.getValueFromHtml(htmlString, "\"categoryId\"", ":", ","));
                //상품코드
                pageJsonObject.put("cpn", _httpEngine.getValueFromHtml(htmlString, "\"productID\"", ":", ","));

                jsonObject.put("sti", sti);
                jsonObject.put("page", pageJsonObject);
                jsonObject.put("ts", (new Date()).getTime());

                String json = jsonObject.toString().replaceAll("\\\\" ,"");
                paramString.append("&g=").append(URLEncoder.encode(json, "UTF-8"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            paramString.append("&lcsurl=").append(URLEncoder.encode(urlString, "UTF-8"));
            paramString.append("&lcssti=").append(sti);

            String x = (int) MathHelper.randomRange(30, 340) + "";
            String y = (int) MathHelper.randomRange(300, 640) + "";

            paramString.append("&px=").append(x);
            paramString.append("&py=").append(py);
            paramString.append("&sx=").append(x);
            paramString.append("&sy=").append(y);

            String uaParam = getUaParam(false, false);

            if (!TextUtils.isEmpty(uaParam)) {
                paramString.append(uaParam);
            }

            paramString.append("&u=").append("about:blank");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return paramString.toString();
    }



    public String getPRdUrl(String htmlString, Document doc, Element elTagA, String targetUrl) {
//        Elements elTqi = doc.select("input[name=tqi]");
        String eventString = elTagA.attr("onclick");

        //"return goOtherCR(this, 'a=rvw*c.link&r=4&i=90000004_016E7E7D0005C52700000000&u='+urlencode(this.href))"
        //"goOtherCR(this,'u='+urlencode(this.href)+'&r=5&i=00000009_00084edec4db&a=shp_lis.item');"
        //"return goOtherCR(this,'u='+urlencode(this.href)+'&r=1&i=00000009_00074c91401e&a=shp_5th*1.tit');"
//        String part = _httpEngine.getStringFromSource(eventString, "'", "'");
        Log.d(TAG, "##1 eventString: " + eventString);
        String part = eventString.replace("goOtherCR(this,", "");
        part = part.replace("urlencode(this.href)", "{encodedUrl}");
        part = part.replace("'+", "");
        part = part.replace("+'", "");
        part = part.replace(")", "");
        part = part.replace(";", "");
        part = part.replace("'", "");
        part = part.replace("return", "");
        part = part.trim();

        int order = 1;
        if (!TextUtils.isEmpty(part)) {
            String[] parts = part.split("&");
            for (String dataSet : parts) {
                if (dataSet.startsWith("a=")) {
                    String sectionName = dataSet.substring(2, dataSet.indexOf("."));
                    if (sectionName.indexOf("*") > -1) {
                        sectionName = sectionName.substring(0, sectionName.indexOf("*"));
                    }
                    Log.d(TAG, "sectionName: " + sectionName);
                    Elements elSections = doc.select("section.sc");

                    for (Element element : elSections) {
                        if (element.className().contains(sectionName)) {
                            break;
                        }

                        ++order;
                    }
                    break;
                }
            }
        }

        Map<String, String> parameters = new LinkedHashMap<>();
        Date mDate = new Date();
        String encodedUrl = targetUrl;

        try {
            String x = (int) MathHelper.randomRange(30, 340) + "";
            String y = (int) MathHelper.randomRange(140, order == 1 ? 380 : 640) + "";
            String py = (int) MathHelper.randomRange(1300, 2800) + "";

            if (order == 1) {
                py = (int) MathHelper.randomRange(250, 380) + "";
            }

            parameters.put("m", "1");
            // 랜덤좌표. x max 는 스크린 크기가 있으니 지정해서 잡고, y 가 문제다.
            parameters.put("px", x);
            parameters.put("py", py);
            parameters.put("sx", x);
            parameters.put("sy", y);
            parameters.put("p", URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_puid"), "UTF-8"));
//            parameters.put("p", URLEncoder.encode(elTqi.val(), "UTF-8"));
            parameters.put("q", URLEncoder.encode(_keyword, "UTF-8"));
//            parameters.put("q", URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_query"), "UTF-8"));
            parameters.put("ie", "utf8");
            parameters.put("rev", "1");
            parameters.put("ssc", URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_ssc"), "UTF-8"));
            //                                parameters.put("ssc", "tab.m.all"); // g_ssc
            parameters.put("f", URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_tab"), "UTF-8"));
            //                                parameters.put("f", "m");   // g_tab
            parameters.put("w", URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_stab"), "UTF-8"));
            //                                parameters.put("w", "m");   // g_stab
            parameters.put("s", URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_suid"), "UTF-8"));
            //                                parameters.put("s", "p3oVe1u4ZNVCtd%2FicONMyQ%3D%3D");  // g_suid
            parameters.put("time", "" + mDate.getTime());
            String abt = _httpEngine.getValueFromHtml(htmlString, "naver.search.abt_param");
            if (!TextUtils.isEmpty(abt)) {
                parameters.put("abt", abt);
            }
//            parameters.put("abt", "%5B%7B%22eid%22%3A%22SBR1%22%2C%22vid%22%3A%22692%22%7D%5D");    // (g_crt or naver.search.abt_param) + (naver.search.csdark === 1 ? "&stm=dark" : "")
            encodedUrl = URLEncoder.encode(targetUrl, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (!TextUtils.isEmpty(part)) {
            part = part.replace("{encodedUrl}", encodedUrl);
            parameters.put("part", "&" + part);
        }

//        parameters.put("r", "3");
//        parameters.put("i", "00000009_0013b3284a59");
//        parameters.put("a", "shp_lis.outitem");

        parameters.put("cr", "" + order);
//        parameters.put("cr", "3");

        StringBuilder genUrl = new StringBuilder("https://m.search.naver.com/p/crd/rd");
        int i = 0;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (entry.getKey().equals("abt") || entry.getKey().equals("part")) {
                genUrl.append(entry.getValue());
            } else {
                genUrl.append(i == 0 ? "?" : "&");
                genUrl.append(entry.getKey() + "=" + entry.getValue());
            }
            ++i;
        }

        /*
        https://m.search.naver.com/p/crd/rd?
        m=1
        &px=332
        &py=2756
        &sx=332
        &sy=691
        &p=h8gARlqVbV8ssKU45t4sssssseZ-373884
        &q=%EB%B2%A0%EB%B9%84%EC%83%B5+A9+%EC%B6%A9%EC%A0%84%EA%B8%B0+%EB%A6%AC%ED%8A%AC%EC%9D%B4%EC%98%A8+%EC%95%84%EB%8B%B5%ED%84%B0
        &ie=utf8
        &rev=1
        &ssc=tab.m.all
        &f=m
        &w=m
        &s=p3oVe1u4ZNVCtd%2FicONMyQ%3D%3D
        &time=1673847492623
        &abt=%5B%7B%22eid%22%3A%22SBR1%22%2C%22vid%22%3A%22692%22%7D%5D
        &u=https%3A%2F%2Fcr3.shopping.naver.com%2Fbridge%2FsearchGate%3Fcat_id%3D50001583%26nv_mid%3D84610140761%26query%3D%25EB%25B2%25A0%25EB%25B9%2584%25EC%2583%25B5%2BA9%2B%25EC%25B6%25A9%25EC%25A0%2584%25EA%25B8%25B0%2B%25EB%25A6%25AC%25ED%258A%25AC%25EC%259D%25B4%25EC%2598%25A8%2B%25EC%2595%2584%25EB%258B%25B5%25ED%2584%25B0%26bt%3D-1%26frm%3DMOSCPRO%26h%3Dc4bafcc70e049b01378b7913502e93395c8ba71f%26t%3DLCYDIFLN
        &r=3
        &i=00000009_0013b3284a59
        &a=shp_lis.outitem
        &cr=3

        /p/crd/rd?m=1&px=326&py=2214&sx=326&sy=192&p=h%2BLuLsprffossn6GlOossssssPs-524286&q=%EC%9E%90%EC%A0%84%EA%B1%B0+%ED%97%AC%EB%A9%A7&ie=utf8&rev=1&ssc=tab.m.all&f=m&w=m&s=fTF1bJbI1R73xVNJ31LTQw%3D%3D&time=1675838818061&abt=%5B%7B%22eid%22%3A%22ONEBEST-PRIOR%22%2C%22vid%22%3A%229%22%7D%2C%7B%22eid%22%3A%22SBR1%22%2C%22vid%22%3A%22756%22%7D%5D&u=https%3A%2F%2Fcr3.shopping.naver.com%2Fbridge%2FsearchGate%3Fcat_id%3D50002846%26nv_mid%3D9406011224%26query%3D%25EC%259E%2590%25EC%25A0%2584%25EA%25B1%25B0%2B%25ED%2597%25AC%25EB%25A9%25A7%26bt%3D-1%26frm%3DMOSCPRO%26h%3D97d6124242dd704a2250228f7e5822edd14c770e%26t%3DLDVAOFYB&r=9&i=00000009_000230a45758&a=shp_tli.outitem&cr=2

        m:1
        px:292
        py:2901
        sx:292
        sy:710
        p:h8RxSlp0iIdssh58ubossssssds-521905
        q:베비샵 A9 충전기 리튬이온 아답터
        ie:utf8
        rev:1
        ssc:tab.m.all
        f:m
        w:m
        s:0dTpr9NEWjd6fPOaiH9NPA==
        time:1673867642567
        abt:[{"eid":"SBR1","vid":"692"}]
        u:https://cr3.shopping.naver.com/bridge/searchGate?cat_id=50001583&nv_mid=84610140761&query=%EB%B2%A0%EB%B9%84%EC%83%B5+A9+%EC%B6%A9%EC%A0%84%EA%B8%B0+%EB%A6%AC%ED%8A%AC%EC%9D%B4%EC%98%A8+%EC%95%84%EB%8B%B5%ED%84%B0&bt=-1&frm=MOSCPRO&h=f0ffb3cf4b682e2ff0dc69afeb11e15b4a54771a&t=LCYPEIO6
        r:3
        i:00000009_0013b3284a59
        a:shp_lis.outitem
        cr:3
         */

        return genUrl.toString();
    }

//    public boolean runLcs(String htmlString) {
//        if (_useLcsGet) {
//            _httpEngine.setUrl(baseReferer);
//            String url = getLcsUrl(htmlString);
//            Log.d(TAG, "lcs Url: " + url);
//            _httpEngine.setReferer(baseReferer);
//            String htmlString3 = _httpEngine.requestNaverMobileContentFromLcs(url);
//
//            if (htmlString3 == null) {
//                Log.d(TAG, "# 통신 오류로 패턴종료.");
//                _workCode = 112019;
//                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
//                return false;
//            }
//        }
//
//        return true;
//    }

    public String getScrollLogUrl(String htmlString, String sti) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "getCurrentUrl: " + urlString);

        //https://s.search.naver.com/n/scrolllog/v2?u=https%3A%2F%2Fm.search.naver.com%2Fsearch.naver%3Fsm%3Dmtp_hty.top%26where%3Dm%26query%3D%25EC%259E%2590%25EC%25A0%2584%25EA%25B1%25B0%2B%25EA%25B1%25B0%25EC%25B9%2598%25EB%258C%2580&q=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80&p=islDqdprfAlssLKzXINssssstwR-156944&sscode=tab.m.all&slogs=%5B%7B%22t%22%3A%22first%22%2C%22pt%22%3A1678255529722%2C%22al%22%3A%22opt%3A118%3A0%3A0%3A0%7Cpwl%3A118%3A1095%3A0%3A779%7Cshp_tli%3A1213%3A1781%3A0%3A0%7Cshb_bas%3A2994%3A395%3A0%3A0%7Citb_bas%3A3390%3A3706%3A0%3A0%7Cpwb%3A7096%3A0%3A0%3A0%7Cimg%3A7096%3A575%3A0%3A0%7Cweb_gen%3A7671%3A753%3A0%3A0%7Ckdc_gnl%3A8424%3A429%3A0%3A0%7Cpag%3A8863%3A47%3A0%3A0%7Crsk_btm%3A8919%3A69%3A0%3A0%22%2C%22cl%22%3A%22opt%3A%3A118%3A0%3A0%3A0%3A0%3A400%3A0%3A400%22%2C%22si%22%3A%229232%3A897%3A400%22%7D%5D&EOU

        return "https://s.search.naver.com/n/scrolllog/v2?" + getScrollLogParams(htmlString, urlString, sti, false, false);
    }

    public String getScrollLogParams(String htmlString, String urlString, String sti, boolean hasBrands, boolean isSlc) {
        Log.d(TAG, "_clickRefererUrl: " + _clickRefererUrl);
        StringBuilder paramString = new StringBuilder();

        try {
            // u=https%3A%2F%2Fm.search.naver.com%2Fsearch.naver%3Fsm%3Dmtp_hty.top%26where%3Dm%26query%3D%25EC%259E%2590%25EC%25A0%2584%25EA%25B1%25B0%2B%25EA%25B1%25B0%25EC%25B9%2598%25EB%258C%2580
            // &q=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80
            // &p=islDqdprfAlssLKzXINssssstwR-156944
            // &sscode=tab.m.all
            // &slogs=%5B%7B%22t%22%3A%22first%22%2C%22pt%22%3A1678255529722%2C%22al%22%3A%22opt%3A118%3A0%3A0%3A0%7Cpwl%3A118%3A1095%3A0%3A779%7Cshp_tli%3A1213%3A1781%3A0%3A0%7Cshb_bas%3A2994%3A395%3A0%3A0%7Citb_bas%3A3390%3A3706%3A0%3A0%7Cpwb%3A7096%3A0%3A0%3A0%7Cimg%3A7096%3A575%3A0%3A0%7Cweb_gen%3A7671%3A753%3A0%3A0%7Ckdc_gnl%3A8424%3A429%3A0%3A0%7Cpag%3A8863%3A47%3A0%3A0%7Crsk_btm%3A8919%3A69%3A0%3A0%22%2C%22cl%22%3A%22opt%3A%3A118%3A0%3A0%3A0%3A0%3A400%3A0%3A400%22%2C%22si%22%3A%229232%3A897%3A400%22%7D%5D
            // &EOU
            paramString.append("u=").append(URLEncoder.encode(urlString, "UTF-8"));
            // e: referer
            paramString.append("&e=").append(URLEncoder.encode(_clickRefererUrl, "UTF-8"));

            String os = "Linux%20armv8l";

            if (_ua.contains("SamsungBrowser")) {
                os = "Linux%20aarch64";
            }

            //os: Linux aarch64
            //ln: ko-KR
            //sr: 412x869
            //pr: 2.625
            //bw: 412
            //bh: 722
            //c: 24
            paramString.append("&os=").append(os);
            paramString.append("&ln=").append(URLEncoder.encode("ko-KR", "UTF-8"));
            paramString.append("&sr=").append(URLEncoder.encode("360x640", "UTF-8"));
            paramString.append("&pr=").append("3");
            paramString.append("&bw=").append("360");
            paramString.append("&bh=").append(HttpHeader.getDeviceHeight(_ua));
            paramString.append("&c=").append("24");
            paramString.append("&j=N");
            paramString.append("&k=Y");
            paramString.append("&i=");
            paramString.append("&ls=").append(_nnb);

            Date mDate = new Date();
            long time = mDate.getTime();

            if (TextUtils.isEmpty(_performanceString)) {
                getPerformanceString(sti);
            } else {
                _performanceTime += MathHelper.randomRange(1, 4);
            }

            paramString.append(_performanceString);

            if (hasBrands) {
                String uaParam = getUaParam(false, false);

                if (!TextUtils.isEmpty(uaParam)) {
                    paramString.append(uaParam);
                }

                paramString.append("&sti=").append(sti);

                if (isSlc) {
                    if (sti.equals("m_smartstore_products")) {
//                    ctp=chnl_prod&sid=101513672&ctg=50000252&cpn=6572783406&serName=slc.commerce.naver.com
                        paramString.append("&ctp=").append("chnl_prod");
                        //채널번호
                        paramString.append("&sid=").append(_httpEngine.getValueFromHtml(htmlString, "\"channelNo\"", ":", ","));
                        //카테고리
                        paramString.append("&ctg=").append(_httpEngine.getValueFromHtml(htmlString, "\"categoryId\"", ":", ","));
                        //상품코드
                        paramString.append("&cpn=").append(_httpEngine.getValueFromHtml(htmlString, "\"productID\"", ":", ","));
                    } else {
                        //채널번호
                        paramString.append("&sid=").append(_httpEngine.getValueFromHtml(htmlString, "\"channelNo\"", ":", ","));
                    }

                    paramString.append("&serName=").append("slc.commerce.naver.com");
                } else {
                    paramString.append("&serName=").append("lcs.naver.com");
                }

                // 여기서는 url무시한다.
                String currentUrl = _httpEngine.getCurrentUrl();
                String htmlString2 = _httpEngine.requestNaverLcsPidFromUrl(_nnb, URLEncoder.encode(urlString, "UTF-8"), _performanceTime);
                _httpEngine.setCurrentUrl(currentUrl);

                if (!TextUtils.isEmpty(htmlString2)) {
                    Log.d(TAG, "requestNaverLcsPidFromUrl: " + htmlString2);
                    String pid = htmlString2.trim();

                    if (isSlc) {
                        _pid = pid;
                    }

                    paramString.append("&pid=").append(pid);
                }
            } else {
                paramString.append("&pid=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_puid"), "UTF-8"));
                paramString.append("&ssc=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_ssc"), "UTF-8"));
            }

            paramString.append("&ts=").append(_performanceTime);
            paramString.append("&EOU");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        navigationStart:              1676829907951               1676745656855           1676833331167
//        fetchStart:                   1676829907956               1676745656869           1676833331175
//        domainLookupStart:            1676829907956               1676745656869           1676833331175
//        domainLookupEnd:              1676829907956               1676745656869           1676833331175
//        connectStart:                 1676829907956               1676745656869           1676833331175
//        connectEnd:                   1676829907956               1676745656869           1676833331175
//        requestStart:                 1676829907959               1676745656921           1676833331185
//        responseStart:                1676829907970               1676745656932           1676833331206
//        responseEnd:                  1676829908277               1676745657270           1676833331516
//        domLoading:                   1676829907990               1676745656983           1676833331250
//        domInteractive:               1676829908584               1676745658684           1676833331982
//        domContentLoadedEventStart:   1676829908584               1676745658684           1676833331982
//        domContentLoadedEventEnd:     1676829908586               1676745658694           1676833331985
//        domComplete:                  1676829908587               1676745658696           1676833331986
//        loadEventStart:               1676829908587               1676745658696           1676833331986
//        loadEventEnd:                 1676829908589               1676745658712           1676833331995
//        first-paint:                  228.4000000357628           998.6000000014901       387.19999999925494
//        first-contentful-paint:       268.10000002384200          1193.1000000014901      473.59999999962747
//        ts:                           1676829908636               1676745658771           1676833332017

        return paramString.toString();
    }

    public String getErUrl(String htmlString) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "getCurrentUrl: " + urlString);

        // https://er.search.naver.com/er?v=2&navt=0:0:0:0:0:5:5:5:5:5:19:27:392:72:732:732:733:742:742:744:0:0&page_id=ismTTsprffosshIz0QZssssstuV-203336&ssc=tab.m.all&tags=conn_r_TLSv1.3_.:alpn.h2:_ssl:_and6_web
        // https://er.search.naver.com/er?
        // v=2
        // &navt=0:0:0:0:0:5:5:5:5:5:19:27:392:72:732:732:733:742:742:744:0:0
        // &page_id=ismTTsprffosshIz0QZssssstuV-203336
        // &ssc=tab.m.all
        // &tags=conn_r_TLSv1.3_.:alpn.h2:_ssl:_and6_web

        StringBuilder paramString = new StringBuilder();

        String g_puid = "";
        String g_ssc = "";
        String version = "";
        String tags = "";

        try {
            g_puid = URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_puid"), "UTF-8");
            g_ssc = URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_ssc"), "UTF-8");

            String[] parts = _ua.split(";");
            for (String part : parts) {
                part = part.trim();

                if (part.startsWith("Android")) {
                    String[] values = part.split(" ");

                    if (values.length > 1) {
                        String[] ver = values[1].split("\\.");
                        version = ver[0];
                        break;
                    }
                }
            }

            tags = "conn_r_TLSv1.3_.:alpn.h2:_ssl:_and" + version + "_web";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder navtString = new StringBuilder();
        String performanceString = getPerformanceString("");
        Map<String, String> kvs = new HashMap<>();
        String[] parts = performanceString.split("&");

        for (String part : parts) {
            String[] kv = part.split("=");
            String value = "";

            if (kv.length > 1) {
                value = kv[1];
            }

            kvs.put(kv[0].trim(), value);
        }

        long start = Long.parseLong(kvs.get("navigationStart"));

        navtString.append(!TextUtils.isEmpty(kvs.get("navigationStart")) ? Long.parseLong(kvs.get("navigationStart")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("unloadEventStart")) ? Long.parseLong(kvs.get("unloadEventStart")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("unloadEventEnd")) ? Long.parseLong(kvs.get("unloadEventEnd")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("redirectStart")) ? Long.parseLong(kvs.get("redirectStart")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("redirectEnd")) ? Long.parseLong(kvs.get("redirectEnd")) - start : 0);

        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("fetchStart")) ? Long.parseLong(kvs.get("fetchStart")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("domainLookupStart")) ? Long.parseLong(kvs.get("domainLookupStart")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("domainLookupEnd")) ? Long.parseLong(kvs.get("domainLookupEnd")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("connectStart")) ? Long.parseLong(kvs.get("connectStart")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("connectEnd")) ? Long.parseLong(kvs.get("connectEnd")) - start : 0);

        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("requestStart")) ? Long.parseLong(kvs.get("requestStart")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("responseStart")) ? Long.parseLong(kvs.get("responseStart")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("responseEnd")) ? Long.parseLong(kvs.get("responseEnd")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("domLoading")) ? Long.parseLong(kvs.get("domLoading")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("domInteractive")) ? Long.parseLong(kvs.get("domInteractive")) - start : 0);

        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("domContentLoadedEventStart")) ? Long.parseLong(kvs.get("domContentLoadedEventStart")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("domContentLoadedEventEnd")) ? Long.parseLong(kvs.get("domContentLoadedEventEnd")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("domComplete")) ? Long.parseLong(kvs.get("domComplete")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("loadEventStart")) ? Long.parseLong(kvs.get("loadEventStart")) - start : 0);
        navtString.append(":").append(!TextUtils.isEmpty(kvs.get("loadEventEnd")) ? Long.parseLong(kvs.get("loadEventEnd")) - start : 0);

        //(t.navigationStart), _(t.unloadEventStart), _(t.unloadEventEnd), _(t.redirectStart), _(t.redirectEnd),
        // _(t.fetchStart), _(t.domainLookupStart), _(t.domainLookupEnd), _(t.connectStart), _(t.connectEnd),
        // _(t.requestStart), _(t.responseStart), _(t.responseEnd), _(t.domLoading), _(t.domInteractive),
        // _(t.domContentLoadedEventStart), _(t.domContentLoadedEventEnd), _(t.domComplete), _(t.loadEventStart), _(t.loadEventEnd),
        // n.type, n.redirectCount]

        navtString.append(":0:0");

        paramString.append("v=2");
        paramString.append("&navt=").append(navtString);
        paramString.append("&page_id=").append(g_puid);
        paramString.append("&ssc=").append(g_ssc);
        paramString.append("&tags=").append(tags);

        return "https://er.search.naver.com/er?" + paramString;
    }

    public String changeKeyword(String url) {
        String parsed = WebViewManager.keywordEncodeForNaver(_keyword);
        return _webViewManager.changeQueryValue(url, "query", parsed);
    }

    public String getShoppingSearchUrl1(String htmlString) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "getCurrentUrl: " + urlString);

        // https://s.search.naver.com/p/sshopping/search.naver?bt=-1&filter=tagByQuery&query=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80&rev=99&source=shp_tli&view=GUIDE&where=recommend_api_mobile&_callback=jQuery22407140817681797409_1678256653315&_=1678256653316
        // https://s.search.naver.com/p/sshopping/search.naver?addon_plan=1&bt=-1&category1=50000007&query=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80&rev=42&source=shp_tli&view=GUIDE&where=addon_vertical_api_mobile&_callback=jQuery22407140817681797409_1678256653317&_=1678256653318

        // https://s.search.naver.com/p/sshopping/search.naver?
        // bt=-1
        // &filter=tagByQuery
        // &query=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80
        // &rev=99
        // &source=shp_tli
        // &view=GUIDE
        // &where=recommend_api_mobile
        // &_callback=jQuery22407140817681797409_1678256653315
        // &_=1678256653316

        String baseUrl = _httpEngine.getValueFromHtml(htmlString, "\"recommendTrendApiUrl\"", ":", ",");
//        String baseUrl = _httpEngine.getValueFromHtml(htmlString, "\"verticalApiUrl\"", ":", ",");
//        String baseUrl = _httpEngine.getValueFromHtml(htmlString, "\"foryouApiUrl\"", ":", ",");
        Log.d(TAG, "getShoppingSearchUrl1 baseUrl: " + baseUrl);

        if (TextUtils.isEmpty(baseUrl)) {
            return null;
        }

        Date mDate = new Date();
        long time = mDate.getTime();
        String url = baseUrl + "&_callback=" + _jqueryFuncName + "_" + time + "&_=" + time;

        return changeKeyword(url);
    }

    public String getShoppingSearchUrl2(String htmlString) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "getCurrentUrl: " + urlString);

        // https://s.search.naver.com/p/sshopping/search.naver?bt=-1&filter=tagByQuery&query=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80&rev=99&source=shp_tli&view=GUIDE&where=recommend_api_mobile&_callback=jQuery22407140817681797409_1678256653315&_=1678256653316
        // https://s.search.naver.com/p/sshopping/search.naver?addon_plan=1&bt=-1&category1=50000007&query=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80&rev=42&source=shp_tli&view=GUIDE&where=addon_vertical_api_mobile&_callback=jQuery22407140817681797409_1678256653317&_=1678256653318
        // https://s.search.naver.com/p/sshopping/search.naver?bt=-1&city=&country=KR&expand_ad=0&lgl_lat=37.6014512&lgl_long=127.0166146&rcode=09290133&region=&rev=99&ro=0&sm=mtp_hty.top&source=shp_tli&ssc=tab.m.all&view=GUIDE&where=relclk_api_mobile&relclk_id=&query=%EC%9E%90%EC%A0%84%EA%B1%B0%20%EA%B1%B0%EC%B9%98%EB%8C%80&puid=islqvdqVWT8ssuHBlRVssssssfK-202336&_callback=jQuery22407140817681797409_1678256653321&_=1678256653322

//        String baseUrl = _httpEngine.getValueFromHtml(htmlString, "\"recommendTrendApiUrl\"", ":", ",");
        String baseUrl = _httpEngine.getValueFromHtml(htmlString, "\"verticalApiUrl\"", ":", ",");
//        String baseUrl = _httpEngine.getValueFromHtml(htmlString, "\"foryouApiUrl\"", ":", ",");
        Log.d(TAG, "getShoppingSearchUrl2 baseUrl: " + baseUrl);

        if (TextUtils.isEmpty(baseUrl)) {
            return null;
        }

        Date mDate = new Date();
        long time = mDate.getTime();
        String url = baseUrl + "&_callback=" + _jqueryFuncName + "_" + time + "&_=" + time;

        return changeKeyword(url);
    }

    public String getShoppingSearchUrl3(String htmlString) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "getCurrentUrl: " + urlString);

        // https://s.search.naver.com/p/sshopping/search.naver?bt=-1&filter=tagByQuery&query=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80&rev=99&source=shp_tli&view=GUIDE&where=recommend_api_mobile&_callback=jQuery22407140817681797409_1678256653315&_=1678256653316
        // https://s.search.naver.com/p/sshopping/search.naver?addon_plan=1&bt=-1&category1=50000007&query=%EC%9E%90%EC%A0%84%EA%B1%B0+%EA%B1%B0%EC%B9%98%EB%8C%80&rev=42&source=shp_tli&view=GUIDE&where=addon_vertical_api_mobile&_callback=jQuery22407140817681797409_1678256653317&_=1678256653318
        // https://s.search.naver.com/p/sshopping/search.naver?abt=-1&city=&country=KR&expand_ad=0&lgl_lat=37.6014512&lgl_long=127.0166146&rcode=09290133&region=&rev=99&ro=0&sm=mtp_hty.top&source=shp_tli&ssc=tab.m.all&view=GUIDE&where=relclk_api_mobile&relclk_id=&query=%EC%9E%90%EC%A0%84%EA%B1%B0%20%EA%B1%B0%EC%B9%98%EB%8C%80&puid=islqvdqVWT8ssuHBlRVssssssfK-202336&_callback=jQuery22407140817681797409_1678256653321&_=1678256653322

//        String baseUrl = _httpEngine.getValueFromHtml(htmlString, "\"recommendTrendApiUrl\"", ":", ",");
//        String baseUrl = _httpEngine.getValueFromHtml(htmlString, "\"verticalApiUrl\"", ":", ",");
        String baseUrl = _httpEngine.getValueFromHtml(htmlString, "\"foryouApiUrl\"", ":", ",");
        Log.d(TAG, "getShoppingSearchUrl3 baseUrl: " + baseUrl);

        if (TextUtils.isEmpty(baseUrl)) {
            return null;
        }

        String g_puid = "";

        try {
            g_puid = URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_puid"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // gps 픽스
        baseUrl = _webViewManager.changeQueryValue(baseUrl, "lgl_lat", "37.6014512");
        baseUrl = _webViewManager.changeQueryValue(baseUrl, "lgl_long", "127.0166146");
        baseUrl = _webViewManager.changeQueryValue(baseUrl, "rcode", "09290133");

        baseUrl = baseUrl.replace("{=nvmid}", "");
        baseUrl = baseUrl.replace("{=query}", "");
        baseUrl = baseUrl.replace("+g_puid", g_puid);

        Date mDate = new Date();
        long time = mDate.getTime();
        String url = baseUrl + "&_callback=" + _jqueryFuncName + "_" + time + "&_=" + (time + 1);

        return changeKeyword(url);
    }

    public String getLcsUrl(String htmlString, String sti) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "getCurrentUrl: " + urlString);

        return "https://lcs.naver.com/m?" + getParams(htmlString, urlString, sti, false, false);
    }

    public String getParams(String htmlString, String urlString, String sti, boolean hasBrands, boolean isSlc) {
        Log.d(TAG, "_clickRefererUrl: " + _clickRefererUrl);
        StringBuilder paramString = new StringBuilder();

        String referer = _clickRefererUrl;

        if (TextUtils.isEmpty(referer)) {
            referer = "";
        }

        try {
            paramString.append("u=").append(URLEncoder.encode(urlString, "UTF-8"));
            // e: referer
            paramString.append("&e=").append(URLEncoder.encode(referer, "UTF-8"));

            String os = "Linux%20armv8l";

            if (_ua.contains("SamsungBrowser")) {
                os = "Linux%20aarch64";
            }

            // TODO: 개선필요. 장비 부분, ua 따라 설정해줘야한다.
            //os: Linux aarch64
            //ln: ko-KR
            //sr: 412x869
            //pr: 2.625
            //bw: 412
            //bh: 722
            //c: 24
            paramString.append("&os=").append(os);
            paramString.append("&ln=").append(URLEncoder.encode("ko-KR", "UTF-8"));
            paramString.append("&sr=").append(URLEncoder.encode("360x640", "UTF-8"));
            paramString.append("&pr=").append("3");
            paramString.append("&bw=").append("360");
            paramString.append("&bh=").append(HttpHeader.getDeviceHeight(_ua));
            paramString.append("&c=").append("24");

            paramString.append("&j=N");
            paramString.append("&k=Y");
            paramString.append("&i=");

            if (!TextUtils.isEmpty(_nnb)) {
                paramString.append("&ls=").append(_nnb);
            }

            Date mDate = new Date();
            long time = mDate.getTime();

            if (TextUtils.isEmpty(_performanceString)) {
                getPerformanceString(sti);
            } else {
                _performanceTime += MathHelper.randomRange(1, 4);
            }

            paramString.append(_performanceString);

            if (hasBrands) {
                String uaParam = getUaParam(sti.equals("m_main_home"), false);

                if (!TextUtils.isEmpty(uaParam)) {
                    paramString.append(uaParam);
                }

                paramString.append("&sti=").append(sti);

                if (sti.equals("m_main_home")) {
                    paramString.append("&pid=").append(getPid(htmlString));
                    paramString.append("&ugr=").append("newmain");
                    paramString.append("&pmd=").append("home");
                } else {
                    if (isSlc) {
                        if (sti.equals("m_smartstore_products")) {
//                    ctp=chnl_prod&sid=101513672&ctg=50000252&cpn=6572783406&serName=slc.commerce.naver.com
                            paramString.append("&ctp=").append("chnl_prod");
                            //채널번호
                            paramString.append("&sid=").append(_httpEngine.getValueFromHtml(htmlString, "?channelNo", "=", "\""));
                            //카테고리
                            paramString.append("&ctg=").append(_httpEngine.getValueFromHtml(htmlString, "\"categoryId\"", ":", ","));
                            //상품코드
                            paramString.append("&cpn=").append(_httpEngine.getValueFromHtml(htmlString, "\"productID\"", ":", ","));
                        } else {
                            //채널번호
                            paramString.append("&sid=").append(_httpEngine.getValueFromHtml(htmlString, "\"channelNo\"", ":", ","));
                        }

                        paramString.append("&serName=").append("slc.commerce.naver.com");
                    } else {
                        paramString.append("&serName=").append("lcs.naver.com");
                    }

                    // 여기서는 url무시한다.
                    String currentUrl = _httpEngine.getCurrentUrl();
                    String htmlString2 = _httpEngine.requestNaverLcsPidFromUrl(_nnb, URLEncoder.encode(urlString, "UTF-8"), _performanceTime);
                    _httpEngine.setCurrentUrl(currentUrl);

                    if (!TextUtils.isEmpty(htmlString2)) {
                        Log.d(TAG, "requestNaverLcsPidFromUrl: " + htmlString2);
                        String pid = htmlString2.trim();

                        if (isSlc) {
                            _pid = pid;
                        }

                        paramString.append("&pid=").append(pid);
                    }
                }
            } else {
                paramString.append("&pid=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_puid"), "UTF-8"));
                paramString.append("&ssc=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_ssc"), "UTF-8"));
            }

            paramString.append("&ts=").append(_performanceTime);
            paramString.append("&EOU");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        navigationStart:              1676829907951               1676745656855           1676833331167
//        fetchStart:                   1676829907956               1676745656869           1676833331175
//        domainLookupStart:            1676829907956               1676745656869           1676833331175
//        domainLookupEnd:              1676829907956               1676745656869           1676833331175
//        connectStart:                 1676829907956               1676745656869           1676833331175
//        connectEnd:                   1676829907956               1676745656869           1676833331175
//        requestStart:                 1676829907959               1676745656921           1676833331185
//        responseStart:                1676829907970               1676745656932           1676833331206
//        responseEnd:                  1676829908277               1676745657270           1676833331516
//        domLoading:                   1676829907990               1676745656983           1676833331250
//        domInteractive:               1676829908584               1676745658684           1676833331982
//        domContentLoadedEventStart:   1676829908584               1676745658684           1676833331982
//        domContentLoadedEventEnd:     1676829908586               1676745658694           1676833331985
//        domComplete:                  1676829908587               1676745658696           1676833331986
//        loadEventStart:               1676829908587               1676745658696           1676833331986
//        loadEventEnd:                 1676829908589               1676745658712           1676833331995
//        first-paint:                  228.4000000357628           998.6000000014901       387.19999999925494
//        first-contentful-paint:       268.10000002384200          1193.1000000014901      473.59999999962747
//        ts:                           1676829908636               1676745658771           1676833332017

        return paramString.toString();
    }

    public String paintValue(long min) {
        Log.d(TAG, "min: " + min);
        long max = 1000;

        if (min == 0) {
            min = 300;
        } else {
            max = min + 200;
            min += 100;
        }

        StringBuilder createdUrl = new StringBuilder();
        createdUrl.append((int) MathHelper.randomRange(min, max));
        long calc = MathHelper.randomRange(0, 9999999);

        if (calc > 0) {
            createdUrl.append(".");
            int firstValue = (int) MathHelper.randomRange(1, 8);
            long baseValue;
            long changeValue;

            if ((int) MathHelper.randomRange(0, 1) == 0) {
                //          10000002384186
                baseValue = 10000000000000L * firstValue;
            } else {
                //          4000000357628
                baseValue = 1000000000000L * firstValue;
            }

            if ((int) MathHelper.randomRange(0, 1) == 0) {
                changeValue = baseValue - calc;
            } else {
                changeValue = baseValue + calc;
            }

            createdUrl.append(changeValue);
        }

        return createdUrl.toString();
    }

    public String getRd0Url(String htmlString) {
        StringBuilder genUrl = new StringBuilder("https://m.search.naver.com/p/crd/rd?");
        Date mDate = new Date();
        try {
            genUrl.append("m=0");
            genUrl.append("&px=0");
            genUrl.append("&py=0");
            genUrl.append("&sx=-1");
            genUrl.append("&sy=-1");
            genUrl.append("&p=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_puid"), "UTF-8"));
            genUrl.append("&q=").append(URLEncoder.encode(_keyword, "UTF-8"));
            genUrl.append("&ie=utf8");
            genUrl.append("&rev=1");
            genUrl.append("&ssc=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_ssc"), "UTF-8"));
//            parameters.put("ssc", "tab.m.all"); // g_ssc
            genUrl.append("&f=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_tab"), "UTF-8"));
//            parameters.put("f", "m");   // g_tab
            genUrl.append("&w=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_stab"), "UTF-8"));
//            parameters.put("w", "m");   // g_stab
            genUrl.append("&s=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "var g_suid"), "UTF-8"));
//            parameters.put("s", "p3oVe1u4ZNVCtd%2FicONMyQ%3D%3D");  // g_suid
            genUrl.append("&time=").append(mDate.getTime());
            String abt = _httpEngine.getValueFromHtml(htmlString, "naver.search.abt_param");
            if (!TextUtils.isEmpty(abt)) {
                genUrl.append(abt);
            }
//            parameters.put("abt", "%5B%7B%22eid%22%3A%22SBR1%22%2C%22vid%22%3A%22692%22%7D%5D");    // (g_crt or naver.search.abt_param) + (naver.search.csdark === 1 ? "&stm=dark" : "")
        } catch (Exception e) {
            e.printStackTrace();
        }

        genUrl.append("&u=").append("javascript");
        genUrl.append("&r=");
        genUrl.append("&i=");
        genUrl.append("&a=shf_tli.rkey");

        return genUrl.toString();
    }

    public boolean runVoltPv() {
        if (_useVoltPv) {
            String urlString = _httpEngine.getCurrentUrl();
            Log.d(TAG, "current url: " + urlString);
            String genUrl = getRunVoltPvUrl(urlString);
            Log.d(TAG, "### runVoltPv url: " + genUrl);

            _httpEngine.setOrigin(null);
            String htmlString3 = _httpEngine.requestUrlWithOkHttpClientImage(genUrl);
            Log.d(TAG, "runVoltPv result: " + htmlString3);

            if (htmlString3 == null) {
                Log.d(TAG, "# 통신 오류로 패턴종료.");
                _workCode = 112032;
                _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                return false;
            }
        }

        return true;
    }

    public String getRunVoltPvUrl(String urlString) {
        Date mDate = new Date();

        try {
            urlString = URLEncoder.encode(urlString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //https://volts.shopping.naver.com/pv?referer=&nsc=&ts=1686246637258&location=https%3A%2F%2Fmsearch.shopping.naver.com%2Fproduct%2F84946754439
        return "https://volts.shopping.naver.com/pv?referer=&nsc=&ts=" + mDate.getTime() + "&location=" + urlString;
    }

    public boolean runProductLog(String htmlString) {
        if (_useProductLog) {
            String genUrl = getProductLogUrl(htmlString);
            Log.d(TAG, "### productLog genUrl: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                String jsonBody = getProductLogJsonString(htmlString);
                Log.d(TAG, "productLog jsonBody: " + jsonBody);
                _httpEngine.setOrigin("https://m.smartstore.naver.com");
                _httpEngine.setReferer(_httpEngine.getCurrentUrl());
                String htmlString3 = _httpEngine.requestNaverMobileContentFromProductLog(genUrl, jsonBody);
                _httpEngine.setOrigin(null);
                Log.d(TAG, "productLog result: " + htmlString3);

                if (TextUtils.isEmpty(htmlString3)) {
                    Log.d(TAG, "# 통신 오류로 패턴종료.");
                    _workCode = 112091;
                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
                    return false;
                }
            }
        }

        return true;
    }

    public String getProductLogUrl(String htmlString) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        if (!urlString.contains("naver.com") || urlString.contains("naver.com/play/") || urlString.contains("naver.com/beauty/") || urlString.contains("naver.com/window-products/") || urlString.contains("naver.com/fresh/")) {
            return null;
        }

        String productId = _httpEngine.getValueFromHtml(htmlString, "productID", ":", ",");

        if (TextUtils.isEmpty(productId)) {
            return null;
        }

        return "https://m.smartstore.naver.com/i/v1/product-logs/" + productId;
    }

    public boolean fetchStoreProducts(String htmlString) {
        if (_useFetchStoreProducts) {
            String urlString = _httpEngine.getCurrentUrl();
            Log.d(TAG, "current url: " + urlString);
            String genUrl = getFetchStoreProductsUrl(urlString, htmlString);
            Log.d(TAG, "### fetchStoreProducts url: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                _httpEngine.setOrigin(null);
                _httpEngine.setReferer(urlString);
                String htmlString3 = _httpEngine.requestUrlWithOkHttpClientFetch(genUrl);
                Log.d(TAG, "fetchStoreProducts result: " + htmlString3);
            }
        }

        return true;
    }

    public String getFetchStoreProductsUrl(String urlString, String htmlString) {
        if (!urlString.contains("naver.com") || urlString.contains("naver.com/play/") || urlString.contains("naver.com/beauty/") || urlString.contains("naver.com/window-products/") || urlString.contains("naver.com/fresh/")) {
            return null;
        }

        String channelNo = _httpEngine.getValueFromHtml(htmlString, "\"channelNo\"", ":", ",");

        if (TextUtils.isEmpty(channelNo)) {
            return null;
        }

        //https://m.smartstore.naver.com/i/v1/stores/100508024/products?page=1&count=6
        return "https://m.smartstore.naver.com/i/v1/stores/" + channelNo + "/products?page=1&count=6";
    }

    public boolean fetchBenefits(String htmlString) {
        if (_useFetchStoreProducts) {
            String urlString = _httpEngine.getCurrentUrl();
            Log.d(TAG, "current url: " + urlString);
            String genUrl = getFetchBenefitsUrl(urlString, htmlString);
            Log.d(TAG, "### fetchBenefits url: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                _httpEngine.setOrigin(null);
                _httpEngine.setReferer(urlString);
                String htmlString3 = _httpEngine.requestUrlWithOkHttpClientFetch(genUrl);
                Log.d(TAG, "fetchBenefits result: " + htmlString3);
            }
        }

        return true;
    }

    public String getFetchBenefitsUrl(String urlString, String htmlString) {
        if (!urlString.contains("naver.com") || urlString.contains("naver.com/play/") || urlString.contains("naver.com/beauty/") || urlString.contains("naver.com/window-products/") || urlString.contains("naver.com/fresh/")) {
            return null;
        }

        JSONObject baseJsonObject = loadJson(htmlString);
        String categoryId = null;

        if (baseJsonObject == null) {
            return null;
        }

        try {
            JSONObject productObject = baseJsonObject.getJSONObject("product");
            JSONObject aObject = productObject.getJSONObject("A");
            JSONObject categoryVariables = aObject.getJSONObject("category");

            categoryId = categoryVariables.getString("categoryId");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String accountNo = _httpEngine.getValueFromHtml(htmlString, "\"accountNo\"", ":", ",");
        String productNo = _httpEngine.getValueFromHtml(htmlString, "\"productNo\"", ":", ",");

        if (TextUtils.isEmpty(accountNo) || TextUtils.isEmpty(categoryId) || TextUtils.isEmpty(productNo)) {
            return null;
        }

        //https://m.smartstore.naver.com/i/v1/benefits/100461897?categoryId=50002830&productNo=5906903589
        return "https://m.smartstore.naver.com/i/v1/benefits/" + accountNo + "?categoryId=" + categoryId + "&productNo=" + productNo;
    }

    public boolean fetchPrediction(String htmlString) {
        if (_useFetchStoreProducts) {
            String urlString = _httpEngine.getCurrentUrl();
            Log.d(TAG, "current url: " + urlString);
            String genUrl = getFetchPredictionUrl(urlString, htmlString);
            Log.d(TAG, "### fetchPrediction url: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                _httpEngine.setOrigin(null);
                _httpEngine.setReferer(urlString);
                String htmlString3 = _httpEngine.requestUrlWithOkHttpClientFetch(genUrl);
                Log.d(TAG, "fetchPrediction result: " + htmlString3);
            }
        }

        return true;
    }

    public String getFetchPredictionUrl(String urlString, String htmlString) {
        if (!urlString.contains("naver.com") || urlString.contains("naver.com/play/") || urlString.contains("naver.com/beauty/") || urlString.contains("naver.com/window-products/") || urlString.contains("naver.com/fresh/")) {
            return null;
        }

        JSONObject baseJsonObject = loadJson(htmlString);
        String targetAddress = null;
        boolean todayDelivery = false;

        if (baseJsonObject == null) {
            return null;
        }

        try {
            JSONObject deliveryObject = baseJsonObject.getJSONObject("delivery").getJSONObject("A");
            JSONObject deliveryAddressObject = deliveryObject.getJSONObject("deliveryAddress");
            targetAddress = deliveryAddressObject.getString("baseAddress");

            JSONObject productObject = baseJsonObject.getJSONObject("product").getJSONObject("A");
            JSONObject productDeliveryInfoObject = productObject.getJSONObject("productDeliveryInfo");
            todayDelivery = productDeliveryInfoObject.getBoolean("todayDelivery");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String accountId = _httpEngine.getValueFromHtml(htmlString, "\"accountId\"", ":", ",");
        String productId = _httpEngine.getValueFromHtml(htmlString, "\"상품번호\"", ":", ",");
        String cat1 = _httpEngine.getValueFromHtml(htmlString, "category1Id", ":", ",");
        String cat2 = _httpEngine.getValueFromHtml(htmlString, "category2Id", ":", ",");
        String cat3 = _httpEngine.getValueFromHtml(htmlString, "category3Id", ":", ",");
        String sourceAddress = _httpEngine.getValueFromHtml(htmlString, "\"shippingAddress\"", ":", ",");

        if (TextUtils.isEmpty(accountId) || TextUtils.isEmpty(productId) ||
                TextUtils.isEmpty(cat1) || TextUtils.isEmpty(cat2) || TextUtils.isEmpty(cat3)) {
            return null;
        }

        if (TextUtils.isEmpty(sourceAddress)) {
            sourceAddress = "서울특별시";
        }

        if (TextUtils.isEmpty(targetAddress)) {
            targetAddress = "서울특별시";
        }

        try {
            sourceAddress = URLEncoder.encode(sourceAddress, "UTF-8");
            targetAddress = URLEncoder.encode(targetAddress, "UTF-8");

            sourceAddress = sourceAddress.replaceAll("%28", "(");
            targetAddress = targetAddress.replaceAll("%28", "(");

            sourceAddress = sourceAddress.replaceAll("%29", ")");
            targetAddress = targetAddress.replaceAll("%29", ")");

            sourceAddress = sourceAddress.replaceAll("%3A", ":");
            targetAddress = targetAddress.replaceAll("%3A", ":");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //https://m.smartstore.naver.com/i/v1/deliveries/prediction?merchantId=ncp_1nt8w9_01&productNo=5934448641&largeCategoryId=50000007&middleCategoryId=50000161&smallCategoryId=50001109&sourceAddress=%EC%84%9C%EC%9A%B8%ED%8A%B9%EB%B3%84%EC%8B%9C+%EC%9D%80%ED%8F%89%EA%B5%AC+%EC%A7%84%ED%9D%A5%EB%A1%9C+27+(%ED%95%98%EB%8A%98%EC%95%A0%EC%95%84%ED%8C%8C%ED%8A%B8)+1002+(%EC%9A%B0+:+03409)+&targetAddress=%EC%84%9C%EC%9A%B8%ED%8A%B9%EB%B3%84%EC%8B%9C
        String url = "https://m.smartstore.naver.com/i/v1/deliveries/prediction?merchantId=" + accountId +
                "&productNo=" + productId +
                "&largeCategoryId=" + cat1 +
                "&middleCategoryId=" + cat2 +
                "&smallCategoryId=" + cat3 +
                "&sourceAddress=" + sourceAddress +
                "&targetAddress=" + targetAddress;

        if (todayDelivery) {
            Date date = new Date();
            Date tomorrow = new Date(date.getTime() + (86400 * 1000));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String dateString = dateFormat.format(tomorrow);

            url += "&todayDispatchDay=" + dateString;
        }

        return url;
    }

    public boolean fetchStoreContents(String htmlString) {
        if (_useFetchStoreProducts) {
            String urlString = _httpEngine.getCurrentUrl();
            Log.d(TAG, "current url: " + urlString);
            String genUrl = getFetchStoreContentsUrl(urlString, htmlString);
            Log.d(TAG, "### fetchStoreContents url: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                _httpEngine.setOrigin(null);
                _httpEngine.setReferer(urlString);
                String htmlString3 = _httpEngine.requestUrlWithOkHttpClientFetch(genUrl);
                Log.d(TAG, "fetchStoreContents result: " + htmlString3);
            }
        }

        return true;
    }

    public String getFetchStoreContentsUrl(String urlString, String htmlString) {
        if (!urlString.contains("naver.com") || urlString.contains("naver.com/play/") || urlString.contains("naver.com/beauty/") || urlString.contains("naver.com/window-products/") || urlString.contains("naver.com/fresh/")) {
            return null;
        }

        String productId = _httpEngine.getValueFromHtml(htmlString, "\"상품번호\"", ":", ",");
        String productNo = _httpEngine.getValueFromHtml(htmlString, "\"productNo\"", ":", ",");

        if (TextUtils.isEmpty(productId) || TextUtils.isEmpty(productNo)) {
            return null;
        }

        //https://m.smartstore.naver.com/i/v1/products/5934448641/contents/5906903589/MOBILE
        return "https://m.smartstore.naver.com/i/v1/products/" + productId + "/contents/" + productNo + "/MOBILE";
    }


    public boolean runLcsPost(String htmlString, String sti) {
        if (_useLcsPost) {
            String genUrl = getLcsPostUrl();
            Log.d(TAG, "### lcsPost genUrl: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                Log.d(TAG, "lcsPost u,pidUrl: " + _httpEngine.getCurrentUrl());
                String jsonBody = getParams(htmlString, _httpEngine.getCurrentUrl(), sti, true, false);
                Log.d(TAG, "lcsPost getParams: " + jsonBody);

                _httpEngine.setOrigin("https://m.smartstore.naver.com");
                _httpEngine.setReferer("https://m.smartstore.naver.com/");
                String htmlString3 = _httpEngine.requestUrlPostWithOkHttpClientText(genUrl, jsonBody);
                Log.d(TAG, "lcsPost result: " + htmlString3);
            }
        }

        return true;
    }

    public String getLcsPostUrl() {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        if (!urlString.contains("naver.com")) {
            return null;
        }

        return "https://lcs.naver.com/m";
    }

    public boolean runSlcPost(String htmlString, String sti) {
        if (_useSlcPost) {
            String genUrl = getSlcPostUrl();
            Log.d(TAG, "### slcPost genUrl: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                Log.d(TAG, "slcPost u,pidUrl: " + _httpEngine.getCurrentUrl());
                String jsonBody = getParams(htmlString, _httpEngine.getCurrentUrl(), sti, true, true);
                Log.d(TAG, "slcPost getParams: " + jsonBody);

                _httpEngine.setOrigin("https://m.smartstore.naver.com");
                _httpEngine.setReferer("https://m.smartstore.naver.com/");
                String htmlString3 = _httpEngine.requestUrlPostWithOkHttpClientText(genUrl, jsonBody);
                Log.d(TAG, "slcPost result: " + htmlString3);
            }
        }

        return true;
    }

    public String getSlcPostUrl() {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        if (!urlString.contains("naver.com") || urlString.contains("naver.com/shortclips/")) {
            return null;
        }

        return "https://slc.commerce.naver.com/m";
    }

    public boolean runInflowBenefits(String htmlString, String sti) {
        if (_useWcsLog) {
//            if (!sti.equals("m_smartstore_products")) {
//                return true;
//            }

            String genUrl = getWcsLogUrl(htmlString);
            Log.d(TAG, "### inflowBenefits url: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                _httpEngine.setOrigin("https://m.smartstore.naver.com");
                _httpEngine.setReferer("https://m.smartstore.naver.com/");
//                String htmlString2 = _httpEngine.requestUrlPostWithOkHttpClientText(genUrl);
//                Log.d(TAG, "wcs result: " + htmlString2);
                _httpEngine.setOrigin(null);

                // 230302 저녁전
//                _httpEngine.setOrigin(null);
//                _httpEngine.setReferer("https://m.smartstore.naver.com/");
//                String htmlString2 = _httpEngine.requestNaverMobileContentFromWcs(genUrl);
//                Log.d(TAG, "wcs result: " + htmlString2);
//
//                if (TextUtils.isEmpty(htmlString2)) {
//                    Log.d(TAG, "# 통신 오류로 패턴종료.");
//                    _workCode = 112017;
//                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
//                    return false;
//                }
            }
        }

        return true;
    }

    public String getInflowBenefitsUrl(String htmlString) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        if (!urlString.contains("naver.com")) {
            return null;
        }

        /*
        https://wcs.naver.com/m?
        u=https%3A%2F%2Fm.smartstore.naver.com%2Fjmfortune%2Fproducts%2F4709745269%3FNaPm%3Dct%253Dle9xmem0%257Cci%253Dd0cce017a73d6eb82f8fa540ac06f5b2c9506f3e%257Ctr%253Dsls%257Csn%253D1008680%257Chk%253D9c9c101c3b888ebee518efd527e90215ef04336f
        &e=https%3A%2F%2Fm.search.naver.com%2Fsearch.naver%3Fsm%3Dmtp_hty.top%26where%3Dm%26query%3D%25EC%259E%2590%25EC%25A0%2584%25EA%25B1%25B0%25EB%25A7%25A4%25ED%258A%25B8
        &wa=s_5d91830388a6
        &bt=-1
        &vtyp=DET
        &pid=4709745269
        &pnm=%ED%97%AC%EC%8A%A4%20%EC%9A%B4%EB%8F%99%EA%B8%B0%EA%B5%AC%20%ED%99%88%EC%A7%90%20%EB%9F%B0%EB%8B%9D%EB%A8%B8%EC%8B%A0%20%EC%8B%A4%EB%82%B4%EC%9E%90%EC%A0%84%EA%B1%B0%20%EB%A7%A4%ED%8A%B8%20%EB%B0%A9%EC%9D%8C%20%EC%B8%B5%EA%B0%84%20%EC%86%8C%EC%9D%8C%EB%B0%A9%EC%A7%80%20%EC%B6%A9%EA%B2%A9%ED%9D%A1%EC%88%98%20%EB%A7%A4%ED%8A%B8%20%EA%B3%A0%EB%AC%B415T
        &lcatid=50000007
        &lcatnm=%EC%8A%A4%ED%8F%AC%EC%B8%A0%2F%EB%A0%88%EC%A0%80
        &mcatid=50000030
        &mcatnm=%ED%97%AC%EC%8A%A4
        &scatid=50001004
        &scatnm=%ED%97%AC%EC%8A%A4%EC%86%8C%ED%92%88
        &dcatid=50003170
        &dcatnm=%EA%B8%B0%ED%83%80%ED%97%AC%EC%8A%A4%EC%86%8C%ED%92%88
        &mid=510455935
        &chno=100491013
        &mtyp=STF
        &os=MacIntel
        &ln=ko-KR
        &sr=400x929
        &bw=400
        &bh=929
        &c=30
        &j=N
        &jv=1.8
        &k=Y
        &ct=
        &cs=UTF-8
        &tl=%25ED%2597%25AC%25EC%258A%25A4%2520%25EC%259A%25B4%25EB%258F%2599%25EA%25B8%25B0%25EA%25B5%25AC%2520%25ED%2599%2588%25EC%25A7%2590%2520%25EB%259F%25B0%25EB%258B%259D%25EB%25A8%25B8%25EC%258B%25A0%2520%25EC%258B%25A4%25EB%2582%25B4%25EC%259E%2590%25EC%25A0%2584%25EA%25B1%25B0%2520%25EB%25A7%25A4%25ED%258A%25B8%2520%25EB%25B0%25A9%25EC%259D%258C%2520%25EC%25B8%25B5%25EA%25B0%2584%2520%25EC%2586%258C%25EC%259D%258C%25EB%25B0%25A9%25EC%25A7%2580%2520%25EC%25B6%25A9%25EA%25B2%25A9%25ED%259D%25A1%25EC%2588%2598%2520%25EB%25A7%25A4%25ED%258A%25B8%2520%25EA%25B3%25A0%25EB%25AC%25B415T
        &vs=0.8.6&nt=1676723069361&EOU
         */

        StringBuilder genUrl = new StringBuilder("https://m.smartstore.naver.com/i/v1/inflow-benefits?");

        Document doc = Jsoup.parse(htmlString);
        Elements elTagTitle = doc.select("head > title");
        String title = "";

        if (!elTagTitle.isEmpty()) {
            title = elTagTitle.text();
        }

//        String title = _httpEngine.getValueFromHtml(htmlString, "_22kNQuEXmb _copyable", ">", "<");
//        Log.d(TAG, "Title: " + title);

        try {
            String encodedTitle = URLEncoder.encode(title, "UTF-8");
            encodedTitle = encodedTitle.replace("+", "%20");
            String wa = _httpEngine.getValueFromHtml(htmlString, "naSiteId", ":", ",");

            genUrl.append("u=").append(URLEncoder.encode(urlString, "UTF-8"));
            genUrl.append("&e=").append(URLEncoder.encode(_clickRefererUrl, "UTF-8"));
            genUrl.append("&wa=").append(wa);
            genUrl.append("&bt=-1");
            genUrl.append("&vtyp=DET");
            genUrl.append("&pid=").append(_httpEngine.getValueFromHtml(htmlString, "productID", ":", ","));
            genUrl.append("&pnm=").append(encodedTitle);

            String cat1 = _httpEngine.getValueFromHtml(htmlString, "category1Id", ":", ",");
            String cat2 = _httpEngine.getValueFromHtml(htmlString, "category2Id", ":", ",");
            String cat3 = _httpEngine.getValueFromHtml(htmlString, "category3Id", ":", ",");
            String cat4 = _httpEngine.getValueFromHtml(htmlString, "category4Id", ":", ",");

            if (!TextUtils.isEmpty(cat1)) {
                genUrl.append("&lcatid=").append(cat1);
                genUrl.append("&lcatnm=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "category1Name", ":", ","), "UTF-8"));
            }

            if (!TextUtils.isEmpty(cat2)) {
                genUrl.append("&mcatid=").append(cat2);
                genUrl.append("&mcatnm=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "category2Name", ":", ","), "UTF-8"));
            }

            if (!TextUtils.isEmpty(cat3)) {
                genUrl.append("&scatid=").append(cat3);
                genUrl.append("&scatnm=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "category3Name", ":", ","), "UTF-8"));
            }

            if (!TextUtils.isEmpty(cat4)) {
                genUrl.append("&dcatid=").append(cat4);
                genUrl.append("&dcatnm=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "category4Name", ":", ","), "UTF-8"));
            }

            genUrl.append("&mid=").append(_httpEngine.getValueFromHtml(htmlString, "naverPaySellerNo", ":", ","));
            genUrl.append("&chno=").append(_httpEngine.getValueFromHtml(htmlString, "\"channelNo\"", ":", ","));
            genUrl.append("&mtyp=STF");
//            genUrl.append("&os=").append(URLEncoder.encode("Linux armv8l", "UTF-8"));
            genUrl.append("&os=").append("Linux%20armv8l");
            genUrl.append("&ln=").append(URLEncoder.encode("ko-KR", "UTF-8"));
            genUrl.append("&sr=").append(URLEncoder.encode("360x640", "UTF-8"));
            genUrl.append("&bw=").append("360");
            genUrl.append("&bh=").append(HttpHeader.getDeviceHeight(_ua));
            genUrl.append("&c=").append("24");
            genUrl.append("&j=N");
            genUrl.append("&jv=1.8");
            genUrl.append("&k=Y");
            genUrl.append("&ct=");
            genUrl.append("&cs=UTF-8");
            genUrl.append("&tl=").append(URLEncoder.encode(encodedTitle, "UTF-8"));
            genUrl.append("&vs=0.8.6");
            Date mDate = new Date();
            genUrl.append("&nt=").append(mDate.getTime());
            genUrl.append("&EOU");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return genUrl.toString();
    }

    public boolean runAmbulancePost(String htmlString, String sti) {
        if (_useAmbulancePost) {
            String genUrl = getAmbulancePostUrl();
            Log.d(TAG, "### ambulancePost genUrl: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                String jsonBody = getAmbulanceBody(htmlString, sti);
                Log.d(TAG, "ambulancePost getAmbulanceBody: " + jsonBody);

                _httpEngine.setOrigin("https://m.smartstore.naver.com");
                _httpEngine.setReferer(_httpEngine.getCurrentUrl());
                String htmlString3 = _httpEngine.requestNaverMobileContentFromProductLog(genUrl, jsonBody);
                Log.d(TAG, "ambulancePost result: " + htmlString3);
            }
        }

        return true;
    }

    public String getAmbulancePostUrl() {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        if (!urlString.contains("naver.com") || urlString.contains("naver.com/shortclips/")) {
            return null;
        }

        return "https://m.smartstore.naver.com/i/v1/ambulance/pages";
    }

    public String getAmbulanceBody(String htmlString, String sti) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        String body = null;

        try {
            JSONObject jsonObject = new JSONObject();

            String type = "PRODUCT";
            boolean isInitialRender = true;
            // isInitialRender 스크립트로 변경되는 페이지는 false, 실제 이동은 true.

            if (sti.equals("m_smartstore_category")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_search")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_bundle")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_sub")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_home")) {
                type = "HOME";
            }

            URL url = new URL(urlString);

            jsonObject.put("path", url.getPath());
            jsonObject.put("pathType", type);
            jsonObject.put("isInitialRender", isInitialRender);

//            "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            long gmtTime = new Date().getTime() - (9 * 60 * 60 * 1000);
            String date = simpleDateFormat.format(gmtTime);
            jsonObject.put("clientTime", date);

            body = jsonObject.toString().replaceAll("\\\\" ,"");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return body;
    }

    public boolean runWcsLog(String htmlString, String sti) {
        if (_useWcsLog) {
//            if (!sti.equals("m_smartstore_products")) {
//                return true;
//            }

            String genUrl = getWcsLogUrl(htmlString);
            Log.d(TAG, "### wcs url: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                String jsonBody = getWcsLogBody(htmlString, sti);
                Log.d(TAG, "wcs getWcsLogBody: " + jsonBody);

                _httpEngine.setOrigin("https://m.smartstore.naver.com");
                _httpEngine.setReferer("https://m.smartstore.naver.com/");
                String htmlString2 = _httpEngine.requestUrlPostWithOkHttpClientText(genUrl, jsonBody);
                Log.d(TAG, "wcs result: " + htmlString2);
                _httpEngine.setOrigin(null);

                // 230302 저녁전
//                _httpEngine.setOrigin(null);
//                _httpEngine.setReferer("https://m.smartstore.naver.com/");
//                String htmlString2 = _httpEngine.requestNaverMobileContentFromWcs(genUrl);
//                Log.d(TAG, "wcs result: " + htmlString2);
//
//                if (TextUtils.isEmpty(htmlString2)) {
//                    Log.d(TAG, "# 통신 오류로 패턴종료.");
//                    _workCode = 112017;
//                    _handler.sendEmptyMessageDelayed(END_PATTERN, 500);
//                    return false;
//                }
            }
        }

        return true;
    }

    public String getWcsLogUrl(String htmlString) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        if (!urlString.contains("naver.com")) {
            return null;
        }

        /*
        https://wcs.naver.com/m?
        u=https%3A%2F%2Fm.smartstore.naver.com%2Fjmfortune%2Fproducts%2F4709745269%3FNaPm%3Dct%253Dle9xmem0%257Cci%253Dd0cce017a73d6eb82f8fa540ac06f5b2c9506f3e%257Ctr%253Dsls%257Csn%253D1008680%257Chk%253D9c9c101c3b888ebee518efd527e90215ef04336f
        &e=https%3A%2F%2Fm.search.naver.com%2Fsearch.naver%3Fsm%3Dmtp_hty.top%26where%3Dm%26query%3D%25EC%259E%2590%25EC%25A0%2584%25EA%25B1%25B0%25EB%25A7%25A4%25ED%258A%25B8
        &wa=s_5d91830388a6
        &bt=-1
        &vtyp=DET
        &pid=4709745269
        &pnm=%ED%97%AC%EC%8A%A4%20%EC%9A%B4%EB%8F%99%EA%B8%B0%EA%B5%AC%20%ED%99%88%EC%A7%90%20%EB%9F%B0%EB%8B%9D%EB%A8%B8%EC%8B%A0%20%EC%8B%A4%EB%82%B4%EC%9E%90%EC%A0%84%EA%B1%B0%20%EB%A7%A4%ED%8A%B8%20%EB%B0%A9%EC%9D%8C%20%EC%B8%B5%EA%B0%84%20%EC%86%8C%EC%9D%8C%EB%B0%A9%EC%A7%80%20%EC%B6%A9%EA%B2%A9%ED%9D%A1%EC%88%98%20%EB%A7%A4%ED%8A%B8%20%EA%B3%A0%EB%AC%B415T
        &lcatid=50000007
        &lcatnm=%EC%8A%A4%ED%8F%AC%EC%B8%A0%2F%EB%A0%88%EC%A0%80
        &mcatid=50000030
        &mcatnm=%ED%97%AC%EC%8A%A4
        &scatid=50001004
        &scatnm=%ED%97%AC%EC%8A%A4%EC%86%8C%ED%92%88
        &dcatid=50003170
        &dcatnm=%EA%B8%B0%ED%83%80%ED%97%AC%EC%8A%A4%EC%86%8C%ED%92%88
        &mid=510455935
        &chno=100491013
        &mtyp=STF
        &os=MacIntel
        &ln=ko-KR
        &sr=400x929
        &bw=400
        &bh=929
        &c=30
        &j=N
        &jv=1.8
        &k=Y
        &ct=
        &cs=UTF-8
        &tl=%25ED%2597%25AC%25EC%258A%25A4%2520%25EC%259A%25B4%25EB%258F%2599%25EA%25B8%25B0%25EA%25B5%25AC%2520%25ED%2599%2588%25EC%25A7%2590%2520%25EB%259F%25B0%25EB%258B%259D%25EB%25A8%25B8%25EC%258B%25A0%2520%25EC%258B%25A4%25EB%2582%25B4%25EC%259E%2590%25EC%25A0%2584%25EA%25B1%25B0%2520%25EB%25A7%25A4%25ED%258A%25B8%2520%25EB%25B0%25A9%25EC%259D%258C%2520%25EC%25B8%25B5%25EA%25B0%2584%2520%25EC%2586%258C%25EC%259D%258C%25EB%25B0%25A9%25EC%25A7%2580%2520%25EC%25B6%25A9%25EA%25B2%25A9%25ED%259D%25A1%25EC%2588%2598%2520%25EB%25A7%25A4%25ED%258A%25B8%2520%25EA%25B3%25A0%25EB%25AC%25B415T
        &vs=0.8.6&nt=1676723069361&EOU
         */

        StringBuilder genUrl = new StringBuilder("https://wcs.naver.com/b");

        // 230302 저녁전
//        StringBuilder genUrl = new StringBuilder("https://wcs.naver.com/m?");
//
//        Document doc = Jsoup.parse(htmlString);
//        Elements elTagTitle = doc.select("head > title");
//        String title = "";
//
//        if (!elTagTitle.isEmpty()) {
//            title = elTagTitle.text();
//        }
//
////        String title = _httpEngine.getValueFromHtml(htmlString, "_22kNQuEXmb _copyable", ">", "<");
////        Log.d(TAG, "Title: " + title);
//
//        try {
//            String encodedTitle = URLEncoder.encode(title, "UTF-8");
//            encodedTitle = encodedTitle.replace("+", "%20");
//            String wa = _httpEngine.getValueFromHtml(htmlString, "naSiteId", ":", ",");
//
//            genUrl.append("u=").append(URLEncoder.encode(urlString, "UTF-8"));
//            genUrl.append("&e=").append(URLEncoder.encode(_clickRefererUrl, "UTF-8"));
//            genUrl.append("&wa=").append(wa);
//            genUrl.append("&bt=-1");
//            genUrl.append("&vtyp=DET");
//            genUrl.append("&pid=").append(_httpEngine.getValueFromHtml(htmlString, "productID", ":", ","));
//            genUrl.append("&pnm=").append(encodedTitle);
//
//            String cat1 = _httpEngine.getValueFromHtml(htmlString, "category1Id", ":", ",");
//            String cat2 = _httpEngine.getValueFromHtml(htmlString, "category2Id", ":", ",");
//            String cat3 = _httpEngine.getValueFromHtml(htmlString, "category3Id", ":", ",");
//            String cat4 = _httpEngine.getValueFromHtml(htmlString, "category4Id", ":", ",");
//
//            if (!TextUtils.isEmpty(cat1)) {
//                genUrl.append("&lcatid=").append(cat1);
//                genUrl.append("&lcatnm=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "category1Name", ":", ","), "UTF-8"));
//            }
//
//            if (!TextUtils.isEmpty(cat2)) {
//                genUrl.append("&mcatid=").append(cat2);
//                genUrl.append("&mcatnm=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "category2Name", ":", ","), "UTF-8"));
//            }
//
//            if (!TextUtils.isEmpty(cat3)) {
//                genUrl.append("&scatid=").append(cat3);
//                genUrl.append("&scatnm=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "category3Name", ":", ","), "UTF-8"));
//            }
//
//            if (!TextUtils.isEmpty(cat4)) {
//                genUrl.append("&dcatid=").append(cat4);
//                genUrl.append("&dcatnm=").append(URLEncoder.encode(_httpEngine.getValueFromHtml(htmlString, "category4Name", ":", ","), "UTF-8"));
//            }
//
//            genUrl.append("&mid=").append(_httpEngine.getValueFromHtml(htmlString, "naverPaySellerNo", ":", ","));
//            genUrl.append("&chno=").append(_httpEngine.getValueFromHtml(htmlString, "\"channelNo\"", ":", ","));
//            genUrl.append("&mtyp=STF");
////            genUrl.append("&os=").append(URLEncoder.encode("Linux armv8l", "UTF-8"));
//            genUrl.append("&os=").append("Linux%20armv8l");
//            genUrl.append("&ln=").append(URLEncoder.encode("ko-KR", "UTF-8"));
//            genUrl.append("&sr=").append(URLEncoder.encode("360x640", "UTF-8"));
//            genUrl.append("&bw=").append("360");
//            genUrl.append("&bh=").append("560");
//            genUrl.append("&c=").append("24");
//            genUrl.append("&j=N");
//            genUrl.append("&jv=1.8");
//            genUrl.append("&k=Y");
//            genUrl.append("&ct=");
//            genUrl.append("&cs=UTF-8");
//            genUrl.append("&tl=").append(URLEncoder.encode(encodedTitle, "UTF-8"));
//            genUrl.append("&vs=0.8.6");
//            Date mDate = new Date();
//            genUrl.append("&nt=").append(mDate.getTime());
//            genUrl.append("&EOU");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        return genUrl.toString();
    }

    public String getWcsLogBody(String htmlString, String sti) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        Document doc = Jsoup.parse(htmlString);
        Elements elTagTitle = doc.select("head > title");
        String title = "";

        if (!elTagTitle.isEmpty()) {
            title = elTagTitle.text();
        }

//        String title = _httpEngine.getValueFromHtml(htmlString, "_22kNQuEXmb _copyable", ">", "<");
//        Log.d(TAG, "Title: " + title);

        String body = null;

        try {
            String encodedTitle = URLEncoder.encode(title, "UTF-8");
            encodedTitle = encodedTitle.replace("+", "%20");
            String wa = _httpEngine.getValueFromHtml(htmlString, "naSiteId", ":", ",");

//            String sti = "m_smartstore_sub";
//            "m_smartstore_products"
//            if (currentUrl.contains("/category/")) {
//                sti = "m_smartstore_category";
//            } else if (currentUrl.contains("/search")) {
//                sti = "m_smartstore_search";
//            } else if (currentUrl.contains("/bundle/")) {
//                sti = "m_smartstore_bundle";
//            } else if (currentUrl.endsWith(_homePath)) {
//                sti = "m_smartstore_home";
//            }

            String vtyp = "DET";
            String bt = null;

            if (sti.equals("m_smartstore_products")) {

            } else {
                if (sti.equals("m_smartstore_category")) {
                    vtyp = "LST";
                } else if (sti.equals("m_smartstore_search")) {
                    vtyp = "LST";
                } else if (sti.equals("m_smartstore_bundle")) {
                    vtyp = "LST";
                } else if (sti.equals("m_smartstore_home")) {
                    vtyp = "HOME";
                }

                String cookies = _cookieController.getCookie(getWcsLogUrl(htmlString));
                Log.d(TAG, "wcs.naver.com cookies: " + cookies);

                String[] cookieArray = cookies.split(";");

                for (String cookie : cookieArray) {
//                    Log.d(TAG, "cookie: " + cookie);
                    String[] kv = cookie.trim().split("=");
//                    Log.d(TAG, "key: " + kv[0]);

                    if (kv[0].equals("NWB") && kv.length > 1) {
                        Log.d(TAG, "value: " + kv[1]);
                        String[] parse1 = kv[1].split("\\.");
                        Log.d(TAG, "parse1: " + parse1[0]);

                        if (parse1.length > 1) {
                            bt = (Long.parseLong(parse1[1]) / 1000) + "";
                            Log.d(TAG, "bt: " + bt);
                        }
                        break;
                    }
                }
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("wa", wa);
            jsonObject.put("u", urlString);
            jsonObject.put("e", _clickRefererUrl);

            if (bt == null) {
                jsonObject.put("bt", -1);
            } else {
                jsonObject.put("bt", bt);
            }

            jsonObject.put("vtyp", vtyp);

            String mid = _httpEngine.getValueFromHtml(htmlString, "naverPaySellerNo", ":", ",");

            if (vtyp.equals("DET")) {
                String pid = _httpEngine.getValueFromHtml(htmlString, "productID", ":", ",");

                if (TextUtils.isEmpty(pid)) {
                    pid = _httpEngine.getValueFromHtml(htmlString, "\"상품번호\"", ":", ",");
                }

                if (!TextUtils.isEmpty(pid)) {
                    jsonObject.put("pid", pid);
                    jsonObject.put("pnm", title);
                }

                String cat1 = _httpEngine.getValueFromHtml(htmlString, "category1Id", ":", ",");
                String cat2 = _httpEngine.getValueFromHtml(htmlString, "category2Id", ":", ",");
                String cat3 = _httpEngine.getValueFromHtml(htmlString, "category3Id", ":", ",");
                String cat4 = _httpEngine.getValueFromHtml(htmlString, "category4Id", ":", ",");

                if (!TextUtils.isEmpty(cat1)) {
                    jsonObject.put("lcatid", cat1);
                    jsonObject.put("lcatnm", _httpEngine.getValueFromHtml(htmlString, "category1Name", ":", ","));
                }

                if (!TextUtils.isEmpty(cat2)) {
                    jsonObject.put("mcatid", cat2);
                    jsonObject.put("mcatnm", _httpEngine.getValueFromHtml(htmlString, "category2Name", ":", ","));
                }

                if (!TextUtils.isEmpty(cat3)) {
                    jsonObject.put("scatid", cat3);
                    jsonObject.put("scatnm", _httpEngine.getValueFromHtml(htmlString, "category3Name", ":", ","));
                }

                if (!TextUtils.isEmpty(cat4)) {
                    jsonObject.put("dcatid", cat4);
                    jsonObject.put("dcatnm", _httpEngine.getValueFromHtml(htmlString, "category4Name", ":", ","));
                }
            } else if (vtyp.equals("LST")) {
//                encodedTitle = _httpEngine.getValueFromHtml(htmlString, "\"channelName\"", ":", ",");
//
//                if (encodedTitle.endsWith("}")) {
//                    encodedTitle.substring(0, encodedTitle.length() - 2);
//                }
//
//                encodedTitle = URLEncoder.encode(encodedTitle, "UTF-8");

                if (sti.equals("m_smartstore_search")) {
                    jsonObject.put("catid", "search");
                    jsonObject.put("catnm", "검색 상품");
                } else {
                    try {
                        JSONObject baseJsonObject = loadJson(htmlString);

                        if (baseJsonObject != null) {
                            JSONObject rootCategoryVariables = baseJsonObject.getJSONObject("category");
                            JSONObject aVariables = rootCategoryVariables.getJSONObject("A");
                            JSONObject categoryVariables = aVariables.getJSONObject("category");

                            jsonObject.put("catid", categoryVariables.getString("id"));
                            jsonObject.put("catnm", categoryVariables.getString("name"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                mid = _httpEngine.getValueFromHtml(htmlString, "payReferenceKey", ":", ",");
            }

            jsonObject.put("mid", mid);
            jsonObject.put("chno", _httpEngine.getValueFromHtml(htmlString, "?channelNo", "=", "\""));
            jsonObject.put("mtyp", "STF");

            String os = "Linux armv8l";

            if (_ua.contains("SamsungBrowser")) {
                os = "Linux aarch64";
            }

            jsonObject.put("os", os);
            jsonObject.put("ln", "ko-KR");
            jsonObject.put("sr", "360x640");
            jsonObject.put("bw", 360);
            jsonObject.put("bh", HttpHeader.getDeviceHeight(_ua));
            jsonObject.put("c", 24);
            jsonObject.put("j", "N");
            jsonObject.put("jv", "1.8");
            jsonObject.put("k", "Y");
            jsonObject.put("ct", "");
            jsonObject.put("cs", "UTF-8");
            jsonObject.put("tl", encodedTitle);

            if (!TextUtils.isEmpty(_wcsRefererUrl)) {
                jsonObject.put("ur", _wcsRefererUrl);
            }

            jsonObject.put("vs", "0.8.7");
            Date mDate = new Date();
            jsonObject.put("nt", mDate.getTime());

            body = jsonObject.toString().replaceAll("\\\\" ,"");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return body;
    }

    public boolean fetchCategoryProducts(String htmlString) {
        if (false) {
            String urlString = _httpEngine.getCurrentUrl();
            Log.d(TAG, "current url: " + urlString);
            String genUrl = getFetchCategoryProductsUrl(urlString, htmlString);
            Log.d(TAG, "### fetchCategoryProducts url: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                _httpEngine.setOrigin(null);
                _httpEngine.setReferer(urlString);
                String htmlString3 = _httpEngine.requestUrlWithOkHttpClientFetch(genUrl);
                Log.d(TAG, "fetchCategoryProducts result: " + htmlString3);
                _categoryProductsHtmlString = htmlString3;
            }
        }

        return true;
    }

    public String getFetchCategoryProductsUrl(String urlString, String htmlString) {
        if (!urlString.contains("naver.com") || urlString.contains("naver.com/play/") || urlString.contains("naver.com/beauty/") || urlString.contains("naver.com/window-products/") || urlString.contains("naver.com/fresh/")) {
            return null;
        }

        JSONObject baseJsonObject = loadJson(htmlString);
        String categoryId = null;

        if (baseJsonObject == null) {
            return null;
        }

        try {
            JSONObject productObject = baseJsonObject.getJSONObject("product");
            JSONObject aObject = productObject.getJSONObject("A");
            JSONObject categoryVariables = aObject.getJSONObject("category");

            categoryId = categoryVariables.getString("categoryId");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String channelNo = _httpEngine.getValueFromHtml(htmlString, ",\"channelNo\"", ":", ",");
        String productID = _httpEngine.getValueFromHtml(htmlString, "\"productID\"", ":", ",");

        if (TextUtils.isEmpty(channelNo) || TextUtils.isEmpty(productID) || TextUtils.isEmpty(categoryId)) {
            return null;
        }

//        return "https://m.smartstore.naver.com/i/v1/stores/101381098/products/7402254117/category-products/50014220?page=1&size=10";
        return "https://m.smartstore.naver.com/i/v1/stores/" + channelNo + "/products/" + productID + "/category-products/" + categoryId + "?page=1&size=10";
    }


    public boolean runExposesPost(String htmlString) {
        if (_runExposePost) {
            String genUrl = getExposesUrl(htmlString);
            Log.d(TAG, "### exposesPost url: " + genUrl);

            if (!TextUtils.isEmpty(genUrl) && !TextUtils.isEmpty(_categoryProductsHtmlString)) {
                String jsonBody = getExposesBody(htmlString);
                Log.d(TAG, "getExposesBody: " + jsonBody);

                _httpEngine.setOrigin("https://m.smartstore.naver.com");
                _httpEngine.setReferer(_httpEngine.getCurrentUrl());
                String htmlString3 = _httpEngine.requestUrlPostWithOkHttpClientFetch(genUrl, jsonBody);
                Log.d(TAG, "exposesPost result: " + htmlString3);
            }
        }

        return true;
    }

    public String getExposesUrl(String htmlString) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        if (!urlString.contains("naver.com") || urlString.contains("naver.com/shortclips/")) {
            return null;
        }

        String channelUid = _httpEngine.getValueFromHtml(htmlString, "\"channelUid\"", ":", ",");

//        https://m.smartstore.naver.com/i/v2/channels/2sWDzhnOaJo6eecGDwhJb/clova/exposes
        return "https://m.smartstore.naver.com/i/v2/channels/" + channelUid + "/clova/exposes";
//        return "https://m.smartstore.naver.com/i/v1/clova/exposes";
    }

    public String getExposesBody(String htmlString) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        String body = null;

        try {
            JSONObject jsonObject = new JSONObject();

//            String channelNo = _httpEngine.getValueFromHtml(htmlString, "\"channelNo\"", ":", ",");
            String productNo = _httpEngine.getValueFromHtml(htmlString, "\"productID\"", ":", ",");

            JSONObject productsJsonObject = new JSONObject(_categoryProductsHtmlString);

            JSONArray productsVariables = productsJsonObject.getJSONArray("products");
            JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < productsVariables.length(); ++i) {
                JSONObject object = productsVariables.getJSONObject(i);
                Log.d("srgsg", "product id: " + object.getString("id"));
                jsonArray.put(object.getString("id"));
            }

//            JSONArray jsonArray = new JSONArray();
//
//            Document doc = Jsoup.parse(htmlString);
//            Elements elATags = doc.select("._3CVrEePCxg");
//            for (Element elTagA : elATags) {
//                String targetUrl = elTagA.attr("href");
//                String[] parts = targetUrl.split("/");
//                if (parts.length > 0) {
//                    String code = parts[parts.length - 1];
//                    jsonArray.put(code);
//                }
//            }

            jsonObject.put("section", "DETAIL");
            jsonObject.put("solution", "BASELINE_CAT");
            jsonObject.put("device", 1);
//            jsonObject.put("channelNo", channelNo);
            jsonObject.put("productNo", productNo);
            jsonObject.put("viewProductNoList", jsonArray);

            body = jsonObject.toString().replaceAll("\\\\" ,"");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return body;
    }

    public String getCatalogAllUrl(Document doc) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "base url: " + urlString);
        Map<String, String> queries = HttpEngine.query2MapFromUrlString(urlString);

        //본링크주소: https://msearch.shopping.naver.com/catalog/31300176618?cat_id=50002731&frm=MOSCPRO&query=%EB%B8%94%EB%9E%99%EB%B0%95%EC%8A%A4&NaPm=ct%3Dle9hwgds%7Cci%3D0f81bebd2b4f57805f66f9a0e4332ac8473b1956%7Ctr%3Dsls%7Csn%3D95694%7Chk%3D7fdafe43f3feb2a6417aac5053c555395393e161
        //카달페이지: https://msearch.shopping.naver.com/catalog/31300176618?NaPm=ct%3Dle9hy294%7Cci%3D4b5d6b5c22b191eb75c7326f6a44063e8eb28977%7Ctr%3Dsls%7Csn%3D95694%7Chk%3D58ed7bfbb02c6fab39c6602015fb63f6cb19362b&cat_id=50002731&frm=MOSCPRO&purchaseConditionSequence=20048015&query=%EB%B8%94%EB%9E%99%EB%B0%95%EC%8A%A4%20%EC%95%84%EC%9D%B4%EB%A1%9C%EB%93%9C%20tx5%202%EC%B1%84%EB%84%90&sort=LOW_PRICE
        //전체판매처: https://msearch.shopping.naver.com/catalog/31300176618/products?NaPm=ct%3Dle9hy294%7Cci%3D4b5d6b5c22b191eb75c7326f6a44063e8eb28977%7Ctr%3Dsls%7Csn%3D95694%7Chk%3D58ed7bfbb02c6fab39c6602015fb63f6cb19362b&cat_id=50002731&frm=MOSCPRO&fromWhere=CATALOG&purchaseConditionSequence=20048014&query=%EB%B8%94%EB%9E%99%EB%B0%95%EC%8A%A4%20%EC%95%84%EC%9D%B4%EB%A1%9C%EB%93%9C%20tx5%202%EC%B1%84%EB%84%90&sort=LOW_PRICE

        StringBuilder genUrl = new StringBuilder("https://msearch.shopping.naver.com/catalog/" + _mid + "/products?");

        genUrl.append("NaPm=").append(queries.get("NaPm"));
        genUrl.append("&cat_id=").append(queries.get("cat_id"));
        genUrl.append("&frm=").append(queries.get("frm"));
        genUrl.append("&fromWhere=CATALOG");

        // 상품옵션
        Elements elTagFilter = doc.select(".productFilter_btn_product__Smi9N.active");
        if (!elTagFilter.isEmpty()) {
            String value = _item.item.code3;

            if (TextUtils.isEmpty(value)) {
                String dataValue = elTagFilter.attr("data-nclick");
                Log.d(TAG, "dataValue1: " + dataValue);
                dataValue = dataValue.replace("N=a:prc*o.list,r:,i:", "");
                Log.d(TAG, "dataValue2: " + dataValue);
                value = dataValue;
            }

            genUrl.append("&purchaseConditionSequence=").append(value);
        }

        genUrl.append("&query=").append(queries.get("query"));
//        genUrl.append("&sort=").append(queries.get("sort"));
        genUrl.append("&sort=LOW_PRICE");

        return genUrl.toString();
    }

    public String getProductLogJsonString(String htmlString) {
        Log.d(TAG, "getCurrentUrl: " + _httpEngine.getCurrentUrl());
        Log.d(TAG, "getUrl: " + _httpEngine.getUrl());
        Log.d(TAG, "_clickRefererUrl: " + _clickRefererUrl);
        Map<String, String> queries = HttpEngine.query2MapFromUrlString(_httpEngine.getCurrentUrl());
        String naPm = queries.get("NaPm");
        String tr = "";

        try {
            naPm = URLDecoder.decode(naPm, "UTF-8");
            tr = _httpEngine.getValueFromHtml(naPm, "tr", "=", "|");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        return _httpEngine.makeProductLogApiBody(loadJson(htmlString), tr, _clickRefererUrl);
    }

    public String getBodyString() {
        Map<String, String> queries = HttpEngine.query2MapFromUrlString(_catalogUrl);
        String naPm = queries.get("NaPm");
        String query = queries.get("query");

        try {
            naPm = URLDecoder.decode(naPm, "UTF-8");
            query = URLDecoder.decode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return HttpEngine.makeCompanyListApiBody(_mid, queries.get("sort"), queries.get("purchaseConditionSequence"), _price, _companyPage, query, naPm, _hasOfficial);
    }

    public boolean fetchReviewsEvaluations(String htmlString) {
        if (_useNewAfter) {
            String urlString = _httpEngine.getCurrentUrl();
            String genUrl = getReviewsEvaluationsUrl(htmlString);
            Log.d(TAG, "### fetchReviewsEvaluations url: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                _httpEngine.setOrigin(null);
                _httpEngine.setReferer(urlString);
                String htmlString3 = _httpEngine.requestUrlWithOkHttpClientFetch(genUrl);
                Log.d(TAG, "fetchReviewsEvaluations result: " + htmlString3);
            }
        }

        return true;
    }

    public String getReviewsEvaluationsUrl(String htmlString) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        if (!urlString.contains("naver.com") || urlString.contains("naver.com/play/") || urlString.contains("naver.com/beauty/") || urlString.contains("naver.com/window-products/") || urlString.contains("naver.com/fresh/")) {
            return null;
        }

        String productNo = _httpEngine.getValueFromHtml(htmlString, "\"productNo\"", ":", ",");
        String categoryId = _httpEngine.getValueFromHtml(htmlString, "\"categoryId\"", ":", ",");
        String merchantNo = _httpEngine.getValueFromHtml(htmlString, "\"naverPaySellerNo\"", ":", ",");

        if (TextUtils.isEmpty(productNo) || TextUtils.isEmpty(categoryId) || TextUtils.isEmpty(merchantNo)) {
            return null;
        }

        return "https://m.smartstore.naver.com/i/v1/reviews/evaluations-result?originProductNo=" + productNo + "&leafCategoryId=" + categoryId + "&merchantNo=" + merchantNo;
    }

    public boolean fetchReviewsAttaches(String htmlString) {
        if (_useNewAfter) {
            String urlString = _httpEngine.getCurrentUrl();
            String genUrl = getReviewsAttachesUrl(htmlString);
            Log.d(TAG, "### fetchReviewsAttaches url: " + genUrl);

            if (!TextUtils.isEmpty(genUrl)) {
                _httpEngine.setOrigin(null);
                _httpEngine.setReferer(urlString);
                String htmlString3 = _httpEngine.requestUrlWithOkHttpClientFetch(genUrl);
                Log.d(TAG, "fetchReviewsAttaches result: " + htmlString3);
            }
        }

        return true;
    }

    public String getReviewsAttachesUrl(String htmlString) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        if (!urlString.contains("naver.com") || urlString.contains("naver.com/play/") || urlString.contains("naver.com/beauty/") || urlString.contains("naver.com/window-products/") || urlString.contains("naver.com/fresh/")) {
            return null;
        }

        String productNo = _httpEngine.getValueFromHtml(htmlString, "\"productNo\"", ":", ",");
        String merchantNo = _httpEngine.getValueFromHtml(htmlString, "\"naverPaySellerNo\"", ":", ",");

        if (TextUtils.isEmpty(productNo) || TextUtils.isEmpty(merchantNo)) {
            return null;
        }

        return "https://m.smartstore.naver.com/i/v1/reviews/attaches/ids-count?merchantNo=" + merchantNo + "&originProductNo=" + productNo + "&reviewContentClassTypes[]=VIDEO,PHOTO&reviewServiceType=SELLBLOG&sortType=REVIEW_RANKING";
    }



    public String getAmbulance11Body(String htmlString, String sti) {
        String urlString = _httpEngine.getCurrentUrl();
        Log.d(TAG, "current url: " + urlString);

        String body = null;

        try {
            JSONObject jsonObject = new JSONObject();

            String type = "PRODUCT";
            boolean isInitialRender = true;
            // isInitialRender 스크립트로 변경되는 페이지는 false, 실제 이동은 true.

            if (sti.equals("m_smartstore_category")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_search")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_bundle")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_sub")) {
//                type = "LST";
            } else if (sti.equals("m_smartstore_home")) {
                type = "HOME";
            }

            URL url = new URL(urlString);

            jsonObject.put("path", url.getPath());
            jsonObject.put("pathType", type);
            jsonObject.put("isInitialRender", isInitialRender);

//            "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String date = simpleDateFormat.format(new Date());
            jsonObject.put("clientTime", date);

            body = jsonObject.toString().replaceAll("\\\\" ,"");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return body;
    }
}
