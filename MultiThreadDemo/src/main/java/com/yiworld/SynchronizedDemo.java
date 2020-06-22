package com.yiworld;

public class SynchronizedDemo {
    public static void main(String[] args) {
        synchronized (SynchronizedDemo.class) {
        }
        method();
    }

    public synchronized static void method() {
    }
}