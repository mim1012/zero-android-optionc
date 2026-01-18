package com.sec.android.app.sbrowser.library.updater.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VersionData {
    @SerializedName("version_code")
    @Expose
    public int versionCode;

    @SerializedName("url")
    @Expose
    public String url;

    @SerializedName("update_message")
    @Expose
    public String updateMessage;
}
