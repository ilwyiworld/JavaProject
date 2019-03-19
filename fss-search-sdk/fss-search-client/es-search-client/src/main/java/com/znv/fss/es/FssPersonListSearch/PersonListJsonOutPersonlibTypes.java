package com.znv.fss.es.FssPersonListSearch;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by User on 2017/12/7.
 */
public class PersonListJsonOutPersonlibTypes {
    private List<PersonListJsonOutBuckets> buckets;
    @JSONField(name = "doc_count_error_upper_bound")
    private int docCountErrorUpperBound;
    @JSONField(name = "sum_other_doc_count")
    private int sumOtherDocCount;

    public void setBuckets(List<PersonListJsonOutBuckets> buckets) {
        this.buckets = buckets;
    }

    public List<PersonListJsonOutBuckets> getBuckets() {
        return buckets;
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

}
