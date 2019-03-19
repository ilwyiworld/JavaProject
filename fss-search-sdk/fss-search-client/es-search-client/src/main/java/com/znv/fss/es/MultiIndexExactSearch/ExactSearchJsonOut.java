package com.znv.fss.es.MultiIndexExactSearch;

import java.util.List;

/**
 * Created by User on 2017/8/25.
 */
public class ExactSearchJsonOut {
    private List<ExactSearchQueryHit> hits;
    private int took;
    private int total;
    private int errorCode;

    public void setHits(List<ExactSearchQueryHit> hits) {
        this.hits = hits;
    }

    public List<ExactSearchQueryHit> getHits() {
        return this.hits;
    }

    public void setTook(int took) {
        this.took = took;
    }

    public int getTook() {
        return this.took;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotal() {
        return this.total;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
