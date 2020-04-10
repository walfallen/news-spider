package com.yc.spider.oss;

/**
 * oss服务器地址类
 */
public class OSSEndPoint {
    public static String BEIJING = "http://oss-cn-beijing.aliyuncs.com";
    public static String HANGZHOU = "http://oss-cn-hangzhou.aliyuncs.com";
    public static String HONGKONG = "http://oss-cn-hongkong.aliyuncs.com";
    public static String QINGDAO = "http://oss-cn-qingdao.aliyuncs.com";
    public static String SHENZHEN = "http://oss-cn-shenzhen.aliyuncs.com";

    public static String fromRaw(String ep) {
        String result = HANGZHOU;
        if (ep != null && !ep.isEmpty()) {
            if (ep.trim().equalsIgnoreCase("bj")) {
                result = BEIJING;
            } else if (ep.trim().equalsIgnoreCase("hh")) {
                result = HONGKONG;
            } else if (ep.trim().equalsIgnoreCase("qd")) {
                result = QINGDAO;
            } else if (ep.trim().equalsIgnoreCase("sz")) {
                result = SHENZHEN;
            }
        }
        return result;
    }
}
