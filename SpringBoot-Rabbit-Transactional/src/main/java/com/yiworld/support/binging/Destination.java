package com.yiworld.support.binging;

// 发送消息的目的地
public interface Destination {

    ExchangeType exchangeType();

    String queueName();

    String exchangeName();

    String routingKey();
}