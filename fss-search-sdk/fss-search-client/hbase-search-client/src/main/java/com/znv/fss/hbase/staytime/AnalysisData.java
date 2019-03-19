package com.znv.fss.hbase.staytime;

/**
 * Created by ZNV on 2017/10/24.
 */
public class AnalysisData {
    private String uuid;
    private String enter_time;
    private String duration_time;
    private long duration_timel;
    private String img_url;

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setEnter_time(String enter_time) {
        this.enter_time = enter_time;
    }

    public String getEnter_time() {
        return this.enter_time;
    }

    public void setDuration_time(String duration_time) {
        this.duration_time = duration_time;
    }

    public String getDuration_time() {
        return this.duration_time;
    }

    public void setDuration_timel(long duration_timel) {
        this.duration_timel = duration_timel;
    }

    public long getDuration_timel() {
        return this.duration_timel;
    }
}
