package com.sec.android.app.sbrowser.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BookmarkItem extends BaseData {
    @SerializedName("bookmark_id")
    @Expose
    public int bookmarkId;
}
