package com.znv.fss.es.FssSearchByTrail;

import com.alibaba.fastjson.annotation.JSONField;
import java.util.List;

public class SearchByTrailJsonOut {
    private List<SearchByTrailQueryHit> hits;
    private int total;
    @JSONField(name = "errorCode")
    private int errorcode;
    private int took;

    public void setHits(List<SearchByTrailQueryHit> hits) {
        this.hits = hits;
    }

    public List<SearchByTrailQueryHit> getHits() {
        return hits;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    public void setErrorcode(int errorcode) {
        this.errorcode = errorcode;
    }

    public int getErrorcode() {
        return errorcode;
    }

    public void setTook(int took) {
        this.took = took;
    }

    public int getTook() {
        return took;
    }
}
