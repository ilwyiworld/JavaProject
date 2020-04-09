package com.yiworld.juc;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class ForkJoinCalculator extends RecursiveTask<Integer> {

    private static final long serialVersionUID = 7333472779649130114L;

    private static final int THRESHOLD = 10;
    private int start;
    private int end;

    public ForkJoinCalculator(int start, int end) {
        this.start = start;
        this.end = end;
    }


    @Override
    protected Integer compute() {
        int sum = 0;
        if ((end - start) < THRESHOLD) {    // 当问题分解到可求解程度时直接计算结果
            for (int i = start; i <= end; i++) {
                sum += i;
            }
        } else {
            int middle = (start + end) >>> 1;
            // 将任务一分为二
            ForkJoinCalculator left = new ForkJoinCalculator(start, middle);
            ForkJoinCalculator right = new ForkJoinCalculator(middle + 1, end);
            left.fork();
            right.fork();
            // 注意：由于此处是递归式的任务分解，也就意味着接下来会二分为四，四分为八...
            sum = left.join() + right.join();   // 合并两个子任务的结果
        }
        return sum;
    }

    public static void main(String[] args) throws Exception {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Future<Integer> result = forkJoinPool.submit(new ForkJoinCalculator(1, 10000));
        System.out.println(result.get());
    }
}
