package com.znv.fss.hbase.mob;

/**
 * Created by Administrator on 2017/11/10.
 */
public class MOBWriteOutputData {
    private String uuid = ""; // 写入数据的key
    private int errorCode; //写入结果

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
