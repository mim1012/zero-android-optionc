package com.sec.android.app.sbrowser.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AccountData extends BaseData {
    @SerializedName("data")
    @Expose
    public AccountItem data;
}
