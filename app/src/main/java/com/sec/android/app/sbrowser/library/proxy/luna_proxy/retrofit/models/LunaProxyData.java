package com.sec.android.app.sbrowser.library.proxy.luna_proxy.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LunaProxyData {
    @SerializedName("code")
    @Expose
    public int code;

    @SerializedName("success")
    @Expose
    public boolean success;

    @SerializedName("msg")
    @Expose
    public String msg;

    @SerializedName("data")
    @Expose
    public List<Proxy> data = null;

    public class Proxy {
        @SerializedName("ip")
        @Expose
        public String ip;

        @SerializedName("port")
        @Expose
        public String port;
    }
}
