package com.znv.faceInvoke;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/12/19.
 */
//C++返回参数的实例类
public class CallbackResult extends Structure{
    public String  uuid;
    public String  deviceIp;
    public String  taskId;
    public String  faceImg;
    public double score;
    public int trackId;
    public int fX;
    public int fY;
    public int fWidth;
    public int fHeight;
    public String body;     //帧图像
    public int type;     //人脸跟踪结束标识

    public static class ByReference extends CallbackResult implements Structure.ByReference {}
    public static class ByValue extends CallbackResult implements Structure.ByValue {}

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(new String[]{"uuid","deviceIp","taskId","faceImg","score","trackId",
               "fX", "fY","fWidth","fHeight","body","type"});
    }
}
