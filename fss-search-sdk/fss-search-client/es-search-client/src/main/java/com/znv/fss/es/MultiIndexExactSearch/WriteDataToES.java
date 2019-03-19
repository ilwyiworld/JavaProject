package com.znv.fss.es.MultiIndexExactSearch;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.es.EsConfig;
import com.znv.fss.es.EsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.znv.fss.es.FormatObject.formatTime;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


public class WriteDataToES {
    protected static final Logger LOG = LogManager.getLogger(WriteDataToES.class);
    private static TransportClient client = null;

    public void initClient(String clusterName, String ip) {
        try {
            // on startup
            Settings settings = Settings.builder()
                    .put("cluster.name", clusterName).build();
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), 9300));
        } catch (UnknownHostException e) {
            LOG.error("es创建client异常", e);
        }
    }


    public void closeClient() {
        if (client != null) {
            client.close();
        }
    }

    public void writeData(String index, String type, String docId, JSONObject json) {
        String imgUrl = "";
        String bigPictureUuid = "";
        String score = "";
        if (json.containsKey("img_url")) {
            imgUrl = json.getString("img_url");
        }
        if (json.containsKey("big_picture_uuid")) {
            bigPictureUuid = json.getString("big_picture_uuid");
        }
        try {
            if (client == null) {
                initClient(EsManager.esClusterName, EsManager.esIp);
            } else {
                BulkRequestBuilder bulkRequest = client.prepareBulk();
                bulkRequest.add(client.prepareIndex(index, type, String.valueOf(docId)).setSource(jsonBuilder()
                                .startObject()
                                .field("img_url", imgUrl)
                                .field("big_picture_uuid", bigPictureUuid)
                                .endObject()
                        )
                );
                BulkResponse bulkResponse = bulkRequest.get();
                if (bulkResponse.hasFailures()) {
                    // process failures by iterating through each bulk response item
                   // System.out.println(bulkRequest.toString());
					LOG.error(bulkRequest.toString());
                }
            }
        } catch (IOException e) {
            LOG.error("es写数据异常", e);
        }
    }
//index:要写数据的索引名； type:要写数据的type； esHits：要写的数据； eventId：事务id，由web发送，标志一次查询； searchNum：查询索引的顺序号；indexName：查询索引的名称
    public  void bulkWriteToEs(String index, String type, JSONArray esHits,String eventId,int searchNum,String indexName) {
        try {
            FeatureCompUtil fc = new FeatureCompUtil();
            fc.setFeaturePoints(EsConfig.getFeaturePoints());
            int len = esHits.size();
          //  if(len > 0){ //已在调用函数中做了判断1

            if (client == null) {
                initClient(EsManager.esClusterName, EsManager.esIp);
            }else {
                BulkRequestBuilder bulkRequest = client.prepareBulk();
                int statusCode = 0;
                for (int i = 0; i < len; i++) {
                    JSONObject hit = esHits.getJSONObject(i);
                    float score = fc.Normalize(hit.getFloatValue("_score"));
                   // System.out.println("未归一化之前的score："+hit.getFloatValue("_score"));
                    //System.out.println("归一化之后的score："+ score);
                    JSONObject source = hit.getJSONObject("_source");
                    String enterTime = source.getString("enter_time");
                    String uuid = source.getString("uuid");
                    String docId =formatTime(enterTime) + uuid;
                    if (i == len - 1) {
                        statusCode = 1;
                    }
                    bulkRequest.add(client.prepareIndex(index, type, String.valueOf(docId))
                            .setSource(jsonBuilder()
                                    .startObject()
                                    .field("score", score)
                                    .field("big_picture_uuid", source.getString("big_picture_uuid"))
                                    .field("img_url", source.getString("img_url"))
                                    .field("enter_time", enterTime)
                                    .field("leave_time", source.getString("leave_time"))
                                    .field("op_time", source.getString("op_time"))
                                    .field("lib_id", source.getInteger("lib_id"))
                                    .field("person_id", source.getString("person_id"))
                                    .field("is_alarm", source.getString("is_alarm"))
                                    .field("similarity", source.getFloatValue("similarity"))
                                    .field("camera_id", source.getString("camera_id"))
                                    .field("camera_name", source.getString("camera_name"))
                                    .field("office_id", source.getString("office_id"))
                                    .field("office_name", source.getString("office_name"))
                                    .field("img_width",source.getIntValue("img_width"))
                                    .field("img_height",source.getIntValue("img_height"))
                                    .field("left_pos",source.getIntValue("left_pos"))
                                    .field("top",source.getIntValue("top"))
                                    .field("index_name", indexName)
                                    .field("search_number", searchNum)
                                    .field("status_code", statusCode)
                                    .field("event_id", eventId)
                                    .endObject()
                            )
                    );
                }
                BulkResponse bulkResponse = bulkRequest.get();
                if (bulkResponse.hasFailures()) {
                    // process failures by iterating through each bulk response item
                    LOG.error(bulkRequest.toString());
                   // System.out.println(bulkRequest.toString());
                }
            }
         //}
        } catch (Exception e) {
            LOG.error("es批量写数据异常", e);
        }
    }

   /* public static void main(String []args) {
        WriteDataToES wd = new WriteDataToES();
        String clusterName = "face.dct-znv.com-es";
        String ip = "face.dct-znv.com";
        wd.initClient(clusterName,ip);
        System.out.println("init success");
        long t = System.currentTimeMillis();
        wd.writeData();
        long ts = System.currentTimeMillis() - t;
        System.out.println("Total Cost: " + ts + " ms.");
        wd.closeClient();

        //System.out.println("Total Cost: " + ts + " ms.");
    }
*/
}
