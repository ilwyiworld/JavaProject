package com.yiworld.threadpool;

import java.util.concurrent.atomic.AtomicLong;

public class Task implements Runnable {
    private final AtomicLong count=new AtomicLong(0L);
    @Override
    public void run() {
        System.out.println("runnning..."+count.getAndIncrement());
    }
}
