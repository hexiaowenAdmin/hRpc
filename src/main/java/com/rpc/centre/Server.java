package com.rpc.centre;

import java.io.IOException;

public interface  Server {

	    public void start() throws IOException;
	 
	    public void register(Class serviceInterface, Class impl);
}
