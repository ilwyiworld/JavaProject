package com.znv.fss.hbase.searchbyimage;

import java.util.List;

/**
 * Created by Qing Li on 2016/12/22.
 */
public class FssInPutParams {
    private int simThreshold = 89; // 相似度：89~100,默认89
    //private String searchFeature = ""; // 查询图片特征值
    private List<String> searchFeatures; // 查询图片特征值
    private String startTime = ""; // 开始时间
    private String endTime = ""; // 结束时间
    private List<String> cameraIds;
    private Page page;
    private String sortType = ""; // 新增排序方式，0：相似度倒排序，1：抓拍时间倒排序
    private String sortOrder = ""; // asc：升序，desc：降序
    private String picFilterType = ""; //交并集，1：交集，2-并集

    public int getSimThreshold() {
        return simThreshold;
    }

    public void setSimThreshold(int simThreshold) {
        this.simThreshold = simThreshold;
    }

    public List<String> getSearchFeatures() {
        return searchFeatures;
    }

    public void setSearchFeatures(List<String> searchFeatures) {
        this.searchFeatures = searchFeatures;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<String> getCameraIds() {
        return cameraIds;
    }

    public void setCameraIds(List<String> cameraIds) {
        this.cameraIds = cameraIds;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getPicFilterType() {
        return picFilterType;
    }

    public void setPicFilterType(String picFilterType) {
        this.picFilterType = picFilterType;
    }
}
