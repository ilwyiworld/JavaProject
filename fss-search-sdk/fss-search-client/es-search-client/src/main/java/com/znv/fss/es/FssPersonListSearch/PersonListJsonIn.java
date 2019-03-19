package com.znv.fss.es.FssPersonListSearch;


/**
 * Created by Administrator on 2017/12/5.
 */
public class PersonListJsonIn {
    private String id;
    private PersonListQueryParam params;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setParams(PersonListQueryParam params) {
        this.params = params;
    }

    public PersonListQueryParam getParams() {
        return params;
    }
}
