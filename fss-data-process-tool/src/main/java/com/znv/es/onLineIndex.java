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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class onLineIndex {
    private static TransportClient searchClient = null;
    private static String clusterNameSearch = "face.dct-znv.com-es";
    private static String transportHostsSearch = "10.45.157.94:9300";
    private static String indexImageSearch = "online_output-1";
    private static String typeImageSearch = "online";
    private Random rnd = new Random();
    private final String startDate = "2015-01-01 00:00:00";
    private final String report_time = "2017-01-01 00:00:00";

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static Logger log = LogManager.getLogger(onLineIndex.class);

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
        String precinct_kind_20="江宁区";
        String lib_person_id=String.format("%13d%3d", t1, 0).replace(" ", "0");
        String birth_date="2002-09-08";
        String device_type="1001";
        String precinct_kind_40="力维大楼";
        String gender="1";
        String face_name="我的人脸";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date event_time=new Date();
        String precinct_kind_10="南京市";
        String identity_card=String.format("%13d%3d", t1, 1).replace(" ", "0");
        String precinct_kind_30="正方中路888号力维";
        String precinct_kind_70="8301";
        String event_type="1";
        String perma_addr_detai="就是这个";
        String resid_addr_region="南京江宁区";
        String face_id="12306";
        String device_address="CHINA";
        String event_id=String.format("%11d%3d", t1, 0).replace(" ", "0");
        String event_title="这个事件";
        String direction="1";
        String face_length="10";
        String face_pass_num="11";
        String precinct_kind_50="1单元";
        String face_is_visitor="101";
        String face_big_picture_url="httttp://10.45.157.121";
        String precinct_kind_60="8楼";
        String face_small_picture_url="httttp://10.45.157.121";
        float confidence=0.89f;
        String full_name="中兴力维";
        String perma_addr_region="南京力维";
        String resid_addr_detail="南京市江宁区正方中路888号";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("lib_person_id", lib_person_id);
        map.put("birth_date", birth_date);
        map.put("device_type", device_type);
        map.put("gender", gender);
        map.put("face_name", face_name);
        map.put("event_time", sdf.format(event_time));
        map.put("identity_card", identity_card);
        map.put("precinct_kind_10", precinct_kind_10);
        map.put("precinct_kind_20", precinct_kind_20);
        map.put("precinct_kind_30", precinct_kind_30);
        map.put("precinct_kind_40", precinct_kind_40);
        map.put("precinct_kind_50", precinct_kind_50);
        map.put("precinct_kind_60", precinct_kind_60);
        map.put("precinct_kind_70", precinct_kind_70);
        map.put("event_type", event_type);
        map.put("perma_addr_detai", perma_addr_detai);
        map.put("resid_addr_region", resid_addr_region);
        map.put("face_id", face_id);
        map.put("device_address", device_address);
        map.put("event_title", event_title);
        map.put("direction", direction);
        map.put("face_length", face_length);
        map.put("face_pass_num", face_pass_num);
        map.put("face_is_visitor", face_is_visitor);
        map.put("event_id", event_id);
        map.put("face_big_picture_url", face_big_picture_url);
        map.put("face_small_picture_url", face_small_picture_url);
        map.put("confidence", confidence);
        map.put("full_name", full_name);
        map.put("perma_addr_region", perma_addr_region);
        map.put("resid_addr_detail", resid_addr_detail);
        return map;
    }

    /**
     * 开始写数据到ES索引
     */
    public static void writeIntoES(String targetIndex, String type, long importNums) {
        long count = 0;
        onLineIndex wdes = new onLineIndex();
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
        onLineIndex wdes = new onLineIndex();
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
        writeIntoES("online_output-1", "online", 8);
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        searchClient.close();

    }
}
