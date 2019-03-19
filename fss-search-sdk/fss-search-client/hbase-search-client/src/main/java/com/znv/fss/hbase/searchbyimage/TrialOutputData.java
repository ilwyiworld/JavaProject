package com.znv.fss.hbase.searchbyimage;

/**
 * Created by User on 2017/8/10.
 */
public class TrialOutputData {
    private String cameraId = ""; // 摄像头ID
    private float gpsx = 0.0f;
    private float gpsy = 0.0f;

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public float getGpsx() {
        return gpsx;
    }

    public void setGpsx(float gpsx) {
        this.gpsx = gpsx;
    }

    public float getGpsy() {
        return gpsy;
    }

    public void setGpsy(float gpsy) {
        this.gpsy = gpsy;
    }
}
