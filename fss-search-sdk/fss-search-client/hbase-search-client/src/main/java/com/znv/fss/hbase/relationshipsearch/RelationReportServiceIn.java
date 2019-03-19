package com.znv.fss.hbase.relationshipsearch;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Administrator on 2017/6/6.
 */
public class RelationReportServiceIn {
    private String id;
    private String type;
    @JSONField(name = "relationshipParam")
    private RelationshipParam relationshipparam;

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

    public void setRelationshipparam(RelationshipParam relationshipparam) {
        this.relationshipparam = relationshipparam;
    }

    public RelationshipParam getRelationshipparam() {
        return relationshipparam;
    }

}
