package com.sec.android.app.sbrowser.engine;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NaverAccountManager {

    private static final String TAG = NaverAccount.class.getSimpleName();

    private static final String PREFERENCES_SETTING = "naverAccount";

    private static final String KEY_DATA_JSON = "dataJson";

    private List<NaverAccount> _accountList = null;
    private Type _listType = new TypeToken<List<NaverAccount>>() {}.getType();

    public static NaverAccountManager getInstance() {
        return NaverAccountManager.LazyHolder.INSTANCE;
    }

    public void loadData(Context context) {
        SharedPreferences pref = getPref(context);
        String dataJson = pref.getString(KEY_DATA_JSON, "");

        try {
            Gson gson = new Gson();
            _accountList = gson.fromJson(dataJson, _listType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveData(Context context) {
        SharedPreferences.Editor editor = getPref(context).edit();
        Gson gson = new Gson();
        editor.putString(KEY_DATA_JSON, gson.toJson(_accountList, _listType));
        editor.apply();
    }

    public void clearData(Context context) {
        SharedPreferences.Editor editor = getPref(context).edit();
        editor.clear();
        editor.apply();
    }


    public NaverAccount getAccount(int index) {
        if (index < 0 || index >= _accountList.size()) {
            return null;
        }

        return _accountList.get(index);
    }

    public NaverAccount getAccount(String accountId) {
        // 이 foreach 는 데이터 관리상 데이터 개수가 몇개 안되므로 foreach 로 구현하지만
        // ArrayList 는 원칙은 for 문으로 작성해야 성능이 좋다.
        for (NaverAccount account : _accountList) {
            if (account.accountId.equals(accountId)) {
                return account;
            }
        }

        return null;
    }

    public NaverAccount getAccountForNidAut(String accountId) {
        for (NaverAccount account : _accountList) {
            if (account.nidAut.equals(accountId)) {
                return account;
            }
        }

        return null;
    }

    public void addAccount(NaverAccount account) {
        // 중복 값이 있다면 삭제 후 다시 추가해준다. 단 이 상황은 거의 발생하지 않는다.
        // 이부분을 나중에는 가능하면, 값 대입으로 바꾸어도 될듯하다.
        removeAccount(account.accountId);
        _accountList.add(account);
    }

    public boolean removeAccount(String accountId) {
        for (int i = 0; i < _accountList.size(); ++i) {
            NaverAccount account = _accountList.get(i);

            if (account.accountId.equals(accountId)) {
                _accountList.remove(i);
                return true;
            }
        }

        return false;
    }

    private static class LazyHolder {
        public static final NaverAccountManager INSTANCE = new NaverAccountManager();
    }

    private NaverAccountManager() {
        _accountList = new ArrayList<>();


//        NaverAccount ss = new NaverAccount();
//        ss.status = 1;
//        ss.accountName = "ssrg";
//        _accountList.add(ss);
//
//        NaverAccount ss2 = new NaverAccount();
//        ss2.status = 2;
//        ss2.accountName = "zelijsg";
//        _accountList.add(ss2);
//
//        NaverAccount ss3 = new NaverAccount();
//        ss3.status = 1;
//        ss3.accountName = "s2srg";
//        _accountData.accountList.add(ss3);
//
//        NaverAccount ss4 = new NaverAccount();
//        ss4.status = 2;
//        ss4.accountName = "z2elijsg";
//        _accountData.accountList.add(ss4);

    }

    private SharedPreferences getPref(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_SETTING, Context.MODE_PRIVATE);
    }
}
