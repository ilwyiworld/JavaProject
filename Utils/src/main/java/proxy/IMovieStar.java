package proxy;

public interface IMovieStar {
    /**
     * 演电影
     * @param money 演电影的片酬,以 int 为单位就够了，除了星爷，没听说谁的片酬能上亿
     */
    void movieShow(int money);

    /**
     * 演电视剧
     * @param money 演电视剧的片酬
     */
    void tvShow(int money);
}
