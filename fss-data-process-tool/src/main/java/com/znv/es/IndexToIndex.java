package com.znv.es;

import Write10000W.util.EsTransportClient;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by zhuhx on 2018/5/16.
 */
public class IndexToIndex {
    private static String sourceCluster = "lv130.dct-znv.com-es";
    private static String targetCluster = "znv-es";
    private static String sourceEsHosts = "lv130.dct-znv.com:9300,lv130.dct-znv.com:9301";
    private static String targetEsHosts = "lv91.dct-znv.com:9300,lv92.dct-znv.com:9300,lv93.dct-znv.com:9300";
    private static String sourceIndex = "linyang_history_fss_data_001";
    private static String targetIndex = "history_community_face_data-test10";
    private static String sourceType = "history_data";
    private static String targetType = "history_data";

    /**
     * 取数据
     */
    private static void esToes() {
        //建立连接
        TransportClient sourceClient = EsTransportClient.initClient(sourceCluster, sourceEsHosts);
        TransportClient targetClient = EsTransportClient.initClient(targetCluster, targetEsHosts);
        //拷贝少于1w的数据
//        searchThenBulk(sourceClient, targetClient);
        //拷贝大于1w的数据
        scrollThenBulk(sourceClient, targetClient);
    }

    /**
     * searchRequest获取数据，用于拷贝少于1w的数据
     *
     * @param sourceClient
     * @param targetClient
     */
    private static void searchThenBulk(TransportClient sourceClient, TransportClient targetClient) {
        //原集群查数据
        SearchRequestBuilder request = sourceClient.prepareSearch(sourceIndex).setTypes(sourceType).setFrom(0).setSize(10000);
        SearchResponse searchResponse = request.execute().actionGet();
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] searchHit = searchHits.getHits();

        //原集群查数据
        IndexRequestBuilder indexRequestBuilder = targetClient.prepareIndex(targetIndex, targetType);
        BulkRequestBuilder bulkRequestBuilder = targetClient.prepareBulk();
        try {
            for (SearchHit hit : searchHit) {
                Map<String, Object> source = hit.getSourceAsMap();//hit.getSource()
                bulkRequestBuilder.add(indexRequestBuilder.setSource(jsonBuilder().map(source)));
            }
            BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
            bulkResponse.buildFailureMessage();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭连接
            if (sourceClient != null) {
                EsTransportClient.closeClient(sourceClient);
            }
            if (targetClient != null) {
                EsTransportClient.closeClient(sourceClient);
            }
        }
    }

    /**
     * scroll获取数据，用于拷贝大于1w的数据
     *
     * @param sourceClient
     * @param targetClient
     */
    private static void scrollThenBulk(TransportClient sourceClient, TransportClient targetClient) {
        int scrollCount = 1;
        int total = 100000000;//写入的总数1亿数据
        int scrollSize = 10000;//scroll的大小
        SearchResponse scrollResp = sourceClient.prepareSearch(sourceIndex).setTypes(sourceType)
                .setScroll(new TimeValue(60000))
                .setSize(scrollSize).get(); //max of 100 hits will be returned for each scroll
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(System.currentTimeMillis()));
        IndexRequestBuilder indexRequestBuilder = targetClient.prepareIndex(targetIndex, targetType);
        try {
            //Scroll until no hits are returned
            do {
                scrollCount++;
                BulkRequestBuilder bulkRequestBuilder = targetClient.prepareBulk();
                for (SearchHit hit : scrollResp.getHits().getHits()) {
                    //Handle the hit...
                    Map<String, Object> source = hit.getSourceAsMap();
                    bulkRequestBuilder.add(indexRequestBuilder.setSource(jsonBuilder().map(source)));
                }
                bulkRequestBuilder.execute().actionGet();
                scrollResp = sourceClient.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
            }
            while (scrollResp.getHits().getHits().length != 0 );// Zero hits mark the end of the scroll and the while loop.

            System.out.println("ScrollCount:" + scrollCount);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            EsTransportClient.closeClient(sourceClient);
            EsTransportClient.closeClient(sourceClient);
        }
    }

    public static void main(String[] args) {
        // java 获取系统当前时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(System.currentTimeMillis()));
            //每次写入到ES集群中的数据量是一定的，所以看下循环多少次可以到1亿数据量
        esToes();
        System.out.println(df.format(System.currentTimeMillis()));
    }

}
