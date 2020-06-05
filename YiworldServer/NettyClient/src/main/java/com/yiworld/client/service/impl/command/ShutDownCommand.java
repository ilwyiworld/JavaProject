package com.yiworld.client.service.impl.command;

import com.yiworld.client.Client;
import com.yiworld.client.service.EchoService;
import com.yiworld.client.service.InnerCommand;
import com.yiworld.client.service.MsgLogger;
import com.yiworld.client.service.RouteRequest;
import com.yiworld.client.service.ShutDownMsg;
import com.yiworld.common.datastruct.RingBufferWheel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ShutDownCommand implements InnerCommand {
    @Autowired
    private RouteRequest routeRequest ;

    @Autowired
    private Client client;

    @Autowired
    private MsgLogger msgLogger;

    @Resource(name = "callBackThreadPool")
    private ThreadPoolExecutor callBackExecutor;

    @Autowired
    private EchoService echoService ;

    @Autowired
    private ShutDownMsg shutDownMsg ;

    @Autowired
    private RingBufferWheel ringBufferWheel ;

    @Override
    public void process(String msg) {
        echoService.echo("client closing...");
        shutDownMsg.shutdown();
        routeRequest.offLine();
        msgLogger.stop();
        callBackExecutor.shutdown();
        ringBufferWheel.stop(false);
        try {
            while (!callBackExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                echoService.echo("thread pool closing");
            }
            client.close();
        } catch (InterruptedException e) {
            log.error("InterruptedException", e);
        }
        echoService.echo("close success!");
        System.exit(0);
    }
}
