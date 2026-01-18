package com.sec.android.app.sbrowser.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KeywordItem extends BaseData {
    // 통검
    public static final int SHOP_HOME_MOBILE = 0;
    // 쇼검
    public static final int SHOP_HOME_MOBILE_SHOP = 1;
    // 빈검
    public static final int SHOP_HOME_SEARCH_EMPTY = 2;
    // 쇼검DI
    public static final int SHOP_HOME_MOBILE_SHOP_DI = 3;
    // 통검DI
    public static final int SHOP_HOME_SEARCH_DI = 4;

    public static final int WORK_TYPE_DUMMY1 = 1;
    public static final int WORK_TYPE_DUMMY2 = 2;
    public static final int WORK_TYPE_INPUT = 3;
    public static final int WORK_TYPE_CLIPBOARD = 4;

    public static final int PATTERN_TYPE_NORMAL = 0;
    public static final int PATTERN_TYPE_SHOP_ABC_BACK = 1;
    public static final int PATTERN_TYPE_SHOP_ABC_RANDOM = 2;
    public static final int PATTERN_TYPE_SHOP_AI_NEWS_VIEW = 3;
    public static final int PATTERN_TYPE_SHOP_URL_CHANGE = 4;
    public static final int PATTERN_TYPE_SHOP_PACKET = 5;
    public static final int PATTERN_TYPE_SHOP_PACKET_BOOST = 6;

    public static final int PATTERN_TYPE_NORMAL_INTO_DETAIL = 7;

    public static final int AFTER_TYPE_NONE = 0;
    public static final int AFTER_TYPE_POPULAR = 1;
    public static final int AFTER_TYPE_OTHER = 2;
    public static final int AFTER_TYPE_REVIEW = 3;
    public static final int AFTER_TYPE_QA = 4;

    public static final int PACKET_PATTERN_TYPE_DEFAULT = 0;
    public static final int PACKET_PATTERN_TYPE_A_CLICK_VIEW = 1;
    public static final int PACKET_PATTERN_TYPE_A_CLICK_OTHER = 2;
    public static final int PACKET_PATTERN_TYPE_A_CLICK_B_CLICK = 3;
    public static final int PACKET_PATTERN_TYPE_A_CLICK_B_CLICK_C_CLICK = 4;
    public static final int PACKET_PATTERN_TYPE_MAIN = 5;

    @SerializedName("traffic_id")
    @Expose
    public int trafficId;

    @SerializedName("keyword_id")
    @Expose
    public int keywordId;

    @SerializedName("service_type")
    @Expose
    public int serviceType;

    @SerializedName("intercept_type")
    @Expose
    public int interceptType;

    @SerializedName("web_target")
    @Expose
    public int webTarget;

    @SerializedName("ua_change")
    @Expose
    public int uaChange;

    @SerializedName("cookie_home_mode")
    @Expose
    public int cookieHomeMode;

    @SerializedName("cookie_use_image")
    @Expose
    public int cookieUseImage;

    @SerializedName("use_image")
    @Expose
    public int useImage;

    @SerializedName("shop_home")
    @Expose
    public int shopHome;

    @SerializedName("search")
    @Expose
    public String search;

    @SerializedName("search_main")
    @Expose
    public String searchMain;

    @SerializedName("search1")
    @Expose
    public String search1;

    @SerializedName("search2")
    @Expose
    public String search2;

    @SerializedName("cat_id")
    @Expose
    public String catId;

    @SerializedName("incl_empty")
    @Expose
    public int inclEmpty;

    @SerializedName("work_type")
    @Expose
    public int workType;

    @SerializedName("work_more")
    @Expose
    public int workMore;

    @SerializedName("referer_mode")
    @Expose
    public int refererMode;

    @SerializedName("sec_fetch_site_mode")
    @Expose
    public int secFetchSiteMode;

    @SerializedName("pattern_type")
    @Expose
    public int patternType;

    @SerializedName("source_type")
    @Expose
    public int sourceType;

    @SerializedName("source_url")
    @Expose
    public String sourceUrl;

    @SerializedName("random_count")
    @Expose
    public int randomScrollCount;

    @SerializedName("random_click_count")
    @Expose
    public int randomClickCount;

    @SerializedName("home_random_click_count")
    @Expose
    public int homeRandomClickCount;

    @SerializedName("low_delay")
    @Expose
    public int lowDelay;

    @SerializedName("account")
    @Expose
    public AccountItem account;

    @SerializedName("use_nid")
    @Expose
    public int useNid;

    @SerializedName("use_random_active")
    @Expose
    public int useRandomActive;

    @SerializedName("use_working_api")
    @Expose
    public int useWorkingApi;

    @SerializedName("use_lcs_post")
    @Expose
    public int useLcsPost;

    @SerializedName("use_slc_post")
    @Expose
    public int useSlcPost;

    @SerializedName("use_fetch_store_products")
    @Expose
    public int useFetchStoreProducts;

    @SerializedName("use_random_nid")
    @Expose
    public int useRandomNid;

    @SerializedName("after_type")
    @Expose
    public int afterType;

    @SerializedName("stay_delay_type")
    @Expose
    public int stayDelayType;

    @SerializedName("search_work")
    @Expose
    public String searchWork;

    @SerializedName("product_name")
    @Expose
    public String productName;

    @SerializedName("store_name")
    @Expose
    public String storeName;

    @SerializedName("target")
    @Expose
    public String target;

    @SerializedName("url")
    @Expose
    public String url;

    @SerializedName("code")
    @Expose
    public String code;

    @SerializedName("code2")
    @Expose
    public String code2;

    @SerializedName("code3")
    @Expose
    public String code3;

    @SerializedName("get_detail")
    @Expose
    public int getDetail;

    @SerializedName("product_url")
    @Expose
    public String productUrl;

    @SerializedName("product_id")
    @Expose
    public String productId;

    @SerializedName("plan_id")
    @Expose
    public String planId;

    @SerializedName("base_url")
    @Expose
    public String baseUrl;

    @SerializedName("max_page")
    @Expose
    public String maxPage;

    public boolean isPacketPattern() {
        return patternType == KeywordItem.PATTERN_TYPE_SHOP_PACKET ||
                patternType == KeywordItem.PATTERN_TYPE_SHOP_PACKET_BOOST;
    }

    public boolean isPacketBoostPattern() {
        return patternType == KeywordItem.PATTERN_TYPE_SHOP_PACKET_BOOST;
    }
}
