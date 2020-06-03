package com.yiworld.common.kit;

import io.netty.channel.ChannelHandlerContext;

public interface HeartBeatHandler {

    /**
     * 处理心跳
     * @param ctx
     * @throws Exception
     */
    void process(ChannelHandlerContext ctx) throws Exception ;
}