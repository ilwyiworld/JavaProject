package com.yiworld.hadoop.hdfs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class HDFSAPI {
	
	private FileSystem fileSystem=null;
	
	@Before
	public void init() throws IOException{
		//与HDFS建立连接
		Configuration conf=new Configuration();
		System.setProperty("HADOOP_USER_NAME","root");			//设置当前用户为root
		//fileSystem.setPermission(p, permission);
		conf.set("fs.defaultFS", "hdfs://106.14.193.13:9000");
		fileSystem=FileSystem.get(conf);
	}

	@Test
	public void testUpload() throws IOException{
		//打开本地文件系统的一个文件作为输入流
		InputStream in =new FileInputStream("E://Sort.java");
		//使用hdfs的filesystem创建一个输出流
		FSDataOutputStream out=fileSystem.create(new Path("/Sort.java"));
		//拷贝 IN--->OUT
		IOUtils.copyBytes(in, out, 1024,true);
		fileSystem.close();
	}
	
	@Test
	public void testDownload() throws IOException {
		//打开一个输入流
		InputStream in =fileSystem.open(new Path("/LICENSE.txt"));
		//打开一个本地的输出流文件
		OutputStream out = new FileOutputStream("E://LICENSE.txt");
		//拷贝 IN--->OUT
		IOUtils.copyBytes(in, out, 1024,true);
		fileSystem.close();
	}
	
	@Test
	public void testMkdir() throws IOException {
		fileSystem.mkdirs(new Path("/a/b"));
		fileSystem.close();
	}
	
	@Test
	public void testDelete() throws IOException {
		fileSystem.delete(new Path("/a"), true);		//递归删除
		fileSystem.close();
	}

}
