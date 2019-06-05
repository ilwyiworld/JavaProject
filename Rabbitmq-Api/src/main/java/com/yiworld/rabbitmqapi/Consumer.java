package com.yiworld.rabbitmqapi;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer {
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory=new ConnectionFactory();
        factory.setHost("10.10.1.163");
        factory.setPort(5672);
        factory.setVirtualHost("/");

        Connection connection=factory.newConnection();

        Channel channel=connection.createChannel();
        //声明一个queue
        channel.queueDeclare("test001",true,false,false,null);
        //创建一个消费者
        QueueingConsumer consumer=new QueueingConsumer(channel);
        //设置channel
        channel.basicConsume("test001",true,consumer);
        //获取消息
        while(true){
            Delivery delivery = consumer.nextDelivery();
            String msg = new String(delivery.getBody());
            System.err.println("消费端: " + msg);
            //Envelope envelope = delivery.getEnvelope();
        }
    }
}
