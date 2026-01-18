package com.sec.android.app.sbrowser;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.github.ybq.android.spinkit.SpinKitView;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.sec.android.app.sbrowser.engine.AppHelper;
import com.sec.android.app.sbrowser.engine.ImageFinder;
import com.sec.android.app.sbrowser.engine.MathHelper;
import com.sec.android.app.sbrowser.engine.MobileNetworkController;
import com.sec.android.app.sbrowser.engine.NetworkEngine;
import com.sec.android.app.sbrowser.engine.ScreenCapture;
import com.sec.android.app.sbrowser.engine.SystemHelper;
import com.sec.android.app.sbrowser.engine.UserManager;
import com.sec.android.app.sbrowser.engine.Utility;
import com.sec.android.app.sbrowser.engine.WebViewManager;
import com.sec.android.app.sbrowser.library.proxy.common.retrofit.models.ProxyUserAuthData;
import com.sec.android.app.sbrowser.library.proxy.ip2world.Ip2WorldApi;
import com.sec.android.app.sbrowser.library.proxy.luna_proxy.LunaProxyApi;
import com.sec.android.app.sbrowser.library.proxy.luna_proxy.retrofit.models.LunaProxyData;
import com.sec.android.app.sbrowser.library.proxy.proxy4free.Proxy4FreeApi;
import com.sec.android.app.sbrowser.library.proxy.proxy4free.retrofit.models.Proxy4FreeData;
import com.sec.android.app.sbrowser.library.proxy.proxy_am.ProxyAmApi;
import com.sec.android.app.sbrowser.library.proxy.proxy_am.retrofit.models.ProxyAmData;
import com.sec.android.app.sbrowser.library.proxy.py_proxy.PyProxyApi;
import com.sec.android.app.sbrowser.library.proxy.py_proxy.retrofit.models.PyProxyData;
import com.sec.android.app.sbrowser.models.DeviceInfoData;
import com.sec.android.app.sbrowser.models.KeywordData;
import com.sec.android.app.sbrowser.models.KeywordItem;
import com.sec.android.app.sbrowser.models.KeywordItemMoon;
import com.sec.android.app.sbrowser.pattern.PatternHandlerThread;
import com.sec.android.app.sbrowser.pattern.PatternMessage;
import com.sec.android.app.sbrowser.pattern.WebViewMessage;
import com.sec.android.app.sbrowser.pattern.ably.shop.AblyShopPatternMessage;
import com.sec.android.app.sbrowser.pattern.action.ProcessAction;
import com.sec.android.app.sbrowser.pattern.ali.rank.AliRankPatternMessage;
import com.sec.android.app.sbrowser.pattern.ali.shop.AliShopPatternMessage;
import com.sec.android.app.sbrowser.pattern.common.CoupangUpdatePatternMessage;
import com.sec.android.app.sbrowser.pattern.common.PlayServiceUpdatePatternMessage;
import com.sec.android.app.sbrowser.pattern.common.UaChangePatternMessage;
import com.sec.android.app.sbrowser.pattern.common.UpdatePatternMessage;
import com.sec.android.app.sbrowser.pattern.common.UpdaterUpdatePatternMessage;
import com.sec.android.app.sbrowser.pattern.common.WebviewUpdatePatternMessage;
import com.sec.android.app.sbrowser.pattern.coupang.pc.CoupangPcPatternMessage;
import com.sec.android.app.sbrowser.pattern.coupang.rank.CoupangRankPatternMessage;
import com.sec.android.app.sbrowser.pattern.coupang.view.CoupangViewPatternMessage;
import com.sec.android.app.sbrowser.pattern.google.rank.GoogleRankPatternMessage;
import com.sec.android.app.sbrowser.pattern.google.view.GoogleViewPatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.active.NaverActivePatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.cookie.NaverSetCookiePatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.influencer.NaverInfluencerPatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.place.NaverPlaceChromePatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.place.NaverPlacePatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.rank.NaverRankPatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.shop.NaverShopChromePatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.shop.NaverShopPacketPatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.shop.NaverShopPatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.shop_keyword.NaverShopKeywordPatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.shop_pc.NaverShopPcPacketPatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.shop_pc.NaverShopPcPatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.site.NaverSitePatternMessage;
import com.sec.android.app.sbrowser.pattern.naver.view.NaverViewPatternMessage;
import com.sec.android.app.sbrowser.pattern.ohouse.rank.OHouseRankPatternMessage;
import com.sec.android.app.sbrowser.pattern.ohouse.shop.OHouseShopPatternMessage;
import com.sec.android.app.sbrowser.receiver.NetworkReceiver;
import com.sec.android.app.sbrowser.receiver.WifiReceiver;
import com.sec.android.app.sbrowser.system.AdbController;
import com.sec.android.app.sbrowser.system.DeviceController;
import com.sec.android.app.sbrowser.system.ToastManager;
import com.sec.android.app.sbrowser.system.WifiApController;
import com.sec.android.app.sbrowser.system.WifiController;
import com.sec.android.app.sbrowser.system.WifiProxyController;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.delight.android.webview.AdvancedWebView;

public class ActivityMCloud extends Activity {

    public static final String PACKAGE_NAME_SYSTEM_UPDATER = "com.zero.updater.zero";
    public static final String PACKAGE_NAME_SYSTEM_UPDATER_RANK = "com.zero.updater.rank";

    public static final String PACKAGE_NAME_PLAY_SERVICE = "com.google.android.gms";
    public static final String PACKAGE_NAME_SYSTEM_WEBVIEW = "com.google.android.webview";
    public static final String PACKAGE_NAME_COUPANG = "com.coupang.mobile";

    private static final boolean SCREEN_CAPTURE_MODE = true;

    private static final int REQUEST_SYSTEM_WRITE_SETTINGS = 1000;
    private static final int REQUEST_USAGE_ACCESS_SETTINGS_START = 1001;
    private static final int REQUEST_USAGE_ACCESS_SETTINGS = 1002;
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 3;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 4;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 5;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 6;

    protected static final int WEB_DEFAULT = 0;
    protected static final int WEB_NAVER = 1;
    protected static final int WEB_GOOGLE = 2;
    protected static final int WEB_COUPANG = 3;
    protected static final int WEB_OHOUSE = 4;

    private static final int DEVICE_TYPE_MOBILE = 10;
    private static final int DEVICE_TYPE_MOBILE_PARENT = 11;
    private static final int DEVICE_TYPE_MOBILE_CHILD = 12;
    private static final int DEVICE_TYPE_MOBILE_RANK = 50;


    public static Context o;
    private ArrayList<KeywordItemMoon> _keywords = new ArrayList<>();

    boolean 로그인쿠키 = false;

    String nnb;
    private static final String TAG = "ActivityMCloud";
    private SpinKitView _spinKitView = null;
    private AdvancedWebView mWebView = null;
    private TextView mTextViewUa = null;
    private TextView mTextViewVersion = null;

    private ProgressBar mProgressBar = null;
    private MainMessage _message = null;
    private PatternHandlerThread _handlerThread = null;
    int _cookieWorkCount = 0;
    int _cookieRetryCount = 0;
    int _currentIndex = 0;
    private WebViewManager _webViewManager = null;
    private PatternHandlerThread _patternHandlerThread = null;
    private List<PatternHandlerThread> _packetPatternHandlerThreadList = new ArrayList<>();
//    String[] 자동재부팅 = {"0800","2030"};
//    String[] 자동재부팅2 = {"0830","2100"};
    String[] 자동재부팅 = {"0700", "1300", "1900"};
    String[] 자동재부팅2 = {"0715", "1315", "1915"};
    String[] 자동재부팅3 = {"0730", "1330", "1930"};
    String[] 자동재부팅4 = {"0745", "1345", "1945"};
    private static final long NETWORK_CHECK_INTERVAL_TIME = 60000;
    private String _loginId = null;
    private WifiReceiver _wifiReceiver = null;
    private MessageBroadcastReceiver _br = null;
    private NetworkReceiver _networkReceiver = null;
    private boolean _wifiWaiting = false;
    private int _startRetryCount = 0;
    private int _keywordRetryCount = 0;
    private boolean _firstIpChanged = false;
    private String _imei = null;
    private int _targetWeb = WEB_NAVER;
    private boolean _changeUa = false;
    private boolean _isPc = false;
    private int _captureMode = 0;

    private boolean _restart = false;
    private boolean _isChecker = false;
    private boolean _isGetting = false;
    private boolean _didRunActive = false;
    private String _backupUa = null;

    private int _threadRunCount = 1;

    boolean _runProxy = false;
    private DeviceInfoData _deviceInfo = null;

    private UpdateHandlerThread __handlerThread = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_m_cloud);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        o = this;
        _br = new MessageBroadcastReceiver();
        _networkReceiver = new NetworkReceiver();

        _wifiReceiver = new WifiReceiver();
        _wifiReceiver.setOnWifiStateChangedListener(wifiStateChangedListener);

        _spinKitView = findViewById(R.id.spinKitView);


        mWebView = findViewById(R.id.webView);

//        mWebView.addJavascriptInterface(new MyJavascriptInterface(), "Android");

        mTextViewUa = findViewById(R.id.textViewUA);
        mTextViewUa.setText("UA: " + mWebView.getSettings().getUserAgentString());
        UserManager.getInstance().ua = mWebView.getSettings().getUserAgentString();
        mTextViewVersion = findViewById(R.id.textViewVersion);

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        // 강제끔.
//        _captureMode = 0;
        _captureMode = UserManager.getInstance().getCaptureMode(getApplicationContext());

        // 신규 암호화가 문제를 일으키므로 삭제. - 240823
        // 원래는 순위체크기 문제가 아니라 s4는 하면 안된다. - 240812
//        if (!BuildConfig.FLAVOR_mode.contains("rank")) {
        if (!DeviceController.isS4()) {
//            Log.e(TAG, "신규 암호화 추가: " + Build.MODEL);
//            Security.insertProviderAt(Conscrypt.newProvider(), 1);
        }

//        Button btn = findViewById(R.id.button);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e(TAG, "웹뷰 고백");
////                _webViewManager.goBack();
//
//                mWebView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        // title: 웹페이지를 사용할 수 없음
//                        Log.e(TAG, "title: " + mWebView.getTitle());
//                        Log.e(TAG, "Url: " + mWebView.getUrl());
//                        Log.e(TAG, "OriUrl: " + mWebView.getOriginalUrl());
//
//                        // 다시실행해본다.
//                        mWebView.loadUrl(mWebView.getUrl());
//                    }
//                });
//
//            }
//        });

        _webViewManager = new WebViewManager(mWebView);
        _webViewManager.setActivity(this);
        _webViewManager.setOnPageListener(new WebViewManager.OnPageListener() {
            @Override
            public void onPageStarted(WebView view) {
                Log.e(TAG, "onPageStarted");
            }
            @Override
            public void onPageLoaded(WebView view, String url) {

                Log.e(TAG, "onPageLoaded: " + url);

                Message msg = Message.obtain();
                msg.what = WebViewMessage.MSG_PAGE_LOADED;
                msg.obj = url;
                sendPatternMessage(msg);
//                _patternHandlerThread.getHandler().sendEmptyMessage(MSG_PAGE_LOADED);
            }

            @Override
            public void onPageLoadFailed(WebView view, int errorCode, String description, String failingUrl) {
                Message msg = Message.obtain();
                msg.what = WebViewMessage.MSG_PAGE_LOAD_FAILED;
                msg.arg1 = errorCode;
                msg.obj = failingUrl;
                sendPatternMessage(msg);
//                _patternHandlerThread.getHandler().sendEmptyMessage(WebViewMessage.MSG_PAGE_LOAD_FAILED);
            }
        });

        _webViewManager.setOnPageFinishedListener(new WebViewManager.OnPageFinishedListener() {
            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager manager = CookieManager.getInstance();
                manager.flush();
                String cookies = manager.getCookie(url);
                String out = "(" + url + ") " + cookies;

                Log.d(TAG, "Current Page All cookies: " + out);
            }
        });

        _webViewManager.setOnProgressChangedListener(new WebViewManager.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.d(TAG, "onProgressChanged: " + newProgress);

                mProgressBar.setProgress(newProgress);
                mProgressBar.setVisibility(View.VISIBLE);

                if (newProgress >= 100) {
                    mProgressBar.setVisibility(View.GONE);
//                    setTitle(view.getTitle());

//                    Log.v(TAG, "getOriginalUrl: " + view.getOriginalUrl());
//                    Log.v(TAG, "getUrl: " + view.getUrl());
//                    Log.v(TAG, "getTitle: " + view.getTitle());
                }
                if (newProgress <= 10) {
//                    Log.v(TAG, "new getOriginalUrl: " + view.getOriginalUrl());
//                    Log.v(TAG, "new getUrl: " + view.getUrl());
//                    Log.v(TAG, "new getTitle: " + view.getTitle());
                }
            }
        });

//        getCodeFromUrl("https://m.place.naver.com/restaurant/1702472176/home?entry=ple");
//        getCodeFromUrl("https://m.place.naver.com/restaurant/1272157573");
//        getCodeFromUrl("https://store.naver.com/restaurants/detail?id=1484698932");
//        getCodeFromUrl("https://m.place.naver.com/restaurant/1799314536/home?entry=plt");
//        long ss = getRemainNextHourSeconds();

//        Log.d(TAG, "telecom: " + SystemHelper.getTelecomName(this));
//        Log.d(TAG, "batt: " + SystemHelper.getBatteryRemain(this));
//        Log.d(TAG, "hel: " + SystemHelper.getBatteryHealth(this));
//        SystemHelper.logDeviceInfo();
//        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
//        Log.d(TAG, "bbbb: " + bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));
//
//        try {
//            URL urlBase = new URL("https://ssl.pstatic.net/sstatic/sdyn.js?f=/au/m/_nx/2021/nx_1028.js+/au/m/_common/nhn.common_210105.js+/au/");
//            String ss = DnsParser.getDomain(urlBase.getHost());
//            int portBase = urlBase.getPort();
//            int portReferer = urlBase.getDefaultPort();
//            int i = 0;
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }

//        try {
//            URL urlBase = new URL("https://wcs.naver.com/m?u=https%3A%2F%2Fm.smartstore.naver.com%2Ffarmkim%2Fproducts%2F6598374353%3FNaPm%3Dct%253Dlbdd6rtk%257Cci%253D0675774faa6b0af7590c5e39da8f0a7cd42408b2%257Ctr%253Dslsl%257Csn%253D1252526%257Chk%253D1e9ae69c269b4c90e8029ffd0f788a18572f6f8e&e=https%3A%2F%2Fmsearch.shopping.naver.com%2Fsearch%2Fall%3Fquery%3D%25EC%25A0%2588%25EC%259E%2584%25EB%25B0%25B0%25EC%25B6%2594%26bt%3D1%26frm%3DMOSCPRO&wa=s_eec9ffd586ef&bt=-1&vtyp=DET&pid=6598374353&pnm=%ED%95%B4%EB%82%A8%20%EC%A0%88%EC%9E%84%EB%B0%B0%EC%B6%94%2020kg%20%EA%B9%80%EC%94%A8%EB%86%8D%EA%B0%80&lcatid=50000006&lcatnm=%EC%8B%9D%ED%92%88&mcatid=50000147&mcatnm=%EA%B9%80%EC%B9%98&scatid=50002031&scatnm=%EC%A0%88%EC%9E%84%EB%B0%B0%EC%B6%94&mid=510689988&chno=100737521&mtyp=STF&os=MacIntel&ln=ko-KR&sr=400x929&bw=400&bh=929&c=30&j=N&jv=1.8&k=Y&ct=&cs=UTF-8&tl=%25ED%2595%25B4%25EB%2582%25A8%2520%25EC%25A0%2588%25EC%259E%2584%25EB%25B0%25B0%25EC%25B6%2594%252020kg%2520%25EA%25B9%2580%25EC%2594%25A8%25EB%2586%258D%25EA%25B0%2580&vs=0.8.6&nt=1670400264758&EOU");
//        "https://wcs.naver.com/m?u=https%3A%2F%2Fm.brand.naver.com%2Fchamoa%2Fproducts%2F4676980831%3FNaPm%3Dct%253Dlbdfyswg%257Cci%253De25a5b90553de6584b323a984082719a0afc1dbc%257Ctr%253Dslsl%257Csn%253D243203%257Chk%253Dc13b9d5a4330646b86542ea8b46b118a26b85af9&e=&wa=300714aed52d98&bt=-1&os=Linux%20armv8l&ln=ko-KR&sr=360x640&bw=360&bh=575&c=24&j=N&jv=1.8&k=Y&ct=&cs=UTF-8&tl=%25EC%25B0%25A8%25EB%25AA%25A8%25EC%2595%2584%2520%25EB%259F%25AD%25EC%2585%2594%25EB%25A6%25AC%2520%25ED%258E%25A0%25ED%258A%25B8%2520%25EC%259E%2590%25EB%258F%2599%25EC%25B0%25A8%2520%25EB%258F%2584%25EC%2596%25B4%25EC%25BB%25A4%25EB%25B2%2584%25203T%2520%25EA%25B7%25B8%25EB%259E%259C%25EC%25A0%25B8xg%252098%25EB%2585%2584~05%25EB%2585%2584%2520CH101&vs=0.8.6&nt=1670404937031&EOU"
//            String ss = DnsParser.getDomain(urlBase.getHost());
//            int portBase = urlBase.getPort();
//            int portReferer = urlBase.getDefaultPort();
//            String qq = urlBase.getQuery();
//            String q2 = qq.replace("Linux%20armv7l", "Linux%20armv8l");
//            String q3 = URLDecoder.decode(qq, "UTF-8");
//            String q4 = URLDecoder.decode(q3, "UTF-8");
//            Log.d("TAG", qq);
//            Log.d("TAG", q2);
//            Log.d("TAG", q3);
//            Log.d("TAG", q4);
//            int i = 0;
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

//        for (int i = 0; i < 20; ++i) {
////            String generatedString = RandomStringUtils.randomAlphanumeric(10);
////            String generatedString = Utility.getRandomString(3, 5, null);
//            String generatedString = BasePatternAction.getRandomName(null);
//            Log.d(TAG, "Random: " + generatedString);
//        }
//
//        for (int i = 0; i < 20; ++i) {
////            String generatedString = RandomStringUtils.randomAlphanumeric(10);
////            String generatedString = Utility.getRandomString(3, 5, "sb");
//            String generatedString = BasePatternAction.getRandomName("sb");
//            Log.d(TAG, "Random: " + generatedString);
//        }
//
//        HttpEngine.makeCompanyListApiBody("121212", "LOW_PRICE", "", "1800", "1", "ssfgsr", "ssrgs", false);

        //jQuery22407140817681797409
        //jQuery2240.5428531932587741
//        String jq = "2.2.4" + Math.random();
//        Log.d(TAG, "jq:" + jq);
//        jq =  jq.replaceAll("\\D", "");
//        Log.d(TAG, "jq11:" + jq);

//        _webViewManager.loadUrl("http://whoer.net");
//        Log.d(TAG, "UA: " + mWebView.getSettings().getUserAgentString());

//        _webViewManager.clearCookie();
//        _webViewManager.loadUrl("http://naver.com");

//        CookieFileManager manager = new CookieFileManager();
//        String ss = manager.getAllCookieString(this, ".naver.com");

//        CookieManager manager = CookieManager.getInstance();
//        String ss = manager.getCookie(".smartstore.naver.com");
//        Log.d(TAG, ss);
//
//        NaverLoginCookieAction loginCookieAction = new NaverLoginCookieAction(this);
//        loginCookieAction.isLoginChrome();

//        NaverAccountData accountData = new NaverAccountData();
//        NaverAccount ss = new NaverAccount();
//        ss.status = 1;
//        ss.accountName = "ssrg";
//        accountData.accountList.add(ss);
//
//        NaverAccount ss2 = new NaverAccount();
//        ss2.status = 2;
//        ss2.accountName = "zelijsg";
//        accountData.accountList.add(ss2);
//
//        String ssJson = accountData.getJsonString();
//        Log.d(TAG, ssJson);
//
//
//        NaverAccountData nn = NaverAccountData.getFromJson(ssJson);
//        Log.d(TAG, nn.getJsonString());
//        NaverAccountManager ss = NaverAccountManager.getInstance();
//        ss.saveData(this);
//        ss.loadData(this);
//
//        if (true) return;


        _message = new MainMessage();
        _handlerThread = new PatternHandlerThread();
        _handlerThread.setOnHandleMessageListener(_message);
        _handlerThread.start();

//        __handlerThread = new UpdateHandlerThread();
//        __handlerThread.context = this;
//        __handlerThread.start();

        _patternHandlerThread = new PatternHandlerThread();
        _patternHandlerThread.start();

        // 패킷 처리용 전용 쓰레드.
        for (int i = 0; i < 10; ++i) {
            PatternHandlerThread thread = new PatternHandlerThread();
            thread.start();
            _packetPatternHandlerThreadList.add(thread);
        }

//        SystemClock.sleep(1000);
        updatedVersionTextView();

//        SystemController con = new SystemController();
//        con.thermal();

//        finishPattern();
//        changeIp();
         // 페이스북쿠키();
         //  sendNotSavedMessage(REBOOT_CHK, 1000);
            //  sendEmptyMessageDelayed(GET_KEYWORDS, 1000);
        // sendEmptyMessageDelayed(TEST, 1000);

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS)
//                != PackageManager.PERMISSION_GRANTED) {
//
//
//
//            // READ_PHONE_STATE permission has not been granted.
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.WRITE_SETTINGS)) {
//                // Provide an additional rationale to the user if the permission was not granted
//                // and the user would benefit from additional context for the use of the permission.
//                // For example if the user has previously denied the permission.
//            } else {
//                // READ_PHONE_STATE permission has not been granted yet. Request it directly.
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.WRITE_SETTINGS},
//                        3);
//            }
//            return;
//        }

//        mWebView.post(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG, "UA: " + mWebView.getSettings().getUserAgentString());
////                mWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 7.0; SAMSUNG SM-A310N0) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/14.2 Chrome/87.0.4280.141 Mobile Safari/537.36");
////                mWebView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 5.0.1; SHV-E330K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Mobile Safari/537.36");
//                mWebView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko");
//                Log.d(TAG, "To UA: " + mWebView.getSettings().getUserAgentString());
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SYSTEM_WRITE_SETTINGS) {
            // 다시 검사.
            sendEmptyMessageDelayed(PERMISSION_CHECK, 0);
//        } else if (requestCode == REQUEST_USAGE_ACCESS_SETTINGS) {
//            sendEmptyMessageDelayed(_message.nextMessage, 0);
//        } else if (requestCode == ScreenCapture.REQUEST_CODE) {
//            ImageFinder.getInstance().getScreenCapture().onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == REQUEST_USAGE_ACCESS_SETTINGS_START) {
            // 다시 검사.
            sendEmptyMessageDelayed(PERMISSION_CHECK2, 0);
        } else if (requestCode == ScreenCapture.REQUEST_CODE) {
            ImageFinder.getInstance().getScreenCapture().onActivityResult(requestCode, resultCode, data);
        }
    }

    public boolean checkUsageStatsPermission(int requestCode) {
        if (!AppHelper.hasUsageStatsPermission(this)) {
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), requestCode);
            return false;
        }

        return true;
    }

    private boolean dataEnabled() {
        boolean dataEnabled = false;

        try {
            dataEnabled = MobileNetworkController.isMobileDataEnabledFromLollipop(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataEnabled;
    }

    private void updatedVersionTextView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = "v" + SystemHelper.getVersionCode(getApplicationContext());
                text = AppHelper.getVersionCode(getApplicationContext(), PACKAGE_NAME_SYSTEM_WEBVIEW) + " / " + text;

                if (!TextUtils.isEmpty(_loginId)) {
                    text = "[" + _loginId + "] " + text;
                }

                mTextViewVersion.setText(text);
            }
        });
    }

    public void showMessage(String text) {
        Log.d(TAG, text);
        ToastManager.getInstance().showText(o, text, Toast.LENGTH_SHORT);
//        Toast.makeText(o, text, Toast.LENGTH_SHORT).show();
    }


    public static final int REBOOT_CHK = 0;
    public static final int LOOPER = 1;
    public static final int START = 2;
    public static final int END = 3;

    public static final int CAPTURE_PREPARE = 10;
    public static final int CHECK_UPDATE = 100;
    public static final int REGISTER_DEVICE = 101;
    public static final int GET_KEYWORDS = 102;
    public static final int GET_IP = 103;
    public static final int TOGGLE_WIFI = 104;
    public static final int TOGGLE_DATA_NETWORK = 105;
    public static final int AUTO_CHANGE_IP = 106;
    public static final int SET_WIFI_PROXY = 107;
    public static final int GET_PROXY_IP = 108;

    public static final int CHECK_IP = 110;

    public static final int GET_DEVICE_INFO = 151;

    public static final int WORK_START = 201;
    public static final int WORK_END = 202;
    public static final int WORK_RUN = 203;
    public static final int WORK_CHECK = 204;
    public static final int CHANGE_DATA = 205;

    public static final int PERMISSION_CHECK = 401;
    public static final int PERMISSION_CHECK2 = 402;
    public static final int APP_CHECK = 302;

    public static final int APP_RESTART = 301;
    public static final int UPDATE_MESSAGE = 501;

    public static final int TEST = 1000;


    public class MainMessage implements PatternHandlerThread.OnHandleMessageListener {
        public int _lastMessage = 0;
        public long _lastTime = 0;
        public int nextMessage = 0;

        @Override
        public void onHandleMessage(Handler handler, Message msg) {
            // 1은 저장하지 않는다는 의미이다.
            if (msg.arg1 != 1) {
                _lastMessage = msg.what;
                _lastTime = System.currentTimeMillis();
            } else {
//                Log.d(TAG, "받은 메시지 저장안함: " + msg.what);
            }

            switch (msg.what) {
                case PatternHandlerThread.MESSAGE_PREPARED_THREAD:
                    sendEmptyMessageDelayed(PERMISSION_CHECK, 0);
                    break;

                case CAPTURE_PREPARE:
                    ImageFinder.getInstance().getScreenCapture().startCapture(ActivityMCloud.this);
                    break;

                case START:
                    if (BuildConfig.PARENT_MODE) {
                            // Parent mode
                            if (WifiController.isWifiEnabled(ActivityMCloud.this)) {
                                showMessage("Wifi 네트워크 종료 중...");
                                // 와이파이가 켜져있다면 와이파이를 끈다.
                                WifiController.wifiEnabled(ActivityMCloud.this, false);
                            } else {
                                if (AdbController.isAirplaneMode()) {
                                    showMessage("비행기 모드 끄는 중...");
                                    AdbController.setAirplaneMode(false);
                                    touchEnabled(false);
                                } else {
                                    // 데이터가 꺼져있다면 킨다.
                                    if (!dataEnabled()) {
                                        showMessage("데이터 네트워크 연결 중...");
                                        touchEnabled(false);
                                        sendNotSavedMessage(TOGGLE_DATA_NETWORK, 0);
                                    } else {
                                        // 22.08.26 - 시작하면 아이피 바꾸고 시작한다.
                                        if (UserManager.getInstance().getChangeIp(ActivityMCloud.this)) {
                                            showMessage("시작전 아이피 변경 중...");
                                            UserManager.getInstance().setChangeIp(ActivityMCloud.this, false);
                                            UserManager.getInstance().setIpChangeTime(ActivityMCloud.this);
                                            changeIp();
                                        } else {
                                            onMessageStart();
                                        }
                                    }
                                }
                            }
                    } else {
                        Log.d(TAG, "자식모드 실행.");
                        // Child mode
                        if (WifiController.isWifiEnabled(ActivityMCloud.this)) {
                            // 와이파이가 켜져있다면 와이파이를 재접속한다.
                            // 와이파이는 켜져있지만 접속이 안되어 있을수 있으므로.
                            // 알고 있는 와이파이에 접속하기 위해 재접속 시도한다.
                            ConnectivityManager conn =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo networkInfo = conn.getActiveNetworkInfo();

                            if ((networkInfo != null) && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
                                // 와이파이가 연결되어 있다면 진행한다.
//                                onMessageStart();

                                String proxy = WifiProxyController.getWifiProxy(ActivityMCloud.this);

                                if (proxy == null) {
                                    // 프록시가 없다면 바로 시작한다.
                                    Log.d(TAG, "프록시가 설정되지 않아 바로 시작");
                                    UserManager.getInstance().setProxy(_webViewManager.getWebView().getContext(), null);
                                    onMessageStart();
                                } else {
//                                    if (UserManager.getInstance().getProxySkip(ActivityMCloud.this)) {
//                                        // 프록시 변경하지 않고 시작.
//                                        Log.d(TAG, "프록시를 초기화 없이 바로 시작");
//                                        onMessageStart();
//                                    } else {
                                        // 프록시 정보를 삭제하기 위해 와이파이를 껐다 켜준다.
                                        showMessage("프록시 초기화 후 Wifi 네트워크 종료 중...");
//                                        _networkWaiting = true;
                                        WifiProxyController.unsetWifiProxy(ActivityMCloud.this);
//                                    }
                                }
                            } else {
                                // 조금더 테스트 해야하지만, 와이파이가 켜져있는 상태라면 끄지말고 와이파이를 연결한다.
                                connectWifi();
//                                showMessage("Wifi 네트워크 종료 중...");
//                                WifiController.wifiEnabled(ActivityMCloud.this, false);
//                                _wifiWaiting = true;
                            }
                        } else {
                            // 와이파이가 꺼져있다면 와이파이를 켠다.
                            showMessage("Wifi 네트워크 연결 중...");
                            WifiController.wifiEnabled(ActivityMCloud.this, true);
                        }
                    }
                    sendNotSavedMessage(REBOOT_CHK, 1000);
                    break;

                case END:
                    restart();
                    break;

                case REBOOT_CHK:
                    REBOOT_CHK();
                    break;

                case CHECK_UPDATE:
                    checkUpdate(1);
                    break;

                case REGISTER_DEVICE:
                    registerDevice();
                    break;
                case CHECK_IP:
                    checkIp();
                    break;


                case WORK_START:
                    if (_changeUa) {
                        changeUa();
                    } else if (_targetWeb == WEB_NAVER) {
                        changeData();
//                    } else if (_targetWeb == WEB_COUPANG) {
//                        changeUa();
                    } else {
                        _webViewManager.clearCookie();
                        initPattern();
                    }
                    break;
                case CHANGE_DATA:
                    changeData();
                    break;
                case WORK_RUN:
                    // 여기에서 네이버면 프록시모드일 경우 프록시를 설정하고 시작한다.
                    if (!_runProxy && _deviceInfo.proxy != null) {
                        getProxyIp();
                    } else {
                        initPattern();
                    }
                    break;
                case GET_KEYWORDS:
                    슬롯불러오기();
                    break;

                case TOGGLE_DATA_NETWORK:
                    AdbController.setDataNetworkThread(!dataEnabled());
//                    toggleDataNetwork();
                    break;

                case AUTO_CHANGE_IP:
                    changeIp();
                    break;

                case SET_WIFI_PROXY:
                    setWifiProxy();
                    break;

                case GET_PROXY_IP:
                    getProxyIp();
                    break;

                case TEST:
                    테스트();
                    break;

                case PERMISSION_CHECK:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.System.canWrite(ActivityMCloud.this)) {
                            if (_captureMode > 0) {
                                sendEmptyMessage(PERMISSION_CHECK2);
                            } else {
                                start();
                            }
                        } else {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            startActivityForResult(intent, REQUEST_SYSTEM_WRITE_SETTINGS);
                        }
                    } else {
                        if (_captureMode > 0) {
                            sendEmptyMessage(PERMISSION_CHECK2);
                        } else {
                            start();
                        }
                    }
                    break;

                case PERMISSION_CHECK2:
                    if (verifyStoragePermissions()) {
                        if (checkUsageStatsPermission(REQUEST_USAGE_ACCESS_SETTINGS_START)) {
                            ImageFinder.getInstance().init(ActivityMCloud.this, SCREEN_CAPTURE_MODE);

                            if (SCREEN_CAPTURE_MODE) {
                                ImageFinder.getInstance().getScreenCapture().setPrepareCallback(new ScreenCapture.PrepareCallback() {
                                    @Override
                                    public void success(ScreenCapture capture) {
                                        Log.d(TAG, "캡쳐 준비됨.");
                                        start();
                                    }

                                    @Override
                                    public void failed(ScreenCapture capture, int code, String message) {
                                        Log.d(TAG, "준비에 실패해서 다시 시도.");
                                        sendEmptyMessage(CAPTURE_PREPARE);
                                    }
                                });

                                sendEmptyMessage(CAPTURE_PREPARE);
                            } else {
                                start();
                            }
//                            takeScreenshot();
                        }
                    }
                    break;

                case UPDATE_MESSAGE:
                    showMessage((String) msg.obj);
                    break;

                case PatternMessage.END_PATTERN:
                    PatternHandlerThread thread = _patternHandlerThread;

                    if (msg.obj != null) {
                        thread = (PatternHandlerThread) msg.obj;
                    }

                    finishPattern(thread);
                    break;

            }
        }
    }

    public boolean sendNotSavedMessage(int what, long delayMillis) {
        if (_handlerThread != null) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = 1;

            return _handlerThread.getHandler().sendMessageDelayed(msg, delayMillis);
        }

        return false;
    }

    public boolean sendEmptyMessage(int what) {
        if (_handlerThread != null) {
            return _handlerThread.getHandler().sendEmptyMessage(what);
        }

        return false;
    }

    public boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        if (_handlerThread != null) {
            return _handlerThread.getHandler().sendEmptyMessageDelayed(what, delayMillis);
        }

        return false;
    }

    public boolean restartMessageDelayed(long delayMillis) {
        if (_handlerThread != null) {
            _handlerThread.getHandler().removeCallbacksAndMessages(null);
        }

        return sendEmptyMessageDelayed(START, delayMillis);
    }

    public boolean sendPatternMessage(Message msg) {
        if (_patternHandlerThread != null) {
            return _patternHandlerThread.getHandler().sendMessage(msg);
        }

        return false;
    }


    private void start() {
        _loginId = UserManager.getInstance().getLoginId(ActivityMCloud.this);
        updatedVersionTextView();

        if (TextUtils.isEmpty(_loginId)) {
            showInputId();
        } else {
            float baseDelaySec = 0.5f;
            int delayMills = (int) (baseDelaySec * 1000);
            Log.d(TAG, "로그인 아이디: " + _loginId);
            showMessage(baseDelaySec + "초 후 작업을 시작합니다.");
            sendEmptyMessageDelayed(START, delayMills);
        }
    }

    private void showInputId() {
        showInputIdMessage("로그인 아이디를 입력해주세요.", null);
    }

    private void showInputIdMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(message);

// Set up the input
        final EditText input = new EditText(this);
//        input.setPadding(16, 10, 16, 0);
//        input.set
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

//        LinearLayout.LayoutParams

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(params);
//        linearLayout.setW

        float dpRatio = getResources().getDisplayMetrics().density;
//        int pixelForDp = (int)dpValue * dpRatio;

        linearLayout.setPadding((int)(16 * dpRatio), (int)(10 * dpRatio), (int)(16 * dpRatio), 0);

        linearLayout.addView(input);

        builder.setView(linearLayout);

//        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("확인", null);
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
            //    ActivityMCloud.super.onBackPressed();
              //  ApplicationManager.safeFinish();
            }
        });

//        builder.show();

        AlertDialog dialog =  builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        _loginId = input.getText().toString().trim();

                        if (_loginId.length() < 2) {
                            Utility.showAlert(ActivityMCloud.this, "아이디는 두자 이상이어야 합니다.");
                            return;
                        }

                        UserManager.getInstance().setLoginId(
                                getApplicationContext(),
                                _loginId);

                        Log.d(TAG, "저장 완료.");
                        dialog.dismiss();

                        // 작업시작.
                        sendEmptyMessageDelayed(START, 1000);
                    }
                });

            }
        });

        dialog.show();
    }

    private void onMessageStart() {
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.HOUR, -1);
//        Date preDate = cal.getTime();

        Date lastCheckDate = UserManager.getInstance().getUpdateCheckTimeDate(this);
        boolean check = false;

        if (lastCheckDate == null) {
            check = true;
        } else {
            long now = System.currentTimeMillis();

            // 업데이트는 두시간 단위로 체크한다.
            if (now - lastCheckDate.getTime() > 3600 * 1000) {
//            if (now - lastCheckDate.getTime() > 3600 * 2 * 1000) {
                check = true;
            }
        }

        // 항시검사. - test
//        check = true;

        if (check) {
            if (!DeviceController.isS4() && AdbController.isPackageVerifier()) {
                AdbController.setPackageVerifier(false);
            }

            UserManager.getInstance().setUpdateCheckTime(this);
            sendEmptyMessageDelayed(CHECK_UPDATE, 100);
        } else {
            getImei();
        }
    }

    public void getImei() {
        //Get IMEI Number of Phone  //////////////// for this example i only need the IMEI
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // READ_PHONE_STATE permission has not been granted.
            requestReadPhoneStatePermission();
            return;
        }

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // ACCESS_FINE_LOCATION permission has not been granted.
//            requestAccessFineLocationPermission();
//            return;
//        }

        //Have an  object of TelephonyManager
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            _imei = tm.getImei();
        } else {
            _imei = tm.getDeviceId();
        }

        UserManager.getInstance().imei = _imei;

        touchEnabled(false);
        sendEmptyMessageDelayed(REGISTER_DEVICE, 100);
    }

    public boolean verifyStoragePermissions() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // READ_EXTERNAL_STORAGE permission has not been granted.
//            requestReadStoragePermission();
//            return false;
//        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // WRITE_EXTERNAL_STORAGE permission has not been granted.
            requestWriteStoragePermission();
            return false;
        }

        return true;
    }

    private void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }
    }

    private void requestAccessFineLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
        } else {
            // ACCESS_FINE_LOCATION permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void requestReadStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private void requestWriteStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImei();
            } else {
            }
        }
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImei();
            } else {
            }
        }
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendEmptyMessage(PERMISSION_CHECK2);
            } else {
            }
        }
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendEmptyMessage(PERMISSION_CHECK2);
            } else {
            }
        }
    }

    private long _lastTryTime = 0;
    private void REBOOT_CHK() {
        long currentTime = System.currentTimeMillis();
        long problemInterval = 2400000;//60000 * 40;

        if (currentTime > _message._lastTime + problemInterval &&
                currentTime > _lastTryTime + problemInterval) {
            // 장비 이상으로 재부팅.
            재부팅();
            return;
        }

        if (BuildConfig.PARENT_MODE) {
            // 마지막 메시지 처리후 인터벌이 지났다면 네트워크 연결을 검사한다.
            if (currentTime > _message._lastTime + NETWORK_CHECK_INTERVAL_TIME &&
                    currentTime > _lastTryTime + NETWORK_CHECK_INTERVAL_TIME) {
                ConnectivityManager conn =  (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = null;

                if (conn != null) {
                    networkInfo = conn.getActiveNetworkInfo();
                }

                _lastTryTime = currentTime;

                if (networkInfo != null) {
                    // 모바일 상태가 아니라면 연결을 시도한다.
                    if (networkInfo.getType() != ConnectivityManager.TYPE_MOBILE) {
                        Log.d(TAG, "[onLooper] 모바일 상태가 아니라서 연결시도.");
                        sendNotSavedMessage(AUTO_CHANGE_IP, 0);
                    } else {
                        Log.d(TAG, "[onLooper] 모바일 연결 중이어서 콜백 강제 실행.");
                        runNetworkConnected(NetworkReceiver.ANY_CONNECTED);
                    }
                } else {
                    Log.d(TAG, "[onLooper] 오프라인 상태라서 연결시도.");
                    sendNotSavedMessage(AUTO_CHANGE_IP, 0);
                }
            }
        } else {
            // 마지막 메시지 처리후 인터벌이 지났다면 네트워크 연결을 검사한다.
//            long interval = NETWORK_CHECK_INTERVAL_TIME;
            long interval = 120000;//60000 * 2;

            if (currentTime > _message._lastTime + interval &&
                    currentTime > _lastTryTime + interval) {
                _lastTryTime = currentTime;

                if (WifiController.isWifiEnabled(ActivityMCloud.this)) {
                    // 와이파이가 켜져있다면 와이파이를 재접속한다.
                    // 와이파이는 켜져있지만 접속이 안되어 있을수 있으므로.
                    // 알고 있는 와이파이에 접속하기 위해 재접속 시도한다.
                    ConnectivityManager conn =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = conn.getActiveNetworkInfo();

                    if (networkInfo != null) {
                        // 모바일 상태가 아니라면 연결을 시도한다.
                        if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
                            Log.d(TAG, "[onLooper] Wifi 연결 상태가 아니라서 연결시도.");
                            connectWifi();
                        } else {
                            Log.d(TAG, "[onLooper] Wifi 연결 중이어서 콜백 강제 실행.");
                            runNetworkConnected(NetworkReceiver.WIFI_CONNECTED);
                        }
                    } else {
                        Log.d(TAG, "[onLooper] 오프라인 상태라서 연결시도.");
                        showMessage("Wifi 네트워크 연결 중...");
                        WifiController.wifiEnabled(ActivityMCloud.this, false);
                    }
                } else {
                    // 와이파이가 꺼져있다면 와이파이를 켠다.
                    Log.d(TAG, "[onLooper]");
                    showMessage("Wifi 네트워크 연결 중...");
                    WifiController.wifiEnabled(ActivityMCloud.this, true);
                }
            }
        }

        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDate = new SimpleDateFormat("HHmm");
        String getTime = simpleDate.format(mDate);

        // ap장비(apOnly = 1)는 재부팅하지 않는다.
        if ((_deviceInfo != null && _deviceInfo.apOnly == 0) && !BuildConfig.FLAVOR_mode.contains("rank")) {
            int rest = getLoginIdLastNumber() % 4;

            if (rest == 1) {
                if (getTime.equals(자동재부팅[0])){
                    재부팅();
                    return;
                } else if (getTime.equals(자동재부팅[1])){
                    재부팅();
                    return;
                } else if (getTime.equals(자동재부팅[2])){
                    재부팅();
                    return;
                }
            } else if (rest == 2) {
                if (getTime.equals(자동재부팅2[0])){
                    재부팅();
                    return;
                } else if (getTime.equals(자동재부팅2[1])){
                    재부팅();
                    return;
                } else if (getTime.equals(자동재부팅2[2])){
                    재부팅();
                    return;
                }
            } else if (rest == 3) {
                if (getTime.equals(자동재부팅3[0])){
                    재부팅();
                    return;
                } else if (getTime.equals(자동재부팅3[1])){
                    재부팅();
                    return;
                } else if (getTime.equals(자동재부팅3[2])){
                    재부팅();
                    return;
                }
            } else {
                if (getTime.equals(자동재부팅4[0])){
                    재부팅();
                    return;
                } else if (getTime.equals(자동재부팅4[1])){
                    재부팅();
                    return;
                } else if (getTime.equals(자동재부팅4[2])){
                    재부팅();
                    return;
                }
            }

//            if (isOddLoginId()) {
//                if (getTime.equals(자동재부팅[0])){
//                    재부팅();
//                    return;
//                } else if (getTime.equals(자동재부팅[1])){
//                    재부팅();
//                    return;
//                }
//            } else {
//                if (getTime.equals(자동재부팅2[0])){
//                    재부팅();
//                    return;
//                } else if (getTime.equals(자동재부팅2[1])){
//                    재부팅();
//                    return;
//                }
//            }
        }

        // 5초마다 체크.
        sendNotSavedMessage(REBOOT_CHK, 5000);
    }

    public boolean isOddLoginId() {
        boolean odd = true;

        if (!TextUtils.isEmpty(_loginId)) {
            String[] ids = _loginId.split("-");

            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(ids[0]);

            if (!BuildConfig.PARENT_MODE) {
                if (ids.length >= 2) {
                    m = p.matcher(ids[1]);
                }
            }

            while(m.find()) {
                int number = Integer.valueOf(m.group());
                if (number % 2 == 0) {
                    odd = false;
                }
                break;
            }
        }

        return odd;
    }

    public int getLoginIdLastNumber() {
        int number = 0;

        if (!TextUtils.isEmpty(_loginId)) {
            String[] ids = _loginId.split("-");

            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(ids[0]);

            if (!BuildConfig.PARENT_MODE) {
                if (ids.length >= 2) {
                    m = p.matcher(ids[1]);
                }
            }

            while(m.find()) {
                number = Integer.valueOf(m.group());
                break;
            }
        }

        return number;
    }

    public void registerDevice() {
        String webPackageName = PACKAGE_NAME_SYSTEM_WEBVIEW;

        if (_captureMode == 1) {
            webPackageName = ProcessAction.PACKAGE_NAME_CHROME;
        }

        NetworkEngine.getInstance().registerDevice(new NetworkEngine.Callback() {
            @Override
            public <T extends Object> void finishSuccess(T data) {
                DeviceInfoData infoData = (DeviceInfoData) data;
                _deviceInfo = infoData;
                _webViewManager.setDeleteLocalStorage(infoData.deleteLocalStorage);
                _startRetryCount = 0;

                if (TextUtils.isEmpty(infoData.xRequestedWith)) {
                    Log.d(TAG, "X-Requested-With 빈값 설정");
                    mWebView.addHttpHeader("X-Requested-With", "");
                } else {
                    if (!infoData.xRequestedWith.equals(getPackageName())) {
                        Log.d(TAG, "X-Requested-With: " + getPackageName() + " -> " + infoData.xRequestedWith);
                        mWebView.addHttpHeader("X-Requested-With", infoData.xRequestedWith);
                    }
                }

                // 서버에서 필터해주지만, 안전하게 클라에서도 해준다.
                if (!DeviceController.isS7Under()) {
                    UserManager.getInstance().setCaptureMode(getApplicationContext(), _deviceInfo.captureMode);
                }

                //TogetherWebDataManager.getInstance().setMaxDataCount(ActivityMCloud.this, infoData.webDataCount);
                if (infoData.type == 11) {
                    UserManager.getInstance().setTargetSsid(getApplicationContext(), infoData.targetSsid);
                    UserManager.getInstance().setTargetPassword(getApplicationContext(), infoData.targetPassword);
                    enableAp();

                    if (infoData.apOnly == 0) {
                        //sendEmptyMessage(CHECK_IP);
                        sendEmptyMessage(GET_KEYWORDS);
                    } else {
                        // 120초마다 상태체크.
                        sendEmptyMessageDelayed(REGISTER_DEVICE, 120000);
                    }

                    Log.d(TAG, "대장 로그인 성공!");
                } else if (infoData.type == 12) {
                    // 보조 장비에 해당한다.
                    if (infoData.targetSsid == null) {
                        Log.d(TAG, "연결 정보 없음.");
                        showMessage("연결할 장비 정보가 잘못되었습니다. 20초 후 다시 시도합니다.");
                        sendEmptyMessageDelayed(REGISTER_DEVICE, 20000);
                    } else {
                        Log.d(TAG, "쫄병 와이파이 연결");
                        UserManager.getInstance().setTargetSsid(getApplicationContext(), infoData.targetSsid);
                        UserManager.getInstance().setTargetPassword(getApplicationContext(), infoData.targetPassword);
                        connectWifi();
                    }
                } else {
                    _isChecker = true;
                    sendEmptyMessage(GET_KEYWORDS);
                }
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response != 200) {
                    if (_startRetryCount < 15) {
                        ++_startRetryCount;
                        showMessage("[" + response + "] 서버에 연결할수 없습니다. 20초 후 다시 시도합니다.");
                        sendEmptyMessageDelayed(START, 20000);
                    } else {
                        rebootForError(response, code);
                    }
                } else {
                    showInputIdMessage("로그인 아이디를 입력해주세요.", "아이디가 잘못되었습니다.");
                }
            }
        }, _loginId, _imei,SystemHelper.getVersionCode(getApplicationContext()) + "",
                AppHelper.getVersionCode(getApplicationContext(), PACKAGE_NAME_PLAY_SERVICE) + "",
                AppHelper.getVersionCode(getApplicationContext(), webPackageName) + "",
                AppHelper.getVersionCode(getApplicationContext(), BuildConfig.UPDATER_PACKAGE_NAME) + "",
                Build.MODEL,
                SystemHelper.getTelecomName(this),
                SystemHelper.getBatteryRemain(this),
                SystemHelper.getBatteryHealth(this));
    }

    public void checkIp() {
        NetworkEngine.getInstance().checkIp(new NetworkEngine.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                _firstIpChanged = true;
                sendEmptyMessage(GET_KEYWORDS);
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response != 200) {
                    Log.d("Check IP","[" + response + "] 서버에 연결할수 없습니다. 20초 후 다시 시도합니다.");
                    showMessage("[" + response + "] 서버에 연결할수 없습니다. 20초 후 다시 시도합니다.");
                    sendEmptyMessageDelayed(START, 20000);
                } else if (code == 102) {
                    Log.d("Check IP","이미 사용한 아이피여서 다시 변경합니다.");
                    showMessage("이미 사용한 아이피여서 다시 변경합니다.");
                    sendNotSavedMessage(AUTO_CHANGE_IP, 0);
                } else {
                    Log.d("Check IP","실행에러(code:" + code + "). 30초후 다시 시도합니다.");
                    showMessage("실행에러(code:" + code + "). 30초후 다시 시도합니다.");
                    sendEmptyMessageDelayed(CHECK_IP, 30000);
                }
            }
        }, _loginId, _imei);
    }

    private boolean isEmptyString(String text) {
        return text == null || text.isEmpty();
    }

    public void connectWifi() {
        connectWifi(true);
    }

    public void connectWifi(boolean waiting) {
//        final String networkSSID = "AndroidHotspot4992";
//        final String networkPassword = "49924992";

        String networkSSID = UserManager.getInstance().getTargetSsid(getApplicationContext());
        String networkPassword = UserManager.getInstance().getTargetPassword(getApplicationContext());

        Log.d(TAG, "SSID: " + networkSSID + "/ PW: " + networkPassword);

        if (!isEmptyString(networkSSID) && !isEmptyString(networkPassword)) {
            // 만약 이미 대기중이면 연결 시도하지 않는다.
            if (_wifiWaiting) {
                showMessage("Wifi 네트워크 연결 대기 중...");
                return;
            }

            showMessage("Wifi 네트워크 연결 중...");
            _wifiWaiting = waiting;
            connectToWifi(networkSSID, networkPassword);
        } else {
            showMessage("Wifi 정보가 잘못되어 20초 후 다시 시도합니다.");
            sendEmptyMessageDelayed(REGISTER_DEVICE, 20000);
        }
    }

    private void connectToWifi(final String networkSSID, final String networkPassword) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        // 연결할 와이파이로 연결되지 않았다면 연결한다.
        // 이미 연결되어 있다면 연결하지 않는다.
        if ((networkSSID != null) && !WifiController.checkConnectedWifi(this, networkSSID)) {
            if (WifiController.connectToWifi(this, networkSSID, networkPassword, true)) {
                _wifiWaiting = false;
            }
        } else {
            showMessage("Wifi 가 연결되어 있어서 다음으로 진행.");
            _wifiWaiting = false;
            runNetworkConnected(NetworkReceiver.WIFI_CONNECTED);
        }
    }

    private boolean checkConnectedWifi(String networkSSID) {
        // 만약 빈값이 들어오면 정상으로 처리한다. TODO: 로직 수정필요.
        if (networkSSID == null) {
            return true;
        }

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ssid = null;

        if (wifiManager != null) {
            ssid = wifiManager.getConnectionInfo().getSSID();
        }

        Log.d(TAG, "Wifi 현재: " + ssid + " / 대상: " + networkSSID);

        if (ssid == null) {
            return false;
        }

        return ssid.contains(networkSSID);
    }


    private void runNetworkConnected(String action) {
        if (_message == null) {
            return;
        }

        if (BuildConfig.PARENT_MODE) {
            if (action.equals(NetworkReceiver.ANY_CONNECTED)) {
                touchEnabled(true);
                if (_restart) {
                    if (!_isChecker) {
                        enableAp();
                    }

                    sendEmptyMessage(END);
                    return;
                }

                if (_message._lastMessage == START) {
                    // 데이터에 연결됬다면 실행한다.
                    showMessage("데이터 네트워크 연결됨.");
                    _firstIpChanged = true;
                    onMessageStart();
                } else if (_message._lastMessage == CHECK_IP) {
                    // 연결대기중이면 연결됬을때 아이피 체크를 해준다.
                    showMessage("아이피 변경이 완료되었습니다.");
                    _firstIpChanged = true;
                    sendEmptyMessageDelayed(CHECK_IP, 1000);
                }
            } else if (action.equals(NetworkReceiver.WIFI_CONNECTED)) {
                // 와이파이가 연결된다면 무조건 꺼준다.
                Log.d(TAG, "와이파이가 켜져서 와이파이 종료.");
                WifiController.wifiEnabled(ActivityMCloud.this, false);
            } else {
                // NetworkReceiver.CONNECTION_LOST
                if (_message._lastMessage == START) {
                    // 처음부터 인터넷 연결이 되지 않는다면 다시 시작한다.
                    showMessage("인터넷 연결이 되지 않아서 다시 시작.");
                    sendEmptyMessageDelayed(START, 100);
                } else if (_message._lastMessage == CHECK_IP) {
                    // 연결대기중이면 연결됬을때 아이피 체크를 해준다.
                    Log.d(TAG, "[CHECK_IP] 인터넷 연결이 끊겨서 1초후 다시 연결.");
                    sendNotSavedMessage(AUTO_CHANGE_IP, 0);
                } else if (_message._lastMessage == PatternMessage.END_PATTERN) {
                    // 연결대기중이면 연결됬을때 아이피 체크를 해준다.
                    Log.d(TAG, "[END_PATTERN] 인터넷 연결이 끊겨서 다시 연결.");
                    changeIp();
                } else {
                    // 그외는 데이터 모드를 켜준다.
                    Log.d(TAG, "인터넷 연결이 끊겨서 1초후 다시 연결.");
                    sendNotSavedMessage(AUTO_CHANGE_IP, 1000);
                }
            }
        } else {
            if (action.equals(NetworkReceiver.WIFI_CONNECTED)) {
                touchEnabled(true);

                if (_message._lastMessage == START) {
                    // 와이파이가 연결됬을때 시작 모드면 실행한다.
                    showMessage("Wifi 네트워크 연결됨.");
                    onMessageStart();
                } else if (_message._lastMessage == REGISTER_DEVICE) {
                    // 장비 등록 모드라면, 키워드를 가져온다.
                    sendEmptyMessage(GET_KEYWORDS);
                } else if (_message._lastMessage == SET_WIFI_PROXY) {
                    showMessage("[SET_WIFI_PROXY] 연결 완료 send WORK_RUN");
                    sendEmptyMessageDelayed(WORK_RUN, 3000);
//                } else if (_message._lastMessage == GET_PROXY_IP) {
//                    touchEnabled(true);
//                    showMessage("[GET_PROXY_IP] 완료 send SET_WIFI_PROXY");
//                    sendEmptyMessageDelayed(SET_WIFI_PROXY, 100);
//                    _ipRetryCount = 0;
//                    sendEmptyMessageDelayed(CHECK_IP, 1000);
//                    showMessage("[GET_PROXY_IP] 완료 send WORK_START");
//                    sendEmptyMessageDelayed(WORK_START, 3000);

//                updateNaverIdPhone("123");
//                    getCurrentIp();
                } else {
                    // 처리중에는 다른 와이파이에 연결되면 안된다.
                    // 다른 와이파이에 연결되었다면 연결을 종료한다.
                    String networkSSID = UserManager.getInstance().getTargetSsid(getApplicationContext());

                    if (!checkConnectedWifi(networkSSID)) {
                        Log.d(TAG, "연결해야하는 wifi 가 아니라서 연결해제.");
                        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        wifiManager.disconnect();
                        wifiManager.setWifiEnabled(false);
                    }
                }
            } else {
                // 데이터 모드 체크는 의미 없으므로 하지 않는다.
                if (_message._lastMessage == START) {
                    // 시작 모드일때 끊어졌다면 와이파이만 다시 켜준다.
                    WifiController.wifiEnabled(ActivityMCloud.this, true);
                } else if(_message._lastMessage == SET_WIFI_PROXY) {
                    if (WifiController.isWifiEnabled(this)) {
                        showMessage("[SET_WIFI_PROXY] 인터넷 연결이 끊겨서 와이파이 재연결...");
//                        _networkWaiting = true;
                        WifiController.reconnect(this);
                    } else {
                        showMessage("[SET_WIFI_PROXY] 인터넷 연결이 끊겨서 와이파이 켜기...");
//                        connectWifi();
                        WifiController.wifiEnabled(this, true);
                    }
//                } else if (_message._lastMessage == GET_PROXY_IP) {
//                    if (WifiController.isWifiEnabled(this)) {
//                        showMessage("[GET_PROXY_IP] 인터넷 연결이 끊겨서 와이파이 재연결...");
//                        _networkWaiting = true;
//                        WifiController.reconnect(this);
//                    } else {
//                        showMessage("[GET_PROXY_IP] 인터넷 연결이 끊겨서 와이파이 켜기...");
//                        _networkWaiting = true;
//                        WifiController.wifiEnabled(this, true);
//                    }
                } else {
                    // 데이터 모드 혹은 연결이 끊어졌다면 와이파이 연결을 시도한다.
                    showMessage("인터넷 연결이 끊겨서 와이파이 연결시도...");
                    WifiController.wifiEnabled(this, true);
                }
            }
        }
    }

    public void 재부팅(){
        AdbController.reboot();
    }

    private void 테스트(){
//        NaverPatternMessage message;
//            String 키워드 = "강남맛집";
//            String MID = "1653280910";
//            message = new NaverPlacePatternMessage(_webViewManager, 키워드, MID);
//        _patternHandlerThread.setOnHandleMessageListener(message);
//        if (message != null) {
//            message.setHandler(_handlerThread.getHandler());
//            _patternHandlerThread.getHandler().sendEmptyMessage(PatternMessage.START_PATTERN);
//
//        }
    }

    private void initPattern() {
        if (!BuildConfig.FLAVOR_mode.contains("rank")) {
            for (int i = 0; i < _keywords.size(); ++i) {
                initPatternForItem(i);
            }
//            for (KeywordItemMoon item : _keywords) {
//                initPatternForItem(item);
//            }
        } else {
            initPatternForItem(_currentIndex);
        }
    }
    private void initPatternForItem(int index) {
        touchEnabled(true);

        // 원할한 쿠팡 처리를 위해 쿠팡을 끈다.
        AdbController.killAppThread(ActivityMCloud.PACKAGE_NAME_COUPANG);

//        if(_currentIndex == 0) {
//            _webViewManager.clearCookie();
//           // Log.d("NNB", nnb.split(":")[0]);
//            //Log.d("UA", nnb.split(":")[1]);
//           // showMessage("NNB : "+ nnb.split(":")[0]);
//            //showMessage("UA : "+ nnb.split(":")[1]);
//            //_webViewManager.setCookie(".naver.com", "NNB", nnb.split(":")[0]);
//           // mWebView.getSettings().setUserAgentString(nnb.split(":")[1]);
//        }

//        NaverPatternMessage message;
        PatternMessage message;
//                    정보.add(item.getString("uid")+","+
//                            item.getString("category")+","+
//                            item.getString("keyword") + "," +
//                            item.getString("mid1") + "," +
//                            mid2);

        KeywordItemMoon item = _keywords.get(index);

        String UID = String.valueOf(item.uid);
        String 카테고리 = item.category;

        boolean _useImage = true;

        if (_backupUa != null) {
            _webViewManager.setUserAgentString(_backupUa);
            _backupUa = null;
        }

        if (!BuildConfig.FLAVOR_mode.contains("rank")) {
            _webViewManager.setInterceptType(item.item.interceptType);

//            카테고리 = "nshop";
            if (카테고리.equals("nshop")) {
                if (item.item.isPacketPattern()) {
                    message = new NaverShopPacketPatternMessage(_webViewManager, item);
                } else {
                    int value = 1;

                    if (item.item.useRandomActive == 1 &&
                            !_didRunActive &&
                            UserManager.getInstance().nnbData != null &&
                            !TextUtils.isEmpty(UserManager.getInstance().nnbData.nidSes)) {
                        // 랜덤확률로 활동패턴 작동.
                        value = (int) MathHelper.randomRange(0, 2);
                        // test.
//                        value = 0;
                        Log.d(TAG, "패턴 선택: " + value);
                    }

                    _didRunActive = true;

                    if (value == 0) {
                        message = new NaverActivePatternMessage(_webViewManager, item);
                    } else {
                        message = new NaverShopPatternMessage(_webViewManager, item);
                    }
                }
            } else if(카테고리.equals("nshop_pc")) {
                if (item.item.isPacketPattern()) {
                    message = new NaverShopPcPacketPatternMessage(_webViewManager, item);
                } else {
                    message = new NaverShopPcPatternMessage(_webViewManager, item);
                }
            } else if(카테고리.equals("nshop_cr")) {
                // 크롬 캡쳐버전으로 작업한다. 만약에 캡쳐모드가 아닌데 여기로 온다면 우선은 일반 작업해준다.
                if (!TextUtils.isEmpty(item.item.sourceUrl) && (_captureMode == 1)) {
                    message = new NaverShopChromePatternMessage(_webViewManager, item);
                } else {
                    message = new NaverShopPatternMessage(_webViewManager, item);
                }
            } else if(카테고리.equals("nview")) {
                String 키워드 = item.keyword;
                String URL = item.url;
                URL = URL.replace("&amp;","&");
                message = new NaverViewPatternMessage(_webViewManager, item, UID,키워드, URL);
            } else if(카테고리.equals("nplace")) {
//                item.url = "https://m.coupang.com/vm/products/38922456?itemId=143140119&q=%EC%9E%90%EC%A0%84%EA%B1%B0&searchId=7c443156f5ad40d7b185b3ee3180f084"; //2위
                item.code = getCodeFromUrl(item.url);

                // 크롬 캡쳐버전으로 작업한다.
                if (!TextUtils.isEmpty(item.item.sourceUrl) && (_captureMode == 1)) {
                    message = new NaverPlaceChromePatternMessage(_webViewManager, item);
                } else {
                    message = new NaverPlacePatternMessage(_webViewManager, item);
                }
            } else if(카테고리.equals("nshop_keyword")) {
                String 키워드 = item.keyword;
                String URL = item.mid1;
                URL = URL.replace("&amp;","&");
                message = new NaverShopKeywordPatternMessage(_webViewManager,키워드, URL);
            } else if(카테고리.equals("place_save")) {
                String 키워드 = item.keyword;
                String URL = item.mid1;
                message = new NaverShopKeywordPatternMessage(_webViewManager,키워드, URL);
            } else if(카테고리.equals("nfplace")) {
                item.code = getCodeFromUrl(item.url);
                message = new NaverPlacePatternMessage(_webViewManager, item);
            } else if(카테고리.equals("site")) {
                item.url = item.url.replace("&amp;","&");
//                item.keyword = "강남성형외과";
//                item.url = "https://www.flickr.com/photos/153487669@N04/27237840678";
                message = new NaverSitePatternMessage(_webViewManager, item);
            } else if(카테고리.equals("ninflu")) {
                item.url = item.url.replace("&amp;","&");
//                item.keyword = "강남성형외과";
//                item.url = "https://www.flickr.com/photos/153487669@N04/27237840678";
                message = new NaverInfluencerPatternMessage(_webViewManager, item);
            } else if(카테고리.equals("coupang")) {
                item.url = item.url.replace("&amp;","&");
//                item.url = "https://m.coupang.com/vm/products/38922456?itemId=143140119&q=%EC%9E%90%EC%A0%84%EA%B1%B0&searchId=7c443156f5ad40d7b185b3ee3180f084"; //2위
//                item.url = "https://m.coupang.com/vm/products/1728598021?itemId=2941745483&q=%EC%9E%90%EC%A0%84%EA%B1%B0&searchId=5864e2614bac4da484c866b3454f3adc"; //1page
//                item.url = "https://m.coupang.com/vm/products/1977903544?itemId=3364519603&q=%EC%9E%90%EC%A0%84%EA%B1%B0&searchId=d61cd31dfca14880bb4ab54dff28a22c"; //2page
                item.code = getCoupangCodeFromUrl(item.url);
                message = new CoupangViewPatternMessage(_webViewManager, item);
            } else if(카테고리.equals("coupang_pc")) {
                item.url = item.url.replace("&amp;","&");
                item.code = getCoupangCodeFromUrl(item.url);

//                if (_backupUa == null) {
////                    _useImage = false;
//                    _backupUa = _webViewManager.getUserAgentString();
//                    _webViewManager.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
//                }

                message = new CoupangPcPatternMessage(_webViewManager, item);
            } else if(카테고리.equals("google")) {
                item.url = item.url.replace("&amp;","&");
                message = new GoogleViewPatternMessage(_webViewManager, item);
            } else if(카테고리.equals("ohouse")) {
//                if (item.item.isPacketPattern()) {
//                    message = new NaverShopPacketPatternMessage(_webViewManager, item);
//                } else {
                    message = new OHouseShopPatternMessage(_webViewManager, item);
//                }
            } else if(카테고리.equals("ably")) {
                message = new AblyShopPatternMessage(_webViewManager, item);
            } else if(카테고리.equals("ali")) {
                message = new AliShopPatternMessage(_webViewManager, item);
            } else if(카테고리.equals("temu")) {
                message = new AblyShopPatternMessage(_webViewManager, item);
            } else {
                message = null;
            }

            _useImage = (item.item.useImage == 1);
        } else {
            if (카테고리.equals("coupang")) {
                _useImage = false;

                if (item.url.length() == 0) {
                    item.url = item.mid1;
                }
                item.url = item.url.replace("&amp;","&");
                item.code = getCoupangCodeFromUrl(item.url);
                message = new CoupangRankPatternMessage(_webViewManager, item);
            } else if (카테고리.equals("coupang_pc")) {
                _webViewManager.setInterceptType(item.item.interceptType);

                if (item.url.length() == 0) {
                    item.url = item.mid1;
                }
                item.url = item.url.replace("&amp;","&");
                item.code = getCoupangCodeFromUrl(item.url);

                if (_backupUa == null) {
//                    _useImage = false;
                    _backupUa = _webViewManager.getUserAgentString();
//                    _webViewManager.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");
//                    _webViewManager.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");
                }
                message = new CoupangRankPatternMessage(_webViewManager, item);
            } else if (카테고리.equals("google")) {
                if (item.url.length() == 0) {
                    item.url = item.mid1;
                }
                item.url = item.url.replace("&amp;","&");
                message = new GoogleRankPatternMessage(_webViewManager, item);
            } else if (카테고리.equals("ohouse")) {
                message = new OHouseRankPatternMessage(_webViewManager, item);
            } else if(카테고리.equals("ably")) {
                message = new AblyShopPatternMessage(_webViewManager, item);
            } else if(카테고리.equals("ali")) {
                _useImage = false;
                message = new AliRankPatternMessage(_webViewManager, item);
            } else if(카테고리.equals("temu")) {
                message = new AblyShopPatternMessage(_webViewManager, item);
            } else {
                _webViewManager.setInterceptType(item.item.interceptType);
                _useImage = false;
//                item.keyword = "대구 교통사고 한의원";
//                item.url = "https://m.place.naver.com/hospital/20144695";

                if (item.url != null && (item.url.length() > 0)) {
                    item.code = getCodeFromUrl(item.url);
                }

                if (카테고리.equals("site")) {
//                    item.keyword = "그레이슈가링";
//                    item.url = "https://graysugaring.modoo.at";

                    if (item.url.length() == 0) {
                        item.url = item.mid1;
                    }
                }

                message = new NaverRankPatternMessage(_webViewManager, item);
            }
        }

        if (item.item.isPacketBoostPattern()) {
            PatternHandlerThread thread = _packetPatternHandlerThreadList.get(index);
            thread.setOnHandleMessageListener(message);

            if (message != null) {
                long delayMillis = 0;

                if (index > 0) {
                    delayMillis = MathHelper.randomRange(50, 200);
                }

                message.setPatternHandlerThread(thread);
                message.setEndHandler(_handlerThread.getHandler());
                thread.getHandler().sendEmptyMessageDelayed(PatternMessage.START_PATTERN, delayMillis);
            }
        } else {
            if (_useImage) {
                _webViewManager.setLoadsImagesAutomatically(true);
            } else {
                _webViewManager.setLoadsImagesAutomatically(false);
            }

            _patternHandlerThread.setOnHandleMessageListener(message);
            if (message != null) {
                message.setPatternHandlerThread(_patternHandlerThread);
                message.setEndHandler(_handlerThread.getHandler());
                _patternHandlerThread.getHandler().sendEmptyMessageDelayed(PatternMessage.START_PATTERN, 0);
            }
        }
    }

    public String getCodeFromUrl(String url) {
        String[] urls = url.split("\\?");
        String code = urls[0].replaceAll("[^0-9]","");

//        int findPos = url.lastIndexOf('/');
//
//        if (findPos == url.length() - 1) {
//            url = url.substring(0, findPos);
//        }
//
//        int lastPos = url.lastIndexOf('/');
//
//        if (lastPos > -1) {
//            code = url.substring(lastPos + 1);
//        }

        return code;
    }

    public String getCoupangCodeFromUrl(String url) {
//        "https://m.coupang.com/vm/products/1728598021?itemId=2941745483&q=%EC%9E%90%EC%A0%84%EA%B1%B0&searchId=af6f1f5b68f844f0bccc6a3ae3c50647"
        String[] urls = url.split("\\?");

        // from productId
        String[] urlParts = urls[0].split("/");
        String code = urlParts[urlParts.length - 1];

        // from itemId
//        String[] params = urls[1].split("&");
//        for (String part : params) {
//            if (part.startsWith("itemId=")) {
//                code = part.replace("itemId=" , "");
//            }
//        }

        return code;
    }

    private void setProxy(String host, String port) {
        setProxy(host, port, null, null);
    }

    private void setProxy(String host, String port, String username, String password) {
        String proxyIp = host + ":" + port;

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            proxyIp += ":" + username + ":" + password;
        }

        showMessage("프록시 아이피: " + proxyIp + " 가져오기 완료.");
        UserManager.getInstance().webProxy = proxyIp;

//        _networkWaiting = true;
        _runProxy = true;
        setProxyToWebview(host, port, username, password);
        // 와이파이 자체에 프록시 설정.
//        UserManager.getInstance().setProxy(this, proxyIp);
//        sendEmptyMessageDelayed(SET_WIFI_PROXY, 10);
    }

    private void setProxyToWebview(String host, String port, String username, String password) {
        String proxy = UserManager.getInstance().webProxy;
        showMessage("프록시 연결: " + proxy);

        _webViewManager.goBlankPage();
        _webViewManager.setProxy(host, port);

        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            _webViewManager.setProxyInfo(username, password);
        }

//        _webViewManager.loadUrl("http://ipinfo.io");
//        _webViewManager.loadUrl("https://api.myip.la/en?json");
        sendEmptyMessageDelayed(WORK_RUN, 100);
    }

    private void setWifiProxy() {
        if (isProxyChanged()) {
            String proxy = UserManager.getInstance().getProxy(this);

            if ((proxy == null) || proxy.length() < 2) {
                sendEmptyMessageDelayed(WORK_RUN, 10);
            } else {
                String[] proxyParts = proxy.split(":", 2);
                // 프록시 정보가 있다면 프록시를 연결한다.
                showMessage("프록시 연결: " + proxy);
                WifiProxyController.setWifiProxy(ActivityMCloud.this, proxyParts[0], Integer.parseInt(proxyParts[1]));
            }
        } else {
            Log.d(TAG, "프록시가 변경되지 않아 바로 진행");
            sendEmptyMessageDelayed(WORK_RUN, 10);
        }
    }

    private boolean isProxyChanged() {
        String currentProxy = WifiProxyController.getWifiProxy(this);
        String newProxy = UserManager.getInstance().getProxy(this);

        return !newProxy.equalsIgnoreCase(currentProxy);
    }

    public void getProxyIp() {
        if (_deviceInfo.proxy != null) {
            if (_deviceInfo.proxy.serviceId == 2) {
                if (_deviceInfo.proxy.key != null) {
                    // proxy.am
                    getLunaProxyIp(_deviceInfo.proxy.key, _deviceInfo.proxy.options);
                } else {
                    showMessage("API 주소 정보가 없습니다. 서버 관리자에 문의 바랍니다. 30초 후 다시 시작합니다.");
                    sendEmptyMessageDelayed(START, 30000);
                }
            } else if (_deviceInfo.proxy.serviceId == 3) {
                setProxy4Free(_deviceInfo.proxy.options);
            } else if (_deviceInfo.proxy.serviceId == 4) {
                setIp2World(_deviceInfo.proxy.options);
            } else {
                getPyProxyIp(_deviceInfo.proxy.options);
            }
        } else {
            showMessage("Proxy 정보가 없습니다. 서버 관리자에 문의 바랍니다. 30초 후 다시 시작합니다.");
            sendEmptyMessageDelayed(START, 30000);
        }
    }

    public void getPyProxyIp(String options) {
//        showMessage("아이피를 갱신합니다");
        Log.d(TAG, "Proxy(PYPROXY) 아이피를 가져옵니다.");
        PyProxyApi.getInstance().getProxyIp(options, new PyProxyApi.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                PyProxyData amData = (PyProxyData) data;

                if (amData.code == 0) {
                    int pickIndex = (int) MathHelper.randomRange(0, amData.data.size() - 1);
                    PyProxyData.Proxy proxy = amData.data.get(pickIndex);
                    setProxy(proxy.ip, "" + proxy.port);
                } else {
                    showMessage("getPyProxyIp: 프록시를 요청하지 못했습니다. 15초 후 다시 시도합니다.");
                    sendEmptyMessageDelayed(GET_PROXY_IP, 15000);
                }
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response != 200) {
                    showMessage("getPyProxyIp: 서버에 연결할수 없습니다. 30초 후 다시 시도합니다.");
                    sendEmptyMessageDelayed(GET_PROXY_IP, 30000);
                } else {
                    showMessage("getPyProxyIp: 알 수 없는 에러(" + code + "). 앱을 다시 실행합니다.");
                    sendEmptyMessageDelayed(END, 10);
//                    autoFinish();
                }
            }
        });
    }

    public void getLunaProxyIp(String key, String options) {
//        showMessage("아이피를 갱신합니다");
        Log.d(TAG, "Proxy(LunaProxy) 아이피를 가져옵니다.");
        LunaProxyApi.getInstance().getProxyIp(key, options, new LunaProxyApi.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                LunaProxyData amData = (LunaProxyData) data;

                if (amData.code == 0) {
                    int pickIndex = (int) MathHelper.randomRange(0, amData.data.size() - 1);
                    LunaProxyData.Proxy proxy = amData.data.get(pickIndex);
                    setProxy(proxy.ip, proxy.port);
                } else {
                    showMessage("getLunaProxyIp: 프록시를 요청하지 못했습니다. 15초 후 다시 시도합니다.");
                    sendEmptyMessageDelayed(GET_PROXY_IP, 15000);
                }
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response != 200) {
                    showMessage("getLunaProxyIp: 서버에 연결할수 없습니다. 30초 후 다시 시도합니다.");
                    sendEmptyMessageDelayed(GET_PROXY_IP, 30000);
                } else {
                    showMessage("getLunaProxyIp: 알 수 없는 에러(" + code + "). 앱을 다시 실행합니다.");
                    sendEmptyMessageDelayed(END, 10);
//                    autoFinish();
                }
            }
        });
    }

    public void setProxy4Free(String options) {
        Log.d(TAG, "Proxy(Proxy4Free) 를 설정합니다.");
        Proxy4FreeData proxyData = Proxy4FreeApi.getInstance().getProxyData(options);
        setProxy(proxyData.host, proxyData.port, proxyData.id, proxyData.pw);
    }

    public void setIp2World(String options) {
        Log.d(TAG, "Proxy(Ip2World) 를 설정합니다.");
        ProxyUserAuthData proxyData = Ip2WorldApi.getInstance().getProxyData(options);
        setProxy(proxyData.host, proxyData.port, proxyData.id, proxyData.pw);
    }

    public void getProxyAmIp(String key, String options) {
//        showMessage("아이피를 갱신합니다");
        Log.d(TAG, "Proxy(proxy.am) 아이피를 가져옵니다.");
        ProxyAmApi.getInstance().getProxyIp(key, options, new ProxyAmApi.Callback() {
            @Override
            public <T> void finishSuccess(T data) {
                ProxyAmData amData = (ProxyAmData) data;
                int pickIndex = (int) MathHelper.randomRange(0, amData.data.size() - 1);
                ProxyAmData.Proxy proxy = amData.data.get(pickIndex);

                showMessage("아이피(외부: " + proxy.ip + ") 가져오기 완료.");
//                _networkWaiting = true;
                UserManager.getInstance().setProxy(ActivityMCloud.this, proxy.ip);
                sendEmptyMessageDelayed(SET_WIFI_PROXY, 10);
            }

            @Override
            public void finishFailed(int response, int code, String message) {
                if (response != 200) {
                    showMessage("getProxyAmIp: 서버에 연결할수 없습니다. 30초 후 다시 시도합니다.");
                    sendEmptyMessageDelayed(GET_PROXY_IP, 30000);
                } else {
                    showMessage("getProxyAmIp: 알 수 없는 에러(" + code + "). 앱을 다시 실행합니다.");
                    sendEmptyMessageDelayed(END, 10);
//                    autoFinish();
                }
            }
        });
    }

    public void restart() {
//        if (BuildConfig.FLAVOR_mode.contains("rank")) {
////                            long remainSec = getRemainNextHourSeconds();
//            long remainSec = 5;
////                            remainSec = 5; // test
//            showMessage("다음 순위 체크를 위해 " + remainSec + "초 대기");
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    ProcessPhoenix.triggerRebirth(ActivityMCloud.this);
//                }
//            }, remainSec * 1000);
//        } else {
//            ProcessPhoenix.triggerRebirth(ActivityMCloud.this);
//        }

        ProcessPhoenix.triggerRebirth(ActivityMCloud.this);
    }

    public void finishPattern(PatternHandlerThread patternHandlerThread) {
        Log.d(TAG, "종료 페턴");
        if (patternHandlerThread.getOnHandleMessageListener() instanceof UpdaterUpdatePatternMessage) {
            Log.d(TAG, "업데이터 업데이트 확인 완료.");
            UpdaterUpdatePatternMessage message = (UpdaterUpdatePatternMessage) patternHandlerThread.getOnHandleMessageListener();

            if (message.isUpdated()) {
                String packageName = ActivityMCloud.PACKAGE_NAME_SYSTEM_UPDATER;

                if (BuildConfig.FLAVOR_mode.equals("child")) {
                } else if (BuildConfig.FLAVOR_mode.equals("rank")) {
                    packageName = ActivityMCloud.PACKAGE_NAME_SYSTEM_UPDATER_RANK;
                } else {
                }

                // 업데이트 시간 초기화.
                UserManager.getInstance().clearUpdateCheckTime(this);
                AdbController.startPackage(this, packageName);
                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        재부팅();
                    }
                }, 30000);
            } else {
                checkUpdate(2);
            }
        } else if (patternHandlerThread.getOnHandleMessageListener() instanceof PlayServiceUpdatePatternMessage) {
            Log.d(TAG, "플레이서비스 업데이트 확인 완료.");

            // 순위체크기가 아니거나, s7 이상만 쿠팡 설치를 진행한다.
            if (BuildConfig.FLAVOR_mode.contains("rank") || DeviceController.isS7Under()) {
                checkUpdate(4);
            } else {
                checkUpdate(3);
            }
        } else if (patternHandlerThread.getOnHandleMessageListener() instanceof CoupangUpdatePatternMessage) {
            Log.d(TAG, "쿠팡 업데이트 확인 완료.");
            checkUpdate(4);
        } else if (patternHandlerThread.getOnHandleMessageListener() instanceof WebviewUpdatePatternMessage) {
            Log.d(TAG, "웹뷰 업데이트 확인 완료.");
            WebviewUpdatePatternMessage message = (WebviewUpdatePatternMessage) patternHandlerThread.getOnHandleMessageListener();

            if (message.isUpdated()) {
                // 업데이트 시간 초기화.
                UserManager.getInstance().clearUpdateCheckTime(this);
                ProcessPhoenix.triggerRebirth(this);
            } else {
                getImei();
            }
        } else if (patternHandlerThread.getOnHandleMessageListener() instanceof NaverSetCookiePatternMessage) {
            NaverSetCookiePatternMessage message = (NaverSetCookiePatternMessage) patternHandlerThread.getOnHandleMessageListener();
            KeywordItemMoon itemMoon = _keywords.get(_currentIndex);
            KeywordItem item = itemMoon.item;
            if (item.isPacketBoostPattern()) {
                --_cookieWorkCount;

                if (_cookieWorkCount > 0) {
                    Log.d(TAG, "쿠키 작업 후 다시 시도: " + _cookieWorkCount);
                    // 반복작업이면 다시 쿠키 변경한다.
                    sendEmptyMessageDelayed(CHANGE_DATA, (item.lowDelay > 0) ? 50 : 1000);
                } else {
                    Log.d(TAG, "쿠키 작업 후 작업 종료");

                    synchronized (this) {
                        if (_threadRunCount > 1) {
                            --_threadRunCount;
                            Log.d(TAG, "다른 쓰레드 작업이 남아 있어서 중단.");
                            return;
                        }
                    }

                    endWork();
                }
            } else {
                if (item.useNid == 1) {
                    // 아이디가 반드시 사용되면 쿠키를 꼭 바꿔야 한다.
                    if (!message.isChanged()) {
                        if (_cookieRetryCount < 5) {
                            ++_cookieRetryCount;
                            Log.d(TAG, "쿠키 다시 변경: " + _cookieRetryCount);
                            sendEmptyMessageDelayed(CHANGE_DATA, 3000);
                        } else {
                            message.getResultAction().item = itemMoon;
                            message.registerResultFinish(100);
                            endWork();
                        }
                        return;
                    }
                }

                Log.d(TAG, "쿠키 변경후 작업시작.");
                Log.d(TAG, "UA: " + _webViewManager.getUserAgentString());
                sendEmptyMessageDelayed(WORK_RUN, (item.lowDelay > 0) ? 50 : 1000);
            }
        } else if (patternHandlerThread.getOnHandleMessageListener() instanceof UaChangePatternMessage) {
            Log.d(TAG, "UA 변경후 작업시작.");
            Log.d(TAG, "UA: " + _webViewManager.getUserAgentString());
            KeywordItem item = _keywords.get(_currentIndex).item;

            if (_targetWeb == WEB_NAVER) {
                int workCount = _keywords.size();

                if (item.isPacketBoostPattern()) {
                    sendEmptyMessageDelayed(WORK_RUN, (item.lowDelay > 0) ? 50 : 1000);

                    synchronized (this) {
                        _threadRunCount = workCount + 1;
                    }
                } else {
                    synchronized (this) {
                        _threadRunCount = 1;
                    }
                }

                _cookieWorkCount = workCount;
                sendEmptyMessageDelayed(CHANGE_DATA, (item.lowDelay > 0) ? 50 : 1000);
            } else {
                sendEmptyMessageDelayed(WORK_RUN, (item.lowDelay > 0) ? 50 : 1000);
            }
        } else if (patternHandlerThread.getOnHandleMessageListener() instanceof NaverActivePatternMessage) {
            Log.d(TAG, "네이버 활동 패턴 완료.");
            sendEmptyMessageDelayed(WORK_RUN, MathHelper.randomRange(100, 2000));
        } else {
            if (patternHandlerThread.getOnHandleMessageListener() instanceof NaverShopPatternMessage) {
                NaverShopPatternMessage message = (NaverShopPatternMessage) patternHandlerThread.getOnHandleMessageListener();

                if (message.needIpChange) {
                    _restart = true;
                    changeIp(true);
                    return;
                }
            }

            synchronized (this) {
                if (_threadRunCount > 1) {
                    --_threadRunCount;
                    Log.d(TAG, "다른 쓰레드 작업이 남아 있어서 중단.");
                    return;
                }
            }

            endWork();
        }
    }

    private void endWork() {
        ++_currentIndex;

        int processCount = UserManager.getInstance().getProcessCount(this) + (_currentIndex + 1);
        UserManager.getInstance().setProcessCount(this, processCount);

        // 단일쓰레드 여러건 처리는 체크기에서만 한다.
        if (BuildConfig.FLAVOR_mode.contains("rank") && _currentIndex < _keywords.size()) {
            String message = "다음처리 실행.. " + (_currentIndex + 1) + "/" + _keywords.size() + " (uid: " + _keywords.get(_currentIndex).uid + ")";
            int rate = 30;
            int delay = 5000;

            if (BuildConfig.FLAVOR_mode.contains("rank")) {
                rate = 30;
                delay = 2000;
                _webViewManager.clearCookie();
            }

            // 30번 마다 아이피를 변경해준다.
            if (_currentIndex % rate == 0) {
                UserManager.getInstance().setProcessCount(this, 0);
                UserManager.getInstance().setChangeIp(this, true);
                message += "\n아이피 변경을 위해 재시작...";
                showMessage(message);
                sendEmptyMessage(END);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                            _restart = true;
//                            changeIp();
                        sendEmptyMessage(END);
                    }
                }, 500);
            } else {
                showMessage(message);
                sendEmptyMessageDelayed(WORK_RUN, delay);
            }
        } else {
            Date lastCheckDate = UserManager.getInstance().getIpChangeTimeDate(this);
            boolean changeIp = false;

            if (lastCheckDate == null) {
                UserManager.getInstance().setIpChangeTime(this);
            } else {
                long now = System.currentTimeMillis();

                // 아이피는 4분 30초 단위로 변경한다.
                if (now - lastCheckDate.getTime() > 4.5 * 60 * 1000) {
                    changeIp = true;
                }
            }

//                int maxCount = 3;
//
//                if (processCount >= maxCount) {
            if (changeIp) {
                UserManager.getInstance().setProcessCount(this, 0);
                UserManager.getInstance().setChangeIp(this, true);
                showMessage("아이피 변경을 위해 재시작...");
            } else {
                showMessage("재시작...");
            }

            Handler handler2 = new Handler();
            handler2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendEmptyMessage(END);
                }
            }, 500);
        }
    }

    public long getRemainNextHourSeconds() {
        Date curDate = new Date();
        Date nextHour = null;

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH");
        
        try {
            String temp = f.format(curDate);
            nextHour = f.parse(f.format(curDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(nextHour);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        nextHour = cal.getTime();

        long diff = nextHour.getTime() - curDate.getTime();
        long sec = diff / 1000;

        return sec;
    }

    private void checkUpdate(UpdatePatternMessage message, String packageName) {
        _patternHandlerThread.setOnHandleMessageListener(message);

        if (message != null) {
            message.setPatternHandlerThread(_patternHandlerThread);
            message.setEndHandler(_handlerThread.getHandler());
            _patternHandlerThread.getHandler().sendEmptyMessageDelayed(PatternMessage.START_PATTERN, 100);
        } else {
            // 설정된 메시지가 없다면 종료처리한다.
            sendEmptyMessage(WORK_END);
        }
    }

    private void checkUpdate(int type) {
        if (type == 1) {
            String packageName = ActivityMCloud.PACKAGE_NAME_SYSTEM_UPDATER;

            if (BuildConfig.FLAVOR_mode.equals("child")) {
                showMessage("[업데이터 - 쫄병] 업데이트 검사...");
            } else if (BuildConfig.FLAVOR_mode.equals("rank")) {
                packageName = ActivityMCloud.PACKAGE_NAME_SYSTEM_UPDATER_RANK;
                showMessage("[업데이터 - 순위] 업데이트 검사...");
            } else {
                showMessage("[업데이터 - 대장] 업데이트 검사...");
            }

            checkUpdate(new UpdaterUpdatePatternMessage(this), packageName);
        } else if (type == 2) {
            showMessage("플레이서비스 업데이트 검사...");
            checkUpdate(new PlayServiceUpdatePatternMessage(this), PACKAGE_NAME_PLAY_SERVICE);
        } else if (type == 3) {
            showMessage("쿠팡 업데이트 검사...");
            checkUpdate(new CoupangUpdatePatternMessage(this), PACKAGE_NAME_COUPANG);
        } else if (type == 4) {
            showMessage("웹뷰 업데이트 검사...");
            checkUpdate(new WebviewUpdatePatternMessage(this), PACKAGE_NAME_SYSTEM_WEBVIEW);
        } else {
        }
    }

    private void changeData() {
        KeywordItemMoon item = _keywords.get(_currentIndex);

        NaverSetCookiePatternMessage message = new NaverSetCookiePatternMessage(_webViewManager);
        message.isPc = _isPc;
        message.deleteLocalStorage = _webViewManager.isDeleteLocalStorage();
        message.item = item;
        _patternHandlerThread.setOnHandleMessageListener(message);

        if (message != null) {
            message.setPatternHandlerThread(_patternHandlerThread);
            message.setEndHandler(_handlerThread.getHandler());

            // 브라우저 종료하고 정상 종료후 쿠키 삭제.
            _webViewManager.stopLoading();
            _webViewManager.clearCookie();
            _patternHandlerThread.getHandler().sendEmptyMessageDelayed(PatternMessage.START_PATTERN, (item.item.lowDelay > 0) ? 50 : 3000);
        } else {
            sendEmptyMessage(WORK_END);
        }
    }

    private void changeUa() {
        UaChangePatternMessage message = new UaChangePatternMessage(_webViewManager);
        message.isPc = _isPc;
        _patternHandlerThread.setOnHandleMessageListener(message);

        if (message != null) {
            message.setPatternHandlerThread(_patternHandlerThread);
            message.setEndHandler(_handlerThread.getHandler());

            // 브라우저 종료하고 정상 종료후 쿠키 삭제.
            _webViewManager.stopLoading();
            _webViewManager.clearCookie();
            _patternHandlerThread.getHandler().sendEmptyMessageDelayed(PatternMessage.START_PATTERN, (_keywords.get(_currentIndex).item.lowDelay > 0) ? 50 : 3000);
        } else {
            sendEmptyMessage(WORK_END);
        }
    }

    //핫스팟 체크 후 연결
    public void enableAp() {
        Log.d(TAG, "Wifi ap on check.");
        if (!WifiApController.isEnabled(this)) {
            Log.d(TAG, "Wifi ap on!");
            WifiApController.setEnabled(this, true);
        }
    }

    public void disableAp() {
        Log.d(TAG, "Wifi ap on check.");
        if (WifiApController.isEnabled(this)) {
            Log.d(TAG, "Wifi ap off!");
            WifiApController.setEnabled(this, false);
        }
    }

    public void 슬롯불러오기() {
        // 쓰레드 중복 처리방지.
        // 근본적으로 해결해야하지만 임시적으로 해결한다.
        if (_isGetting || _keywords.size() > 0) {
            Log.d(TAG, "이미 슬롯을 불러오는 중이거나 불러온 슬롯이 있으므로 패스");
            return;
        }

        _isGetting = true;

        if (BuildConfig.PARENT_MODE) {
            if (!_isChecker) {
                enableAp();
            } else {
                disableAp();
            }
        }
        showMessage("슬롯을 불러오는 중...");

        if (BuildConfig.FLAVOR_mode.contains("rank")) {
            NetworkEngine.getInstance().getKeywordsForRankCheck(new NetworkEngine.Callback() {
                @Override
                public <T extends Object> void finishSuccess(T data) {
                    processKeywordData((KeywordData) data);
                    _isGetting = false;
                }

                @Override
                public void finishFailed(int response, int code, String message) {
                    if (_keywordRetryCount < 5 && response != 200) {
                        ++_keywordRetryCount;
                        showMessage("[" + response + "] 서버에 연결할수 없습니다. 60초 후 다시 시도합니다.");
                        sendEmptyMessageDelayed(START, 60000);
                    } else {
                        processError(response, code, message, REGISTER_DEVICE);
                    }
                    _isGetting = false;
                }
            }, UserManager.getInstance().getLoginId(this), _imei);
        } else {
            NetworkEngine.getInstance().getKeywords(new NetworkEngine.Callback() {
                @Override
                public <T extends Object> void finishSuccess(T data) {
                    processKeywordData((KeywordData) data);
                    _isGetting = false;
                }

                @Override
                public void finishFailed(int response, int code, String message) {
                    if (_keywordRetryCount < 10 && response != 200) {
                        ++_keywordRetryCount;
                        showMessage("[" + response + "] 서버에 연결할수 없습니다. 20초 후 다시 시도합니다.");
                        sendEmptyMessageDelayed(START, 20000);
                    } else {
                        processError(response, code, message, REGISTER_DEVICE);
                    }
                    _isGetting = false;
                }
            }, UserManager.getInstance().getLoginId(this), _imei, UserManager.getInstance().uaId);
        }
    }

    private void processKeywordData(KeywordData keywordData) {
        _keywordRetryCount = 0;
        UserManager.getInstance().setKeywordEmptyCount(this, 0);

//        _retryCount = 0;
//        KeywordData keywordData = (KeywordData) data;
//        _keyword = keywordData;
//        _currentKeywordIndex = 0;
//
//        Log.d(TAG, "ipTime: " + _keyword.ipTime + ", ipUsed: " + _keyword.ipUsed);
//
//        // Test
////                _keyword.ipTime = 10;
////                _keyword.ipUsed = 0;
////                _keyword.proxyIp = "";
//
//        UserManager.getInstance().setIpUsingMin(MainActivity.this, _keyword.ipTime);
//
//        // 아이피가 사용중이면 아이피를 변경한다.
//        if (!_wifiMode && _keyword.ipUsed == 1) {
//            showMessage("아이피가 사용중이어서 아이피 변경을 시도합니다.");
//            sendNotSavedMessage(TOGGLE_DATA_NETWORK, 1000);
//            return;
//        }
//
//        // 서버실수 방지용 예외처리.
//        if ((_keyword.data == null) ||
//                (_keyword.data.isEmpty())) {
//            showMessage("처리할 키워드가 없습니다(code:1). 20초후 다시 시도합니다.");
//            sendEmptyMessageDelayed(GET_KEYWORDS, 20000);
//        } else {
//            if (_keyword.useSmsAuth) {
//                runKeyword();
//            } else {
//                sendNotSavedMessage(CHECK_USER_STATUS, 0);
//            }
//        }

        // Test..
//            if (true) {
//                _targetWeb = WEB_GOOGLE;
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "nview";
////                keywordItem.keyword = "분당임플란트";
////                keywordItem.mid1 = "https://m.blog.naver.com/s17135/220974848478";
////                _keywords.add(keywordItem);
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "site";
////                keywordItem.keyword = "bs블라인드";
//////                keywordItem.url = "https://inflow.pay.naver.com/rd?tr=ds&retUrl=https%3A%2F%2Fm.smartstore.naver.com";
////                keywordItem.url = "https://inflow.pay.naver.com/rd?tr=ds&retUrl=https%3A%2F%2Fm.smartstore.naver.com%2Fbsblind&pType=M&no=&vcode=5nr37hTMbR0M2dOdVUu0tawrqK5gKr3O9B3PLWHPu0nRtiZLcE7yRZCiKFTDQh4od6lromH6DXPxTEoaWGg0xra4Oj";
//////                keywordItem.url = "https://blog.naver.com/pritia1004/222253997779";
//////                keywordItem.url = "https://blog.naver.com/maeil0824/222249612345";
////                _keywords.add(keywordItem);
////
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "coupang";
////                keywordItem.keyword = "식용 아르간오일 250";
////                keywordItem.url = "https://m.coupang.com/vp/products/11340999588?vendorItemId=72357606721&isAddedCart=";
////                _keywords.add(keywordItem);
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "coupang";
////                keywordItem.keyword = "이어팟";
////                keywordItem.url = "http://m.coupang.com/vp/products/241954414";
////                _keywords.add(keywordItem);
////
////                _targetWeb = WEB_COUPANG;
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "place_save";
////                keywordItem.keyword = "안산중고폰";
////                keywordItem.mid1 = "https://m.cafe.naver.com/lovelove20081207/3534";
////                _keywords.add(keywordItem);
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "nshop";
////                keywordItem.keyword = "조깅화";
////                keywordItem.mid1 = "82076253940";
////                keywordItem.mid2 = ".";
////                _keywords.add(keywordItem);
//
//                KeywordItemMoon keywordItem =  new KeywordItemMoon();
//                keywordItem.uid = 999999;
//                keywordItem.category = "nshop";
//                keywordItem.keyword = "물광쿠션";
//                keywordItem.mid1 = "83129072786";
//                keywordItem.mid2 = ".";
//                _keywords.add(keywordItem);
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "nshop";
////                keywordItem.keyword = "차량 공기청정기";
//////                keywordItem.mid1 = "82971372758";
////                keywordItem.mid1 = "24046205986";
////                keywordItem.mid2 = "82473172827";
////                _keywords.add(keywordItem);
////                _changeUa = true;
////                _targetWeb = WEB_NAVER;
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "nview";
////                keywordItem.keyword = "무타공 벽걸이tv";
////                keywordItem.mid1 = "https://m.blog.naver.com/bbabac234/222288404859";
////                _keywords.add(keywordItem);
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "ninflu";
////                keywordItem.keyword = "매니큐어";
////                keywordItem.url = "https://in.naver.com/seia/contents/254734543213088?query=%EB%A7%A4%EB%8B%88%ED%81%90%EC%96%B4";
//////                keywordItem.url = "https://in.naver.com/withgr_sh/contents/246532133669376?query=%EB%A7%A4%EB%8B%88%ED%81%90%EC%96%B4";
////                _keywords.add(keywordItem);
//
//                showMessage( "키워드 " + _keywords.size() + "개 수신 완료.");
//                sendEmptyMessageDelayed(WORK_START, 5000);
//                return;
//            }

        int naverCount = 0;
        _targetWeb = WEB_DEFAULT;
        _changeUa = false;

        for (int i = 0; i < keywordData.data.size(); i++) {
            KeywordItem item = keywordData.data.get(i);
            KeywordItemMoon keywordItem =  new KeywordItemMoon();
            keywordItem.item = item;

            String mid2 = "";
            if (item.code2 != null) {
                mid2 = item.code2;
            }

            if (mid2.equals("")) {
                mid2 = ".";
            }

            keywordItem.uid = item.trafficId;
            if (item.serviceType == 1) {
                keywordItem.category = "nview";
            } else if (item.serviceType == 4) {
                keywordItem.category = "nplace";
            } else if ((item.serviceType == 6) ||
                    (item.serviceType == 10) ||
                    (item.serviceType == 14) ||
                    (item.serviceType == 16) ||
                    (item.serviceType == 17) ||
                    (item.serviceType == 23) ||
                    (item.serviceType == 27)) {
                keywordItem.category = "nshop";
            } else if ((item.serviceType == 7) ||
                    (item.serviceType == 12) ||
                    (item.serviceType == 13) ||
                    (item.serviceType == 15) ||
                    (item.serviceType == 18) ||
                    (item.serviceType == 20) ||
                    (item.serviceType == 24) ||
                    (item.serviceType == 26) ||
                    (item.serviceType == 28)) {
                keywordItem.category = "nshop_pc";
            } else if ((item.serviceType == 11) ||
                    (item.serviceType == 19) ||
                    (item.serviceType == 25)) {
                keywordItem.category = "nshop_cr";
            } else if ((item.serviceType == 8) ||
                    (item.serviceType == 21)) {
                keywordItem.category = "coupang";
            } else if ((item.serviceType == 9) ||
                    (item.serviceType == 22)) {
                keywordItem.category = "coupang_pc";
            } else if (item.serviceType == 32) {
                keywordItem.category = "ohouse";
            } else if (item.serviceType == 33) {
                keywordItem.category = "ohouse_pc";
            } else if (item.serviceType == 34) {
                keywordItem.category = "ably";
            } else if (item.serviceType == 35) {
                keywordItem.category = "ably_pc";
            } else if (item.serviceType == 36) {
                keywordItem.category = "ali";
            } else if (item.serviceType == 37) {
                keywordItem.category = "ali_pc";
            } else if (item.serviceType == 38) {
                keywordItem.category = "temu";
            } else if (item.serviceType == 39) {
                keywordItem.category = "temu_pc";
            } else {
                continue;
            }

            keywordItem.keyword = item.search;
            keywordItem.mid1 = item.code;
            keywordItem.mid2 = mid2;
            keywordItem.url = item.url;
            keywordItem.webTarget = item.webTarget;
            keywordItem.uaChange = item.uaChange;
            keywordItem.shopHome = item.shopHome;
//                    keywordItem.agency = item.optString("agency");
//                    keywordItem.account = item.optString("account");
//                keywordItem.code = item.optString("account");

            if (BuildConfig.FLAVOR_mode.contains("rank")) {
                _keywords.add(keywordItem);
                _changeUa = (keywordItem.uaChange == 1);

                if (keywordItem.category.equals("nshop")) {
                } else if (keywordItem.category.equals("coupang_pc")) {
                    _isPc = true;
                }
//                    break;
            } else {
//                        keywordItem.pcmobile = item.getString("pcmobile");

//                        if (item.getString("pcmobile").equals("mobile")) {
                _keywords.add(keywordItem);
//                    정보.add(item.getSt ring("uid")+","+
//                            item.getString("category")+","+
//                            item.getString("keyword") + "," +
//                            item.getString("mid1") + "," +
//                            mid2);
//                        }

                _changeUa = (keywordItem.uaChange == 1);
                _targetWeb = keywordItem.webTarget;

                if (keywordItem.category.equals("nshop")) {
//                    _changeUa = true;
//                    ++naverCount;
                } else if (keywordItem.category.equals("nshop_pc")) {
//                    _changeUa = true;
                    _isPc = true;
//                    ++naverCount;
                } else if (keywordItem.category.equals("nshop_cr")) {
//                    _changeUa = true;
//                    ++naverCount;
                } else if (keywordItem.category.equals("nview")) {
//                    _changeUa = true;
//                    ++naverCount;
                } else if (keywordItem.category.equals("nplace")) {
//                    _changeUa = true;
//                    ++naverCount;
                } else if (keywordItem.category.equals("nshop_keyword")) {
                    ++naverCount;
                } else if (keywordItem.category.equals("place_save")) {
                    ++naverCount;
                } else if (keywordItem.category.equals("nfplace")) {
                    ++naverCount;
                } else if (keywordItem.category.equals("site")) {
                    ++naverCount;
                } else if (keywordItem.category.equals("coupang")) {
//                    _targetWeb = WEB_COUPANG;
                } else if (keywordItem.category.equals("coupang_pc")) {
//                    _changeUa = true;
                    _isPc = true;
//                    _targetWeb = WEB_COUPANG;
                } else if (keywordItem.category.equals("google")) {
                } else if (keywordItem.category.equals("ohouse")) {
                } else {
                }
            }
        }

//        if (naverCount > 0) {
//            // 원래 취지는 이게 아니지만 작중하나라도 네이버작업이 있다면
//            // 네이버 쿠키를 위해서 기본을 네이버 모드로 해준다.
//            _targetWeb = WEB_NAVER;
//        } else {
//        }

        // test
        if (BuildConfig.FLAVOR_mode.contains("rank")) {
//                keywords.clear();

//                KeywordItemMoon keywordItem =  new KeywordItemMoon();
//                keywordItem.uid = 49;
//                keywordItem.category = "nshop";
//                keywordItem.keyword = "열무물김치";
//                keywordItem.mid1 = "22341077979";
//                keywordItem.mid2 = "";
//                keywords.add(keywordItem);

//                KeywordItemMoon keywordItem =  new KeywordItemMoon();
//                keywordItem.uid = 50;
//                keywordItem.category = "nshop";
//                keywordItem.keyword = "딤채김치냉장고";
//                keywordItem.mid1 = "20932928616";
//                keywordItem.mid2 = "21778440881";
//                keywords.add(keywordItem);

//                KeywordItemMoon keywordItem =  new KeywordItemMoon();
//                keywordItem.uid = 51;
//                keywordItem.category = "nshop";
//                keywordItem.keyword = "삼성김치냉장고";
//                keywordItem.mid1 = "20999657412";
//                keywordItem.mid2 = "82969646527";
//                _keywords.add(keywordItem);

//                KeywordItemMoon keywordItem =  new KeywordItemMoon();
//                keywordItem.uid = 215;
//                keywordItem.category = "nplace";
//                keywordItem.keyword = "종각 맛집";
//                keywordItem.mid1 = "촌놈숯불닭갈비 종로점";
//                keywordItem.mid2 = "서울 종로구 삼일대로19길 6 1층";
//                keywordItem.url = "https://m.place.naver.com/restaurant/1588867567/home?entry=ple";
//                keywordItem.code = "1588867567";
//                keywords.add(keywordItem);

//                KeywordItemMoon keywordItem =  new KeywordItemMoon();
//                keywordItem.uid = 216;
//                keywordItem.category = "nplace";
//                keywordItem.keyword = "부산 연산동 맛집";
//                keywordItem.mid1 = "마포통구이";
//                keywordItem.mid2 = "부산 연제구 쌍미천로151번길 40 1층";
//                keywordItem.url = "https://m.place.naver.com/restaurant/1702472176/home?entry=ple";
//                keywordItem.code = "1702472176";
//                keywords.add(keywordItem);
        }

        if (_keywords.size() > 0) {
                /*
                if(new Random().nextInt(10) >= 0) {
                    로그인쿠키 = true;
                }*/
                /*
                if(BuildConfig.PARENT_MODE) {
                    Log.d(TAG, "아이피 변경!");
                    changeIp();
                }
                 */
            showMessage( "키워드 " + _keywords.size() + "개 수신 완료.");
            sendEmptyMessageDelayed(WORK_START, 100);
        } else {
            showMessage("처리할 키워드가 없습니다. 40초후 다시 시도합니다.");
            sendEmptyMessageDelayed(GET_KEYWORDS, 40000);
        }
    }

    private void reboot() {
        UserManager.getInstance().setBootTime(this);
        AdbController.reboot();
    }

    private void rebootForError(int response, int code) {
        showMessage("연결에러(" + response + ", code:" + code + "). 5초후 재부팅.");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reboot();
            }
        }, 5000);
    }

    private void processError(int response, int code, String errorMessage, int retryMessage) {
        int baseDelaySec = 12;
        int count = UserManager.getInstance().getKeywordEmptyCount(this);

        if (count > 0) {
            long now = System.currentTimeMillis();
            Date mDate = new Date(now);
            SimpleDateFormat simpleDate = new SimpleDateFormat("H");
            String getTime = simpleDate.format(mDate);
            int currentHour = Integer.parseInt(getTime);

            if (currentHour >= 21) {
                baseDelaySec = 17;
            } else {
                UserManager.getInstance().setKeywordEmptyCount(this, 0);
            }
        } else {
            if (code != 100) {
                UserManager.getInstance().setKeywordEmptyCount(this, count + 1);
            }
        }

        if (response == -1) {
//            if (_wifiMode) {
//                if (WifiProxyController.getWifiProxy(MainActivity.this) == null) {
//                    if (_retryCount < MAX_NETWORK_RETRY_COUNT) {
//                        ++_retryCount;
//                        showMessage("연결에러(" + response + ", code:" + code + "). 10초후 다시 시도합니다.");
//                        sendEmptyMessageDelayed(retryMessage, 10000);
//                    } else {
//                        rebootForError(response, code);
//                    }
//                } else {
//                    showMessage("연결에러(" + response + ", code:" + code + "). 프록시 초기화.");
//                    WifiProxyController.unsetWifiProxy(MainActivity.this);
//                }
//            } else {
                rebootForError(response, code);
//            }
        } else {
            int delayMills = baseDelaySec * 1000;

            if (code == 100) {
                // 가입된 클라이언트가 아님. 10초후 재가입 시도.
                showMessage("가입된 휴대폰이 아닙니다. 10초후 재가입을 시도합니다.");
                sendEmptyMessageDelayed(REGISTER_DEVICE, 10000);
            } else if (code == 1) {
                showMessage("처리할 키워드가 없습니다(code:" + code + "). " + baseDelaySec + "초후 다시 시도합니다.");
                sendEmptyMessageDelayed(GET_KEYWORDS, delayMills);
            } else if (code == 2) {
//                    showToast("슬롯으 없습니다. 20초후 다시 시도합니다.", Toast.LENGTH_SHORT);
                showMessage("처리할 키워드가 없습니다(code:" + code + "). " + baseDelaySec + "초후 다시 시도합니다.");
                sendEmptyMessageDelayed(GET_KEYWORDS, delayMills);
            } else if (code == 11) {
//                if (_deviceType == DEVICE_TYPE_MOBILE_AUTO_LIKE) {
//                    showMessage("처리할 게시물이 없습니다.(code:" + code + "). 20초후 다시 시도합니다.");
//                } else {
//                    showMessage("사용할수 있는 아이디가 없습니다.(code:" + code + "). 20초후 다시 시도합니다.");
//                }
                sendEmptyMessageDelayed(GET_KEYWORDS, delayMills);
            } else {
                showMessage("실행에러(" + response + ", code:" + code + "). " + baseDelaySec + "초후 다시 시도합니다.");
                sendEmptyMessageDelayed(retryMessage, delayMills);
            }
        }
    }


    public void 슬롯불러오기2(){
        if(BuildConfig.PARENT_MODE) {
            enableAp();
        }
        showMessage("슬롯을 불러오는 중...");
        try {
            Log.d(TAG, "슬롯 불러오기!");
            touchEnabled(false);
            String responseBody = "";
            HttpClient client = new DefaultHttpClient();
            String url = BuildConfig.SERVER_URL + "/main/api/keyword/request/";
//            String url = BuildConfig.SERVER_URL + "/api/product/nplace/frequent/request";

            // 랜덤 실행.
            int random = (int) MathHelper.randomRange(0, 3);
//            random = 0;
            if (random == 1) {
                Log.d(TAG, "쿠팡 모드");
                url = BuildConfig.SERVER_URL + "/api/product/coupang/keyword/request/";
            } else if (random == 2) {
                Log.d(TAG, "구글 모드");
                url = BuildConfig.SERVER_URL + "/api/product/google/keyword/request/";
            } else if (random == 3) {
                Log.d(TAG, "사이트 모드");
                url = BuildConfig.SERVER_URL + "/api/product/site/keyword/request/";
            } else {
                Log.d(TAG, "네이버 모드");
            }

//            url = BuildConfig.SERVER_URL + "/api/product/nplace/frequent/request/shop.php";
//            url = BuildConfig.SERVER_URL + "/api/product/nplace/frequent/request/view.php";
//            url = BuildConfig.SERVER_URL + "/api/product/coupang/keyword/request/";
//            url = BuildConfig.SERVER_URL + "/api/product/google/keyword/request/";
//            url = BuildConfig.SERVER_URL + "/api/product/site/keyword/request/";

            if (BuildConfig.FLAVOR_mode.equals("rank")) {
                random = (int) MathHelper.randomRange(0, 3);
//                random = 0;
                if (random == 1) {
                    Log.d(TAG, "쿠팡 랭킹 모드");
                    url = BuildConfig.SERVER_URL + "/api/product/ranking/request/coupang.php";
                } else if (random == 2) {
                    Log.d(TAG, "구글 랭킹 모드");
                    url = BuildConfig.SERVER_URL + "/api/product/ranking/request/google.php";
                } else if (random == 3) {
                    Log.d(TAG, "사이트 랭킹 모드");
                    url = BuildConfig.SERVER_URL + "/api/product/ranking/request/site.php";
                } else {
                    Log.d(TAG, "네이버 랭킹 모드");
//                url = BuildConfig.SERVER_URL + "/api/product/ranking/request/";
//                http://198.13.48.146/api/product/ranking/request/inter.php
                    url = BuildConfig.SERVER_URL + "/api/product/ranking/request/inter.php";
//                url = BuildConfig.SERVER_URL + "/api/product/ranking/request/shop.php";
                }
            } else if (BuildConfig.FLAVOR_mode.equals("rankShop")) {
                url = BuildConfig.SERVER_URL + "/api/product/ranking/request/nshop.php";
            } else if (BuildConfig.FLAVOR_mode.equals("rankPlace")) {
                url = BuildConfig.SERVER_URL + "/api/product/ranking/request/nplace.php";
            } else if (BuildConfig.FLAVOR_mode.equals("rankFPlace")) {
                url = BuildConfig.SERVER_URL + "/api/product/ranking/request/nfplace.php";
            }

            // Test..
//            if (true) {
//                _targetWeb = WEB_GOOGLE;
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "nview";
////                keywordItem.keyword = "분당임플란트";
////                keywordItem.mid1 = "https://m.blog.naver.com/s17135/220974848478";
////                _keywords.add(keywordItem);
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "site";
////                keywordItem.keyword = "bs블라인드";
//////                keywordItem.url = "https://inflow.pay.naver.com/rd?tr=ds&retUrl=https%3A%2F%2Fm.smartstore.naver.com";
////                keywordItem.url = "https://inflow.pay.naver.com/rd?tr=ds&retUrl=https%3A%2F%2Fm.smartstore.naver.com%2Fbsblind&pType=M&no=&vcode=5nr37hTMbR0M2dOdVUu0tawrqK5gKr3O9B3PLWHPu0nRtiZLcE7yRZCiKFTDQh4od6lromH6DXPxTEoaWGg0xra4Oj";
//////                keywordItem.url = "https://blog.naver.com/pritia1004/222253997779";
//////                keywordItem.url = "https://blog.naver.com/maeil0824/222249612345";
////                _keywords.add(keywordItem);
////
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "coupang";
////                keywordItem.keyword = "식용 아르간오일 250";
////                keywordItem.url = "https://m.coupang.com/vp/products/11340999588?vendorItemId=72357606721&isAddedCart=";
////                _keywords.add(keywordItem);
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "coupang";
////                keywordItem.keyword = "이어팟";
////                keywordItem.url = "http://m.coupang.com/vp/products/241954414";
////                _keywords.add(keywordItem);
////
////                _targetWeb = WEB_COUPANG;
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "place_save";
////                keywordItem.keyword = "안산중고폰";
////                keywordItem.mid1 = "https://m.cafe.naver.com/lovelove20081207/3534";
////                _keywords.add(keywordItem);
//
//                KeywordItemMoon keywordItem =  new KeywordItemMoon();
//                keywordItem.uid = 999999;
//                keywordItem.category = "nshop";
//                keywordItem.keyword = "차량 공기청정기";
////                keywordItem.mid1 = "82971372758";
//                keywordItem.mid1 = "24046205986";
//                keywordItem.mid2 = "82473172827";
//                _keywords.add(keywordItem);
//                _changeUa = true;
//                _targetWeb = WEB_NAVER;
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "nview";
////                keywordItem.keyword = "무타공 벽걸이tv";
////                keywordItem.mid1 = "https://m.blog.naver.com/bbabac234/222288404859";
////                _keywords.add(keywordItem);
//
////                KeywordItemMoon keywordItem =  new KeywordItemMoon();
////                keywordItem.uid = 999999;
////                keywordItem.category = "ninflu";
////                keywordItem.keyword = "매니큐어";
////                keywordItem.url = "https://in.naver.com/seia/contents/254734543213088?query=%EB%A7%A4%EB%8B%88%ED%81%90%EC%96%B4";
//////                keywordItem.url = "https://in.naver.com/withgr_sh/contents/246532133669376?query=%EB%A7%A4%EB%8B%88%ED%81%90%EC%96%B4";
////                _keywords.add(keywordItem);
//
//                showMessage( "키워드 " + _keywords.size() + "개 수신 완료.");
//                sendEmptyMessageDelayed(WORK_START, 5000);
//                return;
//            }

            HttpGet get = new HttpGet(url);
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                responseBody = EntityUtils.toString(entity);
            }
            JSONArray data = new JSONObject(responseBody).getJSONArray("data");
            int naverCount = 0;
            _targetWeb = WEB_DEFAULT;
            _changeUa = false;

            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.getJSONObject(i);
                KeywordItemMoon keywordItem =  new KeywordItemMoon();
                String mid2 = item.getString("mid2");
                if (mid2.equals("")) {
                    mid2 = ".";
                }

                keywordItem.uid = Integer.parseInt(item.getString("uid"));
                keywordItem.category = item.getString("category");
                keywordItem.keyword = item.getString("keyword");
                keywordItem.mid1 = item.getString("mid1").trim();
                keywordItem.mid2 = mid2;
                keywordItem.url = item.optString("url").trim();
                keywordItem.agency = item.optString("agency");
                keywordItem.account = item.optString("account");
//                keywordItem.code = item.optString("account");

                if (BuildConfig.FLAVOR_mode.contains("rank")) {
                    _keywords.add(keywordItem);
//                    break;
                } else {
                    keywordItem.pcmobile = item.getString("pcmobile");

                    if (item.getString("pcmobile").equals("mobile")) {
                        _keywords.add(keywordItem);
//                    정보.add(item.getSt ring("uid")+","+
//                            item.getString("category")+","+
//                            item.getString("keyword") + "," +
//                            item.getString("mid1") + "," +
//                            mid2);
                    }

                    if (keywordItem.category.equals("nshop")) {
                        _changeUa = true;
                        ++naverCount;
                    } else if (keywordItem.category.equals("nview")) {
                        ++naverCount;
                    } else if (keywordItem.category.equals("nshop_keyword")) {
                        ++naverCount;
                    } else if (keywordItem.category.equals("place_save")) {
                        ++naverCount;
                    } else if (keywordItem.category.equals("nfplace")) {
                        ++naverCount;
                    } else if (keywordItem.category.equals("site")) {
                        ++naverCount;
                    } else if (keywordItem.category.equals("coupang")) {
                        _targetWeb = WEB_COUPANG;
                    } else if (keywordItem.category.equals("google")) {
                    } else {
                    }
                }
            }

            if (naverCount > 0) {
                // 원래 취지는 이게 아니지만 작중하나라도 네이버작업이 있다면
                // 네이버 쿠키를 위해서 기본을 네이버 모드로 해준다.
                _targetWeb = WEB_NAVER;
            } else {
            }

            // test
            if (BuildConfig.FLAVOR_mode.contains("rank")) {
//                keywords.clear();

//                KeywordItemMoon keywordItem =  new KeywordItemMoon();
//                keywordItem.uid = 49;
//                keywordItem.category = "nshop";
//                keywordItem.keyword = "열무물김치";
//                keywordItem.mid1 = "22341077979";
//                keywordItem.mid2 = "";
//                keywords.add(keywordItem);

//                KeywordItemMoon keywordItem =  new KeywordItemMoon();
//                keywordItem.uid = 50;
//                keywordItem.category = "nshop";
//                keywordItem.keyword = "딤채김치냉장고";
//                keywordItem.mid1 = "20932928616";
//                keywordItem.mid2 = "21778440881";
//                keywords.add(keywordItem);
//
//                KeywordItemMoon keywordItem =  new KeywordItemMoon();
//                keywordItem.uid = 51;
//                keywordItem.category = "nshop";
//                keywordItem.keyword = "삼성김치냉장고";
//                keywordItem.mid1 = "20999657412";
//                keywordItem.mid2 = "82224151700";
//                keywords.add(keywordItem);

//                KeywordItemMoon keywordItem =  new KeywordItemMoon();
//                keywordItem.uid = 215;
//                keywordItem.category = "nplace";
//                keywordItem.keyword = "종각 맛집";
//                keywordItem.mid1 = "촌놈숯불닭갈비 종로점";
//                keywordItem.mid2 = "서울 종로구 삼일대로19길 6 1층";
//                keywordItem.url = "https://m.place.naver.com/restaurant/1588867567/home?entry=ple";
//                keywordItem.code = "1588867567";
//                keywords.add(keywordItem);

//                KeywordItemMoon keywordItem =  new KeywordItemMoon();
//                keywordItem.uid = 216;
//                keywordItem.category = "nplace";
//                keywordItem.keyword = "부산 연산동 맛집";
//                keywordItem.mid1 = "마포통구이";
//                keywordItem.mid2 = "부산 연제구 쌍미천로151번길 40 1층";
//                keywordItem.url = "https://m.place.naver.com/restaurant/1702472176/home?entry=ple";
//                keywordItem.code = "1702472176";
//                keywords.add(keywordItem);
            }

            if (_keywords.size() > 0) {
                /*
                if(new Random().nextInt(10) >= 0) {
                    로그인쿠키 = true;
                }*/
                /*
                if(BuildConfig.PARENT_MODE) {
                    Log.d(TAG, "아이피 변경!");
                    changeIp();
                }
                 */
                showMessage( "키워드 " + _keywords.size() + "개 수신 완료.");
                sendEmptyMessageDelayed(WORK_START, 5000);
            } else {
                showMessage("처리할 키워드가 없습니다. 15초후 다시 시도합니다.");
                sendEmptyMessageDelayed(GET_KEYWORDS, 15000);
            }
        } catch (IOException ex) {
            Log.d(TAG, "IOException: " + ex.getMessage());
            changeIp();
            showMessage("처리할 키워드가 없습니다. 15초후 다시 시도합니다.");
            sendEmptyMessageDelayed(GET_KEYWORDS, 15000);
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
            showMessage("처리할 키워드가 없습니다. 15초후 다시 시도합니다.");
            sendEmptyMessageDelayed(GET_KEYWORDS, 15000);
        }
    }

    String 날짜(int date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        cal.add(Calendar.DATE, -date);
        return df.format(cal.getTime());
    }


//    public String nnb가져오기(){
//        String nnb ="";
//        HttpGet httpget;
//        try {
//            HttpClient client = new DefaultHttpClient();
//            String url =  BuildConfig.SERVER_URL+"/main/nnb/get_nnb.php";
//            httpget = new HttpGet(url);
//
//            HttpResponse response = client.execute(httpget);
//            HttpEntity entity = response.getEntity();
//            if (entity != null) {
//                nnb = EntityUtils.toString(entity);
//
//            }
//        } catch (IOException e) {
//
//        }
//        return nnb;
//    }


    /*
    public String 페이스북쿠키(){
        String 데이터 ="";
        HttpGet httpget;
        try {
            HttpClient client = new DefaultHttpClient();
            if(빌드) {
                httpget = new HttpGet("http://baromk01.cafe24.com/main/sns_account/get_sns.php?db=1");
            }else{
                httpget = null;
            }

            HttpResponse response = client.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                데이터 = EntityUtils.toString(entity);
            }
            JSONArray data = new JSONArray(데이터);
            int 랜덤 =  new Random().nextInt(data.length());
           String c_user = data.getJSONArray(랜덤).toString().split(",")[0];
           String xs = data.getJSONArray(랜덤).toString().split(",")[1];
           Log.d("로그인 쿠키",c_user+"/"+xs);
        } catch (IOException e) {

        } catch (JSONException e) {

        }
        return nnb;
    }
     */


    public void changeIp()
    {
        changeIp(false);
    }

    public void changeIp(boolean forceChange)
    {
        // 22.08.26 - 여기서 아이피 변경 플래그를 하지 않고, 시작시에 아이피를 변경하는것으로 변경.
//        int maxCount = 2;
//
//        if (forceChange) {
//            maxCount = 0;
//        }
//
//        int processCount = UserManager.getInstance().getProcessCount(this) + 1;
//
//        if (processCount >= maxCount) {
//            UserManager.getInstance().setProcessCount(this, 0);
//        } else {
//            UserManager.getInstance().setProcessCount(this, processCount);
//            sendEmptyMessage(END);
//            return;
//        }

        if (!BuildConfig.PARENT_MODE) {
            if (_restart) {
                sendEmptyMessage(END);
            } else {
                runNetworkConnected(NetworkReceiver.WIFI_CONNECTED);
            }
            return;
        }

        boolean useAirplane = false;
        String tel = SystemHelper.getTelecomName(this);

        if (tel != null && tel.equals("kt") && Build.MODEL.contains("330S")) {
            useAirplane = true;
        }

        // test
//        useAirplane = true;

        if (useAirplane) {
            Log.d(TAG, "비행기 모드로 아이피 변경");

            if (AdbController.isAirplaneMode()) {
                showMessage("비행기 모드 끄는 중...");
                AdbController.setAirplaneMode(false);
                SystemClock.sleep(5000);

                Log.d(TAG, "데이터 모드 체크");
                // 에어플레인모드를 껐는데 데이터가 꺼져있다면 켜준다.
                if (!dataEnabled()) {
                    Log.d(TAG, "데이터 모드 켜기");
                    AdbController.setDataNetworkThread(true);
                }
            } else {
                showMessage("비행기 모드 켜는 중...");
                AdbController.setAirplaneMode(true);
            }

//            if (SystemController.isAirplaneMode(this)) {
//                SystemController.setAirplaneMode(this, false);
//                SystemClock.sleep(5000);
//            } else {
//                SystemController.setAirplaneMode(this, true);
//                SystemClock.sleep(5000);
//                SystemController.setAirplaneMode(this, false);
//                SystemClock.sleep(5000);
//            }
        } else {
            Log.d(TAG, "데이터 네트워크로 아이피 변경");

            if (!dataEnabled()) {
                Log.d(TAG, "데이터 모드 켜기");
                AdbController.setDataNetworkThread(true);
            } else {
                Log.d(TAG, "데이터 모드 끄기");
                AdbController.setDataNetworkThread(false);
            }

//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    restart();
//                }
//            }, 2000);
        }
    }

    private void toggleDataNetwork() {
        try {
            MobileNetworkController.setMobileNetworkfromLollipop(ActivityMCloud.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void touchEnabled(final boolean enabled) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enabled) {
                    // 터치 활성화.
                    _spinKitView.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                } else {
                    // 터치 비활성화.
                    _spinKitView.setVisibility(View.VISIBLE);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }
        });
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "received action: " + action);
            runNetworkConnected(action);
        }
    }

    public class MyJavascriptInterface {
        @JavascriptInterface
        public void getHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
            System.out.println(html);
        }
    }

    WifiReceiver.OnWifiStateChangedListener wifiStateChangedListener = new WifiReceiver.OnWifiStateChangedListener() {
        @Override
        public void OnChanged(int state) {
            Log.d(TAG, "OnChanged receive");
            switch (state) {
                case WifiManager.WIFI_STATE_DISABLED:
                    Log.d(TAG, "WIFI_STATE_DISABLED");
                    break;

                case WifiManager.WIFI_STATE_ENABLED:
                    Log.d(TAG, "WIFI_STATE_ENABLED");
                    break;

                case WifiManager.WIFI_STATE_UNKNOWN:
                    Log.d(TAG, "WIFI_STATE_UNKNOWN");
                    break;
            }

            if (_wifiWaiting) {
                if (state == WifiManager.WIFI_STATE_ENABLED) {
                    _wifiWaiting = false;
                } else {
                    if (_message._lastMessage == START) {
                        Log.d(TAG, "Wifi 켜는 중...");
                        // 시작 모드일때 끊어졌다면 와이파이만 다시 켜준다.
                        WifiController.wifiEnabled(ActivityMCloud.this, true);
                    } else {
                        if (WifiController.isWifiEnabled(ActivityMCloud.this)) {
                            Log.d(TAG, "Wifi 다시 켜는 중...");
                            // 시작 모드일때 끊어졌다면 와이파이만 다시 켜준다.
                            WifiController.wifiEnabled(ActivityMCloud.this, true);
                        } else {
                            // 그 외는 처리할 와이파이로 잡아준다.
                            Log.d(TAG, "Wifi 다시 연결 중...");
                            connectWifi();
                        }
                    }
                }
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (!BuildConfig.PARENT_MODE) {
            IntentFilter filter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
//        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(_wifiReceiver, filter);
        }

        IntentFilter messageFilter = new IntentFilter(NetworkReceiver.WIFI_CONNECTED);
        messageFilter.addAction(NetworkReceiver.ANY_CONNECTED);
        messageFilter.addAction(NetworkReceiver.CONNECTION_LOST);
        registerReceiver(_br, messageFilter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Registers BroadcastReceiver to track network connection changes.
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(_networkReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!BuildConfig.PARENT_MODE) {
            unregisterReceiver(_wifiReceiver);
        }

        if (_br != null) {
            try {
                unregisterReceiver(_br);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (_networkReceiver != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    unregisterReceiver(_networkReceiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if (_thread != null) {
//            _thread.quit();
//        }

        if (_handlerThread != null) {
            _handlerThread.getHandler().removeCallbacksAndMessages(null);
            _handlerThread.quit();
            _handlerThread = null;
        }

        if (_patternHandlerThread != null) {
            _patternHandlerThread.getHandler().removeCallbacksAndMessages(null);
            _patternHandlerThread.quit();
            _patternHandlerThread = null;
        }

        for (PatternHandlerThread thread : _packetPatternHandlerThreadList) {
            thread.getHandler().removeCallbacksAndMessages(null);
            thread.quit();
        }

        _packetPatternHandlerThreadList.clear();

        if (__handlerThread != null) {
            __handlerThread.quit();
            __handlerThread = null;
        }
    }
}