package org.elasticsearch.plugin.image.test;

import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.util.FeatureCompUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/28.
 */
public class SaveFeature {
    private static TransportClient client = null;
    private static String clusterName130 = "lv130.dct-znv.com-es";//"lv102-elasticsearch";//
    private static String transportHosts130 = "10.45.157.130:9300";
    private static String indexImage = "person_list_data_index_yinchuan";//"hy-pca-test4dim16temp";//"z-es-image-plugin-16single_5";// "history_fss_data_v113-000001";//
    private static String typeImage = "person_list";

    private static void writeTxtLine(FileWriter out, float[] floatFeature) {
        try {
            for (int i = 0; i < floatFeature.length - 1; i++) {
                out.write(floatFeature[i] + " ");
            }
            out.write(floatFeature[(floatFeature.length - 1)] + "\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveFeatureAsTxt() throws IOException {
        File file = new File("E:\\project_test\\python\\pca\\pca_yinchuan_500000.txt");  //存放数组数据的文件
        FileWriter out = new FileWriter(file);  //文件写入流

        SearchResponse scrollResp = client.prepareSearch(indexImage)
                .setFetchSource("feature", null)
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                // .setQuery(qb)
                .setSize(1000).get(); //max of 100 hits will be returned for each scroll
//Scroll until no hits are returned

        Base64 base64 = new Base64();
        FeatureCompUtil fc = new FeatureCompUtil();
        int count = 0;
        do {
            count += scrollResp.getHits().internalHits().length;
            for (SearchHit hit : scrollResp.getHits().getHits()) {

                //Handle the hit...
                //   System.out.println(hit.getSourceAsString());
                Map<String, Object> source = hit.getSource();
                if (source.get("feature") != null) {
                    float[] f = fc.getFloatArray(base64.decode(source.get("feature").toString()));
                    writeTxtLine(out, f);
                }
            }
            if (count > 510000) //500000
                break;//hy test

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.

        out.close();
    }

    public static void main(String[] args) {
        try {
            client = ESUtils.getESTransportClient(clusterName130, transportHosts130);

            saveFeatureAsTxt();

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
