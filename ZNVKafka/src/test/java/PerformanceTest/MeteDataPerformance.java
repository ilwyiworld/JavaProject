package PerformanceTest;

import com.alibaba.fastjson.JSONObject;
import com.znv.kafka.ProducerBase;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static net.sourceforge.argparse4j.impl.Arguments.store;

//for commond line ArgumentParse

/**
 * Created by ct on 2016-07-15.
 */
public class MeteDataPerformance {

    public static void main(String[] args) {
        ArgumentParser parser = argParser();

        try {
            Namespace res = parser.parseArgs(args);

            /* parse args */
            long numRecords = res.getLong("numRecords");
            int numThreads = res.getInt("threadNum");
            int runTime = res.getInt("runTime");
            String topic = res.getString("topic");
            int speed = res.getInt("speed");
            int partitionNum = res.getInt("partitionNum");
            int replicationNum = res.getInt("replicationNum");
            String zookeeper = res.get("zookeeper");

            // for test
            // long numRecords = 1;
            // int numThreads = 1;
            // int runTime = 0;

            long recordsPerThread = numRecords / numThreads;

            AtomicLong totalBytesSent = new AtomicLong(0);
            AtomicLong totalMessagesSent = new AtomicLong(0);
            CountDownLatch allDone = new CountDownLatch(numThreads);

            ExecutorService meteDataExecutor = Executors.newFixedThreadPool(numThreads);

            long startMs = System.currentTimeMillis();
            for (int i = 0; i < numThreads; i++) {
                meteDataExecutor.execute(new MeteDataThread(recordsPerThread, totalBytesSent, totalMessagesSent,
                    allDone, runTime, topic, speed, partitionNum, replicationNum, zookeeper));
            }

            // 等待所有子线程执行完
            allDone.await();
            long endMs = System.currentTimeMillis();
            double esapsedSecs = (endMs - startMs) / 1000.0;
            double totalMBSent = (totalMessagesSent.get() * 110 * 1.0) / (1024 * 1024);// 固定110字节 //147
            System.out.printf(
                "MeteDataPerformance use %d thread,total send %d messages,use time %.2f(sec), avag %.2f(message/sec),avage %.2f(MB/sec)\n",
                numThreads, totalMessagesSent.get(), esapsedSecs, totalMessagesSent.get() / esapsedSecs,
                totalMBSent / esapsedSecs);

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get the command-line argument parser. */
    private static ArgumentParser argParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("meteData-performance").defaultHelp(true)
            .description("This tool is used to verify the meteData performance.");

        parser.addArgument("--num-records").action(store()).required(true).type(Long.class).metavar("NUM-RECORDS")
            .dest("numRecords").help("number of messages to produce");

        parser.addArgument("--thread-num").action(store()).required(true).type(Integer.class).metavar("THREAD-NUM")
            .dest("threadNum").help("thread numbers to be used");

        parser.addArgument("--run-time").action(store()).required(true).type(Integer.class).metavar("RUN-TIME")
            .dest("runTime").help("run time(s)");

        parser.addArgument("--topic").action(store()).required(false).type(String.class).metavar("TOPIC").dest("topic")
            .setDefault("MeteDataJson").help("topic name");

        parser.addArgument("--partition-num").action(store()).required(false).type(Integer.class)
            .metavar("PARTITION-NUM").dest("partitionNum").setDefault(6)
            .help("the partition number of topic if first create the topic");

        parser.addArgument("--replication-num").action(store()).required(false).type(Integer.class)
            .metavar("REPLICATION-NUM").dest("replicationNum").setDefault(1)
            .help("the replication number of topic if first create the topic");

        parser.addArgument("--zookeeper").action(store()).required(true).type(String.class).metavar("ZOOKEEPER")
            .dest("zookeeper").help(
                "The connection string for the zookeeper connection in the form host:port. Multiple URLS can be given to allow fail-over");

        parser.addArgument("--speed").action(store()).required(false).type(Integer.class).metavar("SPEED").dest("speed")
            .setDefault(0).help("speed(ms)");

        return parser;
    }

}

class MeteDataThread implements Runnable {
    // 常量定义关键索引字段
    public static final String KEY_1 = "msg_type";
    public static final String KEY_2 = "FsuId";
    public static final String KEY_3 = "CurrentVal";
    public static final String KEY_4 = "DevId";
    public static final String KEY_5 = "MeteId";
    public static final String KEY_6 = "MeteType";
    public static final String KEY_7 = "OccurTime";

    private Random rnd = new Random();
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final String startDate = "2016-01-01 00:00:00";

    // meteData
    // private String meteDataTopic = "avroMeteData-
    private String meteDataTopic = "MeteDataJson";

    // private MeteDataProducer meteDataProducer = new MeteDataProducer();
    private ProducerBase meteDataProducer; // = new MeteDataProducer("10.45.149.75:2181",12,1);

    private JSONObject avroMeteDataInfo = new JSONObject();
    // private String[][] dataKind = new String[][]{{"遥测量","float"},{"遥信量","int"}};//数据类型 及上报值数据类型
    // 遥信量 遥测量 遥控量 遥调量
    private String[][] dataKind = new String[][]{ { "0", "float" }, { "1", "int" }, { "2", "int" }, { "3", "int" } };// 数据类型
                                                                                                                     // 及上报值数据类型

    // "工程状态","交流输入开关状态","系统均浮充状态",
    // "旁路开关状态","整流器工作状态","逆变器工作状态","电池组开关状态","电池充电状态"
    private String[] meteNameyc = new String[]{ "系统电压", "系统电流", "温度" };// 监控量名称
    private String[] meteNameyx = new String[]{ "工程状态", "开关状态", "均浮充状态", "开关状态", "工作状态", "工作状态", "开关状态", "充电状态" };// 监控量名称

    // 遥测 监控量类型,与监控量类型名称一一对应
    private String[] meteTypeyc = new String[]{ "0101000", "0101001", "0101002", };
    private String[][] meteTypeNameyc = new String[][]{ { "直流系统输出电压", "V" }, { "总负载电流", "A" }, { "环境温度", "C" } };

    // 遥信
    private String[] meteTypeyx = new String[]{ "0101010", "0101011", "0101012", "0101013", "0101014", "0101015",
        "0101016", "0101017" };
    private String[] meteTypeNameyx = new String[]{ "工程状态", "交流输入开关状态", "系统均浮充状态", "旁路开关状态", "整流器工作状态", "逆变器工作状态",
        "电池组开关状态", "电池充电状态" };

    private String[] meteValue = new String[]{ "0", "1" };
    private String[] deviceName = new String[]{ "iPowwrClient", "前置台ztc", "前置台zy" };

    // performance test
    private long numRecords = 0;
    AtomicLong totalBytesSent = null;
    AtomicLong totalMessagesSent = null;
    CountDownLatch allDone = null;
    private int runTime = 0;
    private int speed = 0;

    public MeteDataThread(long numRecords, AtomicLong totalBytesSent, AtomicLong totalMessagesSent,
        CountDownLatch allDone, int runTime, String topic, int speed, int partitionNum, int replicationNum,
        String zookeeper) throws Exception {
        meteDataProducer = new ProducerBase();

        meteDataProducer.init(topic);
        meteDataProducer.setMsgTypeParam("mete", zookeeper, partitionNum, replicationNum);

        this.numRecords = numRecords;
        this.totalBytesSent = totalBytesSent;
        this.totalMessagesSent = totalMessagesSent;
        this.allDone = allDone;
        this.runTime = runTime * 1000;// s--->ms
        this.meteDataTopic = topic;
        this.speed = speed;
    }

    public void generateMeteData() {
        avroMeteDataInfo.put(KEY_1, "mete");
        avroMeteDataInfo.put(KEY_2, ("26869" + String.format("%02d", rnd.nextInt(99))));// FsuId
        Integer currentval = rnd.nextInt(100);
        avroMeteDataInfo.put(KEY_3, currentval.toString());// current val
        // Integer devid = rnd.nextInt(50);
        Integer devid = rnd.nextInt(deviceName.length);
        avroMeteDataInfo.put(KEY_4, devid.toString());// devid
        Integer meteid = rnd.nextInt(50);
        avroMeteDataInfo.put(KEY_5, meteid.toString());// meteid
        // Integer datakindIndex = rnd.nextInt(dataKind.length);
        avroMeteDataInfo.put(KEY_6, rnd.nextInt(dataKind.length));// mete type
        avroMeteDataInfo.put(KEY_7, df.format(new rndDate(startDate, df.format(new Date())).randomDate()));// occurtime
    }

    public void run() {
        long now = System.currentTimeMillis();

        Stats stats = new Stats(numRecords, 5000, totalMessagesSent);// 每5秒打印一次各个线程的发送情况
        for (long nEvents = 0; nEvents < numRecords;)// nEvents++
        {
            generateMeteData();

            long sendStartMs = System.currentTimeMillis();
            // Callback cb = stats.nextCompletion(sendStartMs, 147, stats);//消息的长度先固定写死
            // Callback cb = stats.nextCompletion(sendStartMs, 110, stats);//消息的长度先固定写死 //147

            try {
                meteDataProducer.sendData(avroMeteDataInfo);
                // meteDataProducer.sendData(avroMeteDataInfo,cb);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                meteDataProducer.close();
            }
            if (runTime > 0) {
                if (System.currentTimeMillis() - now > runTime) {
                    break;
                }
            } else {
                nEvents++;
            }
            if (speed != 0) {
                try {
                    Thread.sleep(speed);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        meteDataProducer.close();
        stats.printTotal();
        allDone.countDown();
    }
}
