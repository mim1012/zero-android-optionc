package com.sec.android.app.sbrowser.engine.WebEngine.Headers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ClientHints {

    protected String _ua = null;

    public void setUserAgent(String userAgent) {
        _ua = userAgent;
    }

    public String getVersion(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }

    public abstract UserAgentClientHints getSecChUa(UserAgentClientHints hints, String chromeVersion);

    // 특수 브라우저 지원용.
    public UserAgentClientHints getSecChUa(UserAgentClientHints hints, String chromeVersion, String version) {
        return null;
    }
}
