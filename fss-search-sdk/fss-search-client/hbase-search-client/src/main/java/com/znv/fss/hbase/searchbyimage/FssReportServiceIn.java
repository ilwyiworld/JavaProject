package com.znv.fss.hbase.searchbyimage;

/**
 * Created by Administrator on 2016/12/22.
 */
public class FssReportServiceIn {
    private String id;
    private String type;
    private FssInPutParams fssSearch;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FssInPutParams getFssSearch() {
        return fssSearch;
    }

    public void setFssSearch(FssInPutParams fssSearch) {
        this.fssSearch = fssSearch;
    }
}
