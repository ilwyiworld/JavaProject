package com.yiworld.server;

import com.yiworld.server.config.AppConfiguration;
import com.yiworld.server.kit.RegistryZK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;

@SpringBootApplication
@Slf4j
public class ServerApplication implements CommandLineRunner {

    @Autowired
    private AppConfiguration appConfiguration;

    @Value("${server.port}")
    private int httpPort;

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
        log.info("Start server success!!!");
    }

    @Override
    public void run(String... args) throws Exception {
        // 获得本机 IP
        String addr = InetAddress.getLocalHost().getHostAddress();
        Thread thread = new Thread(new RegistryZK(addr, appConfiguration.getServerPort(), httpPort));
        thread.setName("registry-zk");
        thread.start();
    }
}