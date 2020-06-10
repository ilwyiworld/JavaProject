package com.yiworld.core.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 */
public interface ServiceRegistry {
    /**
     * 注册服务
     *
     * @param serviceName       服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    void registerService(String serviceName, InetSocketAddress inetSocketAddress);

}