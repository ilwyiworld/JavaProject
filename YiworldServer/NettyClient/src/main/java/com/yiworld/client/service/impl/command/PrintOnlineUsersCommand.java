package com.yiworld.client.service.impl.command;

import com.yiworld.client.service.EchoService;
import com.yiworld.client.service.InnerCommand;
import com.yiworld.client.service.RouteRequest;
import com.yiworld.client.vo.response.OnlineUsersResVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PrintOnlineUsersCommand implements InnerCommand {
    private final static Logger LOGGER = LoggerFactory.getLogger(PrintOnlineUsersCommand.class);

    @Autowired
    private RouteRequest routeRequest ;

    @Autowired
    private EchoService echoService ;

    @Override
    public void process(String msg) {
        try {
            List<OnlineUsersResVO.DataBodyBean> onlineUsers = routeRequest.onlineUsers();
            echoService.echo("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            for (OnlineUsersResVO.DataBodyBean onlineUser : onlineUsers) {
                echoService.echo("userId={}=====userName={}",onlineUser.getUserId(),onlineUser.getUserName());
            }
            echoService.echo("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
    }
}
