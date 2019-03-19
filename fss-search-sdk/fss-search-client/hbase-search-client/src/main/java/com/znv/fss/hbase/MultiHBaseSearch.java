package com.znv.fss.hbase;

/**
 * Created by ZNV on 2016/6/27.
 */

import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * MultiHBaseSearch
 */
public class MultiHBaseSearch {
    private final Log log = LogFactory.getLog(MultiHBaseSearch.class);

    private Object jsonResultObj;
    protected int timeOutMinute = 2; // 默认超时时间为2分钟
    private ExecutorService threadPool;
    protected String id;

    protected MultiHBaseSearch(String poolType) {
        threadPool = HBaseConfig.getExecutor(poolType);
    }

    public String getJsonResult(String jsonParamStr) throws Exception {
        Object result = getJsonResultAsObj(jsonParamStr);
        return result.toString();
    }

    public JSONObject getJsonResult(JSONObject jsonParamObj) throws Exception {
        Object result = getJsonResultAsObj(jsonParamObj);
        return (JSONObject) result;
    }

    protected Object getJsonResultAsObj(Object jsonParamObj) throws Exception {
        JSONObject checkResult = checkFeaturePoints();
        if (!checkResult.isEmpty()) {
            return checkResult;
        }
        try {
            CountDownLatch threadSignal = new CountDownLatch(1);
            Thread t = new SearchRequest(threadSignal, this, jsonParamObj);
            threadPool.execute(t);
            boolean ret = threadSignal.await(timeOutMinute, TimeUnit.MINUTES); // 2分钟是否合理？后期关注
            if (!ret) {
                log.info("ThreadSignal await failure !!");
                return getTimeOut(FssErrorCodeEnum.HBASE_TIMEOUT.getCode());
            }
        } catch (RejectedExecutionException ree) {
            log.error("Could not execute search !! Exception: \n", ree);
            return getTimeOut(FssErrorCodeEnum.HBASE_GET_EXCEPTION.getCode());
        } catch (Exception e) {
            log.error(e);
            return getTimeOut(FssErrorCodeEnum.HBASE_GET_EXCEPTION.getCode());
        }
        return this.getJsonResultObj();
    }

    /**
     * @return
     */
    protected JSONObject checkFeaturePoints() {
        JSONObject result = new JSONObject();
        JSONObject reportService = new JSONObject();

        if (id.equals(HBaseManager.SearchId.SearchByImage.getId()) || id.equals(HBaseManager.SearchId.SearchForPeer.getId()) ||
                id.equals(HBaseManager.SearchId.SearchByTrial.getId()) || id.equals(HBaseManager.SearchId.RelationshipSearch.getId()) ||
                id.equals(HBaseManager.SearchId.StayTimeSearch.getId())) {
            try {
                HBaseConfig.getFeaturePoints();
            } catch (Exception e) {
                reportService.put("errorCode", FssErrorCodeEnum.SENSETIME_FEATURE_POINTS_ERROR.getCode());
                reportService.put("id", id);
                reportService.put("type", "response");
                result.put("reportService", reportService);
            }
        }
        return result;
    }

    //添加接口，测试超时问题
    protected JSONObject getTimeOut(int errorCode) {
        JSONObject result = new JSONObject();
        JSONObject reportService = new JSONObject();

        if (id.equals(HBaseManager.SearchId.SearchByImage.getId()) || id.equals(HBaseManager.SearchId.ReadMOB.getId())) {
            reportService.put("errorCode", errorCode);
        } else {
            reportService.put("errorCode", JsonResultType.TIMEOUT);
        }
        reportService.put("id", id);
        reportService.put("type", "response");
        result.put("reportService", reportService);

        return result;
    }


    /**
     * SearchRequest
     */
    protected static class SearchRequest extends Thread {
        private final Log log = LogFactory.getLog(SearchRequest.class);
        private CountDownLatch threadsSignal;
        private Object jsonParam = null;
        private MultiHBaseSearch client = null;

        public SearchRequest(CountDownLatch threadsSignal, MultiHBaseSearch client, Object jsonParam) {
            this.threadsSignal = threadsSignal;
            this.client = client;
            this.jsonParam = jsonParam;
        }

        @Override
        public void run() {
            try {
                if (jsonParam instanceof JSONObject) {
                    JSONObject jsonResult = client.requestSearch((JSONObject) jsonParam);
                    client.setJsonResultObj(jsonResult);
                } else {
                    String jsonResult = client.requestSearch(jsonParam.toString());
                    client.setJsonResultObj(jsonResult);
                }
            } catch (Exception e) {
                log.error(e);
            }
            // 线程结束时计数器减1
            threadsSignal.countDown();
        }
    }

    protected String requestSearch(String jsonParamStr) throws Exception {
        return null;
    }

    protected JSONObject requestSearch(JSONObject jsonParamObj) throws Exception {
        return null;
    }

    public String cancelScan(String jsonParamStr) throws Exception {
        return null;
    }

    public Object getJsonResultObj() {
        return jsonResultObj;
    }

    public void setJsonResultObj(Object jsonResultObj) {
        this.jsonResultObj = jsonResultObj;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
