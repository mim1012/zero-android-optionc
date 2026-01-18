package com.sec.android.app.sbrowser.pattern.ali.rank;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.HtmlJsInterface;
import com.sec.android.app.sbrowser.pattern.js.JsApi;

public class AliRankAction extends BasePatternAction {

    private static final String TAG = AliRankAction.class.getSimpleName();

    protected int _nodeCount = 0;
    protected int _rank = -1;

    public AliRankAction(String jsInterfaceName, WebView webView) {
        super(jsInterfaceName, webView);
    }

    public int getNodeCount() {
        return _nodeCount;
    }

    public int getRank() {
        return _rank;
    }

    protected class RankHtmlJsInterface extends HtmlJsInterface {

        private Integer _rank = null;

        public RankHtmlJsInterface(JsApi jsApi) {
            super(jsApi);
        }

        @Override
        public void reset() {
            super.reset();

            _rank = null;
        }

        public Integer getRank() {
            return _rank;
        }

        @JavascriptInterface
        public void getRank(int rank, int nodeCount) {
            Log.d(TAG, "rank: " + rank + ", nodes: " + nodeCount);
            _nodeCount = nodeCount;
            _rank = rank;
            _jsApi.callbackOnSuccess(_rank);
        }
    }
}
