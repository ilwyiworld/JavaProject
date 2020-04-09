package com.yiworld.countdownlatch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class OneWaitMore {
    public static void main(String[] args) throws InterruptedException{
        AtomicInteger integer=new AtomicInteger(0);
        CountDownLatch latch=new CountDownLatch(5);
        ExecutorService executorService= Executors.newFixedThreadPool(5);
        for(int i=0;i<5;i++){
            executorService.execute(()->{
                System.out.println(Thread.currentThread().getName()+ " produce ....");
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                integer.incrementAndGet();
                latch.countDown();
            });
        }
        System.out.println(Thread.currentThread().getName() + " waiting....");
        latch.await();
        System.out.println(Thread.currentThread().getName() + " finished!");
        System.out.println(Thread.currentThread().getName() + " num: " +  integer.get());
        executorService.shutdown();
    }
}
