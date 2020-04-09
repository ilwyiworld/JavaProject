package com.yiworld.wait;

/**
 * 两个线程交替打印0-100
 */
public class OddEvenBySync {
    /*
     两个线程
        1. 一个处理偶数(Even)，另一个处理奇数(Odd) 用位运算来实现判断
        2. 用synchronized 来实现
     */

    private static volatile int count = 0;
    private static final Object lock = new Object();

    public static void main(String[] args) {

        Thread threadEven = new Thread(() -> {
            while (count < 100) {
                synchronized (lock) {
                    // if (count % 2 == 0) {
                    if (0 == (count & 1)) {
                        System.out.println(Thread.currentThread().getName() + ": " + count++);
                    }
                }
            }
        }, "thread-even");

        Thread threadOdd = new Thread(() -> {
            while (count < 100) {
                synchronized (lock) {
                    // if (count % 2 == 0) {
                    if (1 == (count & 1)) {
                        System.out.println(Thread.currentThread().getName() + ": " + count++);
                    }
                }
            }
        }, "thread-odd");

        threadEven.start();
        threadOdd.start();
    }
}