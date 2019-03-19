package com.znv.fss.hbase.mob;

import com.alibaba.fastjson.annotation.JSONField;
import com.znv.fss.hbase.mob.MOBWriteParam;

/**
 * Created by Administrator on 2017/11/10.
 */
public class MOBWriteReportServiceIn {
    private String id;
    private String type;
    @JSONField(name = "mobWriteParam")
    private MOBWriteParam mobWriteParam;

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

    public MOBWriteParam getMobWriteParam() {
        return mobWriteParam;
    }

    public void setMobWriteParam(MOBWriteParam mobWriteParam) {
        this.mobWriteParam = mobWriteParam;
    }
}
