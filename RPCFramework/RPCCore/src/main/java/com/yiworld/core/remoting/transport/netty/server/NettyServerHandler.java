package com.yiworld.core.remoting.transport.netty.server;

import com.yiworld.common.factory.SingletonFactory;
import com.yiworld.common.utils.threadpool.CustomThreadPoolConfig;
import com.yiworld.common.utils.threadpool.ThreadPoolFactoryUtils;
import com.yiworld.core.handler.RpcRequestHandler;
import com.yiworld.core.remoting.dto.RpcRequest;
import com.yiworld.core.remoting.dto.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final String THREAD_NAME_PREFIX = "netty-server-handler-rpc-pool";
    private final RpcRequestHandler rpcRequestHandler;
    private final ExecutorService threadPool;

    public NettyServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        customThreadPoolConfig.setCorePoolSize(6);
        this.threadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent(THREAD_NAME_PREFIX, customThreadPoolConfig);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        threadPool.execute(() -> {
            try {
                log.info("server receive msg: [{}] ", msg);
                RpcRequest rpcRequest = (RpcRequest) msg;
                // 执行目标方法（客户端需要执行的方法）并且返回方法结果
                Object result = rpcRequestHandler.handle(rpcRequest);
                log.info(String.format("server get result: %s", result.toString()));
                if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                    // 返回方法执行结果给客户端
                    RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                    ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    log.error("not writable now, message dropped");
                }
            } finally {
                // 确保 ByteBuf 被释放，不然可能会有内存泄露问题
                ReferenceCountUtil.release(msg);
            }
        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
