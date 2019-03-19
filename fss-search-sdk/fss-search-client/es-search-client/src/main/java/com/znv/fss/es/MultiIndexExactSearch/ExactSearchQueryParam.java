package com.znv.fss.es.MultiIndexExactSearch;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by User on 2017/8/25.
 */
public class ExactSearchQueryParam {
    @JSONField(name = "event_id")
    private String eventId;
    @JSONField(name = "enter_time_start")
    private String enterTimeStart;
    @JSONField(name = "enter_time_end")
    private String enterTimeEnd;
    @JSONField(name = "office_id")
    private List<String> officeId;
    @JSONField(name = "camera_id")
    private List<String> cameraId;
    @JSONField(name = "office_name")
    private String officeName;
    @JSONField(name = "camera_name")
    private String cameraName;
    @JSONField(name = "person_id")
    private String personId;
    @JSONField(name = "age_start")
    private int ageStart;
    @JSONField(name = "age_end")
    private int ageEnd = -1;
    private int gender = -1;
    private int glass = -1;
    private int mask = -1;
    private int race = -1;
    private int beard = -1;
    private int emotion = -1;
    @JSONField(name = "eye_open")
    private int eyeOpen = -1;
    @JSONField(name = "mouth_open")
    private int mouthOpen = -1;
    @JSONField(name = "is_calcSim")
    private boolean isCalcSim;
    @JSONField(name = "feature_value")
    private List<String> featureValue;
    @JSONField(name = "sim_threshold")
    private double simThreshold;
    @JSONField(name = "filter_type")
    private String filterType;
    private String sortField;
    private String sortOrder;
    private int from;
    private int size;
    @JSONField(name = "coarse_code_num")
    private int coarseCodeNum;

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventId() {
        return this.eventId;
    }

    public void setEnterTimeStart(String enterTimeStart) {
        this.enterTimeStart = enterTimeStart;
    }

    public String getEnterTimeStart() {
        return this.enterTimeStart;
    }

    public void setEnterTimeEnd(String enterTimeEnd) {
        this.enterTimeEnd = enterTimeEnd;
    }

    public String getEnterTimeEnd() {
        return this.enterTimeEnd;
    }

    public void setOfficeId(List<String> officeId) {
        this.officeId = officeId;
    }

    public List<String> getOfficeId() {
        return this.officeId;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getOfficeName() {
        return this.officeName;
    }

    public void setCameraId(List<String> cameraId) {
        this.cameraId = cameraId;
    }

    public List<String> getCameraId() {
        return this.cameraId;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getCameraName() {
        return this.cameraName;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonId() {
        return this.personId;
    }

    public void setAgeStart(int ageStart) {
        this.ageStart = ageStart;
    }

    public int getAgeStart() {
        return this.ageStart;
    }

    public void setAgeEnd(int ageEnd) {
        this.ageEnd = ageEnd;
    }

    public int getAgeEnd() {
        return this.ageEnd;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getGender() {
        return this.gender;
    }

    public void setGlass(int glass) {
        this.glass = glass;
    }

    public int getGlass() {
        return this.glass;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return this.mask;
    }

    public void setRace(int race) {
        this.race = race;
    }

    public int getRace() {
        return this.race;
    }

    public void setBeard(int beard) {
        this.beard = beard;
    }

    public int getBeard() {
        return this.beard;
    }

    public void setEmotion(int emotion) {
        this.emotion = emotion;
    }

    public int getEmotion() {
        return this.emotion;
    }

    public void setEyeOpen(int eyeOpen) {
        this.eyeOpen = eyeOpen;
    }

    public int getEyeOpen() {
        return this.eyeOpen;
    }

    public void setMouthOpen(int mouthOpen) {
        this.mouthOpen = mouthOpen;
    }

    public int getMouthOpen() {
        return this.mouthOpen;
    }

    public void setIsCalcSim(boolean isCalcSim) {
        this.isCalcSim = isCalcSim;
    }

    public boolean getIsCalcSim() {
        return this.isCalcSim;
    }

    public void setSimThreshold(double simThreshold) {
        this.simThreshold = simThreshold;
    }

    public double getSimThreshold() {
        return this.simThreshold;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getFilterType() {
        return this.filterType;
    }

    public void setFeatureValue(List<String>featureValue) {
        this.featureValue = featureValue;
    }

    public List<String> getFeatureValue() {
        return featureValue;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortField() {
        return this.sortField;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSortOrder() {
        return this.sortOrder;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getFrom() {
        return this.from;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }

    public void setCoarseCodeNum(int coarseCodeNum) {
        this.coarseCodeNum = coarseCodeNum;
    }

    public int getCoarseCodeNum() {
        return coarseCodeNum;
    }
}
