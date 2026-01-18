package com.sec.android.app.sbrowser.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UaData extends BaseData {
    @SerializedName("data")
    @Expose
    public List<NnbData> uas;
}
