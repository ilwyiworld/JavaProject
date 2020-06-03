package com.yiworld.client.service;

import com.yiworld.client.vo.request.GroupReqVO;
import com.yiworld.client.vo.request.LoginReqVO;
import com.yiworld.client.vo.request.P2PReqVO;
import com.yiworld.client.vo.response.ServerResVO;
import com.yiworld.client.vo.response.OnlineUsersResVO;

import java.util.List;

public interface RouteRequest {

    /**
     * 群发消息
     * @param groupReqVO 消息
     * @throws Exception
     */
    void sendGroupMsg(GroupReqVO groupReqVO) throws Exception;

    /**
     * 私聊
     * @param p2PReqVO
     * @throws Exception
     */
    void sendP2PMsg(P2PReqVO p2PReqVO)throws Exception;

    /**
     * 获取服务器
     * @return 服务ip+port
     * @param loginReqVO
     * @throws Exception
     */
    ServerResVO.ServerInfo getServer(LoginReqVO loginReqVO) throws Exception;

    /**
     * 获取所有在线用户
     * @return
     * @throws Exception
     */
    List<OnlineUsersResVO.DataBodyBean> onlineUsers()throws Exception ;


    void offLine() ;

}
