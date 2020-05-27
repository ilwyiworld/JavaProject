package com.yiworld.support.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 消息状态
@Getter
@RequiredArgsConstructor
public enum TxMessageStatus {

    /**
     * 成功
     */
    SUCCESS(1),

    /**
     * 待处理
     */
    PENDING(0),

    /**
     * 处理失败
     */
    FAIL(-1),

    ;

    private final Integer status;
}