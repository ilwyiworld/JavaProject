package com.yiworld;

public class DeadLock {
    static Object lock1 = new Object();
    static Object lock2 = new Object();

    public static class Task1 implements Runnable {

        @Override
        public void run() {
            synchronized (lock1) {
                System.out.println(Thread.currentThread().getName() + " 获得了第一把锁!!");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock2) {
                    System.out.println(Thread.currentThread().getName() + " 获得了第二把锁!!");
                }
            }
        }
    }

    public static class Task2 implements Runnable {

        @Override
        public void run() {
            synchronized (lock2) {
                System.out.println(Thread.currentThread().getName() + " 获得了第二把锁!!");
                synchronized (lock1) {
                    System.out.println(Thread.currentThread().getName() + " 获得了第一把锁!!");
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new Thread(new Task1(), "task-1");
        Thread thread2 = new Thread(new Task2(), "task-2");
        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
        System.out.println(Thread.currentThread().getName() + " 执行结束!");
    }

}


