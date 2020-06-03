package com.yiworld.route.service;

import com.yiworld.common.enums.StatusEnum;
import com.yiworld.common.exception.YiworldException;
import com.yiworld.common.pojo.RouteInfo;
import com.yiworld.route.cache.ServerCache;
import com.yiworld.route.kit.NetAddressIsReachable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonBizService {
    private static Logger logger = LoggerFactory.getLogger(CommonBizService.class) ;


    @Autowired
    private ServerCache serverCache ;

    /**
     * check ip and port
     * @param routeInfo
     */
    public void checkServerAvailable(RouteInfo routeInfo){
        boolean reachable = NetAddressIsReachable.checkAddressReachable(routeInfo.getIp(), routeInfo.getCimServerPort(), 1000);
        if (!reachable) {
            logger.error("ip={}, port={} are not available", routeInfo.getIp(), routeInfo.getCimServerPort());
            // rebuild cache
            serverCache.rebuildCacheList();
            throw new YiworldException(StatusEnum.SERVER_NOT_AVAILABLE) ;
        }

    }
}
