/*
// 编译成 Test.class 文件,存放于D盘根目录
public class Test {
    public void printTest() {
        System.out.println("Test类已成功加载运行！");
        ClassLoader classLoader = Test.class.getClassLoader();
        System.out.println("加载我的classLoader：" + classLoader);
        System.out.println("classLoader.parent：" + classLoader.getParent());
    }
}*/
