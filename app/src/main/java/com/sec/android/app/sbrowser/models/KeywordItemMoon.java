package com.sec.android.app.sbrowser.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KeywordItemMoon extends BaseData {
    @SerializedName("uid")
    @Expose
    public int uid;

    @SerializedName("category")
    @Expose
    public String category;

    @SerializedName("keyword")
    @Expose
    public String keyword;

    @SerializedName("mid1")
    @Expose
    public String mid1;

    @SerializedName("mid2")
    @Expose
    public String mid2;

    @SerializedName("url")
    @Expose
    public String url;

    @SerializedName("code")
    @Expose
    public String code;

    @SerializedName("pcmobile")
    @Expose
    public String pcmobile;

    @SerializedName("agency")
    @Expose
    public String agency;

    @SerializedName("account")
    @Expose
    public String account;


    @SerializedName("web_target")
    @Expose
    public int webTarget;

    @SerializedName("ua_change")
    @Expose
    public int uaChange;

    @SerializedName("shop_home")
    @Expose
    public int shopHome;

    public KeywordItem item;
}
