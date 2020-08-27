package com.rpc.nio;

import com.rpc.centre.impl.ServiceCenter;
import com.rpc.util.SerializingUtils;
import com.rpc.util.ServiceReqUtil;
import com.rpc.util.ZookeeperUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NIOHandler {

    //构造线程池
    private static ExecutorService executorService  = Executors.newFixedThreadPool(10);

    public static void read(final SelectionKey key){
        //获得线程并执行
        executorService.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    SocketChannel readChannel = (SocketChannel) key.channel();
                    // I/O读数据操作
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int len = 0;
                    while (true) {
                        buffer.clear();
                        len = readChannel.read(buffer);
                        if (len == -1)
                            break;
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            baos.write(buffer.get());
                        }
                    }
                    //将数据添加到key中
                    key.attach(baos);
                    //将注册写操作添加到队列中
                    ServiceCenter.addWriteQueue(key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void write(final SelectionKey key) {
        //拿到线程并执行
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // 写操作
                    SocketChannel writeChannel = (SocketChannel) key.channel();
                    //拿到客户端传递的数据
                    ByteArrayOutputStream attachment = (ByteArrayOutputStream)key.attachment();
                    ServiceReqUtil serviceReqUtil = (ServiceReqUtil) SerializingUtils.deserialize(attachment.toByteArray());

                    String serviceName = serviceReqUtil.getServerName();
                    String methodName = serviceReqUtil.getMethodName();
                    Class<?>[] parameterTypes = serviceReqUtil.getParameterTypes();
                    Object[] arguments = serviceReqUtil.getValues();
                    Class serviceClass = new ZookeeperUtil().getClassNode(serviceName); //通过对象名获取对象
                    if (serviceClass == null) {
                        throw new ClassNotFoundException(serviceName + " not found");
                    }
                    Method method = serviceClass.getMethod(methodName, parameterTypes);
                    Object result = method.invoke(serviceClass.newInstance(), arguments);
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    buffer.put(SerializingUtils.serialize(result));
                    buffer.flip();
                    writeChannel.write(buffer);
                    writeChannel.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
