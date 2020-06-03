package com.yiworld.common.algorithm.consistenthash;

import com.yiworld.common.algorithm.RouteHandle;

import java.util.List;

public class ConsistentHashHandle implements RouteHandle {
    private AbstractConsistentHash hash ;

    public void setHash(AbstractConsistentHash hash) {
        this.hash = hash;
    }

    @Override
    public String routeServer(List<String> values, String key) {
        return hash.process(values, key);
    }
}
