package com.znv.faceInvoke;

import com.sun.jna.Library;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Created by Administrator on 2017/12/20.
 */
public interface CLibrary extends Library {

    //调用的C++方法
    public int Cinitial(TaskParams params, CallBack_Result callBack_result);
    public int Cdetect(String flag);
    public int Cstop(String flag);

    //回调函数声明
    public interface CallBack_Result extends StdCallLibrary.StdCallCallback {
        public void ReceiveResult(CallbackResult.ByReference result);
    }
}
