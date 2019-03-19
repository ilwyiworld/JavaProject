package com.znv.fss.es.FssSearchByTrail;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class SearchByTrailQueryParam {
    @JSONField(name = "enter_time_start")
    private String enterTimeStart;
    @JSONField(name = "enter_time_end")
    private String enterTimeEnd;
    @JSONField(name = "office_id")
    private List<String> officeId;
    @JSONField(name = "camera_id")
    private List<String> cameraId;
    @JSONField(name = "feature_value")
    private List<String> featureValue;
    @JSONField(name = "sim_threshold")
    private double simThreshold;
    @JSONField(name = "filter_type")
    private String filterType;
    @JSONField(name = "is_lopq")
    private boolean isLopq;

    @JSONField(name = "sort_field")
    private String sortField;
    @JSONField(name = "sort_order")
    private String sortOrder;
    private int from;
    private int size;
    @JSONField(name = "coarse_code_num")
    private int coarseCodeNum;

    public void setEnterTimeStart(String enterTimeStart) {
        this.enterTimeStart = enterTimeStart;
    }

    public String getEnterTimeStart() {
        return enterTimeStart;
    }

    public void setEnterTimeEnd(String enterTimeEnd) {
        this.enterTimeEnd = enterTimeEnd;
    }

    public String getEnterTimeEnd() {
        return enterTimeEnd;
    }

    public void setOfficeId(List<String> officeId) {
        this.officeId = officeId;
    }

    public List<String> getOfficeId() {
        return officeId;
    }

    public void setCameraId(List<String> cameraId) {
        this.cameraId = cameraId;
    }

    public List<String> getCameraId() {
        return cameraId;
    }

    public void setFeatureValue(List<String>featureValue) {
        this.featureValue = featureValue;
    }

    public List<String> getFeatureValue() {
        return featureValue;
    }

    public void setSimThreshold(double simThreshold) {
        this.simThreshold = simThreshold;
    }

    public double getSimThreshold() {
        return simThreshold;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setIsLopq(boolean isLopq) {
        this.isLopq = isLopq;
    }

    public boolean getIsLopq() {
        return isLopq;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getFrom() {
        return from;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setCoarseCodeNum(int coarseCodeNum) {
        this.coarseCodeNum = coarseCodeNum;
    }

    public int getCoarseCodeNum() {
        return coarseCodeNum;
    }
}
