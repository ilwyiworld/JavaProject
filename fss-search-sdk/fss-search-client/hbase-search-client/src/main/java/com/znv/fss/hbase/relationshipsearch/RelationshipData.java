package com.znv.fss.hbase.relationshipsearch;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Administrator on 2017/6/7.
 */
public class RelationshipData {
    @JSONField(name = "imageData")
    private String imagedata;
    @JSONField(name = "peerCount")
    private long peercount; // 同行次数
    @JSONField(name = "peerTime")
    private long peertime; // 同行时长，单位为秒
    @JSONField(name = "peerLibId")
    private int peerlibid;
    @JSONField(name = "peerPersonId")
    private String peerpersonid;
    private String imgUrl;

    public void setImagedata(String imagedata) {
        this.imagedata = imagedata;
    }

    public String getImagedata() {
        return imagedata;
    }

    public long getPeercount() {
        return peercount;
    }

    public void setPeercount(long peercount) {
        this.peercount = peercount;
    }

    public long getPeertime() {
        return peertime;
    }

    public void setPeertime(long peertime) {
        this.peertime = peertime;
    }

    public int getPeerlibid() {
        return peerlibid;
    }

    public void setPeerlibid(int peerlibid) {
        this.peerlibid = peerlibid;
    }

    public String getPeerpersonid() {
        return peerpersonid;
    }

    public void setPeerpersonid(String peerpersonid) {
        this.peerpersonid = peerpersonid;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
