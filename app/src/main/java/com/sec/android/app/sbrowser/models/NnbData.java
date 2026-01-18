package com.sec.android.app.sbrowser.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NnbData extends BaseData {
    @SerializedName("login_cookie_id")
    @Expose
    public long loginCookieId;

    @SerializedName("naver_cookie_id")
    @Expose
    public long naverCookieId;

    @SerializedName("nnb")
    @Expose
    public String nnb;

    @SerializedName("nid_inf")
    @Expose
    public String nidInf;

    @SerializedName("nid_aut")
    @Expose
    public String nidAut;

    @SerializedName("nid_jkl")
    @Expose
    public String nidJkl;

    @SerializedName("nid_ses")
    @Expose
    public String nidSes;

    @SerializedName("naver_cookie_other")
    @Expose
    public String naverCookieOther;

    @SerializedName("id")
    @Expose
    public int uaId;

    @SerializedName("ua")
    @Expose
    public String ua;

    @SerializedName("chrome_version")
    @Expose
    public String chromeVersion;

    @SerializedName("browser_version")
    @Expose
    public String browserVersion;

    @SerializedName("model")
    @Expose
    public String model;

    @SerializedName("platform_version")
    @Expose
    public String platformVersion;
}
