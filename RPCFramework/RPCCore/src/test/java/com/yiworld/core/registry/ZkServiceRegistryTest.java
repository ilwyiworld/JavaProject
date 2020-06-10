package com.yiworld.core.registry;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;

public class ZkServiceRegistryTest {
    @Test
    void should_register_service_successful_and_lookup_service_by_service_name() {
        ServiceRegistry zkServiceRegistry = new ZkServiceRegistry();
        InetSocketAddress givenInetSocketAddress = new InetSocketAddress("127.0.0.1", 9333);
        zkServiceRegistry.registerService("com.yiworld.registry.ZkServiceRegistry", givenInetSocketAddress);
        ServiceDiscovery zkServiceDiscovery = new ZkServiceDiscovery();
        InetSocketAddress acquiredInetSocketAddress = zkServiceDiscovery.lookupService("com.yiworld.registry.ZkServiceRegistry");
        assertEquals(givenInetSocketAddress.toString(), acquiredInetSocketAddress.toString());
    }
}
