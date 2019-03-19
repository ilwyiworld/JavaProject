package com.znv.fss.es.FssArbitrarySearch;

/**
 * Created by User on 2017/8/25.
 */
public class FssArbitraryJsonIn {
    private String id;
    private String type;

    private FssArbitraryQueryParam params;

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

    public void setParams(FssArbitraryQueryParam params) {
        this.params = params;
    }

    public FssArbitraryQueryParam getParams() {
        return this.params;
    }
}
