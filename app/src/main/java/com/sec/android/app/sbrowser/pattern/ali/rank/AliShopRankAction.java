package com.sec.android.app.sbrowser.pattern.ali.rank;

import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.js.JsApi;
import com.sec.android.app.sbrowser.pattern.js.JsQuery;

public class AliShopRankAction extends AliRankAction {

    private static final String TAG = AliShopRankAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = TAG;

    public AliShopRankAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsQuery = new AliShopRankJsQuery(JS_INTERFACE_NAME);
        _jsInterface = new AliShopHtmlJsInterface(_jsApi);
        _jsApi.register(_jsInterface);
    }

    public boolean checkRank(String url) {
        Log.d(TAG, "- 순위 검사");

        _jsInterface.reset();
        _jsApi.postQuery(((AliShopRankJsQuery) _jsQuery).getRankQuery(url));
        threadWait();

        Integer rank = ((AliShopHtmlJsInterface) _jsInterface).getRank();

        if (rank == null) {
            return false;
        }

        if (rank > 0) {
            _rank = rank;
        }

        return true;
    }

    public String getProductName(String pid) {
//        String text = getCurrentTextOnly(".product_btn_link__ArGCa.linkAnchor[data-i=\"" + mid1 + "\"] .product_info_tit__c5_pb");
        String text = getCurrentTextOnlyFromParent(".fm_h:not(:has(.fm_fp))[href*=\"" + pid + "\"]", ".fm_v");

        if (text != null) {
            String[] splatted = text.split("\\n");
            text = splatted[0];
        }

        return text;
    }

    private class AliShopRankJsQuery extends JsQuery {

        public AliShopRankJsQuery(String jsInterfaceName) {
            super(jsInterfaceName);
        }

        public String getRankQuery(String code) {
            String selectorsLeft = ".av_ay .fm_fn";
            String selectorsRight = ".av_az .fm_fn";
            String selectorNotAd = "a:not(:has(.fm_fp))";

            String query = "let list1 = nodeList;"
                    + "let list2 = document.querySelectorAll('" + selectorsRight + "');"
                    + "let selNotAd = '" + selectorNotAd + "';"
                    + "let findCode = '" + code + "';"
                    + "let totalLength = list1.length > list2.length ? list1.length : list2.length;"
                    + "let totalNotAdLength = document.querySelectorAll('" + selectorsLeft + " " + selectorNotAd + "').length + document.querySelectorAll('" + selectorsRight + " " + selectorNotAd + "').length;"
                    + "let rank = 0;"
                    + "let j = 1;"
                    + "for (let i = 0; i < totalLength; ++i) {"
                    + "  if (i < list1.length) {"
                    + "    let itemList = list1[i].querySelectorAll(selNotAd);"
                    + "    if (itemList.length > 0) {"
                    + "      if (itemList[0].href.includes(findCode)) {"
                    + "        rank = j;"
                    + "        break;"
                    + "      } else {"
                    + "        ++j;"
                    + "      }"
                    + "    }"
                    + "  }"
                    + "  if (i < list2.length) {"
                    + "    let itemList = list2[i].querySelectorAll(selNotAd);"
                    + "    if (itemList.length > 0) {"
                    + "      if (itemList[0].href.includes(findCode)) {"
                    + "        rank = j;"
                    + "        break;"
                    + "      } else {"
                    + "        ++j;"
                    + "      }"
                    + "    }"
                    + "  }"
                    + "}"
                    + getJsInterfaceQuery("getRank", "rank, totalNotAdLength");

            return wrapJsFunction(getValidateNodeQuery(selectorsLeft, query));
        }
    }

    protected class AliShopHtmlJsInterface extends RankHtmlJsInterface {

        public AliShopHtmlJsInterface(JsApi jsApi) {
            super(jsApi);
        }
    }
}
