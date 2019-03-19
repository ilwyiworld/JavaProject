package com.znv.hbase.coprocessor.endpoint.searchByImage;

import com.google.protobuf.ByteString;

/**
 * Created by estine on 2016/12/24. 返回hbase表查询出的疑似人相关信息
 */
public class ImageSearchOutData {
    private float suspectSim = 0f; //
    private byte[] suspectRowKey = null; // 保存当前图片的rowkey
    private ByteString bytesRowKey; // phoenix表中rowkey为int型
    private String cameraId;
    private String enterTime;
    private String leaveTime;
    private long durationTime = 1; // 在摄像头前持续时长
    private float gpsx = 0f;
    private float gpsy = 0f;
    private String uuid = ""; // [estine]
    private String cameraName = ""; // [estine]

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public float getSuspectSim() {
        return suspectSim;
    }

    public void setSuspectSim(float suspectSim) {
        this.suspectSim = suspectSim;
    }

    public byte[] getSuspectRowKey() {
        return suspectRowKey;
    }

    public void setSuspectRowKey(byte[] suspectRowKey) {
        this.suspectRowKey = suspectRowKey;
    }

    public ByteString getBytesRowKey() {
        return bytesRowKey;
    }

    public void setBytesRowKey(ByteString bytesRowKey) {
        this.bytesRowKey = bytesRowKey;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
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

    public long getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(long durationTime) {
        this.durationTime = durationTime;
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
