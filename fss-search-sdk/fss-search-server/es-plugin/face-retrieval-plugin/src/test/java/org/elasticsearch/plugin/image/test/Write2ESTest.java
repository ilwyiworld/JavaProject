package org.elasticsearch.plugin.image.test;

import org.apache.commons.codec.Charsets;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

//import org.junit.runner.RunWith;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

/**
 * Created by Administrator on 2017/6/12. 写入ES的测试数据，测试迭代二的任意条件搜索功能及新字段
 */
// @RunWith(com.carrotsearch.randomizedtesting.RandomizedRunner.class)
public class Write2ESTest {

    private static Map createMapData(int index, String path, String name) {

        // 获取特征值
        // String featurePicPath = "E:\\FssProgram\\V1.0\\全表扫描\\相似度阈值测试\\不超过64KB的照片\\男\\陈述.jpg";
        String featurePicPath = path + "\\" + name;
        String feature = PictureUtils.httpExecute(new File(featurePicPath));

        byte[] rt_feature = PictureUtils.getFeature(feature);
        byte[] rt_image = PictureUtils.image2byte(featurePicPath);

        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("rt_feature", rt_feature);
        ret.put("rt_image", rt_image);
        ret.put("name", name);

        String uuid = Integer.toString(index);
        String person_id = Integer.toString(index);
        int lib_type = 0;
        int sub_type = 0;
        String camera_id = "123456";
        String camera_name = "camera123456";
        int camera_type = 0;
        String enter_time = "2017-06-12 09:53:00";
        String leave_time = "2017-06-12 14:53:00";
        long duration_time = 0;
        String gps_xy = "3.0014,-5.2484";
        String office_id = "1";
        String office_name = "office1";
        String op_time = "2017-06-12 09:54:44";
        int frame_index = 1;
        String task_idx = "1";
        String track_idx = "1";

        int img_width = 0;
        int img_height = 0;
        String img_url = ""; // sensetime 中保存的图片URL

        // 获取图片信息
        // String filePath1 = "E:\\FssProgram\\V1.0\\全表扫描\\相似度阈值测试\\不超过64KB的照片\\男\\陈述.jpg";
        // String filePath2 = "E:\\FssProgram\\V1.0\\全表扫描\\相似度阈值测试\\不超过64KB的照片\\李青2.jpg";
        // String filePath3 = "E:\\FssProgram\\V1.0\\全表扫描\\相似度阈值测试\\不超过64KB的照片\\李青2.jpg";
        // byte[] rt_image = PictureUtils.image2byte(filePath1);
        // byte[] rt_image_data2 = PictureUtils.image2byte(filePath2);
        // byte[] rt_image_data3 = PictureUtils.image2byte(filePath3);

        float quality_score = 0.98f;
        int left_pos = 0;
        int top = 0;
        int right_pos = 0;
        int bottom = 0;
        float yaw = 0.56f;
        float pitch = 0.56f;
        float roll = 0.56f;
        int age = 16;
        int gender = 0; // 0：女，1：男
        int glass = 0;
        int mask = 0;
        int race = 0;
        int beard = 0;
        int emotion = 0;
        int eye_open = 1;
        int mouth_open = 0;
        int need_confirm = 0;
        int confirm_status = 1;
        String confirm_by = "cs";
        String confirm_time = "2017-06-12 14:01:00";
        String confirm_comment = "confirmed";
        int alarm_type = 0;
        long alarm_duration = 102455l;
        String first_relation_id = "1";
        String first_relation_name = "cs";
        String first_relation_tel = "15476481358";
        int first_relation_type = 7;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // String str = sdf.format(enter_time) ;

        // ret.put("uuid", uuid);
        // ret.put("person_id", person_id);
        // ret.put("lib_type", lib_type);
        // ret.put("sub_type", sub_type);
        // ret.put("camera_id", camera_id);
        // ret.put("camera_name", camera_name);
        // ret.put("camera_type", camera_type);
        // try {
        // ret.put("enter_time", sdf.parse(enter_time));
        // ret.put("leave_time", sdf.parse(leave_time));
        // ret.put("op_time", sdf.parse(op_time));
        // ret.put("confirm_time", sdf.parse(confirm_time));
        // } catch (ParseException e) {
        // e.printStackTrace();
        // }
        // ret.put("duration_time", duration_time);
        // ret.put("gps_xy", gps_xy);
        // ret.put("office_id", office_id);
        // ret.put("office_name", office_name);
        // ret.put("frame_index", frame_index);
        // ret.put("task_idx", task_idx);
        // ret.put("track_idx", track_idx);
        // ret.put("img_width", img_width);
        // ret.put("img_height", img_height);
        // ret.put("img_url", img_url);

        // ret.put("rt_image_data", rt_image);
        // ret.put("rt_image_data2", rt_image_data2);
        // ret.put("rt_image_data3", rt_image_data3);
        // ret.put("quality_score", quality_score);
        // ret.put("left_pos", left_pos);
        // ret.put("top", top);
        // ret.put("right_pos", right_pos);
        // ret.put("bottom", bottom);
        // ret.put("yaw", yaw);
        // ret.put("pitch", pitch);
        // ret.put("roll", roll);
        // ret.put("age", age);
        // ret.put("gender", gender);
        // ret.put("glass", glass);
        // ret.put("mask", mask);
        // ret.put("race", race);
        // ret.put("beard", beard);
        // ret.put("emotion", emotion);
        // ret.put("eye_open", eye_open);
        // ret.put("mouth_open", mouth_open);
        // ret.put("need_confirm", need_confirm);
        // ret.put("confirm_status", confirm_status);
        // ret.put("confirm_by", confirm_by);
        //
        // ret.put("confirm_comment", confirm_comment);
        // ret.put("alarm_type", alarm_type);
        // ret.put("alarm_duration", alarm_duration);
        // ret.put("first_relation_id", first_relation_id);
        // ret.put("first_relation_name", first_relation_name);
        // ret.put("first_relation_tel", first_relation_tel);
        // ret.put("first_relation_type", first_relation_type);

        return ret;
    }

    private static void createMapData(int index, File[] files, BulkRequestBuilder bulkRequest,
                                      IndexRequestBuilder indexerbuilder) {

        // 获取特征值
        for (File file : files) {
            String feature = PictureUtils.httpExecute(file);

            byte[] rt_feature = PictureUtils.getFeature(feature);
            byte[] rt_image = PictureUtils.image2byte(file.getAbsolutePath());

            Map<String, Object> ret = new HashMap<String, Object>();
            ret.put("rt_feature", rt_feature);
            ret.put("rt_image", rt_image);
            ret.put("name", file.getName());
            bulkRequest.add(indexerbuilder.setSource(ret));
        }

    }

    /**
     * @param clusterName
     * @param transportHosts
     * @param index
     * @param type
     * @param count
     */
    private static void write2ES(String clusterName, String transportHosts, String index, String type, int count) {
        TransportClient esTClient = ESUtils.getESTransportClient(clusterName, transportHosts); // 连接ES
        createIndex(esTClient, index, type);

        BulkRequestBuilder bulkRequest = esTClient.prepareBulk();
        // String path = "E:\\项目\\10-大数据\\1-test\\cos_test\\";
        String path = "E:\\project_test\\face\\BLASTICLIST";
        for (int i = 1; i <= count; i++) {
            // String picpath = String.format("%s%05d", path,i);
            // File root = new File(picpath);
            // for (File file: root.listFiles()) {
            // Map<String, Object> ret = createMapData(i,file.getParent(),file.getName());
            // IndexRequestBuilder indexerbuilder = esTClient.prepareIndex(index, type);
            // bulkRequest.add(indexerbuilder.setSource(ret));
            // }
            // createMapData(i, root.listFiles(),bulkRequest, indexerbuilder);
            Map<String, Object> ret = createMapData(i, path, "test1.jpg");
            bulkRequest.add(esTClient.prepareIndex(index, type).setSource(ret));// .execute().actionGet();

            // bulkRequest.get(); // 单条新增
            if (i % 1000 == 0) {
                bulkRequest.execute().actionGet(); // 批量新增
            }
        }
        bulkRequest.execute().actionGet();

        System.out.println("write data to es end !");
        esTClient.close();
    }

    public static void createIndex(TransportClient esTClient, String index, String type) {
        IndicesExistsResponse indicesExistsResponse = esTClient.admin().indices().prepareExists(index).get();
        if (indicesExistsResponse.isExists()) {
            return;
        }
        String mapping = null;
        try {
            mapping = Streams.copyToString(new InputStreamReader(Write2ESTest.class.getResourceAsStream("/mapping/test-mapping.json"), Charsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateIndexResponse response = esTClient.admin().indices().prepareCreate(index)
                .setSettings(Settings.builder()
                        .put("index.number_of_shards", 5)
                        .put("index.number_of_replicas", 1)
                )
                .addMapping(type, mapping)
                .get();
        if (!response.isAcknowledged()) {
            System.out.println("create index failed!! \n" + response);
        }

    }

    private static void write2ESbulk(String clusterName, String transportHosts, String index, String type, int count) {
        TransportClient esTClient = ESUtils.getESTransportClient(clusterName, transportHosts); // 连接ES
        createIndex(esTClient, index, type);

        String path = "E:\\项目\\10-大数据\\1-test\\cos_test\\";
        // String path = "E:\\project_test\\face\\BLASTICLIST";
        while (count-- > 0) {
            for (int i = 1; i <= 20; i++) {
                String picpath = String.format("%s%05d", path, i);
                File root = new File(picpath);
                BulkRequestBuilder bulkRequest = esTClient.prepareBulk();
                for (File file : root.listFiles()) {
                    Map<String, Object> ret = createMapData(i, file.getParent(), file.getName());
                    IndexRequestBuilder indexerbuilder = esTClient.prepareIndex(index, type);
                    bulkRequest.add(indexerbuilder.setSource(ret));
                }
                // createMapData(i, root.listFiles(),bulkRequest, indexerbuilder);
                // Map<String, Object> ret = createMapData(i,
                // "E:\\项目\\10-大数据\\1-test\\cos_test\\00001","m.02w0y9-6-FaceId-0.jpg");
                // bulkRequest.add(indexerbuilder.setSource(ret));// .execute().actionGet();

                // bulkRequest.get(); // 单条新增
                // if (i % 100 == 0) {
                bulkRequest.execute().actionGet(); // 批量新增
                // }
            }
        }
        // bulkRequest.execute().actionGet();

        System.out.println("write data to es end !");
        esTClient.close();
    }

    private static void writeFromIndex(String clusterName, String transportHosts, String index, String type) {
        String sourceindex = "z-es-image-plugin-16single_4";//"hy-es-image-plugin-test5cos10single";//
        String sourcecluster = clusterName;//"lv102-elasticsearch";
        String sourcehosts = transportHosts;//"10.45.157.113:9300";

        TransportClient sourceClient = ESUtils.getESTransportClient(sourcecluster, sourcehosts); // 连接ES
        TransportClient destClient = ESUtils.getESTransportClient(clusterName, transportHosts); // 连接ES
        //  createIndex(destClient, index, type);


        SearchResponse scrollResp = sourceClient.prepareSearch(sourceindex).setTypes(type)
                .setFetchSource(null, "rt_image")
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                // .setQuery(qb)
                .setSize(1000).get(); //max of 100 hits will be returned for each scroll
//Scroll until no hits are returned
        do {
            BulkRequestBuilder bulkRequest = destClient.prepareBulk();
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                //Handle the hit...
                //   System.out.println(hit.getSourceAsString());
                IndexRequestBuilder indexerbuilder = destClient.prepareIndex(index, type);
                bulkRequest.add(indexerbuilder.setSource(hit.getSource()));
            }
            bulkRequest.execute().actionGet();

            scrollResp = sourceClient.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.

        sourceClient.close();
        destClient.close();

    }


    private static void ES2ES(String sourceClusterName, String sourceTransportHosts, String sourceIndex,
                              String sourceType, String targetClusterName, String targetTransportHosts, String targetIndex,
                              String targetType) {
        TransportClient sourceClient = ESUtils.getESTransportClient(sourceClusterName, sourceTransportHosts); // 连接ES
        SearchRequestBuilder responseBuilder = sourceClient.prepareSearch(sourceIndex).setTypes(sourceType);
        SearchResponse sr = responseBuilder.setQuery(QueryBuilders.matchAllQuery()).setFrom(0).setSize(10)
                .setExplain(true).execute().actionGet();
        SearchHits hits = sr.getHits();
        // todo 写不下去了，获取查询结果并组成object

    }

    private static void writeATTR() {
        String clusterName = "lv04-elasticsearch";
        String transportHosts = "10.45.152.222:9300";
        String index = "fss_arbitrarysearch_lq_02";
        String type = "fss_arbitrarysearch";

        // 插入数据！
        int count = 3100;
        write2ES(clusterName, transportHosts, index, type, count);
    }

    private static void writeImage() {
        String clusterName130 = "lv130.dct-znv.com-es";//"lv102-elasticsearch";//
        String transportHosts130 = "10.45.157.130:9300";
        String indexImage = "hy-lsh-test2cos64-hm";
        String typeImage = "test";
        int count130 = 1;// 10;
        //   write2ES(clusterName130, transportHosts130, indexImage, typeImage, count130);
        write2ESbulk(clusterName130, transportHosts130, indexImage, typeImage, count130);
        //   writeFromIndex(clusterName130, transportHosts130, indexImage, typeImage);
    }

    public static void main(String[] args) {

        // writeATTR();

        writeImage();

    }

}
