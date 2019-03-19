//package org.elasticsearch.plugin.image.test;
//
//import org.apache.commons.codec.Charsets;
//import org.apache.commons.codec.binary.Base64;
//import org.apache.lucene.util.BytesRef;
//import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
//import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
//import org.elasticsearch.action.bulk.BulkRequestBuilder;
//import org.elasticsearch.action.index.IndexRequestBuilder;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.io.Streams;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.unit.TimeValue;
//import org.elasticsearch.hashing.LocalitySensitiveHashingCos;
//import org.elasticsearch.hashing.PCADimReduction;
//import org.elasticsearch.index.mapper.image.FeatureFieldMapper;
//import org.elasticsearch.index.query.QueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.query.image.FeatureQueryBuilder;
//import org.elasticsearch.script.Script;
//import org.elasticsearch.script.ScriptType;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.sort.FieldSortBuilder;
//import org.elasticsearch.search.sort.SortOrder;
//import org.elasticsearch.util.FeatureCompUtil;
////import org.elasticsearch.util.TxtFileOperation;
//import org.elasticsearch.util.TypeConvertUtil;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.*;
//
//import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.scriptFunction;
//
///**
// * Created by Administrator on 2017/10/27.
// */
//public class PcaTest {
//    private static TransportClient client = null;
//    private static String clusterName130 = "lv130.dct-znv.com-es";// "lv102-elasticsearch";//
//    private static String transportHosts130 = "10.45.157.130:9300";
//    private static String indexImage = "pca-test2dim32-10000w";//"history_fss_data_v113_new2-000001";//"pcalee-test2-lee";//"pca-test1dim16-150w-lee";//
//    private static String typeImage = "history_data";//"person_list";//"test";//
//
//    public static void createIndex(TransportClient esTClient, String index, String type) {
//        IndicesExistsResponse indicesExistsResponse = esTClient.admin().indices().prepareExists(index).get();
//        if (indicesExistsResponse.isExists()) {
//            return;
//        }
//        String mapping = null;
//        try {
//            mapping = Streams.copyToString(new InputStreamReader(
//                PcaTest.class.getResourceAsStream("/mapping/test-pca-mapping.json"), Charsets.UTF_8));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        CreateIndexResponse response = esTClient.admin().indices().prepareCreate(index)
//            .setSettings(Settings.builder().put("index.number_of_shards", 10).put("index.number_of_replicas", 0))
//            .addMapping(type, mapping).get();
//        if (!response.isAcknowledged()) {
//            System.out.println("create index failed!! \n" + response);
//        }
//
//    }
//
//    private static void genPCAdatas(Map<String, Object> source, Base64 base64, FeatureCompUtil fc) {
//        String feature = source.get("rt_feature").toString();
//        double[] f_high = fc.getDoubleArray(base64.decode(feature), 12);
//        byte[] f_low = PCADimReduction.double2Bytes(PCADimReduction.generateLowDimensions(f_high));
//        // source.put("feature_16d", Arrays.asList(f_low));
//        source.put("feature_16d", f_low);
//        source.put("lsh_16s", LocalitySensitiveHashingCos.generateHashesAsString(fc.getDoubleArray(f_low, 0)));
//        source.put("lsh_16l", LocalitySensitiveHashingCos.generateHashesAsLong(fc.getDoubleArray(f_low, 0)));
//    }
//
//    private static void writePCAFromIndex() {
//        String sourceindex = "hy-lsh-testcos16-1yi";// "hy-es-image-plugin-test5cos10single";//"z-es-image-plugin-16single_6";//
//        createIndex(client, indexImage, typeImage);
//
//        SearchResponse scrollResp = client.prepareSearch(sourceindex).setFetchSource(null, "rt_image")
//            .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC).setScroll(new TimeValue(60000))
//            // .setQuery(qb)
//            .setSize(1000).get(); // max of 100 hits will be returned for each scroll
//        // Scroll until no hits are returned
//
//        Base64 base64 = new Base64();
//        FeatureCompUtil fc = new FeatureCompUtil();
//        do {
//            BulkRequestBuilder bulkRequest = client.prepareBulk();
//            for (SearchHit hit : scrollResp.getHits().getHits()) {
//                // Handle the hit...
//                // System.out.println(hit.getSourceAsString());
//                IndexRequestBuilder indexerbuilder = client.prepareIndex(indexImage, typeImage);
//                Map<String, Object> source = hit.getSource();
//                genPCAdatas(source, base64, fc);
//                bulkRequest.add(indexerbuilder.setSource(source));
//            }
//            bulkRequest.execute().actionGet();
//            // break;//hy test
//
//            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
//                .actionGet();
//        } while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while
//                                                              // loop.
//    }
//
//    private static void testSimLowVsHigh() {
//        FeatureCompUtil fc = new FeatureCompUtil();
//        String searchfile = "E:\\项目\\10-大数据\\1-test\\cos_test\\00001\\m.010bk0-103-FaceId-0.jpg";
//        // String searchfile = "E:\\project_test\\face\\BLASTICLIST\\test1.jpg";
//        // String searchfile = "E:\\项目\\10-大数据\\10-FSS\\test\\test-relation\\test-1-3.jpg";
//        // String searchfile = "E:\\项目\\10-大数据\\10-FSS\\test\\ms_cls\\m.0251y2-74-FaceId-0.jpg";
//        // String feature = PictureUtils.httpExecute(new File(searchfile));
//        // System.out.println(feature);
//        // byte[] rt_feature = PictureUtils.getFeature(feature);
//        String feature = "7qpXQsFdAAAAAQAAEBPxPAxp2Tuyrrc9H+jzvYr+Uzs0tpY9goenvLvpC77KJzC9EwShvBcGuTzsj6E9giTMvJVrSbvhk4c8jtHivGY/gz3VaGo62z5YvUvorj3t2KA9JRVNvZ7OJ7u6pHI9EYz8vDVDzLs3Aok96mAkvYvotb1fpoc9PP1ePYWXfD0YJqU9QYm8POiyh71ih/66jJk2vX3FrL2i3Ny7xxI6vOwtubyCmf+8I3ABvmGkzzxf5lg85Oe6OwgJIz2stxi9ZncjvUPnnL3YAUw7ni/Evbwymj1GuwI8e+p6ukMWXDwl/r89nMHIvCPGOj3BJ229bH+LvZqokD1USbc9YtsSvgD+WD2fVMi9FYy8vB5hqT20Yyy8G4MVvQwfT73QkmC6pdPIvdhjMDrGFRM9YHzhPUPKRr3cFpo9V3kDvInWPb1yEIe9nGxhvfz6DjycBbk8bdkCPMMGp73TK+q9/EpUvZ7rkzysJL+8YbhyPCdC8Dyq9eC9VTpNPFjQAb4CbZI9h9WgPCBEh7xgfUy+u9AWvT2f6rwoCCe9DdqOvGuWX7009BA918OCveO27jwGbO06RqIqPlA857v7RSI9joKkvVuW873HEEA9D/IDPcPUhL3/UHS9ECUIu1lvMr3/RIK8MoFgvQ3q1rwvZYE9d9sivZyvgD2554w8VDuePVHoFz2+3m+9wjs8vU47A76RNIC9LYFBPbLGHLyrOsC9BWIyvAOkvjzBV5M9PDIMvcFF3zyVZ4A9xS91PYzmhr2G+cA8PwpdPUBlfz0axdg9BBjWPZY6mLwI+SU77Z7BvVOHir2SdJS8V3GOOoPbnj0mUWw90xCuO5jgUjy1L6Y9pcu6vSOzl71T24K9m2WAvdGKMz1+sbm9RqEqPbrl6j3JDUo9qfJ8PfdwKb2rfjK9UxhdvBIJnr0uo109Ey6YPPWugT3jF8K88RhYvU1VMD33pW29HfSwveWq+r2/jTg8R3maPXy7KL1UBwA+UldzvS/nvj07b429Vq0pPE1+s70dHjK99DWxuTReobxP3rM8YtefPEYQgzwsL7q77tJpPKRqbDyPCfc81TR4PWtnLTyATdo9h173vBZeBr6F2y092mYvvYMOl7zJciw99iMnvkN5WT1YYCQ8ar1ePeca17s0qXm92HmJvSHYm721NRI93KO4vZnGGT0hkS+9h2klu0CjSL18LjW9Gd4VPdOeqbyNNE49GdRIPSqllb35vbo94mCmvd5mEj2DuRE9JC+VvdmXRj03jTk+LhbePFHUfzwhGc27LHogPeR3lrxXvs488I1NPd6eFb6pyCM9mqSAPSZfbr05eTk62t3YPRVOgb08puU8LF4CvWTcSb0Sxxg9HuHWPQ==";
//        byte[] rt_feature = new Base64().decode(feature);
//        double[] qfh = fc.getDoubleArray(rt_feature, 12);
//        byte[] qfl_b = PCADimReduction.double2Bytes(PCADimReduction.generateLowDimensions(qfh));
//        List<Double> qfl = fc.getDoubleList(qfl_b, 0);
//        String featureName = "rt_feature";
//        List<byte[]> featureList = new ArrayList<>();
//        featureList.add(rt_feature);
//        System.out.println("begin query 16 short hash and 32 dim by feature script, threshold is 0.6f.");
//        FeatureQueryBuilder imageQueryBuilder1 = new FeatureQueryBuilder(featureName).feature(featureList).hash("LSH")
//                .hmDistance(4).useScript(true);
//        Map<String, Object> params2 = new HashMap<>(20);
//        params2.put("featureName", featureName);
//        params2.put("featureValue", feature);
//        params2.put("lowThreshold",0.6f);
//        QueryBuilder qb2 = QueryBuilders.functionScoreQuery(QueryBuilders.boolQuery().filter(imageQueryBuilder1),
//            scriptFunction(new Script(ScriptType.INLINE, "native", "image-feature-retrieval", params2)));
//
//        // 0.9164/0.57403934 <-> 0.77731466
//        // 0.9089/0.5359223 <-> 0.64526945
//        // 0.8686/0.4735387 <-> 0.6132109
//        SearchResponse sr2 = client.prepareSearch(indexImage).setTypes(typeImage).setQuery(qb2)
//                .setMinScore(0.5261f) // .setExplain(true)
//           // .addSort("rowkey", SortOrder.ASC)
//                .setFrom(0).setSize(10).get();
//
//        long totalCount2 = sr2.getHits().getTotalHits();
//        System.out.println("totalCount " + totalCount2 + ", costtime " + sr2.getTookInMillis() + "ms");
//        String filename2 = "test3pca16-0.8651-esResult.txt";
//        /*for (SearchHit searchHit : sr2.getHits()) {
//            System.out.println(searchHit.getSource().get("person_name"));
//            System.out.println(searchHit.getScore());
//            // 写入文件
//            // String content = String.format("name: %s, score: %f",
//            // searchHit.getSource().get("name"),searchHit.getScore());
//            // TxtFileOperation.contentToTxt(filename2, content);
//        }*/
//
//        /*System.out.println("begin query 16 hash and 32dim by hash hanming distance script, threshold is 0.6f.");
//
//        Map<String, Object> params3 = new HashMap<>(20);
//        params3.put("featureName", featureName);
//        params3.put("useHash", true);
//        params3.put("useFeatureComp", true);
//        params3.put("featureValue", qfl);
//        params3.put("hashDataType","String");
//        params3.put("hashValue", LocalitySensitiveHashingCos.generateHashesAsString(fc.getDoubleArray(qfl_b, 0)));
//        params3.put("hmDistance",4);
//        QueryBuilder qb3 = QueryBuilders.functionScoreQuery(
//                scriptFunction(new Script(ScriptType.INLINE, "native", "image-feature-retrieval", params3)));
//        // 0.9164/0.57403934 <-> 0.77731466
//        // 0.9089/0.5359223 <-> 0.64526945
//        // 0.8686/0.4735387 <-> 0.6132109
//        SearchResponse sr3 = client.prepareSearch(indexImage).setTypes(typeImage).setQuery(qb3).setMinScore(0.6f) // .setExplain(true)
//                // .addSort("rowkey", SortOrder.ASC)
//                .setFrom(0).setSize(10).get();
//
//        long totalCount3 = sr3.getHits().getTotalHits();
//        System.out.println("totalCount " + totalCount3 + ", costtime " + sr3.getTookInMillis() + "ms");*/
//        String filename3 = "test3-f128-hm4-esResult.txt";
//        /*for (SearchHit searchHit : sr3.getHits()) {
//            // System.out.println(searchHit.getSource());
//            System.out.println(searchHit.getSource().get("person_name"));
//            System.out.println(searchHit.getScore());
//            // 写入文件
//            // String content = String.format("name: %s, score: %f",
//            // searchHit.getSource().get("name"),searchHit.getScore());
//            // TxtFileOperation.contentToTxt(filename3, content);
//        }*/
//
//        System.out.println("begin query 32 dim by feature script, threshold is 0.6f.");
//        Map<String, Object> params1 = new HashMap<>(20);
//        params1.put("featureName", featureName);
//        params1.put("featureValue", feature);
//        params1.put("lowThreshold",0.6f);
//        QueryBuilder qb1 = QueryBuilders.functionScoreQuery(
//                scriptFunction(new Script(ScriptType.INLINE, "native", "image-feature-retrieval", params1)));
//        SearchResponse sr1 = client.prepareSearch(indexImage).setTypes(typeImage).setQuery(qb1).setMinScore(0.5261f)
//                // .addSort("rowkey", SortOrder.ASC) // .setExplain(true)
//                .setFrom(0).setSize(10).get();
//        long totalCount1 = sr1.getHits().getTotalHits();
//        System.out.println("totalCount " + totalCount1 + ", costtime " + sr1.getTookInMillis() + "ms");
//        String filename1 = "test3-f128-0.5924-esResult.txt";
//        /*for (SearchHit searchHit : sr1.getHits()) {
//             System.out.println(searchHit.getSource().get("person_name"));
//             System.out.println(searchHit.getScore());
//            // 写入文件
//            // String content = String.format("name: %s, score: %f", searchHit.getSource().get("name"),searchHit.getScore());
//            // TxtFileOperation.contentToTxt(filename1, content);
//        }*/
//
//        System.out.println("begin query 256 dim by feature script, threshold is 90.7%.");
//        Map<String, Object> params4 = new HashMap<>(2);
//        params4.put("featureName", featureName+"."+FeatureFieldMapper.FEATURE_HIGH);
//        params4.put("featureValue", new FeatureCompUtil().getDoubleList(rt_feature, 12));
//        QueryBuilder qb4 = QueryBuilders.functionScoreQuery(scriptFunction(new Script(ScriptType.INLINE,"native","feature-comp",params4)));
//        SearchResponse sr4 = client.prepareSearch(indexImage).setTypes(typeImage).setQuery(qb4).setMinScore(0.907f)
//                // .addSort("rowkey", SortOrder.ASC) // .setExplain(true)
//                .setFrom(0).setSize(10).get();
//        long totalCount4 = sr4.getHits().getTotalHits();
//        System.out.println("totalCount " + totalCount4 + ", costtime " + sr4.getTookInMillis() + "ms");
//
//
//    }
//
//    private static String getFeatureValue(List<Double> floatFeature) {
//        StringBuffer featureValue = new StringBuffer();
//        String featureValueReturn = "";
//        try {
//            featureValue.append("[");
//            for (int i = 0; i < floatFeature.size() - 1; i++) {
//                featureValue.append(floatFeature.get(i));
//                featureValue.append(",");
//            }
//            featureValue.append(floatFeature.get(floatFeature.size() - 1));
//            featureValue.append("]");
//            featureValueReturn = featureValue.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return featureValueReturn;
//    }
//
//    private static void testPcaComp() {
//        String featureh[] = {
//            "7qpXQpFlAACAAAAAPVycPZmMgL2YTi499im4PfWkZDxoriW9/hTOPAfOarzV4Z69M1B1PS7OzbwX4rE98hPJPRQISr0c50C9nDgHPcmXW74dCD260wueve1EQz0CtKO9LgdhvaHoHL4Njqc9rYsUvgvOpD0TQIE9PwSdvHpRybyulKK99w+dPU/i+T0nBdI9biK+vfTElr3L+OI9HFr3O3RhXD2REoO9Q2AVvTEb6ztJ6PA9LoQhvs386z3JqoO9ZwvLPTmbRr28f2K9msKAveQPmj2cbxk8d8mrPbqrVT7rF2y8Bks+O41W4rpvPa2774IcO8/M1L0G6rA6cUzWPa5ror2SNy49Am4iPh4SDL2rys67AK0GPv2nkz04h9O9KnX2vQ0u+D2g/zk+FX6rvXyMsD2UA7O9gqpTvLTYpj08bDS9wVQsvQI+Qj0eF9E9CmOLvcW9/jwuKAQ85yg2vaSmDL5IgJm8VurRPHVbzTxOU8693AvpvYBpnjyIQYi9o+rLO78iPz6W/Dg9QCkGPS4mIz6Z2Aa+JIiAviTVML0ELso9K8eJOwRbCb7ZH7e9xB55PYNfujyBa3A73OMjPBDrpjyblvs9N9n9vE8UAD7cBgE9rhk1PVEXcD0FvP49D2Mfvp3mhLxDHuY8wGALPgeIcz3p3cU770rsPbNZLL6vSM287/M1PvnoiTw=",
//            "7qpXQpFlAACAAAAA7Z+lPCiQKr5Am5a9aAUWvJMOgLw5wMW9C7qePI5aMb5hQ9S5QbvyPXR70zyDI9Y90j7oPeBNBz0NRwC+cjanPQ9CRb7zJu08Mk3+PWLilz3eXMa96dNQveIAmb2hNRG92Dobvpc7djxJOX88w968vELpiDukQKU9lahfPUcC0b133hO++miAu3jOnj3cEW26lP5ouz4mrTwJ0uC6UJeOPFhKVTuupgA+sRkOviACAT5tbhu+6HE/PRVgZL2rnL88A4g3PVI0rD206gM9P+Agvf78hj1yRE29DmlvvWRMfT1d/EM7W90RPVNFbL4aZZI9O0xwPbU5yL3/HR89H0cYPs42hD0Ldl+7XSU2PkIDtr1KED+9adJivc8TOz43Qj097t9VvrJGvz0iB/S91/JtveCAY70zNk258DOJO+Zwxj2Ikgw+1q5ZPWH/ED22jHG9pQtPvQOvJL3SKQ08c4UmPV1zKLpVYFK9CwXYPMFMnL28ECC9rRQSvScADz7BLGM+DPuPPYr7bT0uZ/y93oWHvKrP1L203pM9r9ElPRIJ3L3/26I986rZvRvo/b0WAyK9iMYhPTtkkL1F4Qa9ldSQva5pMT3c7YQ9Q0zAvTAwGLw/iLC9foI7vXCuKD5+4988GnrEPWMvBjxjYp698tbDvcyJKb6UibI9IgP0PatPsL0=",
//            "7qpXQpFlAACAAAAAx6dJOyTLzDySbYW9tDL/vCZNlj0/2ke96Q3uvSrcj7xzqCs9EQqYPfvSnL1msJg7aSOOvSKrND2u2rA4h30RPkCJOb54BwU+bE8Uu3y5Bj4beZw5Y9FYPXqh27z2yh68jHMgvty/8D2sKYQ9zeCePGX5LL0jhE+7ASCsPEEkHjxRNAi+Gr51vYUXFL2X14I9KeiwvVINMr3Lfr49/iDtvWxtPT3LYB4+Ge/HvQq42z3Vm/G9/e0QPkzNA7r+I9U9QFPRvXbKjrwBLKM92YG7PEIeGT6O8LG8DxfWPQDyfroq9p28Ahv1vc5HT76ruYE6FQsTPtvjlzudeMM9QmW1PY8P8T2C+AI9lYMbPosCwjwMxGq92fcOvoOBMD5A8xY+AMX+vXMg27vo+Iy9/fJKvdRBFb1nSvW7FGWJPUkUGj5bpoY9e9ervcgnCLy91iS9nF98ve7h1r2LmpA7cGnEPQ9Mdj1fXp694uB5vTyP1b2aFTK9kwjHu+tPST7tXm48Bq0cvMr9lD2rzwu+CMvrvaq5uTz66qa8Q00GPlknyr3lSXG8uqIMPYVW7LxlM7y9M1qDvYJ7mb0/MOc8P2IUvsTsZ723Ln49iq/ovVdItL3kYCu+p08Pvlsf6D1v+5O8tKiJPYbV9z0rEga+CH7YPRoJhb2oDAQ9CeWvPZaQ+Lo=" };
//        String featureq[] = {
//            "7qpXQpFlAACAAAAAi59lvVlTnr0OB428bN3WPcVemj0k/oK9TyAKPSwlkr0xy1m9EnixvCtig72EqBI9OufhPbQsgL2rume9/wMIPOgcYL7CCIE9TyDJvUyF2T1Lc+W9xHqove7VFL4oGh+8/uq4vX1kyz1hGI88518kvefZkD2BISe+MItPPfuh2TzB1mE9U/2yvbK5Ab5baiM+MfPNPGPP+z3Snka9M+iWvLG+Lr18Q909K4DkvZqBpj03qMu9YEglPRB5nb2lYdu8qknjvX3gSD1HbpM9qpsLOjX5PD7sMRo9NSsivac7Br04agm+jv6uPJJPB77n1pO9MdxGPoXqAL2KFCQ92XC4PdLSoLxhrFg82VT5PSe7Z7zPVgO8JxsTvkL/KD5US9c9mDjcvc2EYTx7IQs9k4aFPLHlJ7ziIj+9/PZEvCijfj2FILw9Y7oCvlV0orwXh7Y9MR+bPaVDGL0N0vm8sYh6vXkcXT075Ba+3g61vI1dZT10Tq08iSpOPdqTSD7U8B0+8L+hvGzWmz32ghG+gmRnvtmqH71kFeA8R390PcaZJ734FsG9cNf6PQ/lB72ZT9M8lK0QvZNKHb3e/7Y9yTPIvaBvIz4WE4M8cmYzvZS5ED2BrOM52I5ovbXsGz3UcAQ934D2u18L3j0akFS9FnAWPnxI6L3VjOU7bf5nPiXSmLw=",
//            "7qpXQpFlAACAAAAAHvEovFXKUj0H0sO9Uj6tPWeOij3RpxC9YHqovVYjCr5bwMa8cq6OPT4fKb3lTpM9WKS2vNywHr3Q2VU8V/ruPXYSo71QtcM9kBWzvdzDej07Axc824R4vQSR0732wsw8HrXTvYfndrz5iO48tbaWvbsyNL2EwMG8hfoNvPRSlD2e4189IF2EvDI0vr0Ez+g9lohPvelvYz23qQo+9gIhvRWWYLxyeR0+oK7JvdaiKz5Aaje9gQuJO0fzVr24kII8dPjXPCEc/Lpf0US8NtPJPPGWHz6xoF49R+i9PcW/5L1sXZG9DnXauiPATr6+pfe8YcHDPcqe770fGow7cBYZPt0QHb39z4K9CNkLPsDKCD2TgsG9J4iAvpp8Wz60lyA+Dbc3vtbCMz1nQjK95AvdPNTbaj2Xvp67GPYIPby37LwtBHI98oe0Pe6PVr0he628WZXcvIS9eL0KPP48TFKePQUpuD2WIQe+anARPRoBpT3JEq+9IbLkPHl+PT6DlQU+mHWCPR4s8jxqkyi+S9xivpqgHr03xzE9Fgq7PR55771vWOK9y7aWvMITjL39t7W8GLGUvETY6jq7ofo9Ui+rvDgRpz1Az4k9qTttvXFN6bzx2hG8Rokkvi0MBT2a+pK9lbqcPd+zrz0qkZ89vMDgPVC6ML58txi96ugJPpj9Vrw=",
//            "7qpXQpFlAACAAAAAj/3ZvHaPO7y3Yoi9gORaPCJMMDxJdYo8wXFhvQYxd70U2zk9UAbBPVTkZ72/b4i8hNrAvK8x2Tz0FyC8ZEIHPaEaTb6/8tE95ilZvTv+HD49TBE8FhH2vNqnyL27/iG7Kr8Mvk0Z1j1D6kY86s8fvDL7TL0E81+9mzcbPMmNeT10E0G9I56EvRjoi70jcMI9412zvbdz1LyKAX88q5Q3vWp8v7oA6h4+YPXTvT76Rj63Y7a9qpKAPbGQMr08T+g8DgBRvR4/orzmZyQ728OjPGxhOz5bmEU9xV3RPaILzrwqfqu9cHQUvG+JXL7/0LY9/PIBPnaWHbygB5g9PXcgPh1XNzxBG38803cpPt0jIr0p69S9GUuMvooBOT6b0D0+DE5Wvke0mjy9kom9+bqaPI4/Ob1JkBG9Rsl9PHKb1ryBtxU9mCzfvMYFT7vpgBW9PDe5vS3kzL2nqp48hrTTPYckuz1Iwuq9fH7eu+/IRz0j12+9XgQBPfrcKj5qOQs+PqM9vW2zyD1ojAO+uDs6vvUF7byXE/q8JM4EPuxdZruEgAw9/siYO81mjL3Wem29wdeFPBLtQTy0n8k89F/wvUD6SDzX2G09onrWvJG+y71tQZ+9PuznvekBGT5Kl0u9HvWAPWXomD0QKhC9IZOAPaFj5b3IIPw85UM9Ph1x/bw=" };
//        String feature2[] = {
//            "7qpXQpFlAACAAAAA9LDYvPz5+Lys/pi90HWPPUjJoz0sqJQ8JrEMPeQEdD5mVe69HmtVPj0jCTw0vLy9sR81vEw1272qpOK7K3myPSdGK7rIJK09ELLhvBtExL3OWdc9zH/LPGp+njrdVtY7lhwbPdrvz73w3HQ9hkwHvW70EL2+4K29QIxLPXOJAT2nxts9oRkdPYn4Rb0YHJS9FFh0PDNcnb1vihI+nvrFvBYpKT2QyOM9US7bvV3cFj4FkQS9eYmhPR/NXrrW6iy9cGEOvj0qob3xs1W90SmPPXV+1zyeoxu8xI9Vvhf0Mz5sNR88zLTKvXgbAD3g3Pc9Ud2HvtLjPj6tqNI9kRcYvtZbn7xHxuk9NXj9vQV0Eb3wG9A9RCFvvrM22j0aGAq+ATkFPS78Ibxr/mO8+dCVuw04F748FHC99fOCvcdGKb06GK29BxlpvVP5nT2Zg468kBfOvNOioz1ewuG9Ry8GvhsgrryH25e9xIoOPhpPSTpQ0TU+dvNTPeNZ1D1CYXU7hnpVPFHj6DygjhC7WCd9PckFZD3Hoa69uoZAvWJTwr3V7cM9QTFwvMtxwT34lvC9FebOvKbCJ71tFYc8+inEPS9UqT3ko0W9IG9cvcavBj43atg8bsMOvDq1vj2/0oy9yhJ8PRLMADz/c189H7YavesBDD0TjBm916z5PPCZ3Ts=",
//            "7qpXQpFlAACAAAAAOZmOPd3JfLxVzvK9FEECvbNXh70wDgO+lQIHPW4INr3kBy29RW0HPg99N7sZyFo9Ak/FvU1DfT2cNSk9yTibu+r7Db7zWYm8oVj1PSLnKj4YpSs8N7BdPYC+vLxL7AI+IaitvSi0ZD2aiUA9Yh4fPeKBpD1c/fG9j3kZPTmw07wCFoi9RxLIPACRv70wvYo97C4UvqQMO7yW2A298PpqvWeMyL0ET/88laKCOzD1F71wOq+8r/8MPCzV5TveL3g9l1W/PWQHUj5dmB0+3ubBPEFJWL2PruA9uqoMvV/Gv7vnUoo+y7nCvc71xryyZ8s9qBLVvOfnZz13W6c8ImfSvZmpgTyOilK+FUNtPVmpUz2xaUi7OMBdvQXdnL1Imlc9ViAFvkYJU77CaeK64fTCvSgtxj1DU/q9VnoSPP+G8zsBKgS9kmoDPGWfsz2NLqo9IQDwPE8BMb54Wfi97SQePh0zHryXIoo9Tc2vvV+lzD0zSCc+Xc8FvvmYZr3+DZ08zFlePPOzRL3s2Xa9xrm6vXZhBT7yeqS9pNwePZ2x/j0oNOw9v3R1PfmXer0yt5E9L9rwvQVyMr13AEE9MCHsvZrW6zzUe0K9/0lbvoa6xL06zAS9vzWtvbm0ib3N6Ny9FPy2PSdhQL1/jKe9zigNvi+n97xa+6U7NTXwPNudZL0=",
//            "7qpXQpFlAACAAAAA9gCnPX+tiT27vJW85N0VvUPXAL5OgvQ7qRI/vWUtRb0XIIy9EDuTPZB61j2kzK69ZnXQO8LEsDrxsa295nKXPGmH1D0FNoG9vC5+vaOPo72mmTc+YwcJvYA4pb3Zt5U85p6uu7lukD2iB/I9d6fXPcQSF70swha+N6t1vdbWib0WJRG+NxSPPF1EHT1BEoE9ytBdPGdJMr72ZiE99RcNvUBfdz1W7h4+Qh05vYbqzjwE9C++kYH0PGRZzb3GKFu9crQdPrrHXz2n1tc9vvfIvKAkm72THJy95zsTvn1yvrwFYJM9j3bUPNpk8j3QBLg8GZ7BvYGGT73zxS89iV4XPk/Eqby1K+28CdPDPS1WyL38UoI8Hk6OPcOZiT2tXZm5CavHPbiskb0LODc9gA0jPFptuT3YbYe9LjSvveqbJ72qas09d0FMPbclIT1agIC9yhg3vbBUSz36jTA+ikz7vFaKsTvZOU69CK+uPR3xGD6CbgW92FgXPmfvvro5dNK9oXWKPZnBEL4/E0M+KhggPSBVlTqJjqQ87q18vfwtED1NU5m90Ia+vZA9jzvIZ0Y8L0yGvKaJ7z2Gf4M995QIvpNu/zzhwue9Fjd8vcOlKL4ypBI9aHsxvi97FT7Kvde9IPS/PUI/oT2ZjSo+q0gKPc67njssVFu+EtZEvgh+lr0=" };
//        FeatureCompUtil fc = new FeatureCompUtil();
//        double[][] f_low = new double[3][16];
//        double[][] f_high = new double[3][128];
//        List<Double> floatFeature1 = new ArrayList<Double>(16);
//        List<Double> floatFeature2 = new ArrayList<Double>(16);
//        List<Double> floatFeature3 = new ArrayList<Double>(16);
//        long[] hashes = new long[3];
//        for (int i = 0; i < 3; i++) {
//            f_high[i] = fc.getDoubleArray(new Base64().decode(feature2[i]), 12);
//            f_low[i] = PCADimReduction.generateLowDimensions(f_high[i]);
//            for (double f : f_low[i]) {
//                if (i == 0)
//                    floatFeature1.add(f);
//                else if (i == 1)
//                    floatFeature2.add(f);
//                else if (i == 2)
//                    floatFeature3.add(f);
//            }
//            hashes[i] = LocalitySensitiveHashingCos.generateHashesAsLong(f_low[i]);
//        }
//        for (double f : f_high[0]) {
//            System.out.print(f + ",");
//        }
//        System.out.println("");
//        byte[] f0 = PCADimReduction.double2Bytes(f_low[0]);
//        double[] fb0 = fc.getDoubleArray(f0, 0);
//        for (double f : fb0) {
//            System.out.print(f + ",");
//        }
//        System.out.println("");
//
//        long xor = hashes[0] ^ hashes[1];
//        System.out.println(fc.cosDistance(f_low[0], f_low[1], 0) + "," + TypeConvertUtil.longOf1(xor));
//        xor = hashes[2] ^ hashes[1];
//        System.out.println(fc.cosDistance(f_low[2], f_low[1], 0) + "," + TypeConvertUtil.longOf1(xor));
//        xor = hashes[2] ^ hashes[1];
//        System.out.println(fc.cosDistance(f_low[2], f_low[0], 0) + "," + TypeConvertUtil.longOf1(xor));
//
//        /*System.out.println(fc.Dot(f_low[0],f_low[1],0));
//        System.out.println(fc.Dot(f_low[2],f_low[1],0));
//        System.out.println(fc.Dot(f_low[2],f_low[0],0));*/
//
//        System.out.println(fc.Dot(f_high[0], f_high[1], 0));
//        System.out.println(fc.Dot(f_high[2], f_high[1], 0));
//        System.out.println(fc.Dot(f_high[2], f_high[0], 0));
//
//        System.out.println("hbase: " + fc.Comp(feature2[0], feature2[1]));
//        System.out.println("hbase: " + fc.Comp(feature2[2], feature2[1]));
//        System.out.println("hbase: " + fc.Comp(feature2[2], feature2[0]));
//
//    }
//
//    private static void testLowSimVSHighSim() {
//        String path = "pcaSimresult2.xlsx";
//        String name = "pca1";
//        List<String> titles = new ArrayList();
//        titles.add("id");
//        titles.add("sim_low");
//        titles.add("sim_high");
//        List<Map<String, Object>> values = new ArrayList();
//
//        FeatureCompUtil fc = new FeatureCompUtil();
//        String query_fh = "7qpXQsFdAAAAAQAA4rWzPLEU87udp0E9KuQgvStiZLyP0+s7RR1jvV98872dLOi9aMbevGAZJD0uKNm9LsXjvKM59j3HJ/o8x/O0vI+HMr3asRA9EQaivVTzeD1+YgA+F8aWPaMjo7pya5G9vq34O8C3ujwfZXm9QOhTvXJA6T1Q1e88deSPvW0pJj272588fsLUPX0TuL0qfww8FDKVPD6u4LytQyO6FBOyPQVxtTwBkum8khScPa9fQb2VBgC85VxwPL+Qgj0AGkG9dq0NPdUDAr5jA1Y9jPyePdAcyr2zNxU9NbMxu0/gaj2nCI+7UEe5vLGggrxR1+Q8S9tHvVmoozxfqW09lz+LPeZrhj3qzBS7e/QGu6WNeD2ltDw6MW5FPTxB/z1YOUy9uYXJPKyrnjz9igW779nkvYTYT7yrYOI9mGU/vefDyTypjp492c5PvXCXWDty40c9t2qxPdg1Qj2flPw9Tio9vP9xD7ugURI928WOPYaWG76Isj89ZvKWvMUdzz2u16i9GeXFvIi9NL0B+o68KXACvmynYrzqWaI89QhYPVyYsr3AAO29zgvgPL+KgD3SULy6ey/wO53H/z0gUqY9zW8wPbmLl7z6d1U9FewePHADpbuMdru9mgA+vbOE5D1DCjE9qEhoPdgsMr3w7tG9HAxOPVT94zw4j9A8Wg1tPY3Tuz3AmEU9yJuxvHGkbbwpPu+7NbYBvsz/iL2b86E77RPbPMrBdT1euSK9f7vKvbG1Rr0Sz+S9hgsvvBpRAz0S2Ka9IoNmvSvpJj3iwiU92tQOPZLtJLf0vB+9dG/evTcBt73P1XO8WLHNPFlHrz1Bq+88HoeEvYG9A71jUTY9U/aOvVuPIb16eqY8xX1cPEBIjj2+zKC9W//YPWNh8D1E/BS9z91ZPY1SYbwEsY48amAxvUvGBrwseqG7+O6rvXKT4b2zA9E9dd/iva1VND0ZdIY8p0iePa/RDL7pqIu9PELCOX75hL3jf7M8mahSO7hOzrgoC/48vMOwve3+bL2RI/u9SdlivcpOAT3/3MI9H1FuPNwkor2qWg298h0MPUaz9zxUH3o9YEJHPUBt4zubh8O8yfQEPOigGr71Vvq91XaVvNUJJ70pf4u96kuavQfMT73hz0Q9S4nzPGWrAjyKh5W8Xl/wPR7hP763uYU8Tc4JvXmelr3iw/U9yTLBPCwCDL035NY8mc95PXPm2bzzyM88r7U/vc+oP71zdfy8938Nvf4dCjy6qM4911rgPNXGLzxxpO88WKaRvAVDkD01GYm8RGsrvOP2wDxUH8U9FDpPPYxFhL1EVCM89RSYPYN3KL2Uqzq97q3ZuV+0qzxzcwY9iCnPParNlb1+MAM96GyPvQ==";
//        byte[] qfh_b = new Base64().decode(query_fh);
//        double[] qfh = fc.getDoubleArray(qfh_b, 12);
//        byte[] qfl_b = PCADimReduction.double2Bytes(PCADimReduction.generateLowDimensions(qfh));
//        List<Double> qfl = fc.getDoubleList(qfl_b, 0);
//        String[] incs = { "feature"};
//        String[] exinc = { "feature2", "feature3"};
//        SearchResponse scrollResp = client.prepareSearch(indexImage).setFetchSource(null, exinc)
//            .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.DESC).setScroll(new TimeValue(60000))
//                .addDocValueField("feature."+ FeatureFieldMapper.FEATURE_LOW)
//            // .setQuery(qb)
//                .setFrom(100).setSize(10).get(); // max of 100 hits will be returned for each scroll
//        // Scroll until no hits are returned
//
//        Base64 base64 = new Base64();
//
//        do {
//            int i = 0;
//            for (SearchHit hit : scrollResp.getHits()) {
//                Map<String, Object> map = new HashMap<>();
//                // Handle the hit...
//                 System.out.println(hit.getSourceAsString());
//                Map<String, Object> source = hit.getSource();
//                BytesRef f_low = hit.getField("feature."+ FeatureFieldMapper.FEATURE_LOW).getValue();
//                String f_high = source.get("feature").toString();
//                float sim_h = fc.Dot(fc.getDoubleArray(new Base64().decode(f_high), 12), qfh, 0);
//                float sim_l = fc.DotAsDouble(qfl, f_low, 0);
//                map.put("id", ++i);
//                map.put("sim_low", sim_l);
//                map.put("sim_high", sim_h);
//                values.add(map);
//            }
//            Write2Excel.writerExcel(path, name, titles, values);
//            break;// hy test
//
//            // scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new
//            // TimeValue(60000)).execute().actionGet();
//        } while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while
//                                                              // loop.
//
//    }
//
//    private static void testLsh2Excel() {
//        String path = "pcaSimresult3.xlsx";
//        String name = "lsh";
//        List<String> titles = new ArrayList();
//        titles.add("id");
//        titles.add("sim_low");
//        titles.add("sim_high");
//        List<Map<String, Object>> values = new ArrayList();
//        FeatureCompUtil fc = new FeatureCompUtil();
//
//         String searchfile = "E:\\项目\\10-大数据\\10-FSS\\test\\ms_cls\\m.03grfv6-91-FaceId-0.jpg";
////         String feature = PictureUtils.httpExecute(new File(searchfile));
////         System.out.println(feature);
////         byte[] rt_feature = PictureUtils.getFeature(feature);
//        byte[] rt_feature = new Base64().decode(
//                "7qpXQsFdAAAAAQAAtJTLvH+kyb3OfeC9hzGMvUOxOj1zW709MuNiPMl51rsDirq93n4IO+QX3jwCxLa81tnuPZzuiL3WgkE7pTUwvbSMAD5VO6u98kAUPIIRNL2NVSi94VSAPeXv7byfvAO+1OFFO9KOEbx3Vtm9s+CZPY2FFr7chqY8sfYnPTg04731O4S9q5YjvXqiKb261qs8AxKMvUYHEr115EK9PmCevYGyFb2ROIe91DoLvcF/wbwXOpG9qLabPc0/wTx+Hoo9LAq9vMd64TwTTeI7MHcQO0BOsTytjIe9UbTEvSMmtL0gLfU9AyHuPcBwMT7MfDI9ZjOPvaZEizscz6e7NKXAPQpCo7zRioM9c5rbPdOXyL2BuNI6U6MPvkcon73Pk9C8jzOUvXlKSj1cUUS9rHuCPLfVJj1FeKw9JqVRvPRQ7z0lGjg97GmSvWsKlj3i4au8gNA4vFyBWr2ILWU8/wKIvA3UajxXIbc8kLyzPI3yJb2ze4q8WFiAPTg3Sz2urmg8nqMzPMJxn7xpppG9ldr9O+7+pTzxRR49LHpsvf4vk70B16A8xXZkvEwAB7vVsUM94NmSPTRtCTtAaAe9ke6rvDwgUzk5bHi9yQr0vESzgj1SPPE9752KvT4Xw70drXS9nPrlPBIfpbUOXeG8yCb0PHjgLb0fhFs86byGPW9Y2Dukg7k9jh9APWQQp70tk7O9QBqNPCvRX72Qhkw7LUBIvcp2orxVfCU9wn3IPOt8kr1me/88xTddvUtR6j2XijQ9L4thPGdWBr61UBu9V3AUPmer4DulfSg9Tu2UPYHffr2uRJM9jECePQtZUr2Oy6k9Rv65vBDcub2u9Lu8wtLQuhuLBj34YoW9IutJO0OqmL3IhxK+xsxFvVBZUj3y3ws9nM/sPY6vnz0iXy88lrERPSKPAjqaNGe9TleLuw9KQLzhNio9JPeEPAQTkr2udh89eQbAPSG3aDxzIdm98ZTtPfURODuE7Aa8L/4Mvd7Zjj0BBHw9LTyYPf5mhr1tYTS7uuGvPUHT5r2M/Lu8N2TCPQBS6rxOl0E9aTlzvVWTpD3YmIW8Sa4FPlQJB71VWQq9l3SiPKThdrzbtEU9bxQ4vaFJDj3fjpk880blvJCznLztlYm9hQ+GPQplaL3B5Qu+wnVJvVtQHb00viE92xHpveAavr2P8as8rOAFva0k5jvzJyM9UdsMPU0MgjxnPJu9SiYLO4ysgT0UR+k9p/3xPPyezD3p6ai9K1kbvSEbFb5Omj+90eAIPABo5jy8qxe97+K7PNaEiL0dZVq9c3L8PUOjnTrmuwq9FHyCufulFb3N+PY8+eKHvfCCgz1Lygo9jLYNvS5TEL3M5xo9XBR7PQ==");
//        double[] qfh = fc.getDoubleArray(rt_feature, 12);
//        byte[] qfl_b = PCADimReduction.double2Bytes(PCADimReduction.generateLowDimensions(qfh));
//        List<Double> qfl = fc.getDoubleList(qfl_b, 0);
//        String featureName = "rt_feature";
//        System.out.println(getFeatureValue(qfl));
//        int hashvalShort = (int)LocalitySensitiveHashingCos.generateHashesAsShort(fc.getDoubleArray(qfl_b, 0));
//
//        Map<String, Object> params1 = new HashMap<>(4);
//        params1.put("featureName", featureName);
//        params1.put("useHash", false);
//        params1.put("useFeatureComp", true);
//        params1.put("featureValue", fc.getDoubleList(rt_feature, 12));
//        QueryBuilder qb1 = QueryBuilders.functionScoreQuery(
//                scriptFunction(new Script(ScriptType.INLINE, "native", "image-feature-retrieval", params1)));
//        String[] incs = { "person_name"};
//        String fieldname = featureName + "." + FeatureFieldMapper.HASH + ".LSH" + FeatureFieldMapper.HASH_SHORT_SUFFIX;
//        SearchResponse sr1 = client.prepareSearch(indexImage).setTypes(typeImage).setQuery(qb1).setMinScore(0.5261f)
//                .setFetchSource(incs, null)
//                .addDocValueField(fieldname)
//                // .addSort("rowkey", SortOrder.ASC) // .setExplain(true)
//                .setFrom(0).setSize(1000).get();
//
//        System.out.println("totalCount " + sr1.getHits().getTotalHits() + ", costtime " + sr1.getTookInMillis() + "ms");
//
//        int i = 0;
//        for (SearchHit hit : sr1.getHits()) {
//            Map<String, Object> map = new HashMap<>();
//            // Handle the hit...
//            // System.out.println(hit.getSourceAsString());
//            long hashShort = hit.getField(fieldname).getValue();
//            int xor = hashvalShort ^ ((int)hashShort);
//            int hm_d = TypeConvertUtil.intOf1(xor);
//            map.put("id", ++i);
//            map.put("sim_low", hm_d);
//            map.put("sim_high", hit.getScore());
//            values.add(map);
//        }
//     //   Write2Excel.writerExcel(path, name, titles, values);
//
//
//        Map<String, Object> params2 = new HashMap<>(20);
//        params2.put("featureName", featureName);
//        params2.put("useHash", true);
//        params2.put("useFeatureComp", false);
//        params2.put("featureValue", qfl);
//        params2.put("hashDataType","short");
//        params2.put("hashValue", hashvalShort);
//        params2.put("hmDistance",4);
//        QueryBuilder qb2 = QueryBuilders.functionScoreQuery(
//                scriptFunction(new Script(ScriptType.INLINE, "native", "image-feature-retrieval", params2)));
//        FeatureQueryBuilder imageQueryBuilder1 = new FeatureQueryBuilder(featureName).hash("LSH")
//                .hmDistance(4).useScript(true);
//        String[] incs2 = {featureName};
//        SearchResponse sr2 = client.prepareSearch(indexImage).setTypes(typeImage).setQuery(qb2)
//                .setMinScore(0)
//                .setFetchSource(incs2, null)
//              //  .addDocValueField("feature."+ FeatureFieldMapper.FEATURE_LOW)
//                // .addSort("rowkey", SortOrder.ASC) // .setExplain(true)
//                .setFrom(0).setSize(1000).get();
//
//        System.out.println("totalCount " + sr2.getHits().getTotalHits() + ", costtime " + sr2.getTookInMillis() + "ms");
//
//        i = 0;
//     //   values.clear();
//        for (SearchHit hit : sr2.getHits()) {
//            Map<String, Object> map = new HashMap<>();
//            // Handle the hit...
//            // System.out.println(hit.getSourceAsString());
//            Map<String, Object> source = hit.getSource();
//            if (source.get(featureName) == null) {
//                continue;
//            }
//            String f_high = source.get(featureName).toString();
//            float sim_h = fc.Dot(fc.getDoubleArray(new Base64().decode(f_high), 12), qfh, 0);
//            map.put("sim_high", sim_h);
//            map.put("id", ++i);
//            map.put("sim_low", hit.getScore());
//
//            values.add(map);
//        }
//     //   String path2 = "pcaSimresult3.xlsx";
//        Write2Excel.writerExcel(path, name, titles, values);
//    }
//
//    private static void testHighSimVsLow2Excel() {
//        String path = "pcaSimresult2.xlsx";
//        String name = "pca1";
//        List<String> titles = new ArrayList();
//        titles.add("id");
//        titles.add("sim_low");
//        titles.add("sim_high");
//        List<Map<String, Object>> values = new ArrayList();
//        FeatureCompUtil fc = new FeatureCompUtil();
//        float sim = fc.reversalNormalize(0.907f);
//        System.out.println(sim);
//
//        String searchfile = "E:\\项目\\10-大数据\\10-FSS\\test\\ms_cls\\m.03grfv6-91-FaceId-0.jpg";
////         String feature = PictureUtils.httpExecute(new File(searchfile));
////         System.out.println(feature);
////         byte[] rt_feature = PictureUtils.getFeature(feature);
//        byte[] rt_feature = new Base64().decode(
//                "7qpXQsFdAAAAAQAAtJTLvH+kyb3OfeC9hzGMvUOxOj1zW709MuNiPMl51rsDirq93n4IO+QX3jwCxLa81tnuPZzuiL3WgkE7pTUwvbSMAD5VO6u98kAUPIIRNL2NVSi94VSAPeXv7byfvAO+1OFFO9KOEbx3Vtm9s+CZPY2FFr7chqY8sfYnPTg04731O4S9q5YjvXqiKb261qs8AxKMvUYHEr115EK9PmCevYGyFb2ROIe91DoLvcF/wbwXOpG9qLabPc0/wTx+Hoo9LAq9vMd64TwTTeI7MHcQO0BOsTytjIe9UbTEvSMmtL0gLfU9AyHuPcBwMT7MfDI9ZjOPvaZEizscz6e7NKXAPQpCo7zRioM9c5rbPdOXyL2BuNI6U6MPvkcon73Pk9C8jzOUvXlKSj1cUUS9rHuCPLfVJj1FeKw9JqVRvPRQ7z0lGjg97GmSvWsKlj3i4au8gNA4vFyBWr2ILWU8/wKIvA3UajxXIbc8kLyzPI3yJb2ze4q8WFiAPTg3Sz2urmg8nqMzPMJxn7xpppG9ldr9O+7+pTzxRR49LHpsvf4vk70B16A8xXZkvEwAB7vVsUM94NmSPTRtCTtAaAe9ke6rvDwgUzk5bHi9yQr0vESzgj1SPPE9752KvT4Xw70drXS9nPrlPBIfpbUOXeG8yCb0PHjgLb0fhFs86byGPW9Y2Dukg7k9jh9APWQQp70tk7O9QBqNPCvRX72Qhkw7LUBIvcp2orxVfCU9wn3IPOt8kr1me/88xTddvUtR6j2XijQ9L4thPGdWBr61UBu9V3AUPmer4DulfSg9Tu2UPYHffr2uRJM9jECePQtZUr2Oy6k9Rv65vBDcub2u9Lu8wtLQuhuLBj34YoW9IutJO0OqmL3IhxK+xsxFvVBZUj3y3ws9nM/sPY6vnz0iXy88lrERPSKPAjqaNGe9TleLuw9KQLzhNio9JPeEPAQTkr2udh89eQbAPSG3aDxzIdm98ZTtPfURODuE7Aa8L/4Mvd7Zjj0BBHw9LTyYPf5mhr1tYTS7uuGvPUHT5r2M/Lu8N2TCPQBS6rxOl0E9aTlzvVWTpD3YmIW8Sa4FPlQJB71VWQq9l3SiPKThdrzbtEU9bxQ4vaFJDj3fjpk880blvJCznLztlYm9hQ+GPQplaL3B5Qu+wnVJvVtQHb00viE92xHpveAavr2P8as8rOAFva0k5jvzJyM9UdsMPU0MgjxnPJu9SiYLO4ysgT0UR+k9p/3xPPyezD3p6ai9K1kbvSEbFb5Omj+90eAIPABo5jy8qxe97+K7PNaEiL0dZVq9c3L8PUOjnTrmuwq9FHyCufulFb3N+PY8+eKHvfCCgz1Lygo9jLYNvS5TEL3M5xo9XBR7PQ==");
//        double[] qfh = fc.getDoubleArray(rt_feature, 12);
//        byte[] qfl_b = PCADimReduction.double2Bytes(PCADimReduction.generateLowDimensions(qfh));
//        List<Double> qfl = fc.getDoubleList(qfl_b, 0);
//        String featureName = "rt_feature";
//        System.out.println(getFeatureValue(qfl));
//
//        Map<String, Object> params1 = new HashMap<>(4);
//        params1.put("featureName", featureName);
//        params1.put("useHash", false);
//        params1.put("useFeatureComp", true);
//        params1.put("featureValue", fc.getDoubleList(rt_feature, 12));
//        QueryBuilder qb1 = QueryBuilders.functionScoreQuery(
//                scriptFunction(new Script(ScriptType.INLINE, "native", "image-feature-retrieval", params1)));
//        String[] incs = { "person_name"};
//        SearchResponse sr1 = client.prepareSearch(indexImage).setTypes(typeImage).setQuery(qb1).setMinScore(sim)
//                .setFetchSource(incs, null)
//                .addDocValueField(featureName + "." + FeatureFieldMapper.FEATURE_LOW)
//                // .addSort("rowkey", SortOrder.ASC) // .setExplain(true)
//                .setFrom(0).setSize(1000).get();
//
//        System.out.println("totalCount " + sr1.getHits().getTotalHits() + ", costtime " + sr1.getTookInMillis() + "ms");
//
//        int i = 0;
//        for (SearchHit hit : sr1.getHits()) {
//            Map<String, Object> map = new HashMap<>();
//            // Handle the hit...
//            // System.out.println(hit.getSourceAsString());
//            BytesRef f_low = hit.getField(featureName + "." + FeatureFieldMapper.FEATURE_LOW).getValue();
//            float sim_l = fc.DotAsDouble(qfl, f_low, 0);
//            map.put("id", ++i);
//            map.put("sim_low", sim_l);
//            map.put("sim_high", hit.getScore());
//            values.add(map);
//        }
//        //   Write2Excel.writerExcel(path, name, titles, values);
//
//
//        Map<String, Object> params2 = new HashMap<>(4);
//        params2.put("featureName", featureName);
//        params2.put("useHash", false);
//        params2.put("useFeatureComp", true);
//        params2.put("featureValue", qfl);
//        QueryBuilder qb2 = QueryBuilders.functionScoreQuery(
//                scriptFunction(new Script(ScriptType.INLINE, "native", "image-feature-retrieval", params2)));
//        String[] incs2 = {featureName};
//        SearchResponse sr2 = client.prepareSearch(indexImage).setTypes(typeImage).setQuery(qb2).setMinScore(0.6f)
//                .setFetchSource(incs2, null)
//                //  .addDocValueField("feature."+ FeatureFieldMapper.FEATURE_LOW)
//                // .addSort("rowkey", SortOrder.ASC) // .setExplain(true)
//                .setFrom(0).setSize(1000).get();
//
//        System.out.println("totalCount " + sr2.getHits().getTotalHits() + ", costtime " + sr2.getTookInMillis() + "ms");
//
//        i = 0;
//        //   values.clear();
//        for (SearchHit hit : sr2.getHits()) {
//            Map<String, Object> map = new HashMap<>();
//            // Handle the hit...
//            // System.out.println(hit.getSourceAsString());
//            Map<String, Object> source = hit.getSource();
//            if (source.get(featureName) == null) {
//                continue;
//            }
//            String f_high = source.get(featureName).toString();
//            float sim_h = fc.Dot(fc.getDoubleArray(new Base64().decode(f_high), 12), qfh, 0);
//            map.put("sim_high", sim_h);
//            map.put("id", ++i);
//            map.put("sim_low", hit.getScore());
//
//            values.add(map);
//        }
//        //   String path2 = "pcaSimresult3.xlsx";
//        Write2Excel.writerExcel(path, name, titles, values);
//    }
//
//    public static void main(String[] args) {
//        try {
//            // 修改LSH矩阵
//            LocalitySensitiveHashingCos.readHashFunctions(
//                FeatureQueryBuilder.class.getResourceAsStream("/hash/lshHashFunctionsCos_16x32.bak"));
//
//            // 初始化获取均值和PCA矩阵
//            PCADimReduction.readPCAMeanAndMatrix(FeatureQueryBuilder.class.getResourceAsStream("/pca/pcaMean_256_32.obj"),
//                FeatureQueryBuilder.class.getResourceAsStream("/pca/pcaComponents_256x32.bak"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        client = ESUtils.getESTransportClient(clusterName130, transportHosts130);
//
//        // writePCAFromIndex();
//        testSimLowVsHigh();
//        // testPcaComp();
//       //  testLowSimVSHighSim();
//      //  testHighSimVsLow2Excel();
//      //  testLsh2Excel();
//
//        client.close();
//    }
//}
