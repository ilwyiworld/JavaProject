package com.yiworld.rabbitmqapi;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producer {
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory=new ConnectionFactory();
        factory.setHost("10.10.1.245");
        factory.setPort(5672);
        factory.setVirtualHost("/");

        Connection connection=factory.newConnection();

        Channel channel=connection.createChannel();
        String body="hello rabbitmq";
        // 1 exchange   2 routingKey
        // The default exchange is implicitly bound to every queue, with a routing key equal to the queue name.
        // It is not possible to explicitly bind to, or unbind from the default exchange. It also cannot be deleted.
        channel.basicPublish("","test001",null,body.getBytes());

        channel.close();
        connection.close();
    }
}
