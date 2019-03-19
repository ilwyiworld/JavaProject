package com.znv.fss.es.AlarmTypeSearch;


public class AlarmTypeJsonIn {
    private String id;
    private AlarmTypeQueryParam params;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setParams(AlarmTypeQueryParam params) {
        this.params = params;
    }

    public AlarmTypeQueryParam getParams() {
        return params;
    }
}
