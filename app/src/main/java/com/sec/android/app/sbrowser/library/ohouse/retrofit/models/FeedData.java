package com.sec.android.app.sbrowser.library.ohouse.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeedData {
    @SerializedName("productions")
    @Expose
    public List<Production> productions = null;

    @SerializedName("has_next")
    @Expose
    public boolean hasNext;

    public class Production {
        @SerializedName("id")
        @Expose
        public String id;

        @SerializedName("type")
        @Expose
        public String type;

        @SerializedName("brand_name")
        @Expose
        public String brandName;

        @SerializedName("cost")
        @Expose
        public int cost;

        @SerializedName("name")
        @Expose
        public String name;

        @SerializedName("original_price")
        @Expose
        public int originalPrice;

        @SerializedName("selling_cost")
        @Expose
        public int sellingCost;

        @SerializedName("selling_price")
        @Expose
        public int sellingPrice;
    }
}
