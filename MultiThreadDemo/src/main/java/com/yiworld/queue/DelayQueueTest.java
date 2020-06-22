package com.yiworld.queue;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayQueueTest {
    private static DelayQueue delayQueue = new DelayQueue();

    public static void main(String[] args) throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                delayQueue.offer(new MyDelayedTask("task1", 10000));
                delayQueue.offer(new MyDelayedTask("task2", 3900));
                delayQueue.offer(new MyDelayedTask("task3", 1900));
                delayQueue.offer(new MyDelayedTask("task4", 5900));
                delayQueue.offer(new MyDelayedTask("task5", 6900));
                delayQueue.offer(new MyDelayedTask("task6", 7900));
                delayQueue.offer(new MyDelayedTask("task7", 4900));
            }
        }).start();

        while (true) {
            Delayed take = delayQueue.take();
            System.out.println(take);
        }
    }
}

class MyDelayedTask implements Delayed {

    private String name;
    private long start = System.currentTimeMillis();
    private long time;

    public MyDelayedTask(String name, long time) {
        this.name = name;
        this.time = time;
    }

    // 需要实现的接口，获得延迟时间，用过期时间-当前时间
    // 元素进入队列后，先进行排序，只有 getDelay 也就是剩余时间为 0 的时候，该元素才有资格被消费者从队列中取出来，
    // 所以构造函数一般都有一个时间传入。
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert((start + time) - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    // 用于延迟队列内部比较排序，当前时间的延迟时间-比较对象的延迟时间
    // compareTo 方法必须提供与 getDelay 方法一致的排序，
    // 也就是说 compareTo 方法里可以按照 getDelay 方法的返回值大小排序，即在 compareTo 方法里比较 getDelay 方法返回值大小
    @Override
    public int compareTo(Delayed o) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public String toString() {
        return "MyDelayedTask{" +
                "name='" + name + '\'' +
                ", time=" + time +
                '}';
    }
}
