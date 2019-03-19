package com.znv.fss.es.FastFeatureSearch;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by Administrator on 2017/12/5.
 */
public class FastFeatureJsonOut {
    private List<FastFeatureQueryHit> hits;
    private int total;
    @JSONField(name = "errorCode")
    private int errorcode;
    private int took;

    public void setHits(List<FastFeatureQueryHit> hits) {
        this.hits = hits;
    }

    public List<FastFeatureQueryHit> getHits() {
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
