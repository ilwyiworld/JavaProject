package PerformanceTest;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ct on 2016-06-30.
 */
public class rndDate {

    private String beginDate;
    private String endDate;

    public rndDate(String bDate, String eDate) {
        beginDate = bDate;
        endDate = eDate;
    }

    private long randomNum(long begin, long end) {
        long rtn = begin + (long) (Math.random() * (end - begin));
        if (rtn == begin || rtn == end) {
            return randomNum(begin, end);
        }
        return rtn;
    }

    public Date randomDate() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = format.parse(beginDate);
            Date end = format.parse(endDate);
            if (start.getTime() >= end.getTime()) {
                return null;
            }
            long date = randomNum(start.getTime(), end.getTime());
            return new Date(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
