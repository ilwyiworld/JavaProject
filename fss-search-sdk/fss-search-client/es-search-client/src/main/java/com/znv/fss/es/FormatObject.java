package com.znv.fss.es;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by User on 2017/8/3.
 */
public class FormatObject {
    protected static final Logger LOG = LogManager.getLogger(FormatObject.class);
    private static String getLevelStr(int level) {
        StringBuffer levelStr = new StringBuffer();
        for (int levelI = 0; levelI < level; levelI++) {
            levelStr.append("\t");
        }
        return levelStr.toString();
    }

    // 使JSON字符串自动换行，美化打印结果
    public static String format(String jsonStr) {
        int level = 0;
        StringBuffer jsonForMatStr = new StringBuffer();
        for (int i = 0; i < jsonStr.length(); i++) {
            char c = jsonStr.charAt(i);
            if (level > 0 && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
                jsonForMatStr.append(getLevelStr(level));
            }
            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c + "\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c + "\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
        return jsonForMatStr.toString();
    }

    public static String formatTime(String time) {
        String formatTime = "";
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        // 转换成东八区时区
        sdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date enterTime1 = sdf1.parse(time);
            formatTime = sdf2.format(enterTime1);
        } catch (ParseException e) {
            LOG.error("time format error.",e);
        }
        return formatTime;
    }
    //向前推n个小时
    public static String timeOffset(String time,int n) {
        String timeAfter = "";
        // 转换成东八区时区
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date time1 = sdf2.parse(time);
            timeAfter = sdf2.format(new Date(time1.getTime()-n*60*60*1000L));
        } catch (ParseException e) {
            LOG.error("time compare error.",e);
        }
        return timeAfter;
    }
    //time1向前推n个小时并与时间time2比较大小，返回大的那个
    public static String timeCompare(String time1,String time2,int n) {
        String timeAfter = "";
        // 转换成东八区时区
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date  date1 = sdf2.parse(time1);
            Date  date2 = sdf2.parse(time2);
            long mininTime1 = date1.getTime()-n*60*60*1000L;
            long mininTime2 = date2.getTime();
            if(mininTime1 > mininTime2){
                timeAfter = sdf2.format(new Date(mininTime1));
            }else{
                timeAfter = sdf2.format(new Date(mininTime2));
            }

        } catch (ParseException e) {
            LOG.error("time compare error.",e);
        }
        return timeAfter;
    }


}
