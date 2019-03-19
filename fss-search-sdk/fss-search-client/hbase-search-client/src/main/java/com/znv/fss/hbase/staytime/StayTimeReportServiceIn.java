package com.znv.fss.hbase.staytime;

/**
 * Created by ZNV on 2017/6/7.
 */
public class StayTimeReportServiceIn {
    private String id;
    private String type;
    private String analysis;
    private int size;
    private StayTimeInput stayTime;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public void setStayTime(StayTimeInput stayTime) {
        this.stayTime = stayTime;
    }

    public StayTimeInput getStayTime() {
        return this.stayTime;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getAnalysis() {
        return this.analysis;
    }
}
