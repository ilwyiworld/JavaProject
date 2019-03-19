package com.znv.fss.es.AlarmTypeSearch;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class AlarmTypeQueryParam {
    @JSONField(name = "enter_time_start")
    private String enterTimeStart;
    @JSONField(name = "enter_time_end")
    private String enterTimeEnd;
    @JSONField(name = "office_id")
    private List<String> officeId;
    @JSONField(name = "camera_id")
    private List<String> cameraId;
    @JSONField(name = "feature_name")
    private String featureName;
    @JSONField(name = "feature_value")
    //private String featureValue;
    private List<String> featureValue;
    @JSONField(name = "sim_threshold")
    private double simThreshold;
    //@JSONField(name = "sort_field")
    private String sortField;
    //@JSONField(name = "sort_order")
    private String sortOrder;
    @JSONField(name = "is_calcSim")
    private boolean calcSim;
    @JSONField(name = "control_event_id")
    private List<String> controlEventId;
    @JSONField(name = "is_camera")
    private boolean isCamera;
    @JSONField(name = "is_office")
    private boolean isOffice;
    @JSONField(name = "is_control_event")
    private boolean isControlEvent;
    private int from;
    private int size;

    public List<String> getControlEventId() {
        return controlEventId;
    }

    public void setControlEventId(List<String> controlEventId) {
        this.controlEventId = controlEventId;
    }


    public String getEnterTimeStart() {
        return enterTimeStart;
    }

    public void setEnterTimeStart(String enterTimeStart) {
        this.enterTimeStart = enterTimeStart;
    }

    public String getEnterTimeEnd() {
        return enterTimeEnd;
    }

    public void setEnterTimeEnd(String enterTimeEnd) {
        this.enterTimeEnd = enterTimeEnd;
    }

    public List<String> getOfficeId() {
        return officeId;
    }

    public void setOfficeId(List<String> officeId) {
        this.officeId = officeId;
    }

    public List<String> getCameraId() {
        return cameraId;
    }

    public void setCameraId(List<String> cameraId) {
        this.cameraId = cameraId;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public List<String> getFeatureValue() {
        return featureValue;
    }

    public void setFeatureValue(List<String> featureValue) {
        this.featureValue = featureValue;
    }

    public double getSimThreshold() {
        return simThreshold;
    }

    public void setSimThreshold(double simThreshold) {
        this.simThreshold = simThreshold;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean getCalcSim() {
        return calcSim;
    }

    public void setCalcSim(boolean calcSim) {
        this.calcSim = calcSim;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean getIsCamera() {
        return isCamera;
    }

    public void setIsCamera(boolean isCamera) {
        this.isCamera = isCamera;
    }

    public boolean getIsOffice() {
        return isOffice;
    }

    public void setIsOffice(boolean isOffice) {
        this.isOffice = isOffice;
    }

    public boolean getIsControlEvent() {
        return isControlEvent;
    }

    public void setIsControlEvent(boolean isControlEvent) {
        this.isControlEvent = isControlEvent;
    }
}
