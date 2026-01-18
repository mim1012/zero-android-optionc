package com.sec.android.app.sbrowser.library.naver.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ShoppingSearchData {
    public SearchAdResult searchAdResult;
    public QueryValidateResult queryValidateResult;
    public ShoppingResult shoppingResult;
    public MultiModalSasResult multiModalSasResult;
    public boolean appliedSmartPriceSort;
    public Object searchBanInfo;
    public SearchParam searchParam;
    public Object mallShortCut;
    public ArrayList<Object> surls;
    public NClick nClick;

    public class SearchAdResult {
        public AdMeta adMeta;
        public ArrayList<Product> products;
        public boolean adExpandedQuery;
        public String adExpandedDepth;
    }

    public class QueryValidateResult {
        public String status;
        public boolean isAdultQuery;
    }

    public class ShoppingResult {
        public Status status;
        public String query;
        public String stopwordQuery;
        public String strQueryType;
        public ArrayList<NluTerm> nluTerms;
        public String brandNo;
        public String makerNo;
        public String mallNo;
        public String isModelNameSearch;
        public String queryProperty;
        public String start;
        public int productCount;
        public int total;
        public int orgQueryTotal;
        public int termCount;
        public ArrayList<String> terms;
        public int intersectionTermCount;
        public ArrayList<Object> intersectionTerms;
        public int exclusionTermCount;
        public ArrayList<Object> exclusionTerms;
        public String searchTime;
        public String switch_version_bt_on;
        public String qlty_imp_version;
        public int partialSearch;
        public int qrSearch;
        public int deepSearch;
        public String preferTag;
        public PriceRecognize priceRecognize;
        public Nlu nlu;
        public Cmp cmp;
        public CmpOrg cmpOrg;
        public ArrayList<Product> products;
        public boolean partialSearched;
    }

    public class MultiModalSasResult{
        public ArrayList<Product> products;
        public int total;
    }

    public class SearchParam {
        public String sort;
        public int pagingIndex;
        public int pagingSize;
        public String viewType;
        public String productSet;
        public String query;
        public String origQuery;
        public String adQuery;
        public ArrayList<Object> iq;
        public ArrayList<Object> eq;
        public ArrayList<Object> xq;
        public String timestamp;
    }

    public class NClick {
        public String code;
        public String codeType;
        public String pageUid;
        public String susVal;
        public String query;
    }



    public class AdMeta {
        public ArrayList<Page> pages;
    }

    public class Category {
        public String id;
        public String name;
        public String relevance;
    }

    public class Category1 {
        public int count;
        public ArrayList<Category> categories;
    }

    public class Category2 {
        public int count;
        public ArrayList<Category> categories;
    }

    public class Category3 {
        public int count;
        public ArrayList<Category> categories;
    }

    public class Category4 {
        public int count;
        public ArrayList<Category> categories;
    }

    public class ChannelInfoCache {
        public String talkAccountId;
    }

    public class Cmp {
        public Category4 category4;
    }

    public class CmpOrg {
        public Category1 category1;
        public Category2 category2;
        public Category3 category3;
        public Category4 category4;
    }

    public class LowMallList {
        public String nvMid;
        public String mallSeq;
        public String mallPid;
        public String price;
        public String chnlSeq;
        public boolean naverPay;
        public String naverPayType;
        public String name;
        public String chnlType;
        public String chnlName;
        public boolean chnlNaverPay;
        public String windowType;
    }

    public class MallInfoCache {
        public String adsrType;
        public String talkAccountId;
        public String npaySellerNo;
        public String seq;
        public String prodCnt;
        public String name;
        public String bizplBaseAddr;
        public String bizplDtlAddr;
        public String businessNo;
        public String onmktRegisterNo;
        public String mallIntroduction;
        public MallLogos mallLogos;
        public String mallGrade;
        public boolean goodService;
        public Object eventScheduledCont;
        public boolean naverPay;
    }

    public class MallLogos {
        @SerializedName("REPRESENTATIVE")
        @Expose
        public String rEPRESENTATIVE;
        @SerializedName("FORYOU")
        @Expose
        public String fORYOU;
        @SerializedName("BASIC")
        @Expose
        public String bASIC;
        @SerializedName("DETAIL")
        @Expose
        public String dETAIL;
        @SerializedName("VARIABLE76X15")
        @Expose
        public String vARIABLE76X15;
        @SerializedName("UNKNOWN")
        @Expose
        public String uNKNOWN;
        @SerializedName("MALL_120X120")
        @Expose
        public String mALL_120X120;
    }

    public class Nlu {
        public String originQuery;
        public String nluQuery;
        public String serviceParam;
        public String searchParam;
    }

    public class NluTerm {
        public String keyword;
        public String type;
    }

    public class Page {
        public PageDetail page;
    }

    public class PageDetail {
        public String no;
    }

    public class PriceRecognize {
        public String prQuery;
        public String minValue;
        public String maxValue;
        public String susaStringCount;
    }

    public class Product {
        public String collection;
        public Object purchaseConditionInfos;
        public String rank;
        public String adId;
        public String id;
        public String parentId;
        public String category4Name;
        public String attributeValueSeqs;
        public String attributeValue;
        public String characterValueSeqs;
        public String characterValue;
        public String productTitle;
        public String mallNo;
        public String mallId;
        public String mallName;
        public String isBrandStore;
        public String isNaverPay;
        public String isMblNaverPay;
        public String nPayPcType;
        public String nPayMblType;
        public String saleTp;
        public String mallInfo;
        public String dlvryCd;
        public String dlvryCont;
        public String fastdlvry;
        public String isAdult;
        public String hasVideo;
        public String deliveryFeeContent;
        public String hasCouponContent;
        public String isHotDeal;
        public String purchaseCnt;
        public String priceUnit;
        public String mobileLowPrice;
        public String mallProductId;
        public String imgColorCd;
        public String lowPrice;
        public String price;
        public String imgSgnt;
        public String checkOutReviewCount;
        public String category4Id;
        public String category2Name;
        public String descriptionOrder;
        public String category2Id;
        public String category1Id;
        public String category3Id;
        public String category1Name;
        public String imgVersion;
        public String prchCondInfo;
        public String brand;
        public String naverPayAccumRto;
        public String imageUrl;
        public String isLgtModelMat;
        public String category3Name;
        public String hasLowestCardPrice;
        public String openDate;
        public String keepCnt;
        public String mobilePrice;
        public String manuTag;
        public String additionalImageCount;
        public String reviewCountSum;
        public String scoreInfo;
        public String maker;
        public String mallCount;
        public String reviewCount;
        public String productName;
        public String originalMallProductId;
        public String prodTp;
        public String mpTp;
        public String overseaTp;
        public String imgSz;
        public String isMisImg;
        public String chnlSeq;
        public String wdTp;
        public String stdPrchCondInfo;
        public String officialCertifiedLowPrice;
        public String dlvryLowPrice;
        public String shopNNo;
        public String buyPointContent;
        public String hasEventContent;
        public String hasBuyPointContent;
        public String hasCardContent;
        public String couponContent;
        public String naverAnalyticsAccountId;
        public String adcrUrl;
        public String naverPayAdAccumulatedType;
        public String naverPayAdAccumulatedValue;
        public String naverPayAdAccumulatedDisplayValue;
        public String adAdditionalDescription;
        public String adAdditionalLongDescription;
        public String adProductInfoEnabled;
        public Object lowMallList;
        public MallInfoCache mallInfoCache;
        public ChannelInfoCache channelInfoCache;
        public String demography;
        public String isAddExps;
        public String diffDeliveryFeeContent;
        public String adProductTitle;
        public String talkTalkAdcrUrl;
        public String videoId;
        public String adImageUrl;
        public String hasAddInFee;
        public String category4NameOrg;
        public String categoryLevel;
        public String makerNo;
        public String brandNo;
        public String series;
        public String seriesNo;
        public String productTitleOrg;
        public String searchKeyword;
        public String dlvryPrice;
        public String lowestCardPrice;
        public String lowestCardName;
        public String mallNameOrg;
        public String mallProductUrl;
        public String mallProdMblUrl;
        public String mallPcUrl;
        public String mallSectionNo;
        public String atmtTag;
        public String preferTag;
        public String smryReview;
        public String lnchYm;
        public String exchangeRateInfo;
        public String wdNm;
        public String comNm;
        public String rentalCont;
        public Object isChnlPnt;
        public String dlvryLowPriceByMallNo;
        public String fastdlvryCont;
        public String lgstDlvryCont;
        public String rmid;
        public String stdPrchOptSeqs;
        public String stdPrchOptNames;
        public String stdPrchOptValSeqs;
        public String stdPrchOptValNames;
        public String stdPrchOptCount;
        public String parentCatalogId;
        public String stdPrchOptVal;
        public String stdPrchOptHit;
        public String rankReviewCount;
        public String stdCatlogMatchType;
        public String stdGroupId;
        public String isAdultExpsRstct;
        public String gdid;
        public String eventContent;
        public String cardContent;
        public String lowPriceByMallNo;
        public String dummy;
        public String crUrl;
    }

    public class Status {
        public String code;
        public String message;
    }

}
