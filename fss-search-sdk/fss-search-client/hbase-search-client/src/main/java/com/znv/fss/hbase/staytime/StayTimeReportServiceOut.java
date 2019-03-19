package com.znv.fss.hbase.staytime;

/**
 * Created by ZNV on 2017/6/7.
 */
public class StayTimeReportServiceOut {
    private String id;
    private String type;
    private String errorCode;
    private String time;
    private int count;
    private StayTimeOut[] stayTimes;
    private Analyses[] analyses;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setStayTimes(StayTimeOut[] stayTimes) {
        if (stayTimes != null) {
            this.stayTimes = stayTimes.clone();
        }
    }

    public StayTimeOut[] getStayTimes() {
        StayTimeOut[] temp = this.stayTimes;
        return temp;
    }

    public void setAnalyses(Analyses[] analyses) {
        if (analyses != null) {
            this.analyses = analyses.clone();
        }
    }

    public Analyses[] getAnalyses() {
        Analyses[] temp = this.analyses;
        return temp;
    }
}
