package com.yiworld.myshop.statics.backend.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

//数据传输对象
@Data
public class DataTableDTO<T> implements Serializable {
    private int draw;
    private long recordsTotal;
    private long recordsFiltered;
    private List<T> data;
    private String error;

}
