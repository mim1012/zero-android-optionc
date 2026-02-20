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
    public static final String STEALTH_JS =
        "(function(){" +
        // 1. ★ webdriver — Navigator.prototype에서 오버라이드 (configurable:true 필수)
        // 인스턴스(navigator)에서는 non-configurable이라 실패 → prototype으로 우회
        "try{Object.defineProperty(Navigator.prototype,'webdriver',{get:function(){return false},configurable:true});}catch(e){}" +
        // 2. plugins/languages — 동일하게 prototype 방식 + try-catch
        "try{Object.defineProperty(Navigator.prototype,'plugins',{get:function(){return[1,2,3,4,5]},configurable:true});}catch(e){}" +
        "try{Object.defineProperty(Navigator.prototype,'languages',{get:function(){return['ko-KR','ko','en-US','en']},configurable:true});}catch(e){}" +
        // 3. chrome 객체
        "if(!window.chrome)window.chrome={runtime:{},loadTimes:function(){},csi:function(){}};" +
        "if(window.chrome&&!window.chrome.app)window.chrome.app={isInstalled:false,getDetails:function(){},getIsInstalled:function(){},installState:function(){}};" +
        // 4. permissions query 우회
        "var op=window.navigator.permissions;if(op&&op.query){var oq=op.query.bind(op);" +
        "op.query=function(p){return p.name==='notifications'?" +
        "Promise.resolve({state:Notification.permission}):oq(p);}}" +
        // 5. ★ Canvas fingerprint noise
        "var _toBlob=HTMLCanvasElement.prototype.toBlob;" +
        "var _toDataURL=HTMLCanvasElement.prototype.toDataURL;" +
        "HTMLCanvasElement.prototype.toBlob=function(){" +
        "var c=this.getContext('2d');if(c){var s=c.fillStyle;" +
        "c.fillStyle='rgba('+(Math.random()*10|0)+','+(Math.random()*10|0)+','+(Math.random()*10|0)+',0.01)';" +
        "c.fillRect(0,0,1,1);c.fillStyle=s;}return _toBlob.apply(this,arguments);};" +
        "HTMLCanvasElement.prototype.toDataURL=function(){" +
        "var c=this.getContext('2d');if(c){var s=c.fillStyle;" +
        "c.fillStyle='rgba('+(Math.random()*10|0)+','+(Math.random()*10|0)+','+(Math.random()*10|0)+',0.01)';" +
        "c.fillRect(0,0,1,1);c.fillStyle=s;}return _toDataURL.apply(this,arguments);};" +
        // 6. ★ WebGL vendor/renderer (S7 Adreno 530)
        "try{var _gp=WebGLRenderingContext.prototype.getParameter;" +
        "WebGLRenderingContext.prototype.getParameter=function(p){" +
        "if(p===37445)return 'Google Inc. (Qualcomm)';" +
        "if(p===37446)return 'ANGLE (Qualcomm, Adreno (TM) 530, OpenGL ES 3.2)';" +
        "return _gp.call(this,p);};}catch(e){}" +
        // 7. 하드웨어 정보
        "try{Object.defineProperty(Navigator.prototype,'hardwareConcurrency',{get:function(){return 4},configurable:true});}catch(e){}" +
        "try{Object.defineProperty(Navigator.prototype,'deviceMemory',{get:function(){return 4},configurable:true});}catch(e){}" +
        // 8. 네트워크 연결
        "try{if(navigator.connection)Object.defineProperty(navigator.connection,'effectiveType',{get:function(){return'4g'},configurable:true});}catch(e){}" +
        // 9. Automation 흔적 제거
        "delete window.__nightmare;delete window._phantom;delete window.callPhantom;" +
        "delete window._selenium;delete window.__driver_evaluate;delete window.__webdriver_evaluate;" +
        "delete window.__fxdriver_evaluate;delete window.__driver_unwrapped;" +
        "})();";
}
