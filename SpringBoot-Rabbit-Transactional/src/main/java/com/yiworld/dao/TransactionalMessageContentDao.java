package com.yiworld.dao;

import com.yiworld.entity.TransactionalMessageContent;

import java.util.List;

public interface TransactionalMessageContentDao {
    void insert(TransactionalMessageContent record);

    List<TransactionalMessageContent> queryByMessageIds(String messageIds);
}
