package org.elasticsearch.plugin.image.test;

import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.mapper.image.FeatureFieldMapper;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.lopq.LOPQModel;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.util.FeatureCompUtil;
import org.elasticsearch.util.TxtFileOperation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;

public class LopqCoarseTest {
    private static TransportClient sourceClient = null;
    private static TransportClient searchClient = null;
    private static String clusterNameSearch = "es111.dct-znv.com-es";//"lv130.dct-znv.com-es";//"lv94.dct-znv.com-es";// "lv102-elasticsearch";//
    private static String transportHostsSearch = "10.45.157.111:9300";//"10.45.157.130:9300";//"10.45.157.94:9300";
    private static String indexImageSearch = "lopq_lee_ms_feature_from_108";//"lopq-test-czl-lee-350w_new";// "lopq-test-czl-personlist-157w_new";//"lopq-plugin-test-czl";//"lopq-test-czl-lee";//"lopq-test-czl-500w";//"lopq-test-czl";//"pca-test2dim32-10000w";
    private static String typeImageSearch = "test";//"history_data";//"test";//

    private static String clusterNameSource = "face.dct-znv.com-es";
    private static String transportHostsSource = "10.45.157.112:9300";
    private static String indexImageSource = "history_search_yc";
    private static String typeImageSource = "history_data";


    public static void createIndex(TransportClient esTClient, String index, String type) {
        IndicesExistsResponse indicesExistsResponse = esTClient.admin().indices().prepareExists(index).get();
        if (indicesExistsResponse.isExists()) {
            return;
        }
        String mapping = null;
        try {
            mapping = Streams.copyToString(new InputStreamReader(Write2ESTest.class.getResourceAsStream("/mapping/test-lopq-mapping.json"), Charsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        CreateIndexResponse response = esTClient.admin().indices().prepareCreate(index)
                .setSettings(Settings.builder()
                        .put("index.number_of_shards", 5)
                        .put("index.number_of_replicas", 0)
                )
                .addMapping(type, mapping)
                .get();
        if (!response.isAcknowledged()) {
            System.out.println("create index failed!! \n" + response);
        } else {
            System.out.println("create index success!!!");
        }
    }


    private static Map<String, Object> genLOPQdatas(Map<String, Object> source, Base64 base64, FeatureCompUtil fc) {
        Map<String, Object> indexes = new HashMap<String, Object>();
        Object f = source.get("rt_feature");//Object f = source.get("rt_feature.feature_high");// history: "rt_feature", personlist: "feature"
        if (f == null) { // 传入特征有问题
            return null;
        }

        String feature = f.toString();
        int[] coarseCode = LOPQModel.predict(fc.getFloatArray(new Base64().decode(feature)));
        String coarseId = SerializationUtils.arrayToString(coarseCode);
        int[] fineCode = LOPQModel.predictFine(fc.getFloatArray(new Base64().decode(feature)), coarseCode);
        String fineId = SerializationUtils.arrayToString(fineCode);

        String rtFeatureName = "rt_feature";
        String coarseName = rtFeatureName + "." + FeatureFieldMapper.LOPQ + FeatureFieldMapper.LOPQ_COARSE_ID_SUFFIX;
        String fineName = rtFeatureName + "." + FeatureFieldMapper.LOPQ + FeatureFieldMapper.LOPQ_FINE_ID_SUFFIX;
        String featureName = rtFeatureName + "." + FeatureFieldMapper.LOPQ + ".feature";

        indexes.put(coarseName, coarseId);
        indexes.put(fineName, fineId);
        indexes.put(featureName, f.toString());

        //  indexes.put("rt_feature", feature);//也可以是indexes.put("rt_feature", new Base64().decode(feature));//测试插件写数据

        // indexes.put("name", source.get("person_name"));//yinchuan personlist
        // indexes.put("tag", source.get("card_id"));//yinchuan personlist
        // indexes.put("birth", source.get("birth")); //yinchuan personlist
        // indexes.put("sex", source.get("sex")); //yinchuan personlist

        // indexes.put("name",source.get("name"));//lee
        // indexes.put("tag", source.get("img_url"));//lee

        return indexes;
    }


    private static void writeIndex(String clusterName, String transportHosts, String index, String type, int count) {
        String sourceIndex = "data_lee_ms_feature_from_108_2";//"pca200w-test3dim32-lee";//"pca200w-test3dim32-personlist";// "hy-es-image-plugin-test5cos10single";//"z-es-image-plugin-16single_6";//
        createIndex(searchClient, index, type);
        SearchResponse scrollResp = sourceClient.prepareSearch(sourceIndex).setFetchSource(null, "rt_image")
                .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.DESC).setScroll(new TimeValue(60000))
                // .setQuery(qb)
                .setSize(1000).get(); // max of 100 hits will be returned for each scroll
        // Scroll until no hits are returned

        Base64 base64 = new Base64();
        FeatureCompUtil fc = new FeatureCompUtil();
        do {
            BulkRequestBuilder bulkRequest = searchClient.prepareBulk();
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                // Handle the hit...
                // System.out.println(hit.getSourceAsString());
                IndexRequestBuilder indexerbuilder = searchClient.prepareIndex(indexImageSearch, typeImageSearch);
                Map<String, Object> source = hit.getSource();
                Map<String, Object> indexes = genLOPQdatas(source, base64, fc);
                bulkRequest.add(indexerbuilder.setSource(indexes));
            }
            bulkRequest.execute().actionGet();
            System.out.println("begin to write data!!!");
            //break;//hy test

            scrollResp = sourceClient.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
                    .actionGet();
        } while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while
        // loop.
    }

    private static Map<String, Object> coarseQuery(String indexImageSearch, String typeImageSearch, String feature, float sim_t) {
        FeatureCompUtil fc = new FeatureCompUtil();
        Map<String, Object> map = new HashMap<>();
        BoolQueryBuilder boolQueryCoarse = QueryBuilders.boolQuery();
        int[] coarseCode = LOPQModel.predict(fc.getFloatArray(new Base64().decode(feature)));
        List<Object> values = new ArrayList();
        String featureName = "rt_feature";
        String coarseName = featureName + "." + FeatureFieldMapper.LOPQ + FeatureFieldMapper.LOPQ_COARSE_ID_SUFFIX;
        String fineName = featureName + "." + FeatureFieldMapper.LOPQ + FeatureFieldMapper.LOPQ_FINE_ID_SUFFIX;
        //将图片特征值变成list<double>形式
        byte[] byteFeature = new Base64().decode(feature);
        List<Double> featureValue = fc.getDoubleList(byteFeature, 12);

        boolQueryCoarse.filter(QueryBuilders.termQuery(coarseName, SerializationUtils.arrayToString(coarseCode)));
        Map<String, Object> params = new HashMap<>(4);
        params.put("featureName", featureName + ".lopq.feature");
        params.put("featureValue", featureValue);
        System.out.println("begin query by feature-comp, threshold is " + sim_t);

        BoolQueryBuilder boolQuery1 = QueryBuilders.boolQuery();
        boolQuery1.filter(QueryBuilders.termQuery(coarseName, SerializationUtils.arrayToString(coarseCode)));
        SearchResponse sr1 = searchClient.prepareSearch(indexImageSearch).setTypes(typeImageSearch).setQuery(boolQuery1)
                .setFrom(0).setSize(10000).get();
        long totalCount1 = sr1.getHits().getTotalHits();
        System.out.println("the only coarseCode filter search result num:" + totalCount1);
        System.out.println("Total Cost: " + sr1.getTookInMillis() + " ms.");

        QueryBuilder qb2 = QueryBuilders.functionScoreQuery(boolQueryCoarse, scriptFunction(new Script(ScriptType.INLINE, "native", "feature-comp", params)));
        SearchResponse sr2 = searchClient.prepareSearch(indexImageSearch).setTypes(typeImageSearch).setQuery(qb2).setMinScore(fc.Normalize(sim_t)).setFrom(0).setSize(1000).get();
        long totalCount2 = sr2.getHits().getTotalHits();
        System.out.println("the coarseCode filter and feature compare search result num:" + totalCount2);
        System.out.println("Total Cost: " + sr2.getTookInMillis() + " ms.");

        QueryBuilder qb3 = QueryBuilders.functionScoreQuery(
                scriptFunction(new Script(ScriptType.INLINE, "native", "feature-comp", params)));
        SearchResponse sr3 = searchClient.prepareSearch(indexImageSearch).setTypes(typeImageSearch).setQuery(qb3).setMinScore(fc.Normalize(sim_t)).setFrom(0).setSize(1000).get();
        long totalCount3 = sr3.getHits().getTotalHits();
        System.out.println("the only feature compare search result num:" + totalCount3);
        System.out.println("Total Cost: " + sr3.getTookInMillis() + " ms.");

        map.put("索引编号", indexImageSearch);
        map.put("查询结果数", totalCount1);
        map.put("正确数", totalCount2);
        map.put("总正确数", totalCount3);
        map.put("丢失数", totalCount3 - totalCount2);
        map.put("错误数", (totalCount1 - totalCount2));
        map.put("查准率", (float) totalCount2 / totalCount1);
        map.put("召回率", (float) totalCount2 / totalCount3);
        map.put("相似度阈值", sim_t);
        map.put("按coarseCode过滤耗时(ms)", sr1.getTookInMillis());
        map.put("按特征比对耗时(ms)", sr3.getTookInMillis());
        map.put("按coarseCode过滤耗时+特征比对耗时(ms)", sr2.getTookInMillis());
        return map;
    }

    private static void fineQuery(String feature) {
        FeatureCompUtil fc = new FeatureCompUtil();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        int[] fineCode = LOPQModel.predictFine(fc.getFloatArray(new Base64().decode(feature)), null);
        String featureName = "rt_feature";
        float sim_t = 0.5261f;
        String fineName = featureName + "." + FeatureFieldMapper.LOPQ + FeatureFieldMapper.LOPQ_FINE_ID_SUFFIX;
        //将图片特征值变成list<double>形式
        byte[] byteFeature = new Base64().decode(feature);
        List<Double> featureValue = fc.getDoubleList(byteFeature, 12);
        // long t = System.currentTimeMillis();
        boolQuery.filter(QueryBuilders.termQuery(fineName, SerializationUtils.arrayToString(fineCode)));
        Map<String, Object> params2 = new HashMap<>(4);
        params2.put("featureName", featureName + ".lopq.feature");
        params2.put("featureValue", featureValue);
        System.out.println("begin query by feature-comp, threshold is " + sim_t);

        BoolQueryBuilder boolQuery1 = QueryBuilders.boolQuery();
        boolQuery1.filter(QueryBuilders.termQuery(fineName, SerializationUtils.arrayToString(fineCode)));
        SearchResponse sr1 = searchClient.prepareSearch(indexImageSearch).setTypes(typeImageSearch).setQuery(boolQuery1)
                .setFrom(0).setSize(10000).get();
        long totalCount1 = sr1.getHits().getTotalHits();
        System.out.println("the only fineCode filter search result num:" + totalCount1);
        System.out.println("Total Cost: " + sr1.getTookInMillis() + " ms.");


        QueryBuilder qb2 = QueryBuilders.functionScoreQuery(boolQuery, scriptFunction(new Script(ScriptType.INLINE, "native", "feature-comp", params2)));
        SearchResponse sr2 = searchClient.prepareSearch(indexImageSearch).setTypes(typeImageSearch).setQuery(qb2).setMinScore(fc.Normalize(sim_t)).setFrom(0).setSize(1000).get();
        long totalCount2 = sr2.getHits().getTotalHits();
        System.out.println("the fineCode filter and feature compare search result num:" + totalCount2);
        System.out.println("Total Cost: " + sr2.getTookInMillis() + " ms.");
    }


    private static Map<String, Object> featureCompare(String indexImageSearch, String typeImageSearch, String feature) {
        FeatureCompUtil fc = new FeatureCompUtil();
        Map<String, Object> map = new HashMap<>();
        String featureName = "rt_feature.lopq.feature";
        float sim_t = 0.5261f;
        // float sim_t = 0.2f;
        byte[] byteFeature = new Base64().decode(feature);
        List<Double> featureValue = fc.getDoubleList(byteFeature, 12);
        Map<String, Object> params2 = new HashMap<>(4);
        params2.put("featureName", featureName);
        //  params2.put("featureValue",featureValue);
        params2.put("featureValue", feature);
        System.out.println("begin query by feature-comp, threshold is " + sim_t);
        QueryBuilder qb2 = QueryBuilders.functionScoreQuery(
                scriptFunction(new Script(ScriptType.INLINE, "native", "feature-comp", params2)));

        SearchResponse sr2 = searchClient.prepareSearch(indexImageSearch).setTypes(typeImageSearch).setQuery(qb2).setMinScore(fc.Normalize(sim_t)).setFrom(0).setSize(1000).get();
        long totalCount2 = sr2.getHits().getTotalHits();
        System.out.println("the only feature compare search result num:" + totalCount2);
        System.out.println("Total Cost: " + sr2.getTookInMillis() + " ms.");
        map.put("总正确数", totalCount2);
        map.put("按特征比对耗时(ms)", sr2.getTookInMillis());
        return map;
    }


    private static Map<String, Object> coarseAndfineQuery(String indexImageSearch, String typeImageSearch, String feature, float sim_t) {
        FeatureCompUtil fc = new FeatureCompUtil();
        Map<String, Object> map = new HashMap<>();
        BoolQueryBuilder boolQueryCoarse = QueryBuilders.boolQuery();
        BoolQueryBuilder boolQueryFine = QueryBuilders.boolQuery();
        int[] coarseCode = LOPQModel.predict(fc.getFloatArray(new Base64().decode(feature)));
        int[] fineCode = LOPQModel.predictFine(fc.getFloatArray(new Base64().decode(feature)), null);
        List<Object> values = new ArrayList();
        String featureName = "rt_feature";
        //float sim_t = 0.5261f;
        String coarseName = featureName + "." + FeatureFieldMapper.LOPQ + FeatureFieldMapper.LOPQ_COARSE_ID_SUFFIX;
        String fineName = featureName + "." + FeatureFieldMapper.LOPQ + FeatureFieldMapper.LOPQ_FINE_ID_SUFFIX;
        //将图片特征值变成list<double>形式
        byte[] byteFeature = new Base64().decode(feature);
        List<Double> featureValue = fc.getDoubleList(byteFeature, 12);

        boolQueryCoarse.filter(QueryBuilders.termQuery(coarseName, SerializationUtils.arrayToString(coarseCode)));
        boolQueryFine.filter(QueryBuilders.termQuery(fineName, SerializationUtils.arrayToString(fineCode)));

        Map<String, Object> params = new HashMap<>(4);
        params.put("featureName", featureName + ".lopq.feature");
        //  params.put("featureValue",featureValue);
        params.put("featureValue", feature);
        System.out.println("begin query by feature-comp, threshold is " + sim_t);

        SearchResponse sr1 = searchClient.prepareSearch(indexImageSearch).setTypes(typeImageSearch).setQuery(boolQueryCoarse)
                .setFrom(0).setSize(1000).get();
        long totalCount1 = sr1.getHits().getTotalHits();
        System.out.println("the only coarseCode filter search result num:" + totalCount1);
        System.out.println("Total Cost: " + sr1.getTookInMillis() + " ms.");

        QueryBuilder qb2 = QueryBuilders.functionScoreQuery(boolQueryCoarse, scriptFunction(new Script(ScriptType.INLINE, "native", "feature-comp", params)));
        SearchResponse sr2 = searchClient.prepareSearch(indexImageSearch).setTypes(typeImageSearch).setQuery(qb2).setMinScore(fc.Normalize(sim_t)).setFrom(0).setSize(1000).get();
        long totalCount2 = sr2.getHits().getTotalHits();
        System.out.println("the coarseCode filter and feature compare search result num:" + totalCount2);
        System.out.println("Total Cost: " + sr2.getTookInMillis() + " ms.");

        SearchResponse sr3 = searchClient.prepareSearch(indexImageSearch).setTypes(typeImageSearch).setQuery(boolQueryFine)
                .setFrom(0).setSize(1000).get();
        long totalCount3 = sr3.getHits().getTotalHits();
        System.out.println("the only fineCode filter search result num:" + totalCount3);
        System.out.println("Total Cost: " + sr3.getTookInMillis() + " ms.");

        QueryBuilder qb3 = QueryBuilders.functionScoreQuery(boolQueryFine, scriptFunction(new Script(ScriptType.INLINE, "native", "feature-comp", params)));
        SearchResponse sr4 = searchClient.prepareSearch(indexImageSearch).setTypes(typeImageSearch).setQuery(qb3).setMinScore(fc.Normalize(sim_t)).setFrom(0).setSize(1000).get();
        long totalCount4 = sr4.getHits().getTotalHits();
        System.out.println("the fineCode filter and feature compare search result num:" + totalCount4);
        System.out.println("Total Cost: " + sr4.getTookInMillis() + " ms.");

        QueryBuilder qb5 = QueryBuilders.functionScoreQuery(
                scriptFunction(new Script(ScriptType.INLINE, "native", "feature-comp", params)));
        SearchResponse sr5 = searchClient.prepareSearch(indexImageSearch).setTypes(typeImageSearch).setQuery(qb5).setMinScore(fc.Normalize(sim_t)).setFrom(0).setSize(1000).get();
        long totalCount5 = sr5.getHits().getTotalHits();
        System.out.println("the only feature compare search result num:" + totalCount5);
        System.out.println("Total Cost: " + sr5.getTookInMillis() + " ms.");


        map.put("索引编号", indexImageSearch);
        map.put("coarseCode查询结果数", totalCount1);
        map.put("coarseCode正确数", totalCount2);
        map.put("fineCode查询结果数", totalCount3);
        map.put("fineCode正确数", totalCount4);
        map.put("总正确数", totalCount5);
        map.put("coarseCode丢失数", totalCount5 - totalCount2);
        map.put("coarseCode错误数", (totalCount1 - totalCount2));
        if (totalCount1 != 0) {
            map.put("coarseCode查准率", (float) totalCount2 / totalCount1);
        } else if (totalCount2 == 0) {
            map.put("coarseCode查准率", 1);
        } else {
            map.put("coarseCode查准率", 0);
        }
        if (totalCount3 != 0) {
            map.put("fineCode查准率", (float) totalCount4 / totalCount3);
        } else if (totalCount4 == 0) {
            map.put("fineCode查准率", 1);
        } else {
            map.put("fineCode查准率", 0);
        }
        if (totalCount5 != 0) {
            map.put("coarseCode召回率", (float) totalCount2 / totalCount5);
            map.put("fineCode召回率", (float) totalCount4 / totalCount5);
        } else {
            if (totalCount2 == 0) {
                map.put("coarseCode召回率", 1);
            } else {
                map.put("coarseCode召回率", 0);
            }
            if (totalCount4 == 0) {
                map.put("fineCode召回率", 1);
            } else {
                map.put("fineCode召回率", 0);
            }
        }


        map.put("fineCode丢失数", totalCount5 - totalCount4);
        map.put("fineCode错误数", (totalCount3 - totalCount4));
        map.put("相似度阈值", sim_t);
        map.put("按coarseCode过滤耗时(ms)", sr1.getTookInMillis());
        map.put("按fineCode过滤耗时(ms)", sr3.getTookInMillis());
        map.put("按特征比对耗时(ms)", sr5.getTookInMillis());
        map.put("按coarseCode过滤耗时+特征比对耗时(ms)", sr2.getTookInMillis());
        map.put("按fineCode过滤耗时+特征比对耗时(ms)", sr4.getTookInMillis());
        return map;
    }


    private static void testLOPQ() {

        String savePath = "D:\\我的任务\\lopq\\Test\\test-01\\";//"/home/estine/lshAndpca/";
        String filePath = savePath + "params";
        //String filePath = savePath + "result";
        File file = new File(filePath);
        String path = savePath + "result\\" + "lopqSimResult.xlsx";
        String name = "f-Lopq";
        float sim_t = 0.5261f;
        int eachTestCount = 13; //每个测试集测试的特性征值个数
        //测试集： 1:银川150万名单库，2：银川500万历史数据，3：李成功350万微软数据，4：家里数据，5:1亿微软数据
        String[] testIndexs = {"lopq_lee_ms_feature_from_108"};//{"lopq-test-czl-personlist-157w_new","lopq-test-czl-history-500w_new","lopq-test-czl-lee-350w_new"};

        List<Map<String, Object>> values = new ArrayList();
        List<String> titles = new ArrayList<>();
        titles.add("索引编号");
        titles.add("相似度阈值");
        titles.add("总正确数");
        titles.add("coarseCode查询结果数");
        titles.add("coarseCode正确数");
        titles.add("coarseCode丢失数");
        titles.add("coarseCode错误数");
        titles.add("coarseCode查准率");
        titles.add("coarseCode召回率");
        titles.add("fineCode查询结果数");
        titles.add("fineCode正确数");
        titles.add("fineCode丢失数");
        titles.add("fineCode错误数");
        titles.add("fineCode查准率");
        titles.add("fineCode召回率");
        titles.add("按coarseCode过滤耗时(ms)");
        titles.add("按fineCode过滤耗时(ms)");
        titles.add("按coarseCode过滤耗时+特征比对耗时(ms)");
        titles.add("按fineCode过滤耗时+特征比对耗时(ms)");
        titles.add("按特征比对耗时(ms)");
        boolean isEnd = false;

        if (file.isDirectory()) {
            String[] fileList = file.list();
            int length = fileList.length;
            for (int testSetNum = 0; testSetNum < length; testSetNum++) {
                String newPath = filePath + "\\" + (testSetNum + 1) + ".txt";

                String writeFileName = savePath + name;
                String content = newPath;
                System.out.println(content);
                TxtFileOperation.contentToTxt(writeFileName + ".txt", content);
                // System.out.println(writeFileName + ".txt");

                //testLsh2Excel(feature, writeFileName);
                // testPca2Excel(feature, writeFileName);

                // 读TXT文件获取特征值
                String feature = "";
                String testIndexImage = testIndexs[testSetNum];
                Map<String, Object> map = new HashMap<>();
                try {
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(newPath)); // 建立一个输入流对象reader
                    BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言

                    for (int eachTestNum = 0; eachTestNum < eachTestCount; eachTestNum++) {
                        System.out.println("eachTestNum:" + eachTestNum);
                        feature = br.readLine();
                        map = coarseAndfineQuery(testIndexImage, "test", feature, sim_t);
                        values.add(map);
                        //  Write2Excel.writerExcel(path, name, titles, values);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            isEnd = true;
        }

        if (isEnd) {
            Write2Excel.writerExcel(path, name, titles, values);
        }

    }


    public static void main(String[] args) {
        try {
            LOPQModel.loadProto(FeatureFieldMapper.class.getResourceAsStream("/lopq/lopq_model_V1.0_D512_C13.lopq"));
        } catch (IOException e) {
            System.out.println("Failed to initialize hash functions and pca matrix" + e);
        }

        sourceClient = ESUtils.getESTransportClient(clusterNameSource, transportHostsSource);
        searchClient = ESUtils.getESTransportClient(clusterNameSearch, transportHostsSearch);

         //writeIndex(null, null, indexImageSearch , typeImageSearch ,0);

        String feature1 = "7qpXQsFdAAAAAQAA+sWovVs40bsb1Ck9Xm9/u2g4g72RkD483TUwPXDbdzwuSa694f49PcYhlTy5i6e8fmLjPZLdvbwwq9y8BEIsPKQnIj0aybM8cjCkOinMAj44YOy9GvBuvfExL736uRq9CAaRvacPkj1Us4s8VIZlveujqDwsB149pr2HPOliLj25E427uoDwPBbDgT0IpGE80J/oPKN0Hj6kTcy8gvGqvM3WGL1o5pk8YjsEvsWikr3Sgs858f3+OmWzATyRjlA966/9OTBrlbvXu2E9GZoaPbyrWT3B9Ce9vQ+du6NkgLux9wc9buC4PTMiu7u223+91VwNPnGsjr03/6O8z2nUPOg6mD22m7i8UVjLPVR0Ob3R7Ui7gE0rPObNkr0xCDI9BPSbvXGTtDzCibW97xYtPftmD71NQLm7ridLPWU8QTy6bQA7krQEvYBXWj1eTN+9Vf2uPVdGOrsPC0q9dR6VPQUYtrxwBtk9aE6kvTLxnzw9Lpk9dFX+vHhc97w2Drk9lG3Gu6Wo272leRu+XZFyvPna1734qNa8mFRWPecZLj3/RQQ+W7p3PSxfbryhHgW8Q3ZrPHwJ6TzWlSS9arXovVcr870SuqY7LI8DveetoL0lFni9OBR6vVFlEDoMHiY86tcsvYCQfjxnk0I9846+vJkCbT17OjC9HRt7PbRfTz1GJ7i84puVvZxG/L3rZGk7M8JSPSJXRr3cwpg9mEuWPfv5072qG788kBfHPGD6h72dHoI8j3L7PAYtb73ABz08Z+bQuf7UeD3CeiI97yEPPeO82DxxkAS+U807vZo1hT0wH/a8m6sBPnQRIL3YpRU9ZvXRuhJSi72ZM607Aui5u40T7bxyQSQ9HOvivQGIC70y03o837oNvn9LN73lmUS9Nku+PU45bz2pVCY9GdgFvdZMFD3ydAC921FSvX2MDz5gfJC91E48vRz85b0rd2Q9OCCLO8iOkr3YV3W8/NjcPau5D72dSrE9F8KLvbnL2juf/E+9EAauvMBkmb2FxeA9oB2vvDM+FLr0Njk7dNeIvHn6tLwSUR48AzPtvR2U0bzPZf09l0HuPQH1djsOZS88qWGTvSrakTziqo+91628vXf8ITtJw3O7oQqJvR/trD1DMpY956fOvaa8AbySpBe99NKnvS3oxz0ioMI8wgGtPUugarvZrri9h9jVPJNRtL3di2M9h7wfvfGVQz6dgJG8ydyzueFYIrps8J+8+/5+vKNqi73I/SC9QQZtPR5Prb3X88Y9OjGCPH5rU72XJ0g9gzMBPb2tgD323Kq9l29VPapREb0o4gO+5L82vmEWEDxJeJm9kQnJPIMS2DyhOOE8WcdkPbgjKDyABYA9RTqnPQ==";
        String feature2 = "7qpXQsFdAAAAAQAApDpvvQEtxTt+Uiw+U9fpPUVl+TzICq66Set5vRAmAT0S3AQ91euLvaT6OLzUYaW9oBubvIYpYz3/ggc9oJKePA/MdzyxOgq+aC+ePSdDeDsgZ3U8PC08PVWEBDwJZry7TWiQPXR3l71ILpi73LR5PdunMT0eSmY9cEswvFQ8nL1/Xf68lHvnPfyTiT1c/ku9Do2GPYWLzr3Hbry9YcK0vHIXvb00+ok90uGdvIp8xT36pUW9W24cPYTOZLx7Sh29erGxPTiwyzxCyD29qgqgPAKPSb6TUaY9RP2ePakrCT0h64q8YPwCPqPhe73Q3Aa9iNOSPNROMr1TSwI8y/YOvYFbb7yx/dg9nVrCPCM4vTyf2iO9GiUePVgwSbxJDGo9pGXnPSol8jt96HA9rVfRO1VjeD0fXCO8NReLPRrO4T13P2W92QgFO44QwD0Wf6g92hQRugq+Gr04XTS9lKIpPQt5tL2RNsW8pl6aOyCJ3D2XGfE8kuu9PTcKAD3oapE9Oa8RPL8BgT075Fs9OgCePAovkT0dA1O9+bCru5Ov6j0SwiC9v53gvLQIXz2zkHo96JWGvN4fyj068Tg94CM/PZ8P5zsPnAK96XMFPh+BBr12u9y8GYIPPdZClb0FVUG9D8QnPTMogDxq6kO9KXdaPTyGbDy7d9e9V1e2vB8gwz1Tste9OuEnvF8VQj2LqsQ91qejOeog4b31tvG9DE+LvSVBXD1yq229M4QTPQFUl7wPkMK8gXGnPOedQTzkeeA9f+k+vZCkiLyj1Gy9UIjnvNeHXr3Ua6k7UZcGPraWib3nrhQ6dZ6fPFjnWTxYpX48hi7+PCXPSL02L6W8XvvGPOsAST0LXly8cs6HPYBdHz5DSag50fkpPRuGxT0bOau9jSCnPUrIwj2KQrk9otlpvTSwgz2xyr28Aq0LPTlDPL3KPkG9mtEyPX2XGD4uDnE9BUZaPT3Zlryvjy09F+lovXRZpL1EQlo9cyzVvf91Njw04LC87MxmPQsn+Tx+Y4c9vBUYvHzkU73MqQ89t26RPATWqT1XEAO8imH7vFIuqjt1zlg9XFiWvei9sjx5p8G9NKtDPTyJJDxnBMw90YUpO+hsyDywo3e7nAqPvZqJMD3rrJq99Dpxu0TULD3F4u29Phe+PMlpOr2VdRq8LaBNuw0RGb3D/h+9eliKvcvTTL03swC9FyZSvYrUoL3jVE88nnedOih+IL5pDka952MlPVESDz1P55Y7OPICPheyKr2d1hQ+jJuVPTXO0L2DRQg96EKlvLWtvzxxFZ29Q4/YPROFI74/I0Y9EtWnPICYcz23S644rPvtPDcfgTxeIrM7j3W+u5Ebh7s3t4C9rGSsvA==";
        /*String f = "7qpXQsFdAAAAAQAAtJTLvH+kyb3OfeC9hzGMvUOxOj1zW709MuNiPMl51rsDirq93n4IO+QX3jwCxLa81tnuPZzuiL3WgkE7pTUwvbSMAD5VO6u98kAUPIIRNL2NVSi94VSAPeXv7byfvAO+1OFFO9KOEbx3Vtm9s+CZPY2FFr7chqY8sfYnPTg04731O4S9q5YjvXqiKb261qs8AxKMvUYHEr115EK9PmCevYGyFb2ROIe91DoLvcF/wbwXOpG9qLabPc0/wTx+Hoo9LAq9vMd64TwTTeI7MHcQO0BOsTytjIe9UbTEvSMmtL0gLfU9AyHuPcBwMT7MfDI9ZjOPvaZEizscz6e7NKXAPQpCo7zRioM9c5rbPdOXyL2BuNI6U6MPvkcon73Pk9C8jzOUvXlKSj1cUUS9rHuCPLfVJj1FeKw9JqVRvPRQ7z0lGjg97GmSvWsKlj3i4au8gNA4vFyBWr2ILWU8/wKIvA3UajxXIbc8kLyzPI3yJb2ze4q8WFiAPTg3Sz2urmg8nqMzPMJxn7xpppG9ldr9O+7+pTzxRR49LHpsvf4vk70B16A8xXZkvEwAB7vVsUM94NmSPTRtCTtAaAe9ke6rvDwgUzk5bHi9yQr0vESzgj1SPPE9752KvT4Xw70drXS9nPrlPBIfpbUOXeG8yCb0PHjgLb0fhFs86byGPW9Y2Dukg7k9jh9APWQQp70tk7O9QBqNPCvRX72Qhkw7LUBIvcp2orxVfCU9wn3IPOt8kr1me/88xTddvUtR6j2XijQ9L4thPGdWBr61UBu9V3AUPmer4DulfSg9Tu2UPYHffr2uRJM9jECePQtZUr2Oy6k9Rv65vBDcub2u9Lu8wtLQuhuLBj34YoW9IutJO0OqmL3IhxK+xsxFvVBZUj3y3ws9nM/sPY6vnz0iXy88lrERPSKPAjqaNGe9TleLuw9KQLzhNio9JPeEPAQTkr2udh89eQbAPSG3aDxzIdm98ZTtPfURODuE7Aa8L/4Mvd7Zjj0BBHw9LTyYPf5mhr1tYTS7uuGvPUHT5r2M/Lu8N2TCPQBS6rxOl0E9aTlzvVWTpD3YmIW8Sa4FPlQJB71VWQq9l3SiPKThdrzbtEU9bxQ4vaFJDj3fjpk880blvJCznLztlYm9hQ+GPQplaL3B5Qu+wnVJvVtQHb00viE92xHpveAavr2P8as8rOAFva0k5jvzJyM9UdsMPU0MgjxnPJu9SiYLO4ysgT0UR+k9p/3xPPyezD3p6ai9K1kbvSEbFb5Omj+90eAIPABo5jy8qxe97+K7PNaEiL0dZVq9c3L8PUOjnTrmuwq9FHyCufulFb3N+PY8+eKHvfCCgz1Lygo9jLYNvS5TEL3M5xo9XBR7PQ==";*/

        /*float sim_t = 0.5261f;
        coarseQuery(indexImageSearch,typeImageSearch,feature2,sim_t);
        fineQuery(feature2);
       featureCompare(indexImageSearch,typeImageSearch,feature2);*/
        testLOPQ();
        sourceClient.close();
        searchClient.close();
    }
}
