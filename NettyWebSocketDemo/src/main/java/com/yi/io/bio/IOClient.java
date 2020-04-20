package com.yi.io.bio;

import java.net.Socket;
import java.util.Date;

public class IOClient {
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                Socket socket = new Socket("127.0.0.1", 8000);
                while (true) {
                    try {
                        socket.getOutputStream().write((new Date() + ":hello world").getBytes());
                        socket.getOutputStream().flush();
                        Thread.sleep(200);
                    } catch (Exception E) {
                    }
                }
            } catch (Exception e) {
            }
        }).start();
    }
}
