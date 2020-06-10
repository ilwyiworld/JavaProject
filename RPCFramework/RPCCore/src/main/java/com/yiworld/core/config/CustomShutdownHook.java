package com.yiworld.core.config;

import com.yiworld.common.utils.threadpool.ThreadPoolFactoryUtils;
import com.yiworld.common.utils.zk.CuratorUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook() {
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CuratorUtils.clearRegistry();
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}
