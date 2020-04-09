package com.yiworld.countdownlatch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MoreWaitOne {
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch latch=new CountDownLatch(1);
        ExecutorService executorService= Executors.newFixedThreadPool(5);
        for(int i=0;i<5;i++){
            executorService.execute(()->{
                System.out.println(Thread.currentThread().getName()+ " ready! ");
                try {
                    latch.await();
                    System.out.println(Thread.currentThread().getName()+ " produce ....");
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        Thread.sleep(10);
        System.out.println(Thread.currentThread().getName() + " ready!");
        latch.countDown();
        System.out.println(Thread.currentThread().getName() + " go!");
        executorService.shutdown();
    }
}
