package com.yiworld.entity;

import lombok.Data;

@Data
public class TransactionalMessageContent {
    private Long id;
    private Long messageId;
    private String content;
}
