package com.yiworld.client.vo.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class ServerResVO implements Serializable {

    /**
     * code : 9000
     * message : 成功
     * reqNo : null
     * dataBody : {"ip":"127.0.0.1","port":8081}
     */
    private String code;
    private String message;
    private Object reqNo;
    private ServerInfo dataBody;

    @Data
    public static class ServerInfo {
        /**
         * ip : 127.0.0.1
         * port : 8081
         */
        private String ip ;
        private Integer serverPort;
        private Integer httpPort;

        @Override
        public String toString() {
            return "ServerInfo{" +
                    "ip='" + ip + '\'' +
                    ", cimServerPort=" + serverPort +
                    ", httpPort=" + httpPort +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ServerResVO{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", reqNo=" + reqNo +
                ", dataBody=" + dataBody +
                '}';
    }
}
