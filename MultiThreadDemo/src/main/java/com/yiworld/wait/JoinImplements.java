package com.yiworld.wait;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class JoinImplements {
    public static void main(String[] args) throws InterruptedException {
        Runnable runnable = () -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " was finished!");
        };
        Thread thread1 = new Thread(runnable);
        thread1.start();
        System.out.println("start to wait child threads.");
        thread1.join(); // 等价于下方代码
        /*synchronized (thread1) {
            thread1.wait();
        }*/
        System.out.println("all threads run completed!");
        LongAdder addr=new LongAdder();

    }
}