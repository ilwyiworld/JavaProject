package com.znv.fss.es.FastFeatureSearch;

import com.znv.fss.es.FastFeatureSearch.FastFeatureQueryParam;

/**
 * Created by Administrator on 2017/12/5.
 */
public class FastFeatureJsonIn {
    private String id;
    private FastFeatureQueryParam params;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setParams(FastFeatureQueryParam params) {
        this.params = params;
    }

    public FastFeatureQueryParam getParams() {
        return params;
    }
}
