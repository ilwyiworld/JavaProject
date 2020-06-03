package com.yiworld.client.service.impl;

import com.yiworld.client.service.CustomMsgHandleListener;
import com.yiworld.client.service.MsgLogger;
import com.yiworld.client.util.SpringBeanFactory;

/**
 * Function:自定义收到消息回调
 */
public class MsgCallBackListener implements CustomMsgHandleListener {

    private MsgLogger msgLogger ;

    public MsgCallBackListener() {
        this.msgLogger = SpringBeanFactory.getBean(MsgLogger.class) ;
    }

    @Override
    public void handle(String msg) {
        msgLogger.log(msg) ;
    }
}
