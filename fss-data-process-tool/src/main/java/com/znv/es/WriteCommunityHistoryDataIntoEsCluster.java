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

public class WriteCommunityHistoryDataIntoEsCluster {
    private static TransportClient searchClient = null;
    private static String clusterNameSearch = "face.dct-znv.com-es";
    private static String transportHostsSearch = "10.45.157.94:9300";
    private static String indexImageSearch = "history_community_face_data-2";
    private static String typeImageSearch = "history_data";
    private Random rnd = new Random();
    private final String startDate = "2015-01-01 00:00:00";
    private final String report_time = "2017-01-01 00:00:00";

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static Logger log = LogManager.getLogger(WriteCommunityHistoryDataIntoEsCluster.class);

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
        int frame_index= rnd.nextInt()*1000000;
        String track_idx=String.format("%2d%2d", t1, 0).replace(" ", "0");
       int race =0;
       String camera_id=String.format("320115%02d01%04d", rnd.nextInt(10), rnd.nextInt(100));
       Date leave_time=new Date();
       int face_width=1;
        String camera_name_array[]={"10.45.144.232动态_camera","10.45.144.232静态_camera","10.45.144.252动态_camera"};
        String camera_name=camera_name_array[rnd.nextInt(camera_name_array.length-1)];
        int face_height=1;
        int msg_type_array[]={1,2};
        int msg_type=msg_type_array[rnd.nextInt(msg_type_array.length-1)];
        int emotion=1;
        Date event_time=new Date();
        String camera_address="";
        String office_name="力维大厦8楼";
        String event_id=String.format("%15d%3d", t1, 0).replace(" ", "0");
        Date op_time=new Date();
        String task_idx=String.format("%17d%3d", t1, 0).replace(" ", "0");
        int eye_open=1;
        int beard=1;
        String office_id=String.format("%8d%3d", t1, 0).replace(" ", "0");
        int face_y=1;
        long duration_time=4;
        float quality_score=0.75471972f;
        float yaw=1.4980263f;
        int age=28;
        String small_picture_path="8bd60a0a44784406b212309b34094b2d";
        String rt_feature="7qpXQoleAAAAAgAAjmeFvIbXGD0N+My7mAEuPRhS5DznrbQ9CP6HvS3Kbj0X8888hrSjvY5MKb3HnY48L2LavHH1HjxXIJS8Ec1PvbIoGjyURWc9gEaTPZXaqzz3Ube997VIPRDiBDk8ZnW83Vs5PV0Igj0vjoc9PLy0vD22w7n4cxg7sp98PSHwJj0nfPQ8jnVRvahQvzyyW1M95KcIvbOuDz0mC5g9FJH1O7Zmobv6KAo9fRscOwzSnLw1agC9RB+6vGpcMr1fJbA8gvOvPDcTSj1AOMG9gj13u4v+Wz3KASu9h5MJvJy2BT3+Hqc8sbknPHTijjtCfXc9Ty19u6wZhTxmcHE9GQY6PQ4UAT2kvGU9cpoyvVWnOjqpMxe9zHq9vbBY9jzu+eW8bcxpvZMNor2mPCY9pc+WPWd7sj0BNKA7WNsLvaPWebtSdSS93wvPvF+PyjyqKgA95eoHPQ/7pr3ZH1C9GdfBPQj8xb2s3Oo880e6uoEWTT3FBFA9Xsr2vCmiEj0Oe6U8zIT+vH55mDuMJ6g9u+QlvJ0AeTyAiR492TG4OzC7Mb06fYu9mgtMvYslqDp0HTW9eLq8vZcd+Dwb93y9PVeZvelzOL2sghw9/YSCvBIhMT0BRUC9loQ7PYZAjjr97JY99rOevOIsVr0gD8C8XUiNPOvIm7s/L9Y8vqJjumFoOT2TF6s8ujyHvdKqhjvIpjI8Vr73vXJkybwtkvi8KGjBPch9Er2RP4+8Tf5VvIhDJb1Shc69brllvbtZfT0pBqG8txCZvSCLRT0xEsQ9f5OHPQ/2PLxI9769ZsIbOwZyMbxsO7U7/nyzOgh7mztjI+U8SBdVPYmlYz34QbG9q751PKmxuD2pFbK85Mn0ucXAkrsuOoe9B0KRPJGmGTyhWDu6t2tNvWPSrbo+zCs9dIFUPUK2AbyTxqM9Y9epOsfH3TxaN1C8BjWZPcH8PL2Auwm8lvZLPUgj2bxfAJe8dj4KPSlwwz16zzw9cXkwvUfEo7wfwUc99zqYvNBdejsKpFK97H3CPEdCcb1+zTq9ZIa3vA7jiL1JrCc9srR5u7LflTxCcqi99WkjvZZhmT2N9Ya92l6UPei4PDw4bDI9nmYYPNdtiz2K4548J6UqvEqHjr1lEeU8aeI8PdBvojucnb08DG6IOrIWxL0hpLg8XY4FvX7XSLyKj+w9I4ajPFo5k70R2Wm6haJzO+e/XbqKf+A74koEvfHaXrzRNBA9TafWve5b8Tx/ZD481RDIvWlZX70Lvl0866thvK/LqL2xa9e7w8I1vXW3vrwoOx096KqHvfYiGr2AToQ8poLgPP0NIL2aQQu8NA5nvbnRGr0JVnC9ghf+PILrrj1fUcA8WwiAuSQXvbohvKC7UEHNuk3TxjzoM908fsGGPW+RWb1TcUU9utTVPNuxWb0Opl69xfomPUeECr1xT+87sFevuvSDMb36pGM8A6g+PZVZoz0vnwY9SO/bvfNGND1dcJA8KcNovI+GOD2aMxg9eJKfPUaJ67u55ow8K+E4PFbBPT0L/+o8Xd5bPF2Z/rwKBto8G6guPUolE70skGM9SX4kPZL8tTsjc0S8tc8XPcnTQzycuhe9vtEOvTqF+bw1Shq9juIpPST8+jxlEVQ84q3GvaM/nLxk11g9FMe4vNBvn7y4PBk9EBUDPZFjxTz3i1O89IWCPaE+TDzXwNc8nmsPPVv1Oj1AA+g8K6iiPVMbV73F3cu6DNc2vVjgpL1AJCo9LBi1vJEpIL1lG629IUixPMVWYT03EWU9evKSPLTGkrxwX+S7yQ4bvEtCcryYxrk6AF2ku6zG1jw7wTu9CUj4vArlhj3dkay99AjjPLu3DD3UZuA8wBMZPZgH4LyBvyk9cIkKPUo4tbz5iL+6rOaCPfGljzvqXUA9MwoRPQDhWLuEAT+9i7xHvYHpkrxJeB28JH9QvYDJh71+X9Y8ZTc9vZHmhL3ToAm9w6REPVefKbwNF9s8qMo+vExV7Dw0BwY8GguFPY5E/LxgqwK9vW2nPD+JBz17XKc8wo4iPCu7NrxmAWI7Fc5QPN16ZL34xYm79ucvPJhyob3yh+S83RnmurKK7j3NGxy9DlMEvTtZhrymMhG9qrSjvRTrmrxJkik9BD70u1TZcL1T8f88Uk2hPRTzUD2Jr1W8bwGivYKEYLvy0xG8Y3GIPAvCKrzyGaA8LjwvPcuo6DwNWlM9k6S+vaSPN7vvacg9eAXEvKK4UTdw/NE8q1C2vGyaYzxYuco7RUaVvFwkkr0yk086EPntPKDsEz3VBb68zP+NPTn9u7xvSso8sW+GvLvNFT1pPwS9r4gQvZz0sjxG5d68p6tbu9Um7zz+VpE9EBESPKmRGL0vkLO8KMoVPaI6Db1bqly8Wba0vGF6xjtc9Vm9DuQ8vTdEgjv3VEa9P1UsPVdiFzzCpNq7QwovvRhWzLwVrzY9YX11vYHxdD2RhyU9bVwdPT35JTyDb2o9+87MPACn7zs6THu9YatbPGkwjD2H/6445w7CPAEsNLvwkYy9+Fs8PVs8Fr0VfxS9HZS5PUGmlzyCoUC9L+uivNR+N7wLidE6BLhAOtLu47wmei68xdk1PfS74b2XmEU9O92oPPJOyL1NP1y9BjwKPZdSQjy1S5S9dPZDPBRHNr2SztK8xDQ/PcvYXL3+XT69JUdMPVmW2jxhxGK90Y5UO6mdhLzLaCC9CdoYvSKThzzxnK49DI7NuJyOtTw=";
        int event_type=2002;
        int mouth_open=1;
        int mask=0;
        int glass=0;
        int camera_type=30000;
        float roll=-0.7855429f;
        Date enter_time=new Date();
        int is_alarm=0;
        float pitch =14.446893f;
        int face_x=1;
        int camera_kind=3;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String uuid="AWWzNh0rd_oQ7f_9CuYo";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("uuid", uuid);
        map.put("camera_kind", camera_kind);
        map.put("face_x", face_x);
        map.put("pitch", pitch);
        map.put("is_alarm", is_alarm);
        map.put("enter_time", df.format(enter_time));
        map.put("roll", roll);
        map.put("camera_type", camera_type);
        map.put("glass", glass);
        map.put("mask", mask);
        map.put("mouth_open", mouth_open);
        map.put("event_type", event_type);
        map.put("rt_feature", rt_feature);
        map.put("small_picture_path", small_picture_path);
        map.put("age", age);
        map.put("yaw", yaw);
        map.put("quality_score", quality_score);
        map.put("duration_time", duration_time);
        map.put("face_y", face_y);
        map.put("office_id", office_id);
        map.put("beard", beard);
        map.put("eye_open", eye_open);
        map.put("task_idx", task_idx);
        map.put("op_time", df.format(op_time));
        map.put("event_id", event_id);
        map.put("office_name", office_name);
        map.put("camera_address", camera_address);
        map.put("event_time", df.format(event_time));
        map.put("emotion", emotion);
        map.put("msg_type", msg_type);
        map.put("face_height", face_height);
        map.put("camera_name", camera_name);
        map.put("face_width", face_width);
        map.put("leave_time", df.format(leave_time));
        map.put("camera_id", camera_id);
        map.put("track_idx", track_idx);
        map.put("frame_index", frame_index);
        return map;
    }

    /**
     * 开始写数据到ES索引
     */
    public static void writeIntoES(String targetIndex, String type, long importNums) {
        long count = 0;
        WriteCommunityHistoryDataIntoEsCluster wdes = new WriteCommunityHistoryDataIntoEsCluster();
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
        WriteCommunityHistoryDataIntoEsCluster wdes = new WriteCommunityHistoryDataIntoEsCluster();
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
        writeIntoES("history_community_face_data-2", "history_data", 1000000);
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        searchClient.close();

    }
}
