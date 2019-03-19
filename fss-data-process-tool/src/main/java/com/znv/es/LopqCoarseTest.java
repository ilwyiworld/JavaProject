package com.znv.es;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import com.znv.utils.*;

/**
 * 直接写入1条数据到ES集群中
 */
public class LopqCoarseTest {
    private static TransportClient sourceClient = null;
    private static TransportClient searchClient = null;
    private static String clusterNameSearch = "myesdb";// "lv102-elasticsearch";//
    private static String transportHostsSearch = "10.45.157.91:9300";
    private static String indexImageSearch = "person_list_data_test";
    private static String typeImageSearch = "person_list";
    private static String clusterNameSource = "lv130.dct-znv.com-es";
    private static String transportHostsSource = "10.45.157.130:9300";
    public static Logger log = LogManager.getLogger(LopqCoarseTest.class);
    public static AtomicLong numCount = new AtomicLong(0);

    public static void createIndex(TransportClient esTClient, String index, String type) {
        IndicesExistsResponse indicesExistsResponse = esTClient.admin().indices().prepareExists(index).get();
        if (indicesExistsResponse.isExists()) {
            return;
        }
        CreateIndexResponse response = esTClient.admin().indices().prepareCreate(index)
                .get();
        if (!response.isAcknowledged()) {
            System.out.println("create index failed!! \n" + response);
        } else {
            System.out.println("create index success!!!");
        }
    }

    //转换"2017-07-07T02:44:58.000Z"类型的时间，并加8小时
    public static String formatUtcDate(String utcDateString) {
        try {
            Date date1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'").parse(utcDateString);
            Calendar ca = Calendar.getInstance();
            ca.setTime(date1);
            ca.add(Calendar.HOUR_OF_DAY, 8);
            utcDateString = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(ca.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return utcDateString;
    }

    public static void testWriteToIndex(String targetIndex, String type, long importNums) {
        long count = 0;
        createIndex(searchClient, targetIndex, type);
        if (importNums < 1000) {
            System.out.println("测试导入数量太少");
        }
        GetResponse response = sourceClient.prepareGet("person_list_data_index_yinchuan_copy", "person_list", "41510730621911454").get();
        Map<String, Object> map = response.getSourceAsMap();
        for (int i = 0; i < Math.ceil((float) importNums / 1000); i++) {
            BulkRequestBuilder bulkRequest = searchClient.prepareBulk();
            IndexRequestBuilder indexerbuilder = searchClient.prepareIndex(indexImageSearch, typeImageSearch);

            for (int j = 0; j < 1000; j++) {
                bulkRequest.add(indexerbuilder.setSource(map));
            }
            bulkRequest.get();
            count += 1000;
            if (count % 100000 == 0) {
                log.info("write 10w data!!!");
            }
        }
    }

    private static void writeIndex(String clusterName, String transportHosts, String index, String type, int count) {
        String sourceIndex = "person_list_data_index_yinchuan_copy";
        createIndex(searchClient, index, type);
        SearchResponse scrollResp = sourceClient.prepareSearch(sourceIndex).setFetchSource(null, "rt_image")
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)//addSort("enter_time", SortOrder.ASC)根据"enter_time"字段拉取数据
                .setScroll(new TimeValue(60000))
                // .setQuery(qb)
                .setSize(1000).get(); // max of 100 hits will be returned for each scroll
        Base64 base64 = new Base64();
        do {
            BulkRequestBuilder bulkRequest = searchClient.prepareBulk();
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                IndexRequestBuilder indexerbuilder = searchClient.prepareIndex(indexImageSearch, typeImageSearch);
                Map<String, Object> source = hit.getSourceAsMap();//hit.getSource()
                log.info(source);
                bulkRequest.add(indexerbuilder.setSource(source));
            }
            bulkRequest.get();
            System.out.println("begin to write data!!!");
            long nums = numCount.addAndGet(1000);
            if (nums >= 1e8) {
                log.error("write 1ww data over ! date : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                return;
            }
            // break;//hy test
            scrollResp = sourceClient.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
                    .actionGet();
        } while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the
    }

    public static void main(String[] args) {
        sourceClient = ESUtils.getESTransportClient(clusterNameSource, transportHostsSource);
        searchClient = ESUtils.getESTransportClient(clusterNameSearch, transportHostsSearch);
        log.info("start import...");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(System.currentTimeMillis()));
        testWriteToIndex("person_list_data_test", "person_list", 5000000);
        String feature1 = "7qpXQsFdAAAAAQAA+sWovVs40bsb1Ck9Xm9/u2g4g72RkD483TUwPXDbdzwuSa694f49PcYhlTy5i6e8fmLjPZLdvbwwq9y8BEIsPKQnIj0aybM8cjCkOinMAj44YOy9GvBuvfExL736uRq9CAaRvacPkj1Us4s8VIZlveujqDwsB149pr2HPOliLj25E427uoDwPBbDgT0IpGE80J/oPKN0Hj6kTcy8gvGqvM3WGL1o5pk8YjsEvsWikr3Sgs858f3+OmWzATyRjlA966/9OTBrlbvXu2E9GZoaPbyrWT3B9Ce9vQ+du6NkgLux9wc9buC4PTMiu7u223+91VwNPnGsjr03/6O8z2nUPOg6mD22m7i8UVjLPVR0Ob3R7Ui7gE0rPObNkr0xCDI9BPSbvXGTtDzCibW97xYtPftmD71NQLm7ridLPWU8QTy6bQA7krQEvYBXWj1eTN+9Vf2uPVdGOrsPC0q9dR6VPQUYtrxwBtk9aE6kvTLxnzw9Lpk9dFX+vHhc97w2Drk9lG3Gu6Wo272leRu+XZFyvPna1734qNa8mFRWPecZLj3/RQQ+W7p3PSxfbryhHgW8Q3ZrPHwJ6TzWlSS9arXovVcr870SuqY7LI8DveetoL0lFni9OBR6vVFlEDoMHiY86tcsvYCQfjxnk0I9846+vJkCbT17OjC9HRt7PbRfTz1GJ7i84puVvZxG/L3rZGk7M8JSPSJXRr3cwpg9mEuWPfv5072qG788kBfHPGD6h72dHoI8j3L7PAYtb73ABz08Z+bQuf7UeD3CeiI97yEPPeO82DxxkAS+U807vZo1hT0wH/a8m6sBPnQRIL3YpRU9ZvXRuhJSi72ZM607Aui5u40T7bxyQSQ9HOvivQGIC70y03o837oNvn9LN73lmUS9Nku+PU45bz2pVCY9GdgFvdZMFD3ydAC921FSvX2MDz5gfJC91E48vRz85b0rd2Q9OCCLO8iOkr3YV3W8/NjcPau5D72dSrE9F8KLvbnL2juf/E+9EAauvMBkmb2FxeA9oB2vvDM+FLr0Njk7dNeIvHn6tLwSUR48AzPtvR2U0bzPZf09l0HuPQH1djsOZS88qWGTvSrakTziqo+91628vXf8ITtJw3O7oQqJvR/trD1DMpY956fOvaa8AbySpBe99NKnvS3oxz0ioMI8wgGtPUugarvZrri9h9jVPJNRtL3di2M9h7wfvfGVQz6dgJG8ydyzueFYIrps8J+8+/5+vKNqi73I/SC9QQZtPR5Prb3X88Y9OjGCPH5rU72XJ0g9gzMBPb2tgD323Kq9l29VPapREb0o4gO+5L82vmEWEDxJeJm9kQnJPIMS2DyhOOE8WcdkPbgjKDyABYA9RTqnPQ==";
        String feature2 = "7qpXQsFdAAAAAQAApDpvvQEtxTt+Uiw+U9fpPUVl+TzICq66Set5vRAmAT0S3AQ91euLvaT6OLzUYaW9oBubvIYpYz3/ggc9oJKePA/MdzyxOgq+aC+ePSdDeDsgZ3U8PC08PVWEBDwJZry7TWiQPXR3l71ILpi73LR5PdunMT0eSmY9cEswvFQ8nL1/Xf68lHvnPfyTiT1c/ku9Do2GPYWLzr3Hbry9YcK0vHIXvb00+ok90uGdvIp8xT36pUW9W24cPYTOZLx7Sh29erGxPTiwyzxCyD29qgqgPAKPSb6TUaY9RP2ePakrCT0h64q8YPwCPqPhe73Q3Aa9iNOSPNROMr1TSwI8y/YOvYFbb7yx/dg9nVrCPCM4vTyf2iO9GiUePVgwSbxJDGo9pGXnPSol8jt96HA9rVfRO1VjeD0fXCO8NReLPRrO4T13P2W92QgFO44QwD0Wf6g92hQRugq+Gr04XTS9lKIpPQt5tL2RNsW8pl6aOyCJ3D2XGfE8kuu9PTcKAD3oapE9Oa8RPL8BgT075Fs9OgCePAovkT0dA1O9+bCru5Ov6j0SwiC9v53gvLQIXz2zkHo96JWGvN4fyj068Tg94CM/PZ8P5zsPnAK96XMFPh+BBr12u9y8GYIPPdZClb0FVUG9D8QnPTMogDxq6kO9KXdaPTyGbDy7d9e9V1e2vB8gwz1Tste9OuEnvF8VQj2LqsQ91qejOeog4b31tvG9DE+LvSVBXD1yq229M4QTPQFUl7wPkMK8gXGnPOedQTzkeeA9f+k+vZCkiLyj1Gy9UIjnvNeHXr3Ua6k7UZcGPraWib3nrhQ6dZ6fPFjnWTxYpX48hi7+PCXPSL02L6W8XvvGPOsAST0LXly8cs6HPYBdHz5DSag50fkpPRuGxT0bOau9jSCnPUrIwj2KQrk9otlpvTSwgz2xyr28Aq0LPTlDPL3KPkG9mtEyPX2XGD4uDnE9BUZaPT3Zlryvjy09F+lovXRZpL1EQlo9cyzVvf91Njw04LC87MxmPQsn+Tx+Y4c9vBUYvHzkU73MqQ89t26RPATWqT1XEAO8imH7vFIuqjt1zlg9XFiWvei9sjx5p8G9NKtDPTyJJDxnBMw90YUpO+hsyDywo3e7nAqPvZqJMD3rrJq99Dpxu0TULD3F4u29Phe+PMlpOr2VdRq8LaBNuw0RGb3D/h+9eliKvcvTTL03swC9FyZSvYrUoL3jVE88nnedOih+IL5pDka952MlPVESDz1P55Y7OPICPheyKr2d1hQ+jJuVPTXO0L2DRQg96EKlvLWtvzxxFZ29Q4/YPROFI74/I0Y9EtWnPICYcz23S644rPvtPDcfgTxeIrM7j3W+u5Ebh7s3t4C9rGSsvA==";
        System.out.println(df.format(System.currentTimeMillis()));
        sourceClient.close();
        searchClient.close();
    }
}
