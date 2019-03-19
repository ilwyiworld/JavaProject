package com.znv.fss.hbase.peerTrack;

/**
 * Created by Administrator on 2017/10/23.
 */
public class PeerOutputData {
    private String peerEnterTime = ""; // 同行人进入时间
    private String peerUuid = ""; // 同行人UUID
    private long intervalTime = 0l; // 同行人与目标人时间间隔
    private String imgUrl = ""; // 小图UUID

    public String getPeerEnterTime() {
        return peerEnterTime;
    }

    public void setPeerEnterTime(String peerEnterTime) {
        this.peerEnterTime = peerEnterTime;
    }

    public String getPeerUuid() {
        return peerUuid;
    }

    public void setPeerUuid(String peerUuid) {
        this.peerUuid = peerUuid;
    }

    public long getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(long intervalTime) {
        this.intervalTime = intervalTime;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
