package com.znv.fss.hbase.mob;

import com.alibaba.fastjson.annotation.JSONField;
import com.znv.fss.hbase.JsonResultType;
import com.znv.fss.hbase.mob.MOBWriteOutputData;

import java.util.List;

/**
 * Created by Administrator on 2017/11/10.
 */
public class MOBWriteReportServiceOut {
    private String id;
    private String type;
    private long time; // 写入耗时
    @JSONField(name = "errorCode")
    private JsonResultType errorcode;
    @JSONField(name = "mobWriteResult")
    private List<MOBWriteOutputData> mobWriteResult; // 写入结果状态集

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

    public List<MOBWriteOutputData> getMobWriteResult() {
        return mobWriteResult;
    }

    public void setMobWriteResult(List<MOBWriteOutputData> mobWriteResult) {
        this.mobWriteResult = mobWriteResult;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public JsonResultType getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(JsonResultType errorcode) {
        this.errorcode = errorcode;
    }
}
