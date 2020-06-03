package com.yiworld.server.handle;

import com.yiworld.constant.Constants;
import com.yiworld.common.exception.YiworldException;
import com.yiworld.common.kit.HeartBeatHandler;
import com.yiworld.common.pojo.UserInfo;
import com.yiworld.common.protocol.RequestProto;
import com.yiworld.common.util.NettyAttrUtil;
import com.yiworld.server.kit.RouteHandler;
import com.yiworld.server.kit.ServerHeartBeatHandlerImpl;
import com.yiworld.server.util.SessionSocketHolder;
import com.yiworld.server.util.SpringBeanFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class ServerHandle extends SimpleChannelInboundHandler<RequestProto.ReqProtocol> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerHandle.class);

    /**
     * 取消绑定
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //可能出现业务判断离线后再次触发 channelInactive
        UserInfo userInfo = SessionSocketHolder.getUserId((NioSocketChannel) ctx.channel());
        if (userInfo != null){
            LOGGER.warn("[{}] trigger channelInactive offline!",userInfo.getUserName());
            //Clear route info and offline.
            RouteHandler routeHandler = SpringBeanFactory.getBean(RouteHandler.class);
            routeHandler.userOffLine(userInfo,(NioSocketChannel) ctx.channel());
            ctx.channel().close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                LOGGER.info("定时检测客户端端是否存活");
                HeartBeatHandler heartBeatHandler = SpringBeanFactory.getBean(ServerHeartBeatHandlerImpl.class) ;
                heartBeatHandler.process(ctx) ;
            }
        }
        super.userEventTriggered(ctx, evt);
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestProto.ReqProtocol msg) {
        LOGGER.info("received msg=[{}]", msg.toString());
        if (msg.getType() == Constants.CommandType.LOGIN) {
            //保存客户端与 Channel 之间的关系
            SessionSocketHolder.put(msg.getRequestId(), (NioSocketChannel) ctx.channel());
            SessionSocketHolder.saveSession(msg.getRequestId(), msg.getReqMsg());
            LOGGER.info("client [{}] online success!!", msg.getReqMsg());
        }

        //心跳更新时间
        if (msg.getType() == Constants.CommandType.PING){
            NettyAttrUtil.updateReaderTime(ctx.channel(),System.currentTimeMillis());
            //向客户端响应 pong 消息
            RequestProto.ReqProtocol heartBeat = SpringBeanFactory.getBean("heartBeat",
                    RequestProto.ReqProtocol.class);
            ctx.writeAndFlush(heartBeat).addListeners((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    LOGGER.error("IO error,close Channel");
                    future.channel().close();
                }
            }) ;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (YiworldException.isResetByPeer(cause.getMessage())) {
            return;
        }
        LOGGER.error(cause.getMessage(), cause);
    }
}
