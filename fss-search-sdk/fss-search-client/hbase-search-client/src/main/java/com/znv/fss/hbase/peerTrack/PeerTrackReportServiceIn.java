package com.znv.fss.hbase.peerTrack;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Administrator on 2017/10/21.
 */
public class PeerTrackReportServiceIn {
    private String id;
    private String type;
    @JSONField(name = "peerTrackParam")
    private PeerTrackParam peerTrackParam;

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

    public PeerTrackParam getPeerTrackParam() {
        return peerTrackParam;
    }

    public void setPeerTrackParam(PeerTrackParam peerTrackParam) {
        this.peerTrackParam = peerTrackParam;
    }
}
