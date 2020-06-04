package com.yiworld.client.thread;

import com.yiworld.client.service.impl.ClientHeartBeatHandlerImpl;
import com.yiworld.client.util.SpringBeanFactory;
import com.yiworld.common.kit.HeartBeatHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReConnectJob implements Runnable {

    private ChannelHandlerContext context ;

    private HeartBeatHandler heartBeatHandler ;

    public ReConnectJob(ChannelHandlerContext context) {
        this.context = context;
        this.heartBeatHandler = SpringBeanFactory.getBean(ClientHeartBeatHandlerImpl.class) ;
    }

    @Override
    public void run() {
        try {
            heartBeatHandler.process(context);
        } catch (Exception e) {
            log.error("Exception",e);
        }
    }
}
