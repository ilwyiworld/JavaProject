package com.znv.fss.hbase.peerTrack;

import com.alibaba.fastjson.annotation.JSONField;
import com.znv.fss.hbase.JsonResultType;

import java.util.List;

/**
 * Created by Administrator on 2017/10/21.
 */
public class PeerTrackReportServiceOut {
    private String id;
    private String type;
    @JSONField(name = "errorCode")
    private JsonResultType errorcode;
    private long time;
    private int count;
    @JSONField(name = "peerTrackData")
    private List<PeerTrackOutputData> peerTrackData;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setErrorcode(JsonResultType errorcode) {
        this.errorcode = errorcode;
    }

    public JsonResultType getErrorcode() {
        return errorcode;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public List<PeerTrackOutputData> getPeerTrackData() {
        return peerTrackData;
    }

    public void setPeerTrackData(List<PeerTrackOutputData> peerTrackData) {
        this.peerTrackData = peerTrackData;
    }
}
