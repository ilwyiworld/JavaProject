package com.yiworld.server.api;

import com.yiworld.server.api.vo.request.SendMsgReqVO;

/**
 * Hello world!
 */
public interface ServerApi {
    /**
     * Push msg to client
     * @param sendMsgReqVO
     * @return
     * @throws Exception
     */
    Object sendMsg(SendMsgReqVO sendMsgReqVO) throws Exception;
}
