package com.yiworld.client.scanner;

import com.yiworld.client.config.AppConfiguration;
import com.yiworld.client.service.EchoService;
import com.yiworld.client.service.MsgHandle;
import com.yiworld.client.service.MsgLogger;
import com.yiworld.client.util.SpringBeanFactory;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class Scan implements Runnable {

    /**
     * 系统参数
     */
    private AppConfiguration configuration;

    private MsgHandle msgHandle;

    private MsgLogger msgLogger;

    private EchoService echoService;

    public Scan() {
        this.configuration = SpringBeanFactory.getBean(AppConfiguration.class);
        this.msgHandle = SpringBeanFactory.getBean(MsgHandle.class);
        this.msgLogger = SpringBeanFactory.getBean(MsgLogger.class);
        this.echoService = SpringBeanFactory.getBean(EchoService.class);
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String msg = sc.nextLine();

            //检查消息
            if (msgHandle.checkMsg(msg)) {
                continue;
            }

            //系统内置命令
            if (msgHandle.innerCommand(msg)) {
                continue;
            }

            //真正的发送消息
            msgHandle.sendMsg(msg);

            //写入聊天记录
            msgLogger.log(msg);

            echoService.echo(EmojiParser.parseToUnicode(msg));
        }
    }
}
