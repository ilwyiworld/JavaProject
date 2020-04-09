package main;

import cn.hutool.extra.ftp.Ftp;

import java.io.File;
import java.io.IOException;

public class FtpUtil {
    public static void main(String[] args) throws IOException {
        //匿名登录（无需帐号密码的FTP服务器）
        Ftp ftp = new Ftp("223.71.103.69", 8521, "ftpuser", "ftpuser");
        //进入远程目录
        ftp.cd("/pics");
        //上传本地文件
        ftp.upload("/pics", new File("C:\\Users\\zhang\\Desktop\\yi\\test.txt"));
        //下载远程文件
        ftp.download("/pics", "test.txt", new File("C:\\Users\\zhang\\Desktop\\yi\\test2.txt"));
        //关闭连接
        ftp.close();
    }
}
