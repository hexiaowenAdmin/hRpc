package com.rpc;

import com.rpc.centre.Server;
import com.rpc.centre.impl.ServiceCenter;
import com.rpc.server.HelloService;
import com.rpc.server.impl.HelloServiceImpl;

import java.io.IOException;

/**
 * 启动服务端
 *
 */
public class RPCServer {
	public static void main(String[] args) {
		 new Thread(new Runnable() {
	            public void run() {
	                try {
	                    Server serviceServer = new ServiceCenter(8080);
	                    serviceServer.register(HelloService.class, HelloServiceImpl.class);
	                    serviceServer.start();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }).start();
	}
}
