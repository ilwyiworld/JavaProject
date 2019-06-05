import org.junit.Test;

import java.util.concurrent.*;

public class TestCompletableFuture {
    @Test
    public void test(){
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<Double> future=executor.submit(new Callable<Double>(){
            public Double call(){
                //异步的方式执行耗时的操作
                return doSomeLongComputation();
            }
        });
        //异步时，做其他的事情
        doSomethingElse();
        try{
            //获取异步操作的结果，如果被阻塞，无法得到结果，那么最多等待1秒钟之后退出
            Double result = future.get(1, TimeUnit.SECONDS);
            System.out.print(result);
        } catch (InterruptedException e) {
            System.out.print("计算抛出一个异常");
        } catch (ExecutionException e) {
            System.out.print("当前线程在等待过程中被中断");
        } catch (TimeoutException e) {
            System.out.print("future对象完成之前已过期");
        }
    }

    private void doSomethingElse() {
        System.out.println("else");
    }

    private Double doSomeLongComputation() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 10d;
    }
}
