package com.yc.spider;

import com.yc.spider.bean.NewsRule;
import com.yc.spider.conf.ConfigUtil;
import com.yc.spider.db.NewsRuleQuery;
import com.yc.spider.spider.NewsSpider;

import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by youmingwei on 17/1/12.
 */
public class Manager {
    // change the runLocal to false before create the jar package server
    public static boolean runLocal = true;
    public static LinkedBlockingQueue newsRuleBQ;

    static {
        newsRuleBQ = new LinkedBlockingQueue<NewsRule>();
    }

    public static void main (String[] args) {
        new Manager().start();
    }

    public void start() {
        float ruleReadFrequency = ConfigUtil.getFloat("urlRuleReadFrequency");
        Timer timer = new Timer();
        timer.schedule(new NewsRuleQuery(), 0, (long) ruleReadFrequency * 3600 * 1000);
        new Thread(new NewsSpider()).start();
    }
}
