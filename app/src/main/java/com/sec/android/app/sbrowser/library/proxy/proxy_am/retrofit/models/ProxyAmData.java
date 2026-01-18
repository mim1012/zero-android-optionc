package com.sec.android.app.sbrowser.library.proxy.proxy_am.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProxyAmData {
    @SerializedName("data")
    @Expose
    public List<Proxy> data = null;

    public class Proxy {
        @SerializedName("proxy")
        @Expose
        public String proxy;

        @SerializedName("ip")
        @Expose
        public String ip;

        @SerializedName("country")
        @Expose
        public String country;

        @SerializedName("city")
        @Expose
        public String city;

        @SerializedName("speed")
        @Expose
        public String speed;

        @SerializedName("uptime")
        @Expose
        public String uptime;
    }
}
