package org.elasticsearch.plugin.image.test;

import net.semanticmetadata.lire.utils.SerializationUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by Administrator on 2017/10/9.
 */
public class ShouldTest {
    private static TransportClient client = null;
    private static String clusterName130 = "lv102-elasticsearch";
    private static String transportHosts130 = "10.45.157.113:9300";
    private static String indexImage = "hy-es-image-plugin-test3should";// "hy-es-image-plugin-test1";
    private static String typeImage = "test";

    private static double drawNumber() {
        double u, v, s;
        do {
            u = Math.random() * 2 - 1;
            v = Math.random() * 2 - 1;
            s = u * u + v * v;
        } while (s == 0 || s >= 1);
        return u * Math.sqrt(-2d * Math.log(s) / s);
        // return Math.sqrt(-2d * Math.log(Math.random())) * Math.cos(2d * Math.PI * Math.random());
    }

    private static int[] generateHashes() {
        int[] result = new int[10];
        for (int k = 0; k < 10; k++) {
            result[k] = drawNumber() > 0d ? 1 : 0;
        }
        return result;
    }

    public static void createIndex() {
        try {

            for (int i = 0; i < 100; i++) {
                IndexResponse response = client.prepareIndex(indexImage, typeImage).setSource(jsonBuilder()
                        .startObject()
                        .field("hash", SerializationUtils.arrayToString(generateHashes()))
                        .field("uuid", i)
                        .endObject())
                        .get();
                if (!response.status().equals(RestStatus.CREATED)) {
                    System.out.println("index failed!! " + response.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void searchIndex() {
        String[] hashes = new String[10];
        int[] hashval = generateHashes();
        System.out.println("search hash val: " + SerializationUtils.arrayToString(hashval));
        for (int i = 0; i < 10; i++) {
            hashes[i] = Integer.toHexString(hashval[i]);
        }

        BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
        for (int i = 0; i < hashes.length; i++) {
            booleanQueryBuilder.should(QueryBuilders.termQuery("hash", hashes[i]));
        }

        SearchRequestBuilder srq = client.prepareSearch(indexImage).setTypes(typeImage);
        srq.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        srq.setQuery(booleanQueryBuilder);
        srq.setExplain(true);

        SearchResponse searchResponse = srq.execute().actionGet();
        SearchHits searchHits = searchResponse.getHits();

        long totalSize = searchHits.getTotalHits();
        System.out.println("totalCount " + totalSize);
        // System.out.println(sr.toString());
        for (SearchHit searchHit : searchHits) {
            System.out.println(searchHit.getSource());
        }
    }

    public static void main(String[] args) {

        client = ESUtils.getESTransportClient(clusterName130, transportHosts130);

        // createIndex();

        searchIndex();

        client.close();

    }
}
