package com.yiworld.hadoop.mapreduce.CountMoney;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Created by yiworld on 2017/7/19.
 * 测试数据
 * 用户id       收入        支出
 *   1          1000        0
 *   2          500         300
 *   1          2000        1000
 *   2          500         200
 *
 * 需求：
 * 用户id       总收入      总支出      余额
 *   1          3000        1000       2000
 *   2          1000        500        500
 */
public class CountMapReduce {

    public static class CountMapper extends Mapper<LongWritable,Text,IntWritable,UserWritable> {
        private UserWritable userWritable=new UserWritable();
        private IntWritable id=new IntWritable();

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            //获得输入的每行数据
            String line=value.toString();

            //解析数据，并封装进javabean中
            String [] words=line.split("\\t");
            if(words.length==3){
                userWritable.setId(Integer.parseInt(words[0]))
                        .setIncome(Integer.parseInt(words[1]))
                        .setExpense(Integer.parseInt(words[2]))
                        .setSum(Integer.parseInt(words[1])-Integer.parseInt(words[2]));
                id.set(Integer.parseInt(words[0]));
            }
            context.write(id,userWritable);
        }

    }

    public static class CountReducer extends Reducer<IntWritable,UserWritable,UserWritable,NullWritable>{

        /**
         * 输入数据：
         * <1,{user01[1,1000,0,1000],user01[1,2000,1000,1000]}>
         * <2,{user02[2,500,300,200],user02[2,500,300,200]}>
         */
        private UserWritable userWritable=new UserWritable();
        private NullWritable n=NullWritable.get();

        @Override
        protected void reduce(IntWritable key, Iterable<UserWritable> values, Context context) throws IOException, InterruptedException {
            //初始化收入支出余额
            Integer income=0;
            Integer expense=0;

            for(UserWritable u :values){
                income+=u.getIncome();
                expense+=u.getExpense();
            }
            Integer sum=income-expense;
            //封装进对象
            userWritable.setId(key.get()).setIncome(income).setExpense(expense).setSum(sum);
            context.write(userWritable,n);
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        System.setProperty("HADOOP_USER_NAME","root");			//设置当前用户为root
        //初始化配置对象
        Configuration conf=new Configuration();
        //conf.set("yarn.resourcemanager.hostname", "aliyun_master");
        //设置job对象的运行信息和名称
        Job job=Job.getInstance(conf,"countMoney");
        //设置job运行类
        job.setJarByClass(CountMapReduce.class);
        //设施mapper输入输出类型和mapper类
        //告知MRAppMaster，本程序里要用到的mapper和reducer业务实现类
        job.setMapperClass(CountMapper.class);
        job.setReducerClass(CountReducer.class);
        //告知MRAppMaster，本程序中的map阶段和reduce阶段输出的数据的类型
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(UserWritable.class);
        job.setOutputKeyClass(UserWritable.class);
        job.setOutputValueClass(NullWritable.class);
        //设置输入输出路径
        args= new String[]{
                "hdfs://106.14.193.13:9000/input/money_data",
                "hdfs://106.14.193.13:9000/ouput"
        };
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        //提交job
        //job.submit();
        boolean res = job.waitForCompletion(true);
        System.out.println(res);
        System.exit(res?0:1);
    }
}
