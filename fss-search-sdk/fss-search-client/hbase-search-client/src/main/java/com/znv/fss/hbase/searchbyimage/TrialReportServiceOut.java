package com.znv.fss.hbase.searchbyimage;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by User on 2017/8/10.
 */
public class TrialReportServiceOut {
    @JsonProperty("cameraId")
    private String cameraid;
    private String id; // hbase表ID
    private String type;
    private String usedTime; // 检索耗时，单位：毫秒
    private int count = 0; // 总记录数
    //private JsonResultType errorCode;
    private int errorCode;
    private TrialOutputDatas fssSearchLists; // 检索返回结果集

    public void setCameraid(String cameraid) {
        this.cameraid = cameraid;
    }

    public String getCameraid() {
        return cameraid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(String usedTime) {
        this.usedTime = usedTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public TrialOutputDatas getFssSearchLists() {
        return fssSearchLists;
    }

    public void setFssSearchLists(TrialOutputDatas fssSearchLists) {
        this.fssSearchLists = fssSearchLists;
    }
}
