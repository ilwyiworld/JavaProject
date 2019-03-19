package com.znv.fss.hbase.searchbyimage;

/**
 * Created by Administrator on 2016/12/22.
 */
public class FssReportServiceOut {
    private String id; // hbase表ID
    private String type;
    private String usedTime; // 检索耗时，单位：毫秒
    private FssOutputDatas fssSearchLists; // 检索返回结果集
    private int errorCode;
    private String pictureExist; // 存在符合相似度要求的图片？
    private int count = 0; // 总记录数
    private int totalPage = 0; // 总页数
    //private int morePage;
    //private int firstSearchPage;

//    public int getMorePage() {
//        return morePage;
//    }
//
//    public void setMorePage(int morePage) {
//        this.morePage = morePage;
//    }
//
//    public int getFirstSearchPage() {
//        return firstSearchPage;
//    }
//
//    public void setFirstSearchPage(int firstSearchPage) {
//        this.firstSearchPage = firstSearchPage;
//    }

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

    public FssOutputDatas getFssSearchLists() {
        return fssSearchLists;
    }

    public void setFssSearchLists(FssOutputDatas fssSearchLists) {
        this.fssSearchLists = fssSearchLists;
    }

//    public JsonResultType getErrorCode() {
//        return errorCode;
//    }
//
//    public void setErrorCode(JsonResultType errorCode) {
//        this.errorCode = errorCode;
//    }


    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
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
