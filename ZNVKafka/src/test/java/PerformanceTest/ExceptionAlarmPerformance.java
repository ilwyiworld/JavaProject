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

/**
 * Created by ct on 2016-07-20.
 */
public class ExceptionAlarmPerformance {
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
            // long numRecords = 100;
            // int numThreads = 1;
            // int runTime = 0;
            // String topic = "test";
            // int speed = 0;

            long recordsPerThread = numRecords / numThreads;

            AtomicLong totalBytesSent = new AtomicLong(0);
            AtomicLong totalMessagesSent = new AtomicLong(0);
            CountDownLatch allDone = new CountDownLatch(numThreads);

            ExecutorService SysLogExecutor = Executors.newFixedThreadPool(numThreads);

            long startMs = System.currentTimeMillis();
            for (int i = 0; i < numThreads; i++) {
                SysLogExecutor.execute(new ExceptionAlarmThread(recordsPerThread, totalBytesSent, totalMessagesSent,
                    allDone, runTime, topic, speed, partitionNum, replicationNum, zookeeper));
            }

            // 等待所有子线程执行完
            allDone.await();
            long endMs = System.currentTimeMillis();
            double esapsedSecs = (endMs - startMs) / 1000.0;
            double totalMBSent = (totalMessagesSent.get() * 425 * 1.0) / (1024 * 1024);// 固定425字节 //146
            System.out.printf(
                "ExceptionAlarmPerformance use %d thread,total send %d messages,use time %.2f(sec), avag %.2f(message/sec),avage %.2f(MB/sec)\n",
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
            .description("This tool is used to verify the SysLog performance.");

        parser.addArgument("--num-records").action(store()).required(true).type(Long.class).metavar("NUM-RECORDS")
            .dest("numRecords").help("number of messages to produce");

        parser.addArgument("--thread-num").action(store()).required(true).type(Integer.class).metavar("THREAD-NUM")
            .dest("threadNum").help("thread numbers to be used");

        parser.addArgument("--run-time").action(store()).required(true).type(Integer.class).metavar("RUN-TIME")
            .dest("runTime").help("run time(s)");

        parser.addArgument("--topic").action(store()).required(false).type(String.class).metavar("TOPIC").dest("topic")
            .setDefault("ExceptionAlarmJson").help("topic name");

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

class ExceptionAlarmThread implements Runnable {
    public static final String KEY_1 = "msg_type";
    public static final String KEY_2 = "action";
    public static final String KEY_3 = "device_id";
    public static final String KEY_4 = "exception_id";
    public static final String KEY_5 = "exception_level";
    public static final String KEY_6 = "exception_time";
    public static final String KEY_7 = "exception_type";

    private Random rnd = new Random();
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final String startDate = "2016-01-01 00:00:00";

    private String[] deviceName = new String[]{ "iPowwrClient", "前置台ztc", "前置台zy" };
    private String[] deviceMode = new String[]{ "业务台V1.6", "前置台", "前置台" };

    // sysLogInfo
    // private String sysLogTopic = "avroSysLog-ct";
    private String excpTopic = "ExceptionAlarmJson";
    private ProducerBase excpAlarmProducer; // = new ExceptionAlarmProducer();
    private JSONObject alarmInfo = new JSONObject();

    // performance test
    private long numRecords = 0;
    AtomicLong totalBytesSent = null;
    AtomicLong totalMessagesSent = null;
    CountDownLatch allDone = null;
    private int runTime = 0;
    private int speed = 0;

    public ExceptionAlarmThread(long numRecords, AtomicLong totalBytesSent, AtomicLong totalMessagesSent,
        CountDownLatch allDone, int runTime, String topic, int speed, int partitionNum, int replicationNum,
        String zookeeper) throws Exception {
        try {
            excpAlarmProducer = new ProducerBase();

            excpAlarmProducer.init(topic);
            excpAlarmProducer.setMsgTypeParam("exception", zookeeper, partitionNum, replicationNum);
            this.numRecords = numRecords;
            this.totalBytesSent = totalBytesSent;
            this.totalMessagesSent = totalMessagesSent;
            this.allDone = allDone;
            this.runTime = runTime * 1000;// s--->ms
            this.excpTopic = topic;
            this.speed = speed;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void generateExceptionInfo() {
        alarmInfo.put(KEY_1, "exception");
        // Integer action = rnd.nextInt(2);//告警状态 1：产生 0：消除
        alarmInfo.put(KEY_2, rnd.nextInt(2));
        alarmInfo.put("affirm_remark", "服务器异常告警");
        Integer state = rnd.nextInt(2);
        alarmInfo.put("affirm_state", state.toString());
        alarmInfo.put("confirm_time", df.format(new rndDate(startDate, df.format(new Date())).randomDate()));
        String employeeid = "004900" + String.format("%02d", rnd.nextInt(100));
        alarmInfo.put("confirm_user", employeeid);
        Integer deviceid = rnd.nextInt(deviceName.length);
        alarmInfo.put(KEY_3, deviceid.toString());
        alarmInfo.put("device_kind", deviceMode[deviceid]);
        alarmInfo.put("device_name", deviceName[deviceid]);
        alarmInfo.put("device_type", ("类型" + String.format("%02d", rnd.nextInt(99))));
        String excpid = "99655" + String.format("%02d", rnd.nextInt(100));
        alarmInfo.put(KEY_4, excpid);
        Integer level = rnd.nextInt(2);
        alarmInfo.put(KEY_5, level.toString());
        Date excptime = new rndDate(startDate, df.format(new Date())).randomDate();
        alarmInfo.put(KEY_6, df.format(excptime));
        alarmInfo.put(KEY_7, "服务器异常");
        Integer precinctid = rnd.nextInt(10);
        alarmInfo.put("precinct_id", precinctid.toString());
        alarmInfo.put("precinct_name", "区域" + precinctid.toString());
        Integer stationid = rnd.nextInt(20);// 500
        alarmInfo.put("station_id", stationid.toString());
        alarmInfo.put("sub_alarm_type", "服务器异常告警");
    }

    public void run() {
        long now = System.currentTimeMillis();

        Stats stats = new Stats(numRecords, 5000, totalMessagesSent);// 每5秒打印一次各个线程的发送情况

        for (long nEvents = 0; nEvents < numRecords;)// nEvents++
        {
            generateExceptionInfo();

            long sendStartMs = System.currentTimeMillis();
            // Callback cb = stats.nextCompletion(sendStartMs, 425, stats);//消息的长度先固定写死 //146

            try {
                excpAlarmProducer.sendData(alarmInfo);
                // excpAlarmProducer.sendData(alarmInfo,cb);
            } catch (Exception e) {
                excpAlarmProducer.close();
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
        excpAlarmProducer.close();
        stats.printTotal();
        allDone.countDown();
    }

}
