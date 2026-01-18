package com.sec.android.app.sbrowser.engine;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

// 미사용.
public class NaverAccountData {

    private static final String TAG = NaverAccountData.class.getSimpleName();

    public List<NaverAccount> accountList = null;

    public NaverAccountData() {
        accountList = new ArrayList<>();
    }

    public String getJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static NaverAccountData getFromJson(String jsonString) {
        NaverAccountData data = null;

        try {
            Gson gson = new Gson();
            data = gson.fromJson(jsonString, NaverAccountData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
