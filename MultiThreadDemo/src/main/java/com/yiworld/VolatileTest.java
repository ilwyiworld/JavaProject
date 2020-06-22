package com.yiworld;

public class VolatileTest {
    private static boolean flag = true;

    private static int i = 0;

    public static void main(String[] args) throws InterruptedException {
        new Thread(() -> {
            while (flag) {
                i++;
                // System.out.println("get flag:"+flag);
            }
            System.out.printf(Thread.currentThread().getName() + "跳出成功, i=%d \n", i);
        }).start();
        Thread.sleep(1);
        flag = false;
        System.out.printf("main thread 结束, i=%d \n", i);
    }
}