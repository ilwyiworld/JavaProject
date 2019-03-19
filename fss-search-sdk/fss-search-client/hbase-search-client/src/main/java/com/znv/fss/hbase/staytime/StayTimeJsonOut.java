package com.znv.fss.hbase.staytime;

/**
 * Created by ZNV on 2017/6/7.
 */
public class StayTimeJsonOut {
    private StayTimeReportServiceOut reportService;

    public void setReportService(StayTimeReportServiceOut reportService) {
        this.reportService = reportService;
    }

    public StayTimeReportServiceOut getReportService() {
        return reportService;
    }
}
