package proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AgentHandler implements InvocationHandler {
    //被代理对象
    private Object mTarget;

    public AgentHandler(Object target) {
        this.mTarget = target;
    }

    /**
     * 方法拦截，可以进行一些额外操作
     * @param proxy
     * @param method 拦截的方法
     * @param args 方法对应的参数
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (methodName.equals("movieShow") || methodName.equals("tvShow")) {
            if (args[0] instanceof Integer && ((int) args[0]) < 300000000) {
                System.out.println(((int) args[0]) + "块钱？！你雇 HuangZiTao 演去吧！");
                return null;
            }
        }
        Object result = method.invoke(mTarget, args);

        return result;
    }

    /**
     * 获取代理
     * @return
     */
    public Object getProxy() {
        return Proxy.newProxyInstance(mTarget.getClass().getClassLoader(), mTarget.getClass().getInterfaces(), this);
    }
}
