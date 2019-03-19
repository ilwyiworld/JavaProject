package com.znv.faceInvoke;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Administrator on 2017/12/25.
 */
public class Data_Realize implements CLibrary.CallBack_Result{
    private static final Logger logger = LogManager.getLogger(Data_Realize.class);
    public void ReceiveResult(CallbackResult.ByReference result) {
        //logger.info("开始解析c++返回的参数");
        try{
            String uuid=result.uuid;
            String deviceIp=result.deviceIp;
            String taskId=result.taskId;
            String faceImg=result.faceImg;
            double score=result.score;
            int trackId=result.trackId;
            int fX=result.fX;
            int fY=result.fY;
            int fWidth=result.fWidth;
            int fHeight=result.fHeight;
            int type=result.type;
            String body=result.body;
            String path=taskId.split("_")[2];
            Base64Util.generateImage(faceImg,uuid,path);
            if(type==0){
                logger.info("taskId:"+taskId+" trackid:"+trackId+" 结束跟踪");
            }
            //任务分析结束 将taskPidMap中的key移除掉，相当于增加一路
            if(score<0 && CallBackFunction.taskIsEndMap.containsKey(taskId)){
                CallBackFunction.taskIsEndMap.put(taskId,true);     //修改taskIsEndMap中的是否停止标识
                logger.warn(taskId+"分析任务结束或被强制停止");
            }
        }catch (Exception e){
            logger.error("解析c++返回的参数出错",e);
        }
    }
}
