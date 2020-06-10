package com.yiworld.core.registry;

import com.yiworld.common.utils.zk.CuratorUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 基于 zookeeper 实现服务发现
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        // TODO 负载均衡
        // 这里直接去了第一个找到的服务地址，eg: 127.0.0.1:9999
        String serviceAddress = CuratorUtils.getChildrenNodes(serviceName).get(0);
        log.info("成功找到服务地址:[{}]", serviceAddress);
        String[] socketAddressArray = serviceAddress.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}