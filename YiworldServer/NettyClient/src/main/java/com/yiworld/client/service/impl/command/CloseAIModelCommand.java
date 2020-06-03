package com.yiworld.client.service.impl.command;

import com.yiworld.client.service.EchoService;
import com.yiworld.client.service.InnerCommand;
import com.yiworld.client.service.MsgHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CloseAIModelCommand implements InnerCommand {
    private final static Logger LOGGER = LoggerFactory.getLogger(CloseAIModelCommand.class);

    @Autowired
    private MsgHandle msgHandle ;

    @Autowired
    private EchoService echoService ;

    @Override
    public void process(String msg) {
        msgHandle.closeAIModel();
        echoService.echo("\033[31;4m" + "｡ﾟ(ﾟ´ω`ﾟ)ﾟ｡  AI 下线了！" + "\033[0m");
    }
}
