package com.yiworld;

public class Main {
    public static void main(String args[]) throws InterruptedException {
        SynchronizedObject synchronizedObject = new SynchronizedObject();
        Thread thread = new MyThread(synchronizedObject);
        thread.start();
        Thread.sleep(500);
        thread.stop();
        System.out.println(synchronizedObject.getName() + " " + synchronizedObject.getPassword());
    }
}

class SynchronizedObject {
    private String name = "a";
    private String password = "aa";

    public synchronized void printString(String name, String password) {
        try {
            this.name = name;
            Thread.sleep(100000);
            this.password = password;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

class MyThread extends Thread {
    private SynchronizedObject synchronizedObject;

    public MyThread(SynchronizedObject synchronizedObject) {
        this.synchronizedObject = synchronizedObject;
    }

    public void run() {
        synchronizedObject.printString("b", "bb");
    }
}
