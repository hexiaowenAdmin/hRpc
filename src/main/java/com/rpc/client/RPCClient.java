package com.rpc.client;

import com.rpc.server.HelloService;
import com.rpc.util.SerializingUtils;
import com.rpc.util.ServiceReqUtil;
import com.rpc.util.ZookeeperUtil;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

@SuppressWarnings("all")
public class RPCClient {
	//请求总数
	private static  int  reqCount = 0;

	public static <T> T getRemoteProxyObj(final Class<?> serviceInterface,String... server) {
		ZookeeperUtil zk = new ZookeeperUtil();
		List<String>  list = zk.getIps();
		if(list != null && list.size() > 0){
			String serverName = "";
			if (server.length == 0){
				serverName = getServierInfo(list);
			}else {
				serverName = server[0];
			}
			String [] ipPort = serverName.split(":");
			final InetSocketAddress addr = new InetSocketAddress(ipPort[0],Integer.parseInt(ipPort[1]));
			// 1.将本地的接口调用转换成JDK的动态代理，在动态代理中实现接口的远程调用
			return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(),
					new Class<?>[] { serviceInterface }, new InvocationHandler() {
						public Object invoke(Object proxy, Method method,
								Object[] args) throws Throwable {
							Socket socket = null;
							ObjectOutputStream output = null;
							ObjectInputStream input = null;
							try {
								//1.创建SocketChannel
								SocketChannel socketChannel= SocketChannel.open();
								//2.连接服务器
								socketChannel.connect(addr);
								// 3.将远程服务调用所需的接口类、方法名、参数列表等编码后发送给服务提供者
								ServiceReqUtil serviceReqUtil = new ServiceReqUtil();
								serviceReqUtil.setMethodName(method.getName());
								serviceReqUtil.setServerName(serviceInterface.getName());
								serviceReqUtil.setParameterTypes(method.getParameterTypes());
								serviceReqUtil.setValues(args);
								//写数据
								ByteBuffer buffer= ByteBuffer.allocate(1024);
								buffer.put(SerializingUtils.serialize(serviceReqUtil));
								buffer.flip();
								socketChannel.write(buffer);
								buffer.clear();
								socketChannel.shutdownOutput();

								//读数据
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								int len = 0;
								while (true) {
									buffer.clear();
									len = socketChannel.read(buffer);
									if (len == -1)
										break;
									buffer.flip();
									while (buffer.hasRemaining()) {
										bos.write(buffer.get());
									}
								}
								socketChannel.close();
								return SerializingUtils.deserialize(bos.toByteArray());
							} finally {
								if (socket != null)
									socket.close();
								if (output != null)
									output.close();
								if (input != null)
									input.close();
							}
						}
					});
		}
		return null;
	}

	/**
	 * 轮询获取服务端IP
	 * @param list 服务节点
	 * @return
	 */
	public static String getServierInfo(List<String> list){
		String serverName = list.get(reqCount % list.size());
		reqCount++;
		return serverName;
	}
}


