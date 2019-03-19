package com.znv.fss.hbase.relationshipsearch;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by Administrator on 2017/6/6.
 */
public class RelationshipParam {
    @JSONField(name = "startTime")
    private String starttime;
    @JSONField(name = "endTime")
    private String endtime;
    @JSONField(name = "topN")
    private int topn;
    @JSONField(name = "peerInterval")
    private int peerinterval;
    @JSONField(name = "sortType")
    private String sorttype; // 1-次数，2-时长
    @JSONField(name = "searchFeature")
    private List<String> searchfeature;
    @JSONField(name = "officeIds")
    private List<String> officeids;
    @JSONField(name = "cameraIds")
    private List<String> cameraids;
    @JSONField(name = "picFilterType")
    private String picfiltertype;

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getStarttime() {
        return starttime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setTopn(int topn) {
        this.topn = topn;
    }

    public int getTopn() {
        return topn;
    }

    public void setPeerinterval(int peerinterval) {
        this.peerinterval = peerinterval;
    }

    public int getPeerinterval() {
        return peerinterval;
    }

    public String getSorttype() {
        return sorttype;
    }

    public void setSorttype(String sorttype) {
        this.sorttype = sorttype;
    }

    public List<String> getSearchfeature() {
        return searchfeature;
    }

    public void setSearchfeature(List<String> searchfeature) {
        this.searchfeature = searchfeature;
    }

    public List<String> getOfficeids() {
        return officeids;
    }

    public void setOfficeids(List<String> officeids) {
        this.officeids = officeids;
    }

    public String getPicfiltertype() {
        return picfiltertype;
    }

    public void setPicfiltertype(String picfiltertype) {
        this.picfiltertype = picfiltertype;
    }

    public List<String> getCameraids() {
        return cameraids;
    }

    public void setCameraids(List<String> cameraids) {
        this.cameraids = cameraids;
    }
}
