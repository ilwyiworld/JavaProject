package org.elasticsearch.plugin.image.test;

import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.image.FeatureQueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 2018/3/26.
 */
public class testPlugin {

    private static TransportClient searchClient = null;
    private static String clusterNameSearch = "face.dct-znv.com-es";//"lv94.dct-znv.com-es";// "lv102-elasticsearch";//
    private static String transportHostsSearch = "10.45.157.112:9300";

    public static void testFeature(List<byte[]> featureList) {
        FeatureQueryBuilder imageQueryBuilder1 = new FeatureQueryBuilder("rt_feature").coarseEncode(true).feature(featureList);
        String[] incs2 = {"rt_feature.feature_high"};
        SearchResponse sr2 = searchClient.prepareSearch("lopq-plugin-test-czl").setTypes("test").setQuery(imageQueryBuilder1)
                .setMinScore(0)
                .setFetchSource(incs2, null)
                //  .addDocValueField("feature."+ FeatureFieldMapper.FEATURE_LOW)
                // .addSort("rowkey", SortOrder.ASC) // .setExplain(true)
                .setFrom(0).setSize(1).get();

        System.out.println("totalCount " + sr2.getHits().getTotalHits() + ", costtime " + sr2.getTookInMillis() + "ms");

    }


    public static void main(String args[]) {
        searchClient = ESUtils.getESTransportClient(clusterNameSearch, transportHostsSearch);
        byte[] featureValue = new Base64().decode("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");
        List<byte[]> featureList = new ArrayList<>();
        featureList.add(featureValue);
        testFeature(featureList);
    }


}

