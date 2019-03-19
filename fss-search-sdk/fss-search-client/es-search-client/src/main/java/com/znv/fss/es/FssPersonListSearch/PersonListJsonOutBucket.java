package com.znv.fss.es.FssPersonListSearch;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by User on 2017/12/7.
 */
public class PersonListJsonOutBucket {
    @JSONField(name = "doc_count")
    private int docCount;
    @JSONField(name = "key")
    private int key;
    public void setDocCount(int docCount) {
        this.docCount = docCount;
    }

    public double getDocCount() {
        return docCount;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public double getKey() {
        return key;
    }
}
