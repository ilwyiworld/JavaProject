package com.znv.fss.hbase.mob;

import com.alibaba.fastjson.annotation.JSONField;
import com.znv.fss.hbase.mob.MOBWriteReportServiceOut;

/**
 * Created by Administrator on 2017/11/10.
 */
public class MOBWriteJsonOutput {
    @JSONField(name = "reportService")
    private MOBWriteReportServiceOut reportService;

    public MOBWriteReportServiceOut getReportService() {
        return reportService;
    }

    public void setReportService(MOBWriteReportServiceOut reportService) {
        this.reportService = reportService;
    }
}
