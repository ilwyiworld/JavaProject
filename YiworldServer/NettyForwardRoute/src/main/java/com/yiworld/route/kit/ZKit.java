package com.yiworld.route.kit;

import com.alibaba.fastjson.JSON;
import com.yiworld.route.cache.ServerCache;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Function: Zookeeper kit
 */
@Component
@Slf4j
public class ZKit {

    @Autowired
    private ZkClient zkClient;

    @Autowired
    private ServerCache serverCache;

    /**
     * 监听事件
     *
     * @param path
     */
    public void subscribeEvent(String path) {
        zkClient.subscribeChildChanges(path, new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
                log.info("Clear and update local cache parentPath=[{}],currentChildren=[{}]", parentPath, currentChildren.toString());
                //update local cache, delete and save.
                serverCache.updateCache(currentChildren);
            }
        });
    }

    /**
     * get all server node from zookeeper
     *
     * @return
     */
    public List<String> getAllNode() {
        List<String> children = zkClient.getChildren("/route");
        log.info("Query all node =[{}] success.", JSON.toJSONString(children));
        return children;
    }
}
