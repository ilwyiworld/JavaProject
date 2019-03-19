package com.znv.fss.es.MultiIndexFastSearch;

import com.znv.fss.es.FastFeatureSearch.FastFeatureQueryParam;

/**
 * Created by Administrator on 2017/12/5.
 */
public class FastSearchJsonIn {
    private String id;
    private FastSearchQueryParam params;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setParams(FastSearchQueryParam params) {
        this.params = params;
    }

    public FastSearchQueryParam getParams() {
        return params;
    }
}
