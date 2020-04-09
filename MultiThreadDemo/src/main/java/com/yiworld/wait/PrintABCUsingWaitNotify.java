package com.yiworld.wait;

public class PrintABCUsingWaitNotify {
    private int times;
    private int state;
    private static final Object LOCK = new Object();

    public PrintABCUsingWaitNotify(int times) {
        this.times = times;
    }

    public static void main(String[] args) {
        PrintABCUsingWaitNotify printABC=new PrintABCUsingWaitNotify(10);
        new Thread(printABC::printA).start();
        new Thread(printABC::printB).start();
        new Thread(printABC::printC).start();
    }

    public void printA() {
        try {
            print("A", 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void printB() {
        try {
            print("B", 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void printC() {
        try {
            print("C", 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void print(String name,int targetState) throws InterruptedException{
        for(int i=0;i<times;i++){
            synchronized (LOCK){
                while(state%3!=targetState){
                    LOCK.wait();
                }
                state++;
                System.out.println(Thread.currentThread().getName()+" "+name);
                LOCK.notifyAll();
            }
        }
    }
}
