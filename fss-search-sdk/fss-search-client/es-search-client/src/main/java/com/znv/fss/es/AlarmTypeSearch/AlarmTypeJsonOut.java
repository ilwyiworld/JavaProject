package com.znv.fss.es.AlarmTypeSearch;


import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class AlarmTypeJsonOut {
    private List<AlarmTypeQueryHit> hits;
    private int total;
    @JSONField(name = "errorCode")
    private int errorcode;
    private int took;

    public List<AlarmTypeQueryHit> getHits() {
        return hits;
    }

    public void setHits(List<AlarmTypeQueryHit> hits) {
        this.hits = hits;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(int errorcode) {
        this.errorcode = errorcode;
    }

    public int getTook() {
        return took;
    }

    public void setTook(int took) {
        this.took = took;
    }
}
