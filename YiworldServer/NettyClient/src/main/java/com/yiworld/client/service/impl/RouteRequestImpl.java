package com.yiworld.client.service.impl;

import com.alibaba.fastjson.JSON;
import com.yiworld.client.config.AppConfiguration;
import com.yiworld.client.service.EchoService;
import com.yiworld.client.service.RouteRequest;
import com.yiworld.client.thread.ContextHolder;
import com.yiworld.client.vo.request.GroupReqVO;
import com.yiworld.client.vo.request.LoginReqVO;
import com.yiworld.client.vo.request.P2PReqVO;
import com.yiworld.client.vo.response.ServerResVO;
import com.yiworld.client.vo.response.OnlineUsersResVO;
import com.yiworld.common.proxy.ProxyManager;
import com.yiworld.common.enums.StatusEnum;
import com.yiworld.common.exception.YiworldException;
import com.yiworld.common.response.BaseResponse;
import com.yiworld.route.api.RouteApi;
import com.yiworld.route.api.vo.request.ChatReqVO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RouteRequestImpl implements RouteRequest {

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${yiworld.route.url}")
    private String routeUrl;

    @Autowired
    private EchoService echoService;

    @Autowired
    private AppConfiguration appConfiguration;

    @Override
    public void sendGroupMsg(GroupReqVO groupReqVO) throws Exception {
        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();
        ChatReqVO chatReqVO = new ChatReqVO(groupReqVO.getUserId(), groupReqVO.getMsg());
        Response response = null;
        try {
            response = (Response) routeApi.groupRoute(chatReqVO);
        } catch (Exception e) {
            log.error("exception", e);
        } finally {
            response.body().close();
        }
    }

    @Override
    public void sendP2PMsg(P2PReqVO p2PReqVO) throws Exception {
        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();
        com.yiworld.route.api.vo.request.P2PReqVO vo = new com.yiworld.route.api.vo.request.P2PReqVO();
        vo.setMsg(p2PReqVO.getMsg());
        vo.setReceiveUserId(p2PReqVO.getReceiveUserId());
        vo.setUserId(p2PReqVO.getUserId());

        Response response = null;
        try {
            response = (Response) routeApi.p2pRoute(vo);
            String json = response.body().string();
            BaseResponse baseResponse = JSON.parseObject(json, BaseResponse.class);
            // account offline.
            if (baseResponse.getCode().equals(StatusEnum.OFF_LINE.getCode())) {
                log.error(p2PReqVO.getReceiveUserId() + ":" + StatusEnum.OFF_LINE.getMessage());
            }

        } catch (Exception e) {
            log.error("exception", e);
        } finally {
            response.body().close();
        }
    }

    @Override
    public ServerResVO.ServerInfo getServer(LoginReqVO loginReqVO) throws Exception {

        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();
        com.yiworld.route.api.vo.request.LoginReqVO vo = new com.yiworld.route.api.vo.request.LoginReqVO();
        vo.setUserId(loginReqVO.getUserId());
        vo.setUserName(loginReqVO.getUserName());

        Response response = null;
        ServerResVO serverResVO = null;
        try {
            response = (Response) routeApi.login(vo);
            String json = response.body().string();
            serverResVO = JSON.parseObject(json, ServerResVO.class);

            // 重复失败
            if (!serverResVO.getCode().equals(StatusEnum.SUCCESS.getCode())) {
                echoService.echo(serverResVO.getMessage());
                // when client in reConnect state, could not exit.
                if (ContextHolder.getReconnect()) {
                    echoService.echo("###{}###", StatusEnum.RECONNECT_FAIL.getMessage());
                    throw new YiworldException(StatusEnum.RECONNECT_FAIL);
                }
                System.exit(-1);
            }
        } catch (Exception e) {
            log.error("exception", e);
        } finally {
            response.body().close();
        }

        return serverResVO.getDataBody();
    }

    @Override
    public List<OnlineUsersResVO.DataBodyBean> onlineUsers() throws Exception {
        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();

        Response response = null;
        OnlineUsersResVO onlineUsersResVO = null;
        try {
            response = (Response) routeApi.onlineUser();
            String json = response.body().string();
            onlineUsersResVO = JSON.parseObject(json, OnlineUsersResVO.class);
        } catch (Exception e) {
            log.error("exception", e);
        } finally {
            response.body().close();
        }
        return onlineUsersResVO.getDataBody();
    }

    @Override
    public void offLine() {
        RouteApi routeApi = new ProxyManager<>(RouteApi.class, routeUrl, okHttpClient).getInstance();
        ChatReqVO vo = new ChatReqVO(appConfiguration.getUserId(), "offLine");
        Response response = null;
        try {
            response = (Response) routeApi.offLine(vo);
        } catch (Exception e) {
            log.error("exception", e);
        } finally {
            response.body().close();
        }
    }
}
