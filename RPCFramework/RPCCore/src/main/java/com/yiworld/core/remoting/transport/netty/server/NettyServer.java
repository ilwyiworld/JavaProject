package com.yiworld.core.remoting.transport.netty.server;

import com.yiworld.core.config.CustomShutdownHook;
import com.yiworld.core.provider.ServiceProvider;
import com.yiworld.core.provider.ServiceProviderImpl;
import com.yiworld.core.registry.ServiceRegistry;
import com.yiworld.core.registry.ZkServiceRegistry;
import com.yiworld.core.remoting.dto.RpcRequest;
import com.yiworld.core.remoting.dto.RpcResponse;
import com.yiworld.core.remoting.transport.netty.codec.kyro.NettyKryoDecoder;
import com.yiworld.core.remoting.transport.netty.codec.kyro.NettyKryoEncoder;
import com.yiworld.core.serialize.kyro.KryoSerializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 服务端。接收客户端消息，并且根据客户端的消息调用相应的方法，然后返回结果给客户端。
 */
@Slf4j
public class NettyServer {
    private String host;
    private int port;
    private KryoSerializer kryoSerializer;
    private ServiceRegistry serviceRegistry;
    private ServiceProvider serviceProvider;

    public NettyServer(String host, int port) {
        this.host = host;
        this.port = port;
        kryoSerializer = new KryoSerializer();
        serviceRegistry = new ZkServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
    }

    public <T> void publishService(T service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service, serviceClass);
        serviceRegistry.registerService(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }

    private void start() {
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RpcRequest.class));
                            ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RpcResponse.class));
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    })
                    // TCP 默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 表示系统用于临时存放已完成三次握手的请求的队列的最大长度，如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128);
            // 绑定端口，同步等待绑定成功
            ChannelFuture f = b.bind(host, port).sync();
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server:", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}