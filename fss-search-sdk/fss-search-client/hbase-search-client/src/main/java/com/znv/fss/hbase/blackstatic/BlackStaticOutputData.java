package com.znv.fss.hbase.blackstatic;

/**
 * Created by estine on 2017/2/20.
 */
public class BlackStaticOutputData {
    private String imageData = ""; // base64编码后图片数据（方便页面呈现）
    private String personName = "";
    private String imageName = "";
    private String personId = "";
    private String startTime = "";
    private String endTime = "";
    private int age = 0;
    private int sex = 0;
    private int fcPid = 0;
    private int controlLevel = 0;
    private int flag = 0;
    private int sim = 0;

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getFcPid() {
        return fcPid;
    }

    public void setFcPid(int fcPid) {
        this.fcPid = fcPid;
    }

    public int getControlLevel() {
        return controlLevel;
    }

    public void setControlLevel(int controlLevel) {
        this.controlLevel = controlLevel;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getSim() {
        return sim;
    }

    public void setSim(int sim) {
        this.sim = sim;
    }
}
