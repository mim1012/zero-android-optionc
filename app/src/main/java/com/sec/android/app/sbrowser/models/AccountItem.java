package com.sec.android.app.sbrowser.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AccountItem extends BaseData {
    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("password")
    @Expose
    public String password;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("birthday")
    @Expose
    public String birthday;

    @SerializedName("code")
    @Expose
    public int code;
}
