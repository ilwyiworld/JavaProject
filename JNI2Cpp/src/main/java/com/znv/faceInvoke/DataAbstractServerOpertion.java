package com.znv.faceInvoke;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/1/8.
 */
public class DataAbstractServerOpertion {

    protected static Map<String,JSONObject> taskMap;         //任务params Map
    protected static final Logger logger = LogManager.getLogger(DataAbstractServerOpertion.class);

    public DataAbstractServerOpertion(String cLibraryLocation){
        //初始化c++ so库
        CallBackFunction.initConnection(cLibraryLocation);
        taskMap=new HashMap<>();
    }

    //摄像头分析路数查询
    public JSONObject getServerInfo(){
        JSONObject obj=new JSONObject();
        JSONArray idArrays=new JSONArray();
        try {
            int current_task_num=CallBackFunction.getCurrent_task_num();
            int maxTaskNum=CallBackFunction.getMaxTaskNum();
            Map<String,Boolean> taskIsEndMap=CallBackFunction.getTaskIsEndMap();
            for(Map.Entry<String, Boolean> entry : taskIsEndMap.entrySet()){
                if(!entry.getValue()){
                    idArrays.add(entry.getKey());
                }
            }
            obj.put("taskIds",idArrays);
            obj.put("usedNum",current_task_num);
            obj.put("totalNum",maxTaskNum-current_task_num);
            obj.put("returnCode",1);
        }catch(Exception e){
            logger.error("摄像头分析路数查询失败",e);
            obj.put("returnCode",0);
        }
        return obj;
    }

    //侦测任务的创建，修改
    public JSONObject updateTask(JSONObject params){
        JSONObject obj=new JSONObject();
        String task_id=params.getString("task_id");
        obj.put("task_id",task_id);
        Map<String,Boolean> taskIsEndMap=CallBackFunction.getTaskIsEndMap();
        try{
            if( !taskIsEndMap.containsKey(task_id) ){
                //如果taskIsEndMap中不存在，即为新建任务
                CallBackFunction.startAnalyseTask(params);
            }else if(taskIsEndMap.containsKey(task_id) && taskIsEndMap.get(task_id)){
                //如果taskIsEndMap中存在，但任务已结束，则新建任务
                CallBackFunction.startAnalyseTask(params);
            } else if(taskIsEndMap.containsKey(task_id) && !taskIsEndMap.get(task_id)){
                //如果taskIsEndMap中存在，但任务未结束
                //更新已有任务的source_addr、model_path需要先结束当前任务，再重新初始化
                //如果只是更新参数，这重新调用Cinitial()方法即可
                if(params.getString("updateType").equals("2")){
                    //表示只更新参数
                    CallBackFunction.refreshAnalyseTask(params);
                }else if(params.getString("updateType").equals("3")){
                    //表示更新了source_addr或者model_path  需要先暂停任务再重新开始
                    CallBackFunction.stopAnalyseTask(task_id);
                    while(true) {
                        //每次循环都要重新获取taskIsEndMap 里面的数据已更新
                        if(CallBackFunction.getTaskIsEndMap().containsKey(task_id)
                                && CallBackFunction.getTaskIsEndMap().get(task_id)) {
                            //如果taskIsEndMap中存在，且任务已结束（即上一步停止成功） 再重新开始任务
                            CallBackFunction.startAnalyseTask(params);
                            break;
                        }
                    }
                }
            }
            obj.put("returnCode",1);
            taskMap.put(task_id,params);
        }catch(Exception e){
            obj.put("returnCode",0);
            logger.error("创建任务："+task_id+"失败",e);
        }
        return obj;
    }

    //侦测任务删除
    public JSONObject deleteTask(String task_id){
        JSONObject obj=new JSONObject();
        Map<String,Boolean> taskIsEndMap=CallBackFunction.getTaskIsEndMap();
        try{
            if(!taskIsEndMap.containsKey(task_id) ){
                //如果taskIsEndMap中不存在
                obj.put("returnCode",-1);
                logger.error("该任务不存在或者已被删除");
            }else if(taskIsEndMap.containsKey(task_id) &&taskIsEndMap.get(task_id)){
                //如果taskIsEndMap中存在，但任务已结束
                obj.put("returnCode",-1);
                logger.error("该任务已结束，无需删除");
            } else if(taskIsEndMap.containsKey(task_id) && !taskIsEndMap.get(task_id)){
                //如果taskIsEndMap中存在，但任务未结束，则停止任务
                CallBackFunction.stopAnalyseTask(task_id);
                obj.put("returnCode",1);
            }
            taskMap.remove(task_id);
        }catch(Exception e){
            obj.put("returnCode",0);
            logger.error("删除任务："+task_id+"失败",e);
        }
        return obj;
    }

    //任务详细信息查看
    public JSONObject getTaskInfo(String task_id){
        JSONObject obj=new JSONObject();
        Map<String,Boolean> taskIsEndMap=CallBackFunction.getTaskIsEndMap();
        try{
            if( !taskIsEndMap.containsKey(task_id) ){
                //如果taskIsEndMap中不存在
                obj.put("returnCode",-1);
                logger.error("该任务不存在或者已被删除");
            }else if(taskIsEndMap.containsKey(task_id) && taskIsEndMap.get(task_id)){
                //如果taskIsEndMap中存在，但任务已结束
                if(taskMap.containsKey(task_id)){
                    obj.put("params",taskMap.get(task_id));
                    obj.put("returnCode",1);
                }
                logger.warn("该任务已结束");
            } else if(taskIsEndMap.containsKey(task_id) && !taskIsEndMap.get(task_id)){
                //如果taskIsEndMap中存在，且任务未结束
                if(taskMap.containsKey(task_id)){
                    obj.put("params",taskMap.get(task_id));
                    obj.put("returnCode",1);
                }
                logger.info("该任务未结束");
            }
        }catch(Exception e){
            obj.put("returnCode",0);
            logger.error("查询任务："+task_id+"失败",e);
        }
        return obj;
    }
}
