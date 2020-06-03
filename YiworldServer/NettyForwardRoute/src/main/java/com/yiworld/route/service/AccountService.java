package com.yiworld.route.service;

import com.yiworld.common.enums.StatusEnum;
import com.yiworld.route.api.vo.request.ChatReqVO;
import com.yiworld.route.api.vo.request.LoginReqVO;
import com.yiworld.route.api.vo.response.ServerResVO;
import com.yiworld.route.api.vo.response.RegisterInfoResVO;

import java.util.Map;

/**
 * Function: 账户服务
 */
public interface AccountService {

    /**
     * 注册用户
     * @param info 用户信息
     * @return
     * @throws Exception
     */
    RegisterInfoResVO register(RegisterInfoResVO info) throws Exception;

    /**
     * 登录服务
     * @param loginReqVO 登录信息
     * @return true 成功 false 失败
     * @throws Exception
     */
    StatusEnum login(LoginReqVO loginReqVO) throws Exception ;

    /**
     * 保存路由信息
     * @param msg 服务器信息
     * @param loginReqVO 用户信息
     * @throws Exception
     */
    void saveRouteInfo(LoginReqVO loginReqVO, String msg) throws Exception ;

    /**
     * 加载所有用户的路有关系
     * @return 所有的路由关系
     */
    Map<Long, ServerResVO> loadRouteRelated() ;

    /**
     * 获取某个用户的路有关系
     * @param userId
     * @return 获取某个用户的路有关系
     */
    ServerResVO loadRouteRelatedByUserId(Long userId) ;


    /**
     * 推送消息
     * @param cimServerResVO
     * @param groupReqVO 消息
     * @param sendUserId 发送者的ID
     * @throws Exception
     */
    void pushMsg(ServerResVO cimServerResVO, long sendUserId, ChatReqVO groupReqVO) throws Exception;

    /**
     * 用户下线
     * @param userId 下线用户ID
     * @throws Exception
     */
    void offLine(Long userId) throws Exception;
}
