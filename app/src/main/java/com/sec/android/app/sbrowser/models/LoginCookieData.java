package com.sec.android.app.sbrowser.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginCookieData extends BaseData {
    @SerializedName("login_cookie_id")
    @Expose
    public long loginCookieId;
}
