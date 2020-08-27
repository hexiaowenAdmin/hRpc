package com.rpc;

import com.rpc.client.RPCClient;
import com.rpc.server.HelloService;

import java.io.IOException;

/**
 * 
 *rpc客户端启动
 *
 */
public class RPCTest {
	
	public static void main(String[] args) throws IOException {
        HelloService service = RPCClient.getRemoteProxyObj(HelloService.class);
        System.out.println(service.sayHi("test"));
        System.out.println(service.sayHeeloWorld());
    }

}
