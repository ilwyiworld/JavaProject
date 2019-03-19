package com.znv.es;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.*;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.Random;
import com.znv.utils.*;

public class WriteDataIntoEsCluster {
    private static TransportClient searchClient = null;
    private static String clusterNameSearch = "znv-es";
    private static String transportHostsSearch = "10.45.157.91:9300";
    private static String indexImageSearch = "xiaov_bigdatatable_4";
    private static String typeImageSearch = "xiaov_bigdatatable";
    private Random rnd = new Random();
    private final String startDate = "2015-01-01 00:00:00";
    private final String report_time = "2017-01-01 00:00:00";

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static Logger log = LogManager.getLogger(WriteDataIntoEsCluster.class);

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
        Integer FsuId = rnd.nextInt(100);// 255
        Integer MeteType = 1;
        String sMeteId = String.format("%02d0021%04d", rnd.nextInt(10), rnd.nextInt(500));
        String sDevId = String.format("320115%02d01%04d", rnd.nextInt(10), rnd.nextInt(100));
        Float CurrentVal = rnd.nextFloat() * 50;
        Date OccurTime = randomDate(startDate, df.format(new Date()));
        // hy devid, meteid, 高位补1，并转换为long型
        long lMeteId = Long.parseLong("1" + sMeteId);
        long lDevId = Long.parseLong("1" + sDevId);
        String stationid = String.format("%02d0021%04d", rnd.nextInt(10), rnd.nextInt(500));
        String precinctid = String.format("%02d0021%04d", rnd.nextInt(10), rnd.nextInt(500));
        String mete_name_array[]={"C2-09机柜直流电流","B相功率因数","C1-14机柜直","C1-13机柜直流电流","C1-09机柜直流电流","直流支路15电流"};
        String mete_name=mete_name_array[rnd.nextInt(mete_name_array.length-1)];
        String metetype_name = String.format("%02d0021%04d", rnd.nextInt(10), rnd.nextInt(100));
        String device_name_array[]={"配电柜-2AP-AC7","配电柜-2AP-AC8","配电柜-2AP-AC12","LA107-备用(PMAC625)","LB105-电梯APE-DT常用(PMAC6)"};
        String device_name=device_name_array[rnd.nextInt(device_name_array.length-1)];
        String devicetype_name_array[]={"多路交直流电表","低压配电进线柜","多路交直流电表",""};
        String devicetype_name=devicetype_name_array[rnd.nextInt(devicetype_name_array.length-1)];
        String precinct_name = String.format("%02d0021%04d", rnd.nextInt(10), rnd.nextInt(100));
        String station_name = String.format("%02d0021%04d", rnd.nextInt(10), rnd.nextInt(100));
        Integer device_type = 2;
        Integer device_kind = 1;
        Integer report_type = 3;
        String report_data = String.format("320115%02d01%04d", rnd.nextInt(10), rnd.nextInt(100));
        String explanation = String.format("%02d0021%04d", rnd.nextInt(10), rnd.nextInt(100));
        String devicekind_name = String.format("%02d0021%04d", rnd.nextInt(10), rnd.nextInt(100));
        String parent_path = String.format("%02d0021%04d", rnd.nextInt(10), rnd.nextInt(100));
        String parent_path_name_array []={"国富光启一期/1F国富光启低压室/国富光启IDC数据中心","国富光启一期/2F华为微模块/201室IDC数据机房/2AP-AC6","国富光启一期/2F华为微模块/201室IDC数据机房/2AP-AC8","国富光启一期/2F华为微模块/201室IDC数据机房/2AP-AC8","国富光启一期/2F华为微模块/201室IDC数据机房/2AP-AC12"};
        String parent_path_name=parent_path_name_array[rnd.nextInt(parent_path_name_array.length-1)];
        Integer mete_type = 2;
        Integer report_unit = rnd.nextInt(100);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("FsuId", FsuId);
        map.put("CurrentVal", CurrentVal);
        map.put("MeteType", MeteType);
        map.put("DevId", lDevId);
        map.put("station_id", stationid);
        map.put("precinct_id", precinctid);
        map.put("MeteId", lMeteId);
        map.put("mete_name", mete_name);
        map.put("metetype_name", metetype_name);
        map.put("device_name", device_name);
        map.put("devicetype_name", devicetype_name);
        map.put("device_type", device_type);
        map.put("device_kind", device_kind);
        map.put("devicekind_name", devicekind_name);
        map.put("precinct_name", precinct_name);
        map.put("station_name", station_name);
        map.put("parent_path", parent_path);
        map.put("parent_path_name", parent_path_name);
        map.put("report_unit", report_unit);
        map.put("mete_type", mete_type);
        map.put("report_type", report_type);
        map.put("report_data", report_data);
        map.put("explanation", explanation);
        map.put("report_time", report_time);
        return map;
    }

    /**
     * 开始写数据到ES索引
     */
    public static void writeIntoES(String targetIndex, String type, long importNums) {
        long count = 0;
        WriteDataIntoEsCluster wdes = new WriteDataIntoEsCluster();
        for (int i = 0; i < Math.ceil((float) importNums/10000 ); i++) {
            BulkRequestBuilder bulkRequest = searchClient.prepareBulk();
            IndexRequestBuilder indexerbuilder = searchClient.prepareIndex(indexImageSearch, typeImageSearch);
            for (int j = 0; j < 10000; j++) {
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
        WriteDataIntoEsCluster wdes = new WriteDataIntoEsCluster();
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
        writeIntoES("xiaov_bigdatatable_4", "xiaov_bigdatatable", 400000000);
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        searchClient.close();

    }
}
