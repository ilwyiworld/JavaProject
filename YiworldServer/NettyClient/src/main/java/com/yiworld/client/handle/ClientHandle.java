package com.yiworld.client.handle;

import com.yiworld.client.service.EchoService;
import com.yiworld.client.service.ReConnectManager;
import com.yiworld.client.service.ShutDownMsg;
import com.yiworld.client.service.impl.EchoServiceImpl;
import com.yiworld.client.util.SpringBeanFactory;
import com.yiworld.constant.Constants;
import com.yiworld.common.protocol.RequestProto;
import com.yiworld.common.protocol.ResponseProto;
import com.yiworld.common.util.NettyAttrUtil;
import com.vdurmont.emoji.EmojiParser;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

@ChannelHandler.Sharable
public class ClientHandle extends SimpleChannelInboundHandler<ResponseProto.ResProtocol> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientHandle.class);

    private MsgHandleCaller caller ;

    private ThreadPoolExecutor threadPoolExecutor ;

    private ScheduledExecutorService scheduledExecutorService ;

    private ReConnectManager reConnectManager ;

    private ShutDownMsg shutDownMsg ;

    private EchoService echoService ;


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt ;

            if (idleStateEvent.state() == IdleState.WRITER_IDLE){
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
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //客户端和服务端建立连接时调用
        LOGGER.info("cim server connect success!");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (shutDownMsg == null){
            shutDownMsg = SpringBeanFactory.getBean(ShutDownMsg.class) ;
        }

        //用户主动退出，不执行重连逻辑
        if (shutDownMsg.checkStatus()){
            return;
        }

        if (scheduledExecutorService == null){
            scheduledExecutorService = SpringBeanFactory.getBean("scheduledTask",ScheduledExecutorService.class) ;
            reConnectManager = SpringBeanFactory.getBean(ReConnectManager.class) ;
        }
        LOGGER.info("客户端断开了，重新连接！");
        reConnectManager.reConnect(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseProto.ResProtocol msg) throws Exception {
        if (echoService == null){
            echoService = SpringBeanFactory.getBean(EchoServiceImpl.class) ;
        }

        //心跳更新时间
        if (msg.getType() == Constants.CommandType.PING){
            //LOGGER.info("收到服务端心跳！！！");
            NettyAttrUtil.updateReaderTime(ctx.channel(),System.currentTimeMillis());
        }

        if (msg.getType() != Constants.CommandType.PING) {
            //回调消息
            callBackMsg(msg.getResMsg());

            //将消息中的 emoji 表情格式化为 Unicode 编码以便在终端可以显示
            String response = EmojiParser.parseToUnicode(msg.getResMsg());
            echoService.echo(response);
        }
    }

    /**
     * 回调消息
     * @param msg
     */
    private void callBackMsg(String msg) {
        threadPoolExecutor = SpringBeanFactory.getBean("callBackThreadPool",ThreadPoolExecutor.class) ;
        threadPoolExecutor.execute(() -> {
            caller = SpringBeanFactory.getBean(MsgHandleCaller.class) ;
            caller.getMsgHandleListener().handle(msg);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //异常时断开连接
        cause.printStackTrace() ;
        ctx.close() ;
    }
}
