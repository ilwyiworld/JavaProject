package proxy;

public class Test {
    public static void main(String[] args) {
        /*Star huangBo = new Star("HuangBo");
        AgentHandler proxyHandler = new AgentHandler(huangBo);
        IMovieStar agent = (IMovieStar) proxyHandler.getProxy();
        agent.movieShow(1000000000);
        agent.tvShow(100);

        ISingerStar singerAgent = (ISingerStar) proxyHandler.getProxy();
        singerAgent.sing(1024);*/

        Star realStar = new Star();
        // 有参构造方法
        Star proxy1 = (Star) new CglibProxyHandler().getProxyInstance(realStar,"huangbo");
        // 无参构造方法
        Star proxy2 = (Star) new CglibProxyHandler().getProxyInstance(realStar);
        proxy1.sing(3);
        proxy2.sing(3);
    }
}
