package com.yiworld.common;

import lombok.Data;
import java.io.Serializable;

@Data
public class People implements Serializable {
    private static final long serialVersionUID = 1415852192397895853L;
    // 人员编号
    private int id;
    // 姓名
    private String name;
    @Override
    public String toString() {
        return "People [id=" + id + ", name=" + name + "]";
    }
}
