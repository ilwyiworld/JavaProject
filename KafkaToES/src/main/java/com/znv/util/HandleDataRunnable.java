package com.znv.util;

import com.alibaba.fastjson.JSONObject;
import com.znv.kafka.ESClient.EsClientFactory;
import com.znv.kafka.ESClient.EsConnectionParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.text.SimpleDateFormat;
import java.util.*;

public class HandleDataRunnable implements Runnable {

    private final Logger logger = LogManager.getLogger(HandleDataRunnable.class);
    private ArrayList<JSONObject> dataArray;
    private String esHost;
    private String esClusterName;
    private TransportClient esClient;
    private Map<String, String> connectionParameters;
    private String queryIndexName;
    private String queryIndexType;
    private String addIndexNamePrefix;
    private String addIndexType;

    public HandleDataRunnable(ArrayList<JSONObject> dataArray) {
        this.dataArray = dataArray;
        esHost = PropertiesUtil.getProperties("config.properties").getProperty("es.transport.hosts");
        esClusterName = PropertiesUtil.getProperties("config.properties").getProperty("es.cluster.name");
        queryIndexName = PropertiesUtil.getProperties("config.properties").getProperty("queryIndexName");
        queryIndexType = PropertiesUtil.getProperties("config.properties").getProperty("queryIndexType");
        addIndexNamePrefix = PropertiesUtil.getProperties("config.properties").getProperty("addIndexName.prefix");
        addIndexType = PropertiesUtil.getProperties("config.properties").getProperty("addIndexType");
        connectionParameters = new HashMap<>();
        connectionParameters.put(EsConnectionParams.HOSTS, esHost);
        connectionParameters.put(EsConnectionParams.CLUSTER_NAME, esClusterName);
        EsClientFactory.init(connectionParameters);
        esClient = EsClientFactory.getTransportClient();
    }

    @Override
    public void run() {
        List<Map<String, Object>> ret=new ArrayList<>();
        for (JSONObject obj : dataArray) {
            String lsc_id = obj.getString("lsc_id");
            String fsu_code = obj.getString("fsu_code");
            String device_code = obj.getString("device_code");
            String mete_id = obj.getString("mete_id");
            StringBuilder id = new StringBuilder().append(lsc_id).append(fsu_code).append(device_code).append(mete_id);
            //查询ES数据
            try{
                Map<String, Object> resultMap=queryES(id.toString());
                ret.add(resultMap);
            }catch(Exception e){
                logger.error("通过_id:"+id+" 查询ES数据失败");
            }
        }
        try{
            write2ES(ret,esClient);
        }catch(Exception e){
            logger.error("批量写入ES索引失败");
        }
    }

    public Map queryES(String id) {
        String ids[] = new String[]{id};
        String indexNames[] = new String[]{queryIndexName};
        QueryBuilder queryBuilder = QueryBuilders.idsQuery().addIds(ids);
        SearchResponse response = esClient.prepareSearch().setIndices(indexNames).setTypes(queryIndexType).
                setQuery(queryBuilder).execute().actionGet();
        SearchHit hit = response.getHits().getAt(0);
        Map<String, Object> resultMap = hit.getSource();
        return resultMap;
    }

    private void write2ES(List<Map<String, Object>> ret, TransportClient esClient) {
        BulkRequestBuilder bulkRequest = esClient.prepareBulk();
        int len = ret.size();
        for (int i = 0; i < len; i++) {
            String report_time=(String)ret.get(i).get("report_time");
            String indexName=addIndexNamePrefix+getThisWeekMonday(report_time);
            bulkRequest.add(esClient.prepareIndex(indexName, addIndexType).setSource(ret.get(i)));
        }
        bulkRequest.execute().actionGet();
    }

    public static String getThisWeekMonday(String time){
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        Date date=new Date();
        try{
            date=sdf1.parse(time);
        }catch(Exception e){
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        return sdf2.format(cal.getTime());
    }
}