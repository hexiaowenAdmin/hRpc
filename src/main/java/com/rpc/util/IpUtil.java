package com.rpc.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * ip工具类
 *
 */
public class IpUtil {
	
	public static String getServerIp(){
		String sysType = System.getProperties().getProperty("os.name");
		String ip;
		if(sysType.toLowerCase().startsWith("win")){
			String localIP = null;
			try {
				localIP = InetAddress.getLocalHost().getHostAddress();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(localIP != null){
				return localIP;
			}
		}else{
			ip = getIpByEthNum("eth0");
			if(ip != null){
				return ip;
			}
		}
		return "获取服务器Ip错误";
	}
	
	@SuppressWarnings("unused")
	private  static String getIpByEthNum(String ethNum){
		try {
			Enumeration enumeration = NetworkInterface.getNetworkInterfaces();
			InetAddress ip;
			while(enumeration.hasMoreElements()){
				NetworkInterface networkInterface = (NetworkInterface) enumeration.nextElement();
				if(ethNum.equals(networkInterface)){
					Enumeration addresses = networkInterface.getInetAddresses();
					while(addresses.hasMoreElements()){
						ip = (InetAddress) addresses.nextElement();
						if(ip != null && ip instanceof Inet4Address){
							return ip.getHostAddress();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "获取服务器Ip错误";
	}
}
