package com.yiworld.client.service.impl.command;

import com.yiworld.client.service.EchoService;
import com.yiworld.client.service.InnerCommand;
import com.yiworld.client.service.MsgLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueryHistoryCommand implements InnerCommand {
    @Autowired
    private MsgLogger msgLogger;

    @Autowired
    private EchoService echoService;

    @Override
    public void process(String msg) {
        String[] split = msg.split(" ");
        if (split.length < 2) {
            return;
        }
        String res = msgLogger.query(split[1]);
        echoService.echo(res);
    }
}
