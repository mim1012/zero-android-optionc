package com.sec.android.app.sbrowser.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceIpData extends BaseData {
    @SerializedName("device_ip")
    @Expose
    public String deviceIp;
}
