package com.sec.android.app.sbrowser.engine.WebEngine;

import android.text.TextUtils;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketTimeoutException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpSimple {

    private static final String TAG = HttpSimple.class.getSimpleName();

    private OkHttpClient _httpClient = null;
    private String _proxyString = null;

    public HttpSimple() {
    }

    public void setProxyString(String proxyString) {
        _proxyString = proxyString;
    }

    public String get(String url) {
        OkHttpClient client = getClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        String body = null;

        try {
            final long startMillis = System.currentTimeMillis();
            Response response = client.newCall(request).execute();
            final long dtMillis = System.currentTimeMillis() - startMillis;
            Log.d(TAG, "Got response: after " + dtMillis + "ms");

            if (response.body() != null) {
                body = response.body().string();
            }
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "SocketTimeoutException: " + e);
            e.printStackTrace();
//            SystemClock.sleep(5000);
//            return loadWithOkHttpClient(webView, request, url, recursiveCount - 1);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e);
            e.printStackTrace();
//            SystemClock.sleep(5000);
//            return loadWithOkHttpClient(webView, request, url, recursiveCount - 1);
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return body;
    }

    private OkHttpClient getClient() {
        if (_httpClient == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
//                    .followRedirects(true)
//                    .followSslRedirects(true)
//                    .connectTimeout(20, TimeUnit.SECONDS)
//                    .writeTimeout(20, TimeUnit.SECONDS)
//                    .readTimeout(20, TimeUnit.SECONDS)
                    .addNetworkInterceptor(loggingInterceptor);

            if (!TextUtils.isEmpty(_proxyString)) {
                String[] proxyParts = _proxyString.split(":");

                if (proxyParts.length >= 2) {
                    String proxyHost = proxyParts[0];
                    int proxyPort = Integer.parseInt(proxyParts[1]);
                    Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
                    builder.proxy(proxy);

                    if (proxyParts.length >= 4) {
                        Authenticator.setDefault(new Authenticator() {
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication() {
                                if (getRequestingHost().equalsIgnoreCase(proxyHost)) {
                                    if (proxyPort == getRequestingPort()) {
                                        return new PasswordAuthentication(proxyParts[2], proxyParts[3].toCharArray());
                                    }
                                }

                                return null;
                            }
                        });
                    }
                }
            }

            _httpClient = builder.build();
        }

        return _httpClient;
    }
}
