import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class Test {
    @org.junit.Test
    public void test() {
        Map<String,Integer> comMap=new HashMap<>();
        comMap.put("key1",1);
        comMap.put("key2",1);
        comMap.put("key3",0);
        Map<String,Integer> map1=comMap.entrySet().stream().filter(e->{return e.getValue()==1;}).collect(Collectors.toMap(
                (e) -> (String) e.getKey(),
                (e) -> e.getValue()
        ));
        System.out.println(map1.entrySet());
    }

    @org.junit.Test
    public void test2(){
        List<Shop> shops = Arrays.asList(
                new Shop("one"),
                new Shop("two"),
                new Shop("three"),
                new Shop("four"));
        long start = System.nanoTime();
        List<String> str = shops.stream().map(shop -> String.format("%s price: %.2f", shop.getName(), shop.getPrice(shop.getName()))).collect(toList());
        System.out.println(str);
        long end = System.nanoTime();
        System.out.println((end - start) / 1000000);

        //并行流
        long start2 = System.nanoTime();
        List<String> str2 = shops.parallelStream().map(shop -> String.format("%s price: %.2f", shop.getName(), shop.getPrice(shop.getName()))).collect(toList());
        System.out.println(str2);
        long end2 = System.nanoTime();
        System.out.println((end2 - start2) / 1000000);

        long start3 = System.nanoTime();
        List<CompletableFuture<String>> str3 = shops.stream().map(shop->
                CompletableFuture.supplyAsync(
                        ()->String.format("%s price: %.2f", shop.getName(), shop.getPrice(shop.getName())))).collect(toList());
        List<String> str4 =str3.stream().map(CompletableFuture::join).collect(toList());
        System.out.println(str4);
        long end3 = System.nanoTime();
        System.out.println((end3 - start3) / 1000000);

        System.out.println(Runtime.getRuntime().availableProcessors());
    }

}
