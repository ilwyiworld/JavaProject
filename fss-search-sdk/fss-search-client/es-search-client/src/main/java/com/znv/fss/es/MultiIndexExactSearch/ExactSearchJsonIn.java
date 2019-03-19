package com.znv.fss.es.MultiIndexExactSearch;

import com.znv.fss.es.FssArbitrarySearch.FssArbitraryQueryParam;

/**
 * Created by User on 2017/8/25.
 */
public class ExactSearchJsonIn {
    private String id;
    private ExactSearchQueryParam params;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setParams(ExactSearchQueryParam params) {
        this.params = params;
    }

    public ExactSearchQueryParam getParams() {
        return this.params;
    }
}
