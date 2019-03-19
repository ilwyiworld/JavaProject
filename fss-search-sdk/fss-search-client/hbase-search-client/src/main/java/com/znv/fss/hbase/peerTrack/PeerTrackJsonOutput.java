package com.znv.fss.hbase.peerTrack;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Administrator on 2017/10/21.
 */
public class PeerTrackJsonOutput {
    @JSONField(name = "reportService")
    private PeerTrackReportServiceOut reportservice;

    public PeerTrackReportServiceOut getReportservice() {
        return reportservice;
    }

    public void setReportservice(PeerTrackReportServiceOut reportservice) {
        this.reportservice = reportservice;
    }
}
