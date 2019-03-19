package com.znv.fss.hbase.staytime;

/**
 * Created by ZNV on 2017/6/7.
 */
public class StayTimeInput {
    private String[] officeIds;
    private String[] cameraIds;
    private String startTime;
    private String endTime;
    private String reportType = "02"; // 01:摄像头ID,02局站ID,默认局站
    private int simThreshold = 89; // 相似度：50~100,默认89

    public void setOfficeIds(String[] officeIds) {
        if (officeIds != null) {
            this.officeIds = officeIds.clone();
        }
    }

    public String[] getOfficeIds() {
        String[] temp = this.officeIds;
        return temp;
    }

    public void setCameraIds(String[] cameraIds) {
        if (cameraIds != null) {
            this.cameraIds = cameraIds.clone();
        }
    }

    public String[] getCameraIds() {
        String[] temp = this.cameraIds;
        return temp;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEndTime() {
        return this.endTime;
    }

    public void setSimThreshold(int simThreshold) {
        this.simThreshold = simThreshold;
    }

    public int getSimThreshold() {
        return this.simThreshold;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReportType() {
        return this.reportType;
    }
}
