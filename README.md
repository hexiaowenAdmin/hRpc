**自研轻量级RPC项目**  
-----------------------------
**应用技术**:    
  1.通讯使用NIO的IO多路复用  
  2.序列化工具自己用流实现序列化和反序列化com.rpc.util.SerializingUtils  
  3.服务注册与发现使用zookeeper,利用zookeeperk临时节点特性实现服务心跳  
  4.目前负载均衡在客户端兼容了轮询策略  
  5.服务调用,利用jdk反射+jdk动态代理去实现服务调用  

**欢迎各位大佬补充和指导**  