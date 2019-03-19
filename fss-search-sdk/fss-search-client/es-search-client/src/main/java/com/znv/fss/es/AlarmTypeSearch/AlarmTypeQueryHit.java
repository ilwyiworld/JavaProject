package com.znv.fss.es.AlarmTypeSearch;

import com.alibaba.fastjson.annotation.JSONField;

public class AlarmTypeQueryHit {
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
    @JSONField(name = "camera_id")
    private String cameraId;
    @JSONField(name = "camera_name")
    private String cameraName;
    @JSONField(name = "office_id")
    private String officeId;
    @JSONField(name = "office_name")
    private String officeName;
    @JSONField(name = "person_name")
    private String personName;
    @JSONField(name = "alarm_type")
    private int alarmType;
    @JSONField(name = "control_event_id")
    private String controlEventId;

    private String birth;

    @JSONField(name="img_width")
    private int imgWidth;
    @JSONField(name="img_height")
    private int imgHeight;
    @JSONField(name="left_pos")
    private int leftPos;
    @JSONField(name="right_pos")
    private int rightPos;
    private int top;
    private int bottom;
    private String uuid;

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    public int getImgHeight() {
        return imgHeight;
    }

    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }

    public int getLeftPos() {
        return leftPos;
    }

    public void setLeftPos(int leftPos) {
        this.leftPos = leftPos;
    }

    public int getRightPos() {
        return rightPos;
    }

    public void setRightPos(int rightPos) {
        this.rightPos = rightPos;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public String getControlEventId() {
        return controlEventId;
    }

    public void setControlEventId(String controlEventId) {
        this.controlEventId = controlEventId;
    }
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public int getLibId() {
        return libId;
    }

    public void setLibId(int libId) {
        this.libId = libId;
    }

    public String getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(String enterTime) {
        this.enterTime = enterTime;
    }

    public String getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(String leaveTime) {
        this.leaveTime = leaveTime;
    }

    public String getOpTime() {
        return opTime;
    }

    public void setOpTime(String opTime) {
        this.opTime = opTime;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getBigPictureUuid() {
        return bigPictureUuid;
    }

    public void setBigPictureUuid(String bigPictureUuid) {
        this.bigPictureUuid = bigPictureUuid;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public int getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(int alarmType) {
        this.alarmType = alarmType;
    }
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
