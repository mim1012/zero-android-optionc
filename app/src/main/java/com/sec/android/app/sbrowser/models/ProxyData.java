package com.sec.android.app.sbrowser.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by K2Y on 2017. 7. 10..
 */

public class ProxyData {
    @SerializedName("service_id")
    @Expose
    public int serviceId;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("domain")
    @Expose
    public String domain;

    @SerializedName("key")
    @Expose
    public String key;

    @SerializedName("options")
    @Expose
    public String options;

    @SerializedName("api_url")
    @Expose
    public String apiUrl;
}
