package com.yiworld.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RouteInfo {
    private String ip ;
    private Integer serverPort;
    private Integer httpPort;
}
