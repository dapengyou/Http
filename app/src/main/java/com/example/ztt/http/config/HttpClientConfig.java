package com.example.ztt.http.config;


import org.apache.http.conn.ssl.SSLSocketFactory;

/**
 * Created by ztt on 16/5/8.
 */
public class HttpClientConfig extends HttpConfig {
    private static HttpClientConfig sConfig = new HttpClientConfig();
    SSLSocketFactory mSslSocketFactory;

    private HttpClientConfig() {

    }

    public static HttpClientConfig getConfig() {
        return sConfig;
    }

    /**
     * 配置https请求的SSLSocketFactory与HostnameVerifier
     *
     * @param sslSocketFactory
     */
    public void setHttpsConfig(SSLSocketFactory sslSocketFactory) {
        mSslSocketFactory = sslSocketFactory;
    }

    public org.apache.http.conn.ssl.SSLSocketFactory getSocketFactory() {
        return mSslSocketFactory;
    }
}


