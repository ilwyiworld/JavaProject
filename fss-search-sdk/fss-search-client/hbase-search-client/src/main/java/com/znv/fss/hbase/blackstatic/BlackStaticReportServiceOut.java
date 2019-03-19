package com.znv.fss.hbase.blackstatic;

import com.znv.fss.hbase.JsonResultType;

/**
 * Created by estine on 2017/2/20.
 */
public class BlackStaticReportServiceOut {
    private String id; // hbase表ID
    private String type;
    private String usedTime; // 检索耗时，单位：毫秒
    private BlackStaticOutputDatas blackStaticSearchLists;
    private JsonResultType errorCode;
    private String pictureExist; // 存在符合相似度要求的图片？
    private int count = 0; // 总记录数
    private int totalPage = 0; // 总页数

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(String usedTime) {
        this.usedTime = usedTime;
    }

    public BlackStaticOutputDatas getBlackStaticSearchLists() {
        return blackStaticSearchLists;
    }

    public void setBlackStaticSearchLists(BlackStaticOutputDatas blackStaticSearchLists) {
        this.blackStaticSearchLists = blackStaticSearchLists;
    }

    public JsonResultType getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(JsonResultType errorCode) {
        this.errorCode = errorCode;
    }

    public String getPictureExist() {
        return pictureExist;
    }

    public void setPictureExist(String pictureExist) {
        this.pictureExist = pictureExist;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }
}
