package com.sec.android.app.sbrowser.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by K2Y on 2017. 7. 10..
 */

public class DeviceInfoData extends BaseData {
    public int type;

    @SerializedName("ap_only")
    @Expose
    public int apOnly;

    @SerializedName("capture_mode")
    @Expose
    public int captureMode;

    @SerializedName("target_ssid")
    @Expose
    public String targetSsid;

    @SerializedName("target_pwd")
    @Expose
    public String targetPassword;

    @SerializedName("delete_local_storage")
    @Expose
    public boolean deleteLocalStorage;

    @SerializedName("x_requested_with")
    @Expose
    public String xRequestedWith;

    @SerializedName("proxy")
    @Expose
    public ProxyData proxy;
}
