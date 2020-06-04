package com.yiworld.client.handle;

import com.yiworld.client.service.CustomMsgHandleListener;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Function:消息回调 bean
 */
@Data
@AllArgsConstructor
public class MsgHandleCaller {
    /**
     * 回调接口
     */
    private CustomMsgHandleListener msgHandleListener;
}
