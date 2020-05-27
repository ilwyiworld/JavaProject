package com.yiworld;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * Hello world!
 */
public class App {
    public static WebSocketClient client;

    public static void main(String[] args) throws URISyntaxException, UnsupportedEncodingException {
        client = new WebSocketClient(new URI("ws://10.10.1.53:9080/parser-admin/alarmInfoSocketServer"), new Draft_17()) {
        //client = new WebSocketClient(new URI("ws://121.40.165.18:8800"), new Draft_17()) {
            @Override
            public void onOpen(ServerHandshake arg0) {
                System.out.println("打开连接");
            }

            @Override
            public void onMessage(String arg0) {
                System.out.println("收到消息" + arg0);
            }

            @Override
            public void onError(Exception arg0) {
                arg0.printStackTrace();
                System.out.println("发生错误已关闭");
            }

            @Override
            public void onClose(int arg0, String arg1, boolean arg2) {
                System.out.println("连接已关闭");
            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                try {
                    System.out.println(new String(bytes.array(), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        };

        client.connect();

        while (!client.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
            System.out.println("还没有打开");
        }
        System.out.println("打开了");
        send("hello world".getBytes("utf-8"));
        client.send("hello world");
    }

    public static void send(byte[] bytes) {
        client.send(bytes);
    }
}
