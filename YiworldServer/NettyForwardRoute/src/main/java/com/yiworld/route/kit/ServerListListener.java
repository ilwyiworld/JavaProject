package com.yiworld.route.kit;

import com.yiworld.route.config.AppConfiguration;
import com.yiworld.route.util.SpringBeanFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerListListener implements Runnable {

    private ZKit zkUtil;

    private AppConfiguration appConfiguration;


    public ServerListListener() {
        zkUtil = SpringBeanFactory.getBean(ZKit.class);
        appConfiguration = SpringBeanFactory.getBean(AppConfiguration.class);
    }

    @Override
    public void run() {
        // 注册监听服务
        zkUtil.subscribeEvent(appConfiguration.getZkRoot());
    }
}
