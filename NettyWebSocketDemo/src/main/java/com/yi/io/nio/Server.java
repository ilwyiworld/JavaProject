package com.yi.io.nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class Server {

    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        // 用来处理 I/O 操作的多线程事件循环器 一旦 "boss" 接收到连接，就会把连接信息注册到 "worker" 上
        EventLoopGroup bossGroup = new NioEventLoopGroup();     // 用来接收进来的连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();   // 用来处理已经被接收的连接
        try {
            // ServerBootstrap 是一个启动 NIO 服务的辅助启动类，可以在这个服务中直接使用 Channel
            ServerBootstrap b = new ServerBootstrap();
            // 这一步是必须的，如果没有设置 group 将会报 java.lang.IllegalStateException: group not set 异常
            b = b.group(bossGroup, workerGroup);
            // ServerSocketChannel 以 NIO 的 selector 为基础进行实现的，用来接收新的连接
            b = b.channel(NioServerSocketChannel.class);
            /***
             * 配置具体的数据处理方式。
             * 这里的事件处理类经常会被用来处理一个最近的已经接收的 Channel。
             * ChannelInitializer 是一个特殊的处理类，他的目的是帮助使用者配置一个新的 Channel。
             * 也许你想通过增加一些处理类比如 NettyServerHandler 来配置一个新的 Channel，
             * 或者其对应的 ChannelPipeline 来实现你的网络程序。当你的程序变的复杂时，可能你会增加更多的处理类到 pipline 上，
             * 然后提取这些匿名类到最顶层的类上。
             */
            b = b.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    /***
                        Netty 中解决 TCP 粘包/拆包的方法：
                        ① 分隔符类：DelimiterBasedFrameDecoder（自定义分隔符）
                        ② 定长：FixedLengthFrameDecoder
                    */
                    // 设置特殊分隔符
                    ByteBuf buf = Unpooled.copiedBuffer("$_".getBytes());
                    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, buf));
                    // 设置字符串形式的编码
                    ch.pipeline().addLast(new StringDecoder());
                    // 最后添加自己的处理器类
                    ch.pipeline().addLast(new ServerHandler());
                }
            });
            /***
             * 可以设置这里指定的通道实现的配置参数。 这里正在写一个 TCP/IP 的服务端，
             * 因此被允许设置 socket 的参数选项，比如 tcpNoDelay 和 keepAlive。
             */
            /**
             * 对于 ChannelOption.SO_BACKLOG 的解释：
             * 服务器端 TCP 内核维护有两个队列，我们称之为 A、B 队列。客户端向服务器端 connect 时，会发送带有 SYN 标志的包（第一次握手），服务器端
             * 接收到客户端发送的 SYN 时，向客户端发送 SYN ACK 确认（第二次握手），此时 TCP 内核模块把客户端连接加入到 A 队列中，然后服务器接收到
             * 客户端发送的 ACK 时（第三次握手），TCP 内核模块把客户端连接从 A 队列移动到 B 队列，连接完成，应用程序的 accept 会返回。也就是说 accept
             * 从 B 队列中取出完成了三次握手的连接。A 队列和 B 队列的长度之和就是 backlog。
             * 当 A、B 队列的长度之和大于 ChannelOption.SO_BACKLOG 时，新的连接将会被 TCP 内核拒绝。
             * 所以，如果 backlog 过小，可能会出现 accept 速度跟不上，A、B 队列满了，导致新的客户端无法连接。要注意的是，backlog 对程序支持的
             * 连接数并无影响，backlog 影响的只是还没有被 accept 取出的连接。
             */
            b.option(ChannelOption.SO_BACKLOG, 128);            // 设置 TCP 缓冲区
            b.option(ChannelOption.SO_SNDBUF, 32 * 1024);       // 设置发送数据缓冲大小
            b.option(ChannelOption.SO_RCVBUF, 32 * 1024);       // 设置接受数据缓冲大小
            /***
             * option() 是提供给 NioServerSocketChannel 用来接收进来的连接。
             * childOption() 是提供给由父管道 ServerChannel 接收到的连接，
             * 在这个例子中也是 NioServerSocketChannel。
             */
            b.childOption(ChannelOption.SO_KEEPALIVE, true);    // 保持连接
            /***
             * 绑定端口并启动去接收进来的连接
             */
            ChannelFuture f = b.bind(port).sync();
            /**
             * 这里会一直等待，直到 socket 被关闭
             */
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8379;
        }
        new Server(port).run();
    }
}