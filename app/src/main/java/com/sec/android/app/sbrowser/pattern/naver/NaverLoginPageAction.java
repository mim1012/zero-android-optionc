package com.sec.android.app.sbrowser.pattern.naver;

import android.util.Log;
import android.webkit.WebView;

import com.sec.android.app.sbrowser.pattern.BasePatternAction;
import com.sec.android.app.sbrowser.pattern.js.InsideData;

public class NaverLoginPageAction extends BasePatternAction {

    private static final String TAG = NaverLoginPageAction.class.getSimpleName();
    private static final String JS_INTERFACE_NAME = getRandomName(null);
//    private static final String JS_INTERFACE_NAME = "__zlp";

    public static final int BUTTON_LOGIN = 0;
    public static final int BUTTON_PHONE_RADIO = 1;
    public static final int BUTTON_PHONE_OK = 2;

    public static final int ERROR_NONE = 0;

    public NaverLoginPageAction(WebView webView) {
        super(JS_INTERFACE_NAME, webView);

        _jsApi.register(_jsInterface);
    }

    public boolean touchIdInput() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = "#id";
        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget(30);
    }

    public boolean touchPwInput() {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector = "#pw";
        if (!getCheckInside(selector)) {
            return false;
        }

        return touchTarget(30);
    }

    public boolean checkSuccess() {
        return getNodeCount(".id_search .MM_LOGINOUT") > 0;
    }

    public int getError() {
        int error = checkCaptchaError();

        if (error != ERROR_NONE) {
            return error;
        }

        error = checkErrorMessage();

        if (error != ERROR_NONE) {
            return error;
        }

        error = checkIdLockError();

        if (error != ERROR_NONE) {
            return error;
        }

        error = checkProtectionError();

        if (error != ERROR_NONE) {
            return error;
        }

        error = checkPhonePickMessage();

        if (error != ERROR_NONE) {
            return error;
        }

        error = checkMassCreationError();

        if (error != ERROR_NONE) {
            return error;
        }

        return ERROR_NONE;
    }

    // document.querySelectorAll('.login_title_wrap, .warning_box, #error_message, .action_inner, .protection_content, .combine .title')
    private int checkCaptchaError() {
        String innerText = getInnerText(".login_title_wrap");

        if (innerText != null && innerText.length() > 0) {
            if (innerText.contains("자동입력 방지 문자를")) {
                return NaverAccountAction.STATUS_ERROR_CAPTCHA;
            }

            return NaverAccountAction.STATUS_ERROR_UNKNOWN_CAPTCHA;
        }

        return ERROR_NONE;
    }

    private int checkErrorMessage() {
        String innerText = getInnerText("#error_message");

        if (innerText != null && innerText.length() > 0) {
            if (innerText.contains("비밀번호를 잘못 입력했습니다")) {
                return NaverAccountAction.STATUS_ERROR_LOGIN_FAILED;
            }

            return NaverAccountAction.STATUS_ERROR_UNKNOWN_MESSAGE;
        }

        return ERROR_NONE;
    }

    private int checkIdLockError() {
        String innerText = getInnerText(".action_inner");

        if (innerText != null && innerText.length() > 0) {
            if (innerText.contains("아이디를 보호")) {
                return NaverAccountAction.STATUS_ERROR_ID_LOCK;
            }

            return NaverAccountAction.STATUS_ERROR_UNKNOWN_ID;
        }

        return ERROR_NONE;
    }

    private int checkProtectionError() {
        String innerText = getInnerText(".protection_content");

        if (innerText != null && innerText.length() > 0) {
            if (innerText.contains("타인으로 의심되는 로그인 내역")) {
                return NaverAccountAction.STATUS_ERROR_IP_PROTECTION;
            } else if (innerText.contains("회원님의 아이디로 작성된 스팸성 게시물")) {
                return NaverAccountAction.STATUS_ERROR_SPAM_PROTECTION;
            }

            return NaverAccountAction.STATUS_ERROR_UNKNOWN_IP;
        }

        return ERROR_NONE;
    }

    private int checkPhonePickMessage() {
        String innerText = getInnerText(".combine .title");

        if (innerText != null && innerText.length() > 0) {
            if (innerText.contains("회원정보에 사용할 휴대 전화번호를")) {
                return NaverAccountAction.STATUS_ERROR_PHONE_PICK;
            }

            return NaverAccountAction.STATUS_ERROR_UNKNOWN_PHONE;
        }

        return ERROR_NONE;
    }

    private int checkMassCreationError() {
        String innerText = getInnerText(".warning_box");

        if (innerText != null && innerText.length() > 0) {
            if (innerText.contains("대량생성")) {
                return NaverAccountAction.STATUS_ERROR_MASS_CREATION;
            }

            return NaverAccountAction.STATUS_ERROR_UNKNOWN_CREATION;
        }

        return ERROR_NONE;
    }

    public InsideData getPhoneNumberRadioButtonInsideData() {
        return getInsideData(getPhoneNumberRadioButtonSelector());
    }

    public InsideData getPhoneOkButtonInsideData() {
        return getInsideData(getPhoneOkButtonSelector());
    }

    public boolean touchButton(int type) {
        if (!getWebViewWindowSize()) {
            return false;
        }

        String selector;
        int offset = 15;
        int topOffset = 0;

        switch (type) {
            case BUTTON_PHONE_RADIO:
                Log.d(TAG, "전화번호 라디오 버튼 위치 얻기");
                selector = getPhoneNumberRadioButtonSelector();
                break;

            case BUTTON_PHONE_OK:
                Log.d(TAG, "전화번호 확인 버튼 위치 얻기");
                selector = getPhoneOkButtonSelector();
                break;

            case BUTTON_LOGIN:
            default:
                Log.d(TAG, "로그인 버튼 위치 얻기");
                selector = getLoginButtonSelector();
                break;
        }

        if (!getCheckInsideTopOffset(selector, topOffset)) {
            return false;
        }

        return touchTarget(offset);
    }


    private String getLoginButtonSelector() {
        return "#upper_login_btn";
    }

    private String getPhoneNumberRadioButtonSelector() {
        return ".tel_number";
    }

    private String getPhoneOkButtonSelector() {
        return "#btnEnd";
    }
}
