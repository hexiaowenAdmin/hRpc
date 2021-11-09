package com.rpc.nio;

import com.rpc.server.HelloService;
import com.rpc.util.SerializingUtils;
import com.rpc.util.ServiceReqUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NIOClientSocket {

    public static void main(String[] args) throws Exception {
        ServiceReqUtil serviceReqUtil = new ServiceReqUtil();
        Method method = HelloService.class.getMethod("sayHi",String.class);
        serviceReqUtil.setMethodName(method.getName());
        serviceReqUtil.setServerName(HelloService.class.getName());
        serviceReqUtil.setParameterTypes(method.getParameterTypes());
        Object obj [] = new Object[]{"test1"};
        serviceReqUtil.setValues(obj);
        //使用线程模拟用户 并发访问
        for (int i = 0; i < 1; i++) {
            new Thread(){
                @Override
                public void run() {
                    try {
                        //1.创建SocketChannel
                        SocketChannel socketChannel= SocketChannel.open();
                        //2.连接服务器
                        socketChannel.connect(new InetSocketAddress("localhost",60000));
                        //写数据
                       // String msg="我是客户端"+Thread.currentThread().getId();
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
                        String str = (String) SerializingUtils.deserialize(bos.toByteArray());
                        System.out.println("客户端收到:"+str);
                        socketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };
            }.start();
        }
    }
}
