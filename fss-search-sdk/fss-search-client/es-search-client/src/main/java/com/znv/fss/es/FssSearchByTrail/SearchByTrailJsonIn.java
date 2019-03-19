package com.znv.fss.es.FssSearchByTrail;

public class SearchByTrailJsonIn {
    private String id;
    private SearchByTrailQueryParam params;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setParams(SearchByTrailQueryParam params) {
        this.params = params;
    }

    public SearchByTrailQueryParam getParams() {
        return params;
    }
}
