package com.yc.spider.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by youmingwei on 16/7/20.
 */
public class DateUtils {
    public static String getDatetimeStringWithFormatString(String format) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }
}
