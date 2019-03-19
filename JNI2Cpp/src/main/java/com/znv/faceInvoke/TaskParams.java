package com.znv.faceInvoke;

/**
 * Created by Administrator on 2017/12/19.
 */
//发送给C++的参数
public class TaskParams {

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

    public String toString() {
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<nms_th.length;i++){
            sb.append("nms_th"+(i+1)+nms_th[i]).append("~");
        }
        for(int i=0;i<scalesAll.length;i++){
            sb.append("scalesAll"+(i+1)+scalesAll[i]).append("~");
        }
        sb.append("miniSize").append(miniSize).append("~").
                append("factorCountAll").append(factorCountAll).append("~").
                append("Max_img_blur").append(Max_img_blur).append("~").
                append("Optimise_interval").append(Optimise_interval).append("~").
                append("task_name").append(task_name).append("~").
                append("task_id").append(task_id).append("~").
                append("source_addr").append(source_addr).append("~").
                append("face_info_addr").append(face_info_addr).append("~").
                append("bg_img_addr").append(bg_img_addr).append("~").
                append("deviceIp").append(deviceIp).append("~").
                append("model_path").append(model_path).append("~").
                append("loop_video").append(loop_video);
        return sb.toString();
    }
}
