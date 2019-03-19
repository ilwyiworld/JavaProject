package com.znv.fss.hbase.relationshipsearch;

import com.alibaba.fastjson.annotation.JSONField;
import com.znv.fss.hbase.JsonResultType;

import java.util.List;

/**
 * Created by Administrator on 2017/6/7.
 */
public class RelationReportServiceOut {
    private String id;
    private String type;
    @JSONField(name = "errorCode")
    private JsonResultType errorcode;
    private long time;
    private int count;
    @JSONField(name = "relationshipData")
    private List<RelationshipData> relationshipdata;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setErrorcode(JsonResultType errorcode) {
        this.errorcode = errorcode;
    }

    public JsonResultType getErrorcode() {
        return errorcode;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setRelationshipdata(List<RelationshipData> relationshipdata) {
        this.relationshipdata = relationshipdata;
    }

    public List<RelationshipData> getRelationshipdata() {
        return relationshipdata;
    }
}
