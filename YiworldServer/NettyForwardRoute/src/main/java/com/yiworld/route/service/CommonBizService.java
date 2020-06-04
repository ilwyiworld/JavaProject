package com.yiworld.route.service;

import com.yiworld.common.enums.StatusEnum;
import com.yiworld.common.exception.YiworldException;
import com.yiworld.common.pojo.RouteInfo;
import com.yiworld.route.cache.ServerCache;
import com.yiworld.route.kit.NetAddressIsReachable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommonBizService {
    @Autowired
    private ServerCache serverCache;

    /**
     * check ip and port
     *
     * @param routeInfo
     */
    public void checkServerAvailable(RouteInfo routeInfo) {
        boolean reachable = NetAddressIsReachable.checkAddressReachable(routeInfo.getIp(), routeInfo.getServerPort(), 1000);
        if (!reachable) {
            log.error("ip={}, port={} are not available", routeInfo.getIp(), routeInfo.getServerPort());
            // rebuild cache
            serverCache.rebuildCacheList();
            throw new YiworldException(StatusEnum.SERVER_NOT_AVAILABLE);
        }

    }
}
