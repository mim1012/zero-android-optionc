package com.sec.android.app.sbrowser.library.proxy.py_proxy.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PyProxyData {
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
    public List<Proxy> data = null;

    public class Proxy {
        @SerializedName("ip")
        @Expose
        public String ip;

        @SerializedName("port")
        @Expose
        public int port;
    }
}
