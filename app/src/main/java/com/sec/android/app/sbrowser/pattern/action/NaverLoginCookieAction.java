package com.sec.android.app.sbrowser.pattern.action;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sec.android.app.sbrowser.engine.CookieFileManager;

public class NaverLoginCookieAction {

    private static final String TAG = NaverLoginCookieAction.class.getSimpleName();

    private static final String KEY_NNB = "NNB";
    private static final String KEY_NID_INF = "nid_inf";
    private static final String KEY_NID_AUT = "NID_AUT";
    private static final String KEY_NID_JKL = "NID_JKL";
    private static final String KEY_NID_SES = "NID_SES";

    private final Context _context;

    private int _retryCount = 0;
    private int _result = 0;



    public NaverLoginCookieAction(Context context) {
        _context = context;
    }

    public String getNidAutChrome() {
        return getNidAut(CookieFileManager.BROWSER_CHROME);
    }

    public boolean isLoginChrome() {
        return isLogin(CookieFileManager.BROWSER_CHROME);
    }

    public String getCookieJsonStringChrome() {
        return getCookieJsonString(CookieFileManager.BROWSER_CHROME);
    }

    private String getNidAut(int browserType) {
        CookieFileManager manager = new CookieFileManager();
        manager.setBrowserType(browserType);
        return manager.getCookieValue(_context, KEY_NID_AUT);
    }

    private boolean isLogin(int browserType) {
        CookieFileManager manager = new CookieFileManager();
        manager.setBrowserType(browserType);
        String valueAut = manager.getCookieValue(_context, KEY_NID_AUT);
        String valueSes = manager.getCookieValue(_context, KEY_NID_SES);

        // 보안상 앞에 자리만 보이기.
        if (!TextUtils.isEmpty(valueAut)) {
            Log.d(TAG, "### aut: " + valueAut.substring(0, 6));
        }

        if (!TextUtils.isEmpty(valueSes)) {
            Log.d(TAG, "### ses: " + valueSes.substring(0, 6));
        }

        return !TextUtils.isEmpty(valueAut) && !TextUtils.isEmpty(valueSes);
    }

    public String getCookieJsonString(int browserType) {
        CookieFileManager manager = new CookieFileManager();
        manager.setBrowserType(browserType);

//                    {"cookies": [{"domain": ".naver.com", "expires": 1717746593.335722, "httpOnly": 0, "name": "NNB", "path": "/", "priority": "Medium", "sameParty": 0, "sameSite": "None", "secure": 1, "session": 0, "size": 16, "sourcePort": 443, "sourceScheme": "Secure", "value": "AYPE6K5FOBHWI"}, {"domain": ".naver.com", "expires": 1685778596.114448, "httpOnly": 0, "name": "nid_inf", "path": "/", "priority": "Medium", "sameParty": 0, "secure": 0, "session": 0, "size": 18, "sourcePort": 443, "sourceScheme": "Secure", "value": "-1272038957"}, {"domain": ".naver.com", "expires": 1717746596.114499, "httpOnly": 1, "name": "NID_AUT", "path": "/", "priority": "Medium", "sameParty": 0, "secure": 0, "session": 0, "size": 71, "sourcePort": 443, "sourceScheme": "Secure", "value": "qEJtIuyqiqNi9DIRn/DOP7Feg0z6COwPtKZu3+rS8QjL3g3FITzNrn7s6pQWERlE"}, {"domain": ".naver.com", "expires": 1685778596.114512, "httpOnly": 0, "name": "NID_SES", "path": "/", "priority": "Medium", "sameParty": 0, "secure": 0, "session": 0, "size": 547, "sourcePort": 443, "sourceScheme": "Secure", "value": "AAABh4pyX/zvcDQC+nkraA8gH1P8kk19jtMgs+NR5afw6q1pc18EjOcERHtYlb6xK9B6GSV80AobNdX7woB67VkWIfD1J0hB6C1Zb4qreLiPvAMAZ39eZFT80IlOhhfpm9ATP+MLw3VDzwUYDCQ4YD6Id7xhp+xjkTW8o5gzx3BVYXYyJ49v5Ioj2JBy4kWsG3e36dag2FxjodmGnH5J8AYsr4gb9JYb8Ac/rwYZe9gH2LHt3Y43bveDwbvsRvU4vf0/FJUQYXxPtFw3fNjhcE2f848EsOGzINBmUH3xDFT7fsHC3nspeMSn/e6KiMgHuS8ent3eO9xgVg4DFmd0lgYz6zfCJ1NrnTPQ7pA17V7IX66KmZagYn8oy5OtzG7XQjle71Ge+wzV4HpFr+A/K3Skhr0NzdLLMENJ/pKc5pt8lyuDwtPyF3LWLFCkFQMkkShmGZ0uVXblci0IRQ3jvIM+HWsUBHg57fOLl6092fEHohpA43eV87wFvW8kQgW8BgrKEN4GRQdAYUW31dOMcD3bNS0="}, {"domain": ".naver.com", "expires": 1717746596.114525, "httpOnly": 0, "name": "NID_JKL", "path": "/", "priority": "Medium", "sameParty": 0, "secure": 1, "session": 0, "size": 51, "sourcePort": 443, "sourceScheme": "Secure", "value": "BBMzsFRjRjFiOPRQS6KyLvJqeKVboIkpUdZ10lCVg5Y="}], "pw1": "112"}
        JsonObject obj1 = new JsonObject();
        obj1.addProperty("name", KEY_NID_INF);
        obj1.addProperty("value", manager.getCookieValue(_context, KEY_NID_INF));

        JsonObject obj2 = new JsonObject();
        obj2.addProperty("name", KEY_NID_AUT);
        obj2.addProperty("value", manager.getCookieValue(_context, KEY_NID_AUT));

        JsonObject obj3 = new JsonObject();
        obj3.addProperty("name", KEY_NID_JKL);
        obj3.addProperty("value", manager.getCookieValue(_context, KEY_NID_JKL));

        JsonObject obj4 = new JsonObject();
        obj4.addProperty("name", KEY_NID_SES);
        obj4.addProperty("value", manager.getCookieValue(_context, KEY_NID_SES));

        JsonObject obj5 = new JsonObject();
        obj5.addProperty("name", KEY_NNB);
        obj5.addProperty("value", manager.getCookieValue(_context, KEY_NNB));

        JsonArray jsonArray = new JsonArray();
        jsonArray.add(obj1);
        jsonArray.add(obj2);
        jsonArray.add(obj3);
        jsonArray.add(obj4);
        jsonArray.add(obj5);

        JsonObject cookieObj = new JsonObject();
        cookieObj.add("cookies", jsonArray);

        Gson gson = new Gson();
        return gson.toJson(cookieObj);
    }
}
