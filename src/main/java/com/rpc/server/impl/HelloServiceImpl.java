package com.rpc.server.impl;

import com.rpc.server.HelloService;

public class HelloServiceImpl implements HelloService {

	public String sayHi(String name) {
		return  "Hi, " + name;
	}

	public String sayHeeloWorld() {
		return "heeloWorld";
	}

}
