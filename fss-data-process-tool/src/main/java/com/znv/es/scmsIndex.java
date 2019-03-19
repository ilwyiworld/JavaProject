package com.znv.es;

import com.znv.utils.ESUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class scmsIndex {
    private static TransportClient searchClient = null;
    private static String clusterNameSearch = "face.dct-znv.com-es";
    private static String transportHostsSearch = "10.45.157.41:9300";
    private static String indexImageSearch = "scms_door_data_2";
    private static String typeImageSearch = "scms_door";
    private Random rnd = new Random();
    private final String startDate = "2015-01-01 00:00:00";
    private final String report_time = "2017-01-01 00:00:00";

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static Logger log = LogManager.getLogger(scmsIndex.class);

    /**
     * 创建索引的代码
     *
     * @param esTClient
     * @param index
     * @param type
     */
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

    /**
     * 时间操作类
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public Date randomDate(String beginDate, String endDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = format.parse(beginDate);
            Date end = format.parse(endDate);
            if (start.getTime() >= end.getTime()) {
                return null;
            }
            long date = randomNum(start.getTime(), end.getTime());
            return new Date(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private long randomNum(long begin, long end) {
        long rtn = begin + (long) (Math.random() * (end - begin));
        if (rtn == begin || rtn == end) {
            return randomNum(begin, end);
        }
        return rtn;
    }

    //用来产生数据
    public Map<String, Object> produceData() {
        long t1 = System.currentTimeMillis();
        String eventName="zc1";
        String eventGroup="这个组";
        String eventId=String.format("%13d%3d", t1, 0).replace(" ", "0");
        String eventTime="2003-09-08 00:00:00";
        String eventDesc="desc";
        int eventType=6;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("eventName", eventName);
        map.put("eventId", eventId);
        map.put("eventTime", eventTime);
        map.put("eventGroup", eventGroup);
        map.put("eventDesc",eventDesc);
        map.put("eventType",eventType);
        return map;
    }

    /**
     * 开始写数据到ES索引
     */
    public static void writeIntoES(String targetIndex, String type, long importNums) {
        long count = 0;
        scmsIndex wdes = new scmsIndex();
        for (int i = 0; i < Math.ceil((float) importNums/1 ); i++) {
            BulkRequestBuilder bulkRequest = searchClient.prepareBulk();
            IndexRequestBuilder indexerbuilder = searchClient.prepareIndex(indexImageSearch, typeImageSearch);
            for (int j = 0; j < 1; j++) {
                bulkRequest.add(indexerbuilder.setSource(wdes.produceData()));
            }
            bulkRequest.get();
            count += 1000;
            if (count % 100000 == 0) {
                log.info("success write　10W data!!!");
            }
        }
    }

    /**
     * transportlient用来进行删除ES索引中的数据
     */
    public static void deleteFromEs() {
        try {
            DeleteResponse deleteResponse = searchClient.prepareDelete(indexImageSearch, typeImageSearch, "1234567890123").get();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("出现异常了，报错了");
        }
        System.out.println("删除该索引中的数据");
    }

    /**
     * 更新索引信息
     */
    public static void updateES() {
        try {
            XContentBuilder Object = XContentFactory.jsonBuilder().startObject().field("name", "zs").endObject();
            UpdateResponse updateResponse = searchClient.prepareUpdate(indexImageSearch, typeImageSearch, "1234567890123").setDoc(Object).get();
        }catch (Exception e){
            e.printStackTrace();
            log.error("出错了，报错了");
        }
        System.out.println("更新索引信息成功");
    }

    /**
     *获取字符长度
     */
    public static void getCharLength(){
        scmsIndex wdes = new scmsIndex();
        Map<String,Object> map=wdes.produceData();
        String data=map.get("parent_path_name").toString()+wdes.produceData().get("devicetype_name").toString()+wdes.produceData().get("device_name").toString()+wdes.produceData().get("mete_name").toString();
//        +wdes.produceData().get("station_id").toString()+wdes.produceData().get("precinct_id").toString()+wdes.produceData().get("precinct_name").toString()+wdes.produceData().get("station_name").toString()+wdes.produceData().get("report_time").toString()+wdes.produceData().get("report_data").toString()+wdes.produceData().get("explanation").toString()+wdes.produceData().get("devicekind_name").toString()+wdes.produceData().get("metetype_name").toString()+map.get("parent_path").toString();
        System.out.println(data.length());

    }

    /**
     * 测试写数据到ES
     *
     * @param args
     */
    public static void main(String args[]) {
        searchClient = ESUtils.getESTransportClient(clusterNameSearch, transportHostsSearch);
        log.info("start import...");
        long t1 = System.currentTimeMillis();
        writeIntoES("scms_door_data_2", "scms_door", 2);
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        searchClient.close();

    }
}
