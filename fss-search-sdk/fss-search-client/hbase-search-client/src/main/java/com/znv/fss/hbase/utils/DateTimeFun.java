package com.znv.fss.hbase.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * DateTimeFun
 */
public class DateTimeFun {
    private static final Log log = LogFactory.getLog(DateTimeFun.class);
    /**
     * Calendar日期时间信息.转化为字符串信息. 转化后的格式为: 2010-04-01 17:02:21 24小时制
     * @param time 日期时间信息
     * @return String 转化后的格式为: 2010-04-01 17:02:21
     */
    public static String dateTimeToString(Calendar time) {
        if (null == time) {
            time = Calendar.getInstance();
        }
        int y = time.get(Calendar.YEAR);
        int mon = time.get(Calendar.MONTH) + 1;
        int d = time.get(Calendar.DATE);
        int h = time.get(Calendar.HOUR);
        int min = time.get(Calendar.MINUTE);
        int s = time.get(Calendar.SECOND);
        int ap = time.get(Calendar.AM_PM);
        if (ap == 1) {
            h = h + 12;
        }
        String string = (y + "-" + ((mon <= 9) ? "0" + mon : mon) + "-" + ((d <= 9) ? "0" + d : d) + " " + h + ":"
            + ((min <= 9) ? "0" + min : min) + ":" + ((s <= 9) ? "0" + s : s));
        return string;
    }

    /**
     * Calendar日期时间信息.转化为字符串信息. 转化后的格式为: 2010-04-01 17:02:21 24小时制
     * @param time 日期时间信息
     * @return String 转化后的格式为: 2010-04-01 17:02:21
     */
    public static String dateTimeToString(Calendar time, boolean milli) {
        if (null == time) {
            time = Calendar.getInstance();
        }
        int y = time.get(Calendar.YEAR);
        int mon = time.get(Calendar.MONTH) + 1;
        int d = time.get(Calendar.DATE);
        int h = time.get(Calendar.HOUR_OF_DAY);
        int min = time.get(Calendar.MINUTE);
        int s = time.get(Calendar.SECOND);
        @SuppressWarnings("unused")
        // int ap = time.get(Calendar.AM_PM);
        int millis = time.get(Calendar.MILLISECOND);
        String string = null;
        if (milli) {
            string = String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d", y, mon, d, h, min, s, millis);
        } else {
            string = String.format("%04d-%02d-%02d %02d:%02d:%02d", y, mon, d, h, min, s);
        }
        /*
         * if (ap == 1) h = h + 12; String string = (y + "-" + ((mon <= 9) ? "0" + mon : mon) + "-" + ((d <= 9) ? "0" +
         * d : d) + " " + h + ":" + ((min <= 9) ? "0" + min : min) + ":" + ((s <= 9) ? "0" + s + "."+mi : s));
         */
        return string;
    }

    /**
     * Calendar日期信息.转化为字符串信息. 转化后的格式为: 2010-04-01 小时制
     * @param time 日期信息
     * @return String 转化后的格式为: 2010-04-01
     */
    public static String dateToString(Calendar time) {
        if (null == time) {
            time = Calendar.getInstance();
        }
        int y = time.get(Calendar.YEAR);
        int mon = time.get(Calendar.MONTH) + 1;
        int d = time.get(Calendar.DATE);
        String string = (y + "-" + ((mon <= 9) ? "0" + mon : mon) + "-" + ((d <= 9) ? "0" + d : d));
        return string;
    }

    /**
     * 日期时间字符串信息转化为日期时间Calendar
     * @param string 字符串日期时间信息 格式为: 2010-04-01 17:02:21 24小时制
     * @return 日期时间Calendar 失败返回null
     */
    public static Calendar stringToDateTime(String string) {
        if (null == string || string.isEmpty()) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        // 取得日期
        Calendar date = stringToDate(string);
        if (null == date) {
            return null;
        }

        Calendar time = stringToTime(string);
        if (null == time) {
            return null;
        }

        calendar.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE),
            time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.SECOND));
        return calendar;
    }

    /**
     * 日期字符串信息转化为日期Calendar
     * @param string 字符串日期信息 格式为: 2010-04-01
     * @return 日期时间Calendar 失败返回null
     */
    public static Calendar stringToDate(String string) {
        if (null == string || string.isEmpty()) {
            return null;
        }
        string = string.trim();
        // 取得年
        int nIndex = string.indexOf("-");
        if (nIndex < 1) {
            return null;
        }
        String yearString = string.substring(0, nIndex);
        int year = Integer.parseInt(yearString);
        if (year < 0) {
            return null;
        }

        // 取得月
        String temp = string.substring(nIndex + 1, string.length());
        nIndex = temp.indexOf("-");
        if (nIndex < 0) {
            return null;
        }
        String monthString = temp.substring(0, nIndex);
        int month = Integer.parseInt(monthString);
        if (month < 0 || month > 12) {
            return null;
        }

        // 取得天
        temp = temp.substring(nIndex + 1, temp.length());
        nIndex = temp.indexOf(" ");
        int day = 0;
        if (nIndex < 0) {
            day = Integer.parseInt(temp);
            if (day < 0 || day > 32) {
                return null;
            }
        } else {
            String dayString = temp.substring(0, nIndex);
            day = Integer.parseInt(dayString);
            if (day < 0 || day > 32) {
                return null;
            }
        }

        Calendar calendar = Calendar.getInstance();

        calendar.set(year, month - 1, day);
        return calendar;
    }

    /**
     * 日期时间字符串信息转化为时间Calendar
     * @param string 字符串时间信息 格式为: 17:02:21 24小时制
     * @return 时间Calendar 失败返回null
     */
    public static Calendar stringToTime(String string) {
        if (null == string || string.isEmpty()) {
            return null;
        }
        String temp = string.trim();
        // 删除前的日期
        int nIndex = temp.indexOf(" ");
        if (nIndex != -1) {
            log.debug(string + " nIndex =" + nIndex);
            temp = temp.substring(nIndex + 1, temp.length());
        }

        // 取得时
        nIndex = temp.indexOf(":");
        if (nIndex < 1) {
            return null;
        }
        String hString = temp.substring(0, nIndex);
        int hourOfDay = Integer.parseInt(hString);
        if (hourOfDay < 0 || hourOfDay > 24) {
            return null;
        }

        // 取得分
        temp = temp.substring(nIndex + 1, temp.length());
        nIndex = temp.indexOf(":");
        if (nIndex < 1) {
            return null;
        }

        String mString = temp.substring(0, nIndex);
        int minute = Integer.parseInt(mString);
        if (minute < 0 || minute > 59) {
            return null;
        }

        // 取得秒
        temp = temp.substring(nIndex + 1, temp.length());
        nIndex = temp.indexOf(".");
        int second = 0;
        if (nIndex < 1) {
            // 没带豪秒
            second = Integer.parseInt(temp);
            if (second < 0 || second > 59) {
                return null;
            }
        } else {
            // 带豪秒
            String secondString = temp.substring(0, nIndex);
            second = Integer.parseInt(secondString);
            if (second < 0 || second > 59) {
                return null;
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return calendar;
    }

    /**
     * 获得UTC时间
     * @return 返回UTC时间
     */
    public static Date getUTCDate() {
        Calendar calendar = new GregorianCalendar();
        // 2、取得时间偏移量：
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = 0; // calendar.get(java.util.Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return calendar.getTime();
    }

    /**
     * 获得当前的UTC时间
     * @return 返回UTC时间
     */
    public static Calendar getUTCCalendar() {
        Calendar calendar = Calendar.getInstance();
        return toUTCCalendar(calendar);
    }

    /**
     * 本地时间 转化为UTC时间
     * @param calendar 本地时间，
     * @return 返回UTC时间
     */
    public static Calendar toUTCCalendar(Calendar calendar) {
        if (calendar == null) {
            return null;
        }

        Calendar utc = Calendar.getInstance();
        utc.setTime(calendar.getTime());
        TimeZone zone = TimeZone.getTimeZone("UTC");
        utc.setTimeZone(zone);
        return utc;
    }

    /**
     * 本地时间 转化为UTC时间 字符串
     * @param calendar 本地时间， 为null时，取得当时时间的UTC时间字符串
     * @return 返回UTC时间 字符串
     */
    public static String toUTCString(Calendar calendar) {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        return dateTimeToString(toUTCCalendar(calendar));
    }

    /**
     * 将收到的时间转换为标准时间(支持对夏令时的处理) Young
     * @param reporttime 距离1900年有多长时间
     * @return
     */
    public static String getUTCTime(String reporttime) {
        Long l = Long.parseLong(reporttime);
        Date datealarmtime = new Date(l * 1000);
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // sdt.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
        String times = sdt.format(datealarmtime);

        return times;
    }

    /**
     * 将时间转化为标准日期(支持对夏令时的处理) Young
     * @param reporttime 距离1900年有多长时间
     * @return
     * @throws ParseException
     */
    public static Date getUTCDate(String reporttime) throws ParseException {
        Long l = Long.parseLong(reporttime);
        Date datealarmtime = new Date(l * 1000);
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // sdt.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
        String times = sdt.format(datealarmtime);
        Date date = new Date();
        Calendar calendar = stringToDateTime(times);
        if (calendar != null) {
            date = new Date(calendar.getTimeInMillis());
        }
        return date;
    }

    /**
     * 将时间转化为时区为0的utc时间 Young
     * @param reporttime 日期时间
     * @return
     * @throws ParseException
     */
    public static String getUTCTime(Date reporttime) throws ParseException {
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdt.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
        String times = sdt.format(reporttime);
        return times;
    }

    /**
     * 得到消除了夏令时影响的日期格式化对象，专门针对获取UTC时间时消除夏令时差 Young
     * @return 日期格式化对象
     */
    public static SimpleDateFormat getSimpleDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
        return sdf;
    }

    /**
     * 将时间转化为字符串（用于时段屏蔽失效后，将内存中告警存入数据库，内存中告警时间已为UTC时间）
     * @param date
     * @return
     */
    public static String dateToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(date);
        return time;
    }

    /**
     * 将标准时间格式的字符串转化为Date类型
     * @param utcTime 时间格式为:yyyy-MM-dd HH:mm:ss; 如2012-04-20 10:21:47
     * @return Date
     * @throws ParseException
     */
    public static Date stringTimeToDate(String utcTime) throws ParseException {
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = sdf.parse(utcTime);
        // System.out.println(date);
        return date;
    }

    /**
     * 计算两个日期相差分钟数
     * @param first
     * @param second
     * @return
     * @throws ParseException
     */
    public static String getTimeDelta(String first, String second) throws ParseException {
        DecimalFormat df2 = new DecimalFormat("0.00");
        Date date1 = stringTimeToDate(first);
        Date date2 = stringTimeToDate(second);
        Double time1 = Double.valueOf(String.valueOf(date1.getTime()));
        Double time2 = Double.valueOf(String.valueOf(date2.getTime()));
        return df2.format((time2 - time1) / 1000 / 60);
    }

    /**
     * 计算两个double变量之和
     * @param double1
     * @param double2
     * @return
     */
    public static String sumDouble(String double1, String double2) {
        BigDecimal bd1 = new BigDecimal(double1);
        BigDecimal bd2 = new BigDecimal(double2);
        return String.valueOf(bd1.add(bd2));
    }

}
