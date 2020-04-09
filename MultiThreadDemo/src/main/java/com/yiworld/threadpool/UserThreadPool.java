package com.yiworld.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UserThreadPool {
    public static void main(String[] args) {
        //缓存队列设置固定长度2
        BlockingQueue queue=new LinkedBlockingQueue(2);

        UserThreadFactory f1=new UserThreadFactory("第一");
        UserThreadFactory f2=new UserThreadFactory("第二");

        UserRejectHandler handler = new UserRejectHandler();
        ThreadPoolExecutor poolFirst=new ThreadPoolExecutor(1,2,60,
                TimeUnit.SECONDS,queue,f1,handler);
        ThreadPoolExecutor poolSecond=new ThreadPoolExecutor(1,2,60,
                TimeUnit.SECONDS,queue,f2,handler);
        //创建400个线程任务
        Runnable task=new Task();
        for (int i=0;i<200;i++){
            poolFirst.execute(task);
            poolSecond.execute(task);
        }
    }
}
