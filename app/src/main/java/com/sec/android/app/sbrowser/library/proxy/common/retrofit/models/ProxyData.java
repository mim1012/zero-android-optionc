package com.sec.android.app.sbrowser.library.proxy.common.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProxyData {
    @SerializedName("ip")
    @Expose
    public String ip;

    @SerializedName("port")
    @Expose
    public String port;
}
