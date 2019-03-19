package com.yiworld.hadoop.rpc;

import java.io.IOException;

import org.apache.hadoop.HadoopIllegalArgumentException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.RPC.Server;

public class RPCServer implements RPCService {

	public static void main(String[] args)
			throws HadoopIllegalArgumentException, IOException {
		Configuration conf = new Configuration();
		Server server = new RPC.Builder(conf) //
				.setProtocol(RPCService.class) //
				.setBindAddress("192.168.3.106")//
				.setPort(9999) //
				.setInstance(new RPCServer()) //
				.build();
		server.start();
	}

	@Override
	public String sayHi(String name) {
		// TODO Auto-generated method stub
		return name;
	}

}
