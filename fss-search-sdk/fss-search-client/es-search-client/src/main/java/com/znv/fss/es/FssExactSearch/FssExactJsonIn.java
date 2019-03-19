package com.znv.fss.es.FssExactSearch;

/**
 * Created by User on 2017/8/25.
 */
public class FssExactJsonIn {
    private String id;
    private String type;
    private FssExactQueryParam params;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setParams(FssExactQueryParam params) {
        this.params = params;
    }

    public FssExactQueryParam getParams() {
        return this.params;
    }
}
