package com.rpc.util;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * zookeeper工具类
 *
 */
@SuppressWarnings("all")
public class ZookeeperUtil implements Watcher{
	
	public static final String groupName = "/server"; 
	private static ZooKeeper zk = null;
	
	/**
	 * 连接zookeeper
	 */
	public ZookeeperUtil() {
		if(zk == null){
			try {
				zk = new ZooKeeper("127.0.0.1:2181", 5000,new Watcher() {
				       // 监控所有被触发的事件
				         public void process(WatchedEvent event) {
				        	new ZookeeperUtil();
				       }
				  });
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 创建节点
	 * @param key
	 * @param data
	 */
	public void createNode(String key,Object data){
		if(zk != null){
			try {
				Stat statGroup = zk.exists(key, false);
				if(statGroup ==null){
					byte [] value = data==null?new byte[0]: SerializingUtils.serialize(data);
					String a = zk.create(key, value,Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	/**
	 * 获取节点属性
	 * @param key 
	 * @param flag(如果为true当节点不存在时创建节点)
	 * @return
	 */
	public Object getNode(String key,boolean flag){
		try {
			Stat statGroup = zk.exists(key, false);
			if(statGroup!=null){
				return getChiNode(key);
			}else{
				if(flag){
					createNode(key, 0);
					return getChiNode(key);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
	}
	/**
	 * 获取节点值
	 * @param key
	 * @return
	 */
	public Object getChiNode(String key){
		try {
			byte[] b = zk.getData(key, false, null);
			if(b != null){
				Object obj = SerializingUtils.deserialize(b);
				return  obj;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 重新设置节点值
	 * @param key
	 * @param value
	 */
	public void setNode(String key,Object value){
		try {
			zk.setData(key, SerializingUtils.serialize(value), -1);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	/**
	 * 创建rpc服务存放节点
	 * @param path 服务名
	 * @param impl 实现类
	 * @param port 端口名
	 */
	public void createNode(String path,Class impl,int port){
		if(zk != null){
			try {
				Stat statGroup = zk.exists(groupName, false);
				if(statGroup == null){
					zk.create(groupName, new byte[0],Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				String className = groupName + "/"+path;
				if(zk.exists(className,false) == null){
					zk.create(className, SerializingUtils.serialize(impl),Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				String ipClassName = className+"/"+ IpUtil.getServerIp()+":"+port;
				zk.create(ipClassName,new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 通过接口名获取rpc服务实体类信息
	 * @param className 通过接口名
	 * @return
	 */
	public Class getClassNode(String className){
		String path = groupName+"/"+className;
		try {
			byte [] b = zk.getData(path, false, null);
			if(b != null){
				return (Class) SerializingUtils.deserialize(b);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
	}
	/**
	 * 获取rpc服务接口ip+port
	 * @return
	 */
	public List<String> getIps(){
		List<String> listIp = new ArrayList<String>();
		if(zk!=null){
			try {
				List<String> claasList = zk.getChildren(groupName, true);
				for (String className : claasList) {
					List<String> ipList = zk.getChildren(groupName+"/" + className, true);
					if(ipList != null && ipList.size()>0){
						for (String ip : ipList) {// 类对应ip:port
							listIp.add(ip);
						}
					}else{
						zk.delete(groupName+"/" + className, -1);//如果类下面没有对应ip删除这个类的节点
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		return listIp;
	}

	public void process(WatchedEvent watchedEvent) {
		//
	}
}
