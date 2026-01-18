package com.sec.android.app.sbrowser.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class KeywordData extends BaseData {
    public ArrayList<KeywordItem> data = new ArrayList<>();

    @SerializedName("device_ip")
    @Expose
    public String deviceIp;

    @SerializedName("user_agent")
    @Expose
    public String userAgent;

    @SerializedName("naver_cookie")
    @Expose
    public NaverCookieData naverCookie;

    @SerializedName("naver_login_cookie")
    @Expose
    public NaverLoginCookieData naverLoginCookie;

    public class NaverLoginCookieData {
        @SerializedName("nid_aut")
        @Expose
        public String nidAut;

        @SerializedName("nid_jkl")
        @Expose
        public String nidJkl;

        @SerializedName("nid_ses")
        @Expose
        public String nidSes;

        @SerializedName("nnb")
        @Expose
        public String nnb;
    }
}
