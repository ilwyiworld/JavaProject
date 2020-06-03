package com.yiworld.common.util;

import com.yiworld.common.exception.YiworldException;
import com.yiworld.common.pojo.RouteInfo;
import com.yiworld.common.enums.StatusEnum;

public class RouteInfoParseUtil {
    public static RouteInfo parse(String info){
        try {
            String[] serverInfo = info.split(":");
            RouteInfo routeInfo =  new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1]),Integer.parseInt(serverInfo[2])) ;
            return routeInfo ;
        }catch (Exception e){
            throw new YiworldException(StatusEnum.VALIDATION_FAIL) ;
        }
    }
}
