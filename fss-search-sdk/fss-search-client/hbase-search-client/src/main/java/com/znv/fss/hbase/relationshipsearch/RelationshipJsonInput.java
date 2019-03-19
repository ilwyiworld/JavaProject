package com.znv.fss.hbase.relationshipsearch;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Administrator on 2017/6/6.
 */
public class RelationshipJsonInput {
    @JSONField(name = "reportService")
    private RelationReportServiceIn reportservice;

    public void setReportservice(RelationReportServiceIn reportservice) {
        this.reportservice = reportservice;
    }

    public RelationReportServiceIn getReportservice() {
        return reportservice;
    }
}
