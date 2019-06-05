import lombok.Data;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static java.util.stream.Collectors.toList;

@Data
public class Shop {

    private String name;

    public Shop(String name){
        this.name=name;
    }

    public double getPrice(String product) {
        //查询商品的数据库，或链接其他外部服务获取折扣
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Random().nextDouble() * product.charAt(0) + product.charAt(1);
    }

    public Future<Double> getPriceAsync(String product){
        //创建CompletableFuture对象
        CompletableFuture<Double> futurePrice = new CompletableFuture<>();

        new Thread (()->{
            try {
                //在另一个线程中执行计算
                double price = getPrice(product);
                //需要长时间计算的任务结束并得出结果时，设置future的返回值
                futurePrice.complete(price);
            } catch (Exception e) {
                futurePrice.completeExceptionally(e);
            }
        }).start();
        return futurePrice;
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {
        System.out.println("begin");
        Future<Double> futurePrice = getPriceAsync("ss");
        System.out.println("doSomething");
        try {
            System.out.println(futurePrice.get(2, TimeUnit.SECONDS));
        } catch (TimeoutException e) {
            System.out.print(e);
        }
        System.out.println("end");
    }

}
