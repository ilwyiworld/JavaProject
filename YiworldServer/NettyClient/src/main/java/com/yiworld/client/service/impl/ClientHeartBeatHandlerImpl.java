package com.yiworld.client.service.impl;

import com.yiworld.client.Client;
import com.yiworld.client.thread.ContextHolder;
import com.yiworld.common.kit.HeartBeatHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientHeartBeatHandlerImpl implements HeartBeatHandler {
    @Autowired
    private Client client;

    @Override
    public void process(ChannelHandlerContext ctx) throws Exception {
        // 重连
        ContextHolder.setReconnect(true);
        client.reconnect();
    }
}
