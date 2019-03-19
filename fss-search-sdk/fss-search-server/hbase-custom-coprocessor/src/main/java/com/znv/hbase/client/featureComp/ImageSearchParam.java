package com.znv.hbase.client.featureComp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Estine on 2016/12/23.
 */
public class ImageSearchParam {

    //private String searchFeature = ""; // 查询图片特征值
    private List<String> searchFeatures = new ArrayList<String>(); // 查询图片特征值
    private String selType ="";  //交并集选择， 1-交集  2-并集
    private float threshold = 0.92f; // 相似度阈值，默认值92%
    private String startTime = ""; // 开始时间
    private String endTime = ""; // 结束时间
    private String searchType = ""; // 查询类型，1-以脸搜脸 or 2-人物关系查询
    private List<String> cameraIds = new ArrayList<String>(); // 多个cameraId
    private List<String> officeIds = new ArrayList<String>(); // 多个officeId

    public List<String> getSearchFeatures() {
        return searchFeatures;
    }

    public void setSearchFeatures(List<String> searchFeatures) {
        this.searchFeatures = searchFeatures;
    }

    public String getSelType() {
        return selType;
    }

    public void setSelType(String selType) {
        this.selType = selType;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
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

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public List<String> getOfficeIds() {
        return officeIds;
    }

    public void setOfficeIds(List<String> officeIds) {
        this.officeIds = officeIds;
    }
}
