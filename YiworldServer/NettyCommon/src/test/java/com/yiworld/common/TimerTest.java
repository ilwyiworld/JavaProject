package com.yiworld.common;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class TimerTest {

    public static void main(String[] args) {
        log.info("start");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log.info("test");
            }
        }, 50000);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log.info("test");
            }
        }, 30000);
    }
}
