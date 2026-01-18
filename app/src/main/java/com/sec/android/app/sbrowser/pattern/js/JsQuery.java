package com.sec.android.app.sbrowser.pattern.js;

import java.util.Locale;

public class JsQuery {

    private static final String TAG = JsQuery.class.getSimpleName();

    private final String _jsInterfaceName;

    public JsQuery(String jsInterfaceName) {
        _jsInterfaceName = jsInterfaceName;
    }

    public String printHtml() {
        return wrapJs(getJsInterfaceQuery("printHtml",
                "document.getElementsByTagName('html')[0].outerHTML"));
    }

    public String getHtml() {
        return wrapJs(getJsInterfaceQuery("readHtml",
                "document.getElementsByTagName('html')[0].outerHTML"));
    }

    public String getLocalStorageStringQuery() {
        return wrapJs(getJsInterfaceQuery("readLocalStorageString",
                "JSON.stringify(window.localStorage)"));
    }

    public String getLocalStorageValueQuery(String key) {
        return wrapJs(getJsInterfaceQuery("readLocalStorageValue",
                "window.localStorage.getItem('" + key + "')"));
    }

    public String getValueFromScript(String script) {
        String query = "var result = (function() {" + script + "})();"
                + "console.log(result);"
                + getJsInterfaceQuery("readValue", "result");

        return wrapJsFunction(query);
    }

    public String getSystemValue(String valuePath) {
        return wrapJsFunction(getJsInterfaceQuery("readValue", valuePath));
    }

    public String getJsonSystemValue(String valuePath) {
        String query = "var value = JSON.stringify(" + valuePath + ");"
//                + "console.log(value);"
                + getJsInterfaceQuery("readJson", "value");

        return wrapJsFunction(query);
    }

    // 쿼리 빌더 부분. 최적화를 위해 나중에 분리한다.
    // 이것이 왠지 인터페이스로 가야할것 처럼 보인다. 이뉴는 사실상 여기서는 function 을 알수 없다.
    // 아직은 좋은 구조가 아님.
    public String getWindowSizeQuery() {
        return wrapJs(getJsInterfaceQuery("readWindowSize",
                "window.innerWidth, window.innerHeight"));
    }

    public String getInnerTextQuery(String selectors) {
        String query = getJsInterfaceQuery("readInnerText",
                "nodeList[0].innerText");

        return wrapJsFunction(getValidateNodeQuery(selectors, query));
    }

    public String getInnerTextFromParentQuery(String childSelectors, String selectors) {
        String selectorsWrap = "'" + selectors + "'";
        String query = "var list = nodeList;"
                + "var name = '';"
                + "for (var childObj of list) {"
                + " var findList = childObj.parentNode.querySelectorAll(" + selectorsWrap + ");"
                + " for (var obj of findList) {"
                + "  name = obj.innerText;"
                + "  break;"
                + " }"
                + "}"
                + getJsInterfaceQuery("readInnerText", "name");

        return wrapJsFunction(getValidateNodeQuery(childSelectors, query));
    }

    public String getCurrentTextOnlyQuery(String selectors) {
//        String query = getJsInterfaceQuery("readCurrentTextOnly", "nodeList[0].childNodes[0].nodeValue");
        String query = "var list = nodeList;"
                + "var name = '';"
                + "for (var obj of list) {"
                + " for (var child of obj.childNodes) {"
                + "  if (child.nodeType == 3) {"
                + "   name = child.nodeValue;"
                + "   break;"
                + "  }"
                + " }"
                + "}"
                + getJsInterfaceQuery("readCurrentTextOnly", "name");

        return wrapJsFunction(getValidateNodeQuery(selectors, query));
    }

    public String getCurrentTextOnlyFromParentQuery(String childSelectors, String selectors) {
        String selectorsWrap = "'" + selectors + "'";
        String query = "var list = nodeList;"
                + "var name = '';"
                + "for (var childObj of list) {"
                + " var findList = childObj.parentNode.querySelectorAll(" + selectorsWrap + ");"
                + " for (var obj of findList) {"
                + "  for (var child of obj.childNodes) {"
                + "   if (child.nodeType == 3) {"
                + "    name = child.nodeValue;"
                + "    break;"
                + "   }"
                + "  }"
                + " }"
                + "}"
                + getJsInterfaceQuery("readCurrentTextOnly", "name");

        return wrapJsFunction(getValidateNodeQuery(childSelectors, query));
    }

    public String getCheckInsideQuery(String selectors) {
        return getCheckInsideQuery(selectors, 0, 0);
    }

    protected String getCheckInsideQueryForNaverSearch(String nodeQuery) {
        return getCheckInsideQuery(nodeQuery, 120, 0);   // 120 은 상단 검색창 및 정보바를 포함한 영역 크기이다.
    }

    public String getCheckInsideQuery(String selectors, int topOffset, int bottomOffset) {
        String query = "var rect = nodeList[0].getBoundingClientRect();"
                + "var inside = 0;"
                + "if (rect.top < " + topOffset + ") { inside = -1; }"
                + "else if (rect.bottom > (window.innerHeight - " + bottomOffset + ")) { inside = 1; }"
                + getJsInterfaceQuery("checkInside", "inside, rect.left, rect.top, rect.right, rect.bottom");

        return wrapJsFunction(getValidateNodeQuery(selectors, query));
    }

    public String getCheckInsideQuery(String selectors, int topOffset, int bottomOffset, int index) {
        String selectorsWrap = "'" + selectors + "'";
        String query = "var rect = nodeList[" + index + "].getBoundingClientRect();"
                + "var inside = 0;"
                + "if (rect.top < " + topOffset + ") { inside = -1; }"
                + "else if (rect.bottom > (window.innerHeight - " + bottomOffset + ")) { inside = 1; }"
                + getJsInterfaceQuery("checkInside", "inside, rect.left, rect.top, rect.right, rect.bottom");

        // 인덱스가 잘못되었을때. 함수를 굳이 분리한 이유는 node 는 있지만 인덱스 오류때문에 모두 실패로 되므로 별도로 처리한다.
        query = "if (nodeList.length <= " + index + ") {"
                + getJsInterfaceQuery("undefinedNode", selectorsWrap)
                + "return;"
                + "}"
                + query;

        return wrapJsFunction(getValidateNodeQuery(selectors, query));
    }

    public String getNodeCount(String selectors) {
        String selectorsWrap = "'" + selectors + "'";
        String query = "var count = document.querySelectorAll(" + selectorsWrap + ").length;"
                + getJsInterfaceQuery("readNodeCount", "count");

        return wrapJsFunction(query);
    }

    public String getRandomValue(String selectors, String valueAttribute) {
        String query = "var list = nodeList;"
                + "var min = 0;"
                + "var max = list.length;"
                + "var i = Math.floor(Math.random() * (max - min)) + min;"
                + "var value = list[i].getAttribute('" + valueAttribute + "');"
                + getJsInterfaceQuery("readRandomValue", "value");

        return wrapJsFunction(getValidateNodeQuery(selectors, query));
    }

    public String getValue(String selector, String valueAttribute) {
        String query = "var list = nodeList;"
                + "var value = null;"
                + "if (list.length > 0) {"
                + "value = list[0].getAttribute('" + valueAttribute + "');"
                + "}"
                + getJsInterfaceQuery("readValue", "value");

        return wrapJsFunction(getValidateNodeQuery(selector, query));
    }

    public String setValue(String selector, String valueAttribute, String value) {
        String query = "var list = nodeList;"
                + "if (list.length > 0) {"
                + "list[0].setAttribute('" + valueAttribute + "','" + value + "');"
                + "}"
                + getJsInterfaceQuery("writeValue");

        return wrapJsFunction(getValidateNodeQuery(selector, query));
    }

    public String clickUrl(String selector) {
        String query = "var list = nodeList;"
                + "if (list.length > 0) {"
                + "list[0].click();"
                + "}"
                + getJsInterfaceQuery("clickUrl");

        return wrapJsFunction(getValidateNodeQuery(selector, query));
    }

    public String scrollToBottom() {
//            String query = "$(document).scrollTop($(document).height())";
        String query = "window.scrollTo(0, document.body.scrollHeight);"
                + getJsInterfaceQuery("scrollToBottom");

        return wrapJsFunction(query);
    }

    public String scrollToBottom(int offset) {
//            String query = "$(document).scrollTop($(document).height())";
        String query = "window.scrollTo(0, document.body.scrollHeight - " + offset + ");"
                + getJsInterfaceQuery("scrollToBottom");

        return wrapJsFunction(query);
    }



    // 기본 쿼리 빌더.
    public String wrapJs(String query) {
        return "javascript:" + query;
    }

    public String wrapJsFunction(String query) {
        return "javascript:(function() {" + query + "})();";
    }

    public String getJsInterfaceQuery(String funcName, String param) {
        return String.format(Locale.getDefault(), "window.%s.%s(%s);",
                _jsInterfaceName, funcName, param);
    }

    public String getJsInterfaceQuery(String funcName) {
        return String.format(Locale.getDefault(), "window.%s.%s();",
                _jsInterfaceName, funcName);
    }

    // CSS 셀렉터 인지 검증.
    public boolean validateCssSelector(String selectors) {
        // 검증로직 만들어야함.
        // ' 이 포함되어있다면 \' 로 치환해준다거나.. 등등등.
        return true;
    }

    public String getValidateNodeQuery(String selectors, String query) {
        String selectorsWrap = "'" + selectors + "'";
        String resultQuery;

        if (validateCssSelector(selectors)) {
            resultQuery = "var nodeList = document.querySelectorAll(" + selectorsWrap + ");"
                    + "if (nodeList.length <= 0) {"
                    + getJsInterfaceQuery("undefinedNode", selectorsWrap)
                    + "return;"
                    + "}"
                    + query;
        } else {
            // css 셀렉터 쿼리가 아니면 오류를 리턴하게 해준다.
            resultQuery = getJsInterfaceQuery("undefinedNode", selectorsWrap)
                    + "return;";
        }

        return resultQuery;
    }
}
