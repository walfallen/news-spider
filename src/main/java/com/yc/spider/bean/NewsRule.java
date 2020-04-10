package com.yc.spider.bean;

/**
 * Created by youmingwei on 17/1/12.
 */
public class NewsRule {
    private int listId;
    private String school;
    private int schoolId;
    private String college;
    private int collegeId;
    private String listName;
    private String listUrl;
    private String listRootUrl;
    private String detailRootUrl;
    private String listRule;
    private String urlRule;
    private String titleRule;
    private String dateRule;
    private String detailRule;
    private int reset;
    private int showListIcon;

    public NewsRule() {
        listId = -1;
        school = "";
        schoolId = -1;
        college = "";
        collegeId = -1;
        listName = "";
        listUrl = "";
        listRootUrl = "";
        detailRootUrl = "";
        listRule = "";
        urlRule = "";
        titleRule = "";
        dateRule = "";
        detailRule = "";
        reset = -1;
        showListIcon = -1;
    }

    public int getShowListIcon() {
        return showListIcon;
    }

    public void setShowListIcon(int showListIcon) {
        this.showListIcon = showListIcon;
    }

    public int getReset() {
        return reset;
    }

    public void setReset(int reset) {
        this.reset = reset;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(int schoolId) {
        this.schoolId = schoolId;
    }

    public int getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(int collegeId) {
        this.collegeId = collegeId;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getListUrl() {
        return listUrl;
    }

    public void setListUrl(String listUrl) {
        this.listUrl = listUrl;
    }

    public String getListRootUrl() {
        return listRootUrl;
    }

    public void setListRootUrl(String listRootUrl) {
        this.listRootUrl = listRootUrl;
    }

    public String getDetailRootUrl() {
        return detailRootUrl;
    }

    public void setDetailRootUrl(String detailRootUrl) {
        this.detailRootUrl = detailRootUrl;
    }

    public String getListRule() {
        return listRule;
    }

    public void setListRule(String listRule) {
        this.listRule = listRule;
    }

    public String getUrlRule() {
        return urlRule;
    }

    public void setUrlRule(String urlRule) {
        this.urlRule = urlRule;
    }

    public String getTitleRule() {
        return titleRule;
    }

    public void setTitleRule(String titleRule) {
        this.titleRule = titleRule;
    }

    public String getDateRule() {
        return dateRule;
    }

    public void setDateRule(String dateRule) {
        this.dateRule = dateRule;
    }

    public String getDetailRule() {
        return detailRule;
    }

    public void setDetailRule(String detailRule) {
        this.detailRule = detailRule;
    }
}
