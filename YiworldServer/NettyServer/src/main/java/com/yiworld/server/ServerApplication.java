package com.yiworld.server;

import com.yiworld.server.config.AppConfiguration;
import com.yiworld.server.kit.RegistryZK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;

@SpringBootApplication
public class ServerApplication implements CommandLineRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerApplication.class);

    @Autowired
    private AppConfiguration appConfiguration;

    @Value("${server.port}")
    private int httpPort;

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
        LOGGER.info("Start server success!!!");
    }

    @Override
    public void run(String... args) throws Exception {
        //获得本机IP
        String addr = InetAddress.getLocalHost().getHostAddress();
        Thread thread = new Thread(new RegistryZK(addr, appConfiguration.getServerPort(), httpPort));
        thread.setName("registry-zk");
        thread.start();
    }
}