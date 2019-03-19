package com.znv.faceInvoke;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/12/19.
 */
//发送给C++的参数
//getFieldOrder()方法中的内容必填完整，字符串数字中的值必须和结构体中的成员名一一对应，成员必须是public
public class TaskParams extends Structure {

    public double[] nms_th;
    public double[] scalesAll;
    public int miniSize;
    public int factorCountAll;
    public int Max_img_blur;
    public int Optimise_interval;
    public String task_name;
    public String task_id;
    public String source_addr;      //资源路径RTSP
    public String face_info_addr;   //人脸信息推送地址
    public String bg_img_addr;      //人脸背景大图推送地址
    public String deviceIp;
    public String model_path;       //模型路径
    public int loop_video;      //是否循环读取视频

    //实现这个接口  jna会认为你的Struct是一个指针
    public static class ByReference extends TaskParams implements Structure.ByReference {}
    public static class ByValue extends TaskParams implements Structure.ByValue {}

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(new String[]{"nms_th","scalesAll","miniSize","factorCountAll","Max_img_blur","Optimise_interval",
                "task_name","task_id", "source_addr","face_info_addr","bg_img_addr","deviceIp","model_path","loop_video"});
    }
}
