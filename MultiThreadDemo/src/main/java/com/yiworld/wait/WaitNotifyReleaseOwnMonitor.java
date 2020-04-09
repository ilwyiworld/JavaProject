package com.yiworld.wait;

public class WaitNotifyReleaseOwnMonitor {
    private static final Object objectOne = new Object();
    private static final Object objectTwo = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread threadOne = new Thread(() -> {
            synchronized (objectOne) {
                System.out.println(Thread.currentThread().getName() + " got objectOne lock ");
                synchronized (objectTwo) {
                    System.out.println(Thread.currentThread().getName() + " got objectTwo lock ");

                    try {
                        System.out.println(Thread.currentThread().getName() + " release objectOne lock ");
                        objectOne.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread threadTwo = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (objectOne) {
                System.out.println(Thread.currentThread().getName() + " got lock objectOne");
                synchronized (objectTwo) {
                    System.out.println(Thread.currentThread().getName() + " got lock objectTwo");
                }
            }
        });

        threadOne.start();
        threadTwo.start();
    }
}