package com.yiworld.server.kit;

import com.yiworld.server.config.AppConfiguration;
import com.yiworld.server.util.SpringBeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryZK implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(RegistryZK.class);

    private ZKit zKit;

    private AppConfiguration appConfiguration;

    private String ip;
    private int serverPort;
    private int httpPort;

    public RegistryZK(String ip, int serverPort, int httpPort) {
        this.ip = ip;
        this.serverPort = serverPort;
        this.httpPort = httpPort;
        zKit = SpringBeanFactory.getBean(ZKit.class);
        appConfiguration = SpringBeanFactory.getBean(AppConfiguration.class);
    }

    @Override
    public void run() {
        // 创建父节点
        zKit.createRootNode();
        // 是否要将自己注册到 ZK
        if (appConfiguration.isZkSwitch()) {
            String path = appConfiguration.getZkRoot() + "/ip-" + ip + ":" + serverPort + ":" + httpPort;
            zKit.createNode(path);
            logger.info("Registry zookeeper success, msg=[{}]", path);
        }
    }
}