package com.znv.fss.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import com.znv.fss.es.AlarmTypeSearch.AlarmTypeSearch;
import com.znv.fss.es.FastFeatureSearch.FastFeatureSearch;
import com.znv.fss.es.FssArbitrarySearch.FssArbitrarySearch;
import com.znv.fss.es.FssPersonListSearch.FssPersonListSearch;
import com.znv.fss.es.FssSearchByTrail.FssByTrailSearch;
import com.znv.fss.es.HumanStrangerTraffic.HumanStrangerTraffic;
import com.znv.fss.es.FssHomePageCount.FssAlarmPersonCount;
import com.znv.fss.es.FssHomePageCount.FssHistoryPersonCount;
import com.znv.fss.es.FssHomePageCount.FssPersonListCount;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.es.LogToEs.WriteLogToEsBulk;
import com.znv.fss.es.MultiIndexExactSearch.MultiIndexExactSearch;
import com.znv.fss.es.MultiIndexExactSearch.WriteDataToES;
import com.znv.fss.es.MultiIndexFastSearch.MultiIndexFastSearch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.mapper.image.FeatureFieldMapper;
import org.elasticsearch.lopq.LOPQModel;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static com.znv.fss.es.EsManager.SearchId.FssByTrailSearch;

/**
 * Created by User on 2017/8/3.
 */

public class EsManager {

    protected static final Logger LOG = LogManager.getLogger(EsManager.class);
    private static boolean hdfsFailed = false;
    private static ExecutorService searchPool = null;

    public static String searchId = "";

    //从配置文件读取的参数
    public static String esIp;
    public static String esClusterName;
    public static String esExactSearchResult;
    private static String esServerIp;
    private static String esHttpPort;
    private static String fssEsIndexHistoryName;
    public static String fssEsIndexHistoryPrefix;
    public static String fssEsIndexHistoryType;
    private static String humTemplateName;
    private static String ariTemplateName;
    private static String fastTemplateName;
    private static String indexPersonListName;
    private static String indexPersonListType;
    private static String indexAlarmName;
    private static String indexAlarmType;
    private static String personListTemplateName;
    private static String personListCountTemplateName;
    private static String historyPersonCountTemplateName;
    private static String alarmPersonCountTemplateName;
    private static String alarmSearchTemplateName;

    private static String indexLogType;
    private static String indexLogName;
    public static final String LOPQ_MODEL_FILE = "/lopq/lopq_model_V1.0_D512_C36.lopq";
    //获取client，将查询结果写入到es中
    private static WriteDataToES esClient = new WriteDataToES();
    //读取配置文件失败，将异常抛出以便web捕获
    public static void initConnection(String hostUrl) throws Exception {
        EsConfig.getProp(hostUrl);
        //从配置文件读取的参数

        esClusterName = EsConfig.getProperty(VConstants.ES_CLUSTER_NAME);
        esExactSearchResult = EsConfig.getProperty(VConstants.INDEX_EXACT_SEARCH_RESULT);
        esServerIp = EsConfig.getProperty(VConstants.ES_SERVER_IP);
        esHttpPort = EsConfig.getProperty(VConstants.ES_HTTP_PORT);
        fssEsIndexHistoryName = EsConfig.getProperty(VConstants.FSS_ES_INDEX_HISTORY_NAME);
        fssEsIndexHistoryPrefix = EsConfig.getProperty(VConstants.FSS_ES_INDEX_HISTORY_Prefix);//"test_history_fss_data_v1_2-";
        fssEsIndexHistoryType = EsConfig.getProperty(VConstants.FSS_ES_INDEX_HISTORY_TYPE);
        humTemplateName = EsConfig.getProperty(VConstants.FSS_ES_SEARCH_TEMPLATE_FLOWCOUNT_ID);
        ariTemplateName = EsConfig.getProperty(VConstants.FSS_ES_SEARCH_TEMPLATE_FACESEARCH_ID);
        fastTemplateName = EsConfig.getProperty(VConstants.FSS_ES_SEARCH_TEMPLATE_FASTFEATURE_ID);
        indexPersonListName = EsConfig.getProperty(VConstants.INDEX_PERSON_LIST_NAME);
        indexPersonListType = EsConfig.getProperty(VConstants.INDEX_PERSON_LIST_TYPE);
        indexAlarmName = EsConfig.getProperty(VConstants.INDEX_ALARM_NAME);
        indexAlarmType = EsConfig.getProperty(VConstants.INDEX_ALARM_TYPE);
        personListTemplateName = EsConfig.getProperty(VConstants.FSS_ES_SEARCH_TEMPLATE_PERSONLIST_ID);
        personListCountTemplateName = EsConfig.getProperty(VConstants.FSS_ES_SEARCH_TEMPLATE_PERSONLIST_COUNT_ID);
        historyPersonCountTemplateName = EsConfig.getProperty(VConstants.FSS_ES_SEARCH_TEMPLATE_HISTORY_PERSON_COUNT_ID);
        alarmPersonCountTemplateName = EsConfig.getProperty(VConstants.FSS_ES_SEARCH_TEMPLATE_ALARM_PERSON_COUNT_ID);
        alarmSearchTemplateName = EsConfig.getProperty(VConstants.FSS_ES_SEARCH_TEMPLATE_ALARM_SEARCH_ID);

        indexLogName = EsConfig.getProperty(VConstants.INDEX_LOG_NAME);
        indexLogType = EsConfig.getProperty(VConstants.INDEX_LOG_TYPE);
        String[] esIpString = esServerIp.split("//");
        esIp = esIpString[1] ;//EsConfig.getProperty(VConstants.ES_IP);
        int searchPoolNum = Integer.parseInt(EsConfig.getPropertySDK().getProperty(VConstants.ES_SEARCH_POOL_NUM, "20"));

        LOPQModel.loadProto(FeatureFieldMapper.class.getResourceAsStream(LOPQ_MODEL_FILE));

        // 创建线程池，控制并发数
        final String n = Thread.currentThread().getName();
        searchPool = Executors.newFixedThreadPool(searchPoolNum, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(n + "-search-" + System.currentTimeMillis());
                return t;
            }
        });

        esClient.initClient(esClusterName,esIp);
    }

    public static ExecutorService getExecutor()  {
        return searchPool;
    }

    public static void close() {

        if (searchPool != null) {
            searchPool.shutdown();
        }
    }

    public static String concatenateURL(String indexName, String typeName) {
        return esServerIp + ":" + esHttpPort + "/" + indexName + "/" + typeName + "/_search/template";
    }

    public static String insertURL(String indexName, String typeName) {
        return esServerIp + ":" + esHttpPort + "/" + indexName + "/" + typeName;
    }

    public static String toBatchJSon(List list) {
        StringBuffer sb = new StringBuffer();
        for (@SuppressWarnings("rawtypes")
             Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            JSONObject json = (JSONObject) JSONObject.toJSON(iterator.next());
            String str = "{ \"index\" : {} }";
            sb.append(str).append("\n");
            sb.append(json).append("\n");
        }
        return sb.toString();
    }

    public static BaseEsSearch createSearch(String params) {
        BaseEsSearch search = null;
        JSONObject searchResult = new JSONObject(true);
        try {
            JSONObject paramId = JSON.parseObject(params);
            if (paramId.containsKey("id")) {
                searchId = paramId.getString("id");
                SearchId sId = SearchId.getSearchId(searchId);
                if (sId == null) {
                    searchResult.put("errorCode", FssErrorCodeEnum.ES_INVALID_PARAM.getCode());
                    search = new BaseEsSearch();
                    search.setJsonResult(searchResult);
                } else {
                    if (EsConfig.getProperty() == null || hdfsFailed) {
                        searchResult.put("errorCode", FssErrorCodeEnum.ES_HDFS_FAILED_READ.getCode());
                        search = new BaseEsSearch();
                        search.setJsonResult(searchResult);
                    } else {

                        String esurl = concatenateURL(fssEsIndexHistoryName, fssEsIndexHistoryType);
                        String esurl1 = concatenateURL(indexPersonListName, indexPersonListType);
                        String esurl2 = concatenateURL(indexAlarmName, indexAlarmType);

                        String esurl4 = insertURL(indexLogName, indexLogType);

                        switch (sId) {
                            case HumanStrangerTraffic:
                                search = new HumanStrangerTraffic(esurl, humTemplateName);
                                break;
                            case FssArbitrarySearch:
                                search = new FssArbitrarySearch(esurl, ariTemplateName);
                                break;
                            case FastFeatureSearch:
                                search = new FastFeatureSearch(esurl, fastTemplateName);
                                break;
                            case FssPersonListSearch:
                                search = new FssPersonListSearch(esurl1, personListTemplateName);
                                break;
                            case FssPersonListCount:
                                search = new FssPersonListCount(esurl1, personListCountTemplateName);
                                break;
                            case FssHistoryPersonCount:
                                search = new FssHistoryPersonCount(esurl, historyPersonCountTemplateName);
                                break;
                            case FssAlarmPersonCount:
                                search = new FssAlarmPersonCount(esurl2, alarmPersonCountTemplateName);
                                break;
                            case FssAlarmTypeSearch:
                                search = new AlarmTypeSearch(esurl2, alarmSearchTemplateName);
                                break;
                            case LogInsert:
//                                search = new WriteLogToEs(esurl4);
//                                search = new WriteLogToEsBulkHttpPost(esurl4, indexLogName);
                                search = new WriteLogToEsBulk(esurl4 + "/_bulk");
                                break;
                            case MultiIndexExactSearch:
                                search = new MultiIndexExactSearch(esurl, ariTemplateName);
                                break;
                            case MultiIndexFastSearch:
                                search = new MultiIndexFastSearch(esurl, fastTemplateName);
                                break;
                            case FssByTrailSearch:
                                search = new FssByTrailSearch(esurl, fastTemplateName);
                                break;
                            default:
                                LOG.info("查询的报表不存在。");
                                break;
                        }
                    }
                }
            } else {
                searchResult.put("errorCode", FssErrorCodeEnum.ES_INVALID_PARAM.getCode());
                search = new BaseEsSearch();
                search.setJsonResult(searchResult);
            }

        } catch (JSONException e) {
            search = new BaseEsSearch();
            searchResult.put("errorCode", FssErrorCodeEnum.ES_INVALID_PARAM.getCode());
            search.setJsonResult(searchResult);
            LOG.error("参数解析错误，请检查格式",e);
        }

        return search;
    }

    /**
     * @Description: 查询类型枚举
     */
    public enum SearchId {
        FssArbitrarySearch("FSS任意条件搜索", "13001"), HumanStrangerTraffic("人流量与陌生人统计", "13002"), FastFeatureSearch("超级检索",
                "13003"), FssPersonListSearch("名单库搜索", "13004"), FssPersonListCount("名单库统计", "13005"), FssHistoryPersonCount("历史表统计", "13006"), FssAlarmPersonCount("告警表统计", "13007"),
        FssAlarmTypeSearch("告警事件检索", "13008"), LogInsert("日志写入", "13009"),MultiIndexExactSearch("多索引精确检索","13010"),MultiIndexFastSearch("多索引极速检索","13011"),FssByTrailSearch("轨迹查询","13012");

        // 成员变量
        private String s_name;
        private String s_id;

        // 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
        SearchId(String name, String id) {
            s_name = name;
            s_id = id;
        }

        public String getName() {
            return s_name;
        }

        public String getId() {
            return s_id;
        }

        public static SearchId getSearchId(String id) {
            switch (id) {
                case "13001":
                    return FssArbitrarySearch;
                case "13002":
                    return HumanStrangerTraffic;
                case "13003":
                    return FastFeatureSearch;
                case "13004":
                    return FssPersonListSearch;
                case "13005":
                    return FssPersonListCount;
                case "13006":
                    return FssHistoryPersonCount;
                case "13007":
                    return FssAlarmPersonCount;
                case "13008":
                    return FssAlarmTypeSearch;
                case "13009":
                    return LogInsert;
                case "13010":
                    return MultiIndexExactSearch;
                case "13011":
                    return MultiIndexFastSearch;
                case "13012":
                    return FssByTrailSearch;
                default:
                    break;
            }
            return null;
        }
    }


}
