package com.yiworld.lock;

public class ReaderThread extends Thread{

    private final Data data;

    public ReaderThread(Data data) {
        this.data = data;
    }

    @Override
    public void run() {
        while (true) {
            String result = null;
            try {
                result = data.read();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " => " + result);
        }
    }
}
