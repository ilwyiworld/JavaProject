package com.yiworld.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WsController {

    @Autowired
    //向浏览器发送消息
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    /**
     * 假如这条消息是由A用户发来的，那么毫无疑问这条消息要转发给B用户，
     * 如果这条消息是由B用户发来的，那么毫无疑问这条消息要转发给A
     */
    public void handleChat(Principal principal, String msg) {
        //可以直接在参数中获取Principal，Principal中包含有当前用户的用户名
        if (principal.getName().equals("yiliang")) {
            //第一个参数是目标用户用户名，第二个参数是浏览器中订阅消息的地址，第三个参数是消息本身
            messagingTemplate.convertAndSendToUser("root", "/queue/notifications", principal.getName() + "给您发来了消息：" + msg);
        }else{
            messagingTemplate.convertAndSendToUser("yiliang", "/queue/notifications", principal.getName() + "给您发来了消息：" + msg);
        }
    }
}
