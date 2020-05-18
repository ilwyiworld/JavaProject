package main;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LambdaTest {
    public static void main(String[] args) {
        // 静态方法引用--通过类名调用
        Consumer<String> consumerStatic = Test::MyNameStatic;
        consumerStatic.accept("3y---static");

        //实例方法引用--通过实例调用
        Test test = new Test();
        Consumer<String> consumer = test::myName;
        consumer.accept("3y---instance");

        // 构造方法方法引用--无参数
        Supplier<Test> supplier = Test::new;
        System.out.println(supplier.get());
    }
}

class Test {
    // 静态方法
    public static void MyNameStatic(String name) {
        System.out.println(name);
    }

    // 实例方法
    public void myName(String name) {
        System.out.println(name);
    }

    // 无参构造方法
    public Test() {
    }
}
