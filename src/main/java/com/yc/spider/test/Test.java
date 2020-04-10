package com.yc.spider.test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by youmingwei on 17/2/16.
 */
public class Test {

    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }

    public static void main(String... args) throws Exception {

        String listUrl = "http://zs.zstu.edu.cn/zszc.htm";

        Document document = Jsoup.connect(listUrl)
                .get();
//        Document document = getDocument(listUrl);
//        System.out.println(document);

        Elements contentE = document.select("body > div > div.mains > div.mainsright > ul > li");
        System.out.println(contentE);
    }

    public static Document getDocument(String url) {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);//当JS执行出错的时候是否抛出异常, 这里选择不需要
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setCssEnabled(false);//是否启用CSS, 因为不需要展现页面, 所以不需要启用
        webClient.getOptions().setJavaScriptEnabled(true); //很重要，启用JS
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());//很重要，设置支持AJAX

        HtmlPage page = null;

        try {
            page = webClient.getPage(url);//尝试加载上面图片例子给出的网页
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            webClient.close();
        }

        webClient.waitForBackgroundJavaScript(5000);//异步JS执行需要耗时,所以这里线程要阻塞30秒,等待异步JS执行结束
        String pageXml = page.asXml();//直接将加载完成的页面转换成xml格式的字符串

        return Jsoup.parse(pageXml);
    }

    public static String solveDatetimeFormat(String datetime) {
        String format = datetime;
        String regex = "\\d{4}-\\d{1,2}-\\d{1,2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(format);
        while(matcher.find()) {
            format = matcher.group();
        }
        return format;
    }

    public static class TrustAnyTrustManager implements X509TrustManager {

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

    private String getRedirectUrl(String path) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(path)
                .openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(5000);
        return conn.getHeaderField("Location");
    }

    public void downloadImage(String urlStr, String filePath) throws Exception {
        URL url = null;
        URLConnection urlConnection = null;
        InputStream inputStream = null;
        byte[] bytes = new byte[1024];
        int len;
        OutputStream outputStream = null;
        url = new URL(urlStr);
        urlConnection = url.openConnection();
        urlConnection.setRequestProperty
                ("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0");
        inputStream = urlConnection.getInputStream();
        outputStream = new FileOutputStream(filePath);
        while ((len = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public void testlistUrl(String url) throws Exception {
        Document document = Jsoup.connect(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0")
                .header("Accept","*/*")
                .header("Connection","keep-alive")
                .timeout(5000)
                .get();
        String format = new String(document.html().getBytes("gb2312"), "gbk");
        Elements contentE = document.select("#detail-content");
        System.out.println(contentE);
    }

}


