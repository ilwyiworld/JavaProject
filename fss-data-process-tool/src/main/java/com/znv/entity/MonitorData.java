package com.znv.entity;/** * Created by ZNV on 2016/6/16. */// 界面查询时返回的数据public class MonitorData {    /* 对应 web界面监控量历史数据 -- begin */    // private String type = ""; // 1- 遥测量，2- 遥信量    private String precinctid = ""; // 监控中心编号    private String precinctname = "";// 监控中心    private String stationid = "";// 局站编号    private String stationname = "";// 局站    private String deviceid = "";// 设备编号    private String devicename = "";// 设备    private String meteid = "";// 监控量编号    private String metename = "";// 监控量名称    private String metetype = "";// 监控量类型    private String metetypename = "";// 监控量类型名称    private String reportdata = "";// 上报值    private String dataunit = "";// 上报值单位    private String datatype = "";// 上报值数据类型    private String reporttime = "";// 上报时间    private String explanation = "";// 解释    /* 对应 web界面监控量历史数据 -- end */    private String key = "";// hbase rowkey部分字段    private long reporttimel = 0;// hbase rowkey上报时间字段    public String getPrecinctid() {        return precinctid;    }    public void setPrecinctid(String precinctid) {        this.precinctid = precinctid;    }    public String getPrecinctname() {        return precinctname;    }    public void setPrecinctname(String precinctname) {        this.precinctname = precinctname;    }    public String getStationid() {        return stationid;    }    public void setStationid(String stationId) {        this.stationid = stationId;    }    public String getStationname() {        return stationname;    }    public void setStationname(String stationName) {        this.stationname = stationName;    }    public String getDeviceid() {        return deviceid;    }    public void setDeviceid(String deviceId) {        this.deviceid = deviceId;    }    public String getDevicename() {        return devicename;    }    public void setDevicename(String deviceName) {        this.devicename = deviceName;    }    public String getMeteid() {        return meteid;    }    public void setMeteid(String meteId) {        this.meteid = meteId;    }    public String getMetename() {        return metename;    }    public void setMetename(String meteName) {        this.metename = meteName;    }    public String getMetetype() {        return metetype;    }    public void setMetetype(String meteType) {        this.metetype = meteType;    }    public String getMetetypename() {        return metetypename;    }    public void setMetetypename(String meteTypeName) {        this.metetypename = meteTypeName;    }    public String getReportdata() {        return reportdata;    }    public void setReportdata(String reportValue) {        this.reportdata = reportValue;    }    public String getDataunit() {        return dataunit;    }    public void setDataunit(String dataunit) {        this.dataunit = dataunit;    }    public String getDatatype() {        return datatype;    }    public void setDatatype(String datatype) {        this.datatype = datatype;    }    public String getReportTime() {        return reporttime;    }    public void setReportTime(String reportTime) {        this.reporttime = reportTime;    }    public String getExplanation() {        return explanation;    }    public void setExplanation(String explanation) {        this.explanation = explanation;    }    public String getKey() {        return key;    }    public void setKey(String key) {        this.key = key;    }    public long getReportTimeL() {        return reporttimel;    }    public void setReportTimeL(long reportTimel) {        this.reporttimel = reportTimel;    }}