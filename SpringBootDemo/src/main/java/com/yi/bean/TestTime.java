package com.yi.bean;

import java.io.Serializable;

public class TestTime{
    private Long time;

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "TestTime{" +
                "time=" + time +
                '}';
    }
}
