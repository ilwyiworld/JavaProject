package com.yiworld.server.kit;

import com.yiworld.common.kit.HeartBeatHandler;
import com.yiworld.common.pojo.UserInfo;
import com.yiworld.common.util.NettyAttrUtil;
import com.yiworld.server.config.AppConfiguration;
import com.yiworld.server.util.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServerHeartBeatHandlerImpl implements HeartBeatHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerHeartBeatHandlerImpl.class);

    @Autowired
    private RouteHandler routeHandler ;

    @Autowired
    private AppConfiguration appConfiguration ;

    @Override
    public void process(ChannelHandlerContext ctx) throws Exception {

        long heartBeatTime = appConfiguration.getHeartBeatTime() * 1000;

        Long lastReadTime = NettyAttrUtil.getReaderTime(ctx.channel());
        long now = System.currentTimeMillis();
        if (lastReadTime != null && now - lastReadTime > heartBeatTime){
            UserInfo userInfo = SessionSocketHolder.getUserId((NioSocketChannel) ctx.channel());
            if (userInfo != null){
                LOGGER.warn("客户端[{}]心跳超时[{}]ms，需要关闭连接!",userInfo.getUserName(),now - lastReadTime);
            }
            routeHandler.userOffLine(userInfo, (NioSocketChannel) ctx.channel());
            ctx.channel().close();
        }
    }
}
