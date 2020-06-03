package com.yiworld.client;

import com.yiworld.client.config.AppConfiguration;
import com.yiworld.client.init.ClientHandleInitializer;
import com.yiworld.client.service.EchoService;
import com.yiworld.client.service.MsgHandle;
import com.yiworld.client.service.ReConnectManager;
import com.yiworld.client.service.RouteRequest;
import com.yiworld.client.service.impl.ClientInfo;
import com.yiworld.client.thread.ContextHolder;
import com.yiworld.client.vo.request.GoogleProtocolVO;
import com.yiworld.client.vo.request.LoginReqVO;
import com.yiworld.client.vo.response.ServerResVO;
import com.yiworld.constant.Constants;
import com.yiworld.common.protocol.RequestProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class Client {

    private final static Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private EventLoopGroup group = new NioEventLoopGroup(0, new DefaultThreadFactory("yiworld-work"));

    @Value("${yiworld.user.id}")
    private long userId;

    @Value("${yiworld.user.userName}")
    private String userName;

    private SocketChannel channel;

    @Autowired
    private EchoService echoService ;

    @Autowired
    private RouteRequest routeRequest;

    @Autowired
    private AppConfiguration configuration;

    @Autowired
    private MsgHandle msgHandle;

    @Autowired
    private ClientInfo clientInfo;

    @Autowired
    private ReConnectManager reConnectManager ;

    /**
     * 重试次数
     */
    private int errorCount;

    @PostConstruct
    public void start() throws Exception {
        //登录 + 获取可以使用的服务器 ip+port
        ServerResVO.ServerInfo yiworldServer = userLogin();

        //启动客户端
        startClient(yiworldServer);

        //向服务端注册
        loginServer();
    }

    /**
     * 启动客户端
     *
     * @param yiworldServer
     * @throws Exception
     */
    private void startClient(ServerResVO.ServerInfo yiworldServer) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ClientHandleInitializer())
        ;

        ChannelFuture future = null;
        try {
            future = bootstrap.connect(yiworldServer.getIp(), yiworldServer.getCimServerPort()).sync();
        } catch (Exception e) {
            errorCount++;

            if (errorCount >= configuration.getErrorCount()) {
                LOGGER.error("连接失败次数达到上限[{}]次", errorCount);
                msgHandle.shutdown();
            }
            LOGGER.error("Connect fail!", e);
        }
        if (future.isSuccess()) {
            echoService.echo("Start yiworld client success!");
            LOGGER.info("启动 yiworld client 成功");
        }
        channel = (SocketChannel) future.channel();
    }

    /**
     * 登录+路由服务器
     *
     * @return 路由服务器信息
     * @throws Exception
     */
    private ServerResVO.ServerInfo userLogin() {
        LoginReqVO loginReqVO = new LoginReqVO(userId, userName);
        ServerResVO.ServerInfo yiworldServer = null;
        try {
            yiworldServer = routeRequest.getServer(loginReqVO);

            //保存系统信息
            clientInfo.saveServiceInfo(yiworldServer.getIp() + ":" + yiworldServer.getCimServerPort())
                    .saveUserInfo(userId, userName);

            LOGGER.info("yiworldServer=[{}]", yiworldServer.toString());
        } catch (Exception e) {
            errorCount++;

            if (errorCount >= configuration.getErrorCount()) {
                echoService.echo("The maximum number of reconnections has been reached[{}]times, close yiworld client!", errorCount);
                msgHandle.shutdown();
            }
            LOGGER.error("login fail", e);
        }
        return yiworldServer;
    }

    /**
     * 向服务器注册
     */
    private void loginServer() {
        RequestProto.ReqProtocol login = RequestProto.ReqProtocol.newBuilder()
                .setRequestId(userId)
                .setReqMsg(userName)
                .setType(Constants.CommandType.LOGIN)
                .build();
        ChannelFuture future = channel.writeAndFlush(login);
        future.addListener((ChannelFutureListener) channelFuture ->
                        echoService.echo("Registry yiworld server success!")
                );
    }

    /**
     * 发送消息字符串
     *
     * @param msg
     */
    public void sendStringMsg(String msg) {
        ByteBuf message = Unpooled.buffer(msg.getBytes().length);
        message.writeBytes(msg.getBytes());
        ChannelFuture future = channel.writeAndFlush(message);
        future.addListener((ChannelFutureListener) channelFuture ->
                LOGGER.info("客户端手动发消息成功={}", msg));

    }

    /**
     * 发送 Google Protocol 编解码字符串
     *
     * @param googleProtocolVO
     */
    public void sendGoogleProtocolMsg(GoogleProtocolVO googleProtocolVO) {

        RequestProto.ReqProtocol protocol = RequestProto.ReqProtocol.newBuilder()
                .setRequestId(googleProtocolVO.getRequestId())
                .setReqMsg(googleProtocolVO.getMsg())
                .setType(Constants.CommandType.MSG)
                .build();


        ChannelFuture future = channel.writeAndFlush(protocol);
        future.addListener((ChannelFutureListener) channelFuture ->
                LOGGER.info("客户端手动发送 Google Protocol 成功={}", googleProtocolVO.toString()));

    }

    /**
     * 1. clear route information.
     * 2. reconnect.
     * 3. shutdown reconnect job.
     * 4. reset reconnect state.
     * @throws Exception
     */
    public void reconnect() throws Exception {
        if (channel != null && channel.isActive()) {
            return;
        }
        //首先清除路由信息，下线
        routeRequest.offLine();

        echoService.echo("yiworld server shutdown, reconnecting....");
        start();
        echoService.echo("Great! reConnect success!!!");
        reConnectManager.reConnectSuccess();
        ContextHolder.clear();
    }

    /**
     * 关闭
     *
     * @throws InterruptedException
     */
    public void close() throws InterruptedException {
        if (channel != null){
            channel.close();
        }
    }
}
