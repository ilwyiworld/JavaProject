package com.znv.hbase.coprocessor.endpoint.staytimestat;

/**
 * Created by ZNV on 2017/5/27.
 */
public class StayTimeSearchOutData {
    private long durationTime = 0L; // 列里面的hbase时长
    private int count = 0; // 组员个数，用于排序
    private int simCount = 0; // 相似度超过阈值的计数
    private int groupId = -1; // 所属分组Id，默认-1：未分组
    private int libId = 0;
    private String personId = "0";
    private byte[] rowKey;
    private byte[] feature;
    private String cameraId = "";
    private String cameraName = "";
    private String imgUrl = "";

    public long getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(long durationTime) {
        this.durationTime = durationTime;
    }

    public byte[] getRowKey() {
        byte[] temp = this.rowKey;
        return temp;
    }

    public void setRowKey(byte[] rowKey) {
        if (rowKey != null) {
            this.rowKey = rowKey.clone();
        }
    }

    public byte[] getFeature() {
        byte[] temp = this.feature;
        return temp;
    }

    public void setFeature(byte[] feature) {
        if (feature != null) {
            this.feature = feature.clone();
        }
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSimCount() {
        return simCount;
    }

    public void setSimCount(int simCount) {
        this.simCount = simCount;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getLibId() {
        return libId;
    }

    public void setLibId(int libId) {
        this.libId = libId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId= cameraId;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName= cameraName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
