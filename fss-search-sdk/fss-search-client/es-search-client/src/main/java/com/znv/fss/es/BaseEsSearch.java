package com.znv.fss.es;

import com.alibaba.fastjson.JSONObject;

import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by User on 2017/8/4.
 */
public class BaseEsSearch {

    private final Log log = LogFactory.getLog(BaseEsSearch.class);
    private JSONObject jsonResult = new JSONObject();
    protected HttpConnection httpConnection = new HttpConnection();

    public StringBuffer getSearchResult(JSONObject obj) {
        httpConnection.esHttpPost(obj);
        StringBuffer sb = null;
        try {
            sb = httpConnection.esHttpGet();
        } catch (IllegalArgumentException e) {
            StringBuffer strBuf = new StringBuffer(FssErrorCodeEnum.SENSETIME_FEATURE_POINTS_ERROR.getCode());
            return strBuf;
        } catch (Exception e) {
            StringBuffer strBuf = new StringBuffer(FssErrorCodeEnum.ES_GET_EXCEPTION.getCode());
            return strBuf;
        } finally {
            httpConnection.esHttpClose();
        }
        return sb;
    }

    public StringBuffer getSearchResultString(String obj) {
        httpConnection.esHttpPost(obj);
        StringBuffer sb = null;
        try {
            sb = httpConnection.esHttpGet();
        } catch (Exception e) {
            return null;
        } finally {
            httpConnection.esHttpClose();
        }
        return sb;
    }

    public JSONObject getSearchResult(String jsonParamStr) throws Exception {
        if (this.jsonResult.containsKey("errorCode")) {
            return this.jsonResult;
        } else {
            ExecutorService searchPool = EsManager.getExecutor();
            try {
                CountDownLatch threadSignal = new CountDownLatch(1);
                Thread t = new SearchRequest(threadSignal, this, jsonParamStr);
                searchPool.execute(t);
                boolean ret = threadSignal.await(2, TimeUnit.MINUTES); // 2分钟是否合理？后期关注
               // boolean ret = threadSignal.await(1, TimeUnit.MICROSECONDS);
                if (!ret) {
                    log.info("ThreadSignal await failure !!");
                    JSONObject errorCode = new JSONObject();
                    errorCode.put("errorCode", FssErrorCodeEnum.ES_TIMEOUT_EXCEPTION.getCode());
                    return errorCode;
                }
            } catch (RejectedExecutionException ree) {
                log.error("Could not execute search !! Exception: \n" + ree);
            } catch (IllegalArgumentException e) { // [lq-add]
                log.info("get sensetime points from hdfs fail !!");
                JSONObject errorCode = new JSONObject();
                errorCode.put("errorCode", FssErrorCodeEnum.SENSETIME_FEATURE_POINTS_ERROR.getCode());
                return errorCode;
            }
        }
        return this.getJsonResult();
    }

    /**      */
    protected static class SearchRequest extends Thread {
        private final Log log = LogFactory.getLog(SearchRequest.class);
        private CountDownLatch threadsSignal;
        private String jsonParamStr = null;
        private BaseEsSearch client = null;

        public SearchRequest(CountDownLatch threadsSignal, BaseEsSearch client, String jsonParamStr) {
            this.threadsSignal = threadsSignal;
            this.client = client;
            this.jsonParamStr = jsonParamStr;
        }

        @Override
        public void run() {
            try {
                JSONObject jsonResult = client.requestSearch(jsonParamStr);
                client.setJsonResult(jsonResult);
            } catch (Exception e) {
                log.error(e);
            }
            // 线程结束时计数器减1
            threadsSignal.countDown();
        }
    }

    protected JSONObject requestSearch(String jsonParamStr) throws Exception {
        return null;
    }

    protected void setJsonResult(JSONObject jsonResult) {
        this.jsonResult = jsonResult;
    }

    protected JSONObject getJsonResult() {

        return this.jsonResult;
    }

    protected JSONObject paramCheck(String inParam) {
        return null;
    }

    public JSONObject getErrorResult(int errCode) {
        JSONObject result = new JSONObject();
        result.put("errorCode", errCode);
        result.put("total", 0);
        return result;
    }

}
