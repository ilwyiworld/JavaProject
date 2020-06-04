package com.yiworld.client;


import com.alibaba.fastjson.JSON;
import com.vdurmont.emoji.EmojiParser;
import com.yiworld.client.vo.response.OnlineUsersResVO;
import com.yiworld.route.api.vo.response.ServerResVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CommonTest {

    @Test
    public void test() {
        String json = "{\"code\":\"9000\",\"message\":\"成功\",\"reqNo\":null,\"dataBody\":{\"ip\":\"127.0.0.1\",\"port\":8081}}" ;
        ServerResVO ServerResVO = JSON.parseObject(json, ServerResVO.class);
        System.out.println(ServerResVO.toString());

        String text = "nihaoaaa" ;
        String[] split = text.split(" ");
        System.out.println(split.length);
    }

    @Test
    public void onlineUser(){
        List<OnlineUsersResVO.DataBodyBean> onlineUsers = new ArrayList<>(64) ;
        OnlineUsersResVO.DataBodyBean bodyBean = new OnlineUsersResVO.DataBodyBean() ;
        bodyBean.setUserId(100L);
        bodyBean.setUserName("zhangsan");
        onlineUsers.add(bodyBean) ;
        bodyBean = new OnlineUsersResVO.DataBodyBean();
        bodyBean.setUserId(200L);
        bodyBean.setUserName("crossoverJie");
        onlineUsers.add(bodyBean) ;
        log.info("list={}",JSON.toJSONString(onlineUsers));
        log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        for (OnlineUsersResVO.DataBodyBean onlineUser : onlineUsers) {
            log.info("userId={}=====userName={}",onlineUser.getUserId(),onlineUser.getUserName());
        }
        log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }

    @Test
    public void searchMsg(){
        StringBuilder sb = new StringBuilder() ;
        String allMsg = "一款面向开发者的 IM(即时通讯)系统；同时提供了一些组件帮助开发者构建一款属于自己可水平扩展的 IM 。\n" +
                "\n" +
                "借助  你可以实现以下需求：" ;
        String key = "IM" ;

        String[] split = allMsg.split("\n");
        for (String msg : split) {
            if (msg.trim().contains(key)){
                sb.append(msg).append("\n") ;
            }
        }
        int pos = 0;

        String result = sb.toString();

        int count = 1 ;
        int multiple = 2 ;
        while((pos = result.indexOf(key, pos)) >= 0) {
            log.info("{},{}",pos, pos + key.length());
            if (count == 1){
                sb.insert(pos,"**");
            }else {
                Double pow = Math.pow(multiple, count);
                sb.insert(pos +pow.intValue(),"**");
            }

            pos += key.length();

            if (count == 1){
                sb.insert(pos +2,"**");
            }else {
                Double pow = Math.pow(multiple, count);
                sb.insert((pos +2) + pow.intValue(),"**");
            }
            count ++ ;
        }
        System.out.println(sb);
    }

    @Test
    public void searchMsg2(){
        StringBuilder sb = new StringBuilder() ;
        String allMsg =
                "(CROSS-IM) 一款面向开发者的 IM(即时通讯)系统；同时提供了一些组件帮助开发者构建一款属于自己可水平扩展的 IM 。\n" +
                "\n" +
                "借助  你可以实现以下需求：" ;
        String key = "" ;

        String[] split = allMsg.split("\n");
        for (String msg : split) {
            if (msg.trim().contains(key)){
                sb.append(msg).append("\n") ;
            }
        }
        int pos = 0;

        String result = sb.toString();

        int count = 1 ;
        int multiple = 2 ;
        while((pos = result.indexOf(key, pos)) >= 0) {
            log.info("{},{}",pos, pos + key.length());
            pos += key.length();
            count ++ ;
        }
        System.out.println(sb.toString());
        System.out.println(sb.toString().replace(key,"\033[31;4m" + key+"\033[0m"));
    }

    @Test
    public void log(){
        String msg = "hahahdsadsd" ;
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        String dir = "/opt/logs//zhangsan" + "/";
        String fileName = dir + year + month + day + ".log";
        log.info("fileName={}", fileName);

        Path file = Paths.get(fileName);
        boolean exists = Files.exists(Paths.get(dir), LinkOption.NOFOLLOW_LINKS);
        try {
            if (!exists) {
                Files.createDirectories(Paths.get(dir));
            }
            List<String> lines = Arrays.asList(msg);
            Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.info("IOException", e);
        }
    }

    @Test
    public void emoji() throws Exception{
        String str = "An :grinning:awesome :smiley:string &#128516;with a few :wink:emojis!";
        String result = EmojiParser.parseToUnicode(str);
        System.out.println(result);

        result = EmojiParser.parseToAliases(str);
        System.out.println(result);
//
//        Collection<Emoji> all = EmojiManager.getAll();
//        for (Emoji emoji : all) {
//            System.out.println(EmojiParser.parseToAliases(emoji.getUnicode())  + "--->" + emoji.getUnicode() );
//        }
    }

    @Test
    public void emoji2(){
        String emostring ="😂";
        String face_with_tears_of_joy = emostring.replaceAll("\uD83D\uDE02", "face with tears of joy");
        System.out.println(face_with_tears_of_joy);
        System.out.println("======" + face_with_tears_of_joy.replaceAll("face with tears of joy","\uD83D\uDE02"));
    }

}
