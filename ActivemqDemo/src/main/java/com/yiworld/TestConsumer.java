package com.yiworld;

/**
 * Created by Administrator on 2017/12/18.
 */
public class TestConsumer {
    public static void main(String[] args){
        Consumer consumer = new Consumer();
        consumer.init();
        TestConsumer testConsumer = new TestConsumer();
        new Thread(testConsumer.new ConsumerMq(consumer)).start();
        new Thread(testConsumer.new ConsumerMq(consumer)).start();
        new Thread(testConsumer.new ConsumerMq(consumer)).start();
        new Thread(testConsumer.new ConsumerMq(consumer)).start();
    }

    private class ConsumerMq implements Runnable{
        Consumer consumer;
        public ConsumerMq(Consumer consumer){
            this.consumer = consumer;
        }

        @Override
        public void run() {
            while(true){
                try {
                    consumer.getMessage("Jaycekon-MQ");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
