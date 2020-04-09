package proxy;

public class Star implements IMovieStar, ISingerStar {
    private String mName;

    public Star() {
    }

    public Star(String name) {
        mName = name;
    }
    @Override
    public void movieShow(int money) {
        System.out.println(mName + " 出演了部片酬 " + money + " 元的电影");
    }
    @Override
    public void tvShow(int money) {
        System.out.println(mName + " 出演了部片酬 " + money + " 元的电视剧");
    }
    @Override
    public void sing(int number) {
        System.out.println(mName + " 唱了 " + number + " 首歌");
    }
}