package org.elasticsearch.plugin.image.test;

import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.commons.codec.binary.*;
import org.apache.commons.codec.binary.Base64;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.hashing.LocalitySensitiveHashingCos;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.image.FeatureQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequestBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.util.FeatureCompUtil;
import org.elasticsearch.util.TxtFileOperation;
import org.elasticsearch.util.TypeConvertUtil;

import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Administrator on 2017/9/29.
 */
public class ImageQueryTest {
    private static TransportClient client = null;
    private static String clusterName130 = "lv130.dct-znv.com-es";//"lv102-elasticsearch";//
    private static String transportHosts130 = "10.45.157.130:9300";
    private static String indexImage = "hy-lsh-test2cos64-hm";//"z-es-image-plugin-16single_5";// "history_fss_data_v113-000001";//
    private static String typeImage = "test";

    // test search with hash
    public static void searchWithHash(byte[] rt_feature, int d) {
        System.out.println("begin query image by hash, hmDistance is  " + d);
        List<byte[]> featureList = new ArrayList<>();
        featureList.add(rt_feature);
        FeatureQueryBuilder imageQueryBuilder = new FeatureQueryBuilder("rt_feature").feature(featureList).hash("LSH")
                .hmDistance(d).useScript(false);

        SearchResponse sr1 = client.prepareSearch(indexImage).setTypes(typeImage).setQuery(imageQueryBuilder)
                .addStoredField("rt_feature.hash.LSH.0")
                .addStoredField("rt_feature.hash.LSH.S")
                .addStoredField("rt_feature.hash.LSH.L")
                // .setExplain(true)
                .setFrom(0).setSize(10).get();

        long totalCount1 = sr1.getHits().getTotalHits();
        System.out.println("totalCount " + totalCount1 + ", costtime " + sr1.getTookInMillis() + "ms");

        System.out.println("begin query image by hash and feature, threshold is 0.92f.");
        SearchResponse sr2 = client.prepareSearch(indexImage).setTypes(typeImage).setQuery(imageQueryBuilder)
                .setMinScore(0.92f)
                // .setExplain(true)
                .setFrom(0).setSize(10).get();

        long totalCount2 = sr2.getHits().getTotalHits();
        System.out.println("totalCount " + totalCount2 + ", costtime " + sr2.getTookInMillis() + "ms");
        System.out.println("");

        // System.out.println(sr.toString());
        String filename = "m.010bk0-103-FaceId-0-esResult.txt";
        SearchHits searchHits = sr1.getHits();
        for (SearchHit searchHit : searchHits) {
            // System.out.println(searchHit.getSource());
            //    System.out.println(searchHit.getSource().get("name"));
            //    System.out.println(searchHit.getScore());
            System.out.println(searchHit.getField("rt_feature.hash.LSH.L").getValue().toString());
            System.out.println(searchHit.getField("rt_feature.hash.LSH.S").getValue().toString());
            //   System.out.println(searchHit.getField("rt_feature.hash.LSH.0").getValue().toString());
            // 写入文件
//             String content = String.format("name: %s, score: %f",
//             searchHit.getSource().get("name"),searchHit.getScore());
//             TxtFileOperation.contentToTxt(filename, content);
        }
    }

    public static void searchWithFeature(byte[] rt_feature) {
       /* Map<String, Object> template_params = new HashMap<>();
        template_params.put("is_calcSim", true);
        template_params.put("sim_threshold", 0.5f);
        template_params.put("feature_name", "rt_feature");
        template_params.put("feature_value", "[-0.14340976,-0.06742853,-0.099410616,-0.05241704,0.036510836,0.019299027,-0.27646574,-0.14166303,0.10836774,0.0023803995,-0.07101731,0.018816901,-0.07120394,-0.22072746,-0.08416043,0.06194898,0.09485545,-0.041264337,-0.131248,0.049768314,-0.08656871,0.21810791,0.067973346,0.014242616,0.037189696,-0.0447119,0.1359221,0.0586776,-0.12664449,-0.041223075,0.20228973,0.13060702,-0.07283142,-0.13709795,-0.15014793,-0.054285727,-0.12175027,-0.05927375,-0.035799976,0.026573818,-0.04087084,0.05294956,-0.05321499,0.094756246,0.04706031,-0.08358512,-0.0055381516,-0.070660554,-0.03111873,0.103895,0.020912485,0.054409858,0.11306541,-0.007504699,0.12465021,-0.01446255,-0.18435428,-0.102368325,0.013871087,0.071238786,0.07709445,-0.05313803,-0.11979571,-0.023446942,-0.020951757,-0.055584606,-0.107904606,-0.068792015,-0.005597634,-0.0834385,-0.04028317,0.012181816,-0.23053062,0.017543854,0.029806906,0.03500317,-0.06025895,-0.08061729,-0.07449185,0.10623799,-0.08032231,0.017671268,-0.0110782655,-0.0364362,-0.11053312,0.09142791,0.22200814,0.10368986,-0.11870235,-0.099634245,0.037512846,0.002024422,0.1413724,0.063534334,-0.019303085,-0.034224264,0.03654001,-0.0945715,0.05415735,-0.040753417,-0.04180012,0.046879806,-0.07083254,-0.08882524,0.014941163,0.011891329,-0.018347627,-0.0880752,0.050775964,0.0785658,-0.08994979,-0.0043628793,-0.07594557,-0.010225901,-0.019781042,-0.15098462,-0.055519458,0.083745025,-0.08280796,0.010623615,0.091468364,-0.03737662,-0.054902792,0.034349144,0.028940953,0.05381191,0.037343506,-0.0052257637]");
        template_params.put("is_calcSim", true);
        SearchResponse srt = new SearchTemplateRequestBuilder(client)
                .setRequest(new SearchRequest(indexImage).types(typeImage)).setScript("{\"query\":{\"function_score\":{\"query\":{\"bool\":{ \"filter\":{} {{#is_calcSim}},\"filter\":{\"feature\":{\"rt_feature\":{\"feature\":\"{{feature_base64}}\",\"hash\":\"LSH\",\"hm_distance\" :{{hm_distance}}}}}{{/is_calcSim}}{{#office_id}},\"should\":{\"terms\":{\"office_id\":{{#toJson}}office_id{{/toJson}}}}{{/office_id}}{{#camera_id}},\"should\":{\"terms\":{\"camera_id\":{{#toJson}}camera_id{{/toJson}}}}{{/camera_id}}{{#minimum_should_match}},\"minimum_should_match\":\"{{minimum_should_match}}\"{{/minimum_should_match}}}}{{#is_calcSim}},\"script_score\":{\"script\":{\"lang\":\"native\",\"inline\":\"image-feature-retrieval\",\"params\":{\"featureName\":\"{{feature_name}}\",\"featureValue\":{{#toJson}}feature_value{{/toJson}}}}},\"min_score\":{{sim_threshold}}{{/is_calcSim}}}},\"from\":{{from}},\"size\":{{size}},\"sort\":[{{#sortField}}{{#sortOrder}}{\"{{sortField}}\":{\"order\":\"{{sortOrder}}\"}},{{/sortOrder}}{{/sortField}}\"_score\"]}")
                .setScriptType(ScriptType.INLINE).setScriptParams(templateParams).get().getResponse();*/


        System.out.println("begin query image by feature script, threshold is 0.92f.");

        Map<String, Object> params = new HashMap<>(2);
        params.put("featureName", "rt_feature");
        params.put("featureValue", new FeatureCompUtil().getDoubleList(rt_feature, 12));
        QueryBuilder qb = QueryBuilders.functionScoreQuery(scriptFunction(new Script(ScriptType.INLINE, "native", "image-feature-retrieval", params)));

        SearchResponse sr1 = client.prepareSearch(indexImage).setTypes(typeImage)
                .setQuery(qb)
                .setMinScore(0.92f)
                // .setExplain(true)
                .setFrom(0).setSize(10).get();

        long totalCount = sr1.getHits().getTotalHits();
        System.out.println("totalCount " + totalCount + ", costtime " + sr1.getTookInMillis() + "ms");
        /*System.out.println("begin query image by feature hash, threshold is 0.92f.");
        FeatureQueryBuilder imageQueryBuilder2 = new FeatureQueryBuilder("rt_feature").feature(rt_feature).hash("LSH")
                .hmDistance(16).useScript(false);
        SearchResponse sr2 = client.prepareSearch(indexImage).setTypes(typeImage)
                .setQuery(imageQueryBuilder2)
                .setMinScore(0.92f)
                // .setExplain(true)
                .setFrom(0).setSize(10).get();

        long totalCount2 = sr2.getHits().getTotalHits();
        System.out.println("totalCount " + totalCount2 + ", costtime " + sr2.getTookInMillis() + "ms");*/
        System.out.println("");
    }

    public static void searchWithScript(byte[] rt_feature, int d) {
        System.out.println("begin query image by hash, hmDistance is  " + d);
        List<byte[]> featureList = new ArrayList<>();
        featureList.add(rt_feature);
        FeatureQueryBuilder imageQueryBuilder1 = new FeatureQueryBuilder("rt_feature").feature(featureList).hash("LSH")
                .hmDistance(d).useScript(true);

        SearchResponse sr1 = client.prepareSearch(indexImage).setTypes(typeImage).setPostFilter(imageQueryBuilder1)
                // .setExplain(true)
                .setFrom(0).setSize(10).get();

        long totalCount1 = sr1.getHits().getTotalHits();
        System.out.println("totalCount " + totalCount1 + ", costtime " + sr1.getTookInMillis() + "ms");

        System.out.println("begin query image by hash and script, threshold is 0.92f.");
        FeatureQueryBuilder imageQueryBuilder = new FeatureQueryBuilder("rt_feature").feature(featureList).hash("LSH")
                .hmDistance(d).useScript(true);

        Map<String, Object> params = new HashMap<>(2);
        params.put("featureName", "rt_feature");
        params.put("featureValue", new FeatureCompUtil().getDoubleList(rt_feature, 12));
        QueryBuilder qb = QueryBuilders.functionScoreQuery(QueryBuilders.boolQuery().filter(imageQueryBuilder), scriptFunction(new Script(ScriptType.INLINE, "native", "image-feature-retrieval", params)));

        SearchResponse sr = client.prepareSearch(indexImage).setTypes(typeImage).setQuery(qb)
                .setMinScore(0.92f)
                // .setExplain(true)
                .setFrom(0).setSize(10).get();

        long totalCount = sr.getHits().getTotalHits();
        System.out.println("totalCount " + totalCount + ", costtime " + sr.getTookInMillis() + "ms");
        SearchHits searchHits = sr.getHits();
        for (SearchHit searchHit : searchHits) {
            // System.out.println(searchHit.getSource());
            //    System.out.println(searchHit.getSource().get("name"));
            //     System.out.println(searchHit.getScore());
            //  System.out.println(searchHit.getExplanation().toString());
            // 写入文件
//             String content = String.format("name: %s, score: %f",
//             searchHit.getSource().get("name"),searchHit.getScore());
//             TxtFileOperation.contentToTxt(filename, content);
        }
    }

    public static void testFeatureCompOrg(String searchfile) {
        String path = "E:\\项目\\10-大数据\\1-test\\cos_test\\";

        String compfeature = PictureUtils.httpExecute(new File(searchfile));
        byte[] comp_feature = PictureUtils.getFeature(compfeature);

        Map<String, Float> resultmap = new HashMap<String, Float>();
        int count = 20;//
        FeatureCompUtil fc = new FeatureCompUtil();
        for (int i = 1; i <= count; i++) {
            String picpath = String.format("%s%05d", path, i);
            File root = new File(picpath);
            for (File file : root.listFiles()) {
                String feature = PictureUtils.httpExecute(file);
                byte[] rt_feature = PictureUtils.getFeature(feature);
                float score = fc.Comp(rt_feature, comp_feature, 12);
                if (score >= 0.92) {
                    resultmap.put(file.getName(), score);
                }
            }
        }

        // 按得分排序
        List<Map.Entry<String, Float>> list = new ArrayList<Map.Entry<String, Float>>(resultmap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
            // 降序排序
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }

        });

        // 写入文件
        String filename = "m.010bk0-103-FaceId-0.txt";
        for (Map.Entry<String, Float> entry : list) {
            String content = String.format("name: %s, score: %f", entry.getKey(), entry.getValue());
            TxtFileOperation.contentToTxt(filename, content);
        }

        System.out.println("org comp total count: " + resultmap.size());
    }

    public static String getFeatureValue(String featureValue1) {
        StringBuffer featureValue = new StringBuffer();
        String featureValueReturn = "";
        Base64 base64 = new Base64();
        byte[] feature = base64.decode(featureValue1);
        try {
            float[] floatFeature = new FeatureCompUtil().getFloatArray(feature);
            featureValue.append("[");
            for (int i = 0; i < floatFeature.length - 1; i++) {
                featureValue.append(floatFeature[i]);
                featureValue.append(",");
            }
            featureValue.append(floatFeature[floatFeature.length - 1]);
            featureValue.append("]");
            featureValueReturn = featureValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(featureValueReturn);
        return featureValueReturn;
    }

    public static void testFeatureComp() {
        String path = "E:\\项目\\10-大数据\\10-FSS\\test\\test-peer";
        String filenames[] = {"test-3.jpg", "test-4.jpg", "test-5.jpg"};
        String featureh[] = {
                "7qpXQpFlAACAAAAAPVycPZmMgL2YTi499im4PfWkZDxoriW9/hTOPAfOarzV4Z69M1B1PS7OzbwX4rE98hPJPRQISr0c50C9nDgHPcmXW74dCD260wueve1EQz0CtKO9LgdhvaHoHL4Njqc9rYsUvgvOpD0TQIE9PwSdvHpRybyulKK99w+dPU/i+T0nBdI9biK+vfTElr3L+OI9HFr3O3RhXD2REoO9Q2AVvTEb6ztJ6PA9LoQhvs386z3JqoO9ZwvLPTmbRr28f2K9msKAveQPmj2cbxk8d8mrPbqrVT7rF2y8Bks+O41W4rpvPa2774IcO8/M1L0G6rA6cUzWPa5ror2SNy49Am4iPh4SDL2rys67AK0GPv2nkz04h9O9KnX2vQ0u+D2g/zk+FX6rvXyMsD2UA7O9gqpTvLTYpj08bDS9wVQsvQI+Qj0eF9E9CmOLvcW9/jwuKAQ85yg2vaSmDL5IgJm8VurRPHVbzTxOU8693AvpvYBpnjyIQYi9o+rLO78iPz6W/Dg9QCkGPS4mIz6Z2Aa+JIiAviTVML0ELso9K8eJOwRbCb7ZH7e9xB55PYNfujyBa3A73OMjPBDrpjyblvs9N9n9vE8UAD7cBgE9rhk1PVEXcD0FvP49D2Mfvp3mhLxDHuY8wGALPgeIcz3p3cU770rsPbNZLL6vSM287/M1PvnoiTw=",
                "7qpXQpFlAACAAAAA7Z+lPCiQKr5Am5a9aAUWvJMOgLw5wMW9C7qePI5aMb5hQ9S5QbvyPXR70zyDI9Y90j7oPeBNBz0NRwC+cjanPQ9CRb7zJu08Mk3+PWLilz3eXMa96dNQveIAmb2hNRG92Dobvpc7djxJOX88w968vELpiDukQKU9lahfPUcC0b133hO++miAu3jOnj3cEW26lP5ouz4mrTwJ0uC6UJeOPFhKVTuupgA+sRkOviACAT5tbhu+6HE/PRVgZL2rnL88A4g3PVI0rD206gM9P+Agvf78hj1yRE29DmlvvWRMfT1d/EM7W90RPVNFbL4aZZI9O0xwPbU5yL3/HR89H0cYPs42hD0Ldl+7XSU2PkIDtr1KED+9adJivc8TOz43Qj097t9VvrJGvz0iB/S91/JtveCAY70zNk258DOJO+Zwxj2Ikgw+1q5ZPWH/ED22jHG9pQtPvQOvJL3SKQ08c4UmPV1zKLpVYFK9CwXYPMFMnL28ECC9rRQSvScADz7BLGM+DPuPPYr7bT0uZ/y93oWHvKrP1L203pM9r9ElPRIJ3L3/26I986rZvRvo/b0WAyK9iMYhPTtkkL1F4Qa9ldSQva5pMT3c7YQ9Q0zAvTAwGLw/iLC9foI7vXCuKD5+4988GnrEPWMvBjxjYp698tbDvcyJKb6UibI9IgP0PatPsL0=",
                "7qpXQpFlAACAAAAAx6dJOyTLzDySbYW9tDL/vCZNlj0/2ke96Q3uvSrcj7xzqCs9EQqYPfvSnL1msJg7aSOOvSKrND2u2rA4h30RPkCJOb54BwU+bE8Uu3y5Bj4beZw5Y9FYPXqh27z2yh68jHMgvty/8D2sKYQ9zeCePGX5LL0jhE+7ASCsPEEkHjxRNAi+Gr51vYUXFL2X14I9KeiwvVINMr3Lfr49/iDtvWxtPT3LYB4+Ge/HvQq42z3Vm/G9/e0QPkzNA7r+I9U9QFPRvXbKjrwBLKM92YG7PEIeGT6O8LG8DxfWPQDyfroq9p28Ahv1vc5HT76ruYE6FQsTPtvjlzudeMM9QmW1PY8P8T2C+AI9lYMbPosCwjwMxGq92fcOvoOBMD5A8xY+AMX+vXMg27vo+Iy9/fJKvdRBFb1nSvW7FGWJPUkUGj5bpoY9e9ervcgnCLy91iS9nF98ve7h1r2LmpA7cGnEPQ9Mdj1fXp694uB5vTyP1b2aFTK9kwjHu+tPST7tXm48Bq0cvMr9lD2rzwu+CMvrvaq5uTz66qa8Q00GPlknyr3lSXG8uqIMPYVW7LxlM7y9M1qDvYJ7mb0/MOc8P2IUvsTsZ723Ln49iq/ovVdItL3kYCu+p08Pvlsf6D1v+5O8tKiJPYbV9z0rEga+CH7YPRoJhb2oDAQ9CeWvPZaQ+Lo="
        };
        String featureq[] = {
                "7qpXQpFlAACAAAAAi59lvVlTnr0OB428bN3WPcVemj0k/oK9TyAKPSwlkr0xy1m9EnixvCtig72EqBI9OufhPbQsgL2rume9/wMIPOgcYL7CCIE9TyDJvUyF2T1Lc+W9xHqove7VFL4oGh+8/uq4vX1kyz1hGI88518kvefZkD2BISe+MItPPfuh2TzB1mE9U/2yvbK5Ab5baiM+MfPNPGPP+z3Snka9M+iWvLG+Lr18Q909K4DkvZqBpj03qMu9YEglPRB5nb2lYdu8qknjvX3gSD1HbpM9qpsLOjX5PD7sMRo9NSsivac7Br04agm+jv6uPJJPB77n1pO9MdxGPoXqAL2KFCQ92XC4PdLSoLxhrFg82VT5PSe7Z7zPVgO8JxsTvkL/KD5US9c9mDjcvc2EYTx7IQs9k4aFPLHlJ7ziIj+9/PZEvCijfj2FILw9Y7oCvlV0orwXh7Y9MR+bPaVDGL0N0vm8sYh6vXkcXT075Ba+3g61vI1dZT10Tq08iSpOPdqTSD7U8B0+8L+hvGzWmz32ghG+gmRnvtmqH71kFeA8R390PcaZJ734FsG9cNf6PQ/lB72ZT9M8lK0QvZNKHb3e/7Y9yTPIvaBvIz4WE4M8cmYzvZS5ED2BrOM52I5ovbXsGz3UcAQ934D2u18L3j0akFS9FnAWPnxI6L3VjOU7bf5nPiXSmLw=",
                "7qpXQpFlAACAAAAAHvEovFXKUj0H0sO9Uj6tPWeOij3RpxC9YHqovVYjCr5bwMa8cq6OPT4fKb3lTpM9WKS2vNywHr3Q2VU8V/ruPXYSo71QtcM9kBWzvdzDej07Axc824R4vQSR0732wsw8HrXTvYfndrz5iO48tbaWvbsyNL2EwMG8hfoNvPRSlD2e4189IF2EvDI0vr0Ez+g9lohPvelvYz23qQo+9gIhvRWWYLxyeR0+oK7JvdaiKz5Aaje9gQuJO0fzVr24kII8dPjXPCEc/Lpf0US8NtPJPPGWHz6xoF49R+i9PcW/5L1sXZG9DnXauiPATr6+pfe8YcHDPcqe770fGow7cBYZPt0QHb39z4K9CNkLPsDKCD2TgsG9J4iAvpp8Wz60lyA+Dbc3vtbCMz1nQjK95AvdPNTbaj2Xvp67GPYIPby37LwtBHI98oe0Pe6PVr0he628WZXcvIS9eL0KPP48TFKePQUpuD2WIQe+anARPRoBpT3JEq+9IbLkPHl+PT6DlQU+mHWCPR4s8jxqkyi+S9xivpqgHr03xzE9Fgq7PR55771vWOK9y7aWvMITjL39t7W8GLGUvETY6jq7ofo9Ui+rvDgRpz1Az4k9qTttvXFN6bzx2hG8Rokkvi0MBT2a+pK9lbqcPd+zrz0qkZ89vMDgPVC6ML58txi96ugJPpj9Vrw=",
                "7qpXQpFlAACAAAAAj/3ZvHaPO7y3Yoi9gORaPCJMMDxJdYo8wXFhvQYxd70U2zk9UAbBPVTkZ72/b4i8hNrAvK8x2Tz0FyC8ZEIHPaEaTb6/8tE95ilZvTv+HD49TBE8FhH2vNqnyL27/iG7Kr8Mvk0Z1j1D6kY86s8fvDL7TL0E81+9mzcbPMmNeT10E0G9I56EvRjoi70jcMI9412zvbdz1LyKAX88q5Q3vWp8v7oA6h4+YPXTvT76Rj63Y7a9qpKAPbGQMr08T+g8DgBRvR4/orzmZyQ728OjPGxhOz5bmEU9xV3RPaILzrwqfqu9cHQUvG+JXL7/0LY9/PIBPnaWHbygB5g9PXcgPh1XNzxBG38803cpPt0jIr0p69S9GUuMvooBOT6b0D0+DE5Wvke0mjy9kom9+bqaPI4/Ob1JkBG9Rsl9PHKb1ryBtxU9mCzfvMYFT7vpgBW9PDe5vS3kzL2nqp48hrTTPYckuz1Iwuq9fH7eu+/IRz0j12+9XgQBPfrcKj5qOQs+PqM9vW2zyD1ojAO+uDs6vvUF7byXE/q8JM4EPuxdZruEgAw9/siYO81mjL3Wem29wdeFPBLtQTy0n8k89F/wvUD6SDzX2G09onrWvJG+y71tQZ+9PuznvekBGT5Kl0u9HvWAPWXomD0QKhC9IZOAPaFj5b3IIPw85UM9Ph1x/bw="
        };
        FeatureCompUtil fc = new FeatureCompUtil();
        try {
            LocalitySensitiveHashingCos
                    .readHashFunctions(FeatureQueryBuilder.class.getResourceAsStream("/hash/lshHashFunctionsCos_16.obj"));
            long[] val = new long[2];
            for (int i = 0; i < 2; i++) {
                int[] hashvals = LocalitySensitiveHashingCos.generateHashes(fc.getDoubleArray(new Base64().decode(featureh[i]), 12));
                System.out.println(SerializationUtils.arrayToString(hashvals));
                System.out.println(TypeConvertUtil.intarrayToString(hashvals));
                val[i] = TypeConvertUtil.intarrayToLong(hashvals);
                System.out.println(val[i]);
            }
            long xor = val[0] ^ val[1];
            int hm_d = TypeConvertUtil.longOf1(xor);
            System.out.println(xor);
            System.out.println(hm_d);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  System.out.println("hbase: "+fc.Comp(featureh[2],featureh[1]));
        //  System.out.println("http: "+fc.Comp(featureq[0],featureq[1]));
    }

    public static void main(String[] args) {
        String searchfile = "E:\\项目\\10-大数据\\1-test\\cos_test\\00001\\m.010bk0-103-FaceId-0.jpg";
        //   String searchfile = "E:\\project_test\\face\\BLASTICLIST\\test1.jpg";
        String feature = PictureUtils.httpExecute(new File(searchfile));
        byte[] rt_feature = PictureUtils.getFeature(feature);

        client = ESUtils.getESTransportClient(clusterName130, transportHosts130);
        searchWithFeature(rt_feature);
        Scanner in = new Scanner(System.in, "utf-8");
        while (true) {
            System.out.print("please input hanming distance:");
            int i = in.nextInt();
            searchWithScript(rt_feature, i);
            //   searchWithHash(rt_feature, i);

            System.out.print("Continue? 0- N, 1- Y");
            i = in.nextInt();
            if (i == 0) {
                break;
            }
        }
        client.close();

        // testFeatureCompOrg(searchfile);
    }
}
