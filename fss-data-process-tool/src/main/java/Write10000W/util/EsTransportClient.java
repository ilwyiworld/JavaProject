package Write10000W.util;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * Created by zhuhx on 2018/2/5.
 */
public class EsTransportClient {
    protected static final Logger LOG = LogManager.getLogger(EsTransportClient.class);

    /**
     * 创建传输客户端实例
     *
     * @param clusterName    集群名称
     * @param transportHosts es集群主机列表
     */
    public static TransportClient initClient(String clusterName, String transportHosts) {
        TransportClient transportClient = null;
        try {
            Settings settings = Settings.builder().put("cluster.name", clusterName).build();
            String[] hostLists = transportHosts.split(",");
            TransportAddress[] address = new TransportAddress[hostLists.length];
            int i = 0;
            for (String host : hostLists) {
                String[] inet = host.split(":");
                address[i++] = new InetSocketTransportAddress(InetAddress.getByName(inet[0]),//new InetSocketTransportAddress
                        Integer.parseInt(inet[1]));
            }
            transportClient = new PreBuiltTransportClient(settings).addTransportAddresses(address);
            System.out.println("TransportClient连接成功！");
        } catch (UnknownHostException e) {
            System.out.println("TransportClient连接异常！");
            e.printStackTrace();
            LOG.error(e);
        }
        return transportClient;
    }

    /**
     * 关闭客户端连接
     */
    public static void closeClient(TransportClient transportClient) {
        transportClient.close();
    }

}
