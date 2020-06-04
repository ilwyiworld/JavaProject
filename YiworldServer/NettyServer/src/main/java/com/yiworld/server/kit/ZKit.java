package com.yiworld.server.kit;

import com.yiworld.server.config.AppConfiguration;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Function: Zookeeper 工具
 */
@Component
public class ZKit {

    @Autowired
    private ZkClient zkClient;

    @Autowired
    private AppConfiguration appConfiguration ;

    /**
     * 创建父级节点
     */
    public void createRootNode(){
        boolean exists = zkClient.exists(appConfiguration.getZkRoot());
        if (exists){
            return;
        }
        //创建 root
        zkClient.createPersistent(appConfiguration.getZkRoot()) ;
    }

    /**
     * 写入指定节点 临时目录
     *
     * @param path
     */
    public void createNode(String path) {
        zkClient.createEphemeral(path);
    }

}
