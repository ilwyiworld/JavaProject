package com.znv.fss.es.FssPersonListSearch;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by Administrator on 2017/12/5.
 */
public class PersonListQueryParam {
    @JSONField(name = "start_time")
    private String startTime;
    @JSONField(name = "end_time")
    private String endTime;
    @JSONField(name = "lib_id")
    private List<Integer> libId;
    @JSONField(name = "personlib_type")
    private int personlibType;
    @JSONField(name = "minimum_should_match")
    private int minimumShouldMatch;
    @JSONField(name = "person_name")
    private String personName;
    @JSONField(name = "flag")
    private String flag;
    @JSONField(name = "is_calcSim")
    private Boolean isCalcSim;
    @JSONField(name = "is_del")
    private Boolean isDel;
    @JSONField(name = "feature_name")
    private String featureName;
    @JSONField(name = "sort_field1")
    private String sortField1;
    @JSONField(name = "sort_field2")
    private String sortField2;
    @JSONField(name = "sort_order1")
    private String sortOrder1;
    @JSONField(name = "sort_order2")
    private String sortOrder2;
    @JSONField(name = "feature_value")
    private String featureValue;
    @JSONField(name = "sim_threshold")
    private float simThreshold;
    private int from;
    private int size;

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setLibId(List<Integer> libId) {
        this.libId = libId;
    }

    public List<Integer> getLibId() {
        return libId;
    }

    public void setPersonlibType(int personlibType) {
        this.personlibType = personlibType;
    }

    public int getPersonlibType() {
        return personlibType;
    }

    public void setMinimumShouldMatch(int minimumShouldMatch) {
        this.minimumShouldMatch = minimumShouldMatch;
    }

    public int getMinimumShouldMatch() {
        return minimumShouldMatch;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonName() {
        return personName;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getFlag() {
        return flag;
    }

    public void setIsCalcSim(boolean isCalcSim) {
        this.isCalcSim = isCalcSim;
    }

    public boolean getIsCalcSim() {
        return isCalcSim;
    }

    public void setIsDel(boolean isDel) {
        this.isDel = isDel;
    }

    public boolean getIsDel() {
        return isDel;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureValue(String featureValue) {
        this.featureValue = featureValue;
    }

    public String getFeatureValue() {
        return featureValue;
    }

    public void setSimThreshold(float simThreshold) {
        this.simThreshold = simThreshold;
    }

    public float getSimThreshold() {
        return simThreshold;
    }

    public void setSortField1(String sortField1) {
        this.sortField1 = sortField1;
    }

    public String getSortField1() {
        return sortField1;
    }

    public void setSortField2(String sortField2) {
        this.sortField2 = sortField2;
    }

    public String getSortField2() {
        return sortField2;
    }

    public void setSortOrder1(String sortOrder1) {
        this.sortOrder1 = sortOrder1;
    }

    public String getSortOrder1() {
        return sortOrder1;
    }

    public void setSortOrder2(String sortOrder1) {
        this.sortOrder2 = sortOrder2;
    }

    public String getSortOrder2() {
        return sortOrder2;
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
}
