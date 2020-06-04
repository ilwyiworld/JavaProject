package com.yiworld.client;

import com.yiworld.client.scanner.Scan;
import com.yiworld.client.service.impl.ClientInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author crossoverJie
 */
@SpringBootApplication
@Slf4j
public class ClientApplication implements CommandLineRunner{

	@Autowired
	private ClientInfo clientInfo ;
	public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
		log.info("启动 Client 服务成功");
	}

	@Override
	public void run(String... args) throws Exception {
		Scan scan = new Scan() ;
		Thread thread = new Thread(scan);
		thread.setName("scan-thread");
		thread.start();
		clientInfo.saveStartDate();
	}
}