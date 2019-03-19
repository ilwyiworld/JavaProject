package Zookeeper;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by Administrator on 2017/8/9.
 */
public class test {

    private ZkClient zkClient;
    private final String ZKServer = "106.14.193.13:2181";

    @Before
    public void createConnection() {
        /**
         * 创建会话
         * new SerializableSerializer() 创建序列化器接口，用来序列化和反序列化
         */
        zkClient = new ZkClient(ZKServer, 10000, 10000, new SerializableSerializer());
        System.out.println("conneted ok!");
    }

    @After
    public void closeConnection() {
        if (zkClient != null) {
            zkClient.close();
        }
    }

    @Test
    public void createNode() {
        User user = new User();
        user.setId(1);
        user.setName("testUser");
        /**
         * "/testUserNode" :节点的地址
         * user：数据的对象
         * CreateMode.PERSISTENT：创建的节点类型
         */
        String path = zkClient.create("/testUserNode3", user, CreateMode.PERSISTENT);
        //输出创建节点的路径
        System.out.println("created path:" + path);
    }

    @Test
    public void updateNode() {
        User user = new User();
        user.setId(2);
        user.setName("testUser2");
        /**
         * testUserNode 节点的路径
         * user 传入的数据对象
         */
        zkClient.writeData("/testUserNode", user);
    }

    @Test
    public void getNodeData() {
        Stat stat = new Stat();
        //获取 节点中的对象
        User user = zkClient.readData("/testUserNode", stat);
        System.out.println(user.getName());
        System.out.println(stat);
    }

    @Test
    public void isNodeExists() {
        boolean e = zkClient.exists("/testUserNode");
        System.out.println(e);
    }

    @Test
    public void deleteNode() {
        //删除单独一个节点，返回true表示成功
        boolean e1 = zkClient.delete("/testUserNode");
        //删除含有子节点的节点
        boolean e2 = zkClient.deleteRecursive("/test");
        //返回 true表示节点成功 ，false表示删除失败
        System.out.println(e1);
        System.out.println(e2);
    }

    private static class ZKChildListener implements IZkChildListener {
        /**
         * handleChildChange： 用来处理服务器端发送过来的通知
         * parentPath：对应的父节点的路径
         * currentChilds：子节点的相对路径
         */
        public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
            System.out.println(parentPath);
            System.out.println(currentChilds.toString());
        }
    }

    @Test
    public void SubscribeChildChanges() throws InterruptedException {
        /**
         * "/testUserNode" 监听的节点，可以是现在存在的也可以是不存在的
         */
        zkClient.subscribeChildChanges("/testUserNode3", new ZKChildListener());
        Thread.sleep(Integer.MAX_VALUE);
    }

    //订阅节点的数据内容的变化
    private static class ZKDataListener implements IZkDataListener {
        public void handleDataChange(String dataPath, Object data) throws Exception {
            System.out.println(dataPath + ":" + data.toString());
        }
        public void handleDataDeleted(String dataPath) throws Exception {
            System.out.println(dataPath);
        }
    }

    @Test
    public void SubscribeDataChanges () throws InterruptedException {
        zkClient.subscribeDataChanges("/testUserNode", new ZKDataListener());
        Thread.sleep(Integer.MAX_VALUE);
    }

}
