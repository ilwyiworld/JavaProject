package com.yiworld.common.algorithm.random;

import com.yiworld.common.algorithm.RouteHandle;
import com.yiworld.common.enums.StatusEnum;
import com.yiworld.common.exception.YiworldException;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Function: 路由策略， 随机
 */
public class RandomHandle implements RouteHandle {

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if (size == 0) {
            throw new YiworldException(StatusEnum.SERVER_NOT_AVAILABLE) ;
        }
        int offset = ThreadLocalRandom.current().nextInt(size);
        return values.get(offset);
    }
}
