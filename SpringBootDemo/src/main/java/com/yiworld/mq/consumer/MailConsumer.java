package com.yiworld.mq.consumer;

import com.rabbitmq.client.Channel;
import com.yiworld.exception.ServiceException;
import com.yiworld.mq.BaseConsumer;
import com.yiworld.mq.MessageHelper;
import com.yiworld.pojo.Mail;
import com.yiworld.util.MailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailConsumer implements BaseConsumer {

    @Autowired
    private MailUtil mailUtil;

    public void consume(Message message, Channel channel) {
        Mail mail = MessageHelper.msgToObj(message, Mail.class);
        log.info("收到消息: {}", mail.toString());

        boolean success = mailUtil.send(mail);
        if (!success) {
            throw new ServiceException("send mail error");
        }
    }

}
