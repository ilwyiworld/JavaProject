package com.znv.fss.hbase.peerTrack;

import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;

import java.util.List;

/**
 * Created by Administrator on 2017/10/21.
 */
public class PeerTrackOutputData {
    // private String cameraId = ""; // 摄像头ID
    // private float gpsx = 0.0f;
    // private float gpsy = 0.0f;
    // private long durationTime = 0l; // 目标人逗留时间
    // private String enterTime = ""; // 目标人进入时间
    // private String uuid = ""; // 目标人UUID
    private List<PeerOutputData> peerList; // 同行人相关信息
    private ImageSearchOutData targetData; // 目标人信息

    // public String getCameraId() {
    // return cameraId;
    // }
    //
    // public void setCameraId(String cameraId) {
    // this.cameraId = cameraId;
    // }
    //
    // public float getGpsx() {
    // return gpsx;
    // }
    //
    // public void setGpsx(float gpsx) {
    // this.gpsx = gpsx;
    // }
    //
    // public float getGpsy() {
    // return gpsy;
    // }
    //
    // public void setGpsy(float gpsy) {
    // this.gpsy = gpsy;
    // }
    //
    // public long getDurationTime() {
    // return durationTime;
    // }
    //
    // public void setDurationTime(long durationTime) {
    // this.durationTime = durationTime;
    // }
    //
    // public String getEnterTime() {
    // return enterTime;
    // }
    //
    // public void setEnterTime(String enterTime) {
    // this.enterTime = enterTime;
    // }
    //
    // public String getUuid() {
    // return uuid;
    // }
    //
    // public void setUuid(String uuid) {
    // this.uuid = uuid;
    // }

    public List<PeerOutputData> getPeerList() {
        return peerList;
    }

    public void setPeerList(List<PeerOutputData> peerList) {
        this.peerList = peerList;
    }

    public ImageSearchOutData getTargetData() {
        return targetData;
    }

    public void setTargetData(ImageSearchOutData targetData) {
        this.targetData = targetData;
    }
}
