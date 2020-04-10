package com.yc.spider.major;

import com.yc.spider.util.FileUtil;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Test_major {

    public static void main(String... args) throws Exception {
        String listUrl = "http://college.gaokao.com/spepoint/y2016/p1";
        testlistUrl(listUrl);
    }
    public static void testlistUrl(String url) throws Exception {
        Document document = Jsoup.connect(url).timeout(10000)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0")
                .get();
        Elements contentE = document.select("#wrapper > div.cont_l.zycx > table > tbody");
        System.out.println(contentE);

    }
}