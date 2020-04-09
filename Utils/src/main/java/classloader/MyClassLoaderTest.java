package classloader;

public class MyClassLoaderTest {
    public static void main(String[] args) throws Exception {
        // 指定类加载器加载调用
        MyClassLoader classLoader = new MyClassLoader();
        Object obj = classLoader.loadClass("Test").newInstance();
        obj.getClass().getMethod("printTest").invoke(obj);
    }
}