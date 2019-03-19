package com.znv.fss.es.FssSearchByTrail;

import com.alibaba.fastjson.annotation.JSONField;

public class SearchByTrailQueryHit {
    @JSONField(name = "camera_id")
    private String cameraId;
    @JSONField(name = "enter_time")
    private String enterTime;
    private String gpsx;
    private String gpsy;
    @JSONField(name = "stay_num")
    private int stayNum;

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getCameraId() {
        return cameraId;
    }

    public String getGpsx() {
        return gpsx;
    }
    public void setGpsx(String gpsx) {
        this.gpsx = gpsx;
    }

    public String getGpsy() {
        return gpsy;
    }

    public void setGpsy(String gpsy) {
        this.gpsy = gpsy;
    }

    public void setEnterTime(String enterTime) {
        this.enterTime = enterTime;
    }

    public String getEnterTime() {
        return enterTime;
    }

    public void setStayNum(int stayNum){this.stayNum = stayNum;}

    public  int getStayNum(){return stayNum;}

}

