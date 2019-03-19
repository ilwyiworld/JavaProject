package com.yiworld.hadoop.rpc;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;

public class RPCClient {

	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
		//在客户端获取代理对象，调用目标对象（RPCServer）上的方法
		RPCService service = RPC.getProxy(RPCService.class, 1000, new InetSocketAddress("192.168.3.106", 9999), conf);
		String result=service.sayHi("Hello Hadoop RPC");
		System.out.println(result);
		RPC.stopProxy(service);
	}

}
