package PerformanceTest;

import com.alibaba.fastjson.JSONObject;
import com.znv.kafka.ProducerBase;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.kafka.clients.producer.Callback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static net.sourceforge.argparse4j.impl.Arguments.store;

/**
 * Created by ct on 2016-07-20.
 */
public class AlarmPerformance {
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
            // long numRecords = 10;
            // int numThreads = 1;
            // int runTime = 0;
            // String topic = "AlarmJson";
            // int speed = 0;

            long recordsPerThread = numRecords / numThreads;

            AtomicLong totalBytesSent = new AtomicLong(0);
            AtomicLong totalMessagesSent = new AtomicLong(0);
            CountDownLatch allDone = new CountDownLatch(numThreads);

            ExecutorService alarmExecutor = Executors.newFixedThreadPool(numThreads);

            long startMs = System.currentTimeMillis();
            for (int i = 0; i < numThreads; i++) {
                alarmExecutor.execute(new AlarmThread(recordsPerThread, totalBytesSent, totalMessagesSent, allDone,
                    runTime, topic, speed, partitionNum, replicationNum, zookeeper));
            }

            // 等待所有子线程执行完
            allDone.await();
            long endMs = System.currentTimeMillis();
            double esapsedSecs = (endMs - startMs) / 1000.0;
            double totalMBSent = (totalMessagesSent.get() * 526 * 1.0) / (1024 * 1024);// 固定526字节 230
            System.out.printf(
                "AlarmPerformance use %d thread,total send %d messages,use time %.2f(sec), avag %.2f(message/sec),avage %.2f(MB/sec)\n",
                numThreads, totalMessagesSent.get(), esapsedSecs, totalMessagesSent.get() / esapsedSecs,
                totalMBSent / esapsedSecs);

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Get the command-line argument parser. */
    private static ArgumentParser argParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("alarm-performance").defaultHelp(true)
            .description("This tool is used to verify the alarm performance.");

        parser.addArgument("--num-records").action(store()).required(true).type(Long.class).metavar("NUM-RECORDS")
            .dest("numRecords").help("number of messages to produce");

        parser.addArgument("--thread-num").action(store()).required(true).type(Integer.class).metavar("THREAD-NUM")
            .dest("threadNum").help("thread numbers to be used");

        parser.addArgument("--run-time").action(store()).required(true).type(Integer.class).metavar("RUN-TIME")
            .dest("runTime").help("run time(s)");

        parser.addArgument("--topic").action(store()).required(false).type(String.class).metavar("TOPIC").dest("topic")
            .setDefault("AlarmJson").help("topic name");

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

class AlarmThread implements Runnable {

    public static final String KEY_1 = "msg_type";
    public static final String KEY_2 = "action";
    public static final String KEY_3 = "alarm_id";
    public static final String KEY_4 = "alarm_level";
    public static final String KEY_5 = "alarm_time";
    public static final String KEY_6 = "alarm_type";
    public static final String KEY_7 = "device_id";
    public static final String KEY_8 = "fsu_id";
    public static final String KEY_9 = "service";
    public static final String KEY_10 = "mete_id";
    public static final String KEY_11 = "mete_type";

    private Random rnd = new Random();
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final String startDate = "2016-01-01 00:00:00";
    private int circle = 0;
    private Date alarmtime = new Date();
    private Date affirm_time = new Date();

    // Alarm
    // private String alarmTopic = "avroAlarm-ct";
    private String alarmTopic = "AlarmJson";
    private ProducerBase alrmProducer; // = new AlarmProducer();
    private JSONObject alarmInfo = new JSONObject();
    private String[] stationKind = new String[]{ "0", "1", "2", "3", "4", "5", "6", "101", "102" };
    private String[] deviceName = new String[]{ "iPowwrClient", "前置台ztc", "前置台zy" };
    private String[] deviceMode = new String[]{ "业务台V1.6", "前置台", "前置台" };
    private String[] AlarmName = new String[]{ "监控模块故障", "交流A相电压", "交流B相电压", "交流C相电压", "第1组电池电压", "整流模块电压",
        "交流输入频率故障" };
    private String[] Alarmexplain = new String[]{ "告警", "过压", "欠压", "过压", "欠压", "过压", "告警" };
    private String[] Alarmlevel = new String[]{ "1", "2", "3", "4" };

    // 遥信量 遥测量 遥控量 遥调量
    private String[][] dataKind = new String[][]{ { "0", "float" }, { "1", "int" }, { "2", "int" }, { "3", "int" } };// 数据类型
                                                                                                                     // 及上报值数据类型

    // performance test
    private long numRecords = 0;
    AtomicLong totalBytesSent = null;
    AtomicLong totalMessagesSent = null;
    CountDownLatch allDone = null;
    private int runTime = 0;
    private int speed = 0;

    public AlarmThread(long numRecords, AtomicLong totalBytesSent, AtomicLong totalMessagesSent, CountDownLatch allDone,
        int runTime, String topic, int speed, int partitionNum, int replicationNum, String zookeeper) throws Exception {
        alrmProducer = new ProducerBase();

        alrmProducer.init(topic);
        alrmProducer.setMsgTypeParam("alarm", zookeeper, partitionNum, replicationNum);
        this.numRecords = numRecords;
        this.totalBytesSent = totalBytesSent;
        this.totalMessagesSent = totalMessagesSent;
        this.allDone = allDone;
        this.runTime = runTime * 1000;// s--->ms
        this.alarmTopic = topic;
        this.speed = speed;
    }

    public void generateAlarm() {
        // 告警产生
        if (circle == 0) {
            alarmInfo.clear();

            alarmInfo.put(KEY_1, "alarm");//
            // Integer action = rnd.nextInt(2);//告警状态 1：产生 0：消除
            alarmInfo.put(KEY_2, 1);// 告警状态 1：产生 0：消除 rnd.nextInt(2)
            alarmInfo.put("affirm_state", "0");// 0未确认，1已确认

            alarmInfo.put("alarm_desc", "没有描述没有描述");
            // alarmInfo.put(KEY_3,"503317" + String.format("%02d",rnd.nextInt(99)));//alarm_id
            Integer levelid = rnd.nextInt(Alarmlevel.length) + 1;// 从1开始
            alarmInfo.put(KEY_4, Alarmlevel[levelid - 1]);

            alarmtime = new rndDate(startDate, df.format(new Date())).randomDate();
            alarmInfo.put(KEY_5, df.format(alarmtime));// 告警时间

            Integer mid = rnd.nextInt(AlarmName.length);
            alarmInfo.put(KEY_6, AlarmName[mid]);// alarm type
            Integer deviceid = rnd.nextInt(deviceName.length);
            alarmInfo.put(KEY_7, deviceid.toString());// devid

            alarmInfo.put(KEY_3, deviceid.toString() + AlarmName[mid]);// alarm_id "503317" +
                                                                       // String.format("%02d",rnd.nextInt(99))

            alarmInfo.put("device_kind", deviceMode[deviceid]);
            alarmInfo.put("device_name", deviceName[deviceid]);
            alarmInfo.put("device_type", ("类型" + String.format("%02d", rnd.nextInt(99))));
            alarmInfo.put(KEY_8, ("26869" + String.format("%02d", rnd.nextInt(99))));// fsuid
            alarmInfo.put("mask_type", "123");
            Integer precinctid = rnd.nextInt(10);
            alarmInfo.put("precinct_id", precinctid.toString());
            alarmInfo.put("precinct_name", "区域" + precinctid.toString());
            alarmInfo.put(KEY_9, "usms");
            Integer stationid = rnd.nextInt(20);// 500
            alarmInfo.put("station_id", stationid.toString());
            alarmInfo.put("station_name", "局站" + stationid.toString());
            alarmInfo.put("sub_alarm_type", "设备告警");
            Integer meteid = rnd.nextInt(50);// 0-49
            alarmInfo.put(KEY_10, meteid.toString());
            alarmInfo.put(KEY_11, rnd.nextInt(dataKind.length));

            // 告警确认相关
            alarmInfo.put("affirm_people", "");
            alarmInfo.put("affirm_people_name", "");
            alarmInfo.put("affirm_remark", "");
            alarmInfo.put("affirm_state", "");// 0未确认，1已确认
            alarmInfo.put("affirm_time", "");

            // 告警消除相关
            alarmInfo.put("clear_time", "");

            circle++;
            return;
        }
        // 告警确认
        else if (circle == 1) {
            String employeeid = "004900" + String.format("%02d", rnd.nextInt(100));// 告警确认相关
            alarmInfo.put("affirm_people", employeeid);
            alarmInfo.put("affirm_people_name", ("admin" + employeeid));
            alarmInfo.put("affirm_remark", "test");
            Integer state = rnd.nextInt(2);
            alarmInfo.put("affirm_state", "1");// 0未确认，1已确认
            Integer span = rnd.nextInt(20);// 单位min
            affirm_time = new Date(alarmtime.getTime() + span * 60 * 1000);
            alarmInfo.put("affirm_time", df.format(affirm_time));

            circle++;
            return;
        } else if (circle == 2) {
            alarmInfo.put(KEY_2, 0);// 告警状态 1产生 0消除
            Integer span = rnd.nextInt(20);// 单位min
            alarmInfo.put("clear_time", df.format(new Date(affirm_time.getTime() + span * 60 * 1000)));

            circle = 0;
            return;
        }
    }

    public void run() {
        long now = System.currentTimeMillis();

        Stats stats = new Stats(numRecords, 5000, totalMessagesSent);// 每5秒打印一次各个线程的发送情况
        for (long nEvents = 0; nEvents < numRecords;)// nEvents++
        {
            generateAlarm();

            long sendStartMs = System.currentTimeMillis();
            Callback cb = stats.nextCompletion(sendStartMs, 526, stats);// 消息的长度先固定写死 //230
            try {
                // System.out.println(alarmInfo);
                alrmProducer.sendData(alarmInfo);
                // alrmProducer.sendData(alarmInfo,cb);
            } catch (Exception e) {
                alrmProducer.close();
                System.out.println(e.getMessage());
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
        alrmProducer.close();
        stats.printTotal();
        allDone.countDown();
    }
}
