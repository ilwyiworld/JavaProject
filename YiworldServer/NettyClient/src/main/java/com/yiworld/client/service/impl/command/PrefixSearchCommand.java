package com.yiworld.client.service.impl.command;

import com.yiworld.client.service.EchoService;
import com.yiworld.client.service.InnerCommand;
import com.yiworld.client.service.RouteRequest;
import com.yiworld.client.vo.response.OnlineUsersResVO;
import com.yiworld.common.datastruct.TrieTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrefixSearchCommand implements InnerCommand {
    private final static Logger LOGGER = LoggerFactory.getLogger(PrefixSearchCommand.class);

    @Autowired
    private RouteRequest routeRequest ;
    @Autowired
    private EchoService echoService ;

    @Override
    public void process(String msg) {
        try {
            List<OnlineUsersResVO.DataBodyBean> onlineUsers = routeRequest.onlineUsers();
            TrieTree trieTree = new TrieTree();
            for (OnlineUsersResVO.DataBodyBean onlineUser : onlineUsers) {
                trieTree.insert(onlineUser.getUserName());
            }
            String[] split = msg.split(" ");
            String key = split[1];
            List<String> list = trieTree.prefixSearch(key);

            for (String res : list) {
                res = res.replace(key, "\033[31;4m" + key + "\033[0m");
                echoService.echo(res) ;
            }
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
    }
}
