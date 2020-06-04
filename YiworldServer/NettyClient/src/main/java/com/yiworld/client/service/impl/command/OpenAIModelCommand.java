package com.yiworld.client.service.impl.command;

import com.yiworld.client.service.InnerCommand;
import com.yiworld.client.service.MsgHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenAIModelCommand implements InnerCommand {
    @Autowired
    private MsgHandle msgHandle ;

    @Override
    public void process(String msg) {
        msgHandle.openAIModel();
        System.out.println("\033[31;4m" + "Hello,我是估值两亿的 AI 机器人！" + "\033[0m");
    }
}
