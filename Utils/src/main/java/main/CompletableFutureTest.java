package main;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CompletableFutureTest {
    private static Random rand = new Random();
    private static long t = System.currentTimeMillis();

    static int getMoreData() {
        System.out.println("begin to start compute");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("end to start compute. passed " + (System.currentTimeMillis() - t) / 1000 + " seconds");
        return rand.nextInt(1000);
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        CompletableFuture future = CompletableFuture.supplyAsync(CompletableFutureTest::getMoreData);
        Future<Integer> f = future.whenCompleteAsync((v, e) -> {
            System.out.println(v);
            System.out.println(e);
        });
        System.out.println(f.get());
        System.in.read();
    }
}
