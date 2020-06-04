package com.yiworld.route.api.vo.response;

import com.yiworld.common.pojo.RouteInfo;
import lombok.Data;

import java.io.Serializable;

@Data
public class ServerResVO implements Serializable {
    private String ip ;
    private Integer serverPort;
    private Integer httpPort;

    public ServerResVO(RouteInfo routeInfo) {
        this.ip = routeInfo.getIp();
        this.serverPort = routeInfo.getServerPort();
        this.httpPort = routeInfo.getHttpPort();
    }
}
