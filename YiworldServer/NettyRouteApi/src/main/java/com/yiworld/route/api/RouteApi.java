package com.yiworld.route.api;

import com.yiworld.common.response.BaseResponse;
import com.yiworld.route.api.vo.request.ChatReqVO;
import com.yiworld.route.api.vo.request.LoginReqVO;
import com.yiworld.route.api.vo.request.P2PReqVO;
import com.yiworld.route.api.vo.request.RegisterInfoReqVO;
import com.yiworld.route.api.vo.response.RegisterInfoResVO;

/**
 * Hello world!
 */
public interface RouteApi {
    /**
     * group chat
     *
     * @param groupReqVO
     * @return
     * @throws Exception
     */
    Object groupRoute(ChatReqVO groupReqVO) throws Exception;

    /**
     * Point to point chat
     * @param p2pRequest
     * @return
     * @throws Exception
     */
    Object p2pRoute(P2PReqVO p2pRequest) throws Exception;


    /**
     * Offline account
     *
     * @param groupReqVO
     * @return
     * @throws Exception
     */
    Object offLine(ChatReqVO groupReqVO) throws Exception;

    /**
     * Login account
     * @param loginReqVO
     * @return
     * @throws Exception
     */
    Object login(LoginReqVO loginReqVO) throws Exception;

    /**
     * Register account
     *
     * @param registerInfoReqVO
     * @return
     * @throws Exception
     */
    BaseResponse<RegisterInfoResVO> registerAccount(RegisterInfoReqVO registerInfoReqVO) throws Exception;

    /**
     * Get all online users
     *
     * @return
     * @throws Exception
     */
    Object onlineUser() throws Exception;
}
