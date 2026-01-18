package com.sec.android.app.sbrowser.engine;

public class DnsParser {

    public static String getDomain(String hostname) {
        String[] dnsNames = {
                ".com", ".org", ".net", ".int", ".edu", ".gov", ".mil",
                ".arpa",
                ".github.io", ".io",
                ".app", ".biz", ".book", ".dev", ".info", ".navy",
                ".co.kr", ".ne.kr", ".or.kr", ".re.kr", ".pe.kr", ".go.kr", ".mil.kr", ".ac.kr", ".kr",
        };

        for (String dns : dnsNames) {
            if (hostname.endsWith(dns)) {
                int dnsIndex = hostname.lastIndexOf(dns);
                int index = hostname.lastIndexOf('.', dnsIndex - 1);

                return hostname.substring(index + 1);
            }
        }

        return null;
    }
}
