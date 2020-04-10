package com.yc.spider.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/*
解决 javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path buildin
https证书安全问题
 */

public class TrustSSL {
    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }


    public static Document NingBoDaxUE(String listUrls) throws Exception {
        URL console = new URL(listUrls);
        HttpURLConnection conn = (HttpURLConnection) console.openConnection();
        if (conn instanceof HttpsURLConnection)  {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
            ((HttpsURLConnection) conn).setSSLSocketFactory(sc.getSocketFactory());
            ((HttpsURLConnection) conn).setHostnameVerifier(new TrustAnyHostnameVerifier());
        }
        // 指定客户端能够接收的内容类型
        conn.setRequestProperty("accept", "*/*");

        // 设置连接的状态为长连接
        conn.setRequestProperty("Connection", "keep-alive");

        // 设置发送请求的客户机系统信息
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        // 设置其他属性
        conn.setConnectTimeout(3000);// 设置连接超时时间

//        conn.setDoOutput(true);
//        conn.setDoInput(true);

        conn.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        String line = null;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
//        System.out.println(sb);
        Document document = Jsoup.parse(sb.toString());
//        System.out.println(document);
        return document;
    }
}
