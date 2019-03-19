package com.znv.kafka.ESClient;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.util.Map;

/**
 * Created by Administrator on 2017/2/15.
 */
public class EsClientFactory {
    private static TransportClient transportClient = null;

    /*
     * 创建传输客户端实例
     * @param clusterName es集群名称
     * @param transportHosts es集群主机列表
     */
    public static void init(Map<String, String> connectionParameters) {
        if (transportClient == null) {
            // on startup
            Settings settings = Settings.builder()
                    .put("cluster.name", EsConnectionParamUtil.getClusterName(connectionParameters)).build();
            transportClient = new PreBuiltTransportClient(settings)
                    .addTransportAddresses(EsConnectionParamUtil.getAddresses(connectionParameters));
        }
    }

    /*
     * 关闭传输客户端实例
     */
    public static void close() {
        transportClient.close();
        transportClient = null;
    }

    /*
     * 获取传输客户端实例
     * @return TransportClient
     */
    public static TransportClient getTransportClient() {
        return transportClient;
    }

}
