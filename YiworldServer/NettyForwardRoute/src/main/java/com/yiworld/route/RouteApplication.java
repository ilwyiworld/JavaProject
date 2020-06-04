package com.yiworld.route;

import com.yiworld.route.kit.ServerListListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class RouteApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(RouteApplication.class, args);
        log.info("Start yiworld route success!!!");
    }

    @Override
    public void run(String... args) {
        //监听服务
        Thread thread = new Thread(new ServerListListener());
        thread.setName("zk-listener");
        thread.start();
    }
}