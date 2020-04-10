package com.yc.spider.bean;

/**
 * Created by youmingwei on 17/1/12.
 */
public class NewsInfo {
    private int detailId;
    private String detailUrl;
    private String title;
    private String datetime;
    private String imgUrlA;
    private String imgUrlB;
    private String imgUrlC;
    private String htmlUrl;


    public NewsInfo() {
        detailId = -1;
        detailUrl = "";
        title = "";
        datetime = "";
        imgUrlA = "";
        imgUrlB = "";
        imgUrlC = "";
        htmlUrl = "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public int getDetailId() {
        return detailId;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }


    public String getImgUrlA() {
        return imgUrlA;
    }

    public void setImgUrlA(String imgUrlA) {
        this.imgUrlA = imgUrlA;
    }

    public String getImgUrlB() {
        return imgUrlB;
    }

    public void setImgUrlB(String imgUrlB) {
        this.imgUrlB = imgUrlB;
    }

    public String getImgUrlC() {
        return imgUrlC;
    }

    public void setImgUrlC(String imgUrlC) {
        this.imgUrlC = imgUrlC;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
}
