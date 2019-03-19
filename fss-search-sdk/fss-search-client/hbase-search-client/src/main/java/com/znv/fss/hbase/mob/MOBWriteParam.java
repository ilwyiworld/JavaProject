package com.znv.fss.hbase.mob;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by Administrator on 2017/11/10.
 */
public class MOBWriteParam {
    @JSONField(name = "data")
    private List<MOBInputData> data;

    public List<MOBInputData> getData() {
        return data;
    }

    public void setData(List<MOBInputData> data) {
        this.data = data;
    }
}
