package com.znv.fss.es.MultiIndexFastSearch;

import com.alibaba.fastjson.annotation.JSONField;
import com.znv.fss.es.FastFeatureSearch.FastFeatureQueryHit;

import java.util.List;

/**
 * Created by Administrator on 2017/12/5.
 */
public class FastSearchJsonOut {
    private List<FastSearchQueryHit> hits;
    private int total;
    @JSONField(name = "errorCode")
    private int errorcode;
    private int took;

    public void setHits(List<FastSearchQueryHit> hits) {
        this.hits = hits;
    }

    public List<FastSearchQueryHit> getHits() {
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
