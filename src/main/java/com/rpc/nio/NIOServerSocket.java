package com.rpc.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NIOServerSocket {

    //存储SelectionKey的队列
    private static List<SelectionKey> writeQueue = new ArrayList<SelectionKey>();
    private static Selector selector = null;

    //添加SelectionKey到队列
    public static void addWriteQueue(SelectionKey key){
        synchronized (writeQueue) {
            writeQueue.add(key);
            //唤醒主线程
            selector.wakeup();
        }
    }

    public static void main(String[] args) throws IOException {

        // 1.创建ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 2.绑定端口
        serverSocketChannel.bind(new InetSocketAddress(60000));
        // 3.设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 4.创建通道选择器
        selector = Selector.open();
        /*
         * 5.注册事件类型
         *
         *  sel:通道选择器
         *  ops:事件类型 ==>SelectionKey:包装类，包含事件类型和通道本身。四个常量类型表示四种事件类型
         *  SelectionKey.OP_ACCEPT 获取报文      SelectionKey.OP_CONNECT 连接
         *  SelectionKey.OP_READ 读           SelectionKey.OP_WRITE 写
         */
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            // 6.获取可用I/O通道,获得有多少可用的通道
            int num = selector.select();
            if (num > 0) { // 判断是否存在可用的通道
                // 获得所有的keys
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                // 使用iterator遍历所有的keys
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                // 迭代遍历当前I/O通道
                while (iterator.hasNext()) {
                    // 获得当前key
                    SelectionKey key = iterator.next();
                    // 调用iterator的remove()方法，并不是移除当前I/O通道，标识当前I/O通道已经处理。
                    iterator.remove();
                    // 判断事件类型，做对应的处理
                    if (key.isAcceptable()) {
                        ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = ssChannel.accept();

                        // 获取客户端的数据
                        // 设置非阻塞状态
                        socketChannel.configureBlocking(false);
                        // 注册到selector(通道选择器)
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        System.out.println("读事件");
                        //取消读事件的监控
                        key.cancel();
                        //调用读操作工具类
                        NIOHandler.read(key);
                    } else if (key.isWritable()) {
                        System.out.println("写事件");
                        //取消读事件的监控
                        key.cancel();
                        //调用写操作工具类
                        NIOHandler.write(key);
                    }
                }
            }else{
                synchronized (writeQueue) {
                    while(writeQueue.size() > 0){
                        SelectionKey key = writeQueue.remove(0);
                        //注册写事件
                        SocketChannel channel = (SocketChannel) key.channel();
                        Object attachment = key.attachment();
                        channel.register(selector, SelectionKey.OP_WRITE,attachment);
                    }
                }
            }
        }
    }

}

