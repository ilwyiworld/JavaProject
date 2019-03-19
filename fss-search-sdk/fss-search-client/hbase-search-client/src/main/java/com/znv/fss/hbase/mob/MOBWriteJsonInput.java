package com.znv.fss.hbase.mob;

import com.alibaba.fastjson.annotation.JSONField;
import com.znv.fss.hbase.mob.MOBWriteReportServiceIn;

/**
 * Created by Administrator on 2017/11/10.
 */
public class MOBWriteJsonInput {
    @JSONField(name = "reportService")
    private MOBWriteReportServiceIn reportservice;

    public MOBWriteReportServiceIn getReportservice() {
        return reportservice;
    }

    public void setReportservice(MOBWriteReportServiceIn reportservice) {
        this.reportservice = reportservice;
    }
}
