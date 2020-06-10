package com.yiworld.core.remoting.transport;

import com.yiworld.core.remoting.dto.RpcRequest;

/**
 * 传输 RpcRequest。
 */
public interface ClientTransport {
    /**
     * 发送消息到服务端
     *
     * @param rpcRequest 消息体
     * @return 服务端返回的数据
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}