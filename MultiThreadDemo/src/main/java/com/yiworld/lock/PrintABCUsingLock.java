package com.yiworld.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 三个线程分别打印A，B，C，要求这三个线程一起运行，打印n次，输出形如“ABCABCABC....”的字符串。
 */
public class PrintABCUsingLock {
    private int times;
    private int state;
    private Lock lock=new ReentrantLock();

    public PrintABCUsingLock(int times) {
        this.times = times;
    }

    public static void main(String[] args) {
        PrintABCUsingLock printABC=new PrintABCUsingLock(10);
        new Thread(printABC::printA).start();
        new Thread(printABC::printB).start();
        new Thread(printABC::printC).start();
    }

    public void printA() {
        print("A", 0);
    }

    public void printB() {
        print("B", 1);
    }

    public void printC() {
        print("C", 2);
    }
    private void print(String name,int targetState){
        for(int i=0;i<times;){
            lock.lock();
            if(state % 3 == targetState){
                state++;
                i++;
                System.out.println(Thread.currentThread().getName()+" "+name);
            }
            lock.unlock();
        }
    }
}
