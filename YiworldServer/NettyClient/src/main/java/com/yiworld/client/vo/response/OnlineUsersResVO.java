package com.yiworld.client.vo.response;

import lombok.Data;

import java.util.List;

@Data
public class OnlineUsersResVO {
    /**
     * code : 9000
     * message : 成功
     * reqNo : null
     * dataBody : [{"userId":1545574841528,"userName":"zhangsan"},{"userId":1545574871143,"userName":"crossoverJie"}]
     */
    private String code;
    private String message;
    private Object reqNo;
    private List<DataBodyBean> dataBody;

    @Data
    public static class DataBodyBean {
        /**
         * userId : 1545574841528
         * userName : zhangsan
         */
        private long userId;
        private String userName;
    }
}
