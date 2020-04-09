package com.yiworld.lock;

public class Data {

    private final ReadWriteLock lock = new ReadWriteLock(); // 创建读写锁
    private final char[] buffer;

    public Data(int size) {
        this.buffer = new char[size];
        for (int i = 0; i < size; i++) {
            buffer[i] = '*';
        }
    }

    public String read() throws InterruptedException {
        lock.readLock(); // 读取上锁
        try {
            return doRead(); // 执行读取操作
        } finally {
            lock.readUnlock(); // 读取解锁
        }
    }

    public void write(char c) throws InterruptedException {
        lock.writeLock(); // 写入上锁
        try {
            doWrite(c); // 执行写入操作
        } finally {
            lock.writeUnlock(); // 写入解锁
        }
    }

    private String doRead() {
        StringBuilder result = new StringBuilder();
        for (char c : buffer) {
            result.append(c);
        }
        sleep(100);
        return result.toString();
    }

    private void doWrite(char c) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = c;
            sleep(100);
        }
    }
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
