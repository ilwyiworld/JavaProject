package com.yiworld.common.algorithm.loop;

import com.yiworld.common.algorithm.RouteHandle;
import com.yiworld.common.enums.StatusEnum;
import com.yiworld.common.exception.YiworldException;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class LoopHandle implements RouteHandle {
    private AtomicLong index = new AtomicLong();

    @Override
    public String routeServer(List<String> values, String key) {
        if (values.size() == 0) {
            throw new YiworldException(StatusEnum.SERVER_NOT_AVAILABLE) ;
        }
        Long position = index.incrementAndGet() % values.size();
        if (position < 0) {
            position = 0L;
        }

        return values.get(position.intValue());
    }
}
