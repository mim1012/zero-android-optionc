package com.sec.android.app.sbrowser.retrofit;

import com.sec.android.app.sbrowser.models.AccountData;
import com.sec.android.app.sbrowser.models.BaseData;
import com.sec.android.app.sbrowser.models.BookmarkData;
import com.sec.android.app.sbrowser.models.DeviceInfoData;
import com.sec.android.app.sbrowser.models.DeviceIpData;
import com.sec.android.app.sbrowser.models.KeywordData;
import com.sec.android.app.sbrowser.models.LoginCookieData;
import com.sec.android.app.sbrowser.models.NnbData;
import com.sec.android.app.sbrowser.models.UaData;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by K2Y on 2017. 7. 8..
 */

public interface Service {

    @GET("main/nnb/nnbua.php")
    Call<NnbData> getNnb();

    @GET("main/nnb/nnb_insert.php")
    Call<BaseData> registerNnb(
            @Query("nnb") String nnb,
            @Query("ua") String ua);

    @GET("api/ua/request/?kind=m")
    Call<UaData> getUa();

    @GET("api/ua/request/?kind=p")
    Call<UaData> getUaPc();

    @FormUrlEncoded
    @POST("api/product/nplace/frequent/response/")
    Call<BaseData> registerPlaceFinish(
            @Field("userid") String userid,
            @Field("uid") String uid,
            @Field("keyword") String keyword,
            @Field("company") String company,
            @Field("url") String url,
            @Field("agency") String agency,
            @Field("account") String account,
            @Field("ranking") String ranking);

    @FormUrlEncoded
    @POST("api/product/ranking/response/")
    Call<BaseData> registerRank(
            @Field("worker") String worker,
            @Field("category") String category,
            @Field("uid") String uid,
            @Field("mid1") String mid1,
            @Field("mid2") String mid2,
            @Field("rank1_page") int rank1Page,
            @Field("rank1_grade") int rank1Grade,
            @Field("rank2_page") int rank2Page,
            @Field("rank2_grade") int rank2Grade);

    @GET("v1/device_ip")
    Call<DeviceIpData> getDeviceIp();

    @FormUrlEncoded
    @POST("v1/mobile/{loginId}")
    Call<DeviceInfoData> getDeviceInfo(
            @Path("loginId") String loginId,
            @Field("imei") String imei);

    @FormUrlEncoded
    @POST("/main/api/devices/checkip/")
    Call<BaseData> checkIp(
            @Field("loginid") String loginId,
            @Field("imei") String imei);

    @FormUrlEncoded
    @POST("v1/mobile/devices")
    Call<DeviceInfoData> registerDevice(
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("version_code") String version,
            @Field("gms_version") String gmsVersion,
            @Field("webview_version") String webviewVersion,
            @Field("updater_version") String updaterVersion,
            @Field("model") String model,
            @Field("telecom") String telecom,
            @Field("battery") int battery,
            @Field("battery_health") int batteryHealth);

    @FormUrlEncoded
    @POST("v1/mobile/keywords/naver/{loginId}")
    Call<KeywordData> getKeywords(
            @Path("loginId") String loginId,
            @Field("imei") String imei,
            @Field("uaId") int uaId);

    @FormUrlEncoded
    @POST("v1/mobile/keyword/{keywordId}/finish")
    Call<BaseData> registerFinish(
            @Path("keywordId") int keywordId,
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("work_id") int workId,
            @Field("result") int result,
            @Field("work_code") int workCode);

    @FormUrlEncoded
    @POST("v1/mobile/keyword/naver/place/bookmark/check")
    Call<BookmarkData> checkBookmark(
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("account_auth_id") int accountAuthId);

    @FormUrlEncoded
    @POST("v1/mobile/keyword/naver/place/bookmark/check")
    Call<BookmarkData> checkBookmark(
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("nid_aut") String nidAut);

    @FormUrlEncoded
    @POST("v1/mobile/keyword/naver/place/bookmark/finish")
    Call<BaseData> registerBookmarkFinish(
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("bookmark_id") int bookmarkId,
            @Field("result") int result);

    @FormUrlEncoded
    @POST("v1/mobile/keyword/naver/place/bookmark/finish")
    Call<BaseData> registerBookmarkFinish(
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("bookmark_id") int bookmarkId,
            @Field("result") int result,
            @Field("work_code") int workCode);

    @FormUrlEncoded
    @POST("v1/mobile/accounts/naver/{accountId}/status")
    Call<BaseData> updateNaverAccountStatus(
            @Path("accountId") String accountId,
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("status") int status);

    @FormUrlEncoded
    @POST("v1/mobile/accounts/naver/get")
    Call<AccountData> getNaverAccount(
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("traffic_id") int trafficId,
            @Field("keyword_id") int keywordId);

    @FormUrlEncoded
    @POST("v1/mobile/accounts/naver/auth/login")
    Call<AccountData> getNaverAuthAccount(
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("direct") int direct);

    @FormUrlEncoded
    @POST("v1/mobile/accounts/naver/auth/login/cookie")
    Call<LoginCookieData> registerNaverAuthAccountCookie(
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("naver_id") String naverId,
            @Field("data") String data,
            @Field("ua") String ua);

    @FormUrlEncoded
    @POST("v1/mobile/keywords/naver/rank_check")
    Call<KeywordData> getKeywordsForRankCheck(
            @Field("login_id") String loginId,
            @Field("imei") String imei);

    @FormUrlEncoded
    @POST("v1/mobile/keyword/naver/{keywordId}/rank")
    Call<BaseData> updateKeywordRank(
            @Path("keywordId") int keywordId,
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("rank") int rank,
            @Field("sub_rank") int subRank);

    @FormUrlEncoded
    @POST("v1/mobile/keyword/naver/{keywordId}/product_info")
    Call<BaseData> updateProductInfo(
            @Path("keywordId") int keywordId,
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("product_name") String productName,
            @Field("store_name") String storeName,
            @Field("mall_id") String mallId,
            @Field("cat_id") String catId,
            @Field("product_url") String productUrl,
            @Field("source_type") String sourceType,
            @Field("source_url") String sourceUrl);

    @FormUrlEncoded
    @POST("v1/mobile/keyword/naver/{keywordId}/rank")
    Call<BaseData> registerNaverKeywordRank(
            @Path("keywordId") int keywordId,
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("rank") int rank);

    @FormUrlEncoded
    @POST("v1/mobile/keyword/naver/{keywordId}/finish")
    Call<BaseData> finishNaverKeyword(
            @Path("keywordId") int keywordId,
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("traffic_id") int trafficId);

    @FormUrlEncoded
    @POST("v1/mobile/data/ua")
    Call<NnbData> getUa(
            @Field("login_id") String loginId,
            @Field("imei") String imei);

    @FormUrlEncoded
    @POST("v1/mobile/data/ua?type=p")
    Call<NnbData> getUaPc(
            @Field("login_id") String loginId,
            @Field("imei") String imei);

    @FormUrlEncoded
    @POST("v1/mobile/data/naver/nnbua")
    Call<NnbData> getNnb(
            @Field("login_id") String loginId,
            @Field("imei") String imei);

    @FormUrlEncoded
    @POST("v1/mobile/data/naver/nnbua?type=p")
    Call<NnbData> getNnbPc(
            @Field("login_id") String loginId,
            @Field("imei") String imei);

    @FormUrlEncoded
    @POST("v1/mobile/data/naver")
    Call<BaseData> registerNaverData(
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("nnb") String nnb,
            @Field("ua") String ua);

    @FormUrlEncoded
    @POST("v1/mobile/data/naver")
    Call<BaseData> registerNaverData(
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("nnb") String nnb,
            @Field("ua") String ua,
            @Field("use") boolean use);

    @FormUrlEncoded
    @POST("v1/mobile/data/naver/cookie/others")
    Call<BaseData> registerNaverCookieOthers(
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("cookie_id") long cookieId,
            @Field("others") String others);

    @FormUrlEncoded
    @POST("v1/mobile/accounts/naver/cookie/check/update")
    Call<BaseData> registerNaverCookieStatus(
            @Field("login_id") String loginId,
            @Field("imei") String imei,
            @Field("cookie_id") long loginCookieId,
            @Field("status") int status);
}
