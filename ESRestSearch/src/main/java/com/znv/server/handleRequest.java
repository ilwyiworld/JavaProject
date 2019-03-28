package com.znv.server;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.servlet.initConnectionServlet;
import com.znv.util.FeatureUtil;
import com.znv.util.JdbcUtil;
import com.znv.util.PhoenixUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

// 这里@Path定义了类的层次路径。
// 指定了资源类提供服务的URI路径。
@Path("/")
public class handleRequest {

    private static final Logger LOGGER = LogManager.getLogger(handleRequest.class.getName());

    @POST
    @Path("/ZNVRSearch")
    //@Path("/ZNVR/{searchType}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)   //只接受json格式的数据
    public String queryZNVR(@Context HttpServletRequest request,String queryString) {
        JSONObject returnJson=new JSONObject();
        //参数
        JSONObject paramJson= JSON.parseObject(queryString);
        String []deviceIds=paramJson.getString("device_id").split(",");
        StringBuilder deviceQuerySb=new StringBuilder();
        deviceQuerySb.append("(");
        for (String id:deviceIds){
            deviceQuerySb.append("\"").append(id).append("\",");
        }
        deviceQuerySb.deleteCharAt(deviceQuerySb.length()-1);
        deviceQuerySb.append(")");
        //获取图片特征值
        if(!paramJson.getString("pic_data").isEmpty()){
            String feature_value= FeatureUtil.getImageFeature(paramJson.getString("pic_data"));
            paramJson.remove("pic_data");
            paramJson.put("feature_value",feature_value);
            LOGGER.warn("获取特征值："+feature_value);
        }
        //查询抓拍机设备id
        StringBuilder searchSql=new StringBuilder("select fssid from t_cfg_cameraid where policeid=\"");
        //查询设备类型 按照设备类型汇总id
        String sql="select device_type,GROUP_CONCAT(device_id separator ',') as device_id " +
                "from t_cfg_device where device_id in " +deviceQuerySb.toString()+
                " group by device_type";
        LOGGER.info("查询的sql："+sql);
        List queryList=new ArrayList<>();
        try {
            queryList=JdbcUtil.query(sql);
        } catch (Exception e) {
            LOGGER.error("查询设备类型失败：",e);
        }

        JSONObject deviceTypeJson=new JSONObject();
        //遍历设备类型
        for(Object deviceTypes:queryList){
            Object[] tmp=(Object[])deviceTypes;
            int deviceType=(Integer)tmp[0];
            String deviceId=(String)tmp[1];
            LOGGER.warn("查询设备："+deviceType+":"+deviceId);
            deviceTypeJson.put(Integer.toString(deviceType),deviceId);
        }
        //汇总门禁和抓拍机数据
        List<JSONObject> resultList=new ArrayList();
        //判断设备类型 门禁
        for(String doorId: initConnectionServlet.doorDeviceId){
            if(deviceTypeJson.containsKey(doorId) && !deviceTypeJson.getString(doorId).isEmpty()){
                LOGGER.warn(doorId);
                //门禁
                String deviceId=deviceTypeJson.getString(doorId);
                String searchId="13005";
                JSONObject searchJson=new JSONObject();
                JSONObject doorJson=new JSONObject();
                doorJson.put("sim_threshold",paramJson.getString("sim_threshold"));
                doorJson.put("feature_name","face_feature");
                doorJson.put("is_calcSim",paramJson.getBoolean("is_calcSim"));
                doorJson.put("feature_value",paramJson.getString("feature_value"));
                doorJson.put("minimum_should_match",1);
                doorJson.put("event_time_start",paramJson.getString("enter_time"));
                doorJson.put("event_time_end",paramJson.getString("leave_time"));
                doorJson.put("from",0);
                doorJson.put("size",9999);
                if(!StringUtils.isEmpty(paramJson.getString("sortField"))){
                    doorJson.put("sortOrder",paramJson.getString("sortOrder"));
                    //存在排序字段
                    if("time".equals(paramJson.getString("sortField"))){
                        doorJson.put("sortField","event_time");
                    }else if("score".equals(paramJson.getString("sortField"))){
                        doorJson.put("sortField","_score");
                    }
                }
                JSONArray devIdArr=new JSONArray();
                String []deviceIdTmp=deviceId.split(",");
                for(String id:deviceIdTmp){
                    devIdArr.add(id);
                }
                doorJson.put("device_id",devIdArr);
                searchJson.put("id",searchId);
                searchJson.put("params",doorJson);
                JSONObject result=new JSONObject();
                LOGGER.info("处理门禁"+doorId+"POST请求开始...");
                LOGGER.info(searchJson.toJSONString());
                //BaseEsSearch search= EsManager.createSearch(searchJson.toJSONString());
                try{
                    //result = search.getSearchResult(searchJson.toJSONString());
                }catch(Exception e){
                    LOGGER.error("查询门禁"+doorId+"数据失败",e.getMessage());
                    result.put("errorCode","QueryError");
                }
                LOGGER.info("处理门禁"+doorId+"POST请求结束...");
                //returnJson.put(doorId,result);
                if(result.containsKey("hits")){
                    JSONArray doorArr=result.getJSONArray("hits");
                    for(int i=0;i<doorArr.size();i++){
                        resultList.add(doorArr.getJSONObject(i));
                    }
                }
            }
        }
        //判断设备类型 抓拍机
        for(String captureId: initConnectionServlet.captureDeviceId){
            if(deviceTypeJson.containsKey(captureId) && !deviceTypeJson.getString(captureId).isEmpty()){
                //抓拍机
                LOGGER.warn(captureId);
                String deviceId=deviceTypeJson.getString(captureId);
                String searchId="13001";
                JSONObject searchJson=new JSONObject();
                JSONObject captureJson=new JSONObject();
                captureJson.put("sim_threshold",paramJson.getString("sim_threshold"));
                captureJson.put("feature_name","rt_feature");
                captureJson.put("feature_value",paramJson.getString("feature_value"));
                captureJson.put("is_calcSim",paramJson.getBoolean("is_calcSim"));
                captureJson.put("enter_time_start",paramJson.getString("enter_time"));
                captureJson.put("enter_time_end",paramJson.getString("leave_time"));
                captureJson.put("minimum_should_match",1);
                if(!StringUtils.isEmpty(paramJson.getString("sortField"))){
                    captureJson.put("sortOrder",paramJson.getString("sortOrder"));
                    //存在排序字段
                    if("time".equals(paramJson.getString("sortField"))){
                        captureJson.put("sortField","enter_time");
                    }else if("score".equals(paramJson.getString("sortField"))){
                        captureJson.put("sortField","_score");
                    }
                }
                captureJson.put("from",0);
                captureJson.put("size",9999);
                JSONArray devIdArr=new JSONArray();
                String []deviceIdTmp=deviceId.split(",");
                for(String id:deviceIdTmp){
                    //根据抓拍机类型根据设备id获取es表中的映射id
                    String querySql=searchSql.toString()+id+"\"";
                    List query=new ArrayList<>();
                    try {
                        query=JdbcUtil.query(querySql);
                    } catch (Exception e) {
                        LOGGER.error("查询抓拍机映射关系表失败：",e);
                    }
                    //遍历设备类型
                    for(Object result:query){
                        Object[] tmp=(Object[])result;
                        String fssId=(String)tmp[0];
                        devIdArr.add(fssId);
                    }
                }
                captureJson.put("camera_id",devIdArr);
                searchJson.put("id",searchId);
                searchJson.put("params",captureJson);
                JSONObject result=new JSONObject();
                LOGGER.info("处理抓拍机"+captureId+"POST请求开始...");
                LOGGER.info(searchJson.toJSONString());
                //BaseEsSearch search= EsManager.createSearch(searchJson.toJSONString());
                try{
                    //result = search.getSearchResult(searchJson.toJSONString());
                }catch(Exception e){
                    LOGGER.error("查询抓拍机"+captureId+"数据失败",e.getMessage());
                    result.put("errorCode","QueryError");
                }
                LOGGER.info("处理抓拍机"+captureId+"POST请求结束...");
                //returnJson.put(captureId,result);
                if(result.containsKey("hits")){
                    JSONArray captureArr=result.getJSONArray("hits");
                    for(int i=0;i<captureArr.size();i++){
                        resultList.add(captureArr.getJSONObject(i));
                    }
                }
            }
            int from=Integer.parseInt(paramJson.getString("from"));
            int size=Integer.parseInt(paramJson.getString("size"));
            String order=paramJson.getString("sortOrder");
            String field=paramJson.getString("sortField");
            if(field.equals("time")){
                //按时间排序
                Collections.sort(resultList, new Comparator<JSONObject>(){
                    public int compare(JSONObject o1, JSONObject o2) {
                        String time1="";
                        String time2="";
                        if(o1.containsKey("event_time")){
                            time1=o1.getString("event_time");
                        }else if(o1.containsKey("enter_time")){
                            time1=o1.getString("enter_time");
                        }
                        if(o2.containsKey("event_time")){
                            time2=o2.getString("event_time");
                        }else if(o2.containsKey("enter_time")){
                            time2=o2.getString("enter_time");
                        }
                        return time1.compareTo(time2);
                    }
                });
            }else if(field.equals("score")){
                //按相似度排序
                Collections.sort(resultList, new Comparator<JSONObject>(){
                    public int compare(JSONObject o1, JSONObject o2) {
                        Float score1=Float.parseFloat(o1.getString("score"));
                        Float score2=Float.parseFloat(o2.getString("score"));
                        return score1.compareTo(score2);
                    }
                });
            }
            if(order.equals("desc")){
                //反转lists
                Collections.reverse(resultList);
            }

            int listSize=resultList.size();
            returnJson.put("total",listSize);
            if(from>listSize) {
                returnJson.put("hits", new JSONArray());
            }else{
                int max=from+size>listSize?listSize:from+size;
                JSONArray hitArr=new JSONArray();
                while(from<max){
                    //根据uuid从历史表获取图片base64数据
                    JSONObject tmpObj=resultList.get(from);
                    if(tmpObj.containsKey("img_url")){
                        try{
                            String uuid=tmpObj.getString("img_url");
                            JSONObject picObj= PhoenixUtil.getHistoryPicture(uuid,initConnectionServlet.getConn());
                            byte[] img_data=picObj.getBytes("rt_image_data");
                            String img_url= Base64.getEncoder().encodeToString(img_data);
                            tmpObj.put("face_big_picture_url",img_url);
                        }catch(Exception e){
                            LOGGER.error("查询历史表图片数据失败："+tmpObj.getString("img_url"));
                        }
                    }
                    hitArr.add(tmpObj);
                    from++;
                }
                returnJson.put("hits", hitArr);
            }
        }
        return returnJson.toJSONString();
    }

    @POST
    @Path("/PeopleSearch")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String queryPeople(@Context HttpServletRequest request,String queryString) {
        String sql="select fssid from t_cfg_cameraid where policeid=\"";
        JSONObject returnJson=new JSONObject();
        //参数
        JSONObject searchJson= new JSONObject();
        searchJson.put("params",JSON.parseObject(queryString));
        searchJson.put("id","13006");
        //BaseEsSearch search= EsManager.createSearch(searchJson.toJSONString());
        try{
            //returnJson = search.getSearchResult(searchJson.toJSONString());
        }catch(Exception e){
            LOGGER.error("查询分人入户数据失败",e.getMessage());
            returnJson.put("errorCode","QueryError");
        }
        if(returnJson.containsKey("hits") && returnJson.getJSONArray("hits").size()!=0){
            JSONArray tmpArr=returnJson.getJSONArray("hits");
            for(int i=0;i<tmpArr.size();i++){
                JSONObject obj=tmpArr.getJSONObject(i);
                String person_id=obj.getString("lib_person_id");
                String querySql=sql+person_id+"\"";
                List queryList=new ArrayList<>();
                try {
                    queryList=JdbcUtil.query(querySql);
                } catch (Exception e) {
                    LOGGER.error("查询名单库人员信息失败：",e);
                }

            }

        }
        return returnJson.toJSONString();
    }
}
