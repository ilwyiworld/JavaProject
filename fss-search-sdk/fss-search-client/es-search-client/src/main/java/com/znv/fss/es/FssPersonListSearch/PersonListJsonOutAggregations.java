package com.znv.fss.es.FssPersonListSearch;

import java.util.List;

/**
 * Created by User on 2017/12/7.
 */
public class PersonListJsonOutAggregations {
    private List<PersonListJsonOutPersonlibTypes> libTypes;
    public void setLibTypes(List<PersonListJsonOutPersonlibTypes> libTypes) {
        this.libTypes = libTypes;
    }

    public List<PersonListJsonOutPersonlibTypes> getLibTypes() {
        return libTypes;
    }
}
