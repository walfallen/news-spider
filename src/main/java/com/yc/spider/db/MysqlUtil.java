package com.yc.spider.db;

import com.yc.spider.bean.NewsInfo;
import com.yc.spider.bean.NewsRule;
import com.yc.spider.conf.ConfigUtil;
import com.yc.spider.util.MonitorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Created by youmingwei on 16/7/19.
 */
public class MysqlUtil {
    private static String url = ConfigUtil.getString("mysqlUrl");
    private static String user = ConfigUtil.getString("mysqlUser");
    private static String pwd = ConfigUtil.getString("mysqlPassword");
    private final static String driver="com.mysql.jdbc.Driver";
    private static Connection connection = null;
    public static final Logger logger = LoggerFactory.getLogger(MysqlUtil.class);

    static {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public synchronized static Connection getConnection(){
        try {
            if (null == connection || connection.isClosed()) {
                connection= DriverManager.getConnection(url, user, pwd);
            }
        } catch (SQLException e) {
            logger.error("数据库链接创建失败:" + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
    public static boolean queryDBAndExistDetailUrl(int schoolId, int collegeId, int listId,  String detailUrl) {
        String sql = "select count(*) cnt from news_detail " +
                "where school_id = ? and college_id = ? and list_id = ? and detail_url = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        int cnt = 0;
        try {
            preparedStatement = MysqlUtil.getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, schoolId);
            preparedStatement.setInt(2, collegeId);
            preparedStatement.setInt(3, listId);
            preparedStatement.setString(4, detailUrl);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                cnt = resultSet.getInt("cnt");
            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return cnt > 0;
    }
    public static int insertNewsDetailInfo(String school, int schoolId, String college, int collegeId, String listName, int listId,
                                           String createTime, String editTime, String isTop, String listUrl, String detailUrl, String title, String datetime) {
        int detailId = -1;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String sql = "";
            if (datetime.isEmpty()) {
                sql = "insert into news_detail(school, school_id, college, college_id, list_name, list_id, " +
                        "create_time, edit_time, is_top, list_url, detail_url, title) " +
                        "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            } else {
                sql = "insert into news_detail(school, school_id, college, college_id, list_name, list_id, " +
                        "create_time, edit_time, is_top, list_url, detail_url, title, datetime) " +
                        "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }
            preparedStatement = MysqlUtil.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, school);
            preparedStatement.setInt(2, schoolId);
            preparedStatement.setString(3, college);
            preparedStatement.setInt(4, collegeId);
            preparedStatement.setString(5, listName);
            preparedStatement.setInt(6, listId);
            preparedStatement.setString(7, createTime);
            preparedStatement.setString(8, editTime);
            preparedStatement.setString(9, isTop);
            preparedStatement.setString(10, listUrl);
            preparedStatement.setString(11, detailUrl);
            preparedStatement.setString(12, title);
            if (!datetime.isEmpty()) {
                preparedStatement.setString(13, datetime);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        try {
            String sql = "select * from news_detail where school_id = ? and college_id = ? " +
                    "and list_id = ? and detail_url = ?";
            preparedStatement = MysqlUtil.getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, schoolId);
            preparedStatement.setInt(2, collegeId);
            preparedStatement.setInt(3, listId);
            preparedStatement.setString(4, detailUrl);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                detailId = resultSet.getInt("detail_id");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return detailId;
    }
    public static void updateNewsDetail(int sourceId, int departId, int detailId,
                                        String imgUrlA, String imgUrlB, String imgUrlC, String htmlUrl) {
        PreparedStatement preparedStatement = null;
        try {
            String sql = "update news_detail set img1 = ?, img2 = ?, img3 = ?, " +
                    "html_url = ? where school_id = ? and college_id = ? and detail_id = ?";
            preparedStatement = MysqlUtil.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, imgUrlA);
            preparedStatement.setString(2, imgUrlB);
            preparedStatement.setString(3, imgUrlC);
            preparedStatement.setString(4, htmlUrl);
            preparedStatement.setInt(5, sourceId);
            preparedStatement.setInt(6, departId);
            preparedStatement.setInt(7, detailId);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
    public static int getCollegeId(String school, String college) {
        int departId = -1;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String sql = "select * from college where u_name = ? and name = ?";
            preparedStatement = MysqlUtil.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, school);
            preparedStatement.setString(2, college);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                departId = resultSet.getInt("id");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return departId;
    }
    public static int getSourceId(String school) {
        int sourceId = -1;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String sql = "select ucode from college where u_name = ? limit 1";
            preparedStatement = MysqlUtil.getConnection().prepareStatement(sql);
            preparedStatement.setString(1, school);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                sourceId = resultSet.getInt("ucode");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return sourceId;
    }
    public static void registerResource(int collegeId, int listId, String listName, String datetime) {
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        boolean exist = false;
        try {
            String sql = "select * from resource where college_id = ? and detail_id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, collegeId);
            preparedStatement.setInt(2, listId);
            resultSet = preparedStatement.executeQuery();
            exist = resultSet.next();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        if (false == exist) {
            try {
                String sql = "insert into resource(create_time, edit_time, is_show, college_id, detail_id, type, title) " +
                        "values(?, ?, ?, ?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, datetime);
                preparedStatement.setString(2, datetime);
                preparedStatement.setInt(3, 1);
                preparedStatement.setInt(4, collegeId);
                preparedStatement.setInt(5, listId);
                preparedStatement.setInt(6, 2);
                preparedStatement.setString(7, listName);
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public static void deleteNewsDetails(int listId) {
        PreparedStatement preparedStatement;
        try {
            String sql = "delete from news_detail where list_id = ?";
            preparedStatement = MysqlUtil.getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, listId);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
    public static void testCrawleredNewsInfo(NewsRule newsRule, NewsInfo newsInfo) {
        boolean crawlerRes = true;
        String resStr = newsRule.getSchool() + " " + newsRule.getCollege() + " " + newsRule.getListName();
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        try {
            String sql = "select * from news_detail where detail_id = ?";
            preparedStatement = MysqlUtil.getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, newsInfo.getDetailId());
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String htmlUrl = resultSet.getString("html_url");
                if (htmlUrl == null || htmlUrl.isEmpty()) {
                    crawlerRes = false;
//                    String alert = newsRule.getSchool() + " " + newsRule.getCollege() + " " + newsRule.getListName()
//                            + " " + newsRule.getListUrl() + " " + newsInfo.getDetailUrl() + " " + "详情爬取失败!";
//                    MonitorUtil.alertToDingDing(alert);
                    logger.info(resStr + " 爬取失败 html_url为空 " + newsInfo.getDetailUrl());
                } else {
                    logger.info("测试:" + "http://newshtml.51zy.org/yc/university_news" + newsInfo.getHtmlUrl());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        if (false == crawlerRes) {
            //delete
            try {
                String sql = "delete from news_detail where detail_id = ?";
                preparedStatement = MysqlUtil.getConnection().prepareStatement(sql);
                preparedStatement.setInt(1, newsInfo.getDetailId());
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
