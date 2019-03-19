package com.znv.utils;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

//Created by Administrator on 2017/6/12.
//@RunWith(RandomizedRunner.class)
public class ESUtils {
    public static TransportClient getESTransportClient(String clusterName, String transportHosts) {
        TransportClient esTClient = null;
        try {
            // on startup
            System.out.println("start connect ES!");
            Settings settings = Settings.builder().put("cluster.name", clusterName).build();
            String hostlists[] = transportHosts.split(",");
            TransportAddress addresses[] = new TransportAddress[hostlists.length];
            int i = 0;
            for (String host : hostlists) {
                String inet[] = host.split(":");
                addresses[i++] = new InetSocketTransportAddress(InetAddress.getByName(inet[0]),//new InetSocketTransportAddress
                        Integer.parseInt(inet[1]));
            }
            esTClient = new PreBuiltTransportClient(settings).addTransportAddresses(addresses);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("ES连接成功！");
        return esTClient;
    }

}
