package com.sec.android.app.sbrowser.engine;

public class NaverAccount {

    private static final String TAG = NaverAccount.class.getSimpleName();

    public static final int STATUS_NONE = 0;
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_ERROR = 2;

    public String accountId;
    public String nidAut;
    public long loginCookieId;
    public int status;

    public NaverAccount() {
        accountId = null;
        nidAut = null;
        loginCookieId = -1;
        status = STATUS_NONE;
    }
}
