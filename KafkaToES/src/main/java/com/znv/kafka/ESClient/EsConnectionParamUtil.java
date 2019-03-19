package com.znv.kafka.ESClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Map;

/**
 * Created by Administrator on 2017/4/12.
 */
public final class EsConnectionParamUtil {
    private static final Log LOG = LogFactory.getLog(EsConnectionParamUtil.class);

    private EsConnectionParamUtil() {
        // prevent construction, utility class
    }

    public static String getClusterName(Map<String, String> connectionParameters) {
        return connectionParameters.get(EsConnectionParams.CLUSTER_NAME);
    }

    public static TransportAddress[] getAddresses(Map<String, String> connectionParameters) {
        String hostlists[] = connectionParameters.get(EsConnectionParams.HOSTS).split(",");
        TransportAddress addresses[] = new TransportAddress[hostlists.length];
        try {
            int i = 0;
            for (String host : hostlists) {
                String inet[] = host.split(":");
                addresses[i++] = new InetSocketTransportAddress(InetAddress.getByName(inet[0]),
                        Integer.parseInt(inet[1]));
            }
        } catch (UnknownHostException e) {
            LOG.error("The es transport address is error. ", e);
        }

        return addresses;
    }

}
