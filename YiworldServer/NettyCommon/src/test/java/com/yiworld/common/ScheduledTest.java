package com.yiworld.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ScheduledTest {

    public static void main(String[] args) {
        log.info("start.....");
        ThreadFactory scheduled = new ThreadFactoryBuilder()
                .setNameFormat("scheduled-%d")
                .build();
        ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(2,scheduled) ;
        scheduledExecutorService.schedule(() -> log.info("scheduled........."),3, TimeUnit.SECONDS) ;
    }
}
