package com.yc.spider.spider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.yc.spider.Manager;
import com.yc.spider.bean.NewsInfo;
import com.yc.spider.bean.NewsRule;
import com.yc.spider.conf.ConfigUtil;
import com.yc.spider.db.MysqlUtil;
import com.yc.spider.oss.OSSEndPoint;
import com.yc.spider.oss.OSSSmartClient;
import com.yc.spider.util.DateUtils;
import com.yc.spider.util.FileUtil;
import com.yc.spider.util.ImgUtils;
import com.yc.spider.util.TrustSSL;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by youmingwei on 17/1/12.
 */

public class NewsSpider implements Runnable {
    public final Logger logger = LoggerFactory.getLogger(NewsSpider.class);
    public String localImageCacheDir = ConfigUtil.getString("localImgCacheDir");
    public String localHtmlCachePath = ConfigUtil.getString("localHtmlCachePath");
    public String ossImgPath = ConfigUtil.getString("ossImgRootPath");
    public String ossHtmlPath = ConfigUtil.getString("ossHtmlRootPath");
    public String imgRootUrlPath = ConfigUtil.getString("imgRootUrlPath");
    public String accessKeyId = ConfigUtil.getString("accessKeyId");
    public String accessKeySecret = ConfigUtil.getString("accessKeySecret");
    public String imgBucketName = ConfigUtil.getString("imgBucketName");
    public String htmlBucketName = ConfigUtil.getString("htmlBucketName");
    public String endPoint = OSSEndPoint.fromRaw(ConfigUtil.getString("endPoint"));

    private NewsRule newsRule;
    private NewsInfo newsInfo;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run() {
        while (true) {
            try {
                newsRule = (NewsRule) Manager.newsRuleBQ.take();
            } catch (InterruptedException e) {
                continue;
            }

            logger.info("start crawler: " + newsRule.getSchool() + " " + newsRule.getCollege() + " " + newsRule.getListName());

            if (1 == newsRule.getReset()) {
                MysqlUtil.deleteNewsDetails(newsRule.getListId());

                logger.info(newsRule.getSchool() + " " + newsRule.getCollege() + " " + newsRule.getListName()+ " reset");
            }

            solveNewsRule();

            if (Manager.newsRuleBQ.size() == 0) {
                logger.info("end solve detail");
            }
        }
    }

    public void solveNewsRule() {
        preSolve();

        List detailMapList = new ArrayList<HashMap<String, String>>();

        if (newsRule.getSchool().equals("浙江科技学院") && newsRule.getListName().equals("走近校友")) {

            crawlerUrls(newsRule, detailMapList);
            removeDuplicate(newsRule, detailMapList);
        } else {
            crawlerUrls(newsRule, detailMapList);
            removeDuplicate(newsRule, detailMapList);
        }

        for (int i = 0; i < detailMapList.size(); ++i) {
            Map detailMap = (Map) detailMapList.get(i);
            String detailUrl = (String) detailMap.get("url");
            String title = (String) detailMap.get("title");
            String datetime = (String) detailMap.get("datetime");

            newsInfo = null;
            newsInfo = new NewsInfo();
            newsInfo.setDetailUrl(detailUrl);
            newsInfo.setTitle(title);
            newsInfo.setDatetime(datetime);
            String nowTime = DateUtils.getDatetimeStringWithFormatString("yyyy-MM-dd hh:mm:ss");

            int detailId = MysqlUtil.insertNewsDetailInfo(
                    newsRule.getSchool(), newsRule.getSchoolId(),
                    newsRule.getCollege(), newsRule.getCollegeId(),
                    newsRule.getListName(), newsRule.getListId(),
                    nowTime, nowTime, nowTime, newsRule.getListUrl(), detailUrl, title, datetime);
            newsInfo.setDetailId(detailId);

            solveDetailPage(newsRule, newsInfo);

            MysqlUtil.testCrawleredNewsInfo(newsRule, newsInfo);
        }


    }

    public void preSolve() {
        int collegeId = MysqlUtil.getCollegeId(newsRule.getSchool(), newsRule.getCollege());
        int schoolId = MysqlUtil.getSourceId(newsRule.getSchool());
        newsRule.setSchoolId(schoolId);
        newsRule.setCollegeId(collegeId);

        MysqlUtil.registerResource(newsRule.getCollegeId(), newsRule.getListId(), newsRule.getListName(),
                DateUtils.getDatetimeStringWithFormatString("yyyy-MM-dd hh:mm:ss"));
    }

    public void crawlerUrls(NewsRule newsRule, List detailMapList) {
        String[] listUrls = newsRule.getListUrl().split(";");

        String[] listRules = newsRule.getListRule().split(";");
        String[] urlRules = newsRule.getUrlRule().split(";");
        String[] titleRules = newsRule.getTitleRule().split(";");
        String[] dateRules = newsRule.getDateRule().split(";");

        Document document = null;
        Elements rowEs = new Elements();
        for (int i = listUrls.length - 1; i >= 0; i--) {
            try {
                if(newsRule.getSchool().equals("宁波大学")){
                    document = TrustSSL.NingBoDaxUE(listUrls[i]);
                    rowEs.addAll(document.select(listRules[0]));
                }
                else if (newsRule.getSchool().equals("杭州医学院") && newsRule.getCollege().equals("招生办")){
                    document = Jsoup.connect(listUrls[i])
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0")
                            .get();
                    Elements contentE = new Elements();
                    if (newsRule.getListName().equals("最新动态")){
                        contentE = document.select("#7170 > div");
                    }else if (newsRule.getListName().equals("招生政策")){
                        contentE = document.select("#7093 > div");
                    }
                    String contentE0 = contentE.toString().replace("&lt;","<");
                    contentE0 = contentE0.replace("&gt;",">");
                    contentE0 = contentE0.replace("<record><![CDATA[","");
                    contentE0 = contentE0.replace("]]></record>","");
                    contentE0 = contentE0.replace("<![CDATA[","");
                    Document doc = Jsoup.parse(contentE0);
                    contentE = doc.select("table");
                    rowEs.addAll(contentE);
                } else {
                    document = Jsoup.connect(listUrls[i])
                            .timeout(5000)
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0")
                            .get();
                    rowEs.addAll(document.select(listRules[0]));
                }
            } catch (Exception e) {
                logger.info(newsRule.getSchool() + " " + newsRule.getCollege() + " "
                        + newsRule.getListName() + " 爬取列表异常 " + e.getMessage());

                continue;
            }
        }

        if (null == rowEs || rowEs.size() == 0) {
            logger.info(newsRule.getSchool() + " " + newsRule.getCollege() + " "
                    + newsRule.getListName() + " 爬取失败");

            return;
        }

        int size = rowEs.size();
        for (int i = size - 1; i >= 0; --i) {
            Element element = rowEs.get(i);
            String url = element.select(urlRules[0]).attr("href").trim();
            if (url.isEmpty()) {
                continue;
            }
            if (!url.startsWith("http")) {
                if (url.startsWith("/")) {
                    url = newsRule.getListRootUrl() + url;
                } else if (url.startsWith("../../")) {
                    url = newsRule.getListRootUrl() + "/" + url.substring(6);
                } else if (url.startsWith("../")){
                    url = newsRule.getListRootUrl() + "/" + url.substring(3);
                }
                else {
                    url = newsRule.getListRootUrl() + "/" + url;
                }
            }
            if (newsRule.getSchool().equals("宁波大学科学技术学院") && newsRule.getCollege().equals("招生办")){
                url = url.replace("\\","/");
            }

            String title;
            if (titleRules[1].equals("attr")) {
                title = element.select(titleRules[0]).attr("title").trim();
            } else if (titleRules[1].equals("text")) {
                title = element.select(titleRules[0]).text().trim();
                if (newsRule.getSchool().equals("中国计量大学") && newsRule.getListName().equals("招生动态")) {
                    title = title.substring(0, title.length() - 10);
                }
            } else {
                title = "";
            }

            String datetime = "";
            if (dateRules.length != 0) {
                datetime = element.select(dateRules[0]).text().trim();
            }

            String formatDatetime;
            if (!datetime.isEmpty()){
                int a = 4;
                char pos = datetime.charAt(a);
                if (pos == '年'){
                    formatDatetime = solveDatetimeFormatCSXY(datetime);
                }
                else if (newsRule.getSchool().equals("浙江海洋大学") && newsRule.getCollege().equals("招生办")
                        && newsRule.getListName().equals("学校动态")){
                    formatDatetime = solveDatetimeFormatZJHY(datetime);
                }
                else if (newsRule.getSchool().equals("浙江中医药大学") && newsRule.getCollege().equals("招生办")){
                    datetime = datetime.substring(1, 11);
                    datetime = datetime.replaceAll("/", "-");
                    formatDatetime = solveDatetimeFormat(datetime);
                }
                else {
                    formatDatetime = solveDatetimeFormat(datetime);
                }
            }else {
                formatDatetime = solveDatetimeFormat(datetime);
            }

            Map map = new HashMap<>();
            map.put("title", title);
            map.put("url", url);
            map.put("datetime", formatDatetime);
            detailMapList.add(map);
        }
    }

    public String solveDatetimeFormat(String datetime) {
        String format = datetime;
        String regex = "\\d{4}-\\d{1,2}-\\d{1,2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(format);
        while(matcher.find()) {
            format = matcher.group();
        }
        return format;
    }

    //solve detail
    public void solveDetailPage(NewsRule newsRule, NewsInfo newsInfo) {
        Document document = null;
        String documentStr = "";
        try {
            Thread.sleep(100);
            if (newsRule.getSchool().equals("宁波大学")){
                documentStr = TrustSSL.NingBoDaxUE(newsInfo.getDetailUrl()).toString();
            } else {
                document = Jsoup.connect(newsInfo.getDetailUrl())
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0")
                        .header("Accept","*/*")
                        .header("Connection","keep-alive")
                        .timeout(5000)
                        .get();
                documentStr = document.toString();
            }
        } catch (Exception e) {
            logger.info("document exception:" + e.getMessage());
//            String alert = newsRule.getSchool() + " " + newsRule.getCollege() + " " + newsRule.getListName()
//                    + " " + newsRule.getListUrl() + " " + newsInfo.getDetailUrl() + " " + "详情爬取失败!";
//            MonitorUtil.alertToDingDing(alert);
            return;
        }

        Element contentE = null;
        String[] imageUrls = new String[]{"", "", ""};
        String[] detailRules = newsRule.getDetailRule().split(";");
        for (int i = 0; i < detailRules.length; ++i) {
            String[] ruleDetails = detailRules[i].split("@");
            try {
                contentE = Jsoup.parse(documentStr).select(ruleDetails[0]).first();
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
            if (null == contentE) {
                continue;
            } else {
                solveImage(contentE, imageUrls, ruleDetails[3], newsRule.getSchool(), newsRule.getCollege());
                solveLink(contentE, ruleDetails[3]);
                transcodeHtml(contentE);
                addHtmlStart(localHtmlCachePath, ruleDetails[1], newsInfo.getTitle());
                if (ruleDetails[2].equals("html")) {
                    addHtmlEnd(localHtmlCachePath, contentE.html(),
                            newsRule.getSchool() + newsRule.getCollege());
                } else if (ruleDetails[2].equals("string")) {
                    addHtmlEnd(localHtmlCachePath, contentE.toString(),
                            newsRule.getSchool() + newsRule.getCollege());
                }

                break;
            }
        }
        if (null == contentE) {
//            String alert = newsRule.getSchool() + " " + newsRule.getCollege() + " " + newsRule.getListName()
//                    + " " + newsRule.getListUrl() + " " + newsInfo.getDetailUrl() + " " + "详情爬取失败!";
//            MonitorUtil.alertToDingDing(alert);
            return;
        }
        String relRemoteHtmlPath = "/" + newsRule.getSchoolId() + "/" + newsRule.getCollegeId()
                + "/" + newsRule.getListId() + "/" + newsInfo.getDetailId()
                + "/" + "index.html";
        newsInfo.setHtmlUrl(relRemoteHtmlPath);
        uploadHtmlToOSS(localHtmlCachePath, ossHtmlPath + relRemoteHtmlPath);
        FileUtil.deleteFile(localHtmlCachePath);
        //update detail db
        if (0 == newsRule.getShowListIcon()) {
            imageUrls[0] = "";
            imageUrls[1] = "";
            imageUrls[2] = "";
        }
        MysqlUtil.updateNewsDetail(newsRule.getSchoolId(), newsRule.getCollegeId(),
                newsInfo.getDetailId(), imageUrls[0],
                imageUrls[1], imageUrls[2], relRemoteHtmlPath);
        logger.info("solved detail " + newsInfo.getTitle());
    }

    public void solveImage(Element contentE, String[] imageUrls, String imageRootUrl, String school, String college) {
        int imgId = 0;
        Elements imgElements = contentE.select("img, input");
        for (Element imageE:imgElements) {
            String absUrlStr = "";
            String relUrlStr = imageE.attr("src");
            if (relUrlStr.isEmpty()) {
                continue;
            }
            if (!relUrlStr.startsWith("http")) {
                if (relUrlStr.startsWith("/")) {
                    absUrlStr = imageRootUrl + relUrlStr;
                } else {
                    absUrlStr = imageRootUrl + "/" + relUrlStr;
                }
            }
            else if (school.equals("宁波财经学院") && relUrlStr.startsWith("http")){
                absUrlStr = relUrlStr.replace("http","https");
            }
            else {
                absUrlStr = relUrlStr;
            }
            int pointIndex = absUrlStr.lastIndexOf(".");
            String imgType = absUrlStr.substring(pointIndex + 1);
            if (newsRule.getSchool().equals("浙江大学") && newsRule.getCollege().equals("外国语言文化与国际交流学院继续教育中心")) {
                imgType = "png";
            }
            String sourceImgPath = localImageCacheDir + "/source." + imgType;
            try {
                if (newsRule.getSchool().equals("浙江大学") && newsRule.getCollege().equals("外国语言文化与国际交流学院继续教育中心")
                        && newsRule.getListName().equals("讲座预告")){
                    HttpURLConnection conn = (HttpURLConnection) new URL(absUrlStr)
                            .openConnection();
                    conn.setInstanceFollowRedirects(false);
                    conn.setConnectTimeout(5000);
                    absUrlStr = conn.getHeaderField("Location");
                    downloadImage(absUrlStr, sourceImgPath);
                }
                else if (newsRule.getSchool().equals("浙江大学城市学院") && newsRule.getCollege().equals("信息与电气工程学院")
                        && newsRule.getListName().equals("学院新闻")){
                    imgType = imageE.attr("data-type");
                    sourceImgPath = localImageCacheDir + "/source." + imgType;
                    ImgUtils.downloadToFile(absUrlStr, sourceImgPath);

                }
                else downloadImage(absUrlStr, sourceImgPath);
            } catch (Exception e) {
                logger.info("image error: " + e.getMessage() + " " + absUrlStr);
                continue;
            }

            String relRemoteTitleImagePath = "/" + newsRule.getSchoolId() + "/" + newsRule.getCollegeId()
                    + "/" + newsRule.getListId() + "/" + newsInfo.getDetailId()
                    + "/" + "title" + "/" + (imgId) + "." + imgType;
            String relRemoteDetailImagePath = "/" + newsRule.getSchoolId() + "/" + newsRule.getCollegeId()
                    + "/" + newsRule.getListId() + "/" + newsInfo.getDetailId()
                    + "/" + "detail" + "/" + (imgId) + "." + imgType;
            if (imgId < 3) {
                imageUrls[imgId] = relRemoteTitleImagePath;
            }
            imgId++;
            uploadImageToOSS(sourceImgPath, ossImgPath + relRemoteTitleImagePath);
            uploadImageToOSS(sourceImgPath, ossImgPath + relRemoteDetailImagePath);
            imageE.attr("src", imgRootUrlPath + relRemoteDetailImagePath);
            imageE.removeAttr("width");
            imageE.removeAttr("height");

            FileUtil.deleteAllFilesFromDir(localImageCacheDir);
        }
    }

    public void solveLink(Element contentEs, String rootUrl) {
        Elements aEs = contentEs.select("a");
        for (Element aE: aEs) {
            String sourceUrl = aE.attr("href").trim();
            if (!sourceUrl.startsWith("http")) {
                if (sourceUrl.startsWith("/")) {
                    sourceUrl = rootUrl + sourceUrl;
                } else {
                    sourceUrl = rootUrl + "/" + sourceUrl;
                }
            } else {
                continue;
            }
            aE.attr("href", sourceUrl);
        }
    }

    public void downloadImage(String urlStr, String filePath) throws Exception {
        URL url = null;
        URLConnection urlConnection = null;
        InputStream inputStream = null;
        byte[] bytes = new byte[1024];
        int len;
        url = new URL(urlStr);
        urlConnection = url.openConnection();
        urlConnection.setRequestProperty
                ("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0");
        inputStream = urlConnection.getInputStream();
        OutputStream outputStream = new FileOutputStream(filePath);
        while ((len = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public void uploadImageToOSS(String localPath, String remotePath) {
        OSSSmartClient client = new OSSSmartClient(endPoint,
                accessKeyId, accessKeySecret);
        try {
            client.putObject(imgBucketName, remotePath, localPath, "image/jpg");
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
    }

    public void uploadHtmlToOSS(String localPath, String remotePath) {
        OSSSmartClient client = new OSSSmartClient(endPoint,
                accessKeyId, accessKeySecret);
        try {
            client.putObject(htmlBucketName, remotePath, localPath, "text/html");
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
    }

    public void addHtmlStart(String localHtmlPath, String titleTag, String title) {
        String a = "<html>";
        String b = "<head>";
        String c = " <meta charset=\"utf-8\">";
        String d = "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1,user-scalable=no\">";
        String e = "  <meta name=\"format-detection\" content=\"telphone=no,email=no\"/>";
        String f = "<h2 style=\"text-align: center\">";
        String g = "</h2>";
        String h = "<style> img{ width: 100%; } </style>";
        String i = "</head>";
        String[] strings;
        if (titleTag.equals("null")) {
            strings = new String[]{a, b, c, d, e, h, i};
        } else {
            strings = new String[]{a, b, c, d, e, f, title, g, h, i};
        }
        for (int num = 0; num < strings.length; num++) {
            FileUtil.writeRowStrToFileWithStream(localHtmlPath, strings[num], true);
        }
    }

    public void addHtmlEnd(String localHtmlPath, String bodyStr, String newsSource) {
        String formatBodyStr = bodyStr.replace("&nbsp;", "");

        String a = "<body>";
        String b = "</body>";
        String c = "</html>";
        String[] strings;

        if (newsSource.equals("浙江大学本科生招生网")) {
            String sourceTag = "<p align=\"left\">" + "来源:" + newsSource + "</p>";
            strings = new String[]{a, formatBodyStr, sourceTag, b, c};
        } else {
            strings = new String[]{a, formatBodyStr, b, c};
        }
        for (int j = 0; j < strings.length; j++) {
            FileUtil.writeRowStrToFileWithStream(localHtmlPath, strings[j], true);
        }
    }

    public void transcodeHtml(Element contentE) {
        String[] rules = {"td", "tr", "table", "img", "div", "li", "iframe", "p", "input"};
        for (int i = 0; i < rules.length; ++i) {
            for (Element pE: contentE.select(rules[i])) {
                pE.removeAttr("style");
                pE.removeAttr("width");
                pE.removeAttr("WIDTH");
                pE.removeAttr("height");
                pE.removeAttr("onclick");
            }
        }
        for (Element tdE: contentE.select("td[nowrap]")) {
            tdE.removeAttr("nowrap");
        }
        String[] removeRules = {"#图片_x0020_1", "#_x0000_s1026"};
        for (int i = 0; i < removeRules.length; ++i) {
            contentE.select(removeRules[i]).remove();
        }
        contentE.select("table").attr("border", "1");
        for (Element element: contentE.select("table")) {
            element.attr("style", "background: #fff");
        }

        if(newsRule.getSchool().equals("浙江大学宁波理工学院")
                && newsRule.getCollege().equals("土木建筑工程学院")
                && newsRule.getListName().equals("师资队伍")) {
            for (Element element: contentE.select("table > tbody > tr > td:nth-child(1)")) {
                element.attr("width", "30%");
            }
        }
        if(newsRule.getSchool().equals("浙江大学宁波理工学院")
                && newsRule.getCollege().equals("传媒与设计学院")) {
            for (Element element: contentE.select("input")) {
                element.attr("width", "98%");
            }
        }
        for (Element element: contentE.select("div, p")) {
            if (element.select("img").size() > 0) {
                element.removeAttr("style");
            }
        }
        if (newsRule.getSchool().equals("浙江理工大学")
                && newsRule.getCollege().equals("招生办")
                && newsRule.getListName().equals("招生动态")) {
            for (Element pE: contentE.select("iframe")) {
                pE.attr("width", "100%");
            }
        }
    }

    public void removeDuplicate(NewsRule newsRule, List detailMapList) {
        Iterator iterator = detailMapList.iterator();

        while (iterator.hasNext()) {
            HashMap hashMap = (HashMap)iterator.next();
            String detailUrl = (String) hashMap.get("url");
            boolean res = MysqlUtil.queryDBAndExistDetailUrl(newsRule.getSchoolId(), newsRule.getCollegeId(),
                    newsRule.getListId(), detailUrl);
            if (true == res) {
                iterator.remove();
            } else {
                logger.info(detailUrl + " " + hashMap.get("title") + " " + hashMap.get("datetime"));
            }
        }
    }

    public void crawlerUrls_shangxueyuan(NewsRule newsRule, List detailMapList){
        Document document = null;
        try{
            Content content = Request.Get(newsRule.getListUrl()).connectTimeout(10000)
                    .setHeader("Accept","*/*")
                    .setHeader("Accept-Encoding","gzip, deflate, sdch")
                    .setHeader("Accept-Language","zh-CN,zh;q=0.8")
                    .setHeader("Cache-Control","max-age=0")
                    .setHeader("Connection","keep-alive")
                    .setHeader("Host","sxy.zucc.edu.cn")
                    .setHeader("Referer", "http://sxy.zucc.edu.cn/module/permissionread/col/col453/index.jsp?colid=453&type=1")
                    .setHeader("Cookie", "_gscu_742156466=76863938818m7q88")
                    .setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36")
                    .execute().returnContent();
            String s = content.toString();
            String substring = s.substring("document.write( '".length()+4, s.length() - 7).replace('\\', ' ').trim();
            document = Jsoup.parse(substring);
            Elements select1 = document.select("div[id=718] table tbody tr");
            for(int i = select1.size() - 1; i >= 0; --i) {
                Element e = select1.get(i);
                Element select = e.select("td>a").first();
                Element select2 = e.select("td:nth-child(2)").first();
                String title = select.text();
                String ss = select.attr("href");
                String[] split = ss.split("_");
                String url = "";
                if (split.length > 2){
//                    logger.info(split.length+"");
//                    logger.info(split[0]);
                    String i_columnid = split[1];
                    String i_artid = split[2].split("\\.")[0];
                    url = newsRule.getListRootUrl()+"/module/permissionread"+select.attr("href").replaceAll("html","jsp").trim()
                            +"?i_columnid="+i_columnid+"&i_artid="+i_artid+"&ptype=1";
                }else
                    continue;
                String datetime = select2.text();
                String formatDatetime = solveDatetimeFormat(datetime);
                Map map = new HashMap<>();
                map.put("title", title);
                map.put("url", url);
                map.put("datetime", formatDatetime);
                detailMapList.add(map);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /*
    这段法学院的代码无法在校内网运行，会报错。
     */
    public void crawlerUrls_faxueyuan(NewsRule newsRule, List detailMapList){
        Document document = null;
        String listUrl = "";
        try{
//            Content content = Request.Get(newsRule.getListUrl()).connectTimeout(10000)
//                    .setHeader("Accept","*/*")
//                    .setHeader("Accept-Encoding","gzip, deflate, sdch")
//                    .setHeader("Accept-Language","zh-CN,zh;q=0.8")
//                    .setHeader("Cache-Control","max-age=0")
//                    .setHeader("Connection","keep-alive")
//                    .setHeader("Host","law.zucc.edu.cn")
//                    .setHeader("Referer", "http://law.zucc.edu.cn/module/permissionread/col/col361/index.jsp?colid=361&type=1")
//                    .setHeader("Cookie", "_gscu_742156466=76863938818m7q88")
//                    .setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36")
//                    .execute().returnContent();
//            String s = content.toString();
//            String substring = s.substring("document.write( '".length()+4, s.length() - 7).replace('\\', ' ').trim();
//            document = Jsoup.parse(substring);
//            Elements select1 = document.select("div[id=826] table tbody tr");
//            for(int i = select1.size() - 1; i >= 0; --i) {
//                Element e = select1.get(i);
//                Element select = e.select("td>a").first();
//                Element select2 = e.select("td:nth-child(2)").first();
//                String title = select.text();
//                String ss = select.attr("href");
//                String[] split = ss.split("_");
//                String url = "";
//                if (split.length > 2){
////                    logger.info(split.length+"");
////                    logger.info(split[0]);
//                    String i_columnid = split[1];
//                    String i_artid = split[2].split("\\.")[0];
//                    url = newsRule.getListRootUrl()+"/module/permissionread"+select.attr("href").replaceAll("html","jsp").trim()
//                            +"?i_columnid="+i_columnid+"&i_artid="+i_artid+"&ptype=1";
//                    System.out.println(url);
//                }else
//                    continue;
//                String datetime = select2.text();
//                String formatDatetime = solveDatetimeFormat(datetime);
//                Map map = new HashMap<>();
//                map.put("title", title);
//                map.put("url", url);
//                map.put("datetime", formatDatetime);
//                detailMapList.add(map);
//            }
            if (newsRule.getListName().equals("学院新闻")){
                listUrl = "http://law.zucc.edu.cn/col/col361/index.html";
            }else if (newsRule.getListName().equals("学工动态")) {
                listUrl = "http://law.zucc.edu.cn/col/col362/index.html";
            }
            document = Jsoup.connect(listUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0")
                    .header("Accept","*/*")
                    .header("Connection","keep-alive")
                    .timeout(50000)
                    .get();
            Elements contentE = document.select("body > table:nth-child(6) > tbody > tr > td:nth-child(2) > table > tbody > tr > td:nth-child(3) > table:nth-child(7) > tbody > tr > td:nth-child(2) >div ");
            String s = contentE.toString().replaceAll("\\<!\\[CDATA\\[|\\]\\]|\\<*record\\>|\\</record\\>", "");
            int table_start = s.indexOf("<table");
            int table_end = s.lastIndexOf("/table>>") + 8;
            s = s.substring(table_start, table_end);
            document = Jsoup.parse(s);
            Elements table = document.select("table");
            //System.out.println(table);

            for(int i = table.size() - 1; i >= 0; --i) {
                Element e = table.get(i);
                Element urle = e.select("a").first();
                String title = urle.text().trim();
//                System.out.println(title);
                String url = urle.attr("href");
                url = "http://law.zucc.edu.cn" + url;
//                System.out.println(url);
                Element time = e.select("td:nth-child(2)").first();
                String datetime = time.text();
                String formatDatetime = solveDatetimeFormat(datetime);
//                System.out.println(formatDatetime);
                Map map = new HashMap<>();
                map.put("title", title);
                map.put("url", url);
                map.put("datetime", formatDatetime);
                detailMapList.add(map);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void crawlerUrls_zhejiangkejixueyuan(NewsRule newsRule, List detailMapList) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(newsRule.getListUrl());

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("name", newsRule.getListRule()));
        nvps.add(new BasicNameValuePair("pageNo", "1"));
        nvps.add(new BasicNameValuePair("pageSize", "20"));
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));

        CloseableHttpResponse response = httpclient.execute(httpPost);
        HttpEntity entity2 = response.getEntity();
        String resStr = EntityUtils.toString(entity2);

        ArrayNode arrayNode = (ArrayNode) objectMapper.readTree(resStr).get("bean");
        for (int i = arrayNode.size() - 1; i >= 0; --i) {
            JsonNode jsonNode = arrayNode.get(i);
            Map map = new HashMap<>();
            map.put("title", jsonNode.get(newsRule.getTitleRule()).asText());
            map.put("url", newsRule.getListRootUrl() + jsonNode.get(newsRule.getUrlRule()).asText());
            map.put("datetime", jsonNode.get(newsRule.getDateRule()).asText().split("T")[0]);
            detailMapList.add(map);
        }

        response.close();
        httpPost.releaseConnection();
        httpclient.close();
    }

    public String solveDatetimeFormatZJHY(String datetime) {
        String str_date = datetime;
        String yyyy = datetime.substring(1,5);
        String mm = datetime.substring(6,8);
        String dd = datetime.substring(9,11);
        str_date = yyyy + "-" + mm + "-" + dd;

        return str_date;
    }

    public String solveDatetimeFormat_nblg_jineng(String datetime) {
        String [] date = datetime.split(" ");
        String str_date = "";
        String yyyy = "2018";
        String mm = "";
        if (date[1].equals("Jan")){
            mm = "01";
        }else if (date[1].equals("Feb")){
            mm = "02";
        }else if (date[1].equals("Mar")){
            mm = "03";
        }else if (date[1].equals("Apr")){
            mm = "04";
        }else if (date[1].equals("May")){
            mm = "05";
        }else if (date[1].equals("Jun")){
            mm = "06";
        }else if (date[1].equals("Jul")){
            mm = "07";
        }else if (date[1].equals("Aug")){
            mm = "08";
        }else if (date[1].equals("Sep")){
            mm = "09";
        }else if (date[1].equals("Oct")){
            mm = "10";
        }else if (date[1].equals("Nov")){
            mm = "11";
        }else if (date[1].equals("Dec")){
            mm = "12";
        }
        String dd = date[0];
        if (dd.length() < 2)
            dd = "0"+dd;
        str_date = yyyy + "-" + mm + "-" + dd;

        return str_date;
    }

    public String solveDatetimeFormatCSXY(String datetime){
        String str_date = datetime;

        try{
            Date d1 = new SimpleDateFormat("yyyy年MM月dd日").parse(str_date);//定义起始日期
            SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy");
            SimpleDateFormat sdf1 = new SimpleDateFormat("MM");
            SimpleDateFormat sdf2= new SimpleDateFormat("dd");

            String str1 = sdf0.format(d1);
            String str2 = sdf1.format(d1);
            String str3 = sdf2.format(d1);

            String format = str1 + "-" + str2 + "-" + str3;

            return format;

        }catch (ParseException e) {
            e.printStackTrace();
        }

        return str_date;
    }

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
}
