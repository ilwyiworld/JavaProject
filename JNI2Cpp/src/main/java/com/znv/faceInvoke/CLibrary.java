package com.znv.faceInvoke;

import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Administrator on 2017/12/20.
 */
public class CLibrary{

    // java调用C/C++函数的本地方法
    public static native int Cinitial(String params);
    public static native int Cdetect(String flag);
    public static native int Cstop(String flag);
    protected static final Logger logger = LogManager.getLogger(CLibrary.class);

    public CLibrary(String cLibraryLocation){
        // 加载动态库
        System.load(cLibraryLocation);
    }

    //c++回调java方法
    public static void ReceiveResult(String result) {
        logger.debug("开始解析c++返回的参数");
        try{
            String []results=result.split("~");
            JSONObject paramObj=new JSONObject();
            for (int i = 0; i <results.length; i++) {
                String []param=results[i].split("@");
                if(param.length<2){
                    logger.error(param[0]+":参数值有误");
                    return;
                }
                paramObj.put(param[0],param[1]);
            }
            String path=paramObj.getString("taskId").split("_")[2];
            Base64Util.generateImage(paramObj.getString("faceImg"),
                    paramObj.getString("uuid"),path);
            if(paramObj.getInteger("type")==0){
                logger.info("taskId:"+paramObj.getString("taskId")+
                        " trackid:"+paramObj.getString("trackId")+
                        " 结束跟踪");
            }
            //任务分析结束 将taskPidMap中的key移除掉，相当于增加一路
            if(paramObj.getDouble("score")<0 &&
                    CallBackFunction.taskIsEndMap.containsKey(paramObj.getString("taskId"))){
                CallBackFunction.taskIsEndMap.put(paramObj.getString("taskId"),true);     //修改taskIsEndMap中的是否停止标识
                logger.warn(paramObj.getString("taskId")+"分析任务结束或被强制停止");
            }
        }catch (Exception e){
            logger.error("解析c++返回的参数出错",e);
        }
    }

}
