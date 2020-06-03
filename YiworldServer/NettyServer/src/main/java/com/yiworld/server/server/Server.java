package com.yiworld.server.server;

import com.yiworld.constant.Constants;
import com.yiworld.common.protocol.RequestProto;
import com.yiworld.server.api.vo.request.SendMsgReqVO;
import com.yiworld.server.init.ServerInitializer;
import com.yiworld.server.util.SessionSocketHolder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

@Component
public class Server {

    private final static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private EventLoopGroup boss = new NioEventLoopGroup();
    private EventLoopGroup work = new NioEventLoopGroup();

    @Value("${yiworld.server.port}")
    private int nettyPort;

    /**
     * 启动 server
     *
     * @return
     * @throws InterruptedException
     */
    @PostConstruct
    public void start() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(boss, work)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(nettyPort))
                //保持长连接
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ServerInitializer());
        ChannelFuture future = bootstrap.bind().sync();
        if (future.isSuccess()) {
            LOGGER.info("Start server success!!!");
        }
    }


    /**
     * 销毁
     */
    @PreDestroy
    public void destroy() {
        boss.shutdownGracefully().syncUninterruptibly();
        work.shutdownGracefully().syncUninterruptibly();
        LOGGER.info("Close server success!!!");
    }

    /**
     * Push msg to client.
     * @param sendMsgReqVO 消息
     */
    public void sendMsg(SendMsgReqVO sendMsgReqVO){
        NioSocketChannel socketChannel = SessionSocketHolder.get(sendMsgReqVO.getUserId());

        if (null == socketChannel) {
            LOGGER.error("client {} offline!", sendMsgReqVO.getUserId());
        }
        RequestProto.ReqProtocol protocol = RequestProto.ReqProtocol.newBuilder()
                .setRequestId(sendMsgReqVO.getUserId())
                .setReqMsg(sendMsgReqVO.getMsg())
                .setType(Constants.CommandType.MSG)
                .build();

        ChannelFuture future = socketChannel.writeAndFlush(protocol);
        future.addListener((ChannelFutureListener) channelFuture ->
                LOGGER.info("server push msg:[{}]", sendMsgReqVO.toString()));
    }
}
