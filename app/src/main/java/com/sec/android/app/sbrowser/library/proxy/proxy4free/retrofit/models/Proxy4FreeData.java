package com.sec.android.app.sbrowser.library.proxy.proxy4free.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Proxy4FreeData {
    @SerializedName("host")
    @Expose
    public String host;

    @SerializedName("port")
    @Expose
    public String port;

    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("pw")
    @Expose
    public String pw;
}
