package com.znv.fss.es.FssExactSearch;

import com.znv.fss.es.FssArbitrarySearch.FssArbitraryOutHits;

import java.util.List;

/**
 * Created by User on 2017/8/25.
 */
public class FssExactJsonOut {
    private List<FssExactOutHits> hits;
    private String id;
    private String total;
    private int errorCode;

    public void setHits(List<FssExactOutHits> hits) {
        this.hits = hits;
    }

    public List<FssExactOutHits> getHits() {
        return this.hits;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getTotal() {
        return this.total;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
