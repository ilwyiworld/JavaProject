package com.yiworld.hadoop.mapreduce;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Sort extends Configured implements Tool{

	public static class Map extends
			Mapper<Object, Text, IntWritable, IntWritable> {

		private static IntWritable data = new IntWritable();
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			String line = value.toString();
			data.set(Integer.parseInt(line));
			context.write(data, new IntWritable(1));
		}

	}

	public static class Reduce extends
			Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
		private static IntWritable linenum = new IntWritable(1);
		public void reduce(IntWritable key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			for (@SuppressWarnings("unused") IntWritable val : values) {
				context.write(linenum, key);
				linenum = new IntWritable(linenum.get() + 1);
			}
		}
	}

	public static class Partition extends Partitioner<IntWritable, IntWritable> {
		@Override
		public int getPartition(IntWritable key, IntWritable value,
				int numPartitions) {
			int MaxNumber = 65223;
			int bound = MaxNumber / numPartitions + 1;
			int keynumber = key.get();
			for (int i = 0; i < numPartitions; i++) {
				if (keynumber < bound * i && keynumber >= bound * (i - 1))
					return i - 1;
			}
			return 0;
		}
	}

	@Override
	public int run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = getConf();				//从父类中获得
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage WordCount <int> <out>");
			return 2;
		}
		@SuppressWarnings("deprecation")
		Job job = new Job(conf, "Sort"); 
		job.setJarByClass(Sort.class);
		job.setMapperClass(Map.class);
		job.setPartitionerClass(Partition.class);
		job.setReducerClass(Reduce.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = new Configuration();
		int status =ToolRunner.run(conf, new Sort(),args);
		System.exit(status);
	}


}
