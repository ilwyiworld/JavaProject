package main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018-11-26.
 */
public class TimerTest {
    private ScheduledExecutorService scheduExec;
    private static int i=0;

    public long start;

    TimerTest(){
        this.scheduExec =  Executors.newScheduledThreadPool(2);
        this.start = System.currentTimeMillis();
    }

    public void timerOne(){
        scheduExec.schedule(new Runnable() {
            public void run() {
                System.out.println("timerOne,the time:" + (System.currentTimeMillis() - start));
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },1000, TimeUnit.MILLISECONDS);
    }

    public void timerTwo(){
        scheduExec.schedule(new Runnable() {
            public void run() {
                System.out.println("timerTwo,the time:" + (System.currentTimeMillis() - start));
            }
        },2000,TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) throws ParseException {
        /*TimerTest test = new TimerTest();
        test.timerOne();
        test.timerTwo();*/
        Date time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-11-27 10:03:00");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    startTask();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, time,3000);
    }

    private static void startTask() {
        System.out.println("test: "+ (i++));
    }
}
