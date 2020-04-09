package com.yiworld.rabbitmqapi;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @PostMapping("/sendQueue")
    public void sendQueue(@RequestBody String data){
        try {
            MessageProperties mp = new MessageProperties();
            mp.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            Message message = new Message(data.getBytes(), mp);
            rabbitTemplate.convertAndSend("cimevue.cp.exchange.message", "", message);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }
}
