package com.zero.traffic.engine;

/**
 * ★ Stealth 설정 공유 상수
 *
 * ActionExecutor와 TrafficService 양쪽에서 참조하는 봇 감지 우회 JS.
 * 단일 소스로 관리하여 동기화 이슈 방지.
 */
public final class StealthConfig {

    private StealthConfig() {} // 인스턴스화 방지

    /**
     * Enhanced Stealth JS — 모든 페이지 로드 시 주입
     *
     * 1. navigator.webdriver 플래그 제거
     * 2. navigator.plugins 위장
     * 3. navigator.languages 한국어 설정
     * 4. window.chrome 객체 완성
     * 5. Permissions API 우회
     * 6. Canvas fingerprint noise (nfront 핑거프린팅 교란)
     * 7. WebGL vendor/renderer (Galaxy S7 Adreno 530)
     * 8. 하드웨어 정보 일관성 (hardwareConcurrency, deviceMemory)
     * 9. 네트워크 연결 정보 (4G)
     * 10. Automation 흔적 제거 (Selenium/Puppeteer/Phantom)
     */
    /** MobileHeaderConfig 기반 동적 Stealth JS 생성 */
    public static String buildStealthJS(String chromeVersion, String chromeFullVersion, String deviceModel) {
        return
            "(function(){" +
            "try{Object.defineProperty(Navigator.prototype,'webdriver',{get:function(){return false},configurable:true});}catch(e){}" +
            "try{Object.defineProperty(Navigator.prototype,'languages',{get:function(){return['ko-KR','ko','en-US','en']},configurable:true});}catch(e){}" +
            "if(!window.chrome)window.chrome={runtime:{},loadTimes:function(){},csi:function(){}};" +
            "if(window.chrome&&!window.chrome.app)window.chrome.app={isInstalled:false,getDetails:function(){},getIsInstalled:function(){},installState:function(){}};" +
            "var op=window.navigator.permissions;if(op&&op.query){var oq=op.query.bind(op);" +
            "op.query=function(p){return p.name==='notifications'?" +
            "Promise.resolve({state:Notification.permission}):oq(p);}}" +
            "try{Object.defineProperty(navigator,'userAgentData',{get:function(){return{" +
            "brands:[{brand:'Not A(Brand',version:'99'},{brand:'Google Chrome',version:'" + chromeVersion + "'},{brand:'Chromium',version:'" + chromeVersion + "'}]," +
            "mobile:true,platform:'Android'," +
            "getHighEntropyValues:function(h){return Promise.resolve({" +
            "brands:[{brand:'Not A(Brand',version:'99.0.0.0'},{brand:'Google Chrome',version:'" + chromeFullVersion + "'},{brand:'Chromium',version:'" + chromeFullVersion + "'}]," +
            "mobile:true,platform:'Android',platformVersion:'14.0'," +
            "architecture:'',bitness:'64',model:'" + deviceModel + "',uaFullVersion:'" + chromeFullVersion + "'," +
            "fullVersionList:[{brand:'Not A(Brand',version:'99.0.0.0'},{brand:'Google Chrome',version:'" + chromeFullVersion + "'},{brand:'Chromium',version:'" + chromeFullVersion + "'}]" +
            "});}" +
            "},configurable:true});}catch(e){}" +
            "})();";
    }

    public static final String STEALTH_JS =
        "(function(){" +
        // 1. webdriver — WebView는 기본 true 노출 → false로 덮어씌우기
        "try{Object.defineProperty(Navigator.prototype,'webdriver',{get:function(){return false},configurable:true});}catch(e){}" +
        // 2. languages — 한국어 고정
        "try{Object.defineProperty(Navigator.prototype,'languages',{get:function(){return['ko-KR','ko','en-US','en']},configurable:true});}catch(e){}" +
        // 3. chrome 객체 — WebView에 없음, nfront가 존재 여부 체크
        "if(!window.chrome)window.chrome={runtime:{},loadTimes:function(){},csi:function(){}};" +
        "if(window.chrome&&!window.chrome.app)window.chrome.app={isInstalled:false,getDetails:function(){},getIsInstalled:function(){},installState:function(){}};" +
        // 4. permissions query 우회
        "var op=window.navigator.permissions;if(op&&op.query){var oq=op.query.bind(op);" +
        "op.query=function(p){return p.name==='notifications'?" +
        "Promise.resolve({state:Notification.permission}):oq(p);}}" +
        // 5. userAgentData (Client Hints) — WebView 미지원, nfront Sec-CH-UA 검증용
        "try{Object.defineProperty(navigator,'userAgentData',{get:function(){return{" +
        "brands:[{brand:'Not A(Brand',version:'99'},{brand:'Google Chrome',version:'131'},{brand:'Chromium',version:'131'}]," +
        "mobile:true,platform:'Android'," +
        "getHighEntropyValues:function(h){return Promise.resolve({" +
        "brands:[{brand:'Not A(Brand',version:'99.0.0.0'},{brand:'Google Chrome',version:'131.0.6778.200'},{brand:'Chromium',version:'131.0.6778.200'}]," +
        "mobile:true,platform:'Android',platformVersion:'14.0'," +
        "architecture:'',bitness:'64',model:'SM-G977N',uaFullVersion:'131.0.6778.200'," +
        "fullVersionList:[{brand:'Not A(Brand',version:'99.0.0.0'},{brand:'Google Chrome',version:'131.0.6778.200'},{brand:'Chromium',version:'131.0.6778.200'}]" +
        "});}" +
        "},configurable:true});}catch(e){}" +
        "})();";
}
