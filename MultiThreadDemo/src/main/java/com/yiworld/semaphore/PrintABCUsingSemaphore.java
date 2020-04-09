package com.yiworld.semaphore;

import java.util.concurrent.Semaphore;

/**
 * 获取和释放的许可证必须一致，acquire和release都是可以传入数值的来确定获取和释放的数量。
 * 如果我们获取和释放不一致，就会容易导致程序bug。当然也不是绝对，除非有特殊业务需求，否则都获取释放设置为一样的
 * 注意在初始化Semaphore的时候设置公平性，一般设置为true会比较合理。如果插队情况比较严重的话，某些线程可能一直阻塞
 * 获取和释放许可对线程并不要求，线程A获取了可以线程B释放。
 */
public class PrintABCUsingSemaphore {
    private int times;
    private Semaphore semaphoreA = new Semaphore(1);
    private Semaphore semaphoreB = new Semaphore(0);
    private Semaphore semaphoreC = new Semaphore(0);

    public PrintABCUsingSemaphore(int times) {
        this.times = times;
    }

    public static void main(String[] args) {
        PrintABCUsingSemaphore printABC = new PrintABCUsingSemaphore(10);
        new Thread(printABC::printA).start();
        new Thread(printABC::printB).start();
        new Thread(printABC::printC).start();
    }

    public void printA() {
        try {
            print("A", semaphoreA, semaphoreB);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void printB() {
        try {
            print("L", semaphoreB, semaphoreC);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void printC() {
        try {
            print("I", semaphoreC, semaphoreA);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void print(String name, Semaphore current, Semaphore next)
            throws InterruptedException {
        for (int i = 0; i < times; i++) {
            current.acquire();
            System.out.print(name);
            next.release();
        }
    }
}
