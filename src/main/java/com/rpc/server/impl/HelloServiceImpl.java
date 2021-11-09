package com.rpc.server.impl;

import com.rpc.server.HelloService;

public class HelloServiceImpl implements HelloService {

	@Override
	public String sayHi(String name) {
		return  "Hi, " + name;
	}

	@Override
	public String sayHeeloWorld() {
		return "heeloWorld";
	}

}
