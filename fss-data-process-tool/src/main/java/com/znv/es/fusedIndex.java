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

public class fusedIndex {
    private static TransportClient searchClient = null;
    private static String clusterNameSearch = "lv130.dct-znv.com-es";
    private static String transportHostsSearch = "10.45.157.130:9300";
    private static String indexImageSearch = "fused_output-1";
    private static String typeImageSearch = "fused";
    private Random rnd = new Random();
    private final String startDate = "2015-01-01 00:00:00";
    private final String report_time = "2017-01-01 00:00:00";

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static Logger log = LogManager.getLogger(fusedIndex.class);

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
        String fused_feature="江宁区";
        String fused_uuid=String.format("%13d%3d", t1, 0).replace(" ", "0");
        String fused_time="2002-09-08 00:00:00";
        int father_num=10;
        Map<String, Object> map = new HashMap<String, Object>();
        Map<Object ,Object> map1=new HashMap<>();
        Map<Object ,Object> map2=new HashMap<>();
        map.put("fused_feature", fused_feature);
        map.put("fused_uuid", fused_uuid);
        map.put("fused_time", fused_time);
        map.put("father_num", father_num);
        List list =new ArrayList();
        map1.put("rowkey", "12345678902345622343");
        map1.put("uuid","8888888");
        map2.put("rowkey","1212");
        map2.put("uuid","4324324");
        list.add(map1);
        list.add(map2);
        map.put("father_link",list.toString());
        return map;
    }

    /**
     * 开始写数据到ES索引
     */
    public static void writeIntoES(String targetIndex, String type, long importNums) {
        long count = 0;
        fusedIndex wdes = new fusedIndex();
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
        fusedIndex wdes = new fusedIndex();
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
        writeIntoES("fused_output-1", "fused", 1);
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        searchClient.close();

    }
}
