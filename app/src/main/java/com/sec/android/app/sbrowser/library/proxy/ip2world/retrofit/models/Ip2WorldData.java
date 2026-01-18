package com.sec.android.app.sbrowser.library.proxy.ip2world.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sec.android.app.sbrowser.library.proxy.common.retrofit.models.ProxyData;

import java.util.List;

public class Ip2WorldData {
    @SerializedName("code")
    @Expose
    public int code;

    @SerializedName("success")
    @Expose
    public boolean success;

    @SerializedName("msg")
    @Expose
    public String msg;

    @SerializedName("request_ip")
    @Expose
    public String request_ip;

    @SerializedName("data")
    @Expose
    public List<ProxyData> data = null;
}
