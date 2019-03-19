package com.znv.faceInvoke;

import com.alibaba.fastjson.JSONObject;
import com.sun.jna.Native;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Administrator on 2017/12/19.
 */
public class CallBackFunction {

    protected static final Logger logger = LogManager.getLogger(CallBackFunction.class);
    protected static final int MAX_TASK_NUM = 16;
    //开始任务、结束任务、刷新任务线程池
    protected static ExecutorService startPool = null;
    protected static ExecutorService stopPool = null;
    protected static ExecutorService refreshPool = null;


    // key:task_id
    // value:isEnd 是否停止的标识 true:已停止，false:未停止
    protected static Map<String,Boolean> taskIsEndMap = null;

    public static int getMaxTaskNum() {
        return MAX_TASK_NUM;
    }

    public static int getCurrent_task_num() {
        int current_task_num=0;
        for(Boolean isEnd:taskIsEndMap.values()){
            if(!isEnd){
                current_task_num++;
            }
        }
        return current_task_num;
    }

    public static Map<String, Boolean> getTaskIsEndMap() {
        return taskIsEndMap;
    }

    protected static CLibrary instanceSo  = null;

    protected static void initConnection(String cLibraryLocation) {
        if(startPool==null){
            startPool=getStartExecutorService();
        }
        if(stopPool==null){
            stopPool=getStopExecutorService();
        }
        if(refreshPool==null){
            refreshPool=getRefreshExecutorService();
        }
        if(taskIsEndMap==null){
            taskIsEndMap=new HashMap<>();
        }
        if(instanceSo==null){
            instanceSo=(CLibrary) Native.loadLibrary(cLibraryLocation,CLibrary.class);
        }
    }

    //开始分析视频任务
    private static void startAnalyseVideo(JSONObject obj) {
        String task_name = obj.getString("task_name");
        String task_id = obj.getString("task_id");
        String source_addr = obj.getString("source_addr");
        String deviceIp = obj.getString("deviceIp");
        String model_path = obj.getString("model_path");
        int Max_img_blur = obj.getInteger("Max_img_blur");
        int Optimise_interval = obj.getInteger("Optimise_interval");
        int miniSize = obj.getInteger("miniSize");
        int loop_video = obj.getInteger("loop_video");
        logger.warn(task_id+"开始发送视频流");
        TaskParams.ByValue params = new TaskParams.ByValue();
        double[] nms_th = {0.7, 0.8, 0.9};
        double[] scalesAll = {0.7, 0.5, 0.35};
        params.nms_th = nms_th;
        params.scalesAll = scalesAll;
        params.miniSize = miniSize;
        params.factorCountAll = 3;
        params.Max_img_blur=Max_img_blur;
        params.Optimise_interval=Optimise_interval;
        params.task_name=task_name;
        params.task_id=task_id;
        params.source_addr=source_addr;
        params.deviceIp=deviceIp;
        params.bg_img_addr = "/data/test1";
        params.face_info_addr = "/data/test2";
        params.model_path=model_path;
        params.loop_video=loop_video;
        CLibrary.CallBack_Result callBack_Result = new Data_Realize();
        try{
            instanceSo.Cinitial(params, callBack_Result);
            taskIsEndMap.put(task_id,false);
            //Cdetect放在最后，因为是一个循环过程
            instanceSo.Cdetect(task_id);
        }catch(Exception e){
            logger.error("视频流解析出错"+e);
        }
        logger.warn("视频流"+task_id+"解析完毕");
    }

    //停止分析视频任务
    private static void stopAnalyseVideo(String task_id) {
        logger.warn("准备停止任务"+task_id);
        if(!taskIsEndMap.containsKey(task_id) || taskIsEndMap.get(task_id) ){
            logger.warn("当前任务不存在或已停止或已结束");
            return;
        }
        try {
            instanceSo.Cstop(task_id);
        }catch (Exception e){
            logger.error("停止任务："+task_id+" 失败",e);
        }
    }

    //刷新分析视频任务
    private static void refreshAnalyseVideo(JSONObject obj) {
        String task_id=obj.getString("task_id");
        logger.warn("准备刷新任务"+task_id);
        if(!taskIsEndMap.containsKey(task_id) || taskIsEndMap.get(task_id) ){
            logger.warn("当前任务不存在或已停止或已结束");
            return;
        }
        try {
            String task_name = obj.getString("task_name");
            String source_addr = obj.getString("source_addr");
            String deviceIp = obj.getString("deviceIp");
            String model_path = obj.getString("model_path");
            int Max_img_blur = obj.getInteger("Max_img_blur");
            int Optimise_interval = obj.getInteger("Optimise_interval");
            int miniSize = obj.getInteger("miniSize");
            logger.warn(task_id+"开始刷新任务");
            TaskParams.ByValue params = new TaskParams.ByValue();
            double[] nms_th = {0.7, 0.8, 0.9};
            double[] scalesAll = {0.7, 0.5, 0.35};
            params.nms_th = nms_th;
            params.scalesAll = scalesAll;
            params.miniSize = miniSize;
            params.factorCountAll = 3;
            params.Max_img_blur=Max_img_blur;
            params.Optimise_interval=Optimise_interval;
            params.task_name=task_name;
            params.task_id=task_id;
            params.source_addr=source_addr;
            params.deviceIp=deviceIp;
            params.bg_img_addr = "/data/test1";
            params.face_info_addr = "/data/test2";
            params.model_path=model_path;
            CLibrary.CallBack_Result callBack_Result = new Data_Realize();
            instanceSo.Cinitial(params, callBack_Result);
        }catch (Exception e){
            logger.error("刷新任务："+task_id+" 失败",e);
        }
    }

    //开始分析任务线程类
    protected static class startInstance extends Thread {
        private final Logger logger = LogManager.getLogger(startInstance.class);
        private JSONObject jsonParam = null;

        public startInstance(JSONObject jsonParam) {
            this.jsonParam = jsonParam;
        }

        public void run() {
            if ((this.jsonParam != null)) {
                startAnalyseVideo(jsonParam);
            } else {
                logger.error("参数不能为空");
            }
        }
    }

    //停止分析任务线程类
    protected static class stopInstance extends Thread {
        private final Logger logger = LogManager.getLogger(stopInstance.class);
        private String task_id = null;

        public stopInstance(String task_id) {
            this.task_id = task_id;
        }

        public void run() {
            if ((this.task_id != null)) {
                stopAnalyseVideo(task_id);
            } else {
                logger.error("参数不能为空");
            }
        }
    }

    //刷新分析任务线程类
    protected static class refreshInstance extends Thread {
        private final Logger logger = LogManager.getLogger(refreshInstance.class);
        private JSONObject jsonParam = null;

        public refreshInstance(JSONObject jsonParam) {
            this.jsonParam = jsonParam;
        }

        public void run() {
            if ((this.jsonParam != null)) {
                refreshAnalyseVideo(jsonParam);
            } else {
                logger.error("参数不能为空");
            }
        }
    }

    //开始任务线程池
    public static ExecutorService getStartExecutorService() {
        String name = Thread.currentThread().getName();
        ExecutorService exec =Executors.newFixedThreadPool(MAX_TASK_NUM, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(name + "-start-" + System.currentTimeMillis());
                return t;
            }
        });
        return exec;
    }

    //结束任务线程池
    public static ExecutorService getStopExecutorService() {
        String name = Thread.currentThread().getName();
        ExecutorService exec =Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(name + "-stop-" + System.currentTimeMillis());
                return t;
            }
        });
        return exec;
    }

    //刷新任务线程池
    public static ExecutorService getRefreshExecutorService() {
        String name = Thread.currentThread().getName();
        ExecutorService exec =Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(name + "-refresh-" + System.currentTimeMillis());
                return t;
            }
        });
        return exec;
    }

    //开始分析任务调用方法
    public static void startAnalyseTask(JSONObject jsonParamStr){
        //int leftThreadNum=MAX_TASK_NUM-((ThreadPoolExecutor)searchPool).getActiveCount();
        int current_task_num=getCurrent_task_num();
        if(current_task_num>=MAX_TASK_NUM){
            logger.error("当前已创建"+MAX_TASK_NUM+"路分析任务，无法新建任务");
            return;
        }
        if(StringUtils.isNotEmpty(jsonParamStr.getString("task_id"))){
            if(taskIsEndMap.containsKey(jsonParamStr.getString("task_id"))
                    && !taskIsEndMap.get(jsonParamStr.getString("task_id"))){
                logger.error(jsonParamStr.getString("task_id")+"任务启动失败，因为该任务已存在且还未结束");
                return;
            }
        }

        try {
            Thread t = new startInstance(jsonParamStr);
            startPool.execute(t);
        } catch (Exception e) {
            logger.error("Could not execute analyse !! Exception: \n" + e);
        }
    }

    //停止分析任务调用方法
    public static void stopAnalyseTask(String task_id){
        try {
            Thread t = new stopInstance(task_id);
            stopPool.execute(t);
        } catch (Exception e) {
            logger.error("Could not execute stop task !! Exception: \n" + e);
        }
    }

    //刷新分析任务调用方法
    public static void refreshAnalyseTask(JSONObject jsonParamStr){
        try {
            Thread t = new refreshInstance(jsonParamStr);
            refreshPool.execute(t);
        } catch (Exception e) {
            logger.error("Could not execute refresh task !! Exception: \n" + e);
        }
    }

    public static void main(String[] args) {
        //DataAbstractServerOpertion das=new DataAbstractServerOpertion("/data/lrj2904/ffmpeg-rstp/dll_test/libdll_detect.so");
        DataAbstractServerOpertion das=new DataAbstractServerOpertion("/data/lrj2904/ffmpeg-rstp/dll_test/test_so/libdll_detect_byte.so");
        JSONObject obj1=new JSONObject();
        obj1.put("model_path","/data/lrj2904/ffmpeg-rstp/MTmodel");
        obj1.put("task_name","task_name_rtsp");
        obj1.put("task_id","task_id_rtsp");
        obj1.put("Max_img_blur",40);
        obj1.put("miniSize",45);
        obj1.put("Optimise_interval",20);
        obj1.put("source_addr","rtsp://admin:Znv123456@10.45.144.231:554/Streaming/Channels/101?transportmode=unicast&profile=Profile_1");
        obj1.put("deviceIp","x.x.x.x");
        obj1.put("deviceIp","x.x.x.x");
        obj1.put("loop_video",0);
        obj1.put("updateType","1");      //表示新建任务
        das.updateTask(obj1);

        JSONObject obj2=new JSONObject();
        obj2.put("model_path","/data/lrj2904/ffmpeg-rstp/MTmodel");
        obj2.put("task_name","task_name_video");
        obj2.put("task_id","task_id_video");
        obj2.put("Max_img_blur",40);
        obj2.put("miniSize",45);
        obj2.put("Optimise_interval",20);
        obj2.put("source_addr","/data/video/nanjing7_cut.mp4");
        obj2.put("deviceIp","x.x.x.x");
        obj2.put("loop_video",1);
        obj2.put("updateType","1");      //表示新建任务
        das.updateTask(obj2);

        try{
            Thread.sleep(1000*30);
            das.deleteTask("task_id_rtsp");
        }catch(Exception e){

        }

        /*try {
            for (int i = 0; i < 16; i++) {
                JSONObject obj=new JSONObject();
                obj.put("model_path","/data/lrj2904/ffmpeg-rstp/MTmodel");
                obj.put("task_name","task_name_video"+i);
                obj.put("task_id","task_id_video"+i);
                obj.put("source_addr","/data/video/nanjing5.mp4");
                obj.put("miniSize",80);
                obj.put("deviceIp","x.x.x.x");
                obj.put("Max_img_blur",40);
                obj.put("Optimise_interval",20);
                obj.put("loop_video",1);
                obj.put("updateType","1");      //表示新建任务
                das.updateTask(obj);
            }
            Thread.sleep(1000*60);
            for (int i = 0; i < 8; i++) {
                String task_id="task_id_video"+i;
                das.deleteTask(task_id);
            }
            JSONObject obj1=new JSONObject();
            obj1.put("model_path","/data/lrj2904/ffmpeg-rstp/MTmodel");
            obj1.put("task_name","task_name_video");
            obj1.put("task_id","task_id_video");
            obj1.put("source_addr","/data/video/nanjing5.mp4");
            obj1.put("deviceIp","x.x.x.x");
            obj1.put("miniSize",80);
            obj1.put("Max_img_blur",40);
            obj1.put("Optimise_interval",20);
            obj1.put("updateType","1");      //表示新建任务
            das.updateTask(obj1);
            Thread.sleep(1000*30);

            JSONObject obj2=new JSONObject();
            obj2.put("model_path","/data/lrj2904/ffmpeg-rstp/MTmodel");
            obj2.put("task_name","task_name_video");
            obj2.put("task_id","task_id_video");
            obj2.put("source_addr","/data/video/nanjing5.mp4");
            obj2.put("deviceIp","x.x.x.x");
            obj2.put("Max_img_blur",40);
            obj2.put("Optimise_interval",20);
            obj2.put("miniSize",50);
            obj2.put("updateType","2");       //表示刷新任务
            das.updateTask(obj2);

            Thread.sleep(1000*30);
            //修改了source_addr
            JSONObject obj3=new JSONObject();
            obj3.put("model_path","/data/lrj2904/ffmpeg-rstp/MTmodel");
            obj3.put("task_name","task_name_video");
            obj3.put("task_id","task_id_video");
            obj3.put("source_addr","rtsp://admin:Znv123456@10.45.146.10:554/Streaming/Channels/101?transportmode=unicast&profile=Profile_1");
            obj3.put("deviceIp","x.x.x.x");
            obj3.put("Max_img_blur",40);
            obj3.put("Optimise_interval",20);
            obj3.put("miniSize",50);
            obj3.put("updateType","3");      //表示更新任务
            das.updateTask(obj3);

        }catch(Exception x){

        }*/
    }
}