package com.yiworld.service;

import com.yiworld.support.binging.Destination;
import com.yiworld.support.message.TxMessage;

// 对外提供的服务类接口
public interface TransactionalMessageService {
    void sendTransactionalMessage(Destination destination, TxMessage message);
}