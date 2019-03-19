package com.znv.fss.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.es.BaseEsSearch;
import com.znv.fss.es.EsManager;
import com.znv.fss.es.FormatObject;
import com.znv.fss.es.FssArbitrarySearch.GetFeatureValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by User on 2017/8/3.
 */

public class httpTest {

    /*
    // 将输入参数转成类，用时从类中获取
    public static void requestSearch(String params) {
        FssArbitraryJsonIn fssArbitraryJsonIn= JSON.parseObject(params, FssArbitraryJsonIn.class);
        FssArbitraryQueryParam fssArbitraryQueryParam = fssArbitraryJsonIn.getParams();
        if( fssArbitraryJsonIn.getId()!=null) {
            String id = fssArbitraryJsonIn.getId();
            System.out.println("id:"+ id);
        }

    }*/
    public static void arbitSearch() {

        List<String> f3=new ArrayList<>();
        String f1 ="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";
        f3.add("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");
f3.add(f1);
        // String params = "{'id':'13001','params':{'camera_id':['11000000001310000018'],'enter_time_end':'2017-10-16 23:44:40','enter_time_start':'2017-10-16 00:44:07','is_calcSim':'true','feature_name':'rt_feature','sim_threshold':'0.8','feature_value':'7qpXQpFlAACAAAAACMcAvTzAcj1238G9B9IVvvARiD0W24+8BbSUvk91L75u1Us9CUYCPbJJNrz/gy29iq9iPDqG/Tuvzcm9GKusPfROBD55BUM+Mpa/vTZfgj3c2N+8Kad2u8ggBb3UN2M9GgBGPs1D7L1JWJy8HDwDPh6YAL3Hwtu8/6IRPpAKsD2sccG8FDYQvtP5Vr3uWO+9/+bhu+S2BbxlaJ+82FYjvaY1Fr5bZK89V6IaPdjRoLvTr7q9HxOZu6JGbj1uwRU77BVPPeMPHjxc+bA7CVqhugm3/j3Fqdc8UygiPXiasb0WCA6+ROyYPX2KgL3Ozks+B8QTPivC4b3DcFy9qK7xPSFUQD5JMBS+WNe8PI7SjrpuNjk+4N/pvS6tQrwFoGm9cFz7vMSm3L2YDoW72780PeMMWT29OOK8+OE6vr6o/j2hVPM8TltAPXkOBL4XVRm9ZsCQPcTFibxNJM882G29vcN3IL6bzz89Qp0Wvoj4zDvpfx4+p2eSvUSWL72HiS+8+PAwPeg7sz09RHo9z4zEvUnEHLzTs+0838+9vCPbLLz3zPQ7pDWCvTiv+7yk3nu9AnW6PNjRjD0k9xG9mToGu4RX3zt6t6E6GdcvvpdjMr5htcY7JkI+vAcspb1ms4s9VzH8PdGFob0CD328n1hIvahq5bw7qpM9mXwPPYdrNjw=','from':'0','minimum_should_match':'0','size':'10'}}";


      //  String params = "{'id':'13001','params':{'enter_time_end':'2018-05-25 00:00:00','enter_time_start':'2018-05-20 00:00:00','is_calcSim':'true','feature_name':'rt_feature','sim_threshold':'0.89','feature_value':'"+f3+"','from':'0','size':'1'}}";


        String params = "{'id':'13001','params':{'enter_time_end':'2018-05-25 00:00:00','enter_time_start':'2018-05-20 00:00:00','is_calcSim':'true','filter_type':'and','feature_name':'rt_feature','sim_threshold':'0.89','feature_value':['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'],'from':'0','size':'1'}}";
        JSONObject searchResult = null;
        long t = System.currentTimeMillis();
        try {
            BaseEsSearch search = EsManager.createSearch(params);
            searchResult = search.getSearchResult(params);
        } catch (Exception e) {
            System.out.println("error");
        }
        long ts = System.currentTimeMillis() - t;
        System.out.println("The result:");
        System.out.println(FormatObject.format(searchResult.toString()));
        System.out.println("Total Cost: " + ts + " ms.");
    }

    public static void humSearch() {

        String params = "{'id':'13002','params':{'start_time':'2018-03-01 00:00:00','end_time':'2018-09-15 13:59:59','camera_id':['11000000001310000001'],'time_query': true,'gender_query':false,'age_query':false,'search_interval':'1d','person_id':'0','order_type':'desc','from':0,'size':1,'child_start_age':0,'child_end_age':8,'child_start_age':0,'teenage_start_age':8,'teenage_end_age':18,'youth_start_age':18,'youth_end_age':41,'midlife_start_age':41,'midlife_end_age':66,'old_start_age':66,'old_end_age':200,'others_start_age':200}}";

        JSONObject searchResult = null;
        long t = System.currentTimeMillis();
        try {
            BaseEsSearch search = EsManager.createSearch(params);
            searchResult = search.getSearchResult(params);
        } catch (Exception e) {

        }
        long ts = System.currentTimeMillis() - t;
        System.out.println("The result:");
        System.out.println(FormatObject.format(searchResult.toString()));
        System.out.println("Total Cost: " + ts + " ms.");
    }

    public static void fastSearch() {
        String params = "{\n" +
                "  'id': '13003',\n" +
                "  'params': {\n" +
                "     'enter_time_start' : '2018-03-13 00:00:00',\n" +
                "     'enter_time_end' : '2018-03-13 23:59:29',\n" +
                // "     'office_id' : ['1'],\n" +
                //  "     'camera_id':['11000000001310000006'],\n" +
                "  'feature_value':'7qpXQideAAAAAQAApoTcPVzfDL0Ahro9Z/A7PWefoj1SaZo8QR71PUw5W7zpL7W8k0WNvcOpiryyOAW9M/BOPQb6lzxuHaQ9lJmIvdpXzryNEAe9cv7pvByxgz3y8Z28t9/cPear4T1Qaxm9czv1PKKn+73RSjo8T4KdvEmo2Lz2oX+9rsoSPUyulD0IV/68b3cgvDLp7TzsgMA8R8wOPdBeYj3nuqY9Aa/avLiGzz2njPu8kSuHvWdt6j0WfYA9H0VIPUBMs71K9IW9AUknPeLXgD3YoW+81+ELu3Am9b1dLd88Vpg/PWxlob0t3SC9HahOPV5Byz0R3lI82wwrPamOvL3BcuC8JQCLPct2mzsd3ZW9CmaDPXJup7vi5qW8/+HbPZUgVb1s2qG6iX69O4z7u70vU069LV5fvbCvMD0J3hg8KVgrvZwRDT5T3AG8DxgQvcbBuz3fVqs9FQzRPA+liTwEv6E4ct80PDAxpT3+X7A9cXSOvODNTz17fyc9Fu/WPUxAFjzAcgC7YBUmvdbPGD12XsA9O5W2vJAvvjzYQ4y7guOLPd86sL0pC5+8c7Z/PTLzhr3hNhe9MiOcPCNkyDt5awK983zMPP6Yj73GvuA7X68Dvd4Ujj37PGy9jcESvaK7iD2eE509U5HHvI7UZDyHOwa9jgtoPf6D0r29XqM9EicgvQxcV70ucG+9IlI8PHLsQTwprle661uDvQYYZjzwoxq9LzS0vNshgj1oBKo9l+LbPPfWFDts//Q8mQCDPND/Mj27WRA+eV8ovodPDj0CQZE9ue6zvVdzOT13VHW8L9oBPaG/Fr75Cio8Do2WvT1oB72rJ169cOX5vLFaxDt5DoG9kCaTvdiq9T2LXZK9lsKiPPBfzjyKUHs8ABe8vdfJwr1I7CI9CYFRPXqKAT7V/8I9B97svT/6DT0qa6o9/9QEvvdi2rzxEgy9/LUwPaATaD00ome9ib5/vZYeR7yUoN+87FHdvPgOhLzSUj+9p84QPNLxsD3csk68TcGvvM1qHL0VzNo9W2/BPd3yTL1Pc/E8acRwPQt6m72TWji9bC9Vva6eob30Yy08LN1gvY+Y5j0AlcY9WcLovHwa5j1f86m8b9k1vSp2pTxCk469PkFhPZa4xrzXcbY8vmaaPI6GoL1+5v88pMvRPOy3/Txjgy692qY+vcrWdrvQnDo87uq2uyBnLb3+tMw8fhNdPHUtHr72wo68PU0lvcpvoT3UtFA9LtjBvdUVpT0YTRQ862aXPSJwGz3SwLM9EEgcvlATYb5ClF28blr9vWb8Rj1kQ4w9fhg4vbg/uLu8lbm9cbMSPdKW2LzKEJk9sm/sPH6VZr1sJVS7a2RIvJj0AL1dJiM9HCgAvQ==',\n" +
                "     'sim_threshold':0.89,\n" +
                "     'sort_field' : 'enter_time',\n" +
                "     'sort_order' : 'desc',\n" +
                "     'from':0,\n" +
                "     'size':36\n" +
                "    }\n" +
                "}";

        JSONObject searchResult = null;
        try {
            BaseEsSearch search = EsManager.createSearch(params);
            searchResult = search.getSearchResult(params);
        } catch (Exception e) {

        }

        System.out.println("The result:");
        System.out.println(FormatObject.format(searchResult.toString()));

    }

    //图片交并集
    public static void fastSearch2() {
        String params = "{\n" +
                "  'id': '13003',\n" +
                "  'params': {\n" +
                "     'enter_time_start' : '2018-05-01 00:00:00',\n" +
                "     'enter_time_end' : '2018-05-25 23:59:29',\n" +
                "      'filter_type':'or',\n" + "'is_lopq':true,\n" +
                // "     'office_id' : ['1'],\n" +
                //  "     'camera_id':['11000000001310000006'],\n" +
                "  'feature_value':['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'],\n" +
                "     'sim_threshold':0.6,\n" +
                "     'sort_field' : 'enter_time',\n" +
                "     'sort_order' : 'desc',\n" +
                "     'from':0,\n" +
                "     'size':1\n" +
                "    }\n" +
                "}";

        JSONObject searchResult = null;
        try {
            BaseEsSearch search = EsManager.createSearch(params);
            searchResult = search.getSearchResult(params);
        } catch (Exception e) {

        }

        System.out.println("The result:");
        System.out.println(FormatObject.format(searchResult.toString()));

    }

    public static void personlistSearch() {

        //String params = "{'id':'13004','params':{'start_time':'2017-09-13 09:00:00','end_time':'2017-11-23 23:59:59','minimum_should_match':0,'lib_aggregation':true,'from':0,'size':9,'sort_field':'modify_time','sort_order':'desc'}}";
        // String params = "{'id':'13004','params':{'minimum_should_match':0,'lib_aggregation':false,'from':0,'size':36,'sort_field1':'modify_time','lib_id':[2],'person_id':'151610173453300','start_time':'2017-11-06 09:00:00','end_time':'2018-12-26 23:59:59','sort_order1':'desc','is_calcSim':'false'}}";
        String params = "{'id':'13004','params':{'end_time':'2018-09-30 11:11:17','feature_name':'feature','feature_value':'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','from':0,'is_calcSim':true,'lib_aggregation':false,'minimum_should_match':0,'sim_threshold':0.89,'is_del':'0','size':1,'sort_field1':'modify_time','sort_order1':'desc','sort_field2':'person_id','lib_id':['12','11'],'sort_order2':'desc','start_time':'1970-01-01 00:00:00'}}";


        JSONObject searchResult = null;
        long t = System.currentTimeMillis();

        try {
            BaseEsSearch search = EsManager.createSearch(params);
            searchResult = search.getSearchResult(params);
        } catch (Exception e) {

        }

        long ts = System.currentTimeMillis() - t;
        System.out.println("The result:");
        System.out.println(FormatObject.format(searchResult.toString()));
        System.out.println("Total Cost: " + ts + " ms.");
    }

    public static void personlistCount() {

        JSONObject paramsJSON = new JSONObject();
        JSONObject paramJSON = new JSONObject();
        paramsJSON.put("id","13005");
        paramsJSON.put("params",paramJSON);

        paramJSON.put("start_time","2018-01-21 00:00:00");
        paramJSON.put("end_time","2017-01-21 00:00:00");
        paramJSON.put("is_del","0");

        JSONObject searchResult = null;
        long t = System.currentTimeMillis();
        try {
            BaseEsSearch search = EsManager.createSearch(paramsJSON.toJSONString());
            searchResult = search.getSearchResult(paramsJSON.toJSONString());
        } catch (Exception e) {

        }

        long ts = System.currentTimeMillis() - t;
        System.out.println("The result:");
        System.out.println(FormatObject.format(searchResult.toString()));
        System.out.println("Total Cost: " + ts + " ms.");
    }

    public static void historyPersonCount() {

        //String params = "{'id':'13006','params':{'start_time':'2018-03-16 00:00:00','end_time':'2018-03-16 17:54:59','search_interval':'5m','order_type':'desc'}}";
        //  String params = "{'id':'13005','params':{'is_del':0}}";
        JSONObject paramsJSON = new JSONObject();
        JSONObject paramJSON = new JSONObject();
        paramsJSON.put("id","13006");
        paramsJSON.put("params",paramJSON);

        paramJSON.put("day_start_time","2018-01-21 00:00:00");
        paramJSON.put("day_end_time","2019-01-21 00:00:00");
        paramJSON.put("agg_start_time","2018-01-21 00:00:00");
        paramJSON.put("agg_end_time","2019-01-21 00:00:00");
        paramJSON.put("person_id","0");
        paramJSON.put("agg_type","office_id");

        JSONObject searchResult = null;
        long t = System.currentTimeMillis();
        try {
            BaseEsSearch search = EsManager.createSearch(paramsJSON.toJSONString());
            searchResult = search.getSearchResult(paramsJSON.toJSONString());
        } catch (Exception e) {

        }

        long ts = System.currentTimeMillis() - t;
        System.out.println("The result:");
        System.out.println(FormatObject.format(searchResult.toString()));
        System.out.println("Total Cost: " + ts + " ms.");
    }

    public static void alarmPersonCount() {

        //String params = "{'id':'13007','params':{'start_time':'2016-01-22 00:00:00','end_time':'2019-01-22 23:59:59'}}";
        //String params = "{'id':'13007','params':{'top':10000}}";
        JSONObject paramsJSON = new JSONObject();
        JSONObject paramJSON = new JSONObject();
        paramsJSON.put("id","13007");
        paramsJSON.put("params",paramJSON);

        //paramJSON.put("lib_agg_start_time","2018-01-21 00:00:00");
        //paramJSON.put("lib_agg_end_time","2019-01-21 00:00:00");
        //paramJSON.put("camera_agg_start_time","2018-01-21 00:00:00");
        //paramJSON.put("camera_agg_end_time","2019-01-21 00:00:00");
        paramJSON.put("top","5");


        JSONObject searchResult = null;
        long t = System.currentTimeMillis();
        try {
            BaseEsSearch search = EsManager.createSearch(paramsJSON.toJSONString());
            searchResult = search.getSearchResult(paramsJSON.toJSONString());
        } catch (Exception e) {

        }

        long ts = System.currentTimeMillis() - t;
        System.out.println("The result:");
        System.out.println(FormatObject.format(searchResult.toString()));
        System.out.println("Total Cost: " + ts + " ms.");
    }

    //告警查询
    public static void alarmTypeSearch() {
        System.out.println("statrt alarm search!");
        String feature_value = "7qpXQsFdAAAAAQAAa8HqvLqOqr2XRhy9TXntvcP+Db1yhfW8swfavTnEIb26jFM7ZTFuvRqTZztVJUK7SY5kvYmlFz09dq48rRZ7vL/lnDx+Z7W9dEekuq6+mj3CyCo8V1wpvVxwz72EEvc8TUi9O1OqH717ndY7IauivK8AZz2OPsO92yH4vG2Foj2jz7O7KY8jPUTfeTxG10m9vaxJPftY3LzfNJY9FP/PPF+tqz0zpg09jVGzPc6cBr2diHY9DtwXvZB8MjxHkUU6vX3GPTT5Sj38hQO9N5xWOsNFUrzuBfy89dW1PR1E8jy+2RG9T94pvX6rAL0KrIc9McBNvUi9hLznsFe8NnLVPXyXRLzoSRW9WwxqvSdHjb3u1Wi9toN1vfATL70+3TE9A0FwPMYQ9jx79Qu+iHDgvUrSEjxZAvQ88n0ju8OVXDz9Zfy9ZfzTvX39gr1yLX09zFFau85BjDxQzAy9L1ZKvIRCCr3EjJq9n6bBPX+CPr5TQDy9NgqEO3Y1x71O0a29DXYJvaw1JT2a45U7V/g5PfZ3zD3KZqA90LQxPRkU27zcagi9NhsxvccnuDykX6a8R6GXvQPntL0r2Vo8fW9hvIrG+T3teRq9zx6nPXMRo70ogfk78ZONOzWDub0IxdU9nM0pPdW27byniai9rluqPVSfsr3Hxac83J6LvZHdDD6u84g97esBO8/dCr6EVKu9QV+zPahCCrxLlYA8FX8fvaCJNbmgVYY9l9uWvXVmOj1qZFu9uTlZPdKDBT1lVdW8uhMVvAKYTrxFo9i77GOFvXQZTzzhRfK9RyOJPOkT7b1OGXY7LarHuhDvL70gG8W9XViivEO6e7075s89VXQKO86blby7Ae+9lP4rvrBozz24r5y9K2X/vDjurD3hBzW8jI48PLL8obxgoES8d562umxyVb426ZO6xyJvvHuEsDwwdYU9Xe+0vVkT2r2zhty8RzyFPbUMyjyNfKe9AUmbPfBIqr2MLay85nucveutmDw0XWe8pa8NPE85n7sBn5q8cDTRPKUCm71Jc5U97WqFPVxCgD37n969X/7gPFy3PD1UYLg8Y0PePRE2lL1kPRU94/4nPaC8rjzA9DQ9o7uzu8IVEr4JyAe6HMUQvsjt27wiBYE93FCHPW1IM71sJOw9OdvMPZxCXD3+Njy8FKeIvXUtE75V/zq9LzknvDyuHL2t9Uk8lk5JPShqUTy0GaY9PKXFPNW/s734Tm89ic58vTaMVjyK6fM8IOeQvEpThL0dFVg96B93va4KLz3OCCa9LIL5u/V3nr1rsGe8WdkePY0Wl72UCUM9JHqjvD6A5r1vAgC9MAkLvV3wCjuMOY493jOGPQwRFb2z2rc8JtbOPA==";
        String feature = "7qpXQsFdAAAAAQAAtIdQvKwvYL0A1gg+RbIxvsLZqT3KLW49wn/GPXcdAL3YtLG8SQh0PKaxz72BOYW8xAP9PWOb5r1P4py9CnTcPdD5Kj2RRae9YVH7PK2sa7ydTq49JLFSvZWnJbzAFbe9o3sEPf+DWD05ISu90IKPPM7p1zvwypu9OfBevaNUHb00MYQ8cTEQvXo5nj1Bhki9B5TgPXK3hb0rN4A8qdKjPHwTezw+R4m9m1+JvSwf4Tw+7jY9o3I6vfidvD09W8Y9Ol6lPbbEcry7d9O8jiGiPVXQsj07dv48v3tbPZZlkTzmmrq9HgpPveqMlD2SrYi92j0bPLo4h7zH4vw8+UaSvX+3vD3dvqA773wuvT0gz7uE+3A8RwA5PpHGVrx4AwO9VJqWPUbbDj1eR4e9oHSYPSoiFD0b6Kg8WcfePMdOTzw9YR6+Nv6KvU4EHT2lLLc9mCaQvffPwL1eGBS9/93IPDu4ED1mVCY9bGKePBQ7Zj2xxoa9RJxTvc0UqrzUDyk9+HODPZ46TD2giXS9xfKiPbpbarzmXEI9nc/pvOlnb7uT/qu7MApEvUbaIT1zrAC9xgrZvO4wFbtw5tS9+POdvXk7Hj0KwaI9E46NvMUMhLy9RpQ9RB70vJwp3T3S/Kk9bvlCOz7Tab1PvmK9Jnz3Pdjokz1jwFK9P0vhu5/nAr2iJGG9UayQvRAZWb2+l/e9bfTcvWiloD16UQ+9kSc/vJoWmzzq2K29Q0yxvVWBjrwmloW9w4+8vDMqcr1SUsG9P4+TPEbN+jtyimk9ACxfvY5DC736YlO8+3iJvcdnID36i+27ooC1PPMvHz7TaII9OZgjvU9XqLzyaMw99fpAvCE1Jb2Yf949QrGTPcvR+T1OSbU9yC3SusTeiDzOk+m8HuhkvTYutz31zM+9dmCDPeQ+trw1/3W9x/Wcu1rLjTvyKNk8sEJqPVIZQrpHDkK93k6KvWldvzyBqP68fvPHvfNP/junUsA9IcCTvKjoEL0qGyM9NCKrPACZO77XAFM8rHcHPdV+ST3vJMg8F6woPW2PJrw31KM9yVfpvQmii72eGsO9y1DzPMEKdbzxu1g9Sc4ju6dSar2nQY69jM6ivTjQlD2yoSc9MPQSvdmr6D3R42i8wvy5PQS8kjuYlUk9xhGdPZAQezx8HCU9Dsm6PS19YT3kJqQ8WQqBPLKSCTwQefq8W5sdPFE2Gj0W2IE9dlClvG/HOL0xoy29Zi1tPTRXxT1DoYE8U2jzPPtzwT1qqEK7aAvwvI75mz2ADda5gkRpvSg1SD23oI69GCUovHib8jz92gG+/XO1PIS3MrylxXw9c0nPPQvXoD1OMUY9fdvdPH3zCr29PZ487xSJvQ==";
        //FeatureCompUtil fc = new FeatureCompUtil();
        //List<Float> listFea = fc.getFloatList(new org.apache.commons.codec.binary.Base64().decode(feature_value), 12);
//        String params = "{'id':'13005','params':{'enter_time_start':'2018-03-19 09:00:00','enter_time_end':'2018-03-19 23:59:59','camera_id':['32011500001310000009'],'office_id':['121245'],'alarm_type':3," +
//                "'is_calcSim':true,'feature_value':" + listFea + ",'sim_threshold':0.6,'from':0,'size':'5'}}";

      /*  String params = "{'id':'13008','params':{'enter_time_start':'2017-12-21 00:00:00','enter_time_end':'2017-12-21 23:59:59','camera_id':['11000000001310000099'],'office_id':['11000020010'],'alarm_type':3," +
                "'is_calcSim':true,'feature_value':'" + feature +
                "','sim_threshold':0.6,'from':0,'size':1}}";*/
        String params = "{'id':'13008','params':{'enter_time_start':'2018-05-21 00:00:00','enter_time_end':'2018-09-25 23:59:59','is_calcSim':true,'camera_id':['11000000001310000001','11000000001310000003'],'feature_value':['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'],'sim_threshold':0.6,'from':0,'size':5}}";
        JSONObject searchResult = null;
        long t = System.currentTimeMillis();

        try {
            BaseEsSearch search = EsManager.createSearch(params);
            searchResult = search.getSearchResult(params);
        } catch (Exception e) {
            System.out.println("catch exception : " + e.getMessage());
        }

        long ts = System.currentTimeMillis() - t;
        System.out.println("The result:");
        System.out.println(FormatObject.format(searchResult.toString()));
        System.out.println("Total Cost: " + ts + " ms.");
    }

    /* public static void main(String[] args){
         String hostUrl = "hdfs://lv120.dct-znv.com:8020/user/fss_V113/development";
         EsManager.initConnection(hostUrl);
         String path = "d://interfaceTool//image5.jpg";
         GetFeatureValue.getFeatureValuePath(path);
     }
 }*/
    public static void multiIndexExactSearch() {
//'feature_name':'rt_feature',
        String params = "{'id':'13010','params':{'enter_time_end':'2018-09-25 00:00:00','enter_time_start':'2016-03-20 00:00:00','camera_id':['11000000001310000001'],'is_calcSim':'false','filter_type':'or','sim_threshold':'0.89','event_id':'','feature_value':['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'], 'coarse_code_num':36,'from':'0','size':'1'}}";

        JSONObject searchResult = null;
        long t = System.currentTimeMillis();
        try {
            BaseEsSearch search = EsManager.createSearch(params);
            searchResult = search.getSearchResult(params);
        } catch (Exception e) {
            System.out.println("error");
        }
        long ts = System.currentTimeMillis() - t;
        System.out.println("The result:");
        System.out.println(FormatObject.format(searchResult.toString()));
        System.out.println("Total Cost: " + ts + " ms.");
    }

    public static void  multiIndexFastSearch() {
        String params = "{\n" +
                "  'id': '13011',\n" +
                "  'params': {\n" +
                "     'enter_time_start' : '2016-04-13 00:00:00',\n" +
                "     'enter_time_end' : '2018-09-25 23:59:29',\n" +
                // "     'office_id' : ['1'],\n" +
                  "     'camera_id':['11000000001310000001'],\n" +
                "  'feature_value':['7qpXQoleAAAAAgAAQPEbPWZh5LsDOyG8tg3GPIYO37xmoAk9U+34PQt8hT02Ege9PUbEO1CY6ry4P0e7KtNDu9x3wryYZj69nbv+PKWIjL3IZyo8CRMGPZoKhb1TZcC8ulewvYdCSD1shwO8vagdvEs96T1Qf9w8ImakPUn2gD2A+3o9rqunPN8X7juurvC8Z5z0O0ODmT2rLoY8S0kuPYLvqjqqLAa9KP6xO1NVrD23aL49FZf+PEUY0L0uucm8ZZlavFaKjL3+V5k9x+8wPCp2kD3RRFM9+UxdO4qsUbyWhqW9l96iPGtH67xF9Fg9y6lUvSNLNTsU1rw9ndW4PeGGQjzwNo89D3GCvSGaAz05vp+9Cx+ovdviCT1UXdq8rWkGPXKeRL3Buc88Dcw/velAoz3Ruhm8DvXFPa9KoT1AWxs9b5+AvCvNWL0yfIo9pvkpvQJF9jzKiKs8jmMDPGeA7Dyhehc9l9pvPDToy70kO4e7HMPFu6qkljxF4ri8ncRTPaS6jDysxEY9piPbPA8i5TtPShk9pcyKvcKH7b2c6YM8mMUOvSJYDLy8LIG7qj8bPX4BMz0jsyO9CDQjPWERqLzG0NC8oWo0vG+9fT0LHYo6UuJbPTR91zsk1RC94NgcvbhJiz2tw8E8Mf4BPkrMe7yWkvS8uyztvWxg5rzG3VU9wE0ru70vrTy5SoY9xD4jPUkcq7xBhWc9VQaEPJs7JzxwLZm7n1wtukrVrj2lhcK8hfjOOuirR72lr7+9zV+fPCtEkryJqp05DHEuve23gD1wOWi8eaPVPU5JIL3CVpS9vAtsPVcETz1JtCU9UFKKvQAaDb0qbqC9j0SpPPZvDD0CCKi8suSoPU3eTzvpM+A8qjINvDbTZT1ygTQ81OSxPR8MTj3WNGi8sap5vUWSsz2FA3k8VoX8PCIeNbttoy89E8riPEgOOT3i0eE8rRIpvf3ViLw3HQY8kfuhOuZVg73JmP68WGBAvRxyXz14p3i8IQGrvLE4szz9zoa77f1NvVfcW7wRTXc9euv0PKWdvDy73ma8KCt8vVcDWDwFK9Q8mNhePccU6rz4hFK9IP4iPHk6Jj1v/yI9NlDAvKQmTbwZSee8s7xdveTFSz2GeMa9O5WrvDiFyjwgUWG8eT8bve/PmDznshM9+RDsPKKWFT2fdIS9jgLSO5WNKj1Ulrk9WmeIPC47l7yyoaA8P0qhvOfzjr0t/OQ9vbj/PBkHPr2Ukaa86ESSOklJVDyA+BE9LGT7O9nIi7weTSo7qBbjvJpdoTwVVEq9OgObPY2wRr0TsoM8NoNXPE7kPL1JJpK9/FcvPVwPlLzh+mi9Jx0RPR/wrLuMZ7293vSMPbKsTT0ASWg7PUWMuzNMDj1LweC7RgGyOyylWLyyZrm8sPo8PcABwT0VX0k9AOamvH3ZUDupJOA7jjrUPHtSGTys9Aw8+VcevZ8zPT3DhIC9xO4WObFslTzHIIq96BTWvECjZb2gx1s9uqy1vInJRjyWrM09otFxPJ/Pnz028pg9IEhSPdOvWzyA4K874Z4TO8cLnzrvBI09pj6CO7rGTz1zzGA8F/lAvW/1WzymkCo9UUtKPcCXGT2k15C9Ws8wOURisLoDxmi9RmStPfUjujy9MGU9T7eJPToLmLslVay874QrvexsOT1A4Ly7VKNyPSdcI72faU482XesPUdkiT3+dJY8Cio7PXfkE708/Ns8FGR3vdF9O70lSuI8qcJIvSSfUTw6LCu9qQAlPCK9BL0jRZA97Ityuy2+bj1n6lQ98SdvPYQG1LycAdC8KNFPPfeOirzxdB48dgq9uqpx3DyA+ZE8OfctPfh63zzhUt+9aWOMvO7yKLxmnCM9fE9UvX+tkjyMWgg7Q92zPI5kKD3w4da856jYPMrsh71Px9C988klPHWv6Lz0FZw8+a+yOxn/ODwApSs9wJY/vbPt/jyfigy9tGLqvAdPdDteRWY936qIPNaOcj3ewRa8/boHvfK+8ryXnbw8fEysPC7bvD2+GIa8cc8PPIoZsb1AJwy87GhBPXQX57zODHU8mJVoPRAdtj2rJMq7aHMKPc58Mj2aA7E8fSIEvT6eqTyyMHQ9c6gEvKUQ8rt/WXC8TpRwvY9hz7t7/oq8ybU+vS6mMb2OIqs84pryO5kgjT3iSd684gtGvY59Rz0VvTE9+SUnPUSoZr1hPvW8cPtmvcm5YLv/dPk7fM4ivUKamz2Xkyw8qNbtO220BTz/p149YPJLPHAKmj2yvEE94/EWve37bL1Iym09NmpUPBhOYzyIX1m85U/8PG3CrzyYEfk86y/JPMhHZb1yGV+7d804O4Iv6LvhhtW8C/HcvLFGiLxvnBg96E3XOxnJ87yFUBg8SAemO3bDc73GmPG8ayiQPb/kCDw5bNM3IgMevYlkS72qZDI8ZCYqO8wAlD3H8q28ZwkMvU6FADztquk8fET8PDchwrwfF9A87d0Yva2II72Oia88wmXEvZJQpbof6hi8GLOgvM0QF73441w6DeQfPdNrNj0pMPE8IiFivXcEB7y8ADw91NyuPaaXoDwqO5U8NHXTvAyLXrwIJTm9DBiaPV3YlzzaiTS9eCARvM3npLzsO6w84KNKPZBHx7vcM1G8fcuPvFWt+7z3Q/67vm5vvauifz1SDk+9qaiEux6xCzv9vta8et90vZQLDTkZKwa9JsGQvLjKHD1kR7w5E/KQvbg7jj0ILOs8u20yPYk6qTw='],\n" +
                "     'sim_threshold':0.89,\n" +
                "     'sort_field' : 'enter_time',\n" +
                "     'sort_order' : 'asc',\n" +
                "     'coarse_code_num':3,\n"+
                "      'filter_type':'or',\n" +
                "     'from':0,\n" +
                "     'size':10\n" +
                "    }\n" +
                "}";

        JSONObject searchResult = null;
        try {
            BaseEsSearch search = EsManager.createSearch(params);
            searchResult = search.getSearchResult(params);
        } catch (Exception e) {

        }

        System.out.println("The result:");
        System.out.println(FormatObject.format(searchResult.toString()));

    }

    public static void  FssByTrailSearch(){
        String params = "{\n" +
                "  'id': '13012',\n" +
                "  'params': {\n" +
                "     'enter_time_start' : '2016-04-13 00:00:00',\n" +
                "     'enter_time_end' : '2018-09-25 23:59:29',\n" +
                // "     'office_id' : ['1'],\n" +
                //  "     'camera_id':['11000000001310000006'],\n" +
                "  'feature_value':['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'],\n" +
                "     'sim_threshold':0.89,\n" +
                "     'sort_field' : 'enter_time',\n" +
                "     'sort_order' : 'asc',\n" +
                "     'coarse_code_num':3,\n"+
                "      'filter_type':'or',\n" +
                "     'from':0,\n" +
                "     'size':1\n" +
                "    }\n" +
                "}";

        JSONObject searchResult = null;
        try {
            BaseEsSearch search = EsManager.createSearch(params);
            searchResult = search.getSearchResult(params);
        } catch (Exception e) {

        }

        System.out.println("The result:");
        System.out.println(FormatObject.format(searchResult.toString()));
    }
    public static void main(String[] args) throws IOException {

       //String hostUrl = "hdfs://face.dct-znv.com:8020/user/fss/V120";
        String hostUrl = "hdfs://lv112.dct-znv.com:8020/user/fss/V120";
        // String hostUrl = "hdfs://10.45.144.92:8020/user/fss_V113/development";
        try {
            EsManager.initConnection(hostUrl);
            while (true) {
                System.out.println("entering option: 0 -exit, 1 -arbitSearch, 2 -humSearch, 3-fastSearch,4-personlistSearch,\n" +
                        "5-personlistCount,6-historyPersonCount,7-alarmPersonCount,8-alarmTypeSearch,10-multiIndexExactSearch,11-multiIndexFastSearch,12-FssByTrailSearch");
                Scanner in = new Scanner(System.in, "utf-8");
                int i = in.nextInt();
                switch (i) {
                    case 1:
                        arbitSearch();
                        break;
                    case 2:
                        humSearch();
                        break;
                    case 3:
                        fastSearch2();
                        break;
                    case 4:
                        personlistSearch();
                        break;
                    case 5:
                        personlistCount();
                        break;
                    case 6:
                        historyPersonCount();
                        break;
                    case 7:
                        alarmPersonCount();
                        break;
                    case 8:
                        alarmTypeSearch();
                        break;
                    case 10:
                        multiIndexExactSearch();
                        break;
                    case 11:
                        multiIndexFastSearch();
                        break;
                    case 12:
                        FssByTrailSearch();
                        break;
                    default:
                        break;
                }
                if (i == 0) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            EsManager.close();
        }
    }
}
