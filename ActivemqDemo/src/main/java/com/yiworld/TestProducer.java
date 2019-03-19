package com.yiworld;

/**
 * Created by Administrator on 2017/12/18.
 */
public class TestProducer {
    public static void main(String[] args){
        Producer producer = new Producer();
        producer.init();
        TestProducer testProducer = new TestProducer();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Thread 1
        new Thread(testProducer.new ProductorMq(producer)).start();
        //Thread 2
        new Thread(testProducer.new ProductorMq(producer)).start();
        //Thread 3
        new Thread(testProducer.new ProductorMq(producer)).start();
        //Thread 4
        new Thread(testProducer.new ProductorMq(producer)).start();
        //Thread 5
        new Thread(testProducer.new ProductorMq(producer)).start();
    }

    private class ProductorMq implements Runnable{
        Producer producer;
        public ProductorMq(Producer producer){
            this.producer = producer;
        }

        @Override
        public void run() {
            while(true){
                try {
                    producer.sendMessage("Jaycekon-MQ");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
