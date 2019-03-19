package com.znv.fss.es.FssPersonListSearch;

import java.util.List;

/**
 * Created by User on 2017/12/7.
 */
public class PersonListJsonOutBuckets {
    private List<PersonListJsonOutLibIds> libIds;

    public void setLibIds(List<PersonListJsonOutLibIds> bucket) {
        this.libIds = libIds;
    }

    public List<PersonListJsonOutLibIds> getLibIds() {
        return libIds;
    }
}
