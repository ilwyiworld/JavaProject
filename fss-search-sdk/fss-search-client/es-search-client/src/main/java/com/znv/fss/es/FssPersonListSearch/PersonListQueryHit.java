package com.znv.fss.es.FssPersonListSearch;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Administrator on 2017/12/5.
 */
public class PersonListQueryHit {
    private double score;
    private double similarity;
    @JSONField(name = "person_id")
    private String personId;
    @JSONField(name = "lib_id")
    private int libId;
    @JSONField(name = "enter_time")
    private String enterTime;
    @JSONField(name = "leave_time")
    private String leaveTime;
    @JSONField(name = "op_time")
    private String opTime;
    @JSONField(name = "img_url")
    private String imgUrl;
    @JSONField(name = "big_picture_uuid")
    private String bigPictureUuid;
    @JSONField(name = "is_alarm")
    private String isAlarm;
    @JSONField(name = "camera_id")
    private String cameraId;
    @JSONField(name = "camera_name")
    private String cameraName;
    @JSONField(name = "office_id")
    private String officeId;
    @JSONField(name = "office_name")
    private String officeName;

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setLibId(int libId) {
        this.libId = libId;
    }

    public int getLibId() {
        return libId;
    }

    public void setEnterTime(String enterTime) {
        this.enterTime = enterTime;
    }

    public String getEnterTime() {
        return enterTime;
    }

    public void setLeaveTime(String leaveTime) {
        this.leaveTime = leaveTime;
    }

    public String getLeaveTime() {
        return leaveTime;
    }

    public void setOpTime(String opTime) {
        this.opTime = opTime;
    }

    public String getOpTime() {
        return opTime;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setBigPictureUuid(String bigPictureUuid) {
        this.bigPictureUuid = bigPictureUuid;
    }

    public String getBigPictureUuid() {
        return bigPictureUuid;
    }

    public void setIsAlarm(String isAlarm) {
        this.isAlarm = isAlarm;
    }

    public String getIsAlarm() {
        return isAlarm;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getOfficeName() {
        return officeName;
    }
}
