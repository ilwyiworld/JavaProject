package com.znv.fss.es.FssPersonListSearch;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by User on 2017/12/7.
 */
public class PersonListJsonOutLibIds {

    private List<PersonListJsonOutBucket> bucket;
    @JSONField(name = "doc_count_error_upper_bound")
    private int docCountErrorUpperBound;
    @JSONField(name = "sum_other_doc_count")
    private int sumOtherDocCount;
    @JSONField(name = "doc_count")
    private int docCount;
    @JSONField(name = "key")
    private int key;

    public void setBucket(List<PersonListJsonOutBucket> bucket) {
        this.bucket = bucket;
    }

    public List<PersonListJsonOutBucket> getBucket() {
        return bucket;
    }

    public void setDocCountErrorUpperBound(int docCountErrorUpperBound) {
        this.docCountErrorUpperBound = docCountErrorUpperBound;
    }

    public double getDocCountErrorUpperBound() {
        return docCountErrorUpperBound;
    }

    public void setSumOtherDocCount(int sumOtherDocCount) {
        this.sumOtherDocCount = sumOtherDocCount;
    }

    public double getSumOtherDocCount() {
        return sumOtherDocCount;
    }

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
