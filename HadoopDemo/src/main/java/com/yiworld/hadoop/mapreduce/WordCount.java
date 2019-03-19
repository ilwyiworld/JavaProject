package com.yiworld.hadoop.mapreduce;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * KEYIN	:框架读取的一行数据的起始偏移量		Long
 * VALUEIN	:框架读到的一行数据的内容			String
 * KEYOUT	:业务逻辑输出的数据的key的类型		String
 * VALUEOUT	:业务逻辑输出的数据的key的类型		Int
 * Hadoop实现了一套序列化机制，它们的序列化数据相比jdk的serializable序列化之后的数据更加精简，从而可以提高网络传输率
 * 
 * String --> Text
 * Long --> LongWritable
 * Integer --> IntWritable
 * Null --> NullWritable
 */

public class WordCount {
	
	public static class WordCountMap extends
			Mapper<LongWritable, Text, Text, IntWritable> {
		private final IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			//key --> 框架传来的KEYIN   value --> 框架传来的VALUEIN（一行数据）
			String line = value.toString();
			StringTokenizer token = new StringTokenizer(line);  //将一行数据内容根据分隔符分隔...
			while (token.hasMoreTokens()) {
				word.set(token.nextToken());
				context.write(word, one);			//将每个单词输入到context中
			}
		}
	}

	public static class WordCountReduce extends
			Reducer<Text, IntWritable, Text, IntWritable> {
		//一组相同key的数据调用次reduce方法
		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			//key --> map传来的一个key  values--> map传来的value的集合
			//累计这一组key数据中的所有value值即可
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("HADOOP_USER_NAME","root");			//设置当前用户为root
		Configuration conf = new Configuration();
		conf.set("yarn.resourcemanager.hostname", "master");
		//创建一个job提交器对象
		@SuppressWarnings("deprecation")
		Job job = new Job(conf);
		//告知客户端，mr程序所在的jar包
		job.setJarByClass(WordCount.class);
		//告知MRAppMaster，本程序里要用到的mapper和reducer业务实现类
		job.setMapperClass(WordCountMap.class);
		job.setReducerClass(WordCountReduce.class);
		//告知MRAppMaster，本程序中的map阶段和reduce阶段输出的数据的类型
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		//告知MRAppMaster，本程序要求要启动的reduce task的数量
		job.setNumReduceTasks(3);
		//告知MRAppMaster，本程序要处理的数据的目录和输出结果所在的目录
		FileInputFormat.setInputPaths(job, new Path("hdfs://106.14.193.13:9000/input"));
		FileOutputFormat.setOutputPath(job, new Path("hdfs://106.14.193.13:9000/output"));
		//提交job
		//job.submit();
		boolean res = job.waitForCompletion(true);
		System.out.println(res);
		System.exit(res?0:1);
	}
}
