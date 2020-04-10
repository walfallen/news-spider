package com.yc.spider.db;

import com.yc.spider.Manager;
import com.yc.spider.bean.NewsRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimerTask;

/**
 * Created by youmingwei on 16/7/19.
 */

public class NewsRuleQuery extends TimerTask {
    public static final Logger logger = LoggerFactory.getLogger(NewsRuleQuery.class);
    @Override
    public void run() {
        logger.info("query rule start");
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = "select * from news_rule where status = 1";
        try {
            preparedStatement = MysqlUtil.getConnection().prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                NewsRule newsRule = new NewsRule();
                newsRule.setSchool(resultSet.getString("school"));
                newsRule.setCollege(resultSet.getString("college"));
                newsRule.setListName(resultSet.getString("list_name"));
                newsRule.setListId(resultSet.getInt("list_id"));
                newsRule.setListUrl(resultSet.getString("list_url"));
                newsRule.setListRootUrl(resultSet.getString("list_root_url"));
                newsRule.setListRule(resultSet.getString("list_rule"));
                newsRule.setUrlRule(resultSet.getString("url_rule"));
                newsRule.setTitleRule(resultSet.getString("title_rule"));
                newsRule.setDateRule(resultSet.getString("date_rule"));
                newsRule.setDetailRule(resultSet.getString("detail_rule"));
                newsRule.setReset(resultSet.getInt("reset"));
                newsRule.setShowListIcon(resultSet.getInt("show_list_icon"));

                try {
                    Manager.newsRuleBQ.put(newsRule);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        logger.info("query rule end");
    }
}


//public class NewsRuleQuery {
//    public static final Logger logger = LoggerFactory.getLogger(NewsRuleQuery.class);
//    public float ruleReadFrequency = ConfigUtil.getFloat("urlRuleReadFrequency");
//    Timer timer;
//
//    public NewsRuleQuery(){
//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            public void run() {
//                logger.info("query rule start");
//                PreparedStatement preparedStatement = null;
//                ResultSet resultSet = null;
//                String sql = "select * from news_rule where status = 1";
//                try {
//                    preparedStatement = MysqlUtil.getConnection().prepareStatement(sql);
//                    resultSet = preparedStatement.executeQuery();
//                    while (resultSet.next()) {
//                        int reset = resultSet.getInt("reset");
//                        if (1 == reset) {
//                            MysqlUtil.deleteNewsDetails(resultSet.getInt("list_id"));
//                            logger.info(resultSet.getString("school") + " "
//                                    + resultSet.getString("college") + " "
//                                    + resultSet.getString("list_name") + " reset");
//                        }
//                        NewsRule newsRule = new NewsRule();
//                        newsRule.setSchool(resultSet.getString("school"));
//                        newsRule.setCollege(resultSet.getString("college"));
//                        newsRule.setListName(resultSet.getString("list_name"));
//                        newsRule.setListId(resultSet.getInt("list_id"));
//                        newsRule.setListUrl(resultSet.getString("list_url"));
//                        newsRule.setListRootUrl(resultSet.getString("list_root_url"));
//                        newsRule.setListRule(resultSet.getString("list_rule"));
//                        newsRule.setUrlRule(resultSet.getString("url_rule"));
//                        newsRule.setTitleRule(resultSet.getString("title_rule"));
//                        newsRule.setDateRule(resultSet.getString("date_rule"));
//                        newsRule.setDetailRule(resultSet.getString("detail_rule"));
//
//                        try {
//                            Manager.newsRuleBQ.put(newsRule);
//                        } catch (InterruptedException e) {
//                            logger.error(e.getMessage());
//                        }
//                    }
//                } catch (SQLException e) {
//                    logger.error(e.getMessage());
//                } finally {
//                    try {
//                        preparedStatement.close();
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }
//                }
//                logger.info("query rule end");
//            }
//        }, 0, (long)ruleReadFrequency * 3600 * 1000);
//    }
//}

//public class NewsRuleQuery implements Runnable {
//    public static final Logger logger = LoggerFactory.getLogger(NewsRuleQuery.class);
//    public float ruleReadFrequency = ConfigUtil.getFloat("urlRuleReadFrequency");
//
//
//    @Override
//    public void run() {
//        while (true) {
//            logger.info("query rule start");
//            PreparedStatement preparedStatement = null;
//            ResultSet resultSet = null;
//            String sql = "select * from news_rule where status = 1";
//            try {
//                preparedStatement = MysqlUtil.getConnection().prepareStatement(sql);
//                resultSet = preparedStatement.executeQuery();
//                while (resultSet.next()) {
//                    int reset = resultSet.getInt("reset");
//                    if (1 == reset) {
//                        MysqlUtil.deleteNewsDetails(resultSet.getInt("list_id"));
//                        logger.info(resultSet.getString("school") + " "
//                                + resultSet.getString("college") + " "
//                                + resultSet.getString("list_name") + " reset");
//                    }
//                    NewsRule newsRule = new NewsRule();
//                    newsRule.setSchool(resultSet.getString("school"));
//                    newsRule.setCollege(resultSet.getString("college"));
//                    newsRule.setListName(resultSet.getString("list_name"));
//                    newsRule.setListId(resultSet.getInt("list_id"));
//                    newsRule.setListUrl(resultSet.getString("list_url"));
//                    newsRule.setListRootUrl(resultSet.getString("list_root_url"));
//                    newsRule.setListRule(resultSet.getString("list_rule"));
//                    newsRule.setUrlRule(resultSet.getString("url_rule"));
//                    newsRule.setTitleRule(resultSet.getString("title_rule"));
//                    newsRule.setDateRule(resultSet.getString("date_rule"));
//                    newsRule.setDetailRule(resultSet.getString("detail_rule"));
//
//                    try {
//                        Manager.newsRuleBQ.put(newsRule);
//                    } catch (InterruptedException e) {
//                        logger.error(e.getMessage());
//                    }
//                }
//            } catch (SQLException e) {
//                logger.error(e.getMessage());
//            } finally {
//                try {
//                    preparedStatement.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//            logger.info("query rule end");
//            try {
//                Thread.sleep((long) (ruleReadFrequency * 3600 * 1000));
//            } catch (InterruptedException e) {
//                logger.error(e.getMessage());
//            }
//        }
//    }
//}
