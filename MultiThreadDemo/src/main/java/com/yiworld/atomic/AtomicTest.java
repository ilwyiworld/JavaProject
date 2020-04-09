package com.yiworld.atomic;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicTest {
    public static void main(String[] args) throws InterruptedException {
        AtomicIntegerArray ATOMIC_INTEGER_ARRAY=new AtomicIntegerArray(new int[]{1,2,3,4,5});
        Thread []threads =new Thread[5];
        for(int i=0;i<threads.length;i++){
            final int index = i;
            final int threadNum = i;
            threads[i] = new Thread() {
                public void run() {
                    int result = ATOMIC_INTEGER_ARRAY.addAndGet(index, index + 1);
                    System.out.println("线程编号为：" + threadNum + " , 对应的原始值为：" + (index + 1) + "，增加后的结果为：" + result);
                }
            };
            threads[i].start();
        }
        for(Thread thread : threads) {
            thread.join();
        }
        System.out.println("=========================>\n执行已经完成，结果列表：");
        for(int i = 0 ; i < ATOMIC_INTEGER_ARRAY.length() ; i++) {
            System.out.println(ATOMIC_INTEGER_ARRAY.get(i));
        }
    }
}
