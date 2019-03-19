package com.znv.fss.hbase.mob;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Administrator on 2017/11/10.
 */
public class MOBInputData {
    @JSONField(name = "uuid")
    private String uuid;
    @JSONField(name = "imageData")
    private byte[] imageData;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
