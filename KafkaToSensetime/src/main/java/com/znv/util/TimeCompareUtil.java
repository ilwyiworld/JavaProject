package com.znv.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间间隔比较
 */
public class TimeCompareUtil {

    public static long compareTime(String dateString1, String dateString2) {
        long interval=0l;
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date1 = simpleDateFormat.parse(dateString1);
            Date date2 = simpleDateFormat.parse(dateString2);
            interval = (date1.getTime() - date2.getTime())/1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return interval;
    }
}
