package com.yiworld.route;

import com.yiworld.route.kit.NetAddressIsReachable;
import org.junit.Test;

public class CommonTest {
    @Test
    public void test() {
        boolean reachable = NetAddressIsReachable.checkAddressReachable("127.0.0.1", 11211, 1000);
        System.out.println(reachable);
    }
}
