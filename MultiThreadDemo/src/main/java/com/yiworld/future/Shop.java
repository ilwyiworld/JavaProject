package com.yiworld.future;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Shop {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Shop shop=new Shop();
        System.out.println("begin");
        Future<Double> futurePrice = shop.getPriceAsync("ss");
        System.out.println("doSomething");
        System.out.println(futurePrice.get());
        System.out.println("end");
    }

    public double getPrice(String product) {
        //查询商品的数据库，或链接其他外部服务获取折扣
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Random().nextDouble() * product.charAt(0) + product.charAt(1);
    }

    public Future<Double> getPriceAsync(String product) {
        //创建CompletableFuture对象
        CompletableFuture<Double> futurePrice = new CompletableFuture<>();
        new Thread(() -> {
            try {
                //在另一个线程中执行计算
                double price = getPrice(product);
                //需要长时间计算的任务结束并得出结果时，设置future的返回值
                futurePrice.complete(price);
            } catch (Exception e) {
                //抛出异常
                futurePrice.completeExceptionally(e);
            }
        }).start();
        return futurePrice;
        //return CompletableFuture.supplyAsync(() -> getPrice(product));
    }
}