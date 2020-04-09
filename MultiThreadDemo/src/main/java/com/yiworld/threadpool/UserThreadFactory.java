package com.yiworld.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class UserThreadFactory implements ThreadFactory {

    private final String namePrefix;
    private final AtomicInteger nextId=new AtomicInteger(1);

    UserThreadFactory(String whatFearureOfGroup){
        namePrefix="UserThreadFactory's "+whatFearureOfGroup+" -worker-";
    }

    @Override
    public Thread newThread(Runnable r) {
        String name=namePrefix+nextId.getAndIncrement();
        Thread thread=new Thread(null,r,name,0);
        System.out.println(thread.getName());
        return thread;
    }
}
