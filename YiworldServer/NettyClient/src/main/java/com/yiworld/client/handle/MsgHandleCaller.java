package com.yiworld.client.handle;

import com.yiworld.client.service.CustomMsgHandleListener;

/**
 * Function:消息回调 bean
 */
public class MsgHandleCaller {

    /**
     * 回调接口
     */
    private CustomMsgHandleListener msgHandleListener;

    public MsgHandleCaller(CustomMsgHandleListener msgHandleListener) {
        this.msgHandleListener = msgHandleListener;
    }

    public CustomMsgHandleListener getMsgHandleListener() {
        return msgHandleListener;
    }

    public void setMsgHandleListener(CustomMsgHandleListener msgHandleListener) {
        this.msgHandleListener = msgHandleListener;
    }
}
