package com.znv.fss.hbase.relationshipsearch;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Administrator on 2017/6/7.
 */
public class RelationshipJsonOutput {
    @JSONField(name = "reportService")
    private RelationReportServiceOut reportservice;

    public void setReportservice(RelationReportServiceOut reportservice) {
        this.reportservice = reportservice;
    }

    public RelationReportServiceOut getReportservice() {
        return reportservice;
    }
}
