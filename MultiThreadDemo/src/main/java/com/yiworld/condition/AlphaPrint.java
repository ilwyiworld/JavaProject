package com.yiworld.condition;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 两个线程分别打印26个字母的元音（a,e,i,o,u） 和辅音（其他），要求按字母序输出
 */
public class AlphaPrint {
    public static void main(String[] args) {
        ReentrantLock lock = new ReentrantLock();
        Condition condition1 = lock.newCondition();
        Condition condition2 = lock.newCondition();

        new Thread(() -> {
            int i = 'a';
            while (i <= 'z') {
                lock.lock();
                int[] arr = {'a', 'e', 'i', 'o', 'u'};
                if (judge(arr, i)) {
                    try {
                        i++;
                        condition2.signal();
                        condition1.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }
                System.out.println("线程1：" + (char) i);
                i++;
            }
        }).start();

        new Thread(() -> {
            lock.lock();
            try {
                int[] arr = {'a', 'e', 'i', 'o', 'u'};
                for (int i : arr) {
                    System.out.println("线程2：" + (char) i);
                    condition1.signal();
                    condition2.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }).start();
    }

    private static boolean judge(int[] arr, int i) {
        for (int a : arr) {
            if (i == a) {
                return true;
            }
        }
        return false;
    }
}
