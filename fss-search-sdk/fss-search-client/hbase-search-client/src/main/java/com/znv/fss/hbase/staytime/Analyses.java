package com.znv.fss.hbase.staytime;

import java.util.List;

/**
 * Created by ZNV on 2017/10/24.
 */
public class Analyses {
    private String camera_id;
    private String camera_name;
    private List<AnalysisData> analysisData;
    public void setCamera_id(String camera_id) {
        this.camera_id = camera_id;
    }

    public String getCamera_id() {
        return this.camera_id;
    }

    public void setCamera_name(String camera_name) {
        this.camera_name = camera_name;
    }

    public String getCamera_name() {
        return this.camera_name;
    }

    public void setAnalysisData(List<AnalysisData> analysisData) {
        this.analysisData = analysisData;
    }

    public List<AnalysisData> getAnalysisData() {
        return analysisData;
    }

}
