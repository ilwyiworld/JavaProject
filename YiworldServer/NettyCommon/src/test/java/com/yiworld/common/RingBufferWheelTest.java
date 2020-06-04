package com.yiworld.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yiworld.common.datastruct.RingBufferWheel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RingBufferWheelTest {

    public static void main(String[] args) throws Exception {
        test7();
    }

    private static void test1() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        RingBufferWheel.Task task = new Task();
        task.setKey(10);
        RingBufferWheel wheel = new RingBufferWheel(executorService);
        wheel.addTask(task);

        task = new Task();
        task.setKey(74);
        wheel.addTask(task);

        while (true) {
            log.info("task size={}", wheel.taskSize());
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private static void test2() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        RingBufferWheel.Task task = new Task();
        task.setKey(10);
        RingBufferWheel wheel = new RingBufferWheel(executorService);
        wheel.addTask(task);

        task = new Task();
        task.setKey(74);
        wheel.addTask(task);

        wheel.start();
//        new Thread(() -> {
//            while (true){
//                log.info("task size={}" , wheel.taskSize());
//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
        TimeUnit.SECONDS.sleep(12);
        wheel.stop(true);
    }

    private static void test3() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        RingBufferWheel.Task task = new Task();
        task.setKey(10);
        RingBufferWheel wheel = new RingBufferWheel(executorService);
        wheel.addTask(task);
        task = new Task();
        task.setKey(60);
        wheel.addTask(task);

        TimeUnit.SECONDS.sleep(2);
        wheel.stop(false);
    }

    private static void test4() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        RingBufferWheel wheel = new RingBufferWheel(executorService);
        for (int i = 0; i < 65; i++) {
            RingBufferWheel.Task task = new Job(i);
            task.setKey(i);
            wheel.addTask(task);
        }
        wheel.start();
        log.info("task size={}", wheel.taskSize());
        wheel.stop(false);
    }

    private static void test5() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        RingBufferWheel wheel = new RingBufferWheel(executorService, 512);

        for (int i = 0; i < 65; i++) {
            RingBufferWheel.Task task = new Job(i);
            task.setKey(i);
            wheel.addTask(task);
        }
        log.info("task size={}", wheel.taskSize());
        wheel.stop(false);
    }

    private static void test6() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        RingBufferWheel wheel = new RingBufferWheel(executorService, 512);

        for (int i = 0; i < 10; i++) {
            RingBufferWheel.Task task = new Job(i);
            task.setKey(i);
            wheel.addTask(task);
        }

        TimeUnit.SECONDS.sleep(5);
        RingBufferWheel.Task task = new Job(15);
        task.setKey(15);
        wheel.addTask(task);

        log.info("task size={}", wheel.taskSize());

        wheel.stop(false);
    }

    private static void test7() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        RingBufferWheel wheel = new RingBufferWheel(executorService, 512);

        for (int i = 0; i < 10; i++) {
            RingBufferWheel.Task task = new Job(i);
            task.setKey(i);
            wheel.addTask(task);
        }

        RingBufferWheel.Task task = new Job(15);
        task.setKey(15);
        int cancel = wheel.addTask(task);

        new Thread(() -> {
            boolean flag = wheel.cancel(cancel);
            log.info("cancel task={}", flag);
        }).start();

        RingBufferWheel.Task task1 = new Job(20);
        task1.setKey(20);
        wheel.addTask(task1);

        log.info("task size={}", wheel.taskSize());

        wheel.stop(false);
    }

    private static void concurrentTest() throws Exception {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue(10);
        ThreadFactory product = new ThreadFactoryBuilder()
                .setNameFormat("msg-callback-%d")
                .setDaemon(true)
                .build();
        ThreadPoolExecutor business = new ThreadPoolExecutor(4, 4, 1, TimeUnit.MILLISECONDS, queue, product);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        RingBufferWheel wheel = new RingBufferWheel(executorService);

        for (int i = 0; i < 10; i++) {
            business.execute(new Runnable() {
                @Override
                public void run() {
                    for (int i1 = 0; i1 < 30; i1++) {
                        RingBufferWheel.Task task = new Job(i1);
                        task.setKey(i1);
                        wheel.addTask(task);
                    }
                }
            });
        }
        log.info("task size={}", wheel.taskSize());
        wheel.stop(false);
    }

    private static class Job extends RingBufferWheel.Task {
        private int num;
        public Job(int num) {
            this.num = num;
        }
        @Override
        public void run() {
            log.info("number={}", num);
        }
    }

    private static class Task extends RingBufferWheel.Task {
        @Override
        public void run() {
            log.info("================");
        }
    }


    public static void hashTimerTest() {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue(10);
        ThreadFactory product = new ThreadFactoryBuilder()
                .setNameFormat("msg-callback-%d")
                .setDaemon(true)
                .build();
        ThreadPoolExecutor business = new ThreadPoolExecutor(4, 4, 1, TimeUnit.MILLISECONDS, queue, product);
        HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            business.execute(new Runnable() {
                @Override
                public void run() {
                    for (int i1 = 0; i1 < 10; i1++) {
                        hashedWheelTimer.newTimeout(new TimerTask() {
                            @Override
                            public void run(Timeout timeout) throws Exception {
                                log.info("====" + finalI);
                            }
                        }, finalI, TimeUnit.SECONDS);
                    }
                }
            });
        }
    }
}