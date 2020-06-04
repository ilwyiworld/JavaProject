package com.yiworld.client.service.impl;

import com.yiworld.client.Client;
import com.yiworld.client.config.AppConfiguration;
import com.yiworld.client.service.InnerCommand;
import com.yiworld.client.service.InnerCommandContext;
import com.yiworld.client.service.MsgHandle;
import com.yiworld.client.service.MsgLogger;
import com.yiworld.client.service.RouteRequest;
import com.yiworld.client.vo.request.GroupReqVO;
import com.yiworld.client.vo.request.P2PReqVO;
import com.yiworld.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MsgHandler implements MsgHandle {
    @Autowired
    private RouteRequest routeRequest;

    @Autowired
    private AppConfiguration configuration;

    @Resource(name = "callBackThreadPool")
    private ThreadPoolExecutor executor;

    @Autowired
    private Client client;

    @Autowired
    private MsgLogger msgLogger;

    @Autowired
    private ClientInfo clientInfo;

    @Autowired
    private InnerCommandContext innerCommandContext;

    private boolean aiModel = false;

    @Override
    public void sendMsg(String msg) {
        if (aiModel) {
            aiChat(msg);
        } else {
            normalChat(msg);
        }
    }

    /**
     * 正常聊天
     *
     * @param msg
     */
    private void normalChat(String msg) {
        String[] totalMsg = msg.split(";;");
        if (totalMsg.length > 1) {
            //私聊
            P2PReqVO p2PReqVO = new P2PReqVO();
            p2PReqVO.setUserId(configuration.getUserId());
            p2PReqVO.setReceiveUserId(Long.parseLong(totalMsg[0]));
            p2PReqVO.setMsg(totalMsg[1]);
            try {
                p2pChat(p2PReqVO);
            } catch (Exception e) {
                log.error("Exception", e);
            }

        } else {
            // 群聊
            GroupReqVO groupReqVO = new GroupReqVO(configuration.getUserId(), msg);
            try {
                groupChat(groupReqVO);
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }
    }

    /**
     * AI model
     *
     * @param msg
     */
    private void aiChat(String msg) {
        msg = msg.replace("吗", "");
        msg = msg.replace("嘛", "");
        msg = msg.replace("?", "!");
        msg = msg.replace("？", "!");
        msg = msg.replace("你", "我");
        System.out.println("AI:\033[31;4m" + msg + "\033[0m");
    }

    @Override
    public void groupChat(GroupReqVO groupReqVO) throws Exception {
        routeRequest.sendGroupMsg(groupReqVO);
    }

    @Override
    public void p2pChat(P2PReqVO p2PReqVO) throws Exception {
        routeRequest.sendP2PMsg(p2PReqVO);
    }

    @Override
    public boolean checkMsg(String msg) {
        if (StringUtil.isEmpty(msg)) {
            log.warn("不能发送空消息！");
            return true;
        }
        return false;
    }

    @Override
    public boolean innerCommand(String msg) {
        if (msg.startsWith(":")) {
            InnerCommand instance = innerCommandContext.getInstance(msg);
            instance.process(msg);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 关闭系统
     */
    @Override
    public void shutdown() {
        log.info("系统关闭中。。。。");
        routeRequest.offLine();
        msgLogger.stop();
        executor.shutdown();
        try {
            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                log.info("线程池关闭中。。。。");
            }
            client.close();
        } catch (InterruptedException e) {
            log.error("InterruptedException", e);
        }
        System.exit(0);
    }

    @Override
    public void openAIModel() {
        aiModel = true;
    }

    @Override
    public void closeAIModel() {
        aiModel = false;
    }

}
