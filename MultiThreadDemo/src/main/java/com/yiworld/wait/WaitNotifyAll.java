package com.yiworld.wait;

public class WaitNotifyAll implements Runnable {

    private static final Object objectOne = new Object();

    @Override
    public void run() {
        synchronized (objectOne) {
            Thread currentThread = Thread.currentThread();
            System.out.println(currentThread.getName() + " in run before wait, state is " + currentThread.getState());
            try {
                objectOne.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(currentThread.getName() + " in run after wait, state is " + currentThread.getState());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        WaitNotifyAll waitNotifyAll = new WaitNotifyAll();
        Thread threadOne = new Thread(waitNotifyAll,"thread-one");
        Thread threadTwo = new Thread(waitNotifyAll,"thread-two");
        Thread threadThree = new Thread(() -> {
            synchronized (objectOne) {
                Thread currentThread = Thread.currentThread();
                System.out.println(currentThread.getName() + " in run before notifyAll, state is " + currentThread.getState());
                objectOne.notifyAll();
                System.out.println(currentThread.getName() + " in run after notifyAll, state is " + currentThread.getState());

            }
        }, "thread-three");
        threadOne.start();
        threadTwo.start();
        Thread.sleep(200);
        threadThree.start();
    }
}