package com.znv.fss.hbase.peerTrack;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Administrator on 2017/10/21.
 */
public class PeerTrackJsonInput {
    @JSONField(name = "reportService")
    private PeerTrackReportServiceIn reportservice;

    public PeerTrackReportServiceIn getReportservice() {
        return reportservice;
    }

    public void setReportservice(PeerTrackReportServiceIn reportservice) {
        this.reportservice = reportservice;
    }
}
